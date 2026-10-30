package com.example.parku.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.parku.data.Parking

@Entity(tableName = "parking")
data class ParkingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val carSpaces: Int,
    val motorcycleSpaces: Int,
    val pricePerMinute: Double,
    val openingTime: String?,
    val closingTime: String?,
    val availableCarSpaces: Int?,
    val availableMotorcycleSpaces: Int?,
    val cachedAt: Long,
)

@Entity(
    tableName = "favorite",
    foreignKeys = [
        ForeignKey(
            entity = ParkingEntity::class,
            parentColumns = ["id"],
            childColumns = ["parkingId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parkingId")],
)
data class FavoriteEntity(
    @PrimaryKey val parkingId: String,
    val position: Int,
)

fun Parking.toEntity(cachedAt: Long = System.currentTimeMillis()) = ParkingEntity(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    carSpaces = carSpaces,
    motorcycleSpaces = motorcycleSpaces,
    pricePerMinute = pricePerMinute,
    openingTime = openingTime,
    closingTime = closingTime,
    availableCarSpaces = availableCarSpacesOrNull,
    availableMotorcycleSpaces = availableMotorcycleSpacesOrNull,
    cachedAt = cachedAt,
)

fun ParkingEntity.toModel() = Parking(
    id = id,
    name = name,
    address = address,
    latitude = latitude,
    longitude = longitude,
    carSpaces = carSpaces,
    motorcycleSpaces = motorcycleSpaces,
    pricePerMinute = pricePerMinute,
    openingTime = openingTime,
    closingTime = closingTime,
    availableCarSpacesOrNull = availableCarSpaces,
    availableMotorcycleSpacesOrNull = availableMotorcycleSpaces,
)
