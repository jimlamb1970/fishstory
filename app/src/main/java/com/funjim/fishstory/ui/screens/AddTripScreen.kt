package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.AddFishermanDialog
import com.funjim.fishstory.ui.utils.AddSpeciesDialog
import com.funjim.fishstory.ui.utils.BodiesOfWaterRow
import com.funjim.fishstory.ui.utils.BodyOfWaterSelection
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.FishermanRow
import com.funjim.fishstory.ui.utils.FishermanSelection
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.TargetSpeciesRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.AddTripViewModel
import kotlinx.coroutines.launch
import kotlin.collections.minus
import kotlin.collections.plus

// ---------------------------------------------------------------------------
// AddTripScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    viewModel: AddTripViewModel,
    navigateToEditTackleBox: ((fishermanId: String, tackleBoxId: String) -> Unit),
    navigateToAddEvent: ((tripId: String) -> Unit),
    navigateBack: () -> Unit
) {
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tripDraft by viewModel.tripDraft.collectAsStateWithLifecycle()

    var showBodyOfWaterSelection by remember { mutableStateOf(false) }
    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
    val bodiesOfWater by viewModel.tripBodiesOfWater.collectAsStateWithLifecycle()
    var addNewBodyOfWater by remember { mutableStateOf(false) }

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    val targetSpecies by viewModel.tripTargetSpecies.collectAsStateWithLifecycle()
    var addNewSpecies by remember { mutableStateOf(false) }

    var showFishermanSelection by remember { mutableStateOf(false) }
    val tripFishermen by viewModel.tripFishermen.collectAsStateWithLifecycle()
    val allFishermen by viewModel.allFishermen.collectAsStateWithLifecycle()
    var addNewFisherman by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val tripTackleBoxMap by viewModel.tripTackleBoxMap.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }

    var showEventDialog by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = tripDraft.latitude,
        existingLng = tripDraft.longitude,
        onFetchLocation = {
            scope.launch { viewModel.fetchDeviceLocationOnce() }
        },
        onLocationConfirmed = { lat, lng ->
            viewModel.updateTripDraft { it.copy(latitude = lat, longitude = lng) }
        }
    )

    fun cancelAndExit() {
        navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("New Trip")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        cancelAndExit()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showEventDialog = true }, // Intercept save to present choices
                        enabled = tripDraft.name.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Trip"
                        )
                    }

                    val hasLocation = tripDraft.latitude != null
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }) {
                            if (hasLocationPermission) {
                                DropdownMenuItem(
                                    text = { Text("Use Current Location") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            null
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            viewModel.fetchLocation()?.let { loc ->
                                                viewModel.updateTripDraft {
                                                    it.copy(
                                                        latitude = loc.latitude,
                                                        longitude = loc.longitude
                                                    )
                                                }
                                            } ?: Toast.makeText(
                                                context,
                                                "Could not get location",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Select on Map") },
                                leadingIcon = {
                                    Icon(Icons.Default.Map, null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    locationPicker.openPicker()
                                }
                            )
                            if (hasLocation) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOff,
                                            null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.updateTripDraft {
                                            it.copy(
                                                latitude = null,
                                                longitude = null
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Trip Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    val displayLat = tripDraft.latitude
                    val displayLng = tripDraft.longitude
                    val hasLocation = displayLat != null && displayLng != null

                    if (hasLocation) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = getOnMainColor(),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val mapUri =
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${displayLat},${displayLng}")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Could not open map",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        )
                    }
                }

                OutlinedTextField(
                    value = tripDraft.name,
                    onValueChange = { name ->
                        viewModel.updateTripDraft { tripDraft.copy(name = name.trimStart()) }
                    },
                    label = { Text("Trip Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Start",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(48.dp)
                    )
                    DateTimePickerButton(
                        label = "start",
                        millis = tripDraft.startDate,
                        modifier = Modifier.weight(1f)
                    ) { new ->
                        viewModel.updateTripDraft {
                            tripDraft.copy(startDate = new)
                        }
                        if (new > tripDraft.endDate) {
                            viewModel.updateTripDraft {
                                tripDraft.copy(endDate = new)
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "End",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.width(48.dp)
                    )
                    DateTimePickerButton(
                        label = "end",
                        millis = tripDraft.endDate,
                        modifier = Modifier.weight(1f)
                    ) { new ->
                        when {
                            new < tripDraft.startDate ->
                                Toast.makeText(
                                    context,
                                    "End must be after start",
                                    Toast.LENGTH_SHORT
                                ).show()
                            else ->
                                viewModel.updateTripDraft {
                                    tripDraft.copy(endDate = new)
                                }
                        }
                    }
                }

                HorizontalDivider()
                TargetSpeciesRow(
                    items = targetSpecies,
                    onAdd = { showSpeciesSelection = true },
                    onDelete = { species ->
                        viewModel.updateTripTargetSpecies(targetSpecies - species)
                    },
                    thumbnailProvider = { species ->
                        val thumbnailFlow = remember(species.id) {
                            viewModel.speciesThumbnail(species.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.TargetFish,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()
                BodiesOfWaterRow(
                    items = bodiesOfWater,
                    onAdd = { showBodyOfWaterSelection = true },
                    onClick = {},
                    onDelete = { bodyOfWater ->
                        viewModel.updateTripBodiesOfWater(bodiesOfWater - bodyOfWater)
                    },
                    thumbnailProvider = { bodyOfWater ->
                        val thumbnailFlow = remember(bodyOfWater.id) {
                            viewModel.speciesThumbnail(bodyOfWater.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.BodyOfWater,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()
                FishermanRow(
                    items = tripFishermen,
                    onAdd = { showFishermanSelection = true },
                    onClick = {},
                    onDelete = { item ->
                        viewModel.updateTripFishermen(tripFishermen - item)
                        viewModel.updateTripTackleBoxMap(tripTackleBoxMap - item.id)
                    },
                    thumbnailProvider = { item ->
                        val thumbnailFlow = remember(item.id) {
                            viewModel.fishermanThumbnail(item.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.Fisherman,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showBodyOfWaterSelection) {
            BodyOfWaterSelection(
                items = allBodiesOfWater,
                selectedItems = bodiesOfWater,
                onSelected = { selected ->
                    viewModel.updateTripBodiesOfWater(bodiesOfWater + selected)
                },
                onUnselected = { unselected ->
                    viewModel.updateTripBodiesOfWater(bodiesOfWater - unselected)
                },
                onAdd = {
                    addNewBodyOfWater = true
                },
                onDone = { showBodyOfWaterSelection = false },
                modifier = Modifier.fillMaxWidth(),
                thumbnailProvider = { item ->
                    val thumbnailFlow = remember(item.id) {
                        viewModel.bodyOfWaterThumbnail(item.id)
                    }

                    val thumbnail by thumbnailFlow.collectAsState(initial = null)

                    ThumbnailBox(
                        thumbnail = thumbnail,
                        imageVector = AppIcons.Default.BodyOfWater,
                        modifier = Modifier.size(48.dp)
                    )
                }
            )
        }
        if (showFishermanSelection) {
            val sorted = allFishermen.sortedBy { it.fullName }
            FishermanSelection(
                items = sorted,
                selectedItems = tripFishermen,
                onSelected = { selected->
                    viewModel.updateTripFishermen(tripFishermen + selected)
                },
                onUnselected = { unselected ->
                    viewModel.updateTripFishermen(tripFishermen - unselected)
                },
                onAdd = {
                    addNewFisherman = true
                },
                onDone = { showFishermanSelection = false },
                modifier = Modifier.fillMaxWidth(),
                thumbnailProvider = { item ->
                    val thumbnailFlow = remember(item.id) {
                        viewModel.fishermanThumbnail(item.id)
                    }

                    val thumbnail by thumbnailFlow.collectAsState(initial = null)

                    ThumbnailBox(
                        thumbnail = thumbnail,
                        imageVector = AppIcons.Default.Fisherman,
                        modifier = Modifier.size(48.dp)
                    )
                }
            )
        }
        if (showSpeciesSelection) {
            SpeciesSelection(
                items = allSpecies,
                selectedItems = targetSpecies,
                onSelected = { selected ->
                    viewModel.updateTripTargetSpecies(targetSpecies + selected)
                },
                onUnselected = { unselected ->
                    viewModel.updateTripTargetSpecies(targetSpecies - unselected)
                },
                onAdd = {
                    addNewSpecies = true
                },
                onDone = { showSpeciesSelection = false },
                modifier = Modifier.fillMaxWidth(),
                thumbnailProvider = { species ->
                    val thumbnailFlow = remember(species.id) {
                        viewModel.speciesThumbnail(species.id)
                    }

                    val thumbnail by thumbnailFlow.collectAsState(initial = null)

                    ThumbnailBox(
                        thumbnail = thumbnail,
                        imageVector = AppIcons.Default.TargetFish,
                        modifier = Modifier.size(48.dp)
                    )
                }
            )
        }
    }

    if (addNewBodyOfWater) {
        AddSpeciesDialog(
            onDismiss = { addNewBodyOfWater = false },
            onConfirm = { name ->
                viewModel.updateTripBodiesOfWater(BodyOfWater(name = name))
                addNewBodyOfWater = false
            }
        )
    }
    if (addNewFisherman) {
        AddFishermanDialog(
            onDismiss = { addNewFisherman = false },
            onAdd = { firstName, lastName, nickname ->
                viewModel.updateTripFishermen(Fisherman(
                    firstName = firstName,
                    lastName = lastName,
                    nickname = nickname))
                addNewFisherman = false
            }
        )
    }
    if (addNewSpecies) {
        AddSpeciesDialog(
            onDismiss = { addNewSpecies = false },
            onConfirm = { name ->
                viewModel.updateTripTargetSpecies(Species(name = name))
                addNewSpecies = false
            }
        )
    }

    if (showEventDialog) {
        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            title = {
                Text(
                    text = "Save Trip",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "How would you like to add events for this trip?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 1: Ignore for now
                    OutlinedButton(
                        onClick = {
                            showEventDialog = false
                            viewModel.saveTrip()
                            navigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & don't add events")
                    }

                    // Option 2: Auto-create single event spanning full trip
                    Button(
                        onClick = {
                            showEventDialog = false
                            viewModel.saveTripWithEvent()
                            navigateBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & add full trip event")
                    }

                    // Option 3: Navigate to custom event creation screen
                    ElevatedButton(
                        onClick = {
                            showEventDialog = false
                            val tripId = tripDraft.id
                            viewModel.saveTrip()
                            navigateBack()
                            navigateToAddEvent(tripId)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & manually add event(s)")
                    }
                }
            },
            confirmButton = {}, // Actions are self-contained in the option buttons
            dismissButton = {
                TextButton(onClick = { showEventDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
