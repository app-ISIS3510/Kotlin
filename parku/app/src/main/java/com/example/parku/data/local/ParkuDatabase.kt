package com.example.parku.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ParkingEntity::class, FavoriteEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class ParkuDatabase : RoomDatabase() {

    abstract fun parkingDao(): ParkingDao

    companion object {

        @Volatile
        private var instance: ParkuDatabase? = null

        fun get(context: Context): ParkuDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ParkuDatabase::class.java,
                    "parku.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
