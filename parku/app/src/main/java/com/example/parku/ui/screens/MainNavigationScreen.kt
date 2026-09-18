package com.example.parku.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.parku.ui.data.ParkingLot
import com.example.parku.ui.data.findParkingLot
import com.example.parku.ui.data.searchParkingLots

/**
 * Equivale a las rutas que en Flutter se abren con Navigator.push
 * por encima de la pantalla con pestanas.
 */
private sealed interface Overlay {
    data class Details(val parking: ParkingLot) : Overlay

    data class Pickup(val parking: ParkingLot) : Overlay

    data object ChangePickup : Overlay

    data class Search(val query: String) : Overlay

    data class Results(val query: String) : Overlay
}

@Composable
fun MainNavigationScreen() {
    var currentIndex by remember { mutableStateOf(0) }

    // Arranque en limpio: sin parqueadero activo y sin favoritos. Se llenan
    // cuando el usuario guarda un favorito o inicia un parqueo.
    var hasActiveParking by remember { mutableStateOf(false) }
    var pickupTime by remember { mutableStateOf("4:00") }
    var parkingName by remember { mutableStateOf("") }
    var parkingAddress by remember { mutableStateOf("") }

    val favorites = remember { mutableStateListOf<Favorite>() }

    // Pila simple: cada pantalla apilada se cierra con atras, en orden.
    val stack = remember { mutableStateListOf<Overlay>() }

    fun push(overlay: Overlay) {
        stack.add(overlay)
    }

    fun pop() {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
    }

    fun closeAll() {
        stack.clear()
    }

    // Cierra lo que haya apilado antes de cambiar de pestana.
    val changePageFromOverlay: (Int) -> Unit = { index ->
        closeAll()
        currentIndex = index
    }

    val changePage: (Int) -> Unit = { index -> currentIndex = index }

    // El boton atras del sistema se comporta como el Navigator.pop de Flutter.
    BackHandler(enabled = stack.isNotEmpty()) {
        pop()
    }

    val current = stack.lastOrNull()

    if (current != null) {
        when (current) {
            is Overlay.Details -> {
                val parking = current.parking
                val isFavorite = favorites.any { it.name == parking.name }

                ParkingDetailsScreen(
                    parking = parking,
                    isFavorite = isFavorite,
                    onToggleFavorite = {
                        if (isFavorite) {
                            favorites.removeAll { it.name == parking.name }
                        } else {
                            favorites.add(Favorite(parking.name, parking.address))
                        }
                    },
                    onParkHere = { push(Overlay.Pickup(parking)) },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )
            }

            is Overlay.Pickup -> PickupTimeScreen(
                onNavTap = changePageFromOverlay,
                onBack = { pop() },
                onStartParking = { time ->
                    closeAll()
                    pickupTime = time
                    parkingName = current.parking.name
                    parkingAddress = current.parking.address
                    hasActiveParking = true
                    currentIndex = 3
                },
            )

            Overlay.ChangePickup -> ChangePickupTimeScreen(
                initialTime = pickupTime,
                onNavTap = changePageFromOverlay,
                onBack = { pop() },
                onSave = { time ->
                    pop()
                    pickupTime = time
                },
            )

            is Overlay.Search -> SearchScreen(
                initialQuery = current.query,
                onSubmit = { query ->
                    pop()
                    push(Overlay.Results(query))
                },
                onNavTap = changePageFromOverlay,
                onBack = { pop() },
            )

            is Overlay.Results -> {
                val results = searchParkingLots(current.query)

                if (results.isEmpty()) {
                    NoSearchResultsScreen(
                        query = current.query,
                        onEditSearch = {
                            pop()
                            push(Overlay.Search(current.query))
                        },
                        onShowAll = {
                            closeAll()
                            currentIndex = 1
                        },
                        onNavTap = changePageFromOverlay,
                        onBack = { pop() },
                    )
                } else {
                    SearchResultsScreen(
                        query = current.query,
                        results = results,
                        onSelectParking = { parking -> push(Overlay.Details(parking)) },
                        onClearSearch = {
                            pop()
                            push(Overlay.Search(""))
                        },
                        onNavTap = changePageFromOverlay,
                        onBack = { pop() },
                    )
                }
            }
        }
    } else {
        when (currentIndex) {
            1 -> ParkingListScreen(
                currentIndex = currentIndex,
                onNavTap = changePage,
                // Flujo de la wiki: Parking Lots -> Parking Details -> Pickup Time.
                onSelectParking = { parking -> push(Overlay.Details(parking)) },
                onOpenSearch = { push(Overlay.Search("")) },
            )

            2 -> if (favorites.isEmpty()) {
                NoFavoritesScreen(onNavTap = changePage)
            } else {
                FavoritesScreen(
                    favorites = favorites,
                    onNavTap = changePage,
                    onRemove = { index -> favorites.removeAt(index) },
                    onSelectFavorite = { favorite ->
                        findParkingLot(favorite.name)?.let { push(Overlay.Details(it)) }
                    },
                )
            }

            3 -> if (hasActiveParking) {
                MyParkingScreen(
                    currentIndex = currentIndex,
                    onNavTap = changePage,
                    onEndParking = { hasActiveParking = false },
                    onChangePickupTime = { push(Overlay.ChangePickup) },
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
