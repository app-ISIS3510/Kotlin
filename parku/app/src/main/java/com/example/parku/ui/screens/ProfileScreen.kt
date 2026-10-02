package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun ProfileScreen(
    currentIndex: Int,
    fullName: String,
    email: String,
    busy: Boolean,
    isAdmin: Boolean,
    onNavTap: (Int) -> Unit,
    onEditProfile: () -> Unit,
    onMyVehicles: () -> Unit,
    onMyParking: () -> Unit,
    onMyFavorites: () -> Unit,
    onAnalyticsDashboard: () -> Unit,
    onSignOut: () -> Unit,
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
            ScreenHeader(title = "My profile")

            Spacer(Modifier.height(33.dp))

            // TARJETA DE PERFIL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.lightPurple)
                    .padding(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.white),
                    contentAlignment = Alignment.Center,
                ) {
                    DesignIcon("profile_user", size = 28.dp)
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = fullName.ifEmpty { "My profile" },
                    fontFamily = Inter,
                    fontSize = 25.sp,
                    lineHeight = 33.75.sp,
                    fontWeight = FontWeight.W700,
                    color = AppColors.darkText,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = email,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    lineHeight = 17.55.sp,
                    color = AppColors.greyText,
                )

                Spacer(Modifier.height(12.dp))

                PickupButton(
                    text = "Edit profile",
                    onPressed = onEditProfile,
                )
            }

            Spacer(Modifier.height(16.dp))

            MenuRow(
                icon = "profile_car",
                title = "My vehicles",
                subtitle = "Add your car or motorcycle plate",
                onClick = onMyVehicles,
            )

            Spacer(Modifier.height(16.dp))

            MenuRow(
                icon = "profile_clock",
                title = "My parking",
                subtitle = "Check your parking and pickup time",
                onClick = onMyParking,
            )

            Spacer(Modifier.height(16.dp))

            MenuRow(
                icon = "profile_heart",
                title = "My favorites",
                subtitle = "Your saved parking lots",
                onClick = onMyFavorites,
            )

            if (isAdmin) {
                Spacer(Modifier.height(16.dp))

                MenuRow(
                    icon = "profile_clock",
                    title = "Analytics dashboard",
                    subtitle = "View ParkU business metrics",
                    onClick = onAnalyticsDashboard,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Everything you need to get to campus and back.",
                fontFamily = Inter,
                fontSize = 13.sp,
                lineHeight = 17.55.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(16.dp))

            PickupButton(
                text = if (busy) "Signing out..." else "Sign out",
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = { if (!busy) onSignOut() },
            )
        }

        NavBar(currentIndex = currentIndex, onTap = onNavTap)
    }
}

@Composable
private fun MenuRow(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.white)
            .clickable(role = Role.Button) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesignIcon(icon)

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Inter,
                fontSize = 16.sp,
                lineHeight = 21.6.sp,
                fontWeight = FontWeight.W600,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = subtitle,
                fontFamily = Inter,
                fontSize = 12.sp,
                lineHeight = 16.2.sp,
                color = AppColors.greyText,
            )
        }

        Spacer(Modifier.width(12.dp))

        DesignIcon("profile_chevron", size = 16.dp, color = AppColors.greyText)
    }
}
