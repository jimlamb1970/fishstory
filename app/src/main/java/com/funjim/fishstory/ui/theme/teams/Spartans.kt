package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
val SpartanGreenContainer = Color(0xFFD0E8D1)
val SpartanWhiteContainer = Color(0xFFF8FBF8)

// Official Team Colors
val SpartanGreen = Color(0xFF18453B)
val SpartanBronze = Color(0xFF8E7356) // Often used in MSU branding for accents

// "On" Colors (For text legibility)
val OnSpartanGreenContainer = Color(0xFF002116) // Deep forest-black text
val OnSpartanGreen = Color(0xFFFFFFFF)          // White text on green buttons

val LightSpartansColorScheme = lightColorScheme(
    primary = SpartanGreen,
    onPrimary = OnSpartanGreen,
    primaryContainer = SpartanGreenContainer,
    onPrimaryContainer = SpartanGreen,

    secondary = SpartanGreen, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = SpartanBronze,
    onTertiary = SpartanGreen, // Blue text on Maize background looks sharp
    tertiaryContainer = SpartanWhiteContainer,
    onTertiaryContainer = SpartanGreen,

    background = Color.White,
    onBackground = SpartanGreen,

    surface = Color.White,
    onSurface = SpartanGreen,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = SpartanGreen,
    surfaceVariant = SpartanBronze,
    onSurfaceVariant = SpartanGreen,
)
