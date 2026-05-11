package com.funjim.fishstory.ui.theme.teams

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Containers (Lightest versions for backgrounds/chips)
val GopherMaroonContainer = Color(0xFFFFDADA)
val GopherGoldContainer = Color(0xFFFFF176)

// Official Team Colors
val GopherMaroon = Color(0xFF7A0019)
val GopherGold = Color(0xFFFFCC33)

// "On" Colors (For text legibility)
val OnGopherMaroonContainer = Color(0xFF3B0007) // Very deep maroon text
val OnGopherMaroon = Color(0xFFFFFFFF)          // Pure white text on dark buttons

val LightGophersColorScheme = lightColorScheme(
    primary = GopherMaroon,
    onPrimary = GopherGold,
    primaryContainer = GopherGoldContainer,
    onPrimaryContainer = OnGopherMaroonContainer,

    secondary = GopherMaroon, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = GopherGold,
    onTertiary = GopherMaroon,
    tertiaryContainer = GopherMaroonContainer,
    onTertiaryContainer = OnGopherMaroonContainer,

    background = Color.White,
    onBackground = GopherMaroon,

    surface = Color.White,
    onSurface = GopherMaroon,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = GopherMaroon,
    surfaceVariant = GopherGold,
    onSurfaceVariant = GopherMaroon,
)
