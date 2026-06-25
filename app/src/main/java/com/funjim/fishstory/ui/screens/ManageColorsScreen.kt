package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.viewmodels.LureViewModel
import androidx.core.graphics.toColorInt
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageColorsScreen(
    viewModel: LureViewModel,
    navigateBack: () -> Unit
) {
    val colorsList by viewModel.lureColors.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var colorToDelete by remember { mutableStateOf<LureColor?>(null) }
    var colorToEdit by remember { mutableStateOf<LureColor?>(null) }

    var colorToPickFor by remember { mutableStateOf<LureColor?>(null) }
    var pickMaxHexCodes by remember { mutableStateOf(1) } // Supports 1 or 4 hex values

    var editName by remember { mutableStateOf("") }
    var pendingNewColorName by remember { mutableStateOf<String?>(null) }

    val filteredColors = remember(searchQuery, colorsList) {
        colorsList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.name }
    }

    val showAddButton = searchQuery.isNotBlank() &&
            colorsList.none { it.name.equals(searchQuery.trim(), ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Colors") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search or add new color...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (showAddButton) {
                        IconButton(onClick = {
                            pendingNewColorName = searchQuery.trim()
                        }) {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add New",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                singleLine = true
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                val filteredSize = filteredColors.size
                itemsIndexed(filteredColors, key = { _, s -> s.id }) { index, item ->
                    var menuExpanded by remember { mutableStateOf(false) }
                    var thumbnailMenuExpanded by remember { mutableStateOf(false) }

                    val backgroundColor = getCardColor(index, filteredSize)
                    val borderColor = getCardBorderColor(index, filteredSize)

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = borderColor,
                                shape = MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium)
                            .combinedClickable(
                                onClick = { /* do nothing */ },
                                onLongClick = { menuExpanded = true }
                            ),
                        leadingContent = {
                            Box(
                                modifier = Modifier.combinedClickable(
                                    onClick = { /* do nothing */ },
                                    onLongClick = {
                                        thumbnailMenuExpanded = true
                                    }
                                )
                            ) {
                                if (item.hexCode.isNullOrBlank()) {
                                    ThumbnailBox(
                                        thumbnail = null,
                                        imageVector = Icons.Default.Palette,
                                        modifier = Modifier.size(48.dp)
                                    )
                                } else {
                                    val hexList = remember(item.hexCode) {
                                        item.hexCode.split(",").filter { it.isNotBlank() }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.onSurface,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Dynamic layout grid depending on hex count
                                        MultiColorCirclePreview(hexList = hexList)
                                    }
                                }

                                DropdownMenu(
                                    expanded = thumbnailMenuExpanded,
                                    onDismissRequest = { thumbnailMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Select Single Color") },
                                        onClick = {
                                            thumbnailMenuExpanded = false
                                            pickMaxHexCodes = 1
                                            colorToPickFor = item
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Select Multiple Colors (Up to 4)") },
                                        onClick = {
                                            thumbnailMenuExpanded = false
                                            pickMaxHexCodes = 4
                                            colorToPickFor = item
                                        }
                                    )
                                    // Disable the clearing of the HEXCODE for the color
/*
                                    if (!item.hexCode.isNullOrBlank()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Clear Color(s)",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                thumbnailMenuExpanded = false
                                                // Save out a null reference to clear
                                                viewModel.upsertLureColor(item.copy(hexCode = null))
                                            }
                                        )
                                    }
*/
                                }
                            }
                        },
                        headlineContent = { Text(item.name) },
                        trailingContent = {
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options"
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            menuExpanded = false
                                            colorToEdit = item
                                            editName = item.name
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Select Single Color") },
                                        onClick = {
                                            thumbnailMenuExpanded = false
                                            pickMaxHexCodes = 1
                                            colorToPickFor = item
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Palette, contentDescription = "Edit")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Select Multiple Colors (Up to 4)") },
                                        onClick = {
                                            thumbnailMenuExpanded = false
                                            pickMaxHexCodes = 4
                                            colorToPickFor = item
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Palette, contentDescription = "Edit")
                                        }
                                    )
// Disable the clearing of the HEXCODE for the color
/*
                                    if (!item.hexCode.isNullOrBlank()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Clear Color(s)",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                thumbnailMenuExpanded = false
                                                // Save out a null reference to clear
                                                viewModel.upsertLureColor(item.copy(hexCode = null))
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Clear, contentDescription = "Edit")
                                            }
                                        )
                                    }
*/
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            menuExpanded = false
                                            colorToDelete = item
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = backgroundColor)
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }

    // DELETE CONFIRMATION
    colorToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { colorToDelete = null },
            title = { Text("Delete Color?") },
            text = { Text("""Are you sure you want to delete '${item.name}'?

This cannot be undone.

Lures that were using this color may not have a color assigned to them.
"""
            ) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLureColor(item)
                        colorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { colorToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // EDIT DIALOG
    colorToEdit?.let { item ->
        AlertDialog(
            onDismissRequest = { colorToEdit = null },
            title = { Text("Rename Color") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    label = { Text("Color Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.upsertLureColor(item.copy(name = editName.trim()))
                    colorToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { colorToEdit = null }) { Text("Cancel") }
            }
        )
    }

    colorToPickFor?.let { item ->
        AdvancedColorPickerDialog(
            initialColor = item.hexCode,
            maxAllowedColors = pickMaxHexCodes, // Tell picker if it allows multi-select
            onDismiss = {
                colorToPickFor = null
            },
            onSave = { finalizedCommaSeparatedString ->
                viewModel.upsertLureColor(item.copy(hexCode = finalizedCommaSeparatedString))
                colorToPickFor = null
            }
        )
    }

    pendingNewColorName?.let { name ->
        AdvancedColorPickerDialog(
            initialColor = null,
            maxAllowedColors = 1,
            onDismiss = {
                pendingNewColorName = null
            },
            onSave = { hexCode ->
                viewModel.addLureColor(LureColor(name = name, hexCode = hexCode))
                pendingNewColorName = null
                searchQuery = ""
            }
        )
    }
}

@Composable
fun MultiColorCirclePreview(hexList: List<String>) {
    val colors = remember(hexList) {
        hexList.map { hex ->
            try {
                Color(hex.toColorInt())
            } catch (e: Exception) {
                Color.Transparent
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (colors.size) {
            1 -> {
                Box(modifier = Modifier.fillMaxSize().background(colors[0]))
            }
            2 -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(colors[0]))
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(colors[1]))
            }
            3 -> {
                // Split top row into half, bottom row solid or mixed
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[0]))
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[1]))
                }
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(colors[2]))
            }
            else -> { // 4 colors (Quadrants)
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[0]))
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[1]))
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[2]))
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[3]))
                }
            }
        }
    }
}

