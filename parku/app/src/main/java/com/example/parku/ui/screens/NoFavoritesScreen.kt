package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.DesignIcon
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun NoFavoritesScreen(onNavTap: (Int) -> Unit) {
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
            ScreenHeader(title = "Favorites")

            Spacer(Modifier.height(97.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.lightPurple)
                    .padding(16.dp),
            ) {
                DesignIcon("heart1", size = 48.dp)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Your favorites start here",
                    fontFamily = Inter,
                    fontSize = 25.sp,
                    lineHeight = 33.75.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Save a parking lot from its details to find it faster next time.",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    lineHeight = 21.6.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(12.dp))

                PickupButton(
                    text = "Explore parking lots",
                    onPressed = { onNavTap(1) },
                )
            }
        }

        NavBar(currentIndex = 2, onTap = onNavTap)
    }
}
