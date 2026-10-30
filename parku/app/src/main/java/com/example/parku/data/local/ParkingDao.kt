package com.example.parku.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
abstract class ParkingDao {

    @Query("select * from parking order by name")
    abstract suspend fun catalog(): List<ParkingEntity>

    @Query("select * from parking where id = :id")
    abstract suspend fun byId(id: String): ParkingEntity?

    @Query(
        """
        select * from parking
        where name like '%' || :query || '%' or address like '%' || :query || '%'
        order by name
        """,
    )
    abstract suspend fun search(query: String): List<ParkingEntity>

    @Query(
        """
        select p.* from parking p
        inner join favorite f on f.parkingId = p.id
        order by f.position
        """,
    )
    abstract suspend fun favorites(): List<ParkingEntity>

    @Query("select exists(select 1 from favorite where parkingId = :parkingId)")
    abstract suspend fun isFavorite(parkingId: String): Boolean

    @Upsert
    abstract suspend fun upsert(rows: List<ParkingEntity>)

    @Upsert
    abstract suspend fun upsert(row: ParkingEntity)

    @Upsert
    abstract suspend fun upsertFavorite(row: FavoriteEntity)

    @Query("delete from parking where id not in (:keep)")
    abstract suspend fun deleteCatalogExcept(keep: List<String>)

    @Query("delete from parking")
    abstract suspend fun deleteCatalog()

    @Query("delete from favorite")
    abstract suspend fun deleteFavorites()

    @Query("delete from favorite where parkingId = :parkingId")
    abstract suspend fun deleteFavorite(parkingId: String)

    @Query("select coalesce(min(position), 0) - 1 from favorite")
    abstract suspend fun topPosition(): Int

    @Transaction
    open suspend fun replaceCatalog(rows: List<ParkingEntity>) {
        if (rows.isEmpty()) {
            deleteCatalog()
            return
        }
        upsert(rows)
        deleteCatalogExcept(rows.map { it.id })
    }

    @Transaction
    open suspend fun replaceFavorites(rows: List<ParkingEntity>) {
        upsert(rows)
        deleteFavorites()
        rows.forEachIndexed { index, row -> upsertFavorite(FavoriteEntity(row.id, index)) }
    }

    @Transaction
    open suspend fun addFavorite(row: ParkingEntity) {
        upsert(row)
        upsertFavorite(FavoriteEntity(row.id, topPosition()))
    }

    @Transaction
    open suspend fun clear() {
        deleteFavorites()
        deleteCatalog()
    }
}
