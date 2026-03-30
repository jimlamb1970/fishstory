package com.funjim.fishstory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLureScreen(
    viewModel: MainViewModel,
    lureId: String? = null, // Pass null for "Add", pass ID for "Edit"
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val colors by viewModel.lureColors.collectAsState(initial = emptyList())
    val sortedColors = remember(colors) { colors.sortedBy { it.name } }

    // Form State
    var name by remember { mutableStateOf("") }
    var selectedPrimaryColorId by remember { mutableStateOf<String?>(null) }
    var selectedSecondaryColorId by remember { mutableStateOf<String?>(null) }
    var isSingleHook by remember { mutableStateOf(false) }
    var glows by remember { mutableStateOf(false) }
    var selectedGlowColorId by remember { mutableStateOf<String?>(null) }

    // Load data if editing
    LaunchedEffect(lureId) {
        if (lureId != null) {
            val lure = viewModel.getLureById(lureId) // Ensure this exists in your ViewModel
            lure?.let {
                name = it.name
                selectedPrimaryColorId = it.primaryColorId
                selectedSecondaryColorId = it.secondaryColorId
                isSingleHook = it.hasSingleHook
                glows = it.glows
                selectedGlowColorId = it.glowColorId
            }
        }
    }

    // Dropdown/Dialog States
    var primaryExpanded by remember { mutableStateOf(false) }
    var secondaryExpanded by remember { mutableStateOf(false) }
    var glowExpanded by remember { mutableStateOf(false) }
    var showAddColorDialog by remember { mutableStateOf<ColorTarget?>(null) }
    var newColorName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lureId == null) "New Lure" else "Edit Lure") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Lure Name") },
                placeholder = { Text("e.g. Rapala Shad Rap") },
                modifier = Modifier.fillMaxWidth()
            )

            // Primary Color
            LureColorDropdown(
                label = "Primary Color",
                selectedColorId = selectedPrimaryColorId,
                colors = sortedColors,
                expanded = primaryExpanded,
                onExpandedChange = { primaryExpanded = it },
                onSelect = { selectedPrimaryColorId = it },
                onAddNew = { showAddColorDialog = ColorTarget.PRIMARY }
            )

            // Secondary Color
            LureColorDropdown(
                label = "Secondary Color (Optional)",
                selectedColorId = selectedSecondaryColorId,
                colors = sortedColors,
                expanded = secondaryExpanded,
                onExpandedChange = { secondaryExpanded = it },
                onSelect = { selectedSecondaryColorId = it },
                onAddNew = { showAddColorDialog = ColorTarget.SECONDARY }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSingleHook, onCheckedChange = { isSingleHook = it })
                Text("Single Hook")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = glows, onCheckedChange = { glows = it })
                Text("Glows")
            }

            if (glows) {
                LureColorDropdown(
                    label = "Glow Color",
                    selectedColorId = selectedGlowColorId,
                    colors = sortedColors,
                    expanded = glowExpanded,
                    onExpandedChange = { glowExpanded = it },
                    onSelect = { selectedGlowColorId = it },
                    onAddNew = { showAddColorDialog = ColorTarget.GLOW }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        val newLure = Lure(
                            id = lureId ?: UUID.randomUUID().toString(),
                            name = name,
                            primaryColorId = selectedPrimaryColorId,
                            secondaryColorId = selectedSecondaryColorId,
                            hasSingleHook = isSingleHook,
                            glows = glows,
                            glowColorId = selectedGlowColorId
                        )
                        viewModel.addLure(newLure)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && selectedPrimaryColorId != null
            ) {
                Text("Save Lure")
            }
        }
    }

    // Add Color Dialog
    if (showAddColorDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddColorDialog = null; newColorName = "" },
            title = { Text("Add New Color") },
            text = {
                TextField(value = newColorName, onValueChange = { newColorName = it }, placeholder = { Text("Color Name") })
            },
            confirmButton = {
                Button(onClick = {
                    if (newColorName.isNotBlank()) {
                        scope.launch {
                            val newColor = LureColor(name = newColorName)
                            viewModel.addLureColor(newColor)
                            val id = newColor.id
                            when (showAddColorDialog) {
                                ColorTarget.PRIMARY -> selectedPrimaryColorId = id
                                ColorTarget.SECONDARY -> selectedSecondaryColorId = id
                                ColorTarget.GLOW -> selectedGlowColorId = id
                                null -> {}
                            }
                            showAddColorDialog = null
                            newColorName = ""
                        }
                    }
                }) { Text("Add") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LureColorDropdown(
    label: String,
    selectedColorId: String?,
    colors: List<LureColor>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    onAddNew: () -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        val text = colors.find { it.id == selectedColorId }?.name ?: "Select Color"
        OutlinedTextField(
            value = text,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            colors.forEach { color ->
                DropdownMenuItem(
                    text = { Text(color.name) },
                    onClick = { onSelect(color.id); onExpandedChange(false) }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Add color...", color = MaterialTheme.colorScheme.primary) },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = { onExpandedChange(false); onAddNew() }
            )
        }
    }
}

private enum class ColorTarget { PRIMARY, SECONDARY, GLOW }