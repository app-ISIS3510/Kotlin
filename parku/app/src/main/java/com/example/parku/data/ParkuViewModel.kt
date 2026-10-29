package com.example.parku.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class ParkuViewModel : ViewModel() {

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

    var isSignedIn by mutableStateOf(AuthRepository.currentUserId() != null)
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
                dashboard = DashboardRepository.load()
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

    fun addVehicle(vehicleType: String, plate: String, onDone: () -> Unit = {}) {
        val problem = ProfileValidation.validatePlate(vehicleType, plate)
        if (problem != null) {
            errorMessage = problem
            return
        }

        viewModelScope.launch {
            try {
                VehicleRepository.addVehicle(vehicleType, plate)
                vehicles = VehicleRepository.getVehicles()
                onDone()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not add the vehicle"
            }
        }
    }

    fun deleteVehicle(vehicle: Vehicle, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                VehicleRepository.deleteVehicle(vehicle.id)
                vehicles = VehicleRepository.getVehicles()
                onDone()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not delete the vehicle"
            }
        }
    }

    val drivingRestriction: DrivingRestriction?
        get() = selectedVehicle?.let { DrivingRestrictionService.check(it) }

    fun selectVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                VehicleRepository.selectVehicle(vehicle.id)
                vehicles = VehicleRepository.getVehicles()
            } catch (e: Exception) {
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
                isSignedIn = AuthRepository.currentUserId() != null
                userName = AuthRepository.currentUserName().orEmpty()
                userEmail = AuthRepository.currentUserEmail().orEmpty()

                coroutineScope {
                    // Independent calls: the load costs the slowest one, not the sum.
                    val parkingLotsCall = async { ParkingRepository.getParkingLots() }

                    if (!isSignedIn) {
                        parkingLots = parkingLotsCall.await()
                        return@coroutineScope
                    }

                    val isAdminCall = async { AdminRepository.isAdmin() }
                    val favoritesCall = async { FavoriteRepository.getFavorites() }
                    val vehiclesCall = async { VehicleRepository.getVehicles() }
                    val activeSessionCall = async { loadActiveSession() }

                    // Await first: assigning between awaits would let Compose
                    // draw a half-updated screen.
                    val lots = parkingLotsCall.await()
                    val admin = isAdminCall.await()
                    val favorited = favoritesCall.await()
                    val registered = vehiclesCall.await()
                    val (session, parking) = activeSessionCall.await()

                    // Apply together, with no suspension in between.
                    parkingLots = lots
                    isAdmin = admin
                    favorites = favorited
                    vehicles = registered
                    activeSession = session
                    activeParking = parking
                }
            } catch (e: Exception) {
                if (!silent) errorMessage = e.message ?: "Could not reach the server"
            } finally {
                if (!silent) isLoading = false
            }
        }
    }

    /** Returns the state instead of writing it, so the caller decides when to apply it. */
    private suspend fun loadActiveSession(): Pair<ParkingSession?, Parking?> {
        val session = SessionRepository.getActiveSession() ?: return null to null
        return session to ParkingRepository.getParkingById(session.parkingId)
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

        viewModelScope.launch {
            try {
                val session = SessionRepository.createSession(
                    parkingId = parking.id,
                    pickupTimeIso = pickupLabelToIso(pickupLabel),
                    vehicleId = vehicle.id,
                )
                activeSession = session
                activeParking = parking
                track("parking_started", screen = "pickup_time", parkingId = parking.id)
                onDone()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not start parking"
            }
        }
    }

    fun changePickupTime(pickupLabel: String, onDone: () -> Unit = {}) {
        val session = activeSession ?: return
        viewModelScope.launch {
            try {
                activeSession = SessionRepository.updatePickupTime(
                    sessionId = session.id,
                    pickupTimeIso = pickupLabelToIso(pickupLabel),
                )
                track(
                    "pickup_time_changed",
                    screen = "change_pickup_time",
                    parkingId = session.parkingId,
                )
                onDone()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not update the pickup time"
            }
        }
    }

    var endingParking by mutableStateOf(false)
        private set

    fun endParking(onDone: () -> Unit = {}) {
        val session = activeSession ?: return
        viewModelScope.launch {
            endingParking = true
            try {
                SessionRepository.endSession(session.id, nowIso())
                track("parking_ended", screen = "end_parking", parkingId = session.parkingId)
                activeSession = null
                activeParking = null
                onDone()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not end parking"
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

    private fun track(eventType: String, screen: String?, parkingId: String?) {
        viewModelScope.launch {
            // La analitica nunca debe tumbar la pantalla.
            runCatching { AnalyticsRepository.trackEvent(eventType, screen, parkingId) }
        }
    }
}
