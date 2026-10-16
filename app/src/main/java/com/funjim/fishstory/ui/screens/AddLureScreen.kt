package com.funjim.fishstory.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.LureColorComposition
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.crud.AddColorDialog
import com.funjim.fishstory.ui.utils.selection.LureColorSelectionField
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLureScreen(
    viewModel: LureViewModel,
    lureId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

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
    var newColorHex by remember { mutableStateOf<String?>(null) }
    var showColorPickerForNew by remember { mutableStateOf(false) }

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
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveLure(
                                lureId = lureId,
                                name = name,
                                hookCount = hookCount,
                                glows = glows,
                                primaryColors = selectedPrimaryColors,
                                secondaryColors = selectedSecondaryColors,
                                glowColors = selectedGlowColors,
                                originalLure = originalLure,
                                currentPhotos = photos,
                                onComplete = onNavigateBack
                            )
                        },
                        enabled = name.isNotBlank() && hasChanges
                    ) {
                        Icon(
                            imageVector = AppIcons.Default.Save,
                            modifier = Modifier.size(28.dp),
                            contentDescription = "Save Lure"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            viewModel.saveLure(
                                lureId = lureId,
                                name = name,
                                hookCount = hookCount,
                                glows = glows,
                                primaryColors = selectedPrimaryColors,
                                secondaryColors = selectedSecondaryColors,
                                glowColors = selectedGlowColors,
                                originalLure = originalLure,
                                currentPhotos = photos,
                                onComplete = onNavigateBack
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && hasChanges
                    ) {
                        Text(if (lureId == null) "Add Lure" else "Update Lure")
                    }
                }
            }
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
        }
    }

    // Step 1: Name dialog — collect the color name, then advance to hex picker
    if (showAddColorDialog != null && !showColorPickerForNew) {
        AddColorDialog(
            onConfirm = {
                newColorName = it
                showColorPickerForNew = true
            },
            onDismiss = {
                newColorName = ""
                showAddColorDialog = null
            }
        )
    }

    // Step 2: Hex picker — reuse AdvancedColorPickerDialog from ManageColorsScreen
    if (showColorPickerForNew) {
        AdvancedColorPickerDialog(
            initialColor = null,
            onDismiss = {
                showColorPickerForNew = false
                showAddColorDialog = null

                newColorName = ""
                newColorHex = null
            },
            onAdd = { hexCode ->
                val newColor = LureColor(name = newColorName, hexCode = hexCode)
                viewModel.addLureColor(newColor) { color ->
                    when (showAddColorDialog) {
                        ColorTarget.PRIMARY -> selectedPrimaryColors = selectedPrimaryColors + color
                        ColorTarget.SECONDARY -> selectedSecondaryColors = selectedSecondaryColors + color
                        ColorTarget.GLOW -> selectedGlowColors = selectedGlowColors + color
                        null -> {}
                    }
                }

                showColorPickerForNew = false
                showAddColorDialog = null

                newColorName = ""
                newColorHex = null
            }
        )
    }
}

private enum class ColorTarget { PRIMARY, SECONDARY, GLOW }