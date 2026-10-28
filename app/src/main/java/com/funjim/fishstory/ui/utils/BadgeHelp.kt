package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeHelp(
    selected: Boolean,
    badgeColor: Color,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    textColor: Color,
    size : Dp = 20.dp,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) badgeColor else textColor,
        border = BorderStroke(2.dp, badgeColor),
        modifier = Modifier.padding(start = 2.dp).size(size)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Text(
                text = "?",
                style = textStyle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (selected) textColor else badgeColor,
            )
        }
    }
}
