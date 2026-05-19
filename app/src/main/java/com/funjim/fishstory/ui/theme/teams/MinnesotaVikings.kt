package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Minnesota Viking Colors
val VikesPurpleContainer = Color(0xFFE1BEE7) // A light lavender-purple
val VikesGoldContainer = Color(0xFFFFF176)   // A bright, soft gold
val VikesPurple = Color(0xFF4F2683)
val VikesGold = Color(0xFFFFC62F)
val OnVikesPurpleContainer = Color(0xFF21005D) // Deep purple text for the light container
val OnVikesGoldContainer = Color(0xFF241D00)   // Dark gold/black text for the gold container

val LightVikingsColorScheme = lightColorScheme(
    primary = VikesPurple,
    onPrimary = VikesGold,
    primaryContainer = VikesPurpleContainer,
    onPrimaryContainer = VikesPurple,

    secondary = VikesPurple,
    onSecondary = Color.White,

    tertiary = VikesGold,
    onTertiary = VikesPurple,
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
