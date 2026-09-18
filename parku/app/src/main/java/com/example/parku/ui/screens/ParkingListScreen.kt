package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.ParkingCard
import com.example.parku.ui.data.ParkingLot
import com.example.parku.ui.data.parkingLots
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun ParkingListScreen(
    currentIndex: Int,
    onNavTap: (Int) -> Unit,
    onSelectParking: ((ParkingLot) -> Unit)? = null,
    onOpenSearch: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        // HEADER
        Text(
            text = "Parking lots",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, top = 22.dp, end = 32.dp, bottom = 18.dp),
            fontFamily = Inter,
            fontSize = 32.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        // SEARCH BAR
        Row(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.white)
                .clickable { onOpenSearch() }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = AppColors.greyText,
                modifier = Modifier.size(25.dp),
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Search parking lots",
                fontFamily = Inter,
                fontSize = 16.sp,
                color = AppColors.greyText,
            )
        }

        Spacer(Modifier.height(24.dp))

        // SUBTITLE
        Text(
            text = "Find a place to park",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            fontFamily = Inter,
            fontSize = 22.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        Spacer(Modifier.height(16.dp))

        // LIST
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(parkingLots) { parking ->
                ParkingCard(
                    name = parking.name,
                    address = parking.address,
                    type = parking.type,
                    onTap = if (onSelectParking == null) null else {
                        { onSelectParking(parking) }
                    },
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}
