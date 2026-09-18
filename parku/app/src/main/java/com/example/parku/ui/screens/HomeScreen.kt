package com.example.parku.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.R
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun HomeScreen(
    currentIndex: Int,
    onNavTap: (Int) -> Unit,
    hasActiveParking: Boolean,
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
            Image(
                painter = painterResource(R.drawable.map),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

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

            // MARCADORES
            ParkingMarker(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 210.dp, y = 160.dp),
            )

            ParkingMarker(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-100).dp, y = 270.dp),
            )

            ParkingMarker(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 120.dp, y = 310.dp),
            )

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
                        text = "City U Parking",
                        fontFamily = Inter,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.darkText,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Car ABC123 · Pick up at 4:00 PM",
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        color = AppColors.greyText,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "Calle 20 · Las Aguas, Bogotá",
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

@Composable
fun ParkingMarker(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(AppColors.white),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = AppColors.primary,
            modifier = Modifier.size(34.dp),
        )
    }
}
