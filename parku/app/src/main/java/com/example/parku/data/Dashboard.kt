package com.example.parku.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Espejo de lib/repositories/dashboard_repository.dart. Cada Business Question
 * es una vista ya calculada en Supabase; aqui solo se leen.
 */

@Serializable
data class Bq1Row(
    val day: String,
    @SerialName("parking_sessions_started") val sessionsStarted: Int,
)

@Serializable
data class Bq2Row(
    @SerialName("parking_name") val parkingName: String,
    @SerialName("completed_sessions") val completedSessions: Int,
)

@Serializable
data class Bq3Row(
    @SerialName("flow_step") val flowStep: String,
    @SerialName("abandonment_count") val abandonmentCount: Int,
)

@Serializable
data class Bq4Row(
    @SerialName("favorite_add_events") val favoriteAddEvents: Int,
)

@Serializable
data class Bq5Row(
    @SerialName("event_type") val eventType: String,
    @SerialName("usage_count") val usageCount: Int,
)

@Serializable
data class Bq6Row(
    @SerialName("time_slot") val timeSlot: String,
    @SerialName("parking_name") val parkingName: String,
    @SerialName("parking_starts") val parkingStarts: Int,
)

/** Todo lo que pinta el dashboard, en una sola carga. */
data class DashboardData(
    val bq1: List<Bq1Row> = emptyList(),
    val bq2: List<Bq2Row> = emptyList(),
    val bq3: List<Bq3Row> = emptyList(),
    val bq4: Int = 0,
    val bq5: List<Bq5Row> = emptyList(),
    val bq6: List<Bq6Row> = emptyList(),
)

object DashboardRepository {

    suspend fun load(): DashboardData = DashboardData(
        bq1 = supabase.from("bq1_parking_starts_last_7_days").select().decodeList(),
        bq2 = supabase.from("bq2_top_parking_completed_last_7_days").select().decodeList(),
        bq3 = supabase.from("bq3_parking_flow_abandonment_last_7_days").select().decodeList(),
        bq4 = supabase.from("bq4_favorite_additions_last_7_days")
            .select()
            .decodeList<Bq4Row>()
            .firstOrNull()
            ?.favoriteAddEvents
            ?: 0,
        bq5 = supabase.from("bq5_action_usage_last_7_days").select().decodeList(),
        bq6 = supabase.from("bq6_top_parking_by_2h_slot_last_30_days").select().decodeList(),
    )
}

/** Fila de app_admins. La tabla solo deja ver la propia por RLS. */
@Serializable
private data class AdminRow(@SerialName("user_id") val userId: String)

/** Espejo de UserRepository.isAdmin de Flutter. */
object AdminRepository {

    suspend fun isAdmin(): Boolean {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return false

        return runCatching {
            supabase.from("app_admins")
                .select { filter { eq("user_id", uid) } }
                .decodeList<AdminRow>()
                .isNotEmpty()
        }.getOrDefault(false)
    }
}
