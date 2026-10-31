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

/**
 * Sprint 3, BQ2 de la lista del Sprint 1: pantallas con mayor tiempo de carga,
 * segmentadas por modelo de telefono y version de Android.
 */
@Serializable
data class LoadTimeRow(
    val screen: String,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("avg_network_ms") val avgNetworkMs: Int? = null,
    @SerialName("avg_cached_ms") val avgCachedMs: Int? = null,
    val samples: Int = 0,
)

/**
 * Sprint 3, BQ12: parqueaderos con mas solicitudes de navegacion y cuantas
 * terminan en un parqueo iniciado ahi mismo.
 */
@Serializable
data class NavigationConversionRow(
    @SerialName("parking_name") val parkingName: String,
    @SerialName("navigation_requests") val navigationRequests: Int,
    @SerialName("parking_starts") val parkingStarts: Int,
    @SerialName("conversion_pct") val conversionPct: Double = 0.0,
)

/** Todo lo que pinta el dashboard, en una sola carga. */
@Serializable
data class DashboardData(
    val bq1: List<Bq1Row> = emptyList(),
    val bq2: List<Bq2Row> = emptyList(),
    val bq3: List<Bq3Row> = emptyList(),
    val bq4: Int = 0,
    val bq5: List<Bq5Row> = emptyList(),
    val bq6: List<Bq6Row> = emptyList(),
    val loadTimes: List<LoadTimeRow> = emptyList(),
    val navigationConversion: List<NavigationConversionRow> = emptyList(),
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
        // Si la vista todavia no existe en Supabase, el resto del dashboard sigue funcionando.
        loadTimes = runCatching {
            supabase.from("bq2_slowest_screens_by_device").select().decodeList<LoadTimeRow>()
        }.getOrDefault(emptyList()),
        navigationConversion = runCatching {
            supabase.from("bq12_navigation_conversion_last_7_days")
                .select()
                .decodeList<NavigationConversionRow>()
        }.getOrDefault(emptyList()),
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
