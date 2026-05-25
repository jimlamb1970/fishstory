package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Michigan Wolverine Colors
val MichBlue = Color(0xFF00274C)
val MichMaize = Color(0xFFFFCB05)

val MichBlueContainer = Color(0xFF00E3FF)
val MichMaizeContainer = Color(0xFFFFE082)

val DarkMichiganColorScheme = darkColorScheme(
    primary = MichMaize,
    onPrimary = MichBlue,
    primaryContainer = MichMaizeContainer,
    onPrimaryContainer = MichBlue,

    secondary = MichMaize,
    onSecondary = MichBlue,

    tertiary = Color.White,
    onTertiary = MichBlue,
    tertiaryContainer = MichBlueContainer,
    onTertiaryContainer = MichBlue,

    background = MichBlue,
    onBackground = Color.White,

    surface = MichBlue,
    onSurface = Color.White,

    surfaceContainer = MichBlue,
    surfaceContainerLow = MichBlue,
    surfaceContainerHigh = MichBlue,

    // This is for the "outline" of the menu or cards
    outlineVariant = MichMaize,
    surfaceVariant = MichMaize,
    onSurfaceVariant = MichMaize,
)
val LightMichiganColorScheme = lightColorScheme(
    primary = MichBlue,
    onPrimary = Color.White,
    primaryContainer = MichBlueContainer,
    onPrimaryContainer = MichBlue,

    secondary = MichBlue,
    onSecondary = Color.White,

    tertiary = MichMaize,
    onTertiary = MichBlue,
    tertiaryContainer = MichMaizeContainer,
    onTertiaryContainer = MichBlue,

    background = Color.White,
    onBackground = MichBlue,

    surface = Color.White,
    onSurface = MichBlue,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = MichBlue,
    surfaceVariant = MichMaize,
    onSurfaceVariant = MichBlue,
)
