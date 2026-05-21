package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
// Containers (Lightest versions for backgrounds/chips)
val LionsBlueContainer = Color(0xFFCBEBFC)
val LionsSilverContainer = Color(0xFFE5E9EC)

// Official Team Colors
val LionsBlue = Color(0xFF0076B6)   // Honolulu Blue
val LionsSilver = Color(0xFFB2B7BB) // Silver

// "On" Colors (For text legibility)
val OnLionsBlueContainer = Color(0xFF001E31)  // Deep navy-black text
val OnLionsSilverContainer = Color(0xFF191C1E) // Dark charcoal text
val OnLionsBlue = Color(0xFFFFFFFF)            // Pure white text on blue buttons

val DarkDetroitLionsColorScheme = darkColorScheme(
    primary = OnLionsBlueContainer,
    onPrimary = LionsBlue,
    primaryContainer = LionsSilverContainer,
    onPrimaryContainer = LionsBlue,

    secondary = OnLionsBlueContainer,
    onSecondary = LionsBlue,

    tertiary = Color.White,
    onTertiary = LionsBlue,
    tertiaryContainer = LionsBlueContainer,
    onTertiaryContainer = LionsBlue,

    background = LionsBlue,
    onBackground = Color.White,

    surface = LionsBlue,
    onSurface = Color.White,

    surfaceContainer = LionsBlue,
    surfaceContainerLow = LionsBlue,
    surfaceContainerHigh = LionsBlue,

    outlineVariant = OnLionsBlueContainer,
    surfaceVariant = LionsSilver,
    onSurfaceVariant = LionsSilver,
)

val LightDetroitLionsColorScheme = lightColorScheme(
    primary = LionsBlue,
    onPrimary = OnLionsBlue,
    primaryContainer = LionsBlueContainer,
    onPrimaryContainer = LionsBlue,

    secondary = LionsBlue,
    onSecondary = Color.White,

    tertiary = LionsSilver,
    onTertiary = LionsBlue,
    tertiaryContainer = LionsSilverContainer,
    onTertiaryContainer = LionsBlue,

    background = Color.White,
    onBackground = LionsBlue,

    surface = Color.White,
    onSurface = LionsBlue,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    outlineVariant = LionsBlue,
    surfaceVariant = LionsSilver,
    onSurfaceVariant = LionsBlue,
)
