package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun NoActiveParkingScreen(
    currentIndex: Int,
    onNavTap: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 30.dp, top = 22.dp, end = 30.dp, bottom = 20.dp),
        ) {
            Text(
                text = "My parking",
                fontFamily = Inter,
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(110.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(AppColors.lightPurple)
                    .padding(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalParking,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(54.dp),
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "No active parking",
                    fontFamily = Inter,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Start parking after you leave your vehicle to see how much time is left until pickup.",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    lineHeight = 22.4.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Parking lots
                        onNavTap(1)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary,
                        contentColor = AppColors.white,
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                ) {
                    Text(
                        text = "Find parking",
                        fontFamily = Inter,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W600,
                    )
                }
            }
        }

        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}
