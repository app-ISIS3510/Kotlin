package com.example.parku.data.local

import android.content.Context
import com.example.parku.data.Parking

data class CachedRate(val pricePerMinute: Double, val cachedAt: Long)

class ParkingCatalog(context: Context) {

    private val dao = ParkuDatabase.get(context).parkingDao()

    suspend fun parkingLots(): List<Parking> = dao.catalog().map { it.toModel() }

    suspend fun saveParkingLots(lots: List<Parking>) =
        dao.replaceCatalog(lots.map { it.toEntity() })

    suspend fun parkingById(id: String): Parking? = dao.byId(id)?.toModel()

    /** Precio por minuto guardado y cuando se guardo; null si el parqueadero no esta en cache. */
    suspend fun cachedRate(id: String): CachedRate? =
        dao.byId(id)?.let { CachedRate(it.pricePerMinute, it.cachedAt) }

    suspend fun saveParking(parking: Parking) = dao.upsert(parking.toEntity())

    suspend fun search(query: String): List<Parking> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return dao.search(trimmed).map { it.toModel() }
    }

    suspend fun favorites(): List<Parking> = dao.favorites().map { it.toModel() }

    suspend fun saveFavorites(lots: List<Parking>) =
        dao.replaceFavorites(lots.map { it.toEntity() })

    suspend fun isFavorite(parkingId: String): Boolean = dao.isFavorite(parkingId)

    suspend fun addFavorite(parking: Parking) = dao.addFavorite(parking.toEntity())

    suspend fun removeFavorite(parkingId: String) = dao.deleteFavorite(parkingId)

    suspend fun clear() = dao.clear()
}
