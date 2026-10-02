package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.Vehicle
import com.example.parku.ui.components.DesignIcon
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun VehiclesScreen(
    vehicles: List<Vehicle>,
    /** Cuando se llega desde la reserva, al elegir uno se vuelve atras. */
    choosingVehicle: Boolean = false,
    onUseVehicle: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit,
    onAddVehicle: () -> Unit,
    onCheckRestrictions: () -> Unit,
    onNavTap: (Int) -> Unit,
    onBack: () -> Unit,
) {
    // Confirmacion antes de borrar, como el AlertDialog de Flutter.
    var pendingDelete by remember { mutableStateOf<Vehicle?>(null) }

    pendingDelete?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text(
                    text = "Delete ${vehicle.plate}?",
                    fontFamily = Inter,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )
            },
            text = {
                Text(
                    text = "You can add this vehicle again later.",
                    fontFamily = Inter,
                    color = AppColors.greyText,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    onDeleteVehicle(vehicle)
                }) {
                    Text("Delete", fontFamily = Inter, color = AppColors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel", fontFamily = Inter, color = AppColors.greyText)
                }
            },
            containerColor = AppColors.white,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 24.dp),
        ) {
            ScreenHeader(
                title = if (choosingVehicle) "Choose a vehicle" else "My vehicles",
                onBack = onBack,
            )

            Spacer(Modifier.height(if (vehicles.isEmpty()) 70.dp else 24.dp))

            if (vehicles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.lightPurple)
                        .padding(16.dp),
                ) {
                    DesignIcon("empty_car", size = 48.dp)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Add your first vehicle",
                        fontFamily = Inter,
                        fontSize = 25.sp,
                        lineHeight = 33.75.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.darkText,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Save your car or motorcycle plate to track your parking and check driving restrictions.",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        lineHeight = 21.6.sp,
                        color = AppColors.greyText,
                    )

                    Spacer(Modifier.height(12.dp))

                    PickupButton(text = "Add vehicle", onPressed = onAddVehicle)
                }

                Spacer(Modifier.height(16.dp))

                PickupButton(
                    text = "Back to profile",
                    background = AppColors.white,
                    foreground = AppColors.primary,
                    onPressed = onBack,
                )
            } else {
                Text(
                    text = "Choose the vehicle you want to use.",
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    lineHeight = 20.25.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(16.dp))

                vehicles.forEach { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onUse = { onUseVehicle(vehicle) },
                        onDelete = { pendingDelete = vehicle },
                    )

                    Spacer(Modifier.height(16.dp))
                }

                PickupButton(text = "Add vehicle", onPressed = onAddVehicle)

                Spacer(Modifier.height(16.dp))

                PickupButton(
                    text = "Check driving restrictions",
                    background = AppColors.white,
                    foreground = AppColors.primary,
                    onPressed = onCheckRestrictions,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Vehicle for your next parking stay",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 16.2.sp,
                    color = AppColors.greyText,
                )
            }
        }

        NavBar(currentIndex = 3, onTap = onNavTap)
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onUse: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.white)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (vehicle.vehicleType == "motorcycle") {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                DesignIcon("profile_car")
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = vehicle.label,
                fontFamily = Inter,
                fontSize = 18.sp,
                lineHeight = 24.3.sp,
                fontWeight = FontWeight.W600,
                color = AppColors.darkText,
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = vehicle.plate,
            fontFamily = Inter,
            fontSize = 25.sp,
            lineHeight = 33.75.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        Spacer(Modifier.height(12.dp))

        Row {
            PickupButton(
                text = if (vehicle.isSelected) "In use" else "Use vehicle",
                height = 44.dp,
                background = AppColors.lightPurple,
                foreground = AppColors.primary,
                onPressed = onUse,
                modifier = Modifier.weight(192f),
            )

            Spacer(Modifier.width(8.dp))

            PickupButton(
                text = "Delete",
                height = 44.dp,
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = onDelete,
                modifier = Modifier.weight(110f),
            )
        }
    }
}
