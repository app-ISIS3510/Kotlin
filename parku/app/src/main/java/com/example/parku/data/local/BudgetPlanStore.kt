package com.example.parku.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ultimo plan del Parking Budget Planner de una cuenta. */
data class BudgetPlan(
    val durationMinutes: Int,
    val budget: Long?,
    val updatedAt: Long,
)

/**
 * Almacenamiento local del Parking Budget Planner con SQLite directo
 * (SQLiteOpenHelper, sin Room). Es una sola tabla con una fila por cuenta, asi que
 * un ORM no aporta nada. Ademas va en su propio archivo (budget_plan.db): la base de
 * Room usa fallbackToDestructiveMigration y borraria el plan en cada cambio de esquema.
 *
 * Todas las lecturas y escrituras van en Dispatchers.IO para no tocar disco en el
 * hilo principal.
 */
class BudgetPlanStore(context: Context) {

    private val helper = Helper(context.applicationContext)

    suspend fun read(owner: String): BudgetPlan? = withContext(Dispatchers.IO) {
        runCatching {
            helper.readableDatabase.query(
                TABLE,
                arrayOf(COL_DURATION, COL_BUDGET, COL_UPDATED_AT),
                "$COL_OWNER = ?",
                arrayOf(owner),
                null,
                null,
                null,
            ).use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                BudgetPlan(
                    durationMinutes = cursor.getInt(0),
                    budget = if (cursor.isNull(1)) null else cursor.getLong(1),
                    updatedAt = cursor.getLong(2),
                )
            }
        }.getOrNull()
    }

    suspend fun write(owner: String, durationMinutes: Int, budget: Long?) =
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(COL_OWNER, owner)
                put(COL_DURATION, durationMinutes)
                if (budget == null) putNull(COL_BUDGET) else put(COL_BUDGET, budget)
                put(COL_UPDATED_AT, System.currentTimeMillis())
            }
            // La llave primaria es la cuenta: REPLACE deja siempre una sola fila por persona.
            runCatching {
                helper.writableDatabase.insertWithOnConflict(
                    TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE,
                )
            }.isSuccess
        }

    private class Helper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                create table $TABLE (
                    $COL_OWNER text primary key not null,
                    $COL_DURATION integer not null check ($COL_DURATION >= 0),
                    $COL_BUDGET integer check ($COL_BUDGET is null or $COL_BUDGET >= 0),
                    $COL_UPDATED_AT integer not null
                )
                """.trimIndent(),
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            // Version 1: todavia no hay migraciones.
        }
    }

    private companion object {
        const val DB_NAME = "budget_plan.db"
        const val DB_VERSION = 1
        const val TABLE = "budget_plan"
        const val COL_OWNER = "owner_id"
        const val COL_DURATION = "duration_minutes"
        const val COL_BUDGET = "budget_cop"
        const val COL_UPDATED_AT = "updated_at"
    }
}
