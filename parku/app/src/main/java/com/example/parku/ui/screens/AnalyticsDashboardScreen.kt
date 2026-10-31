package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.DashboardData
import com.example.parku.ui.components.BarDatum
import com.example.parku.ui.components.DashboardCard
import com.example.parku.ui.components.RankedList
import com.example.parku.ui.components.SlotList
import com.example.parku.ui.components.VerticalBarChart
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.components.StatNumber
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

/**
 * Espejo de lib/screens/analytics_dashboard.dart. Las seis vistas ya vienen
 * calculadas desde Supabase, asi que aqui solo se presentan.
 */
@Composable
fun AnalyticsDashboardScreen(
    data: DashboardData?,
    loading: Boolean,
    error: String?,
    savedAt: Long,
    onRetry: () -> Unit,
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
                .padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 24.dp),
        ) {
            ScreenHeader(title = "ParkU Analytics", onBack = onBack)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Business Questions Dashboard",
                fontFamily = Inter,
                fontSize = 14.sp,
                lineHeight = 18.9.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(20.dp))

            when {
                loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AppColors.primary)
                }

                error != null -> Column {
                    Text(
                        text = "Error loading dashboard:\n$error",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppColors.lightPurple)
                            .padding(16.dp),
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        lineHeight = 18.9.sp,
                        color = AppColors.primary,
                    )

                    Spacer(Modifier.height(16.dp))

                    com.example.parku.ui.components.PickupButton(
                        text = "Try again",
                        onPressed = onRetry,
                    )
                }

                data != null -> Column {
                    if (savedAt > 0) {
                        Text(
                            text = "Offline · these results are from " + formatSavedAt(savedAt) +
                                " and may not be current.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppColors.lightPurple)
                                .padding(16.dp),
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            lineHeight = 17.55.sp,
                            color = AppColors.primary,
                        )

                        Spacer(Modifier.height(18.dp))
                    }

                    DashboardContent(data)
                }
            }
        }

        NavBar(currentIndex = 3, onTap = onNavTap)
    }
}

private fun formatSavedAt(millis: Long): String =
    java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(millis))

/** Mismos nombres cortos que usa _Bq5Content en Flutter. */
private fun shortEventName(event: String): String = when (event) {
    "pickup_time_changed" -> "Pickup"
    "navigation_opened" -> "Navigation"
    "favorite_added" -> "Favorite"
    "parking_ended" -> "End"
    else -> event
}

private fun screenLabel(screen: String): String = when (screen) {
    "parking_list" -> "Parking list"
    "vehicles" -> "Vehicles"
    "my_parking" -> "My parking"
    "analytics_dashboard" -> "Dashboard"
    else -> screen
}

@Composable
private fun DashboardContent(data: DashboardData) {
    DashboardCard(
        title = "BQ1 · Parking starts by day",
        subtitle = "Last 7 days",
    ) {
        VerticalBarChart(
            // "2026-10-02" -> "10-02", como el substring(5, 10) de Flutter.
            data = data.bq1.map { BarDatum(it.day.drop(5).take(5), it.sessionsStarted) },
        )
    }

    Spacer(Modifier.height(18.dp))

    DashboardCard(
        title = "BQ2 · Top completed parking lots",
        subtitle = "Last 7 days",
    ) {
        RankedList(
            data = data.bq2.map { BarDatum(it.parkingName, it.completedSessions) },
        )
    }

    Spacer(Modifier.height(18.dp))

    DashboardCard(
        title = "BQ3 · Parking flow abandonment",
        subtitle = "Last 7 days",
    ) {
        VerticalBarChart(
            data = data.bq3.map {
                val label = if (it.flowStep.contains("Parking details")) {
                    "Details →\nPickup"
                } else {
                    "Pickup →\nStart"
                }
                BarDatum(label, it.abandonmentCount)
            },
        )
    }

    Spacer(Modifier.height(18.dp))

    DashboardCard(
        title = "BQ4 · Users who added favorites",
        subtitle = "Last 7 days",
    ) {
        // Una sola cifra: un numero grande se lee mejor que una barra suelta.
        StatNumber(
            value = data.bq4.toString(),
            caption = "Users who added a favorite",
        )
    }

    Spacer(Modifier.height(18.dp))

    DashboardCard(
        title = "BQ5 · Action usage",
        subtitle = "Last 7 days",
    ) {
        VerticalBarChart(
            data = data.bq5.map { BarDatum(shortEventName(it.eventType), it.usageCount) },
        )
    }

    Spacer(Modifier.height(18.dp))

    DashboardCard(
        title = "BQ6 · Top parking by time slot",
        subtitle = "Last 30 days",
    ) {
        SlotList(
            slots = data.bq6.map { Triple(it.timeSlot, it.parkingName, it.parkingStarts) },
        )
    }

    Spacer(Modifier.height(18.dp))

    // Sprint 3: BQ2 de la lista del Sprint 1 (tiempo de carga por pantalla y dispositivo).
    DashboardCard(
        title = "Slowest screens by device",
        subtitle = "Average load time in ms (network)",
    ) {
        RankedList(
            data = data.loadTimes
                .filter { it.avgNetworkMs != null }
                .take(8)
                .map {
                    BarDatum(
                        "${screenLabel(it.screen)} · ${it.deviceModel} · ${it.osVersion}",
                        it.avgNetworkMs ?: 0,
                    )
                },
        )
    }

    Spacer(Modifier.height(18.dp))

    DashboardCard(
        title = "Cache effect on load time",
        subtitle = "Average load time in ms (from local cache)",
    ) {
        RankedList(
            data = data.loadTimes
                .filter { it.avgCachedMs != null }
                .take(8)
                .map {
                    BarDatum(
                        "${screenLabel(it.screen)} · ${it.deviceModel}",
                        it.avgCachedMs ?: 0,
                    )
                },
        )
    }
}
