package com.example.parku.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

const val COMPARISON_MIN_SELECTION = 2
const val COMPARISON_MAX_SELECTION = 3

const val COMPARISON_CACHE_CAPACITY = 8
const val COMPARISON_CACHE_LIFETIME_MS = 60_000L

const val COMPARISON_MAX_PARALLEL = 2

private const val UNAVAILABLE = "Unavailable"

data class ComparedParking(
    val parking: Parking,
    val updatedAt: Long? = null,
    val isFromCache: Boolean = false,
    val isUnavailable: Boolean = false,
)

fun comparisonPrice(parking: Parking): String {
    val amount = parking.pricePerMinute
    if (!amount.isFinite() || amount < 0) return UNAVAILABLE
    val text = if (amount == floor(amount)) {
        amount.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", amount)
    }
    return "COP $text"
}

fun comparisonHours(parking: Parking): String {
    val opening = parking.openingTime
    val closing = parking.closingTime
    if (opening == null || opening.length < 5 || closing == null || closing.length < 5) {
        return UNAVAILABLE
    }
    return opening.take(5) + "\u2013" + closing.take(5)
}

fun comparisonSpaces(parking: Parking, vehicleType: SmartVehicleType): String {
    val capacity = when (vehicleType) {
        SmartVehicleType.CAR -> parking.carSpaces
        SmartVehicleType.MOTORCYCLE -> parking.motorcycleSpaces
    }
    if (capacity <= 0) return "Not offered"

    val available = when (vehicleType) {
        SmartVehicleType.CAR -> parking.availableCarSpaces
        SmartVehicleType.MOTORCYCLE -> parking.availableMotorcycleSpaces
    }.coerceIn(0, capacity)

    return "$available of $capacity (est.)"
}

fun comparisonStatus(item: ComparedParking, isOnline: Boolean): String {
    if (item.isUnavailable) return UNAVAILABLE

    val saved = !isOnline || item.isFromCache
    val status = if (saved) "Last available" else "Available"
    val updatedAt = item.updatedAt ?: return status

    return status + "\n" + statusFormatter().format(Date(updatedAt))
}

private fun statusFormatter(): SimpleDateFormat =
    SimpleDateFormat("MMM d, yyyy \u00B7 h:mm a", Locale.US)
