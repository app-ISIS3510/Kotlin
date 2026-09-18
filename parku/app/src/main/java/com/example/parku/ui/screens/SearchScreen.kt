package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.components.SearchByNameLabel
import com.example.parku.ui.components.SearchInput
import com.example.parku.ui.components.SectionCaption
import com.example.parku.ui.data.parkingLots
import com.example.parku.ui.data.searchParkingLots
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun SearchScreen(
    initialQuery: String = "",
    onSubmit: (String) -> Unit,
    onNavTap: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf(initialQuery) }

    // Con el campo vacio se sugieren todos; al escribir se filtran en vivo.
    val typing = query.isNotBlank()
    val matches = if (typing) searchParkingLots(query) else parkingLots

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
            ScreenHeader(title = "Search", onBack = onBack)

            Spacer(Modifier.height(20.dp))

            SearchByNameLabel()

            Spacer(Modifier.height(10.dp))

            SearchInput(
                value = query,
                onValueChange = { query = it },
                onSubmit = onSubmit,
            )

            Spacer(Modifier.height(16.dp))

            SectionCaption(
                if (typing) "MATCHING PARKING LOTS" else "SUGGESTED PARKING LOTS",
            )

            Spacer(Modifier.height(12.dp))

            if (typing && matches.isEmpty()) {
                // Mismo aviso que la pantalla de "No search results", pero aqui
                // mismo: el campo sigue arriba para seguir corrigiendo el nombre.
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
                    text = "Show all parking lots",
                    background = AppColors.white,
                    foreground = AppColors.primary,
                    onPressed = { query = "" },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(matches) { parking ->
                        PickupButton(
                            text = parking.name,
                            background = AppColors.white,
                            foreground = AppColors.primary,
                            onPressed = { onSubmit(parking.name) },
                        )
                    }
                }
            }
        }

        NavBar(currentIndex = 1, onTap = onNavTap)
    }
}
