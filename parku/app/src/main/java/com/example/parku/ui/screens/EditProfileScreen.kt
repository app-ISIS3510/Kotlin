package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.data.ProfileValidation
import com.example.parku.ui.components.NavBar
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.components.ScreenHeader
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun EditProfileScreen(
    initialName: String,
    initialEmail: String,
    busy: Boolean,
    onSave: (String, String) -> Unit,
    onNavTap: (Int) -> Unit,
    onBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

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
            ScreenHeader(title = "Edit profile", onBack = onBack)

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Keep your details up to date",
                fontFamily = Inter,
                fontSize = 20.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.W600,
                color = AppColors.darkText,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Update your profile details.",
                fontFamily = Inter,
                fontSize = 14.sp,
                lineHeight = 18.9.sp,
                color = AppColors.greyText,
            )

            Spacer(Modifier.height(16.dp))

            ProfileField(
                label = "Full name",
                hint = "Enter your full name",
                value = name,
                onValueChange = { name = it },
                enabled = !busy,
            )

            Spacer(Modifier.height(16.dp))

            ProfileField(
                label = "Email address",
                hint = "Enter your email",
                value = email,
                onValueChange = { email = it },
                enabled = !busy,
                keyboardType = KeyboardType.Email,
            )

            if (error != null) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = error.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.lightPurple)
                        .padding(12.dp),
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 18.9.sp,
                    color = AppColors.primary,
                )
            }

            Spacer(Modifier.height(16.dp))

            PickupButton(
                text = if (busy) "Saving..." else "Save changes",
                onPressed = {
                    if (busy) return@PickupButton

                    val problem = ProfileValidation.validateName(name)
                        ?: ProfileValidation.validateEmail(email)

                    error = problem
                    if (problem == null) onSave(name, email)
                },
            )

            Spacer(Modifier.height(16.dp))

            PickupButton(
                text = "Cancel",
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = { if (!busy) onBack() },
            )
        }

        NavBar(currentIndex = 3, onTap = onNavTap)
    }
}

@Composable
internal fun ProfileField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.white)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = AppColors.greyText,
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = AppColors.darkText,
                ),
                cursorBrush = SolidColor(AppColors.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done,
                ),
            )
        }
    }
}
