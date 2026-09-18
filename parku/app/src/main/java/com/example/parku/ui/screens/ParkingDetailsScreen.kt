package com.example.parku.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.R
import com.example.parku.ui.components.DesignIcon
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.data.ParkingLot
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun ParkingDetailsScreen(
    parking: ParkingLot,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onParkHere: () -> Unit,
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
            ScreenHeader(title = "Parking details", onBack = onBack)

            Spacer(Modifier.height(20.dp))

            // FOTO CON EL CORAZON ENCIMA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                Image(
                    painter = painterResource(R.drawable.cityu),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.white)
                        .clickable(role = Role.Checkbox) { onToggleFavorite() },
                    contentAlignment = Alignment.Center,
                ) {
                    // Contorno cuando no esta guardado, relleno cuando si.
                    DesignIcon(
                        name = if (isFavorite) "heart_filled" else "heart",
                        size = 20.dp,
                        color = AppColors.primary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // NOMBRE EN MORADO
            Text(
                text = parking.name,
                fontFamily = Inter,
                fontSize = 26.sp,
                lineHeight = 35.1.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.primary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = parking.address,
                fontFamily = Inter,
                fontSize = 15.sp,
                lineHeight = 20.25.sp,
                color = AppColors.greyText,
            )

            Text(
                text = parking.hours,
                fontFamily = Inter,
                fontSize = 15.sp,
                lineHeight = 20.25.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(14.dp))

            // CUPOS POR TIPO DE VEHICULO
            Row {
                SpacesCard(
                    spaces = parking.carSpaces,
                    price = parking.carPrice,
                    icon = { DesignIcon("car", size = 22.dp) },
                    modifier = Modifier.weight(1f),
                )

                Spacer(Modifier.width(12.dp))

                SpacesCard(
                    spaces = parking.motorcycleSpaces,
                    price = parking.motorcyclePrice,
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.TwoWheeler,
                            contentDescription = null,
                            tint = AppColors.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Get directions",
                fontFamily = Inter,
                fontSize = 16.sp,
                lineHeight = 21.6.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.primary,
            )

            Spacer(Modifier.height(12.dp))

            Row {
                PickupButton(
                    text = "Waze",
                    background = AppColors.lightPurple,
                    foreground = AppColors.primary,
                    onPressed = {},
                    modifier = Modifier.weight(1f),
                )

                Spacer(Modifier.width(12.dp))

                PickupButton(
                    text = "Google Maps",
                    background = AppColors.lightPurple,
                    foreground = AppColors.primary,
                    onPressed = {},
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            PickupButton(
                text = "Park here",
                onPressed = onParkHere,
            )

            Spacer(Modifier.height(12.dp))

            PickupButton(
                text = if (isFavorite) "Remove from favorites" else "Save to favorites",
                background = AppColors.lightPurple,
                foreground = AppColors.primary,
                onPressed = onToggleFavorite,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Start parking when you leave your vehicle here.",
                fontFamily = Inter,
                fontSize = 13.sp,
                lineHeight = 17.55.sp,
                color = AppColors.greyText,
            )
        }

        NavBar(currentIndex = 1, onTap = onNavTap)
    }
}

@Composable
private fun SpacesCard(
    spaces: Int,
    price: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.white)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()

        Spacer(Modifier.width(10.dp))

        Column {
            Text(
                text = "$spaces spaces",
                fontFamily = Inter,
                fontSize = 15.sp,
                lineHeight = 20.25.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Text(
                text = price,
                fontFamily = Inter,
                fontSize = 11.sp,
                lineHeight = 14.85.sp,
                color = AppColors.greyText,
            )
        }
    }
}
