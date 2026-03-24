package com.funjim.fishstory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureDialog(
    initialName: String = "",
    initialPrimaryColorId: Int? = null,
    initialSecondaryColorId: Int? = null,
    initialIsSingleHook: Boolean = false,
    initialGlows: Boolean = false,
    initialGlowColorId: Int? = null,
    title: String = "New Lure",
    colors: List<LureColor>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, Int?, Boolean, Boolean, Int?) -> Unit,
    onAddColor: (String, (Int) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedPrimaryColorId by remember { mutableStateOf(initialPrimaryColorId) }
    var selectedSecondaryColorId by remember { mutableStateOf(initialSecondaryColorId) }
    var isSingleHook by remember { mutableStateOf(initialIsSingleHook) }
    var glows by remember { mutableStateOf(initialGlows) }
    var selectedGlowColorId by remember { mutableStateOf(initialGlowColorId) }
    
    var primaryColorExpanded by remember { mutableStateOf(false) }
    var secondaryColorExpanded by remember { mutableStateOf(false) }
    var glowColorExpanded by remember { mutableStateOf(false) }
    
    var showAddColorDialogForPrimary by remember { mutableStateOf(false) }
    var showAddColorDialogForSecondary by remember { mutableStateOf(false) }
    var showAddColorDialogForGlow by remember { mutableStateOf(false) }
    var newColorName by remember { mutableStateOf("") }

    val sortedColors = remember(colors) {
        colors.sortedBy { it.name }
    }

    if (showAddColorDialogForPrimary || showAddColorDialogForSecondary || showAddColorDialogForGlow) {
        AlertDialog(
            onDismissRequest = { 
                showAddColorDialogForPrimary = false
                showAddColorDialogForSecondary = false
                showAddColorDialogForGlow = false
                newColorName = ""
            },
            title = { Text("Add New Color") },
            text = {
                TextField(
                    value = newColorName,
                    onValueChange = { newColorName = it },
                    placeholder = { Text("Color Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newColorName.isNotBlank()) {
                        onAddColor(newColorName) { newId ->
                            if (showAddColorDialogForPrimary) {
                                selectedPrimaryColorId = newId
                            } else if (showAddColorDialogForSecondary) {
                                selectedSecondaryColorId = newId
                            } else if (showAddColorDialogForGlow) {
                                selectedGlowColorId = newId
                            }
                        }
                        showAddColorDialogForPrimary = false
                        showAddColorDialogForSecondary = false
                        showAddColorDialogForGlow = false
                        newColorName = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                Button(onClick = { 
                    showAddColorDialogForPrimary = false
                    showAddColorDialogForSecondary = false
                    showAddColorDialogForGlow = false
                    newColorName = ""
                }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Lure Name (e.g. Rapala Shad Rap)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Primary Color Dropdown
                ExposedDropdownMenuBox(
                    expanded = primaryColorExpanded,
                    onExpandedChange = { primaryColorExpanded = !primaryColorExpanded }
                ) {
                    val selectedColorName = sortedColors.find { it.id == selectedPrimaryColorId }?.name ?: "Select Primary Color"
                    TextField(
                        value = selectedColorName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary Color") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = primaryColorExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = primaryColorExpanded,
                        onDismissRequest = { primaryColorExpanded = false }
                    ) {
                        sortedColors.forEach { color ->
                            DropdownMenuItem(
                                text = { Text(color.name) },
                                onClick = {
                                    selectedPrimaryColorId = color.id
                                    primaryColorExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Add color...", color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                primaryColorExpanded = false
                                showAddColorDialogForPrimary = true
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Color Dropdown
                ExposedDropdownMenuBox(
                    expanded = secondaryColorExpanded,
                    onExpandedChange = { secondaryColorExpanded = !secondaryColorExpanded }
                ) {
                    val selectedColorName = sortedColors.find { it.id == selectedSecondaryColorId }?.name ?: "Select Secondary Color"
                    TextField(
                        value = selectedColorName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Secondary Color (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = secondaryColorExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = secondaryColorExpanded,
                        onDismissRequest = { secondaryColorExpanded = false }
                    ) {
                        sortedColors.forEach { color ->
                            DropdownMenuItem(
                                text = { Text(color.name) },
                                onClick = {
                                    selectedSecondaryColorId = color.id
                                    secondaryColorExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Add color...", color = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                secondaryColorExpanded = false
                                showAddColorDialogForSecondary = true
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSingleHook,
                        onCheckedChange = { isSingleHook = it }
                    )
                    Text("Single Hook")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = glows,
                        onCheckedChange = { glows = it }
                    )
                    Text("Glows")
                }

                if (glows) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = glowColorExpanded,
                        onExpandedChange = { glowColorExpanded = !glowColorExpanded }
                    ) {
                        val selectedGlowColorName = sortedColors.find { it.id == selectedGlowColorId }?.name ?: "Select Glow Color"
                        TextField(
                            value = selectedGlowColorName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Glow Color") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = glowColorExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = glowColorExpanded,
                            onDismissRequest = { glowColorExpanded = false }
                        ) {
                            sortedColors.forEach { color ->
                                DropdownMenuItem(
                                    text = { Text(color.name) },
                                    onClick = {
                                        selectedGlowColorId = color.id
                                        glowColorExpanded = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Add color...", color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    glowColorExpanded = false
                                    showAddColorDialogForGlow = true
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(name, selectedPrimaryColorId, selectedSecondaryColorId, isSingleHook, glows, selectedGlowColorId)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ManageColorsDialog(
    colors: List<LureColor>,
    onDismiss: () -> Unit,
    onAddColor: (String) -> Unit,
    onDeleteColor: (LureColor) -> Unit
) {
    var newColorName by remember { mutableStateOf("") }

    val sortedColors = remember(colors) {
        colors.sortedBy { it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Colors") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newColorName,
                        onValueChange = { newColorName = it },
                        placeholder = { Text("New Color") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newColorName.isNotBlank()) {
                            onAddColor(newColorName)
                            newColorName = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(sortedColors) { color ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(color.name)
                            IconButton(onClick = { onDeleteColor(color) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun LureItem(
    lure: Lure, 
    primaryColorName: String?,
    secondaryColorName: String?,
    glowColorName: String?,
    viewModel: MainViewModel,
    onEdit: () -> Unit, 
    onDelete: () -> Unit
) {
    val lurePhotos by viewModel.getPhotosForLure(lure.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName), style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (lure.hasSingleHook) "Single Hook" else "Multiple Hooks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            PhotoPickerRow(
                photos = lurePhotos,
                onPhotoSelected = { uri ->
                    scope.launch {
                        viewModel.addPhoto(Photo(uri = uri.toString(), lureId = lure.id))
                    }
                },
                onPhotoDeleted = { photo ->
                    scope.launch {
                        viewModel.deletePhoto(photo)
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
