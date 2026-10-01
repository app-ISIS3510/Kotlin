package com.example.parku.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val PARKING_VIEW = "parking_lots_with_availability"

/** Id del usuario con sesion iniciada, o null si no hay ninguno. */
private val userId: String? get() = supabase.auth.currentUserOrNull()?.id

private fun requireUserId(): String =
    userId ?: throw IllegalStateException("Sign in to continue.")

/** Espejo de lib/repositories/auth_repository.dart. */
object AuthRepository {

    fun currentUserId(): String? = userId

    fun currentUserEmail(): String? = supabase.auth.currentUserOrNull()?.email

    fun currentUserName(): String? =
        supabase.auth.currentUserOrNull()
            ?.userMetadata
            ?.get("full_name")
            ?.toString()
            ?.trim('"')

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
    }

    suspend fun signUp(fullName: String, email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = buildJsonObject { put("full_name", fullName.trim()) }
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    /**
     * Espejo de user_repository.updateProfile: el correo solo se manda si
     * cambio, porque Supabase exige confirmarlo antes de reemplazarlo.
     */
    suspend fun updateProfile(fullName: String, email: String) {
        val current = supabase.auth.currentUserOrNull()?.email
        val newEmail = email.trim().takeIf { it != current }

        supabase.auth.updateUser {
            data = buildJsonObject { put("full_name", fullName.trim()) }
            if (newEmail != null) this.email = newEmail
        }
    }
}

/** Espejo de las validaciones de lib/services/profile_service.dart. */
object ProfileValidation {

    fun validateName(value: String): String? {
        val name = value.trim()
        return when {
            name.isEmpty() -> "Enter your full name."
            name.length > 100 -> "Use 100 characters or fewer."
            else -> null
        }
    }

    fun validateEmail(value: String): String? {
        val email = value.trim()
        val pattern = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")
        return if (!pattern.matches(email) || email.length > 254) {
            "Enter a valid email address."
        } else {
            null
        }
    }

    fun normalizePlate(value: String): String = value.trim().uppercase()

    /** Carro: ABC123. Moto: ABC12D. */
    fun validatePlate(vehicleType: String, value: String): String? {
        val plate = normalizePlate(value)
        val pattern = if (vehicleType == "car") {
            Regex("^[A-Z]{3}[0-9]{3}$")
        } else {
            Regex("^[A-Z]{3}[0-9]{2}[A-Z]$")
        }
        return if (pattern.matches(plate)) null else "Enter a complete license plate to continue."
    }
}

/** Espejo de lib/repositories/parking_repository.dart. No depende del usuario. */
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

    suspend fun getFavorites(): List<Parking> {
        val uid = userId ?: return emptyList()

        return supabase.from("favorites")
            .select(Columns.raw("parking_lots(*)")) {
                filter { eq("user_id", uid) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<FavoriteRow>()
            .mapNotNull { it.parking }
    }

    suspend fun isFavorite(parkingId: String): Boolean {
        val uid = userId ?: return false

        return supabase.from("favorites")
            .select(Columns.list("id")) {
                filter {
                    eq("parking_id", parkingId)
                    eq("user_id", uid)
                }
                limit(1)
            }
            .decodeList<Map<String, String>>()
            .isNotEmpty()
    }

    suspend fun addFavorite(parkingId: String) {
        if (isFavorite(parkingId)) return

        supabase.from("favorites").insert(
            buildJsonObject {
                put("parking_id", parkingId)
                put("user_id", requireUserId())
            },
        )
    }

    suspend fun removeFavorite(parkingId: String) {
        val uid = requireUserId()

        supabase.from("favorites").delete {
            filter {
                eq("parking_id", parkingId)
                eq("user_id", uid)
            }
        }
    }
}

/** Espejo de lib/repositories/session_repository.dart. */
object SessionRepository {

    suspend fun createSession(
        parkingId: String,
        pickupTimeIso: String,
        vehicleId: String,
    ): ParkingSession {
        requireUserId()

        return supabase.postgrest.rpc(
            function = "start_parking_with_vehicle",
            parameters = buildJsonObject {
                put("p_parking_id", parkingId)
                put("p_pickup_time", pickupTimeIso)
                put("p_vehicle_id", vehicleId)
            },
        ).decodeAs()
    }

    suspend fun getActiveSession(): ParkingSession? {
        val uid = userId ?: return null

        return supabase.from("parking_sessions")
            .select {
                filter {
                    eq("status", "active")
                    eq("user_id", uid)
                }
                order("started_at", Order.DESCENDING)
                limit(1)
            }
            .decodeSingleOrNull()
    }

    suspend fun updatePickupTime(sessionId: String, pickupTimeIso: String): ParkingSession {
        val uid = requireUserId()

        return supabase.from("parking_sessions")
            .update({ set("pickup_time", pickupTimeIso) }) {
                select()
                filter {
                    eq("id", sessionId)
                    eq("user_id", uid)
                }
            }
            .decodeSingle()
    }

    suspend fun endSession(sessionId: String, endedAtIso: String): ParkingSession {
        val uid = requireUserId()

        return supabase.from("parking_sessions")
            .update(
                {
                    set("status", "completed")
                    set("ended_at", endedAtIso)
                },
            ) {
                select()
                filter {
                    eq("id", sessionId)
                    eq("user_id", uid)
                }
            }
            .decodeSingle()
    }
}

/** Espejo de la parte de vehiculos de lib/repositories/user_repository.dart. */
object VehicleRepository {

    suspend fun getVehicles(): List<Vehicle> {
        val uid = userId ?: return emptyList()

        return supabase.from("vehicles")
            .select {
                filter { eq("user_id", uid) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList()
    }

    suspend fun addVehicle(vehicleType: String, plate: String) {
        requireUserId()

        supabase.postgrest.rpc(
            function = "add_my_vehicle",
            parameters = buildJsonObject {
                put("p_vehicle_type", vehicleType)
                put("p_plate", plate.trim().uppercase())
            },
        )
    }

    suspend fun selectVehicle(vehicleId: String) {
        requireUserId()

        supabase.postgrest.rpc(
            function = "select_my_vehicle",
            parameters = buildJsonObject { put("p_vehicle_id", vehicleId) },
        )
    }
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
