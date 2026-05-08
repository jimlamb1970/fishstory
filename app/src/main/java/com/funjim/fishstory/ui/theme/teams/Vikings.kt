package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.funjim.fishstory.ui.theme.VikesGold
import com.funjim.fishstory.ui.theme.VikesGoldContainer
import com.funjim.fishstory.ui.theme.VikesPurple
import com.funjim.fishstory.ui.theme.VikesPurpleContainer

val LightVikingsColorScheme = lightColorScheme(
    primary = VikesPurple,
    onPrimary = VikesGold,
    primaryContainer = VikesPurpleContainer,
    onPrimaryContainer = VikesPurple,

    secondary = VikesPurple, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = VikesGold,
    onTertiary = VikesPurple, // Blue text on Maize background looks sharp
    tertiaryContainer = VikesGoldContainer,
    onTertiaryContainer = VikesPurple,

    background = Color.White,
    onBackground = VikesPurple,

    surface = Color.White,
    onSurface = VikesPurple,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = VikesPurple,
    surfaceVariant = VikesGold,
    onSurfaceVariant = VikesPurple,
)
