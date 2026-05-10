package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReleasedChip(
    released: Boolean
) {
    // Determine colors based on the state
    val containerColor = if (released)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    val contentColor = if (released)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (released)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (released) Icons.Default.RemoveCircle else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
                // tint is no longer needed because Surface sets the contentColor!
            )
            Text(
                text = if (released) "Released" else "Kept",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}