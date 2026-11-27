package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.parku.data.SessionSummary
import com.example.parku.data.isoToDisplayTime
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun SessionSummaryScreen(
    summary: SessionSummary,
    currentIndex: Int,
    onDone: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(start = 30.dp, top = 22.dp, end = 30.dp, bottom = 20.dp),
        ) {
            Text(
                text = "Parking summary",
                fontFamily = Inter,
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(AppColors.white)
                    .padding(24.dp),
            ) {
                Text(
                    text = summary.parkingName,
                    fontFamily = Inter,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                if (summary.parkingAddress.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = summary.parkingAddress,
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        color = AppColors.greyText,
                    )
                }

                Spacer(Modifier.height(24.dp))

                SummaryRow("Vehicle", listOfNotNull(summary.vehicleLabel, summary.plate).joinToString(" - "))
                SummaryRow("Started", isoToDisplayTime(summary.startedAt))
                SummaryRow("Ended", isoToDisplayTime(summary.endedAt))
                SummaryRow("Duration", summary.durationLabel)

                Spacer(Modifier.height(8.dp))

                SummaryRow("Total", summary.totalLabel, strong = true)

                Spacer(Modifier.height(28.dp))

                PickupButton(
                    text = "Done",
                    height = 56.dp,
                    onPressed = onDone,
                )
            }
        }

        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}

@Composable
private fun SummaryRow(label: String, value: String, strong: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 16.sp,
            color = AppColors.greyText,
        )
        Text(
            text = value,
            fontFamily = Inter,
            fontSize = if (strong) 20.sp else 16.sp,
            fontWeight = if (strong) FontWeight.W700 else FontWeight.W500,
            color = if (strong) AppColors.primary else AppColors.darkText,
        )
    }
}
