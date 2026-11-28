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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.BUDGET_MAX_MINUTES
import com.example.parku.data.BudgetEstimate
import com.example.parku.data.Parking
import com.example.parku.data.estimateBudget
import com.example.parku.data.formatCop
import com.example.parku.data.formatDuration
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter
import kotlin.math.abs

private val WithinBudget = Color(0xFF1E7A46)
private val OverBudget = Color(0xFFC0392B)

/**
 * Sprint 4: Parking Budget Planner. Se abre desde el detalle del parqueadero y estima
 * cuanto costaria la estadia planeada con el precio por minuto del parqueadero.
 */
@Composable
fun ParkingBudgetScreen(
    parking: Parking,
    pricePerMinute: Double,
    hours: String,
    minutes: String,
    budget: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onNavTap: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val totalMinutes = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
    val estimate = estimateBudget(pricePerMinute, totalMinutes, budget.toLongOrNull())

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
            ScreenHeader(title = "Budget planner", onBack = onBack)

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.white)
                    .padding(18.dp),
            ) {
                Text(
                    text = parking.name,
                    fontFamily = Inter,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.primary,
                )

                if (parking.address.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = parking.address,
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = AppColors.greyText,
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Rate: ${formatCop(pricePerMinute)} per minute",
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    color = AppColors.darkText,
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Planned stay",
                fontFamily = Inter,
                fontSize = 16.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.primary,
            )

            Spacer(Modifier.height(10.dp))

            Row {
                Column(Modifier.weight(1f)) {
                    ProfileField(
                        label = "Hours",
                        hint = "0",
                        value = hours,
                        onValueChange = onHoursChange,
                        keyboardType = KeyboardType.Number,
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    ProfileField(
                        label = "Minutes",
                        hint = "0",
                        value = minutes,
                        onValueChange = onMinutesChange,
                        keyboardType = KeyboardType.Number,
                    )
                }
            }

            if (totalMinutes > BUDGET_MAX_MINUTES) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Plan a stay of 24 hours or less.",
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = OverBudget,
                )
            }

            Spacer(Modifier.height(18.dp))

            ProfileField(
                label = "Budget (COP)",
                hint = "e.g. 15000",
                value = budget,
                onValueChange = onBudgetChange,
                keyboardType = KeyboardType.Number,
            )

            Spacer(Modifier.height(22.dp))

            EstimateCard(estimate)

            Spacer(Modifier.height(14.dp))

            Text(
                text = "This is an estimate. The final amount is calculated with the " +
                    "actual time when you end parking.",
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
private fun EstimateCard(estimate: BudgetEstimate?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.white)
            .padding(18.dp),
    ) {
        if (estimate == null) {
            Text(
                text = "Enter how long you plan to stay to see the estimated total.",
                fontFamily = Inter,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = AppColors.greyText,
            )
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Estimated total",
                fontFamily = Inter,
                fontSize = 15.sp,
                color = AppColors.greyText,
            )
            Text(
                text = formatDuration(estimate.minutes),
                fontFamily = Inter,
                fontSize = 15.sp,
                color = AppColors.greyText,
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = formatCop(estimate.total),
            fontFamily = Inter,
            fontSize = 28.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.primary,
        )

        val fits = estimate.fitsBudget
        val difference = estimate.difference
        if (fits != null && difference != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (fits) {
                    "Within your budget · ${formatCop(difference)} left"
                } else {
                    "Over your budget by ${formatCop(abs(difference))}"
                },
                fontFamily = Inter,
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                color = if (fits) WithinBudget else OverBudget,
            )
        } else {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Add a budget to check if this stay fits it.",
                fontFamily = Inter,
                fontSize = 14.sp,
                color = AppColors.greyText,
            )
        }
    }
}
