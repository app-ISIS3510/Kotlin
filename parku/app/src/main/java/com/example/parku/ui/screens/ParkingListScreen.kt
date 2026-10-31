package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.Parking
import com.example.parku.data.SMART_RADIUS_OPTIONS
import com.example.parku.data.SmartParkingResults
import com.example.parku.data.SmartVehicleType
import com.example.parku.ui.components.DesignIcon
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.ParkingCard
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

private fun radiusLabel(meters: Int): String =
    if (meters < 1000) "$meters m" else "${meters / 1000} km"

@Composable
private fun ChoiceChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onSelect: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AppColors.primary else AppColors.white)
            .clickable { onSelect() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) AppColors.white else AppColors.primary,
                modifier = Modifier.size(20.dp),
            )

            Spacer(Modifier.width(8.dp))
        }

        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontFamily = Inter,
            fontSize = 15.sp,
            fontWeight = FontWeight.W600,
            color = if (selected) AppColors.white else AppColors.primary,
        )
    }
}

@Composable
private fun EmptyCard(
    title: String,
    body: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.lightPurple)
            .padding(16.dp),
    ) {
        DesignIcon("parking", size = 48.dp)

        Spacer(Modifier.height(12.dp))

        Text(
            text = title,
            fontFamily = Inter,
            fontSize = 25.sp,
            lineHeight = 33.75.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = body,
            fontFamily = Inter,
            fontSize = 16.sp,
            lineHeight = 21.6.sp,
            color = AppColors.greyText,
        )

        if (action != null) {
            Spacer(Modifier.height(12.dp))

            PickupButton(text = action, onPressed = onAction)
        }
    }
}

@Composable
fun ParkingListScreen(
    currentIndex: Int,
    parkingLots: List<Parking>,
    onNavTap: (Int) -> Unit,
    onSelectParking: ((Parking) -> Unit)? = null,
    onOpenSearch: () -> Unit = {},
    onRetry: () -> Unit = {},
    smartParking: SmartParkingResults = SmartParkingResults(),
    vehicleType: SmartVehicleType = SmartVehicleType.CAR,
    onVehicleType: (SmartVehicleType) -> Unit = {},
    radiusMeters: Int = SMART_RADIUS_OPTIONS[1],
    onRadius: (Int) -> Unit = {},
    onRefreshLocation: () -> Unit = {},
    locationNotice: String? = null,
) {
    val vehicleWord = if (vehicleType == SmartVehicleType.CAR) "car" else "motorcycle"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        // HEADER
        Text(
            text = "Parking lots",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 22.dp, end = 32.dp, bottom = 18.dp),
            fontFamily = Inter,
            fontSize = 32.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // SEARCH BAR
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.white)
                        .clickable { onOpenSearch() }
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = AppColors.greyText,
                        modifier = Modifier.size(25.dp),
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Search parking lots",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        color = AppColors.greyText,
                    )
                }
            }

            // SUBTITLE
            item {
                Text(
                    text = "Find a place to park",
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = Inter,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )
            }

            // FILTERS
            if (parkingLots.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Vehicle type",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W700,
                            color = AppColors.darkText,
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ChoiceChip(
                                text = "Car",
                                selected = vehicleType == SmartVehicleType.CAR,
                                modifier = Modifier.weight(1f),
                                icon = Icons.Filled.DirectionsCar,
                                onSelect = { onVehicleType(SmartVehicleType.CAR) },
                            )

                            ChoiceChip(
                                text = "Motorcycle",
                                selected = vehicleType == SmartVehicleType.MOTORCYCLE,
                                modifier = Modifier.weight(1f),
                                icon = Icons.Filled.TwoWheeler,
                                onSelect = { onVehicleType(SmartVehicleType.MOTORCYCLE) },
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Search radius",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W700,
                            color = AppColors.darkText,
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SMART_RADIUS_OPTIONS.forEach { meters ->
                                ChoiceChip(
                                    text = radiusLabel(meters),
                                    selected = radiusMeters == meters,
                                    modifier = Modifier.weight(1f),
                                    onSelect = { onRadius(meters) },
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        ChoiceChip(
                            text = "Refresh location",
                            selected = false,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Filled.MyLocation,
                            onSelect = onRefreshLocation,
                        )
                    }
                }
            }

            // LOCATION NOTICE
            if (parkingLots.isNotEmpty() && locationNotice != null) {
                item {
                    Text(
                        text = locationNotice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.lightPurple)
                            .padding(14.dp),
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = AppColors.darkText,
                    )
                }
            }

            // RADIUS NOTICE
            if (smartParking.outsideRadius) {
                item {
                    Text(
                        text = "Nothing within " + radiusLabel(radiusMeters) +
                            ". Showing the closest ones instead.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.lightPurple)
                            .padding(14.dp),
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = AppColors.darkText,
                    )
                }
            }

            // LIST
            when {
                parkingLots.isEmpty() -> item {
                    EmptyCard(
                        title = "Nothing saved yet",
                        body = "Connect once to download the parking lots. After that you can " +
                            "browse and search them without a connection.",
                        action = "Try again",
                        onAction = onRetry,
                    )
                }

                smartParking.items.isEmpty() -> item {
                    EmptyCard(
                        title = "No lots for this vehicle",
                        body = "None of the saved parking lots has spaces for a " + vehicleWord +
                            ". Try the other vehicle type.",
                    )
                }

                else -> items(smartParking.items) { result ->
                    ParkingCard(
                        name = result.parking.name,
                        address = result.parking.address,
                        type = "${result.totalSpaces} $vehicleWord spaces",
                        onTap = if (onSelectParking == null) null else {
                            { onSelectParking(result.parking) }
                        },
                        recommended = result.isRecommended,
                        distance = result.formattedDistance,
                        availabilityLabel = result.availabilityLabel,
                        availabilityRatio = (result.availabilityPercentage / 100.0).toFloat(),
                        spacesLabel = result.spacesLabel,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}
