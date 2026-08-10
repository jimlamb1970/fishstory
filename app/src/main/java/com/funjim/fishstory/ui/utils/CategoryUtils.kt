package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class CategoryType(val label: String) {
    BODIES_OF_WATER("Bodies of Water"),
    EVENTS("Events"),
    FISHERMEN("Fishermen"),
    TARGET_SPECIES("Target Species"),
    WATER("Water"),
    WEATHER("Weather")
}

data class CategoryChipConfig(
    val category: CategoryType,
    val icon: @Composable () -> Unit,
    val count: Int? = null // Null if you don't want to display a badge count
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCarousel(
    categories: List<CategoryChipConfig>,
    selectedCategory: CategoryType,
    onCategorySelected: (CategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(categories) { config ->
            val isSelected = config.category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(config.category) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = config.category.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )

                        // Badge Count Indicator
                        config.count?.let { count ->
                            if (count > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = getMainButtonColor(),
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = getOnMainButtonColor(),
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = getChipColor(true),
                    selectedBorderWidth = 2.dp,
                    borderColor = getOnChipColor(),
                    borderWidth = 1.dp
                ),
                leadingIcon = {
                    Box(modifier = Modifier.size(18.dp)) {
                        config.icon()
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = getChipColor(true).copy(alpha = 0.15f),
                    selectedLabelColor = getOnChipSecondaryColor(),
                    labelColor = getOnChipColor()
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryRow(
    categories: List<CategoryChipConfig>,
    selectedCategory: CategoryType,
    onCategorySelected: (CategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { config ->
            val isSelected = config.category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(config.category) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = config.category.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )

                        // Badge Count Indicator
                        config.count?.let { count ->
                            if (count > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = getMainButtonColor(),
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = getOnMainButtonColor(),
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = getChipColor(true),
                    selectedBorderWidth = 2.dp,
                    borderColor = getOnChipColor(),
                    borderWidth = 1.dp
                ),
                leadingIcon = {
                    Box(modifier = Modifier.size(18.dp)) {
                        config.icon()
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = getChipColor(true).copy(alpha = 0.15f),
                    selectedLabelColor = getOnChipSecondaryColor(),
                    labelColor = getOnChipColor()
                )
            )
        }
    }
}