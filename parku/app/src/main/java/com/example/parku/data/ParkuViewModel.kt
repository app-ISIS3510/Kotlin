package com.example.parku.data

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Unico dueno del estado de la app. Sustituye a los datos de ejemplo que antes
 * vivian en ui/data/SampleData.kt: ahora todo sale de Supabase.
 */
class ParkuViewModel(application: Application) : AndroidViewModel(application) {

    private val store = LocalStore(application)
    private val connectivity = ConnectivityObserver(application)
    private val syncMutex = Mutex()

    /** Estado de la red, para el aviso de "sin conexion". */
    var isOnline by mutableStateOf(connectivity.online.value)
        private set

    /** Cuando se confirmo por ultima vez contra Supabase (epoch ms), 0 si nunca. */
    var lastSyncAt by mutableStateOf(0L)
        private set

    /**
     * LruCache en memoria con los parqueaderos ya consultados. La sesion activa se
     * relee cada pocos segundos y su parqueadero casi no cambia, asi que se evita
     * repetir la consulta; al llenarse, sale el menos usado.
     */
    private val parkingMemoryCache = android.util.LruCache<String, Parking>(32)

    /** Cambios hechos sin red que todavia no se han enviado. */
    var pendingCount by mutableStateOf(0)
        private set

    override fun onCleared() {
        connectivity.close()
    }

    var parkingLots by mutableStateOf<List<Parking>>(emptyList())
        private set

    var favorites by mutableStateOf<List<Parking>>(emptyList())
        private set

    var activeSession by mutableStateOf<ParkingSession?>(null)
        private set

    var activeParking by mutableStateOf<Parking?>(null)
        private set

    var searchResults by mutableStateOf<List<Parking>>(emptyList())
        private set

    var userLocation by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    fun updateUserLocation(location: Pair<Double, Double>?) {
        userLocation = location
    }

    val nearestParkingLots: List<Parking>
        get() = userLocation?.let { (latitude, longitude) ->
            nearestParkingLots(parkingLots, latitude, longitude)
        } ?: parkingLots

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * El SDK de Supabase carga la sesion guardada de forma asincrona. Hasta que
     * termina, "no hay sesion" no es una respuesta valida, asi que AuthGate espera.
     */
    var authReady by mutableStateOf(false)
        private set

    var isSignedIn by mutableStateOf(false)
        private set

    var userName by mutableStateOf(AuthRepository.currentUserName().orEmpty())
        private set

    var userEmail by mutableStateOf(AuthRepository.currentUserEmail().orEmpty())
        private set

    var vehicles by mutableStateOf<List<Vehicle>>(emptyList())
        private set

    var authError by mutableStateOf<String?>(null)
        private set

    var isAdmin by mutableStateOf(false)
        private set

    var dashboard by mutableStateOf<DashboardData?>(null)
        private set

    var dashboardLoading by mutableStateOf(false)
        private set

    var dashboardError by mutableStateOf<String?>(null)
        private set

    fun loadDashboard() {
        viewModelScope.launch {
            dashboardLoading = true
            dashboardError = null
            try {
                dashboard = timed("analytics_dashboard") { DashboardRepository.load() }
            } catch (e: Exception) {
                dashboardError = e.message ?: "Could not load the dashboard"
            } finally {
                dashboardLoading = false
            }
        }
    }

    var authBusy by mutableStateOf(false)
        private set

    val hasActiveParking: Boolean get() = activeSession != null

    val selectedVehicle: Vehicle? get() = vehicles.firstOrNull { it.isSelected } ?: vehicles.firstOrNull()

    fun signIn(email: String, password: String) {
        runAuth { AuthRepository.signIn(email, password) }
    }

    fun signUp(fullName: String, email: String, password: String) {
        runAuth { AuthRepository.signUp(fullName, email, password) }
    }

