package com.example.parku.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Espejo de lib/models/parking.dart. Los nombres en snake_case son los de la
 * vista `parking_lots_with_availability` de Supabase.
 */
@Serializable
data class Parking(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("car_spaces") val carSpaces: Int = 0,
    @SerialName("motorcycle_spaces") val motorcycleSpaces: Int = 0,
    @SerialName("price_per_minute") val pricePerMinute: Double = 0.0,
    @SerialName("opening_time") val openingTime: String? = null,
    @SerialName("closing_time") val closingTime: String? = null,
    // La tabla base `parking_lots` no trae estas dos columnas, solo la vista.
    // Igual que en Dart, cuando faltan se cae al total de cupos.
    @SerialName("available_car_spaces") val availableCarSpacesOrNull: Int? = null,
    @SerialName("available_motorcycle_spaces") val availableMotorcycleSpacesOrNull: Int? = null,
) {
    val availableCarSpaces: Int get() = availableCarSpacesOrNull ?: carSpaces

    val availableMotorcycleSpaces: Int get() = availableMotorcycleSpacesOrNull ?: motorcycleSpaces

    /** "05:30 - 20:00". La base devuelve "05:30:00", asi que se cortan los segundos. */
    val hours: String
        get() {
            val open = openingTime?.take(5).orEmpty()
            val close = closingTime?.take(5).orEmpty()
            return if (open.isEmpty() && close.isEmpty()) "" else "$open - $close"
        }

    /** Mismo criterio que search_results.dart en Flutter. */
    val type: String
        get() = when {
            availableCarSpaces > 0 && availableMotorcycleSpaces > 0 -> "Cars and motorcycles"
            availableCarSpaces > 0 -> "Cars"
            availableMotorcycleSpaces > 0 -> "Motorcycles"
            else -> "No spaces available"
        }

    /** "$120 min", como lo arma parking_detail.dart. */
    val priceLabel: String
        get() {
            val value = if (pricePerMinute % 1.0 == 0.0) {
                pricePerMinute.toLong().toString()
            } else {
                pricePerMinute.toString()
            }
            return "$$value min"
        }
}

/** Espejo de lib/models/parking_session.dart. */
@Serializable
data class ParkingSession(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("parking_id") val parkingId: String,
    @SerialName("pickup_time") val pickupTime: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String? = null,
    val status: String,
)
