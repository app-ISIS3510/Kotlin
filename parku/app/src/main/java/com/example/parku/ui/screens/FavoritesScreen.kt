package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

/** Equivale al Map<String, String> que usa la version en Flutter. */
data class Favorite(
    val name: String,
    val address: String,
)

@Composable
fun FavoritesScreen(
    favorites: List<Favorite>,
    onRemove: (Int) -> Unit,
    onNavTap: (Int) -> Unit,
    onSelectFavorite: (Favorite) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 24.dp, top = 18.dp, end = 24.dp, bottom = 24.dp),
        ) {
            item {
                ScreenHeader(title = "Favorites")

                Spacer(Modifier.height(33.dp))

                Text(
                    text = "Your saved places, always handy.",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 18.9.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(12.dp))
            }

            itemsIndexed(favorites) { i, favorite ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.white)
                        .clickable(role = Role.Button) { onSelectFavorite(favorite) }
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        DesignIcon("heart")
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = favorite.name,
                            modifier = Modifier.weight(1f),
                            fontFamily = Inter,
                            fontSize = 17.sp,
                            lineHeight = 22.95.sp,
                            fontWeight = FontWeight.W600,
                            color = AppColors.darkText,
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = favorite.address,
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        lineHeight = 16.2.sp,
                        color = AppColors.greyText,
                    )

                    Spacer(Modifier.height(8.dp))

                    PickupButton(
                        text = "Remove",
                        height = 44.dp,
                        background = AppColors.white,
                        foreground = AppColors.primary,
                        onPressed = { onRemove(i) },
                    )
                }

                if (i < favorites.size - 1) Spacer(Modifier.height(12.dp))
            }
        }

        NavBar(currentIndex = 2, onTap = onNavTap)
    }
}
