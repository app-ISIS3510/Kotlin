package com.example.parku.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Equivale a las rutas que en Flutter se abren con Navigator.push
 * por encima de la pantalla con pestanas.
 */
private sealed interface Overlay {
    data class Pickup(val parking: ParkingLot) : Overlay

    data object ChangePickup : Overlay
}

@Composable
fun MainNavigationScreen() {
    var currentIndex by remember { mutableStateOf(0) }

    // Datos de ejemplo para recorrer las pantallas del MS7.
    var hasActiveParking by remember { mutableStateOf(true) }
    var pickupTime by remember { mutableStateOf("4:00") }
    var parkingName by remember { mutableStateOf("City U Parking") }
    var parkingAddress by remember { mutableStateOf("Calle 20 · Las Aguas, Bogotá") }

    val favorites = remember {
        mutableStateListOf(
            Favorite("City U Parking", "Calle 20 · Las Aguas, Bogotá"),
            Favorite("MetroPark Center", "45 Market St"),
            Favorite("University Lot C", "102 Campus Drive"),
            Favorite("Library Underground", "250 Civic Center"),
        )
    }

    var overlay by remember { mutableStateOf<Overlay?>(null) }

    // Cierra la pantalla de hora antes de cambiar de pestana.
    val changePageFromPickup: (Int) -> Unit = { index ->
        overlay = null
        currentIndex = index
    }

    val changePage: (Int) -> Unit = { index -> currentIndex = index }

    // El boton atras del sistema se comporta como el Navigator.pop de Flutter.
    BackHandler(enabled = overlay != null) {
        overlay = null
    }

    val current = overlay

    if (current != null) {
        when (current) {
            is Overlay.Pickup -> PickupTimeScreen(
                onNavTap = changePageFromPickup,
                onBack = { overlay = null },
                onStartParking = { time ->
                    overlay = null
                    pickupTime = time
                    parkingName = current.parking.name
                    parkingAddress = current.parking.address
                    hasActiveParking = true
                    currentIndex = 3
                },
            )

            Overlay.ChangePickup -> ChangePickupTimeScreen(
                initialTime = pickupTime,
                onNavTap = changePageFromPickup,
                onBack = { overlay = null },
                onSave = { time ->
                    overlay = null
                    pickupTime = time
                },
            )
        }
    } else {
        when (currentIndex) {
            1 -> ParkingListScreen(
                currentIndex = currentIndex,
                onNavTap = changePage,
                onSelectParking = { parking -> overlay = Overlay.Pickup(parking) },
            )

            2 -> if (favorites.isEmpty()) {
                NoFavoritesScreen(onNavTap = changePage)
            } else {
                FavoritesScreen(
                    favorites = favorites,
                    onNavTap = changePage,
                    onRemove = { index -> favorites.removeAt(index) },
                )
            }

            3 -> if (hasActiveParking) {
                MyParkingScreen(
                    currentIndex = currentIndex,
                    onNavTap = changePage,
                    onEndParking = { hasActiveParking = false },
                    onChangePickupTime = { overlay = Overlay.ChangePickup },
                    pickupTime = pickupTime,
                    parkingName = parkingName,
                    parkingAddress = parkingAddress,
                )
            } else {
                NoActiveParkingScreen(
                    currentIndex = currentIndex,
                    onNavTap = changePage,
                )
            }

            else -> HomeScreen(
                currentIndex = currentIndex,
                onNavTap = changePage,
                hasActiveParking = hasActiveParking,
                onOpenMyParking = { currentIndex = 3 },
            )
        }
    }
}
