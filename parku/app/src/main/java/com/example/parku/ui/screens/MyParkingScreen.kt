package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun MyParkingScreen(
    currentIndex: Int,
    onNavTap: (Int) -> Unit,
    onEndParking: () -> Unit,
    onChangePickupTime: (() -> Unit)? = null,
    pickupTime: String = "4:00",
    parkingName: String = "City U Parking",
    parkingAddress: String = "Calle 20 · Las Aguas, Bogotá",
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
                .padding(start = 30.dp, top = 22.dp, end = 30.dp, bottom = 20.dp),
        ) {
            // TITLE
            Text(
                text = "My parking",
                fontFamily = Inter,
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(30.dp))

            // TIME LABEL
            Text(
                text = "TIME UNTIL PICKUP",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.primary,
            )

            Spacer(Modifier.height(12.dp))

            // COUNTDOWN CIRCLE
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(250.dp)
                    .border(16.dp, AppColors.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .padding(26.dp)
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(AppColors.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = AppColors.primary,
                        modifier = Modifier.size(38.dp),
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "01:30",
                        fontFamily = Inter,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.primary,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "hours : minutes",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = AppColors.greyText,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // PICKUP TIME
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = AppColors.greyText)) {
                        append("Pick up at ")
                    }
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.W700,
                            color = AppColors.darkText,
                        ),
                    ) {
                        append("$pickupTime PM")
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontFamily = Inter,
                fontSize = 17.sp,
            )

            Spacer(Modifier.height(20.dp))

            // PARKING INFORMATION CARD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppColors.white)
                    .padding(20.dp),
            ) {
                Text(
                    text = parkingName,
                    fontFamily = Inter,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Car",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        color = AppColors.greyText,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "ABC123",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.darkText,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = parkingAddress,
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(18.dp))

                // MAP BUTTONS
                Row {
                    SecondaryButton(
                        text = "Waze",
                        onPressed = {},
                        modifier = Modifier.weight(1f),
                    )

                    Spacer(Modifier.width(12.dp))

                    SecondaryButton(
                        text = "Google Maps",
                        onPressed = {},
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // CHANGE PICKUP TIME
            Button(
                onClick = { onChangePickupTime?.invoke() },
                enabled = onChangePickupTime != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.lightPurple,
                    contentColor = AppColors.primary,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            ) {
                Text(
                    text = "Change pickup time",
                    fontFamily = Inter,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W600,
                )
            }

            Spacer(Modifier.height(14.dp))

            // END PARKING
            Button(
                onClick = onEndParking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary,
                    contentColor = AppColors.white,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
            ) {
                Text(
                    text = "I picked up my vehicle",
                    fontFamily = Inter,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W600,
                )
            }
        }

        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onPressed,
        modifier = modifier.height(55.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.lightPurple,
            contentColor = AppColors.primary,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontSize = 16.sp,
            fontWeight = FontWeight.W600,
        )
    }
}
