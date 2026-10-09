package com.example.parku.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine


object LocationProvider {

    /** Devuelve (latitud, longitud) o null si no hay permiso o no se pudo leer. */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(context: Context): Pair<Double, Double>? =
        suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationTokenSource()

            runCatching {
                LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
                    .addOnSuccessListener { location ->
                        continuation.resume(location?.let { it.latitude to it.longitude })
                    }
                    .addOnFailureListener { continuation.resume(null) }
            }.onFailure { continuation.resume(null) }

            continuation.invokeOnCancellation { cancellation.cancel() }
        }

    /** Metros entre dos puntos, igual que Geolocator.distanceBetween. */
    fun distanceBetween(
        fromLatitude: Double,
        fromLongitude: Double,
        toLatitude: Double,
        toLongitude: Double,
    ): Float {
        val result = FloatArray(1)
        Location.distanceBetween(fromLatitude, fromLongitude, toLatitude, toLongitude, result)
        return result[0]
    }
}

/**
 * Los parqueaderos con coordenadas, ordenados del mas cercano al mas lejano.
 * Espejo de ParkingService.getNearestParkingLots.
 */
fun nearestParkingLots(
    parkingLots: List<Parking>,
    userLatitude: Double,
    userLongitude: Double,
    limit: Int = 4,
): List<Parking> =
    parkingLots
        .filter { it.latitude != null && it.longitude != null }
        .sortedBy {
            LocationProvider.distanceBetween(
                userLatitude,
                userLongitude,
                it.latitude!!,
                it.longitude!!,
            )
        }
        .take(limit)
