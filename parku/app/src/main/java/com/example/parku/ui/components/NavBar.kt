package com.example.parku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

private val icons = listOf("map", "parking", "heart", "user")
private val labels = listOf("Map", "Parking lots", "Favorites", "Profile")

@Composable
fun NavBar(
    currentIndex: Int,
    onTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.white)
            .padding(8.dp),
    ) {
        labels.forEachIndexed { i, label ->
            if (i > 0) Spacer(Modifier.width(2.dp))

            val selected = currentIndex == i

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) AppColors.lightPurple else Color.Transparent)
                    .clickable(role = Role.Tab) { onTap(i) }
                    .defaultMinSize(minHeight = 56.dp)
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                DesignIcon(
                    name = icons[i],
                    size = 22.dp,
                    color = if (selected) AppColors.primary else AppColors.greyText,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = label,
                    fontFamily = Inter,
                    fontSize = 10.sp,
                    lineHeight = 13.5.sp,
                    fontWeight = FontWeight.W600,
                    color = if (selected) AppColors.primary else AppColors.greyText,
                )
            }
        }
    }
}
