package com.example.parku.data

import android.os.Build
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Un unico lugar que sabe como ejecutar cada operacion pendiente contra Supabase. */
object PendingSync {

    suspend fun run(op: PendingOp) {
        when (op.type) {
            OP_TRACK -> AnalyticsRepository.trackEvent(op.eventType!!, op.screen, op.parkingId, op.metadata)
        }
    }
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
