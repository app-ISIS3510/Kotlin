package com.example.parku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.example.parku.ui.screens.MainNavigationScreen
import com.example.parku.ui.theme.AppColors
import com.example.parku.ui.theme.ParkUTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ParkUApp()
        }
    }
}

/** Equivale al widget ParkUApp de lib/main.dart. */
@Composable
fun ParkUApp() {
    ParkUTheme {
        // El equivalente del SafeArea que envuelve cada pantalla en Flutter.
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .safeDrawingPadding(),
        ) {
            MainNavigationScreen()
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun ParkUAppPreview() {
    ParkUApp()
}
