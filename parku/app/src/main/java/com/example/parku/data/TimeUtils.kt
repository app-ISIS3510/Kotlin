package com.example.parku.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Espejo de la logica de horas de lib/screens/pickup_time.dart y del contador
 * de lib/screens/my_parking.dart. Todo se apoya en el reloj del dispositivo.
 *
 * Las etiquetas de hora viajan en formato "HH:mm" de 24 horas, igual que en
 * Flutter, y solo se convierten a "4:00 PM" para mostrarlas.
 */

private fun isoFormatter(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

fun nowIso(): String = isoFormatter().format(Date())

/** "HH:mm" de hoy, en hora local, convertido a UTC para guardarlo. */
fun pickupLabelToIso(label: String): String {
    val parts = label.split(":")
    val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 12
    val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return isoFormatter().format(calendar.time)
}

/** Lee la marca de tiempo que devuelve Supabase, con o sin fraccion de segundo. */
private fun parseIso(iso: String): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    )

    for (pattern in patterns) {
        val parsed = runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                if (pattern.endsWith("'Z'")) timeZone = TimeZone.getTimeZone("UTC")
            }.parse(iso)
        }.getOrNull()

        if (parsed != null) return parsed
    }

    return null
}

/** De la marca guardada a la etiqueta "HH:mm" en hora local. */
fun isoToPickupLabel(iso: String): String {
    val parsed = parseIso(iso) ?: return "12:00"

    val calendar = Calendar.getInstance().apply { time = parsed }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    return "%02d:%02d".format(hour, minute)
}

/** "16:00" -> "4:00 PM". */
fun formatTime12h(label: String): String {
    val parts = label.split(":")
    val hour24 = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return label
    val minute = parts.getOrNull(1)?.trim() ?: "00"

    val period = if (hour24 >= 12) "PM" else "AM"
    val hour12 = (hour24 % 12).let { if (it == 0) 12 else it }

    return "$hour12:$minute $period"
}

/** Sube al siguiente :00 o :30. */
private fun roundToNext30Minutes(calendar: Calendar): Calendar {
    val rounded = calendar.clone() as Calendar
    rounded.set(Calendar.SECOND, 0)
    rounded.set(Calendar.MILLISECOND, 0)

    if (rounded.get(Calendar.MINUTE) < 30) {
        rounded.set(Calendar.MINUTE, 30)
    } else {
        rounded.set(Calendar.MINUTE, 0)
        rounded.add(Calendar.HOUR_OF_DAY, 1)
    }

    return rounded
}

private fun parkingTimeToday(time: String, fallbackHour: Int, fallbackMinute: Int): Calendar {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: fallbackHour
    val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: fallbackMinute

    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

/**
 * Horas posibles de hoy, cada 30 minutos, desde la siguiente media hora hasta
 * el cierre del parqueadero. Las que ya pasaron no aparecen.
 */
fun generateAvailableTimes(openingTime: String?, closingTime: String?): List<String> {
    val opening = parkingTimeToday(openingTime.orEmpty(), 0, 0)
    val closing = parkingTimeToday(closingTime.orEmpty(), 23, 59)

    var current = roundToNext30Minutes(Calendar.getInstance())
    if (current.before(opening)) current = opening.clone() as Calendar

    if (current.after(closing)) return emptyList()

    val times = mutableListOf<String>()
    while (!current.after(closing)) {
        times += "%02d:%02d".format(
            current.get(Calendar.HOUR_OF_DAY),
            current.get(Calendar.MINUTE),
        )
        current.add(Calendar.MINUTE, 30)
    }

    return times
}

/** Lo que falta para la recogida, en horas y minutos. Nunca baja de cero. */
fun remainingUntil(pickupIso: String): Pair<Int, Int> {
    val pickup = parseIso(pickupIso) ?: return 0 to 0
    val millis = pickup.time - System.currentTimeMillis()

    if (millis <= 0) return 0 to 0

    val totalMinutes = millis / 60_000
    return (totalMinutes / 60).toInt() to (totalMinutes % 60).toInt()
}
