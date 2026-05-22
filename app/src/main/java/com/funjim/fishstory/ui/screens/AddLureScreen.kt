package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getCardContentColor
import com.funjim.fishstory.ui.utils.getGridCardBorderColor
import com.funjim.fishstory.ui.utils.getGridCardColor
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
                TextField(
                    value = newColorName,
                    onValueChange = { newColorName = it },
                    placeholder = { Text("Color Name") }
                )
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
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    val maxSelections = 4

    // Display for the current selection
    OutlinedTextField(
        value = "${selectedItems.size} of $maxSelections colors selected",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
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
                        text = "Select Colors (${selectedItems.size} of $maxSelections)",
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
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxHeight(0.8f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Colors...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                val isSelectionLocked = selectedItems.size >= maxSelections
                val filteredSize = filtered.size

                if (isGridView) {
                    // ── GRID VIEW ───────────────────────────────────────────
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        gridItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isChecked = selectedItems.contains(item)
                            val isClickable = isChecked || !isSelectionLocked

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        width = if (isChecked) 2.dp else 0.dp,
                                        color =
                                            if (isChecked) getCardContentColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable(enabled = isClickable) { onSelected(item) },
                                leadingContent = null,
                                headlineContent = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth() // Forces the column to span the whole grid cell width
                                            .padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        ColorPreviewIcon(
                                            item,
                                            getGridCardBorderColor(index, filteredSize),
                                            isGrid = true)

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                trailingContent = null,
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize, isChecked),
                                    headlineColor = getCardContentColor()
                                )
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AddColorButton(onAdd)
                        }
                    }
                } else {
                    // ── LIST VIEW ───────────────────────────────────────────
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        listItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isChecked = selectedItems.contains(item)
                            val isClickable = isChecked || !isSelectionLocked

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        width = if (isChecked) 2.dp else 0.dp,
                                        color =
                                            if (isChecked) getCardContentColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable(enabled = isClickable) { onSelected(item) },
                                leadingContent = {
                                    ColorPreviewIcon(item, getCardBorderColor(index, filteredSize), isGrid = false)
                                },
                                headlineContent = {
                                    Text(
                                        item.name,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                trailingContent = {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = null,
                                        enabled = isClickable
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize, isChecked),
                                    headlineColor = getCardContentColor()
                                )
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item {
                            AddColorButton(onAdd)
                        }
                    }
                }
            }
        }
    }
}

// Helper Composable to clean up duplicate asset drawing rules across views
@Composable
private fun ColorPreviewIcon(item: LureColor, borderColor: Color, isGrid: Boolean) {
    val size = if (isGrid) 32.dp else 48.dp
    if (item.hexCode.isNullOrBlank()) {
        ThumbnailBox(
            thumbnail = null,
            imageVector = Icons.Default.Palette,
            modifier = Modifier.size(size)
        )
    } else {
        val hexList = remember(item.hexCode) {
            item.hexCode.split(",").filter { it.isNotBlank() }
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(if (isGrid) 1.5.dp else 2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MultiColorCirclePreview(hexList = hexList)
        }
    }
}

// Helper Composable for the footer button action
@Composable
private fun AddColorButton(onAdd: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                "Add color...",
                color = getCardContentColor(),
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingContent = { Icon(Icons.Default.Add, null) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onAdd() }
    )
}
private enum class ColorTarget { PRIMARY, SECONDARY, GLOW }