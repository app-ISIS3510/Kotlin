package com.example.parku.ui.data

/**
 * Datos de ejemplo de la maqueta. Antes vivian dentro de ParkingListScreen;
 * ahora los comparten la lista, la busqueda y el detalle.
 *
 * Los campos [hours], [carSpaces], [carPrice], [motorcycleSpaces] y
 * [motorcyclePrice] los pide la pantalla de detalle del Figma. Los valores de
 * City U Parking son los del prototipo; los de los demas parqueaderos estan
 * inventados para poder recorrer la maqueta.
 */
data class ParkingLot(
    val name: String,
    val address: String,
    val type: String,
    val hours: String,
    val carSpaces: Int,
    val carPrice: String,
    val motorcycleSpaces: Int,
    val motorcyclePrice: String,
)

val parkingLots = listOf(
    ParkingLot(
        name = "City U Parking",
        address = "Calle 20 · Las Aguas, Bogotá",
        type = "Cars and motorcycles · Indoor",
        hours = "5:30-20:00",
        carSpaces = 40,
        carPrice = "$120 min",
        motorcycleSpaces = 15,
        motorcyclePrice = "$120 min",
    ),
    ParkingLot(
        name = "MetroPark Center",
        address = "45 Market St",
        type = "Indoor",
        hours = "6:00-22:00",
        carSpaces = 25,
        carPrice = "$150 min",
        motorcycleSpaces = 10,
        motorcyclePrice = "$100 min",
    ),
    ParkingLot(
        name = "University Lot C",
        address = "102 Campus Drive",
        type = "Outdoor",
        hours = "5:00-21:00",
        carSpaces = 60,
        carPrice = "$100 min",
        motorcycleSpaces = 30,
        motorcyclePrice = "$80 min",
    ),
    ParkingLot(
        name = "Library Underground",
        address = "250 Civic Center",
        type = "Indoor",
        hours = "7:00-19:00",
        carSpaces = 18,
        carPrice = "$140 min",
        motorcycleSpaces = 6,
        motorcyclePrice = "$110 min",
    ),
)

/** Devuelve el parqueadero con ese nombre exacto, o null si no existe. */
fun findParkingLot(name: String): ParkingLot? = parkingLots.firstOrNull { it.name == name }

/** Busqueda por nombre o direccion, sin distinguir mayusculas. */
fun searchParkingLots(query: String): List<ParkingLot> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()

    return parkingLots.filter {
        it.name.contains(q, ignoreCase = true) || it.address.contains(q, ignoreCase = true)
    }
}