@Composable
fun AdvancedColorPickerDialog(
    initialColor: String?, // Now could be something like "#FF0000,#00FF00" or null
    maxAllowedColors: Int, // Governs if we cap at 1 or 4 selections
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val predefinedColors = listOf(
        // --- High-Visibility & Attractors ---
        "#7FFF00", // Chartreuse (Traditional Yellow-Green)
        "#DFFF00", // Chartreuse (Neon/Web/Fluorescent)
        "#00FF00", // Lime Green
        "#FF69B4", // Hot Pink / Bubblegum
        "#FF00FF", // Magenta / Fuchsia
        "#FF4500", // Orange (Firetiger Orange)
        "#FFCC00", // Bright Yellow

        // --- Natural & Forage Mimics ---
        "#556B2F", // Green Pumpkin / Pumpkinseed
        "#708238", // Watermelon Green
        "#6B8E23", // Olive Drab
        "#4682B4", // Shad Blue / Tennessee Shad
        "#F8F8FF", // Pearl White
        "#E6DFD3", // Bone / Off-White
        "#990000", // Crawfish Red
        "#8B4513", // Saddle Brown

        // --- Metallics & Flash ---
        "#C0C0C0", // Silver / Chrome
        "#FFD700", // Gold
        "#B87333", // Copper

        // MISC
        "#0000FF", // BLUE
        "#FFFF00", // YELLOW
        "#FFA500", // ORANGE
        "#4B0082", // PURPLE

        // --- The Wonderbread Pattern ---
        "#FFFFFF", // Wonderbread Base (Pure White)
        "#FFF79A", // Wonderbread Dot 1 (Pastel Yellow)
        "#FEC3E1", // Wonderbread Dot 2 (Pastel Pink)
        "#A9E2F3", // Wonderbread Dot 3 (Pastel Blue)

        // --- Contrast & Accents ---
        "#000000", // Black (Strong Silhouette)
        "#FF0000", // Red (Bleeding Red)
        "#300060"  // Deep Purple
    )

    // Parse existing hex inputs, stripping hash markers
    var selectedHexList by remember {
        mutableStateOf(
            initialColor?.split(",")
                ?.filter { it.isNotBlank() }
                ?.map { it.removePrefix("#").trim().uppercase() }
                ?.take(maxAllowedColors)
                ?: listOf("FFFFFF")
        )
    }

    // Active working cursor (for text field adjustment manipulation)
    var activeFieldInput by remember { mutableStateOf(selectedHexList.firstOrNull() ?: "FFFFFF") }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(
                text = if (maxAllowedColors > 1) "Select Colors (Up to 4)" else "Select Lure Color",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Multi-Color Preview Row Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Selected:", style = MaterialTheme.typography.labelMedium)
                    selectedHexList.forEach { hex ->
                        val chipColor = try { Color("#$hex".toColorInt()) } catch(e: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
                                    // Tap a badge to remove it if multi-mode is on
                                    if (selectedHexList.size > 1) {
                                        selectedHexList = selectedHexList.filter { it != hex }
                                        activeFieldInput = selectedHexList.last()
                                    }
                                }
                        )
                    }
                }

                Text(
                    text = "Quick Select Palette",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    items(predefinedColors) { rawHex ->
                        val cleanHex = rawHex.removePrefix("#").uppercase()
                        val isSelected = selectedHexList.contains(cleanHex)
                        val itemColor = remember { Color(android.graphics.Color.parseColor(rawHex)) }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(itemColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable {
                                    if (maxAllowedColors == 1) {
                                        selectedHexList = listOf(cleanHex)
                                        activeFieldInput = cleanHex
                                    } else {
                                        if (isSelected) {
                                            // Remove if selected
                                            if (selectedHexList.size > 1) {
                                                selectedHexList = selectedHexList.filter { it != cleanHex }
                                            }
                                        } else if (selectedHexList.size < 4) {
                                            // Add item up to 4 elements max
                                            selectedHexList = selectedHexList + cleanHex
                                        }
                                        activeFieldInput = cleanHex
                                    }
                                }
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val fallbackPreview = remember(activeFieldInput) {
                        try { Color("#$activeFieldInput".toColorInt()) } catch (e: Exception) { Color.LightGray }
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(fallbackPreview)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )

                    OutlinedTextField(
                        value = activeFieldInput,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                            if (filtered.length <= 6) {
                                activeFieldInput = filtered
                                if (filtered.length == 6) {
                                    // Update the selected item list entry actively
                                    if (maxAllowedColors == 1) {
                                        selectedHexList = listOf(filtered.uppercase())
                                    } else {
                                        val updated = selectedHexList.toMutableList()
                                        if (updated.isNotEmpty()) {
                                            updated[updated.lastIndex] = filtered.uppercase()
                                            selectedHexList = updated
                                        }
                                    }
                                }
                            }
                        },
                        leadingIcon = { Text("#", style = MaterialTheme.typography.bodyLarge) },
                        label = { Text(if (maxAllowedColors > 1) "Modify Last Hex" else "Hex Code") },
                        placeholder = { Text("RRGGBB") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Combine all selected hex values with standard leading '#' hashes into a clear comma-string
                    val databaseOutputString = selectedHexList.joinToString(",") { hex ->
                        "#${hex.uppercase().padEnd(6, '0')}"
                    }
                    onSave(databaseOutputString)
                },
                enabled = selectedHexList.all { it.length == 6 }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdvancedColorPickerDialog(
    initialColor: String?, // e.g., "#FF0000" or null
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val predefinedColors = listOf(
        // --- High-Visibility & Attractors ---
        "#7FFF00", // Chartreuse (Traditional Yellow-Green)
        "#DFFF00", // Chartreuse (Neon/Web/Fluorescent)
        "#00FF00", // Lime Green
        "#FF69B4", // Hot Pink / Bubblegum
        "#FF00FF", // Magenta / Fuchsia
        "#FF4500", // Orange (Firetiger Orange)
        "#FFCC00", // Bright Yellow

        // --- Natural & Forage Mimics ---
        "#556B2F", // Green Pumpkin / Pumpkinseed
        "#708238", // Watermelon Green
        "#6B8E23", // Olive Drab
        "#4682B4", // Shad Blue / Tennessee Shad
        "#F8F8FF", // Pearl White
        "#E6DFD3", // Bone / Off-White
        "#990000", // Crawfish Red
        "#8B4513", // Saddle Brown

        // --- Metallics & Flash ---
        "#C0C0C0", // Silver / Chrome
        "#FFD700", // Gold
        "#B87333", // Copper

        // MISC
        "#0000FF", // BLUE
        "#FFFF00", // YELLOW
        "#FFA500", // ORANGE
        "#4B0082", // PURPLE

        // --- The Wonderbread Pattern ---
        "#FFFFFF", // Wonderbread Base (Pure White)
        "#FFF79A", // Wonderbread Dot 1 (Pastel Yellow)
        "#FEC3E1", // Wonderbread Dot 2 (Pastel Pink)
        "#A9E2F3", // Wonderbread Dot 3 (Pastel Blue)

        // --- Contrast & Accents ---
        "#000000", // Black (Strong Silhouette)
        "#FF0000", // Red (Bleeding Red)
        "#300060"  // Deep Purple
    )

    // State to track the current text input (stripping the '#' internally for easier typing)
    var hexInput by remember {
        mutableStateOf(initialColor?.removePrefix("#") ?: "FFFFFF")
    }

    // Live color parsing for the preview circle
    val previewColor = remember(hexInput) {
        try {
            // Re-add the '#' safely to parse
            Color("#$hexInput".toColorInt())
        } catch (e: Exception) {
            Color.LightGray // Fallback if the user is mid-typing an invalid hex
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Lure Color", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: Predefined Quick-Select Grid
                Text(
                    text = "Quick Select",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp) // Bound the grid height in the dialog
                ) {
                    items(predefinedColors) { hex ->
                        val itemColor = remember { Color(android.graphics.Color.parseColor(hex)) }
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(itemColor)
                                .border(
                                    width = if (hexInput.uppercase() == hex.removePrefix("#")) 3.dp else 1.dp,
                                    color = if (hexInput.uppercase() == hex.removePrefix("#")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable {
                                    // Clicking a preset updates the text field and the preview instantly
                                    hexInput = hex.removePrefix("#")
                                }
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp)

                // SECTION 2: Custom Fine-Tuning & Live Preview
                Text(
                    text = "Custom Hex Adjustment",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Live Preview Circle with a cut-out border style
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )

                    // Hex Manual Input
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { newValue ->
                            // Restrict to valid hex characters and max length of 6
                            val filtered = newValue.filter { it.isDigit() || it.uppercaseChar() in 'A'..'F' }
                            if (filtered.length <= 6) {
                                hexInput = filtered
                            }
                        },
                        leadingIcon = { Text("#", style = MaterialTheme.typography.bodyLarge) },
                        label = { Text("Hex Code") },
                        placeholder = { Text("RRGGBB") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Format back into a clean standard hex string before saving to database
                    val finalHex = "#${hexInput.uppercase().padEnd(6, '0')}"
                    onSave(finalHex)
                },
                enabled = hexInput.length == 6 // Only allow saving if it's a complete color
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}