    private fun runAuth(block: suspend () -> Unit) {
        if (!isOnline) {
            authError = "A connection is needed to sign in. Please check your internet."
            return
        }
        viewModelScope.launch {
            authBusy = true
            authError = null
            try {
                block()
                isSignedIn = AuthRepository.currentUserId() != null
                if (isSignedIn) refresh()
            } catch (e: Exception) {
                authError = e.message ?: "Could not sign in"
            } finally {
                authBusy = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { AuthRepository.signOut() }
            store.clearUserData()
            pendingCount = 0
            isSignedIn = false
            isAdmin = false
            dashboard = null
            favorites = emptyList()
            vehicles = emptyList()
            activeSession = null
            activeParking = null
        }
    }

    fun dismissAuthError() {
        authError = null
    }

    fun saveProfile(fullName: String, email: String, onDone: (String) -> Unit = {}) {
        if (!requireConnection("save your profile")) return
        val emailChanged = email.trim() != userEmail
        viewModelScope.launch {
            authBusy = true
            try {
                AuthRepository.updateProfile(fullName, email)
                userName = fullName.trim()
                if (!emailChanged) userEmail = AuthRepository.currentUserEmail().orEmpty()
                onDone(
                    if (emailChanged) {
                        "Name saved. Check your email to confirm the new address."
                    } else {
                        "Profile updated."
                    },
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not update the profile"
            } finally {
                authBusy = false
            }
        }
    }

    /** Matriz de la wiki, "Network only": lo que escribe en el servidor no se confirma sin red. */
    private fun requireConnection(action: String): Boolean {
        if (isOnline) return true
        errorMessage = "You need an internet connection to $action."
        return false
    }

    fun addVehicle(vehicleType: String, plate: String, onDone: () -> Unit = {}) {
        val problem = ProfileValidation.validatePlate(vehicleType, plate)
        if (problem != null) {
            errorMessage = problem
            return
        }
        if (!requireConnection("add a vehicle")) return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { VehicleRepository.addVehicle(vehicleType, plate) }
                reloadVehicles()
                onDone()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = e.message ?: "Could not add the vehicle"
            }
        }
    }

