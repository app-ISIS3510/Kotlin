package com.example.parku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun ParkingCard(
    name: String,
    address: String,
    type: String,
    modifier: Modifier = Modifier,
    onTap: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.white)
            .then(if (onTap != null) Modifier.clickable { onTap() } else Modifier)
            .padding(18.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // ICON BOX
        Column(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.lightPurple),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.LocalParking,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(30.dp),
            )
        }

        Spacer(Modifier.width(16.dp))

        // TEXT
        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                fontFamily = Inter,
                fontSize = 18.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = address,
                fontFamily = Inter,
                fontSize = 14.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = type,
                fontFamily = Inter,
                fontSize = 14.sp,
                color = AppColors.greyText,
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AppColors.greyText,
        )
    }
}
