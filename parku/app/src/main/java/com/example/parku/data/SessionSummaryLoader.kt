package com.example.parku.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

object SessionSummaryLoader {

    private val pool = Executors.newFixedThreadPool(2)

    suspend fun load(summary: SessionSummary): SessionSummary = withContext(Dispatchers.IO) {
        val parkingTask = pool.submit(Callable {
            runBlocking { ParkingRepository.getParkingById(summary.parkingId) }
        })
        val vehicleTask = pool.submit(Callable {
            val vehicleId = summary.vehicleId ?: return@Callable null
            runBlocking { VehicleRepository.getVehicles().firstOrNull { it.id == vehicleId } }
        })

        try {
            val parking = parkingTask.get()
            val vehicle = vehicleTask.get()
            summary.copy(
                parkingName = parking?.name ?: summary.parkingName,
                parkingAddress = parking?.address ?: summary.parkingAddress,
                vehicleType = vehicle?.vehicleType ?: summary.vehicleType,
                plate = vehicle?.plate ?: summary.plate,
                total = parking?.let { summary.minutes * it.pricePerMinute } ?: summary.total,
            )
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
}
