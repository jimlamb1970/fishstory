package com.funjim.fishstory.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getOnMainColor(): Color {
    return MaterialTheme.colorScheme.primary
}

@Composable
fun getOnSecondaryColor(): Color {
    return MaterialTheme.colorScheme.onSurface
}

@Composable
fun getMainButtonColor(): Color {
    return MaterialTheme.colorScheme.primary
}

@Composable
fun getOnMainButtonColor(): Color {
    return MaterialTheme.colorScheme.onPrimary
}

@Composable
fun getCardColor(
    index: Int = 0,
    totalItems: Int = 0,
    selected: Boolean = false
): Color {
    return if (index % 2 == 0 || totalItems <= 3) {
        if (selected)
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)
        else
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    } else {
        if (selected)
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f)
        else
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }
}

@Composable
fun getGridCardColor(
    index: Int = 0,
    totalItems: Int = 0,
    selected: Boolean = false
): Color {
    return if (index % 4 == 0 || index % 4 == 3 || totalItems <= 3) {
        if (selected)
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)
        else
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    } else {
        if (selected)
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f)
        else
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    }
}

@Composable
fun getCardBorderColor(index: Int = 0, totalItems: Int = 0): Color {
    return if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}

@Composable
fun getGridCardBorderColor(index: Int = 0, totalItems: Int = 0): Color {
    return if (index % 4 == 0 || index % 4 == 3 || totalItems <= 3) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
}

@Composable
fun getOnCardColor(): Color {
    return MaterialTheme.colorScheme.primary
}

@Composable
fun getOnCardSecondaryColor(): Color {
    return MaterialTheme.colorScheme.onSurface
}

@Composable
fun getChipColor(selected: Boolean = false): Color {
    return if (selected) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.secondary
    }
}

@Composable
fun getOnChipColor(): Color {
    return MaterialTheme.colorScheme.primary
}

@Composable
fun getOnChipSecondaryColor(): Color {
    return MaterialTheme.colorScheme.onSurface
}
