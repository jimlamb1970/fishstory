package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
val TigersNavyContainer = Color(0xFFDDE2EA)
val TigersOrangeContainer = Color(0xFFFFE5D9)

// Official Team Colors
val TigersNavy = Color(0xFF0C2340)
val TigersOrange = Color(0xFFFA4616)

// "On" Colors (For text legibility)
val OnTigersOrangeContainer = Color(0xFF3E0E00) // Deep rust/black text
val OnTigersNavy = Color(0xFFFFFFFF)            // White text on navy buttons

val DarkDetroitTigersColorScheme = darkColorScheme(
    primary = TigersOrange,
    onPrimary = TigersNavy,
    primaryContainer = TigersOrangeContainer,
    onPrimaryContainer = TigersNavy,

    secondary = TigersOrange, // Keeping it consistent
    onSecondary = TigersNavy,

    tertiary = Color.White,
    onTertiary = TigersNavy, // Blue text on Maize background looks sharp
    tertiaryContainer = TigersNavyContainer,
    onTertiaryContainer = TigersNavy,

    background = TigersNavy,
    onBackground = Color.White,

    surface = TigersNavy,
    onSurface = Color.White,

    surfaceContainer = TigersNavy,
    surfaceContainerLow = TigersNavy,
    surfaceContainerHigh = TigersNavy,

    // This is for the "outline" of the menu or cards
    outlineVariant = TigersOrange,
    surfaceVariant = TigersOrange,
    onSurfaceVariant = TigersOrange,
)

val LightDetroitTigersColorScheme = lightColorScheme(
    primary = TigersNavy,
    onPrimary = OnTigersNavy,
    primaryContainer = TigersNavyContainer,
    onPrimaryContainer = TigersNavy,

    secondary = TigersNavy, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = TigersOrange,
    onTertiary = TigersNavy, // Blue text on Maize background looks sharp
    tertiaryContainer = TigersOrangeContainer,
    onTertiaryContainer = TigersNavy,

    background = Color.White,
    onBackground = TigersNavy,

    surface = Color.White,
    onSurface = TigersNavy,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = TigersNavy,
    surfaceVariant = TigersOrange,
    onSurfaceVariant = TigersNavy,
)
