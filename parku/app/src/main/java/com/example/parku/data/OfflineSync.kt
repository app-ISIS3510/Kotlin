package com.example.parku.data

import android.os.Build
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Un unico lugar que sabe como ejecutar cada operacion pendiente contra Supabase. */
object PendingSync {

    suspend fun run(op: PendingOp) {
        when (op.type) {
            OP_TRACK -> AnalyticsRepository.trackEvent(
                op.eventType!!,
                op.screen,
                op.parkingId,
                withEventInfo(op),
            )
            OP_FAVORITE_ADD -> FavoriteRepository.addFavorite(op.parkingId!!)
            OP_FAVORITE_REMOVE -> FavoriteRepository.removeFavorite(op.parkingId!!)
        }
    }
}

/**
 * Si el evento se envia tarde (estaba en la cola), la base le pone la hora del envio.
 * Por eso se manda tambien la hora real y el id del evento, para no contarlo en
 * otro dia ni dos veces si se reintenta.
 */
private fun withEventInfo(op: PendingOp): JsonObject = buildJsonObject {
    op.metadata?.forEach { (key, value) -> put(key, value) }
    put("client_event_id", op.id)
    op.occurredAt?.let { put("occurred_at", it) }
}

/** Fila que se inserta en `screen_load_times`. user_id lo pone la base con auth.uid(). */
@Serializable
private data class ScreenLoadRow(
    val screen: String,
    @SerialName("load_ms") val loadMs: Int,
    @SerialName("device_model") val deviceModel: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("from_cache") val fromCache: Boolean,
)

/** BQ2: mide cuanto tarda cada pantalla y lo envia junto con el modelo y la version de Android. */
object PerfRepository {

    val deviceModel: String get() = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    val osVersion: String get() = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    suspend fun send(samples: List<PerfSample>) {
        if (samples.isEmpty()) return
        supabase.from("screen_load_times").insert(
            samples.map {
                ScreenLoadRow(it.screen, it.loadMs, it.deviceModel, it.osVersion, it.fromCache)
            },
        )
    }
}
