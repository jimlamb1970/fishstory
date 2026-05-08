package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
val BadgerRedContainer = Color(0xFFFFDADA)
val BadgerWhiteContainer = Color(0xFFF5F5F5)

// Official Team Colors
val BadgerRed = Color(0xFFC5050C)
val BadgerBlack = Color(0xFF282728)

// "On" Colors (For text legibility)
val OnBadgerRedContainer = Color(0xFF410002) // Deep maroon text
val OnBadgerRed = Color(0xFFFFFFFF)          // Pure white text on red buttons

val LightBadgersColorScheme = lightColorScheme(
    primary = BadgerRed,
    onPrimary = OnBadgerRed,
    primaryContainer = BadgerWhiteContainer,
    onPrimaryContainer = BadgerBlack,

    secondary = BadgerBlack, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = BadgerBlack,
    onTertiary = BadgerRed,
    tertiaryContainer = BadgerRedContainer,
    onTertiaryContainer = BadgerBlack,

    background = Color.White,
    onBackground = BadgerBlack,

    surface = Color.White,
    onSurface = BadgerBlack,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = BadgerBlack,
    surfaceVariant = BadgerRed,
    onSurfaceVariant = BadgerBlack,
)
