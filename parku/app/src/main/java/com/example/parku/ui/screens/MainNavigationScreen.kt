package com.example.parku.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parku.data.Parking
import com.example.parku.data.ParkuViewModel
import com.example.parku.data.isoToPickupLabel
import kotlinx.coroutines.delay

/** Cada cuanto se relee el backend mientras la app esta en pantalla. */
private const val REFRESH_INTERVAL_MS = 5_000L

/**
 * Equivale a las rutas que en Flutter se abren con Navigator.push
 * por encima de la pantalla con pestanas.
 */
private sealed interface Overlay {
    data class Details(val parking: Parking) : Overlay

    data class Pickup(val parking: Parking) : Overlay

    data object ChangePickup : Overlay

    data class Search(val query: String) : Overlay

    data class Results(val query: String) : Overlay
}

@Composable
fun MainNavigationScreen(viewModel: ParkuViewModel = viewModel()) {
    var currentIndex by remember { mutableStateOf(0) }

    // Pila simple: cada pantalla apilada se cierra con atras, en orden.
    val stack = remember { mutableStateListOf<Overlay>() }

    fun push(overlay: Overlay) = stack.add(overlay)

    fun pop() {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
    }

    fun closeAll() = stack.clear()

    // Cierra lo que haya apilado antes de cambiar de pestana.
    val changePageFromOverlay: (Int) -> Unit = { index ->
        closeAll()
        currentIndex = index
    }

    val changePage: (Int) -> Unit = { index -> currentIndex = index }

    // El boton atras del sistema se comporta como el Navigator.pop de Flutter.
    BackHandler(enabled = stack.isNotEmpty()) { pop() }

    // Mientras la app este en pantalla, relee cada pocos segundos. Asi se ve lo
    // que cambio desde otro dispositivo sin tener que salir y volver a entrar.
    // El ciclo se detiene solo cuando la app pasa a segundo plano.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var first = true
            while (true) {
                viewModel.refresh(silent = !first)
                first = false
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    // Y tambien al cambiar de pestana, que es cuando mas se nota.
    LaunchedEffect(currentIndex) {
        if (currentIndex != 0) viewModel.refresh(silent = true)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Los errores del backend se avisan sin tumbar la pantalla.
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    val pickupLabel = viewModel.activeSession?.let { isoToPickupLabel(it.pickupTime) } ?: "4:00"

    Box(Modifier.fillMaxSize()) {
        val current = stack.lastOrNull()

        if (current != null) {
            when (current) {
                is Overlay.Details -> {
                    val parking = current.parking

                    ParkingDetailsScreen(
                        parking = parking,
                        isFavorite = viewModel.isFavorite(parking),
                        onToggleFavorite = { viewModel.toggleFavorite(parking) },
                        onParkHere = { push(Overlay.Pickup(parking)) },
                        onNavTap = changePageFromOverlay,
                        onBack = { pop() },
                    )
                }

                is Overlay.Pickup -> PickupTimeScreen(
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                    onStartParking = { time ->
                        viewModel.startParking(current.parking, time) {
                            closeAll()
                            currentIndex = 3
                        }
                    },
                )

                Overlay.ChangePickup -> ChangePickupTimeScreen(
                    initialTime = pickupLabel,
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                    onSave = { time -> viewModel.changePickupTime(time) { pop() } },
                )

                is Overlay.Search -> SearchScreen(
                    initialQuery = current.query,
                    parkingLots = viewModel.parkingLots,
                    onSubmit = { query ->
                        viewModel.search(query)
                        pop()
                        push(Overlay.Results(query))
                    },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )

                is Overlay.Results -> {
                    val results = viewModel.searchResults

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
                    parkingLots = viewModel.parkingLots,
                    onNavTap = changePage,
                    // Flujo de la wiki: Parking Lots -> Parking Details -> Pickup Time.
                    onSelectParking = { parking -> push(Overlay.Details(parking)) },
                    onOpenSearch = { push(Overlay.Search("")) },
                )

                2 -> if (viewModel.favorites.isEmpty()) {
                    NoFavoritesScreen(onNavTap = changePage)
                } else {
                    FavoritesScreen(
                        favorites = viewModel.favorites,
                        onNavTap = changePage,
                        onRemove = { parking -> viewModel.toggleFavorite(parking) },
                        onSelectFavorite = { parking -> push(Overlay.Details(parking)) },
                    )
                }

                3 -> if (viewModel.hasActiveParking) {
                    MyParkingScreen(
                        currentIndex = currentIndex,
                        onNavTap = changePage,
                        onEndParking = { viewModel.endParking() },
                        onChangePickupTime = { push(Overlay.ChangePickup) },
                        pickupTime = pickupLabel,
                        parkingName = viewModel.activeParking?.name.orEmpty(),
                        parkingAddress = viewModel.activeParking?.address.orEmpty(),
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
                    hasActiveParking = viewModel.hasActiveParking,
                    onOpenMyParking = { currentIndex = 3 },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
