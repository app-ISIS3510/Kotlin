package com.example.parku.data

object NavigationLinks {

    fun waze(latitude: Double, longitude: Double): String =
        "https://waze.com/ul?ll=$latitude,$longitude&navigate=yes"

    fun googleMaps(latitude: Double, longitude: Double): String =
        "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
}
