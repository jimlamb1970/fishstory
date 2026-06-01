package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.flow.Flow
import androidx.core.graphics.toColorInt
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureSummaryWithColors
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.ui.screens.MultiColorCirclePreview
import com.funjim.fishstory.viewmodels.LureSortOrder

@Composable
fun LureItem(
    item: LureSummaryWithColors,
    thumbnailFlow: Flow<ByteArray?>,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    var menuExpanded by remember { mutableStateOf(false) }

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThumbnailBox(
                thumbnail = thumbnail,
                imageVector = AppIcons.Default.Lure,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((4).dp)
                ) {
                    Text(
                        text = item.lure.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                LureColorComposition(
                    primary = item.primaryColors,
                    secondary = item.secondaryColors,
                    glows = item.lure.glows,
                    glow = item.glowColors
                )

                Text(
                    text = "Number of hooks: ${item.lure.hookCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryContentColor
                )

                if (item.caughtCount != 0) {
                    FishCaughtItem(
                        icon = AppIcons.Default.LeapingFish,
                        caughtCount = item.caughtCount,
                        keptCount = item.keptCount,
                        contentColor = secondaryContentColor
                    )
                }
            }

            Box {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LureCompositionWithColors(
    name: String,
    primary: List<LureColor>,
    secondary: List<LureColor>,
    glows: Boolean,
    glow: List<LureColor>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurface,
    colorBadgeSize: Dp = 28.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        if (name.isNotEmpty()) {
            Text(
                text = name,
                style = style,
                color = contentColor,
            )
        }

        primary.forEach { color ->
            ProcessColor(
                color,
                contentColor = secondaryColor,
                colorBadgeSize = colorBadgeSize)
        }

        secondary.forEach { color ->
            if (primary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor
                )
            }
            ProcessColor(
                color,
                contentColor = secondaryColor,
                colorBadgeSize = colorBadgeSize)
        }

        if (glows) {
            if (primary.isNotEmpty() || secondary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor
                )
            }
            Text(
                text = "Glows",
                style = MaterialTheme.typography.bodySmall,
                color = secondaryColor
            )
            glow.forEach { color ->
                ProcessColor(
                    color,
                    contentColor = secondaryColor,
                    colorBadgeSize = colorBadgeSize,
                    glows = true)
            }
        }
    }
}

@Composable
fun ProcessColor(
    color: LureColor,
    glows: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    colorBadgeSize: Dp = 28.dp
) {
    if (color.hexCode.isNullOrBlank()) {
        Text(
            text = color.name,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    } else {
        ColorCircleBadge(
            hexCode = color.hexCode,
            label = color.name,
            modifier = Modifier.size(colorBadgeSize),
            isGlow = glows
        )
    }
}

@Composable
fun LureColorCompositionMultiLine(
    modifier: Modifier = Modifier,
    primary: List<LureColor> = emptyList(),
    secondary: List<LureColor> = emptyList(),
    glows: Boolean = false,
    glow: List<LureColor> = emptyList(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    colorBadgeSize: Dp = 28.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (primary.isNotEmpty() || secondary.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                primary.forEach { lureColor ->
                    ProcessColor(
                        color = lureColor,
                        contentColor = contentColor,
                        colorBadgeSize = colorBadgeSize
                    )
                }

                if (secondary.isNotEmpty()) {
                    Text(
                        text = " / ",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    secondary.forEach { lureColor ->
                        ProcessColor(
                            color = lureColor,
                            contentColor = contentColor,
                            colorBadgeSize = colorBadgeSize
                        )
                    }
                }
            }
        }

        if (glows || glow.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Glows ",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                glow.forEach { lureColor ->
                    ProcessColor(
                        color = lureColor,
                        contentColor = contentColor,
                        colorBadgeSize = colorBadgeSize
                    )
                }
            }
        }
    }
}

