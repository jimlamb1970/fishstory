package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

enum class CategoryType(val label: String) {
    BODIES_OF_WATER("Bodies of Water"),
    EVENTS("Events"),
    LIMITS("Limits"),
    FISHERMEN("Fishermen"),
    NOTES("Notes"),
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
    modifier: Modifier = Modifier,
    onExpand: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedCategory) {
        val index = categories.indexOfFirst { it.category == selectedCategory }
        if (index != -1) {
            coroutineScope.launch {
                // Use animateScrollToItem for a smooth transition,
                // or scrollToItem for an immediate jump without animation.
                listState.animateScrollToItem(index)
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onExpand != null) {
            IconButton(
                onClick = onExpand,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
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
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = config.category.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            // Badge Count Indicator
                            config.count?.let { count ->
                                if (count > 0) {
                                    BadgeCount(
                                        count = count,
                                        badgeColor = getMainButtonColor(),
                                        textColor = getOnMainButtonColor(),
                                        selected = isSelected
                                    )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryRow(
    categories: List<CategoryChipConfig>,
    selectedCategory: CategoryType,
    onCategorySelected: (CategoryType) -> Unit,
    modifier: Modifier = Modifier,
    onCollapse: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
) {
    FlowRow(
        modifier = modifier.fillMaxWidth().padding(contentPadding),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        if (onCollapse != null) {
            Box(
                modifier = Modifier.padding(end = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    IconButton(
                        onClick = onCollapse,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            categories.forEach { config ->
                val isSelected = config.category == selectedCategory

                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(config.category) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = config.category.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            // Badge Count Indicator
                            config.count?.let { count ->
                                if (count > 0) {
                                    BadgeCount(
                                        count = count,
                                        badgeColor = getMainButtonColor(),
                                        textColor = getOnMainButtonColor(),
                                        selected = isSelected
                                    )
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
                    ),
                    modifier = Modifier.padding(start = 0.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}