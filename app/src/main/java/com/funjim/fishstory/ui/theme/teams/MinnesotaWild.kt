package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
val WildGreenContainer = Color(0xFFD6E4DC)
val WildWheatContainer = Color(0xFFF1EAD7) // Beautiful vintage cream highlight

// Official Team Colors
val WildGreen = Color(0xFF154734) // Forest Green
val WildRed = Color(0xFFA6192E)   // Iron Range Red
val WildWheat = Color(0xFFDDCBA4) // Minnesota Wheat

// "On" Colors (For text legibility)
val OnWildWheatContainer = Color(0xFF2B220F)  // Deep brown-black text for cream chips
val OnWildGreenContainer = Color(0xFF002111)  // Deep forest text
val OnWildGreen = Color(0xFFFFFFFF)           // Pure white text on dark green buttons

val LightWildColorScheme = lightColorScheme(
    primary = WildGreen,
    onPrimary = WildWheat,
    primaryContainer = WildGreenContainer,
    onPrimaryContainer = WildGreen,

    secondary = WildGreen,
    onSecondary = Color.White,

    tertiary = WildRed,
    onTertiary = Color.White,
    tertiaryContainer = WildWheatContainer,
    onTertiaryContainer = WildGreen,

    background = Color.White,
    onBackground = WildGreen,

    surface = Color.White,
    onSurface = WildGreen,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    outlineVariant = WildGreen,
    surfaceVariant = WildRed,
    onSurfaceVariant = WildGreen,
)
