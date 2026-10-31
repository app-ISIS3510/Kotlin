package com.example.parku.data

import java.util.Locale
import kotlin.math.roundToInt

enum class SmartVehicleType { CAR, MOTORCYCLE }

val SMART_RADIUS_OPTIONS = listOf(500, 1000, 2000)

data class SmartParkingResult(
    val parking: Parking,
    val distanceMeters: Float?,
    val availabilityPercentage: Double,
    val availableSpaces: Int,
    val totalSpaces: Int,
    val isRecommended: Boolean,
) {
    val formattedDistance: String?
        get() = distanceMeters?.let {
            if (it < 1000f) {
                "${it.roundToInt()} m away"
            } else {
                String.format(Locale.US, "%.2f km away", it / 1000f)
            }
        }

    val availabilityLabel: String
        get() = String.format(Locale.US, "%.1f%% available", availabilityPercentage)

    val spacesLabel: String
        get() = "$availableSpaces of $totalSpaces spaces available"
}

data class SmartParkingResults(
    val items: List<SmartParkingResult> = emptyList(),
    val outsideRadius: Boolean = false,
)

private fun collect(
    parkingLots: List<Parking>,
    userLocation: Pair<Double, Double>?,
    radiusMeters: Int?,
    vehicleType: SmartVehicleType,
): List<SmartParkingResult> = parkingLots.mapNotNull { parking ->
    val total = when (vehicleType) {
        SmartVehicleType.CAR -> parking.carSpaces
        SmartVehicleType.MOTORCYCLE -> parking.motorcycleSpaces
    }
    if (total <= 0) return@mapNotNull null

    val distance = if (userLocation == null) {
        null
    } else {
        val latitude = parking.latitude ?: return@mapNotNull null
        val longitude = parking.longitude ?: return@mapNotNull null
        val meters = LocationProvider.distanceBetween(
            userLocation.first,
            userLocation.second,
            latitude,
            longitude,
        )
        if (!meters.isFinite()) return@mapNotNull null
        if (radiusMeters != null && meters > radiusMeters) return@mapNotNull null
        meters
    }

    val available = when (vehicleType) {
        SmartVehicleType.CAR -> parking.availableCarSpaces
        SmartVehicleType.MOTORCYCLE -> parking.availableMotorcycleSpaces
    }.coerceIn(0, total)

    SmartParkingResult(
        parking = parking,
        distanceMeters = distance,
        availabilityPercentage = available * 100.0 / total,
        availableSpaces = available,
        totalSpaces = total,
        isRecommended = false,
    )
}

private fun rank(
    results: List<SmartParkingResult>,
    userLocation: Pair<Double, Double>?,
    recommend: Boolean,
): List<SmartParkingResult> {
    val sorted = if (userLocation == null) {
        results.sortedBy { it.parking.name }
    } else {
        results.sortedWith(
            compareByDescending<SmartParkingResult> { it.availabilityPercentage }
                .thenBy { it.distanceMeters ?: Float.MAX_VALUE }
                .thenBy { it.parking.id },
        )
    }

    return sorted.mapIndexed { index, result ->
        result.copy(
            isRecommended = recommend &&
                userLocation != null &&
                index == 0 &&
                result.availableSpaces > 0,
        )
    }
}

fun smartParkingResults(
    parkingLots: List<Parking>,
    userLocation: Pair<Double, Double>?,
    radiusMeters: Int,
    vehicleType: SmartVehicleType,
): SmartParkingResults {
    if (parkingLots.isEmpty()) return SmartParkingResults()

    val withinRadius = collect(parkingLots, userLocation, radiusMeters, vehicleType)

    if (withinRadius.isNotEmpty() || userLocation == null) {
        return SmartParkingResults(items = rank(withinRadius, userLocation, recommend = true))
    }

    val anyDistance = collect(parkingLots, userLocation, null, vehicleType)

    return SmartParkingResults(
        items = rank(anyDistance, userLocation, recommend = false),
        outsideRadius = anyDistance.isNotEmpty(),
    )
}
