package com.example.parku.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parku.data.Parking
import com.example.parku.data.DrivingRestrictionService
import com.example.parku.data.LocationProvider
import com.example.parku.data.NavigationLinks
import com.example.parku.data.ParkuViewModel
import com.example.parku.data.generateAvailableTimes
import com.example.parku.data.isoToPickupLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    data object MyParking : Overlay

    data class Vehicles(val choosing: Boolean = false) : Overlay

    data object AddVehicle : Overlay

    data object EditProfile : Overlay

    data object Restrictions : Overlay

    data object EndParking : Overlay

    data object Dashboard : Overlay
}

/**
 * Equivale a lib/screens/auth_gate.dart: sin sesion iniciada no se entra a la app.
 */
@Composable
fun AuthGate(viewModel: ParkuViewModel = viewModel()) {
    var showCreateAccount by rememberSaveable { mutableStateOf(false) }

    if (viewModel.isSignedIn) {
        MainNavigationScreen(viewModel)
        return
    }

    if (showCreateAccount) {
        CreateAccountScreen(
            busy = viewModel.authBusy,
            error = viewModel.authError,
            onCreateAccount = { name, email, password ->
                viewModel.signUp(name, email, password)
            },
            onGoToSignIn = {
                viewModel.dismissAuthError()
                showCreateAccount = false
            },
        )
    } else {
        SignInScreen(
            busy = viewModel.authBusy,
            error = viewModel.authError,
            onSignIn = { email, password -> viewModel.signIn(email, password) },
            onGoToCreateAccount = {
                viewModel.dismissAuthError()
                showCreateAccount = true
            },
        )
    }
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Permiso de ubicacion: se pide una vez al entrar, igual que el
    // checkPermission/requestPermission del DistanceManager de Flutter.
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        hasLocationPermission = granted.values.any { it }
    }

    // Con el permiso concedido se lee la posicion para ordenar por cercania.
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.updateUserLocation(LocationProvider.currentLocation(context))
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    // Los errores del backend se avisan sin tumbar la pantalla.
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    val pickupLabel = viewModel.activeSession?.let { isoToPickupLabel(it.pickupTime) } ?: "4:00"

    Box(Modifier.fillMaxSize()) {
        // Abre Waze o Google Maps, con los mismos avisos que usa Flutter cuando
    // el parqueadero no tiene coordenadas o no hay app que atienda el enlace.
    fun openNavigation(parking: Parking?, app: String) {
        val latitude = parking?.latitude
        val longitude = parking?.longitude

        if (latitude == null || longitude == null) {
            scope.launch {
                snackbarHostState.showSnackbar("Parking location is not available.")
            }
            return
        }

        val url = if (app == "Waze") {
            NavigationLinks.waze(latitude, longitude)
        } else {
            NavigationLinks.googleMaps(latitude, longitude)
        }

        viewModel.trackNavigationOpened(parking.id)

        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            scope.launch {
                snackbarHostState.showSnackbar("Could not open $app: ${it.message}")
            }
        }
    }

    val current = stack.lastOrNull()

        if (current != null) {
            when (current) {
                is Overlay.Details -> {
                    val parking = current.parking

                    LaunchedEffect(parking.id) { viewModel.trackDetailViewed(parking.id) }

                    ParkingDetailsScreen(
                        parking = parking,
                        isFavorite = viewModel.isFavorite(parking),
                        onToggleFavorite = { viewModel.toggleFavorite(parking) },
                        onParkHere = { push(Overlay.Pickup(parking)) },
                        onWaze = { openNavigation(parking, "Waze") },
                        onGoogleMaps = { openNavigation(parking, "Google Maps") },
                        onNavTap = changePageFromOverlay,
                        onBack = { pop() },
                    )
                }

                is Overlay.Pickup -> if (viewModel.selectedVehicle == null) {
                    AddVehicleScreen(
                        busy = false,
                        onAdd = { type, plate -> viewModel.addVehicle(type, plate) },
                        onBack = { pop() },
                    )
                } else PickupTimeScreen(
                    availableTimes = generateAvailableTimes(
                        current.parking.openingTime,
                        current.parking.closingTime,
                    ),
                    vehicleLabel = viewModel.selectedVehicle?.label.orEmpty(),
                    vehiclePlate = viewModel.selectedVehicle?.plate.orEmpty(),
                    onChangeVehicle = { push(Overlay.Vehicles(choosing = true)) },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                    onStartParking = { time ->
                        viewModel.startParking(current.parking, time) {
                            closeAll()
                            currentIndex = 3
                            push(Overlay.MyParking)
                        }
                    },
                )

                Overlay.ChangePickup -> ChangePickupTimeScreen(
                    initialTime = pickupLabel,
                    availableTimes = generateAvailableTimes(
                        viewModel.activeParking?.openingTime,
                        viewModel.activeParking?.closingTime,
                    ),
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                    onSave = { time -> viewModel.changePickupTime(time) { pop() } },
                )

                is Overlay.Search -> SearchScreen(
                    initialQuery = current.query,
                    parkingLots = viewModel.parkingLots,
                    nearestParkingLots = viewModel.nearestParkingLots,
                    hasLocation = viewModel.userLocation != null,
                    onSubmit = { query ->
                        viewModel.search(query)
                        pop()
                        push(Overlay.Results(query))
                    },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )

                Overlay.MyParking -> if (viewModel.hasActiveParking) {
                    MyParkingScreen(
                        currentIndex = 3,
                        onNavTap = changePageFromOverlay,
                        onEndParking = { push(Overlay.EndParking) },
                        onChangePickupTime = { push(Overlay.ChangePickup) },
                        pickupTime = pickupLabel,
                        pickupIso = viewModel.activeSession?.pickupTime.orEmpty(),
                        parkingName = viewModel.activeParking?.name.orEmpty(),
                        parkingAddress = viewModel.activeParking?.address.orEmpty(),
                        vehicleLabel = viewModel.activeSession?.let {
                            if (it.vehicleType == "motorcycle") "Motorcycle" else "Car"
                        }.orEmpty(),
                        vehiclePlate = viewModel.activeSession?.vehiclePlate
                            ?: viewModel.selectedVehicle?.plate.orEmpty(),
                        onBack = { pop() },
                        onWaze = { openNavigation(viewModel.activeParking, "Waze") },
                        onGoogleMaps = {
                            openNavigation(viewModel.activeParking, "Google Maps")
                        },
                    )
                } else {
                    NoActiveParkingScreen(
                        currentIndex = 3,
                        onNavTap = changePageFromOverlay,
                        onBack = { pop() },
                    )
                }

                Overlay.EditProfile -> EditProfileScreen(
                    initialName = viewModel.userName,
                    initialEmail = viewModel.userEmail,
                    busy = viewModel.authBusy,
                    onSave = { name, email ->
                        viewModel.saveProfile(name, email) { message ->
                            pop()
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )

                is Overlay.Vehicles -> VehiclesScreen(
                    vehicles = viewModel.vehicles,
                    choosingVehicle = current.choosing,
                    onUseVehicle = { vehicle ->
                        viewModel.selectVehicle(vehicle)
                        if (current.choosing) {
                            pop()
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "${vehicle.plate} selected for your next parking stay.",
                                )
                            }
                        }
                    },
                    onDeleteVehicle = { viewModel.deleteVehicle(it) },
                    onAddVehicle = { push(Overlay.AddVehicle) },
                    onCheckRestrictions = { push(Overlay.Restrictions) },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )

                Overlay.Dashboard -> AnalyticsDashboardScreen(
                    data = viewModel.dashboard,
                    loading = viewModel.dashboardLoading,
                    error = viewModel.dashboardError,
                    onRetry = { viewModel.loadDashboard() },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )

                Overlay.EndParking -> EndParkingScreen(
                    currentIndex = 3,
                    busy = viewModel.endingParking,
                    onConfirm = { viewModel.endParking { closeAll() } },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )

                Overlay.Restrictions -> DrivingRestrictionsScreen(
                    vehicle = viewModel.selectedVehicle,
                    restriction = viewModel.drivingRestriction,
                    onOfficialInfo = {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(DrivingRestrictionService.OFFICIAL_URL),
                                ),
                            )
                        }.onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Could not open the official website. Please try again.",
                                )
                            }
                        }
                    },
                    onChangeVehicle = { push(Overlay.Vehicles(choosing = true)) },
                    onNavTap = changePageFromOverlay,
                    onBack = { pop() },
                )

                Overlay.AddVehicle -> AddVehicleScreen(
                    busy = false,
                    onAdd = { type, plate -> viewModel.addVehicle(type, plate) { pop() } },
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

                3 -> ProfileScreen(
                    currentIndex = currentIndex,
                    fullName = viewModel.userName,
                    email = viewModel.userEmail,
                    busy = viewModel.authBusy,
                    isAdmin = viewModel.isAdmin,
                    onNavTap = changePage,
                    onEditProfile = { push(Overlay.EditProfile) },
                    onMyVehicles = { push(Overlay.Vehicles()) },
                    onMyParking = { push(Overlay.MyParking) },
                    onMyFavorites = { changePage(2) },
                    onAnalyticsDashboard = {
                        viewModel.loadDashboard()
                        push(Overlay.Dashboard)
                    },
                    onSignOut = { viewModel.signOut() },
                )

                else -> HomeScreen(
                    currentIndex = currentIndex,
                    parkingLots = viewModel.parkingLots,
                    hasLocationPermission = hasLocationPermission,
                    onSelectParking = { parking -> push(Overlay.Details(parking)) },
                    onNavTap = changePage,
                    hasActiveParking = viewModel.hasActiveParking,
                    parkingName = viewModel.activeParking?.name.orEmpty(),
                    parkingAddress = viewModel.activeParking?.address.orEmpty(),
                    pickupLabel = pickupLabel,
                    vehicleLabel = viewModel.activeSession?.let {
                        if (it.vehicleType == "motorcycle") "Motorcycle" else "Car"
                    } ?: "Car",
                    vehiclePlate = viewModel.activeSession?.vehiclePlate
                        ?: viewModel.selectedVehicle?.plate.orEmpty(),
                    onOpenMyParking = {
                        currentIndex = 3
                        push(Overlay.MyParking)
                    },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
