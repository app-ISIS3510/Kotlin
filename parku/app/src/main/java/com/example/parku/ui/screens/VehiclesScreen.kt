package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
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
    onSelect: (Vehicle) -> Unit,
    onAddVehicle: () -> Unit,
    onNavTap: (Int) -> Unit,
    onBack: () -> Unit,
) {
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
            ScreenHeader(title = "My vehicles", onBack = onBack)

            Spacer(Modifier.height(24.dp))

            if (vehicles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.lightPurple)
                        .padding(16.dp),
                ) {
                    DesignIcon("profile_car", size = 48.dp)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "No vehicles yet",
                        fontFamily = Inter,
                        fontSize = 25.sp,
                        lineHeight = 33.75.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.darkText,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Add your car or motorcycle plate to start parking.",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        lineHeight = 21.6.sp,
                        color = AppColors.greyText,
                    )
                }
            } else {
                Text(
                    text = "Tap a vehicle to use it for your next parking.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    lineHeight = 17.55.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(12.dp))

                vehicles.forEach { vehicle ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (vehicle.isSelected) AppColors.lightPurple else AppColors.white,
                            )
                            .clickable(role = Role.RadioButton) { onSelect(vehicle) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DesignIcon("profile_car")

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = vehicle.plate,
                                fontFamily = Inter,
                                fontSize = 17.sp,
                                lineHeight = 22.95.sp,
                                fontWeight = FontWeight.W700,
                                color = AppColors.darkText,
                            )

                            Text(
                                text = vehicle.label,
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                lineHeight = 17.55.sp,
                                color = AppColors.greyText,
                            )
                        }

                        if (vehicle.isSelected) {
                            Text(
                                text = "In use",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W700,
                                color = AppColors.primary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            PickupButton(
                text = "Add vehicle",
                onPressed = onAddVehicle,
            )
        }

        NavBar(currentIndex = 3, onTap = onNavTap)
    }
}
