package com.example.parku.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val Context.parkuDataStore by preferencesDataStore(name = "parku_prefs")

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

@Serializable
data class CachedProfile(val name: String = "", val email: String = "")

@Serializable
data class CachedSession(val session: ParkingSession, val parking: Parking? = null)

@Serializable
data class CachedDashboard(val data: DashboardData, val savedAt: Long)

/** Operacion hecha sin conexion que espera a ser enviada a Supabase. */
@Serializable
data class PendingOp(
    val id: String,
    val type: String,
    val sessionId: String? = null,
    val vehicleId: String? = null,
    val vehicleType: String? = null,
    val plate: String? = null,
    val timeIso: String? = null,
    val eventType: String? = null,
    val screen: String? = null,
    val parkingId: String? = null,
    val metadata: JsonObject? = null,
    val attempts: Int = 0,
    val occurredAt: String? = null,
)

/** Una medicion de tiempo de carga de una pantalla (BQ2). */
@Serializable
data class PerfSample(
    val id: String,
    val screen: String,
    val loadMs: Int,
    val deviceModel: String,
    val osVersion: String,
    val fromCache: Boolean,
)

const val OP_TRACK = "track"
const val OP_FAVORITE_ADD = "favorite_add"
const val OP_FAVORITE_REMOVE = "favorite_remove"

/**
 * Almacenamiento local, con dos de las estrategias de la wiki:
 * - Local Files: archivos JSON en el almacenamiento privado de la app (filesDir) para
 *   lo que se muestra offline (perfil, vehiculos, sesion activa, parqueaderos), la cola
 *   de operaciones pendientes y las mediciones de rendimiento.
 * - Preferences (DataStore): solo valores pequenos, el dueno de la cache y la hora de
 *   la ultima sincronizacion.
 * Todo corre en Dispatchers.IO y las escrituras son atomicas (archivo temporal + rename).
 */
class LocalStore(context: Context) {

    private val ds = context.applicationContext.parkuDataStore
    private val dir = File(context.applicationContext.filesDir, "parku_cache").apply { mkdirs() }
    private val fileMutex = Mutex()

    private object Keys {
        val owner = stringPreferencesKey("owner")
        val lastSync = longPreferencesKey("last_sync_at")
        val navApp = stringPreferencesKey("preferred_nav_app")
    }

    private object Files {
        const val profile = "profile.json"
        const val vehicles = "vehicles.json"
        const val session = "active_session.json"
        const val parkingLots = "parking_lots.json"
        const val pending = "pending_ops.json"
        const val perf = "perf_queue.json"
        const val dashboard = "dashboard.json"
    }

    // Sin candado: se usan siempre dentro de fileMutex.
    private inline fun <reified T> readRaw(name: String): T? {
        val file = File(dir, name)
        if (!file.exists()) return null
        return runCatching { json.decodeFromString<T>(file.readText()) }.getOrNull()
    }

    private inline fun <reified T> writeRaw(name: String, value: T) {
        val tmp = File(dir, "$name.tmp")
        tmp.writeText(json.encodeToString(value))
        if (!tmp.renameTo(File(dir, name))) {
            File(dir, name).delete()
            tmp.renameTo(File(dir, name))
        }
    }

    private suspend inline fun <reified T> read(name: String): T? =
        withContext(Dispatchers.IO) { fileMutex.withLock { readRaw<T>(name) } }

    private suspend inline fun <reified T> write(name: String, value: T) {
        withContext(Dispatchers.IO) { fileMutex.withLock { writeRaw(name, value) } }
    }

    // --- Preferences (DataStore) -------------------------------------------

    suspend fun owner(): String? =
        withContext(Dispatchers.IO) { ds.data.first()[Keys.owner] }

    suspend fun saveOwner(userId: String) {
        withContext(Dispatchers.IO) { ds.edit { it[Keys.owner] = userId } }
    }

    /** Hora (epoch ms) de la ultima vez que se confirmo contra Supabase; 0 si nunca. */
    suspend fun lastSyncAt(): Long =
        withContext(Dispatchers.IO) { ds.data.first()[Keys.lastSync] ?: 0L }

    suspend fun saveLastSyncAt(millis: Long) {
        withContext(Dispatchers.IO) { ds.edit { it[Keys.lastSync] = millis } }
    }

    /** "Waze" o "Google Maps": la ultima app que la persona uso para llegar. */
    suspend fun preferredNavApp(): String? =
        withContext(Dispatchers.IO) { ds.data.first()[Keys.navApp] }

    suspend fun savePreferredNavApp(app: String) {
        withContext(Dispatchers.IO) { ds.edit { it[Keys.navApp] = app } }
    }

    // --- Cache de lectura (archivos JSON) ----------------------------------

    suspend fun profile(): CachedProfile? = read(Files.profile)
    suspend fun saveProfile(value: CachedProfile) = write(Files.profile, value)

    suspend fun vehicles(): List<Vehicle>? = read(Files.vehicles)
    suspend fun saveVehicles(value: List<Vehicle>) = write(Files.vehicles, value)

    suspend fun parkingLots(): List<Parking>? = read(Files.parkingLots)
    suspend fun saveParkingLots(value: List<Parking>) = write(Files.parkingLots, value)

    suspend fun session(): CachedSession? = read(Files.session)

    suspend fun dashboard(): CachedDashboard? = read(Files.dashboard)
    suspend fun saveDashboard(value: CachedDashboard) = write(Files.dashboard, value)

    suspend fun saveSession(value: CachedSession?) {
        if (value != null) {
            write(Files.session, value)
        } else {
            withContext(Dispatchers.IO) { fileMutex.withLock { File(dir, Files.session).delete() } }
        }
    }

    // --- Cola de operaciones pendientes ------------------------------------

    suspend fun pendingOps(): List<PendingOp> = read<List<PendingOp>>(Files.pending).orEmpty()

    /** Lee, transforma y escribe bajo el mismo candado, para que dos cambios seguidos no se pisen. */
    suspend fun updatePending(transform: (List<PendingOp>) -> List<PendingOp>) {
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                val current = readRaw<List<PendingOp>>(Files.pending).orEmpty()
                writeRaw(Files.pending, transform(current))
            }
        }
    }

    // --- Mediciones de rendimiento (BQ2) -----------------------------------

    suspend fun perfSamples(): List<PerfSample> = read<List<PerfSample>>(Files.perf).orEmpty()

    suspend fun updatePerf(transform: (List<PerfSample>) -> List<PerfSample>) {
        withContext(Dispatchers.IO) {
            fileMutex.withLock {
                val current = readRaw<List<PerfSample>>(Files.perf).orEmpty()
                // Tope para que la cola no crezca sin limite si nunca hay red.
                writeRaw(Files.perf, transform(current).takeLast(200))
            }
        }
    }

    /** Al cerrar sesion se borra todo lo del usuario anterior. */
    suspend fun clearUserData() {
        withContext(Dispatchers.IO) {
            fileMutex.withLock { dir.listFiles()?.forEach { it.delete() } }
            ds.edit { it.clear() }
        }
    }
}