    fun deleteVehicle(vehicle: Vehicle, onDone: () -> Unit = {}) {
        if (!requireConnection("delete a vehicle")) return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { VehicleRepository.deleteVehicle(vehicle.id) }
                reloadVehicles()
                onDone()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = e.message ?: "Could not delete the vehicle"
            }
        }
    }

    val drivingRestriction: DrivingRestriction?
        get() = selectedVehicle?.let { DrivingRestrictionService.check(it) }

    fun selectVehicle(vehicle: Vehicle) {
        if (!requireConnection("change the vehicle")) return

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { VehicleRepository.selectVehicle(vehicle.id) }
                reloadVehicles()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = e.message ?: "Could not change the vehicle"
            }
        }
    }

    /**
     * No se llama en el init: MainNavigationScreen la dispara cuando la app
     * pasa a primer plano, y hacerlo en los dos sitios duplica la consulta.
     *
     * @param silent para los refrescos automaticos: no marca cargando ni avisa
     * de errores de red, para no interrumpir mientras la persona usa la app.
     */
    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                isLoading = true
                errorMessage = null
            }
            try {
                if (!isOnline) return@launch

                // Primero lo pendiente, para no pisar con datos viejos lo que hizo la persona.
                if (store.pendingOps().any { it.type != OP_TRACK }) syncPending()
                val hasPending = store.pendingOps().any { it.type != OP_TRACK }

                isSignedIn = AuthRepository.currentUserId() != null
                userName = AuthRepository.currentUserName().orEmpty()
                userEmail = AuthRepository.currentUserEmail().orEmpty()
                val signedIn = isSignedIn
                val record = !silent

                // Multi-hilo: las consultas son independientes, asi que salen en
                // paralelo en el hilo de IO en vez de una detras de otra.
                coroutineScope {
                    val lots = async(Dispatchers.IO) {
                        timed("parking_list", record) { ParkingRepository.getParkingLots() }
                    }
                    val admin = async(Dispatchers.IO) { signedIn && AdminRepository.isAdmin() }
                    val favs = async(Dispatchers.IO) {
                        if (signedIn) FavoriteRepository.getFavorites() else emptyList()
                    }
                    val vehs = async(Dispatchers.IO) {
                        if (signedIn) {
                            timed("vehicles", record) { VehicleRepository.getVehicles() }
                        } else {
                            emptyList()
                        }
                    }
                    val sess = async(Dispatchers.IO) {
                        if (signedIn) timed("my_parking", record) { fetchActiveSession() } else null
                    }

                    parkingLots = lots.await()
                    parkingLots.forEach { parkingMemoryCache.put(it.id, it) }
                    if (signedIn) {
                        isAdmin = admin.await()
                        favorites = favs.await()
                        val freshVehicles = vehs.await()
                        val freshSession = sess.await()
                        if (!hasPending) {
                            vehicles = freshVehicles
                            activeSession = freshSession?.first
                            activeParking = freshSession?.second
                        }
                    }
                }
                persistAll()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (!silent) errorMessage = e.message ?: "Could not reach the server"
            } finally {
                if (!silent) isLoading = false
            }
        }
    }

    private suspend fun fetchActiveSession(): Pair<ParkingSession?, Parking?> {
        val session = SessionRepository.getActiveSession()
        val parking = session?.let { s ->
            parkingMemoryCache.get(s.parkingId)
                ?: ParkingRepository.getParkingById(s.parkingId)?.also { parkingMemoryCache.put(it.id, it) }
        }
        return session to parking
    }

    // --- Cache local (DataStore) -------------------------------------------

    /** Pinta lo ultimo que se vio, sin esperar a la red. */
    private suspend fun restoreFromCache() {
        val uid = AuthRepository.currentUserId() ?: return
        if (store.owner() != uid) return

        lastSyncAt = store.lastSyncAt()
        store.profile()?.let {
            if (userName.isBlank()) userName = it.name
            if (userEmail.isBlank()) userEmail = it.email
        }
        timed("parking_list", fromCache = true) { store.parkingLots() }?.let { parkingLots = it }
        timed("vehicles", fromCache = true) { store.vehicles() }?.let { vehicles = it }
        timed("my_parking", fromCache = true) { store.session() }?.let {
            activeSession = it.session
            activeParking = it.parking
        }
    }

    private suspend fun persistAll() {
        val uid = AuthRepository.currentUserId() ?: return
        store.saveOwner(uid)
        store.saveProfile(CachedProfile(userName, userEmail))
        store.saveParkingLots(parkingLots)
        store.saveVehicles(vehicles)
        store.saveSession(activeSession?.let { CachedSession(it, activeParking) })
        lastSyncAt = System.currentTimeMillis()
        store.saveLastSyncAt(lastSyncAt)
    }

    private fun persistVehicles() {
        val snapshot = vehicles
        viewModelScope.launch { store.saveVehicles(snapshot) }
    }

    private fun persistSession() {
        val session = activeSession
        val parking = activeParking
        viewModelScope.launch { store.saveSession(session?.let { CachedSession(it, parking) }) }
    }

    private suspend fun reloadVehicles() {
        vehicles = VehicleRepository.getVehicles()
        store.saveVehicles(vehicles)
    }

    fun isFavorite(parking: Parking): Boolean = favorites.any { it.id == parking.id }

    fun toggleFavorite(parking: Parking) {
        // Se refleja de una en la UI y despues se confirma contra el servidor.
        val wasFavorite = isFavorite(parking)
        favorites = if (wasFavorite) {
            favorites.filterNot { it.id == parking.id }
        } else {
            favorites + parking
        }

        viewModelScope.launch {
            try {
                if (wasFavorite) {
                    FavoriteRepository.removeFavorite(parking.id)
                    track("favorite_removed", screen = "favorites", parkingId = parking.id)
                } else {
                    FavoriteRepository.addFavorite(parking.id)
                    track("favorite_added", screen = "parking_details", parkingId = parking.id)
                }
                favorites = FavoriteRepository.getFavorites()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not update favorites"
                favorites = runCatching { FavoriteRepository.getFavorites() }.getOrDefault(favorites)
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            try {
                searchResults = ParkingRepository.searchParkingLots(query)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Search failed"
                searchResults = emptyList()
            }
        }
    }

    fun startParking(parking: Parking, pickupLabel: String, onDone: () -> Unit = {}) {
        val vehicle = selectedVehicle
        if (vehicle == null) {
            errorMessage = "Add a vehicle before starting a parking session"
            return
        }
        // La disponibilidad se valida en el servidor (RPC), asi que aqui si hace falta red.
        if (!requireConnection("start a parking session")) return

        viewModelScope.launch {
            try {
                // EC-05: antes de crear, se mira si el servidor ya tiene una sesion activa
                // (por ejemplo, si un intento anterior llego pero la respuesta se perdio).
                val existing = withContext(Dispatchers.IO) { fetchActiveSession() }
                // Solo se adopta si es de ESTE parqueadero; si no, decide el servidor.
                val alreadyCreated = existing.first?.takeIf { it.parkingId == parking.id }
                val session = alreadyCreated ?: withContext(Dispatchers.IO) {
                    SessionRepository.createSession(
                        parkingId = parking.id,
                        pickupTimeIso = pickupLabelToIso(pickupLabel),
                        vehicleId = vehicle.id,
                    )
                }
                activeSession = session
                activeParking = parking
                persistSession()
                track("parking_started", screen = "pickup_time", parkingId = parking.id)
                onDone()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // La pantalla conserva la seleccion (no se llama a onDone) para poder reintentar.
                errorMessage = if (isNetworkFailure(e)) {
                    "Connection lost. Your parking session is not confirmed. Please try again."
                } else {
                    e.message ?: "Could not start parking"
                }
            }
        }
    }

    fun changePickupTime(pickupLabel: String, onDone: () -> Unit = {}) {
        val session = activeSession ?: return
        // Sin red no se llama a onDone: la pantalla se queda con la hora elegida
        // y no se afirma que el servidor la acepto.
        if (!requireConnection("change the pickup time")) return

        viewModelScope.launch {
            try {
                activeSession = withContext(Dispatchers.IO) {
                    SessionRepository.updatePickupTime(
                        sessionId = session.id,
                        pickupTimeIso = pickupLabelToIso(pickupLabel),
                    )
                }
                persistSession()
                track(
                    "pickup_time_changed",
                    screen = "change_pickup_time",
                    parkingId = session.parkingId,
                )
                onDone()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = if (isNetworkFailure(e)) {
                    "Connection lost. Your new pickup time was not saved."
                } else {
                    e.message ?: "Could not update the pickup time"
                }
            }
        }
    }

    var endingParking by mutableStateOf(false)
        private set

    fun endParking(onDone: () -> Unit = {}) {
        val session = activeSession ?: return
        // La sesion sigue activa hasta que el servidor confirme que termino.
        if (!requireConnection("end your parking session")) return

        viewModelScope.launch {
            endingParking = true
            try {
                withContext(Dispatchers.IO) { SessionRepository.endSession(session.id, nowIso()) }
                track("parking_ended", screen = "end_parking", parkingId = session.parkingId)
                activeSession = null
                activeParking = null
                persistSession()
                onDone()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                errorMessage = if (isNetworkFailure(e)) {
                    "Connection lost. Your session is still active until the server confirms it ended."
                } else {
                    e.message ?: "Could not end parking"
                }
            } finally {
                endingParking = false
            }
        }
    }

    fun trackDetailViewed(parkingId: String) {
        track("parking_detail_viewed", screen = "parking_detail", parkingId = parkingId)
    }

    fun trackNavigationOpened(parkingId: String) {
        track("navigation_opened", screen = "parking_detail", parkingId = parkingId)
    }

    fun dismissError() {
        errorMessage = null
    }

    /**
     * La analitica nunca debe tumbar la pantalla. Sin red, el evento se guarda en
     * la cola y se envia al volver la conexion, para no perder datos de las BQs.
     */
    private fun track(eventType: String, screen: String?, parkingId: String?) {
        viewModelScope.launch {
            val op = newOp(OP_TRACK, eventType = eventType, screen = screen, parkingId = parkingId)
            if (isOnline) {
                try {
                    withContext(Dispatchers.IO) { PendingSync.run(op) }
                    return@launch
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    if (!isNetworkFailure(e)) return@launch
                }
            }
            enqueue(op)
        }
    }

    // --- Conectividad eventual: cola de operaciones ------------------------

    private fun newOp(
        type: String,
        sessionId: String? = null,
        vehicleId: String? = null,
        vehicleType: String? = null,
        plate: String? = null,
        timeIso: String? = null,
        eventType: String? = null,
        screen: String? = null,
        parkingId: String? = null,
    ) = PendingOp(
        id = UUID.randomUUID().toString(),
        type = type,
        sessionId = sessionId,
        vehicleId = vehicleId,
        vehicleType = vehicleType,
        plate = plate,
        timeIso = timeIso,
        eventType = eventType,
        screen = screen,
        parkingId = parkingId,
    )

    private suspend fun enqueue(op: PendingOp) {
        store.updatePending { it + op }
        refreshPendingCount()
    }

    private suspend fun refreshPendingCount() {
        // Los eventos de analitica no se le muestran a la persona.
        pendingCount = store.pendingOps().count { it.type != OP_TRACK }
    }

    /** Envia la cola en orden, en el hilo de IO, y de a una persona a la vez (Mutex). */
    private suspend fun syncPending() {
        if (AuthRepository.currentUserId() == null) return
        syncMutex.withLock {
            withContext(Dispatchers.IO) {
                for (op in store.pendingOps()) {
                    try {
                        PendingSync.run(op)
                        store.updatePending { l -> l.filterNot { it.id == op.id } }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        // Sin red se corta y se reintenta cuando vuelva.
                        if (isNetworkFailure(e)) break
                        // Si el servidor la rechaza, se reintenta un par de veces y se descarta.
                        store.updatePending { l ->
                            l.mapNotNull {
                                when {
                                    it.id != op.id -> it
                                    it.attempts >= 2 -> null
                                    else -> it.copy(attempts = it.attempts + 1)
                                }
                            }
                        }
                    }
                }
            }
        }
        refreshPendingCount()
        flushPerf()
    }

    // --- BQ2: tiempos de carga por pantalla --------------------------------

    private val perfMutex = Mutex()

    private suspend fun <T> timed(
        screen: String,
        record: Boolean = true,
        fromCache: Boolean = false,
        block: suspend () -> T,
    ): T {
        val start = SystemClock.elapsedRealtime()
        val result = block()
        if (record) recordLoad(screen, (SystemClock.elapsedRealtime() - start).toInt(), fromCache)
        return result
    }

    private fun recordLoad(screen: String, loadMs: Int, fromCache: Boolean) {
        viewModelScope.launch {
            store.updatePerf {
                it + PerfSample(
                    id = UUID.randomUUID().toString(),
                    screen = screen,
                    loadMs = loadMs,
                    deviceModel = PerfRepository.deviceModel,
                    osVersion = PerfRepository.osVersion,
                    fromCache = fromCache,
                )
            }
            flushPerf()
        }
    }

    private suspend fun flushPerf() {
        if (!isOnline || AuthRepository.currentUserId() == null) return
        perfMutex.withLock {
            val batch = store.perfSamples()
            if (batch.isEmpty()) return
            val sent = runCatching { withContext(Dispatchers.IO) { PerfRepository.send(batch) } }
            sent.exceptionOrNull()?.let { android.util.Log.w("ParkU", "Could not send load times", it) }
            if (sent.isSuccess) {
                val ids = batch.map { it.id }.toSet()
                store.updatePerf { l -> l.filterNot { it.id in ids } }
            }
        }
    }

    // El init va al final: las coroutinas arrancan de inmediato en el hilo principal
    // y todas las propiedades de arriba tienen que estar inicializadas antes.
    init {
        // 1) Cache primero: la app pinta lo ultimo que vio antes de pedir nada a la red.
        //    Y si habia cambios pendientes de la sesion anterior, se envian ya.
        viewModelScope.launch {
            // Espera (maximo 5 s) a que el SDK cargue la sesion guardada; funciona sin red.
            withTimeoutOrNull(5_000) { runCatching { supabase.auth.awaitInitialization() } }
            isSignedIn = AuthRepository.currentUserId() != null
            userName = AuthRepository.currentUserName().orEmpty()
            userEmail = AuthRepository.currentUserEmail().orEmpty()
            authReady = true

            restoreFromCache()
            refreshPendingCount()
            if (isOnline) syncPending()
        }
        // 2) Cuando la red VUELVE (no en el primer valor) se envia lo pendiente y se refresca.
        viewModelScope.launch {
            var wasOnline = connectivity.online.value
            connectivity.online.collect { online ->
                isOnline = online
                if (online && !wasOnline) {
                    syncPending()
                    refresh(silent = true)
                }
                wasOnline = online
            }
        }
    }
}
