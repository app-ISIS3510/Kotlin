package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.Parking
import com.example.parku.data.formatTime12h
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

/** Mismo encuadre inicial que usa home.dart en Flutter. */
private val CAMPUS = LatLng(4.6030, -74.0660)

@Composable
fun HomeScreen(
    currentIndex: Int,
    parkingLots: List<Parking>,
    hasLocationPermission: Boolean,
    onSelectParking: (Parking) -> Unit,
    onNavTap: (Int) -> Unit,
    hasActiveParking: Boolean,
    parkingName: String,
    parkingAddress: String,
    pickupLabel: String,
    vehicleLabel: String,
    vehiclePlate: String,
    onOpenMyParking: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        // ENCABEZADO
        Text(
            text = "ParkU",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 22.dp, end = 32.dp, bottom = 22.dp),
            fontFamily = Inter,
            fontSize = 32.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        // MAPA
        Box(Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(CAMPUS, 15.5f)
                },
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                ),
            ) {
                parkingLots.forEach { parking ->
                    val latitude = parking.latitude
                    val longitude = parking.longitude
                    if (latitude == null || longitude == null) return@forEach

                    Marker(
                        state = rememberMarkerState(
                            key = parking.id,
                            position = LatLng(latitude, longitude),
                        ),
                        title = parking.name,
                        snippet = parking.address,
                        onInfoWindowClick = {
                            onSelectParking(parking)
                            true
                        },
                    )
                }
            }

            // CAJA DE UBICACION
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp, start = 30.dp, end = 30.dp)
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.white)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(32.dp),
                )

                Spacer(Modifier.width(14.dp))

                Text(
                    text = "Near Universidad de los Andes",
                    modifier = Modifier.weight(1f),
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = AppColors.darkText,
                )
            }

            // TARJETA MY PARKING
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 30.dp, end = 30.dp, bottom = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(AppColors.white)
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 18.dp),
            ) {
                // ETIQUETA
                Text(
                    text = "MY PARKING",
                    modifier = Modifier
                        .width(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.lightPurple)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.primary,
                )

                Spacer(Modifier.height(14.dp))

                if (!hasActiveParking) {
                    // CONTENIDO SIN PARQUEO
                    Text(
                        text = "No active parking",
                        fontFamily = Inter,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.darkText,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Your current parking will appear here.",
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        color = AppColors.greyText,
                    )
                } else {
                    // CONTENIDO CON PARQUEO
                    Text(
                        text = parkingName,
                        fontFamily = Inter,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.darkText,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$vehicleLabel $vehiclePlate · Pick up at ${formatTime12h(pickupLabel)}",
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        color = AppColors.greyText,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = parkingAddress,
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = AppColors.greyText,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // BOTON
                Button(
                    onClick = onOpenMyParking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary,
                        contentColor = AppColors.white,
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                ) {
                    Text(
                        text = if (hasActiveParking) "View my parking" else "My parking",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                    )
                }
            }
        }

        Spacer(Modifier.height(15.dp))

        // BARRA DE NAVEGACION
        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}

