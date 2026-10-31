package com.example.parku.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine


object LocationProvider {

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

    /** Ultima posicion conocida por el telefono; sirve cuando no se logra una lectura nueva. */
    @SuppressLint("MissingPermission")
    suspend fun lastLocation(context: Context): Pair<Double, Double>? =
        suspendCancellableCoroutine { continuation ->
            runCatching {
                LocationServices.getFusedLocationProviderClient(context)
                    .lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location?.let { it.latitude to it.longitude })
                    }
                    .addOnFailureListener { continuation.resume(null) }
            }.onFailure { continuation.resume(null) }
        }

    /** Metros entre dos puntos, igual que Geolocator.distanceBetween. */
    fun failureReason(context: Context): String {
        val granted = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ).any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!granted) return "Location permission is off."

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null || !LocationManagerCompat.isLocationEnabled(manager)) {
            return "Location services are off."
        }

        return "Could not get your location."
    }

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
