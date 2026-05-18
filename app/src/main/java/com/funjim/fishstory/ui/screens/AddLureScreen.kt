package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureGlowColorCrossRef
import com.funjim.fishstory.model.LurePrimaryColorCrossRef
import com.funjim.fishstory.model.LureSecondaryColorCrossRef
import com.funjim.fishstory.model.LureWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.utils.LureColorComposition
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLureScreen(
    viewModel: LureViewModel,
    lureId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors by viewModel.lureColors.collectAsState(initial = emptyList())
    val sortedColors = remember(colors) { colors.sortedBy { it.name } }

    var originalLure by remember { mutableStateOf<LureWithDetails?>(null) }

    // Form State
    var name by remember { mutableStateOf("") }
    var hookCount by remember { mutableIntStateOf(1) }
    var glows by remember { mutableStateOf(false) }
    var photos by remember { mutableStateOf<List<Photo>>(emptyList()) }

    var selectedPrimaryColors by remember { mutableStateOf<List<LureColor>>(emptyList()) }
    var selectedSecondaryColors by remember { mutableStateOf<List<LureColor>>(emptyList()) }
    var selectedGlowColors by remember { mutableStateOf<List<LureColor>>(emptyList()) }

    val hasChanges by remember(
        originalLure, name, hookCount, glows,
        selectedPrimaryColors, selectedSecondaryColors, selectedGlowColors,
        photos
    ) {
        derivedStateOf {
            originalLure?.let { original ->
                name != original.lure.name ||
                        hookCount != original.lure.hookCount ||
                        glows != original.lure.glows ||
                        selectedPrimaryColors != original.primaryColors ||
                        selectedSecondaryColors != original.secondaryColors ||
                        selectedGlowColors != original.glowColors ||
                        photos != original.photos
            } ?: name.isNotBlank() // If lureId is null, check if it's a valid new lure
        }
    }

    // Load data if editing
    LaunchedEffect(lureId) {
        if (lureId != null) {
            val lure = viewModel.getLureWithDetails(lureId) // Ensure this exists in your ViewModel
            lure?.let {
                originalLure = it

                name = it.lure.name
                hookCount = it.lure.hookCount
                glows = it.lure.glows

                selectedPrimaryColors = it.primaryColors
                selectedSecondaryColors = it.secondaryColors
                selectedGlowColors = it.glowColors
                photos = it.photos
            }
        }
    }

    // Dropdown/Dialog States
    var showAddColorDialog by remember { mutableStateOf<ColorTarget?>(null) }
    var newColorName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lureId == null) "New Lure" else "Edit Lure") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Lure Name") },
                placeholder = { Text("e.g. Rapala Shad Rap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            LureColorSelectionField(
                items = sortedColors,
                selectedItems = selectedPrimaryColors,
                label = "Primary Color",
                onSelected = {
                    selectedPrimaryColors = if (selectedPrimaryColors.contains(it)) {
                        selectedPrimaryColors - it
                    } else {
                        selectedPrimaryColors + it
                    }
                    if (selectedPrimaryColors.isEmpty()) {
                        selectedPrimaryColors = selectedSecondaryColors
                        selectedSecondaryColors = emptyList()
                    }
                },
                onAdd = { showAddColorDialog = ColorTarget.PRIMARY },
                onClear = {
                    selectedPrimaryColors = selectedSecondaryColors
                    selectedSecondaryColors = emptyList()
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedPrimaryColors.isNotEmpty()) {
                LureColorComposition(
                    primary = selectedPrimaryColors
                )
            }

            if (selectedPrimaryColors.isNotEmpty()) {
                LureColorSelectionField(
                    items = sortedColors,
                    selectedItems = selectedSecondaryColors,
                    label = "Secondary Color",
                    onSelected = {
                        selectedSecondaryColors = if (selectedSecondaryColors.contains(it)) {
                            selectedSecondaryColors - it
                        } else {
                            selectedSecondaryColors + it
                        }
                    },
                    onAdd = { showAddColorDialog = ColorTarget.SECONDARY },
                    onClear = { selectedSecondaryColors = emptyList() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (selectedSecondaryColors.isNotEmpty()) {
                LureColorComposition(
                    secondary = selectedSecondaryColors
                )
            }

            // TODO -- change this to an Int field
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = (hookCount == 1),
                    onCheckedChange = {
                        hookCount = if (it) {
                            1
                        } else {
                            3
                        }
                    })
                Text("Single Hook")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = glows,
                    onCheckedChange = {
                        glows = it
                        if (!glows && selectedGlowColors.isNotEmpty()) {
                            selectedGlowColors = emptyList()
                        }
                    })
                Text("Glows")
            }

            if (glows) {
                LureColorSelectionField(
                    items = sortedColors,
                    selectedItems = selectedGlowColors,
                    label = "Glow Color",
                    onSelected = {
                        selectedGlowColors = if (selectedGlowColors.contains(it)) {
                            selectedGlowColors - it
                        } else {
                            selectedGlowColors + it
                        }
                    },
                    onAdd = { showAddColorDialog = ColorTarget.GLOW },
                    onClear = { selectedGlowColors = emptyList() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (selectedGlowColors.isNotEmpty()) {
                LureColorComposition(
                    glows = glows,
                    glow = selectedGlowColors
                )
            }

            PhotoPickerRow(
                photos = photos,
                onPhotoSelected = { uri ->
                    scope.launch {
                        val metadata = viewModel.getPhotoMetadata(uri)
                        val exists = photos.find { it.hashcode == metadata.hashcode }
                        if (exists == null) {
                            photos = photos + (Photo(
                                uri = uri.toString(),
                                hashcode = metadata.hashcode,
                                thumbnail = metadata.thumbnail
                            ))
                        }
                    }
                },
                onPhotoTaken = { uri ->
                    scope.launch {
                        val metadata = viewModel.getPhotoMetadata(uri)
                        photos = photos + (Photo(
                            uri = uri.toString(),
                            hashcode = metadata.hashcode,
                            thumbnail = metadata.thumbnail
                        ))
                    }
                },
                onPhotoDeleted = { photo -> photos = photos.filter { it != photo } },
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        val newLure = Lure(
                            id = lureId ?: UUID.randomUUID().toString(),
                            name = name,
                            hookCount = hookCount,
                            glows = glows,
                        )
                        viewModel.upsertLure(newLure)

                        selectedPrimaryColors.forEach { primaryColor ->
                            viewModel.upsertLurePrimaryColorCrossRef(
                                LurePrimaryColorCrossRef(
                                    newLure.id,
                                    primaryColor.id
                                )
                            )
                        }
                        selectedSecondaryColors.forEach { secondaryColor ->
                            viewModel.upsertLureSecondaryColorCrossRef(
                                LureSecondaryColorCrossRef(
                                    newLure.id,
                                    secondaryColor.id
                                )
                            )
                        }
                        selectedGlowColors.forEach { glowColor ->
                            viewModel.upsertLureGlowColorCrossRef(
                                LureGlowColorCrossRef(
                                    newLure.id,
                                    glowColor.id
                                )
                            )
                        }

                        // Logic to identify the changes
                        val originalPhotos = originalLure?.photos ?: emptyList()
                        val currentPhotos = photos

                        val newPhotos = currentPhotos.filter { current ->
                            originalPhotos.none { it.id == current.id }
                        }

                        val deletedPhotos = originalPhotos.filter { original ->
                            currentPhotos.none { it.id == original.id }
                        }
                        viewModel.addLurePhotos(newLure.id, newPhotos)
                        viewModel.deleteLurePhotos(newLure.id, deletedPhotos)

                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && hasChanges
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
                                ColorTarget.PRIMARY -> selectedPrimaryColors = selectedPrimaryColors + newColor
                                ColorTarget.SECONDARY -> selectedSecondaryColors = selectedSecondaryColors + newColor
                                ColorTarget.GLOW -> selectedGlowColors = selectedGlowColors + newColor
                                null -> {}
                            }
                            showAddColorDialog = null
                            newColorName = ""
                        }
                    }
                }) { Text("New") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddColorDialog = null
                    newColorName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureColorSelectionField(
    items: List<LureColor>,
    selectedItems: List<LureColor>,
    label: String,
    onSelected: (LureColor) -> Unit,
    onAdd: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val maxSelections = 4

    // Display for the current selection
    OutlinedTextField(
        value = "${selectedItems.size} of $maxSelections colors selected",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false, // Prevents focus/keyboard on the main text field
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text(label) },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )


    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Colors (${selectedItems.size} of $maxSelections)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxHeight(0.8f)) {
                // Search bar inside the sheet
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Colors...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List inside the sheet
                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                val isSelectionLocked = selectedItems.size >= maxSelections

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

                        val isChecked = selectedItems.contains(item)
                        val isClickable = isChecked || !isSelectionLocked

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                // TODO -- hide the border for now
                                //.border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.medium)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(enabled = isClickable) {
                                    onSelected(item)
                                },
                            leadingContent = {
                                if (item.hexCode.isNullOrBlank()) {
                                    ThumbnailBox(
                                        thumbnail = null,
                                        imageVector = Icons.Default.Palette,
                                        modifier = Modifier.size(36.dp)
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
                                                borderColor,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Dynamic layout grid depending on hex count
                                        MultiColorCirclePreview(hexList = hexList)
                                    }
                                }
                            },
                            headlineContent = { Text(item.name) },
                            trailingContent = {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = null,
                                    enabled = isClickable
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = backgroundColor,
                                headlineColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    item {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Add color...", color = MaterialTheme.colorScheme.primary) },
                            leadingContent = { Icon(Icons.Default.Add, null) },
                            modifier = Modifier.clickable {
                                showSheet = false
                                onAdd()
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class ColorTarget { PRIMARY, SECONDARY, GLOW }