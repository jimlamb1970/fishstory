package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SortChip(
    label: String,
    selected:
    Boolean, onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelSmall) },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = getChipColor(true),
            selectedBorderWidth = 2.dp,
            borderColor = getOnChipColor(),
            borderWidth = 1.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = getChipColor(true).copy(alpha = 0.15f),
            selectedLabelColor = getOnChipSecondaryColor(),
            labelColor = getOnChipColor()
        ),
        modifier = Modifier.padding(end = 4.dp)
    )
}
