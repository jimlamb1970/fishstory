package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
val PackersGreenContainer = Color(0xFFC8E6C9)
val PackersGoldContainer = Color(0xFFFFF59D)

// High-Contrast Primary/Secondary (For buttons and headers)
val PackersGreen = Color(0xFF204E32)
val PackersGold = Color(0xFFFFB81C)

// "On" Colors (For text legibility)
val OnPackersGreenContainer = Color(0xFF002105) // Near-black green text
val OnPackersGoldContainer = Color(0xFF241D00)  // Deep brownish-black text

val LightPackersColorScheme = lightColorScheme(
    primary = PackersGreen,
    onPrimary = PackersGold,
    primaryContainer = PackersGreenContainer,
    onPrimaryContainer = PackersGreen,

    secondary = PackersGreen, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = PackersGold,
    onTertiary = PackersGreen, // Blue text on Maize background looks sharp
    tertiaryContainer = PackersGoldContainer,
    onTertiaryContainer = PackersGreen,

    background = Color.White,
    onBackground = PackersGreen,

    surface = Color.White,
    onSurface = PackersGreen,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = PackersGreen,
    surfaceVariant = PackersGold,
    onSurfaceVariant = PackersGreen,
)
