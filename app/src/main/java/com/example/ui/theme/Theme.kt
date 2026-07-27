package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VibrantColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeContainer,
    onPrimaryContainer = OnPrimaryOrangeContainer,
    secondary = SecondaryWarm,
    secondaryContainer = SecondaryWarmContainer,
    onSecondaryContainer = OnPrimaryOrangeContainer,
    background = WarmBackground,
    onBackground = TextDarkBrown,
    surface = WarmSurface,
    onSurface = TextDarkBrown,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = TextDarkBrown,
    outline = BorderWarm,
    outlineVariant = BorderAccent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = VibrantColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

