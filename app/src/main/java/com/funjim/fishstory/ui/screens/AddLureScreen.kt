package com.funjim.fishstory.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureWithPhotos
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

private fun generateThumbnail(context: Context,uri: Uri): ByteArray {
    val bitmap = context.contentResolver.loadThumbnail(uri, Size(200, 200), null)

    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        bitmap.recycle()
        out.toByteArray()
    }
}

fun md5Hash(context: Context, uri: Uri): String {
    val digest = MessageDigest.getInstance("MD5")
    context.contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(8192)
        var bytes = input.read(buffer)
        while (bytes != -1) {
            digest.update(buffer, 0, bytes)
            bytes = input.read(buffer)
        }
    } ?: throw IOException("Could not open input stream for URI: $uri")

    return digest.digest().joinToString("") { "%02x".format(it) }
}

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

    var originalLure by remember { mutableStateOf<LureWithPhotos?>(null) }

    // Form State
    var name by remember { mutableStateOf("") }
    var selectedPrimaryColorId by remember { mutableStateOf<String?>(null) }
    var selectedSecondaryColorId by remember { mutableStateOf<String?>(null) }
    var isSingleHook by remember { mutableStateOf(false) }
    var glows by remember { mutableStateOf(false) }
    var selectedGlowColorId by remember { mutableStateOf<String?>(null) }
    var photos by remember { mutableStateOf<List<Photo>>(emptyList()) }

    val hasChanges by remember(
        originalLure, name, selectedPrimaryColorId, selectedSecondaryColorId,
        isSingleHook, glows, selectedGlowColorId, photos
    ) {
        derivedStateOf {
            originalLure?.let { original ->
                name != original.lure.name ||
                        selectedPrimaryColorId != original.lure.primaryColorId ||
                        selectedSecondaryColorId != original.lure.secondaryColorId ||
                        isSingleHook != original.lure.hasSingleHook ||
                        glows != original.lure.glows ||
                        selectedGlowColorId != original.lure.glowColorId ||
                        photos != original.photos
            } ?: name.isNotBlank() // If lureId is null, check if it's a valid new lure
        }
    }

    // Load data if editing
    LaunchedEffect(lureId) {
        if (lureId != null) {
            val lure = viewModel.getLureWithPhotos(lureId) // Ensure this exists in your ViewModel
            lure?.let {
                originalLure = it

                name = it.lure.name
                selectedPrimaryColorId = it.lure.primaryColorId
                selectedSecondaryColorId = it.lure.secondaryColorId
                isSingleHook = it.lure.hasSingleHook
                glows = it.lure.glows
                selectedGlowColorId = it.lure.glowColorId
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Lure Name") },
                placeholder = { Text("e.g. Rapala Shad Rap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next // Moves focus to the next field/dismisses
                )
            )

            LureColorSelectionField(
                items = sortedColors,
                selectedItem = selectedPrimaryColorId,
                label = "Primary Color",
                onSelected = { selectedPrimaryColorId = it },
                onAdd = { showAddColorDialog = ColorTarget.PRIMARY },
                onClear = {
                    selectedPrimaryColorId = selectedSecondaryColorId
                    selectedSecondaryColorId = null
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedPrimaryColorId != null) {
                LureColorSelectionField(
                    items = sortedColors,
                    selectedItem = selectedSecondaryColorId,
                    label = "Secondary Color",
                    onSelected = { selectedSecondaryColorId = it },
                    onAdd = { showAddColorDialog = ColorTarget.SECONDARY },
                    onClear = { selectedSecondaryColorId = null },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSingleHook, onCheckedChange = { isSingleHook = it })
                Text("Single Hook")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = glows,
                    onCheckedChange = {
                        glows = it
                        if (!glows && selectedGlowColorId != null) {
                            selectedGlowColorId = null
                        }
                    })
                Text("Glows")
            }

            if (glows) {
                LureColorSelectionField(
                    items = sortedColors,
                    selectedItem = selectedGlowColorId,
                    label = "Glow Color",
                    onSelected = { selectedGlowColorId = it },
                    onAdd = { showAddColorDialog = ColorTarget.GLOW },
                    onClear = { selectedGlowColorId = null },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            PhotoPickerRow(
                photos = photos,
                onPhotoSelected = { uri ->
                    val hashcode = md5Hash(context, uri)
                    val existing = photos.find { it.hashcode == hashcode }
                    if (existing == null) {
                        photos = photos + (
                            Photo(
                                uri = uri.toString(),
                                hashcode = hashcode,
                                thumbnail = generateThumbnail(context, uri)
                            )
                        )
                    }
                },
                onPhotoDeleted = { photo -> photos = photos.filter { it != photo } },
                modifier = Modifier.padding(top = 8.dp)
            )

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
                        viewModel.upsertLure(newLure)

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
                                ColorTarget.PRIMARY -> selectedPrimaryColorId = id
                                ColorTarget.SECONDARY -> selectedSecondaryColorId = id
                                ColorTarget.GLOW -> selectedGlowColorId = id
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
    selectedItem: String?,
    label: String,
    onSelected: (String) -> Unit,
    onAdd: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val selectedItem = items.find { it.id == selectedItem }

    // Display for the current selection
    OutlinedTextField(
        value = selectedItem?.name ?: "Select Color",
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
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
                // Search bar inside the sheet
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Lures...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // List inside the sheet
                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    val filteredSize = filtered.size
                    itemsIndexed(filtered) { index, item ->
                        val backgroundColor = if ((index % 2 == 0) || (filteredSize < 4)) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            // Use a very light tint of your primary or surfaceVariant
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }

                        ListItem(
                            headlineContent = { Text(item.name) },
                            modifier = Modifier.clickable {
                                onSelected(item.id)
                                showSheet = false
                                searchQuery = ""
                            },
                            colors = ListItemDefaults.colors(containerColor = backgroundColor)
                        )
                    }

                    if (selectedItem != null) {
                        item {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "No color",
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

                    // "Add New" option
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