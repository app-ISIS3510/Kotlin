package com.example.parku.data

/**
 * Espejo de lib/adapters/waze_adapter.dart y google_maps_adapter.dart.
 * Las URLs son exactamente las mismas que usa la version en Flutter.
 */
object NavigationLinks {

    fun waze(latitude: Double, longitude: Double): String =
        "https://waze.com/ul?ll=$latitude,$longitude&navigate=yes"

    fun googleMaps(latitude: Double, longitude: Double): String =
        "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
}
