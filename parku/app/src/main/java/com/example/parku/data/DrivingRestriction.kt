package com.example.parku.data

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Espejo de lib/services/driving_restriction_service.dart.
 *
 * Bogota usa UTC-5 todo el año, asi que el calculo no depende de la zona
 * horaria que tenga configurada el telefono.
 */
data class DrivingRestriction(
    /** null cuando no se puede determinar (año fuera del calendario o placa rara). */
    val restricted: Boolean?,
    val hours: String? = null,
    val explanation: String? = null,
    val dateLabel: String,
)

object DrivingRestrictionService {

    const val OFFICIAL_URL = "https://www.movilidadbogota.gov.co/pico-y-placa"

    // Calendario 2026. Mantener al dia con la Secretaria de Movilidad.
    private val holidays2026 = setOf(
        "01-01", "01-12", "03-23", "04-02", "04-03", "05-01", "05-18",
        "06-08", "06-15", "06-29", "07-20", "08-07", "08-17", "10-12",
        "11-02", "11-16", "12-08", "12-25",
    )

    private val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    fun check(vehicle: Vehicle): DrivingRestriction {
        val bogota = Calendar.getInstance(TimeZone.getTimeZone("GMT-5"), Locale.US)

        val year = bogota.get(Calendar.YEAR)
        val month = bogota.get(Calendar.MONTH) + 1
        val day = bogota.get(Calendar.DAY_OF_MONTH)

        // Calendar.MONDAY vale 2, asi que se normaliza a 1..7 empezando en lunes.
        val weekday = (bogota.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1

        val dateLabel = "Bogotá · ${dayNames[weekday - 1]}, ${monthNames[month - 1]} $day"
        val dayKey = "%02d-%02d".format(month, day)

        if (year != 2026) {
            return DrivingRestriction(
                restricted = null,
                explanation = "Check the updated calendar on the official website.",
                dateLabel = dateLabel,
            )
        }

        // Dia sin carro y sin moto de 2026.
        if (dayKey == "02-05") {
            return DrivingRestriction(
                restricted = true,
                hours = "5:00 AM–9:00 PM",
                explanation = "Car-free and motorcycle-free day.",
                dateLabel = dateLabel,
            )
        }

        if (vehicle.vehicleType == "motorcycle" || weekday > 5 || dayKey in holidays2026) {
            return DrivingRestriction(restricted = false, dateLabel = dateLabel)
        }

        val lastDigit = vehicle.plate.lastOrNull()?.digitToIntOrNull()
            ?: return DrivingRestriction(restricted = null, dateLabel = dateLabel)

        val firstGroup = lastDigit in 1..5
        val restricted = if (day % 2 == 0) firstGroup else !firstGroup

        return DrivingRestriction(
            restricted = restricted,
            hours = if (restricted) "6:00 AM–9:00 PM" else null,
            dateLabel = dateLabel,
        )
    }
}
