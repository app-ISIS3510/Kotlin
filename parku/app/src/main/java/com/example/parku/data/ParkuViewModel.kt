package com.example.parku.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Unico dueno del estado de la app. Sustituye a los datos de ejemplo que antes
 * vivian en ui/data/SampleData.kt: ahora todo sale de Supabase.
 */
class ParkuViewModel : ViewModel() {

    var parkingLots by mutableStateOf<List<Parking>>(emptyList())
        private set

    var favorites by mutableStateOf<List<Parking>>(emptyList())
        private set

    var activeSession by mutableStateOf<ParkingSession?>(null)
        private set

    /** El parqueadero de la sesion activa, para poder mostrar nombre y direccion. */
    var activeParking by mutableStateOf<Parking?>(null)
        private set

    var searchResults by mutableStateOf<List<Parking>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val hasActiveParking: Boolean get() = activeSession != null

    /**
     * La primera carga y las siguientes las dispara MainNavigationScreen cuando
     * la app pasa a primer plano, asi que aqui no se llama en el init: eso
     * duplicaria la consulta al arrancar.
     */
    /**
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
                parkingLots = ParkingRepository.getParkingLots()
                favorites = FavoriteRepository.getFavorites()
                loadActiveSession()
            } catch (e: Exception) {
                if (!silent) errorMessage = e.message ?: "Could not reach the server"
            } finally {
                if (!silent) isLoading = false
            }
        }
    }

    private suspend fun loadActiveSession() {
        val session = SessionRepository.getActiveSession()
        activeSession = session
        activeParking = session?.let { ParkingRepository.getParkingById(it.parkingId) }
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
        viewModelScope.launch {
            try {
                val session = SessionRepository.createSession(
                    parkingId = parking.id,
                    pickupTimeIso = pickupLabelToIso(pickupLabel),
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
                onDone()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not update the pickup time"
            }
        }
    }

    fun endParking() {
        val session = activeSession ?: return
        viewModelScope.launch {
            try {
                SessionRepository.endSession(session.id, nowIso())
                activeSession = null
                activeParking = null
            } catch (e: Exception) {
                errorMessage = e.message ?: "Could not end parking"
            }
        }
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

private fun isoFormatter(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

fun nowIso(): String = isoFormatter().format(Date())

/**
 * "4:00" -> hoy a las 16:00 locales, en UTC. Las opciones de la maqueta
 * (3:30, 4:00, 4:30) son todas PM.
 */
fun pickupLabelToIso(label: String): String {
    val parts = label.split(":")
    var hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 12
    val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    if (hour < 12) hour += 12

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return isoFormatter().format(calendar.time)
}

/** "2026-09-28T21:00:00Z" -> "4:00", para volver a la etiqueta que muestra la UI. */
fun isoToPickupLabel(iso: String): String {
    val parsed = runCatching { isoFormatter().parse(iso) }.getOrNull()
        ?: runCatching {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(iso)
        }.getOrNull()
        ?: return "4:00"

    val calendar = Calendar.getInstance().apply { time = parsed }
    val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val minute = calendar.get(Calendar.MINUTE)

    return "$hour12:${minute.toString().padStart(2, '0')}"
}
