package com.example.parku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.white)
                    .clickable(role = Role.Button) { onBack() },
                contentAlignment = Alignment.Center,
            ) {
                DesignIcon("arrow")
            }

            Spacer(Modifier.width(12.dp))
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontFamily = Inter,
            fontSize = 26.sp,
            lineHeight = 35.1.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )
    }
}

@Composable
fun PickupButton(
    text: String,
    onPressed: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = AppColors.primary,
    foreground: Color = AppColors.white,
    height: Dp = 52.dp,
) {
    Button(
        onClick = onPressed,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = foreground,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontSize = 16.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
fun SuggestedTimes(
    selectedTime: String,
    onChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val times = listOf("3:30", "4:00", "4:30")

    Row(modifier = modifier.fillMaxWidth()) {
        times.forEachIndexed { i, time ->
            if (i > 0) Spacer(Modifier.width(6.dp))

            PickupButton(
                text = time,
                background = AppColors.lightPurple,
                foreground = AppColors.primary,
                onPressed = { onChanged(time) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
