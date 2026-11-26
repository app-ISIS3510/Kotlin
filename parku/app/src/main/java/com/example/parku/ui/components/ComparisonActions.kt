package com.example.parku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.COMPARISON_MAX_SELECTION
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

// Add / remove a parking lot from the comparison, shown inside a card.
@Composable
fun ComparisonToggle(
    selected: Boolean,
    hasCapacity: Boolean,
    onToggle: () -> Unit,
) {
    TextButton(
        onClick = onToggle,
        enabled = selected || hasCapacity,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = AppColors.primary),
    ) {
        Icon(
            imageVector = if (selected) {
                Icons.Filled.CheckCircleOutline
            } else {
                Icons.Filled.AddCircleOutline
            },
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = if (selected) "Added to compare" else "Add to compare",
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

// Selection summary above the nav bar, with the entry point to the comparison.
@Composable
fun ComparisonBar(
    count: Int,
    canCompare: Boolean,
    onCompare: () -> Unit,
) {
    if (count == 0) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.lightPurple)
            .padding(start = 16.dp, top = 4.dp, end = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$count of $COMPARISON_MAX_SELECTION selected",
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = AppColors.darkText,
            )
        }

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = onCompare,
            enabled = canCompare,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.primary,
                contentColor = AppColors.white,
            ),
        ) {
            Text(
                text = "Compare",
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
            )
        }
    }
}
