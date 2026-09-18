package com.example.parku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.parku.R

object AppColors {
    val primary = Color(0xFF5936E9)

    val background = Color(0xFFF8F7FC)

    val lightPurple = Color(0xFFEEE9FF)

    val darkText = Color(0xFF231B3D)

    val greyText = Color(0xFF706981)

    val white = Color.White
}

val Inter = FontFamily(Font(R.font.inter))

@Composable
fun ParkUTheme(content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(
        typography = Typography(
            displayLarge = base.displayLarge.copy(fontFamily = Inter),
            displayMedium = base.displayMedium.copy(fontFamily = Inter),
            displaySmall = base.displaySmall.copy(fontFamily = Inter),
            headlineLarge = base.headlineLarge.copy(fontFamily = Inter),
            headlineMedium = base.headlineMedium.copy(fontFamily = Inter),
            headlineSmall = base.headlineSmall.copy(fontFamily = Inter),
            titleLarge = base.titleLarge.copy(fontFamily = Inter),
            titleMedium = base.titleMedium.copy(fontFamily = Inter),
            titleSmall = base.titleSmall.copy(fontFamily = Inter),
            bodyLarge = base.bodyLarge.copy(fontFamily = Inter),
            bodyMedium = base.bodyMedium.copy(fontFamily = Inter),
            bodySmall = base.bodySmall.copy(fontFamily = Inter),
            labelLarge = base.labelLarge.copy(fontFamily = Inter),
            labelMedium = base.labelMedium.copy(fontFamily = Inter),
            labelSmall = base.labelSmall.copy(fontFamily = Inter),
        ),
        content = content,
    )
}
