package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.ui.theme.AppIcons

@Composable
fun AchievementItem(
    icon: ImageVector,
    label: String,
    name: String?,
    description: String? = null,
    modifier: Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    // Parent Column to stack Label over the Icon/Name group
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier) {
        // 1. Label at the very top
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            modifier = Modifier.padding(bottom = 4.dp) // Space before the icon/name
        )

        // 2. Row to keep Icon and Name on the same line
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp), // Slightly smaller to let name lead
                tint = color
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = name ?: "---",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = color
            )
        }

        // 3. Description (Optional) stays at the bottom
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun CardItemWithValue(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}

@Composable
fun FishCaughtItem(
    icon: ImageVector,
    caughtCount: Int,
    keptCount: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds space between icon and text
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = "Fish",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            BoldingNumbersText(
                text = "Kept $keptCount of $caughtCount",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    description: String? = null,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