@Composable
fun LureColorComposition(
    modifier: Modifier = Modifier,
    primary: List<LureColor> = emptyList(),
    secondary: List<LureColor> = emptyList(),
    glows: Boolean = false,
    glow: List<LureColor> = emptyList(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    colorBadgeSize: Dp = 28.dp,
) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        primary.forEach { color ->
            ProcessColor(
                color = color,
                contentColor = contentColor,
                colorBadgeSize = colorBadgeSize)
        }

        if (secondary.isNotEmpty()) {
            if (primary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            secondary.forEach { color ->
                ProcessColor(
                    color = color,
                    contentColor = contentColor,
                    colorBadgeSize = colorBadgeSize)
            }
        }

        if (glows) {
            if (primary.isNotEmpty() || secondary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Text(
                text = "Glows",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            glow.forEach { color ->
                ProcessColor(
                    color = color,
                    glows = true,
                    contentColor = contentColor,
                    colorBadgeSize = colorBadgeSize)
            }
        }
    }
}

@Composable
fun ColorCircleBadge(
    hexCode: String,
    label: String,
    modifier: Modifier = Modifier,
    isGlow: Boolean = false
) {
    val hexList = remember(hexCode) {
        hexCode.split(",").filter { it.isNotBlank() }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f) // Crucial: forces the shape to stay a perfect circle even if sizes match weirdly
            .clip(CircleShape)
            .border(
                width = if (isGlow) 2.dp else 1.dp,
                color =
                    if (isGlow) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hexList.size > 1) {
            // Dynamic layout grid depending on hex count
            MultiColorCirclePreview(hexList = hexList)
        } else {
            val color = remember(hexCode) {
                try {
                    Color(hexCode.toColorInt())
                } catch (e: Exception) {
                    Color.Gray
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
            ) {
                Text(
                    text = if (label.length > 1) label[0].toString() else label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isColorDark(color)) Color.White else Color.Black,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }
    }
}

// Quick helper to determine if text should be white or black inside the circle
fun isColorDark(color: Color): Boolean {
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return luminance < 0.5
}

fun sortLures(lureList: List<LureWithColors>): List<LureWithColors> {
    // Helper function to turn a list of colors into a single, predictable alphabetical string
    val getColorsSortingString = { colors: List<LureColor> ->
        colors.map { it.name }
            .sorted() // Sort the colors of THIS specific lure alphabetically first
            .joinToString(separator = ",") // Merge them into a single string like "Black,Blue"
    }

    return lureList.sortedWith(
        compareBy<LureWithColors> { it.lure.name }
            .thenBy { getColorsSortingString(it.primaryColors) }
            .thenBy { getColorsSortingString(it.secondaryColors) }
            .thenBy { getColorsSortingString(it.glowColors) }
    )
}

fun sortLures(lureList: List<LureSummaryWithColors>, order: LureSortOrder): List<LureSummaryWithColors> {
    // Helper function to turn a list of colors into a single, predictable alphabetical string
    val getColorsSortingString = { colors: List<LureColor> ->
        colors.map { it.name }
            .sorted() // Sort the colors of THIS specific lure alphabetically first
            .joinToString(separator = ",") // Merge them into a single string like "Black,Blue"
    }

    return when (order) {
        LureSortOrder.NAME -> lureList.sortedWith(
            compareBy<LureSummaryWithColors> { it.lure.name }
                .thenBy { getColorsSortingString(it.primaryColors) }
                .thenBy { getColorsSortingString(it.secondaryColors) }
                .thenBy { getColorsSortingString(it.glowColors) }
        )
        LureSortOrder.PRIMARY_COLOR -> lureList.sortedWith(
            compareBy<LureSummaryWithColors> { getColorsSortingString(it.primaryColors) }
                .thenBy { it.lure.name }
                .thenBy { getColorsSortingString(it.secondaryColors) }
                .thenBy { getColorsSortingString(it.glowColors) }
        )
        LureSortOrder.SECONDARY_COLOR -> lureList.sortedWith(
            compareBy<LureSummaryWithColors> { getColorsSortingString(it.secondaryColors) }
                .thenBy { it.lure.name }
                .thenBy { getColorsSortingString(it.primaryColors) }
                .thenBy { getColorsSortingString(it.glowColors) }
        )
        LureSortOrder.GLOW_COLOR -> lureList.sortedWith(
            compareBy<LureSummaryWithColors> { getColorsSortingString(it.glowColors) }
                .thenBy { it.lure.name }
                .thenBy { getColorsSortingString(it.primaryColors) }
                .thenBy { getColorsSortingString(it.secondaryColors) }
        )
        else -> lureList.sortedWith(
            compareBy<LureSummaryWithColors> { it.lure.name }
                .thenBy { getColorsSortingString(it.primaryColors) }
                .thenBy { getColorsSortingString(it.secondaryColors) }
                .thenBy { getColorsSortingString(it.glowColors) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureSelectionField(
    items: List<LureWithColors>,
    modifier: Modifier = Modifier,
    defaultText : String = "Select Lure (optional)",
    selectedItem: LureWithColors?,
    onSelected: (LureWithColors) -> Unit,
    onAdd: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    thumbnailProvider: @Composable (LureWithColors) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = selectedItem?.lure?.name ?: defaultText,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Lure") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (selectedItem != null) {
        LureColorComposition(
            primary = selectedItem.primaryColors,
            secondary = selectedItem.secondaryColors,
            glows = selectedItem.lure.glows,
            glow = selectedItem.glowColors
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Select Lure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier
                .fillMaxHeight(0.8f)
                .padding(start = 16.dp, end = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Lures...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter {
                    it.lure.name.contains(searchQuery, ignoreCase = true)
                }
                val filteredSize = filtered.size

                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        gridItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.lure.id }
                        ) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                headlineContent = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        thumbnailProvider(item)

                                        Text(
                                            text = item.lure.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        LureColorCompositionMultiLine(
                                            primary = item.primaryColors,
                                            secondary = item.secondaryColors,
                                            glows = item.lure.glows,
                                            glow = item.glowColors,
                                            colorBadgeSize = 16.dp
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (onClear != null && selectedItem != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider() }

                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ModalResetButton(
                                    title = "Reset Lure",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }

                        if (onAdd != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider() }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ModalAddButton(
                                    title = "Add lures to tackle box...",
                                    onAdd = { showSheet = false; onAdd() }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listItemsIndexed(filtered) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                leadingContent = {
                                    thumbnailProvider(item)
                                },
                                headlineContent = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = item.lure.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal,
                                            color = getOnCardColor()
                                        )

                                        LureColorComposition(
                                            primary = item.primaryColors,
                                            secondary = item.secondaryColors,
                                            glows = item.lure.glows,
                                            glow = item.glowColors
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (onClear != null && selectedItem != null) {
                            item { HorizontalDivider() }
                            item {
                                ModalResetButton(
                                    title = "Reset Lure",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }

                        if (onAdd != null) {
                            item { HorizontalDivider() }
                            item {
                                ModalAddButton(
                                    title = "Add lures to tackle box...",
                                    onAdd = { showSheet = false; onAdd() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
