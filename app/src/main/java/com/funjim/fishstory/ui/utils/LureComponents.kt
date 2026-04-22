package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Photo

@Composable
fun ManageColorsDialog(
    colors: List<LureColor>,
    onDismiss: () -> Unit,
    onAddColor: (String) -> Unit,
    onDeleteColor: (LureColor) -> Unit
) {
    var newColorName by remember { mutableStateOf("") }
    val sortedColors = remember(colors) { colors.sortedBy { it.name } }

    var colorToDelete by remember { mutableStateOf<LureColor?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Colors") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                OutlinedTextField(
                    value = newColorName,
                    onValueChange = { newColorName = it },
                    label = { Text("Add New Color") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newColorName.isNotBlank()) {
                                onAddColor(newColorName.trim())
                                newColorName = ""
                            }
                        }
                    ),
                    trailingIcon = {
                        IconButton(
                            enabled = newColorName.isNotBlank(),
                            onClick = {
                                onAddColor(newColorName.trim())
                                newColorName = ""
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (sortedColors.isEmpty()) {
                    // Default message for empty list
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No colors added yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(horizontal = 4.dp)
                    ) {
                        itemsIndexed(sortedColors, key = { _, color -> color.id }) { index, color ->
                            // Calculate the background color based on the index
                            val backgroundColor = if (index % 2 == 0) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                // Use a very light tint of your primary or surfaceVariant
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            }

                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = color.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                trailingContent = {
                                    IconButton(onClick = { colorToDelete = color }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = backgroundColor
                                )
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )

    // Confirmation Sub-Dialog
    colorToDelete?.let { color ->
        AlertDialog(
            onDismissRequest = { colorToDelete = null },
            title = { Text("Delete Color?") },
            text = { Text("Are you sure you want to delete '${color.name}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteColor(color)
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
}

@Composable
fun LureItem(
    lure: Lure,
    index: Int = 0,
    totalItems: Int = 0,
    primaryColorName: String?,
    secondaryColorName: String?,
    glowColorName: String?,
    photos: List<Photo>,
    onAddPhoto: ((Photo) -> Unit)? = null,
    onDeletePhoto: ((Photo) -> Unit)? = null,
    onEdit: () -> Unit, 
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val backgroundColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lure.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val sb = StringBuilder("Colors: ")
                    val colors = mutableListOf<String>()
                    if (!primaryColorName.isNullOrBlank()) {
                        colors.add(primaryColorName)
                    }
                    if (!secondaryColorName.isNullOrBlank()) {
                        colors.add(secondaryColorName)
                    }
                    if (colors.isNotEmpty()) {
                        sb.append(" ${colors.joinToString("/")}")
                        Text(
                            text = sb.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (lure.glows) {
                        val sb = StringBuilder("Glows")
                        if (!glowColorName.isNullOrBlank()) {
                            sb.append(" : $glowColorName")
                        }
                        Text(
                            text = sb.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary)
                    }

                    Text(
                        text = if (lure.hasSingleHook) "Single Hook" else "Multiple Hooks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
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

            if (onAddPhoto != null && onDeletePhoto != null) {
                PhotoPickerRow(
                    photos = photos,
                    onPhotoSelected = { uri ->
                        onAddPhoto(Photo(uri = uri.toString(), lureId = lure.id))
                    },
                    onPhotoDeleted = { photo ->
                        onDeletePhoto(photo)
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
