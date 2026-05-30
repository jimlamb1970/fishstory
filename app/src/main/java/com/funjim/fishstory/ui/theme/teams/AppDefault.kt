package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
// Containers (Lightest versions for backgrounds/chips)
val ForestGreenContainer = Color(0xFFD6E4DC)
val AmberBrownContainer = Color(0xFFFBEBE0) // Soft, warm brownish-orange tint

// Brand Colors
val ForestGreen = Color(0xFF154734)     // The deep Wild Forest Green
val AmberBrown = Color(0xFFD96B27)    // The brownish-orange (Cognac / Burnt Amber)

// "On" Colors (For text legibility)
val OnAmberBrownContainer = Color(0xFF371400) // Deep dark brown text for chips
val OnForestGreen = Color(0xFFFFFFFF)           // Pure white text on green buttons
val OnSurfaceDark = Color(0xFF1A1C1A)         // Dark charcoal for primary body text

val LightAppDefaultColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = OnForestGreen,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreen,

    secondary = ForestGreen, // Keeping it consistent
    onSecondary = OnForestGreen,
    secondaryContainer = AmberBrownContainer,
    onSecondaryContainer =  OnAmberBrownContainer,

    tertiary = AmberBrown,
    onTertiary = ForestGreen,
    tertiaryContainer = AmberBrownContainer,
    onTertiaryContainer = OnAmberBrownContainer,

    background = Color.White,
    onBackground = ForestGreen,

    surface = Color.White,
    onSurface = ForestGreen,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = ForestGreen,
    surfaceVariant = AmberBrown,
    onSurfaceVariant = ForestGreen,
)
