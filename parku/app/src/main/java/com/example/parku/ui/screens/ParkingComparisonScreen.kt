package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.COMPARISON_MAX_SELECTION
import com.example.parku.data.COMPARISON_MIN_SELECTION
import com.example.parku.data.ComparedParking
import com.example.parku.data.Parking
import com.example.parku.data.SmartVehicleType
import com.example.parku.data.comparisonHours
import com.example.parku.data.comparisonPrice
import com.example.parku.data.comparisonSpaces
import com.example.parku.data.comparisonStatus
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

private val LABEL_WIDTH = 88.dp
private val MIN_COLUMN_WIDTH = 100.dp
private val MAX_COLUMN_WIDTH = 240.dp

@Composable
fun ParkingComparisonScreen(
    selection: List<ComparedParking>,
    isOnline: Boolean,
    refreshing: Boolean,
    refreshFailed: Boolean,
    onRefresh: () -> Unit,
    onRemove: (String) -> Unit,
    onOpenDetails: (Parking) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        Column(modifier = Modifier.padding(start = 24.dp, top = 18.dp, end = 24.dp)) {
            ScreenHeader(title = "Compare Parking Lots", onBack = onBack)
        }

        if (selection.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Add parking lots from the list or search results.",
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    lineHeight = 21.6.sp,
                    color = AppColors.greyText,
                )
            }

            return@Column
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${selection.size} of $COMPARISON_MAX_SELECTION selected",
                    modifier = Modifier.weight(1f),
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                IconButton(
                    onClick = onRefresh,
                    enabled = isOnline && !refreshing,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh comparison",
                        tint = if (isOnline && !refreshing) {
                            AppColors.primary
                        } else {
                            AppColors.greyText
                        },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (selection.size < COMPARISON_MIN_SELECTION) {
                Text(
                    text = "Add one more parking lot to compare.",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(12.dp))
            }

            if (!isOnline || refreshFailed) {
                StatusNotice(
                    title = if (!isOnline) "Offline" else "Could not refresh comparison",
                    description = "Showing the last available parking data. " +
                        "Availability may have changed.",
                )

                Spacer(Modifier.height(12.dp))
            }

            if (refreshing && isOnline) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AppColors.primary,
                    trackColor = AppColors.lightPurple,
                )

                Spacer(Modifier.height(12.dp))
            }

            ComparisonTable(
                selection = selection,
                isOnline = isOnline,
                onRemove = onRemove,
                onOpenDetails = onOpenDetails,
            )
        }
    }
}

@Composable
private fun StatusNotice(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.lightPurple)
            .padding(14.dp),
    ) {
        Text(
            text = title,
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = description,
            fontFamily = Inter,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = AppColors.greyText,
        )
    }
}

@Composable
private fun ComparisonTable(
    selection: List<ComparedParking>,
    isOnline: Boolean,
    onRemove: (String) -> Unit,
    onOpenDetails: (Parking) -> Unit,
) {
    BoxWithConstraints {
        val columnWidth = ((maxWidth - LABEL_WIDTH) / selection.size)
            .coerceIn(MIN_COLUMN_WIDTH, MAX_COLUMN_WIDTH)
        val tableWidth = LABEL_WIDTH + columnWidth * selection.size

        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier
                    .width(tableWidth)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                HeaderRow(selection, columnWidth, onRemove)

                MetricRow("Price per minute", selection, columnWidth) {
                    comparisonPrice(it)
                }

                MetricRow("Hours", selection, columnWidth) {
                    comparisonHours(it)
                }

                MetricRow("Car spaces", selection, columnWidth) {
                    comparisonSpaces(it, SmartVehicleType.CAR)
                }

                MetricRow("Motorcycle spaces", selection, columnWidth) {
                    comparisonSpaces(it, SmartVehicleType.MOTORCYCLE)
                }

                RowDivider()

                Row(
                    modifier = Modifier
                        .background(AppColors.white)
                        .height(IntrinsicSize.Min),
                ) {
                    LabelCell("Data status")

                    selection.forEach { item ->
                        ColumnDivider()

                        ValueCell(comparisonStatus(item, isOnline), columnWidth)
                    }
                }

                RowDivider()

                DetailsRow(selection, columnWidth, onOpenDetails)
            }
        }
    }
}

@Composable
private fun HeaderRow(
    selection: List<ComparedParking>,
    columnWidth: Dp,
    onRemove: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(AppColors.lightPurple)
            .height(IntrinsicSize.Min),
    ) {
        LabelCell("Parking lot")

        selection.forEach { item ->
            ColumnDivider()

            Column(
                modifier = Modifier
                    .width(columnWidth)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = item.parking.name,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                IconButton(
                    onClick = { onRemove(item.parking.id) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.RemoveCircleOutline,
                        contentDescription = "Remove ${item.parking.name} from comparison",
                        tint = AppColors.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    selection: List<ComparedParking>,
    columnWidth: Dp,
    value: (Parking) -> String,
) {
    RowDivider()

    Row(
        modifier = Modifier
            .background(AppColors.white)
            .height(IntrinsicSize.Min),
    ) {
        LabelCell(label)

        selection.forEach { item ->
            ColumnDivider()

            ValueCell(
                text = if (item.isUnavailable) "Unavailable" else value(item.parking),
                columnWidth = columnWidth,
            )
        }
    }
}

@Composable
private fun DetailsRow(
    selection: List<ComparedParking>,
    columnWidth: Dp,
    onOpenDetails: (Parking) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(AppColors.white)
            .height(IntrinsicSize.Min),
    ) {
        Spacer(Modifier.width(LABEL_WIDTH))

        selection.forEach { item ->
            ColumnDivider()

            Box(
                modifier = Modifier
                    .width(columnWidth)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                TextButton(
                    onClick = { onOpenDetails(item.parking) },
                    enabled = !item.isUnavailable,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.primary,
                    ),
                ) {
                    Text(
                        text = "Details",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelCell(text: String) {
    Box(
        modifier = Modifier
            .width(LABEL_WIDTH)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontSize = 13.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )
    }
}

@Composable
private fun ValueCell(text: String, columnWidth: Dp) {
    Box(
        modifier = Modifier
            .width(columnWidth)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.W500,
            color = AppColors.darkText,
        )
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppColors.lightPurple),
    )
}

@Composable
private fun ColumnDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(AppColors.lightPurple),
    )
}
