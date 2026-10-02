package com.example.parku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

/** Una barra del grafico: su etiqueta y su valor. */
data class BarDatum(val label: String, val value: Int)

@Composable
private fun NoData(modifier: Modifier = Modifier) {
    Text(
        text = "No data available.",
        modifier = modifier,
        fontFamily = Inter,
        fontSize = 14.sp,
        color = AppColors.greyText,
    )
}

/**
 * Barras verticales, como el BarChart de fl_chart que usa Flutter en BQ1, BQ3
 * y BQ5. Alto fijo de 230 y barras de 22, igual que alla.
 */
@Composable
fun VerticalBarChart(
    data: List<BarDatum>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        NoData(modifier)
        return
    }

    // El tope de la escala deja un escalon libre arriba, como el maxY + 1 de Flutter.
    val maxValue = (data.maxOf { it.value } + 1).coerceAtLeast(1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEach { datum ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight(),
            ) {
                Text(
                    text = datum.value.toString(),
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(4.dp))

                // La columna de la barra ocupa el alto restante; el peso
                // reparte relleno vacio arriba y barra abajo.
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    val fraction = datum.value.toFloat() / maxValue

                    Spacer(Modifier.weight((1f - fraction).coerceAtLeast(0.001f)))

                    Box(
                        modifier = Modifier
                            .weight(fraction.coerceAtLeast(0.001f))
                            .width(22.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(AppColors.primary),
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = datum.label,
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    color = AppColors.greyText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Lista con puesto, nombre y cifra. Es el formato de BQ2 en Flutter. */
@Composable
fun RankedList(
    data: List<BarDatum>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        NoData(modifier)
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        data.forEachIndexed { index, datum ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.background)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AppColors.lightPurple),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "#${index + 1}",
                        fontFamily = Inter,
                        fontWeight = FontWeight.W700,
                        color = AppColors.primary,
                    )
                }

                Spacer(Modifier.width(14.dp))

                Text(
                    text = datum.label,
                    modifier = Modifier.weight(1f),
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = AppColors.darkText,
                )

                Text(
                    text = datum.value.toString(),
                    fontFamily = Inter,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.primary,
                )
            }
        }
    }
}

/** Tarjeta por franja horaria con barra de progreso. Es el formato de BQ6. */
@Composable
fun SlotList(
    slots: List<Triple<String, String, Int>>,
    modifier: Modifier = Modifier,
) {
    if (slots.isEmpty()) {
        NoData(modifier)
        return
    }

    val maxStarts = slots.maxOf { it.third }

    Column(modifier = modifier.fillMaxWidth()) {
        slots.forEach { (slot, parkingName, starts) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.background)
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = slot,
                        modifier = Modifier.weight(1f),
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = AppColors.greyText,
                    )

                    Text(
                        text = "$starts starts",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W700,
                        color = AppColors.primary,
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = parkingName,
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.lightPurple),
                ) {
                    val fraction = if (maxStarts == 0) 0f else starts.toFloat() / maxStarts

                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(9.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColors.primary),
                        )
                    }
                }
            }
        }
    }
}

/** Numero grande para las preguntas que se responden con un solo dato. */
@Composable
fun StatNumber(
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            fontFamily = Inter,
            fontSize = 44.sp,
            lineHeight = 52.8.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.primary,
        )

        Text(
            text = caption,
            fontFamily = Inter,
            fontSize = 13.sp,
            lineHeight = 17.55.sp,
            color = AppColors.greyText,
        )
    }
}

/** Tarjeta blanca con titulo y subtitulo, como las del dashboard de Flutter. */
@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.white)
            .padding(18.dp),
    ) {
        Text(
            text = title,
            fontFamily = Inter,
            fontSize = 16.sp,
            lineHeight = 21.6.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        Text(
            text = subtitle,
            fontFamily = Inter,
            fontSize = 12.sp,
            lineHeight = 16.2.sp,
            color = AppColors.greyText,
        )

        Spacer(Modifier.height(16.dp))

        content()
    }
}
