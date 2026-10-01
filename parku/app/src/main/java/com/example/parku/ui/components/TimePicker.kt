package com.example.parku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.formatTime12h
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

/**
 * Caja lavanda con el reloj y la hora elegida. Al tocarla se despliegan las
 * horas posibles, igual que el DropdownButton de pickup_time.dart en Flutter.
 */
@Composable
fun PickupTimeSelector(
    times: List<String>,
    selectedTime: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.lightPurple)
                .clickable(
                    enabled = times.isNotEmpty(),
                    role = Role.DropdownList,
                ) { expanded = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DesignIcon("clock", size = 28.dp)

            Spacer(Modifier.width(12.dp))

            Text(
                text = when {
                    times.isEmpty() -> "Closed for today"
                    selectedTime == null -> "Choose a time"
                    else -> formatTime12h(selectedTime)
                },
                modifier = Modifier.weight(1f),
                fontFamily = Inter,
                fontSize = 30.sp,
                lineHeight = 40.5.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.primary,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(AppColors.white)
                .heightIn(max = 320.dp),
        ) {
            times.forEach { time ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = formatTime12h(time),
                            fontFamily = Inter,
                            fontSize = 17.sp,
                            fontWeight = if (time == selectedTime) {
                                FontWeight.W700
                            } else {
                                FontWeight.W400
                            },
                            color = if (time == selectedTime) {
                                AppColors.primary
                            } else {
                                AppColors.darkText
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(time)
                    },
                )
            }
        }
    }
}
