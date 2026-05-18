package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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

    val backgroundColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }
    val borderColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.primary
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
                modifier = Modifier.size(72.dp)
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (item.caughtCount != 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Default.LeapingFish,
                            contentDescription = "Fish",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        BoldingNumbersText(
                            text = "Kept ${item.keptCount} of ${item.caughtCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
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
    color: Color = MaterialTheme.colorScheme.primary,
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
                color = color,
            )
        }

        primary.forEach { color ->
            if (color.hexCode.isNullOrBlank()) {
                Text(
                    text = color.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                ColorCircleBadge(
                    hexCode = color.hexCode,
                    label = color.name,
                    modifier = Modifier
                        .size(colorBadgeSize)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        secondary.forEach { color ->
            if (primary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (color.hexCode.isNullOrBlank()) {
                Text(
                    text = color.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                ColorCircleBadge(
                    hexCode = color.hexCode,
                    label = color.name,
                    modifier = Modifier
                        .size(colorBadgeSize)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        if (glows) {
            if (primary.isNotEmpty() || secondary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Glows",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            glow.forEach { color ->
                if (color.hexCode.isNullOrBlank()) {
                    Text(
                        text = color.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    ColorCircleBadge(
                        hexCode = color.hexCode,
                        label = color.name,
                        isGlow = true,
                        modifier = Modifier
                            .size(colorBadgeSize)
                            .align(Alignment.CenterVertically)
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
    colorBadgeSize: Dp = 28.dp
) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        primary.forEach { color ->
            if (color.hexCode.isNullOrBlank()) {
                Text(
                    text = color.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            } else {
                ColorCircleBadge(
                    hexCode = color.hexCode,
                    label = color.name,
                    modifier = Modifier
                        .size(colorBadgeSize)
                        .align(Alignment.CenterVertically)
                )
            }
        }

        if (secondary.isNotEmpty()) {
            if (primary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            secondary.forEach { color ->
                if (color.hexCode.isNullOrBlank()) {
                    Text(
                        text = color.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterVertically)

                    )
                } else {
                    ColorCircleBadge(
                        hexCode = color.hexCode,
                        label = color.name,
                        modifier = Modifier
                            .size(colorBadgeSize)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }

        if (glows) {
            if (primary.isNotEmpty() || secondary.isNotEmpty()) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)

                )
            }
            Text(
                text = "Glows",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            glow.forEach { color ->
                if (color.hexCode.isNullOrBlank()) {
                    Text(
                        text = color.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    ColorCircleBadge(
                        hexCode = color.hexCode,
                        label = color.name,
                        isGlow = true,
                        modifier = Modifier
                            .size(colorBadgeSize)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

@Composable
fun LureColorComposition(
    primaryHex: String?,
    secondaryHex: String?,
    glowHex: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy((-6).dp) // Negative spacing overlaps them elegantly
    ) {
        // 1. Primary Color (Bottom layer if overlapping)
        if (!primaryHex.isNullOrBlank()) {
            ColorCircleBadge(hexCode = primaryHex, label = "P")
        }

        // 2. Secondary Color
        if (!secondaryHex.isNullOrBlank()) {
            ColorCircleBadge(hexCode = secondaryHex, label = "S")
        }

        // 3. Glow Color (With a subtle dashed border to indicate it glows)
        if (!glowHex.isNullOrBlank()) {
            ColorCircleBadge(
                hexCode = glowHex,
                label = "G",
                isGlow = true
            )
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
//            .minimumInteractiveComponentSize() // Prevents overlapping and layout collapse
//            .then(modifier) // Consumes the .size(28.dp) safely
            .aspectRatio(1f) // Crucial: forces the shape to stay a perfect circle even if sizes match weirdly
            .clip(CircleShape)
            .border(
                width = if (isGlow) 1.5.dp else 2.dp,
                color = if (isGlow) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface,
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
                    style = MaterialTheme.typography.labelMedium,
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

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Lures...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.lure.name.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    val filteredSize = filtered.size
                    itemsIndexed(filtered) { index, item ->
                        val backgroundColor = if ((index % 2 == 0) || (filteredSize < 4)) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        }

                        val borderColor = if (index % 2 == 0 || filteredSize <= 3) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable {
                                    onSelected(item)
                                    showSheet = false
                                    searchQuery = ""
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = backgroundColor,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, color = borderColor)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                thumbnailProvider(item)

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy((4).dp)
                                    ) {
                                        Text(
                                            text = item.lure.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    LureColorComposition(
                                        primary = item.primaryColors,
                                        secondary = item.secondaryColors,
                                        glows = item.lure.glows,
                                        glow = item.glowColors
                                    )
                                }
                            }
                        }
                    }

                    if (onAdd != null) {
                        item {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Add lures to tackle box...",
                                        color = MaterialTheme.colorScheme.primary) },
                                leadingContent = { Icon(Icons.Default.Add, null) },
                                modifier = Modifier.clickable {
                                    showSheet = false
                                    onAdd()
                                }
                            )
                        }
                    }

                    if (onClear != null && selectedItem != null) {
                        item {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Clear selected lure",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.clickable {
                                    showSheet = false
                                    onClear()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
