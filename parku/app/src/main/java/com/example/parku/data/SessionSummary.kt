package com.example.parku.data

import java.util.Locale

data class SessionSummary(
    val sessionId: String,
    val parkingId: String,
    val vehicleId: String? = null,
    val parkingName: String,
    val parkingAddress: String,
    val vehicleType: String,
    val plate: String? = null,
    val startedAt: String,
    val endedAt: String,
    val minutes: Int,
    val total: Double,
) {
    val vehicleLabel: String get() = if (vehicleType == "motorcycle") "Motorcycle" else "Car"

    val totalLabel: String get() = "COP " + String.format(Locale.US, "%,.0f", total)

    val durationLabel: String
        get() {
            val hours = minutes / 60
            val rest = minutes % 60
            return if (hours > 0) "${hours}h ${rest}min" else "${rest}min"
        }
}

fun buildSessionSummary(session: ParkingSession, parking: Parking?, endedAt: String): SessionSummary {
    val minutes = minutesBetween(session.startedAt, endedAt)
    return SessionSummary(
        sessionId = session.id,
        parkingId = session.parkingId,
        vehicleId = session.vehicleId,
        parkingName = parking?.name ?: "Parking lot",
        parkingAddress = parking?.address.orEmpty(),
        vehicleType = session.vehicleType,
        plate = session.vehiclePlate,
        startedAt = session.startedAt,
        endedAt = endedAt,
        minutes = minutes,
        total = minutes * (parking?.pricePerMinute ?: 0.0),
    )
}
