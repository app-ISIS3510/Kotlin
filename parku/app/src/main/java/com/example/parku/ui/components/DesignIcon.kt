package com.example.parku.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.parku.R
import com.example.parku.ui.theme.AppColors

//Iconos

// Los archivos son los iconos exportados del Figma del equipo.
@Composable
fun DesignIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = AppColors.primary,
) {
    Icon(
        painter = painterResource(iconRes(name)),
        contentDescription = null,
        tint = color,
        modifier = modifier.size(size),
    )
}

private fun iconRes(name: String): Int = when (name) {
    "arrow" -> R.drawable.ic_arrow
    "car" -> R.drawable.ic_car
    "clock" -> R.drawable.ic_clock
    "heart" -> R.drawable.ic_heart
    "heart_filled" -> R.drawable.ic_heart_filled
    "heart1" -> R.drawable.ic_heart1
    "map" -> R.drawable.ic_map
    "parking" -> R.drawable.ic_parking
    "user" -> R.drawable.ic_user
    else -> throw IllegalArgumentException("Icono desconocido: $name")
}
