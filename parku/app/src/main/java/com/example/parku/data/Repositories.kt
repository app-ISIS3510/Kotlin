package com.example.parku.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val PARKING_VIEW = "parking_lots_with_availability"

/** Espejo de lib/repositories/parking_repository.dart. */
object ParkingRepository {

    suspend fun getParkingLots(): List<Parking> =
        supabase.from(PARKING_VIEW)
            .select { order("name", Order.ASCENDING) }
            .decodeList()

    suspend fun getParkingById(id: String): Parking? =
        supabase.from(PARKING_VIEW)
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()

    suspend fun searchParkingLots(query: String): List<Parking> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        return supabase.from(PARKING_VIEW)
            .select {
                filter {
                    or {
                        ilike("name", "%$trimmed%")
                        ilike("address", "%$trimmed%")
                    }
                }
                order("name", Order.ASCENDING)
            }
            .decodeList()
    }
}

/** Fila de `favorites` con el parqueadero embebido, como hace el select de Flutter. */
@Serializable
private data class FavoriteRow(
    @SerialName("parking_lots") val parking: Parking? = null,
)

/** Espejo de lib/repositories/favorite_repository.dart. */
object FavoriteRepository {

    suspend fun getFavorites(): List<Parking> =
        supabase.from("favorites")
            .select(Columns.raw("parking_lots(*)")) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FavoriteRow>()
            .mapNotNull { it.parking }

    suspend fun isFavorite(parkingId: String): Boolean =
        supabase.from("favorites")
            .select(Columns.list("id")) {
                filter { eq("parking_id", parkingId) }
                limit(1)
            }
            .decodeList<Map<String, String>>()
            .isNotEmpty()

    suspend fun addFavorite(parkingId: String) {
        if (isFavorite(parkingId)) return

        supabase.from("favorites").insert(
            buildJsonObject { put("parking_id", parkingId) },
        )
    }

    suspend fun removeFavorite(parkingId: String) {
        supabase.from("favorites").delete {
            filter { eq("parking_id", parkingId) }
        }
    }

    suspend fun toggleFavorite(parkingId: String) {
        if (isFavorite(parkingId)) removeFavorite(parkingId) else addFavorite(parkingId)
    }
}

/** Espejo de lib/repositories/session_repository.dart. */
object SessionRepository {

    suspend fun createSession(
        parkingId: String,
        pickupTimeIso: String,
        vehicleType: String = "car",
    ): ParkingSession =
        supabase.postgrest.rpc(
            function = "start_parking_session",
            parameters = buildJsonObject {
                put("p_parking_id", parkingId)
                put("p_pickup_time", pickupTimeIso)
                put("p_vehicle_type", vehicleType)
            },
        ).decodeAs()

    suspend fun getActiveSession(): ParkingSession? =
        supabase.from("parking_sessions")
            .select {
                filter { eq("status", "active") }
                order("started_at", Order.DESCENDING)
                limit(1)
            }
            .decodeSingleOrNull()

    suspend fun updatePickupTime(sessionId: String, pickupTimeIso: String): ParkingSession =
        supabase.from("parking_sessions")
            .update({ set("pickup_time", pickupTimeIso) }) {
                select()
                filter { eq("id", sessionId) }
            }
            .decodeSingle()

    suspend fun endSession(sessionId: String, endedAtIso: String): ParkingSession =
        supabase.from("parking_sessions")
            .update(
                {
                    set("status", "completed")
                    set("ended_at", endedAtIso)
                },
            ) {
                select()
                filter { eq("id", sessionId) }
            }
            .decodeSingle()
}

/** Espejo de lib/repositories/analytics_repository.dart. */
object AnalyticsRepository {

    suspend fun trackEvent(
        eventType: String,
        screen: String? = null,
        parkingId: String? = null,
    ) {
        supabase.from("analytics_events").insert(
            buildJsonObject {
                put("event_type", eventType)
                put("screen", screen)
                put("parking_id", parkingId)
            },
        )
    }
}
