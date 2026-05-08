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
fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = MaterialTheme.colorScheme.tertiary,
            selectedBorderWidth = 2.dp,
            borderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
            labelColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(end = 4.dp)
    )
}
