package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.funjim.fishstory.ui.theme.MichBlue
import com.funjim.fishstory.ui.theme.MichBlueContainer
import com.funjim.fishstory.ui.theme.MichMaize
import com.funjim.fishstory.ui.theme.MichMaizeContainer

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
    surfaceVariant = MichBlue,
    onSurfaceVariant = MichMaize,
)
val LightMichiganColorScheme = lightColorScheme(
    primary = MichBlue,
    onPrimary = MichMaize,
    primaryContainer = MichBlueContainer,
    onPrimaryContainer = MichBlue,

    secondary = MichBlue, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = MichMaize,
    onTertiary = MichBlue, // Blue text on Maize background looks sharp
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
