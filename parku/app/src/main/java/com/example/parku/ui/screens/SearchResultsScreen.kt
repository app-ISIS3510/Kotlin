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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.DesignIcon
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.components.SearchByNameLabel
import com.example.parku.ui.components.SearchQueryBox
import com.example.parku.ui.components.SectionCaption
import com.example.parku.ui.data.ParkingLot
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun SearchResultsScreen(
    query: String,
    results: List<ParkingLot>,
    onSelectParking: (ParkingLot) -> Unit,
    onClearSearch: () -> Unit,
    onNavTap: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, top = 18.dp, end = 24.dp),
        ) {
            ScreenHeader(title = "Search results", onBack = onBack)

            Spacer(Modifier.height(20.dp))

            SearchByNameLabel()

            Spacer(Modifier.height(10.dp))

            SearchQueryBox(query)

            Spacer(Modifier.height(12.dp))

            PickupButton(
                text = "Clear search",
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = onClearSearch,
            )

            Spacer(Modifier.height(16.dp))

            SectionCaption("MATCHING PARKING LOTS")

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(results) { parking ->
                    SearchResultCard(
                        parking = parking,
                        onClick = { onSelectParking(parking) },
                    )
                }
            }
        }

        NavBar(currentIndex = 1, onTap = onNavTap)
    }
}

@Composable
private fun SearchResultCard(
    parking: ParkingLot,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.white)
            .clickable(role = Role.Button) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesignIcon("parking", size = 26.dp)

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = parking.name,
                fontFamily = Inter,
                fontSize = 18.sp,
                lineHeight = 24.3.sp,
                fontWeight = FontWeight.W700,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = parking.address,
                fontFamily = Inter,
                fontSize = 14.sp,
                lineHeight = 18.9.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = parking.type,
                fontFamily = Inter,
                fontSize = 13.sp,
                lineHeight = 17.55.sp,
                color = AppColors.primary,
            )
        }
    }
}
