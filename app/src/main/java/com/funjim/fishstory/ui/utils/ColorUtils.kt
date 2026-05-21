package com.funjim.fishstory.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getCardColor(index: Int, totalItems: Int): Color {
    return if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }
}

@Composable
fun getCardBorderColor(index: Int, totalItems: Int): Color {
    return if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}

@Composable
fun getCardContentColor(): Color {
    return MaterialTheme.colorScheme.primary
}

@Composable
fun getCardSecondaryContentColor(): Color {
    return MaterialTheme.colorScheme.onSurface
}