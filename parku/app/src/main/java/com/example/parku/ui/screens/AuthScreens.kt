package com.example.parku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parku.ui.components.PickupButton
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.Inter

@Composable
fun SignInScreen(
    busy: Boolean,
    error: String?,
    onSignIn: (String, String) -> Unit,
    onGoToCreateAccount: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthScaffold(
        title = "ParkU",
        subtitle = "Welcome back",
        error = error,
    ) {
        AuthField(
            label = "Email address",
            value = email,
            onValueChange = { email = it },
            keyboardType = KeyboardType.Email,
        )

        Spacer(Modifier.height(16.dp))

        AuthField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            keyboardType = KeyboardType.Password,
            isPassword = true,
        )

        Spacer(Modifier.height(24.dp))

        PickupButton(
            text = if (busy) "Signing in..." else "Sign in",
            onPressed = { if (!busy) onSignIn(email, password) },
        )

        Spacer(Modifier.height(12.dp))

        PickupButton(
            text = "Create an account",
            background = AppColors.white,
            foreground = AppColors.primary,
            onPressed = onGoToCreateAccount,
        )
    }
}

@Composable
fun CreateAccountScreen(
    busy: Boolean,
    error: String?,
    onCreateAccount: (String, String, String) -> Unit,
    onGoToSignIn: () -> Unit,
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AuthScaffold(
        title = "Create account",
        subtitle = "Make room for easier days",
        error = error,
    ) {
        AuthField(
            label = "Full name",
            value = fullName,
            onValueChange = { fullName = it },
        )

        Spacer(Modifier.height(16.dp))

        AuthField(
            label = "Email address",
            value = email,
            onValueChange = { email = it },
            keyboardType = KeyboardType.Email,
        )

        Spacer(Modifier.height(16.dp))

        AuthField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            keyboardType = KeyboardType.Password,
            isPassword = true,
            helper = "Use at least 8 characters.",
        )

        Spacer(Modifier.height(24.dp))

        PickupButton(
            text = if (busy) "Creating..." else "Create account",
            onPressed = { if (!busy) onCreateAccount(fullName, email, password) },
        )

        Spacer(Modifier.height(12.dp))

        PickupButton(
            text = "Already have an account? Sign in",
            background = AppColors.white,
            foreground = AppColors.primary,
            onPressed = onGoToSignIn,
        )
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    error: String?,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 40.dp),
    ) {
        Spacer(Modifier.height(40.dp))

        Text(
            text = title,
            fontFamily = Inter,
            fontSize = 34.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.primary,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontFamily = Inter,
            fontSize = 20.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.W600,
            color = AppColors.darkText,
        )

        Spacer(Modifier.height(32.dp))

        content()

        if (error != null) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppColors.lightPurple)
                    .padding(14.dp),
                fontFamily = Inter,
                fontSize = 14.sp,
                lineHeight = 18.9.sp,
                color = AppColors.primary,
            )
        }
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    helper: String? = null,
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
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.white)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = AppColors.darkText,
                ),
                cursorBrush = SolidColor(AppColors.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Next,
                ),
                visualTransformation = if (isPassword) {
                    PasswordVisualTransformation()
                } else {
                    androidx.compose.ui.text.input.VisualTransformation.None
                },
            )
        }

        if (helper != null) {
            Spacer(Modifier.height(6.dp))

            Text(
                text = helper,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = AppColors.greyText,
            )
        }
    }
}

/** Pantalla mínima para registrar el primer vehículo, sin el cual no se puede parquear. */
@Composable
fun AddVehicleScreen(
    busy: Boolean,
    onAdd: (String, String) -> Unit,
    onBack: (() -> Unit)? = null,
) {
    var plate by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("car") }

    AuthScaffold(
        title = "Add a vehicle",
        subtitle = "You need one to start parking",
        error = null,
    ) {
        AuthField(
            label = "Plate",
            value = plate,
            onValueChange = { plate = it },
            helper = "For example ABC123.",
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Vehicle type",
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.W700,
            color = AppColors.darkText,
        )

        Spacer(Modifier.height(8.dp))

        listOf("car" to "Car", "motorcycle" to "Motorcycle").forEach { (value, label) ->
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (type == value) AppColors.lightPurple else AppColors.white)
                    .clickable(role = Role.RadioButton) { type = value }
                    .padding(16.dp),
                fontFamily = Inter,
                fontSize = 16.sp,
                fontWeight = if (type == value) FontWeight.W700 else FontWeight.W400,
                color = if (type == value) AppColors.primary else AppColors.darkText,
            )
        }

        Spacer(Modifier.height(16.dp))

        PickupButton(
            text = if (busy) "Saving..." else "Save vehicle",
            onPressed = { if (!busy && plate.isNotBlank()) onAdd(type, plate) },
        )

        if (onBack != null) {
            Spacer(Modifier.height(12.dp))

            PickupButton(
                text = "Back",
                background = AppColors.white,
                foreground = AppColors.primary,
                onPressed = onBack,
            )
        }
    }
}
