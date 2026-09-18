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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.DesignIcon
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.components.SuggestedTimes
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter
import kotlinx.coroutines.launch

@Composable
fun PickupTimeScreen(
    onNavTap: (Int) -> Unit,
    onStartParking: (String) -> Unit,
    onBack: () -> Unit,
) {
    // Solo cambia la maqueta; no se guarda en un servidor.
    var selectedTime by rememberSaveable { mutableStateOf("4:00") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                title = "Pickup time",
                onBack = onBack,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "When will you pick it up?",
                fontFamily = Inter,
                fontSize = 25.sp,
                lineHeight = 33.75.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Choose an approximate time to pick up your vehicle.",
                fontFamily = Inter,
                fontSize = 15.sp,
                lineHeight = 20.25.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.white)
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DesignIcon("car")
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Car",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        lineHeight = 21.6.sp,
                        fontWeight = FontWeight.W600,
                        color = AppColors.darkText,
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "ABC123",
                    fontFamily = Inter,
                    fontSize = 22.sp,
                    lineHeight = 29.7.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(12.dp))

                PickupButton(
                    text = "Change vehicle",
                    background = AppColors.white,
                    foreground = AppColors.primary,
                    onPressed = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Demo vehicle: Car · ABC123")
                        }
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Today · Pickup time",
                fontFamily = Inter,
                fontSize = 14.sp,
                lineHeight = 18.9.sp,
                fontWeight = FontWeight.W600,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.lightPurple)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                DesignIcon("clock", size = 28.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "$selectedTime PM",
                    fontFamily = Inter,
                    fontSize = 30.sp,
                    lineHeight = 40.5.sp,
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
                text = "Start parking",
                onPressed = { onStartParking(selectedTime) },
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "You can change this time whenever you need.",
                fontFamily = Inter,
                fontSize = 12.sp,
                lineHeight = 16.2.sp,
                color = AppColors.greyText,
            )
        }

        SnackbarHost(hostState = snackbarHostState)

        NavBar(currentIndex = 1, onTap = onNavTap)
    }
}
