package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

/**
 * Espejo de lib/screens/end_parking.dart: confirma antes de cerrar la sesion,
 * para que un toque accidental no termine el parqueo.
 */
@Composable
fun EndParkingScreen(
    currentIndex: Int,
    busy: Boolean,
    onConfirm: () -> Unit,
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
                .padding(start = 30.dp, top = 22.dp, end = 30.dp, bottom = 20.dp),
        ) {
            Text(
                text = "End parking",
                fontFamily = Inter,
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(90.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(AppColors.white)
                    .padding(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalParking,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(52.dp),
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Have you picked up\nyour vehicle?",
                    fontFamily = Inter,
                    fontSize = 28.sp,
                    lineHeight = 32.2.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Confirm to end this parking stay and stop the countdown.",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(28.dp))

                PickupButton(
                    text = if (busy) "Ending parking..." else "Yes, I've picked it up",
                    height = 56.dp,
                    onPressed = { if (!busy) onConfirm() },
                )

                Spacer(Modifier.height(14.dp))

                PickupButton(
                    text = "Not yet, go back",
                    height = 56.dp,
                    background = AppColors.lightPurple,
                    foreground = AppColors.primary,
                    onPressed = { if (!busy) onBack() },
                )
            }
        }

        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}
