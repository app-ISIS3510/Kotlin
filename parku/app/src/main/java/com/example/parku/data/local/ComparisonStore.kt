package com.example.parku.data.local

import android.content.Context
import com.example.parku.data.ComparedParking
import com.example.parku.data.Parking
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

@Serializable
private data class StoredComparison(
    val parking: Parking,
    val updatedAt: Long? = null,
    val unavailable: Boolean = false,
)

/**
 * Almacenamiento local (ObjectBox) de la ultima comparacion. Se guarda una fila por
 * cuenta y la lista mantiene el orden en que se eligieron los parqueaderos, para
 * volver a pintar la tabla al abrir la app aunque no haya red.
 */
class ComparisonStore(context: Context) {

    private val app = context.applicationContext

    suspend fun read(owner: String): List<ComparedParking> = withContext(Dispatchers.IO) {
        val box = box() ?: return@withContext emptyList()
        val saved = runCatching { find(box, owner)?.selectionJson }.getOrNull()
            ?: return@withContext emptyList()

        runCatching {
            json.decodeFromString<List<StoredComparison>>(saved).map {
                ComparedParking(
                    parking = it.parking,
                    updatedAt = it.updatedAt,
                    // Lo guardado no se ha confirmado contra el servidor en esta sesion.
                    isFromCache = true,
                    isUnavailable = it.unavailable,
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun write(owner: String, selection: List<ComparedParking>) {
        val text = withContext(Dispatchers.Default) {
            json.encodeToString(
                selection.map { StoredComparison(it.parking, it.updatedAt, it.isUnavailable) },
            )
        }

        withContext(Dispatchers.IO) {
            val store = store(app) ?: return@withContext
            val box = store.boxFor(ComparisonRecord::class.java)
            // Buscar y escribir en la misma transaccion, para que dos cambios
            // seguidos no dejen dos filas con el mismo dueno.
            runCatching {
                store.runInTx {
                    val previous = find(box, owner)
                    if (selection.isEmpty()) {
                        previous?.let { box.remove(it.id) }
                    } else {
                        box.put(ComparisonRecord(previous?.id ?: 0L, owner, text))
                    }
                }
            }
        }
    }

    private fun box(): Box<ComparisonRecord>? =
        store(app)?.boxFor(ComparisonRecord::class.java)

    private fun find(box: Box<ComparisonRecord>, owner: String): ComparisonRecord? =
        box.query(ComparisonRecord_.owner.equal(owner, QueryBuilder.StringOrder.CASE_SENSITIVE))
            .build()
            .use { it.findFirst() }

    private companion object {
        @Volatile
        private var opened: BoxStore? = null

        @Volatile
        private var unavailable = false

        /** Una sola base por proceso: abrirla dos veces lanza. */
        fun store(context: Context): BoxStore? {
            opened?.let { return it }
            synchronized(this) {
                opened?.let { return it }
                if (unavailable) return null
                val store = runCatching {
                    MyObjectBox.builder().androidContext(context).build()
                }.getOrNull()
                if (store == null) unavailable = true else opened = store
                return store
            }
        }
    }
}
