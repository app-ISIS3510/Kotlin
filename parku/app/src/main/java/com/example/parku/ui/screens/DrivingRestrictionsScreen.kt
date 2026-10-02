package com.example.parku.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.DrivingRestriction
import com.example.parku.data.Vehicle
import com.example.parku.ui.components.DesignIcon
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

private val restrictedColor = Color(0xFFA3313F)
private val restrictedBackground = Color(0xFFFCECF0)
private val clearColor = Color(0xFF16704A)
private val clearBackground = Color(0xFFE8F6EE)

@Composable
fun DrivingRestrictionsScreen(
    vehicle: Vehicle?,
    restriction: DrivingRestriction?,
    onOfficialInfo: () -> Unit,
    onChangeVehicle: () -> Unit,
    onNavTap: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val restricted = restriction?.restricted
    val color = if (restricted == false) clearColor else restrictedColor
    val background = if (restricted == false) clearBackground else restrictedBackground

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
            ScreenHeader(title = "Driving restrictions", onBack = onBack)

            Spacer(Modifier.height(36.dp))

            if (vehicle == null || restriction == null) {
                Text(
                    text = "Select a vehicle to check driving restrictions.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.lightPurple)
                        .padding(12.dp),
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 18.9.sp,
                    color = AppColors.primary,
                )
            } else {
                Text(
                    text = restriction.dateLabel,
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 18.9.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(background)
                        .padding(16.dp),
                ) {
                    DesignIcon(
                        name = if (restricted == false) {
                            "restriction_check"
                        } else {
                            "restriction_alert"
                        },
                        size = 40.dp,
                        color = color,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = when (restricted) {
                            null -> "Check today’s restrictions"
                            true -> "Driving restrictions apply today"
                            false -> "No driving restrictions today"
                        },
                        fontFamily = Inter,
                        fontSize = 26.sp,
                        lineHeight = 35.1.sp,
                        fontWeight = FontWeight.W700,
                        color = color,
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vehicle.label,
                            fontFamily = Inter,
                            fontSize = 16.sp,
                            color = AppColors.darkText,
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = vehicle.plate,
                            fontFamily = Inter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W700,
                            color = AppColors.darkText,
                        )
                    }

                    if (restriction.hours != null) {
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Restricted: ${restriction.hours}",
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            color = color,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = restriction.explanation
                        ?: "Restrictions depend on your vehicle type, license plate and date.",
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    lineHeight = 20.25.sp,
                    color = AppColors.greyText,
                )
            }

            Spacer(Modifier.height(16.dp))

            PickupButton(
                text = "View official information",
                background = AppColors.lightPurple,
                foreground = AppColors.primary,
                onPressed = onOfficialInfo,
            )

            Spacer(Modifier.height(16.dp))

            PickupButton(
                text = "Change vehicle",
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = onChangeVehicle,
            )
        }

        NavBar(currentIndex = 3, onTap = onNavTap)
    }
}
