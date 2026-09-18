package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.components.SearchByNameLabel
import com.example.parku.ui.components.SearchQueryBox
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun NoSearchResultsScreen(
    query: String,
    onEditSearch: () -> Unit,
    onShowAll: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 24.dp),
        ) {
            ScreenHeader(title = "No search results", onBack = onBack)

            Spacer(Modifier.height(20.dp))

            SearchByNameLabel()

            Spacer(Modifier.height(10.dp))

            SearchQueryBox(query)

            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppColors.lightPurple)
                    .padding(horizontal = 18.dp, vertical = 20.dp),
            ) {
                Text(
                    text = "No parking lots found",
                    fontFamily = Inter,
                    fontSize = 26.sp,
                    lineHeight = 35.1.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Try another name or browse all parking lots near campus.",
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    lineHeight = 20.25.sp,
                    color = AppColors.greyText,
                )
            }

            Spacer(Modifier.height(14.dp))

            PickupButton(
                text = "Edit search",
                onPressed = onEditSearch,
            )

            Spacer(Modifier.height(12.dp))

            PickupButton(
                text = "Show all parking lots",
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = onShowAll,
            )
        }

        NavBar(currentIndex = 1, onTap = onNavTap)
    }
}
