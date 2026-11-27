package com.example.parku.data

import android.content.Context

class SessionSummaryStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("session_summary", Context.MODE_PRIVATE)

    fun save(summary: SessionSummary) {
        prefs.edit()
            .putString(SESSION_ID, summary.sessionId)
            .putString(PARKING_NAME, summary.parkingName)
            .putString(PARKING_ADDRESS, summary.parkingAddress)
            .putString(VEHICLE_TYPE, summary.vehicleType)
            .putString(PLATE, summary.plate)
            .putString(STARTED_AT, summary.startedAt)
            .putString(ENDED_AT, summary.endedAt)
            .putInt(MINUTES, summary.minutes)
            .putString(TOTAL, summary.total.toString())
            .apply()
    }

    fun load(): SessionSummary? {
        val sessionId = prefs.getString(SESSION_ID, null) ?: return null
        return SessionSummary(
            sessionId = sessionId,
            parkingName = prefs.getString(PARKING_NAME, null) ?: return null,
            parkingAddress = prefs.getString(PARKING_ADDRESS, "").orEmpty(),
            vehicleType = prefs.getString(VEHICLE_TYPE, "car").orEmpty(),
            plate = prefs.getString(PLATE, null),
            startedAt = prefs.getString(STARTED_AT, null) ?: return null,
            endedAt = prefs.getString(ENDED_AT, null) ?: return null,
            minutes = prefs.getInt(MINUTES, 0),
            total = prefs.getString(TOTAL, null)?.toDoubleOrNull() ?: 0.0,
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val SESSION_ID = "session_id"
        const val PARKING_NAME = "parking_name"
        const val PARKING_ADDRESS = "parking_address"
        const val VEHICLE_TYPE = "vehicle_type"
        const val PLATE = "plate"
        const val STARTED_AT = "started_at"
        const val ENDED_AT = "ended_at"
        const val MINUTES = "minutes"
        const val TOTAL = "total"
    }
}
