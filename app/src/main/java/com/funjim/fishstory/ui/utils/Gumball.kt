package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GumballStatus {
    GOOD,      // Green
    CAUTION,   // Yellow/Orange
    BAD        // Red
}

val GumballStatus.color: Color
    get() = when (this) {
        GumballStatus.GOOD -> Color(0xFF4CAF50)    // Green
        GumballStatus.CAUTION -> Color(0xFFFF9800) // Orange / Yellow
        GumballStatus.BAD -> Color(0xFFF44336)     // Red
    }

@Composable
fun StatusGumball(
    status: GumballStatus,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = MaterialTheme.colorScheme.surface // Matches card/screen background
) {
    val fillColor = when (status) {
        GumballStatus.GOOD -> Color(0xFF4CAF50)    // Green
        GumballStatus.CAUTION -> Color(0xFFFF9800) // Orange / Yellow
        GumballStatus.BAD -> Color(0xFFF44336)     // Red
    }

    Canvas(modifier = modifier.size(size)) {
        // 1. Solid filled color gumball
        drawCircle(color = fillColor)

        // 2. Outer border stroke around the gumball
        drawCircle(
            color = borderColor,
            style = Stroke(width = borderWidth.toPx())
        )
    }
}