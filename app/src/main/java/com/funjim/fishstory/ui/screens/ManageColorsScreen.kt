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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageColorsScreen(
    viewModel: LureViewModel,
    navigateBack: () -> Unit
) {
    val colorsList by viewModel.lureColors.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var colorToDelete by remember { mutableStateOf<LureColor?>(null) }
    var colorToSelect by remember { mutableStateOf<LureColor?>(null) }
    var colorToEdit by remember { mutableStateOf<LureColor?>(null) }
    var editName by remember { mutableStateOf("") }

    // Tracks which species is receiving a new image
    var currentColorForPhoto by remember { mutableStateOf<LureColor?>(null) }

    // 1. Logic: Filter list based on search query
    val filteredColors = remember(searchQuery, colorsList) {
        colorsList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.name }
    }

    // 2. Logic: Should we show the "Add" button?
    // Show if the query isn't empty and doesn't exactly match an existing name
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
            // SEARCH & ADD SECTION
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search or add new color...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (showAddButton) {
                        IconButton(onClick = {
                            viewModel.addLureColor(LureColor(name = searchQuery.trim()))
                            searchQuery = ""
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

            // THE LIST
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(filteredColors, key = { _, s -> s.id }) { index, item ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    // Your Zebra Striping
                    val backgroundColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)

                    ListItem(
                        modifier = Modifier
                            .background(backgroundColor)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { menuExpanded = true }
                            ),
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
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                                    )
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
                        leadingContent = {
                            // Wrapped in a Box with combinedClickable for long-press registration
                            Box(
                                modifier = Modifier.combinedClickable(
                                    onClick = { /* Regular tap on image does nothing or shows preview */ },
                                    onLongClick = {
                                        currentColorForPhoto = item
                                        colorToSelect = item
                                    }
                                )
                            ) {
                                if (item.hexCode.isNullOrBlank()) {
                                    ThumbnailBox(
                                        thumbnail = null,
                                        imageVector = Icons.Default.Palette
                                    )
                                } else {
                                    val color = remember(item.hexCode) {
                                        try {
                                            Color(item.hexCode.toColorInt())
                                        } catch (e: Exception) {
                                            Color.Transparent
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            // The border uses the theme's surface color to create a "cutout" look
                                            .border(
                                                width = 4.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = backgroundColor
                        )
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }

    // TODO - see if there are lures using the color to be deleted?
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
            title = { Text("Rename Species") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    label = { Text("Species Name") }
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

    colorToSelect?.let { item ->
        AdvancedColorPickerDialog(
            initialColor = item.hexCode,
            onDismiss = { colorToSelect = null },
            onSave = { code ->
                viewModel.upsertLureColor(item.copy(hexCode = code))
                colorToSelect = null
            })
    }
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
                    onDismiss()
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