package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountBadge(
    count: Int,
    badgeColor: Color,
    textColor: Color,
    selected: Boolean = false,
) {
    Surface(
        shape = CircleShape,
        color = badgeColor,
        modifier = Modifier.padding(start = 2.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 2.dp
            )
        )
    }
}
