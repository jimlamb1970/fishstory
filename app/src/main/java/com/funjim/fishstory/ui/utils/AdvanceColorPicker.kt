package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.graphics.toColorInt

@Composable
fun AdvancedColorPickerDialog(
    initialColor: String?, // Now could be something like "#FF0000,#00FF00" or null
    maxAllowedColors: Int, // Governs if we cap at 1 or 4 selections
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val predefinedColors = listOf(
        // --- High-Visibility & Attractors ---
        "#7FFF00", // Chartreuse (Traditional Yellow-Green)
        "#DFFF00", // Chartreuse (Neon/Web/Fluorescent)
        "#00FF00", // Lime Green
        "#FF69B4", // Hot Pink / Bubblegum
        "#FF00FF", // Magenta / Fuchsia
        "#FF4500", // Orange (Fire-tiger Orange)
        "#FFCC00", // Bright Yellow

        // --- Natural & Forage Mimics ---
        "#556B2F", // Green Pumpkin / Pumpkin seed
        "#708238", // Watermelon Green
        "#6B8E23", // Olive Drab
        "#4682B4", // Shad Blue / Tennessee Shad
        "#F8F8FF", // Pearl White
        "#E6DFD3", // Bone / Off-White
        "#990000", // Crawfish Red
        "#8B4513", // Saddle Brown

        // --- Metallic & Flash ---
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
                text = if (maxAllowedColors > 1) "Select Colors (Up to 4)" else "Select Color",
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
                        val chipColor = try { Color("#$hex".toColorInt()) } catch(_: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable {
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
                        val itemColor = remember { Color(rawHex.toColorInt()) }

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
                        try {
                            Color("#$activeFieldInput".toColorInt())
                        } catch (_: Exception) {
                            Color.LightGray
                        }
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
                    onConfirm(databaseOutputString)
                },
                enabled = selectedHexList.all { it.length == 6 }
            ) {
                Text("OK")
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
    onAdd: (String) -> Unit
) {
    val predefinedColors = listOf(
        // --- High-Visibility & Attractors ---
        "#7FFF00", // Chartreuse (Traditional Yellow-Green)
        "#DFFF00", // Chartreuse (Neon/Web/Fluorescent)
        "#00FF00", // Lime Green
        "#FF69B4", // Hot Pink / Bubblegum
        "#FF00FF", // Magenta / Fuchsia
        "#FF4500", // Orange (Fire tiger Orange)
        "#FFCC00", // Bright Yellow

        // --- Natural & Forage Mimics ---
        "#556B2F", // Green Pumpkin / Pumpkin seed
        "#708238", // Watermelon Green
        "#6B8E23", // Olive Drab
        "#4682B4", // Shad Blue / Tennessee Shad
        "#F8F8FF", // Pearl White
        "#E6DFD3", // Bone / Off-White
        "#990000", // Crawfish Red
        "#8B4513", // Saddle Brown

        // --- Metallic & Flash ---
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
        } catch (_: Exception) {
            Color.LightGray // Fallback if the user is mid-typing an invalid hex
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color", fontWeight = FontWeight.Bold) },
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
                        val itemColor = remember { Color(hex.toColorInt()) }
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
                    onAdd(finalHex)
                },
                enabled = hexInput.length == 6 // Only allow saving if it's a complete color
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
