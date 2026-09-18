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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.components.SuggestedTimes
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun ChangePickupTimeScreen(
    initialTime: String = "4:00",
    onNavTap: (Int) -> Unit,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
) {
    var selectedTime by rememberSaveable(initialTime) { mutableStateOf(initialTime) }

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
                title = "Change pickup time",
                onBack = onBack,
            )

            Spacer(Modifier.height(36.dp))

            Text(
                text = "Need more time?",
                fontFamily = Inter,
                fontSize = 25.sp,
                lineHeight = 33.75.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Update the time you plan to return to your vehicle.",
                fontFamily = Inter,
                fontSize = 16.sp,
                lineHeight = 21.6.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.lightPurple)
                    .padding(16.dp),
            ) {
                Text(
                    text = "NEW PICKUP TIME",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    lineHeight = 16.2.sp,
                    fontWeight = FontWeight.W600,
                    color = AppColors.primary,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "$selectedTime PM",
                    fontFamily = Inter,
                    fontSize = 36.sp,
                    lineHeight = 48.6.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.primary,
                )
            }

            Spacer(Modifier.height(16.dp))

            SuggestedTimes(
                selectedTime = selectedTime,
                onChanged = { selectedTime = it },
            )

            Spacer(Modifier.height(16.dp))

            PickupButton(
                text = "Save pickup time",
                onPressed = { onSave(selectedTime) },
            )

            Spacer(Modifier.height(16.dp))

            PickupButton(
                text = "Back to my parking",
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = onBack,
            )
        }

        NavBar(currentIndex = 3, onTap = onNavTap)
    }
}
