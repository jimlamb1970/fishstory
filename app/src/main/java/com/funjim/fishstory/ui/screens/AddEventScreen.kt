package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.BodiesOfWaterRow
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.FishermanRow
import com.funjim.fishstory.ui.utils.TargetSpeciesRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.crud.AddFishermanDialog
import com.funjim.fishstory.ui.utils.crud.AddSpeciesDialog
import com.funjim.fishstory.ui.utils.crud.AddTackleBoxDialog
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnVariantColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.ui.utils.selection.BodyOfWaterSelection
import com.funjim.fishstory.ui.utils.selection.FishermanSelection
import com.funjim.fishstory.ui.utils.selection.SpeciesSelection
import com.funjim.fishstory.ui.utils.selection.TackleBoxBottomSheet
import com.funjim.fishstory.viewmodels.AddEventUiState
import com.funjim.fishstory.viewmodels.AddEventViewModel
import kotlinx.coroutines.launch
import kotlin.collections.plus
import androidx.core.net.toUri

// ---------------------------------------------------------------------------
// AddEventScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    viewModel: AddEventViewModel,
    tripId: String,
    navigateToEditTackleBox: ((fishermanId: String, tackleBoxId: String) -> Unit),
    navigateBack: () -> Unit
) {
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val uiState by viewModel.uiAddEventState.collectAsStateWithLifecycle()

    val eventDraft by viewModel.eventDraft.collectAsStateWithLifecycle()

    var showBodyOfWaterSelection by remember { mutableStateOf(false) }
    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
    val bodiesOfWater by viewModel.eventBodiesOfWater.collectAsStateWithLifecycle()
    var addNewBodyOfWater by remember { mutableStateOf(false) }

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    val targetSpecies by viewModel.eventTargetSpecies.collectAsStateWithLifecycle()
    var addNewSpecies by remember { mutableStateOf(false) }

    var showFishermanSelection by remember { mutableStateOf(false) }
    val eventFishermen by viewModel.eventFishermen.collectAsStateWithLifecycle()
    val allFishermen by viewModel.allFishermen.collectAsStateWithLifecycle()
    var addNewFisherman by remember { mutableStateOf(false) }

    var showTackleBoxSelection by remember { mutableStateOf(false) }
    var addNewTackleBox by remember { mutableStateOf(false) }
    var selectedFisherman by remember { mutableStateOf<Fisherman?>(null) }

    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val eventTackleBoxMap by viewModel.eventTackleBoxMap.collectAsState()

    var isDraftInitialized by rememberSaveable { mutableStateOf(false) }

    var menuExpanded by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = eventDraft.latitude,
        existingLng = eventDraft.longitude,
        onFetchLocation = {
            scope.launch { viewModel.fetchDeviceLocationOnce() }
        },
        onLocationConfirmed = { lat, lng ->
            viewModel.updateEventDraft { it.copy(latitude = lat, longitude = lng) }
        }
    )

    fun cancelAndExit() {
        navigateBack()
    }

    when (val state = uiState) {
        is AddEventUiState.Loading -> {
            // Keeps the screen entirely blank or showing a spinner
            // until all 3 database points arrive
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AddEventUiState.Success -> {
            val trip = state.trip

            if (!isDraftInitialized) {
                viewModel.updateEventDraft {
                    it.copy(
                        name = "",
                        tripId = trip.trip.id,
                        startTime = trip.trip.startDate,
                        endTime = trip.trip.endDate,
                        latitude = null,
                        longitude = null
                    )
                }

                viewModel.updateEventFishermen(trip.fishermen)
                viewModel.updateEventBodiesOfWater(trip.bodiesOfWater)
                viewModel.updateEventTackleBoxMap(state.tripTackleBoxMap)
                viewModel.updateEventTargetSpecies(trip.targetSpecies)

                isDraftInitialized = true
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("New Event")
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
                                onClick = {
                                    showSaveDialog = true
                                },
                                enabled = eventDraft.name.isNotBlank() && eventFishermen.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = AppIcons.Default.Save,
                                    contentDescription = "Save Event",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            val hasLocation = eventDraft.latitude != null
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
                                                        viewModel.updateEventDraft {
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
                                                viewModel.updateEventDraft {
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
                                onClick = { showSaveDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = eventDraft.name.isNotBlank() && eventFishermen.isNotEmpty()
                            ) {
                                Text("Add Event")
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Event Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            val displayLat = eventDraft.latitude ?: trip.trip.latitude
                            val displayLng = eventDraft.longitude ?: trip.trip.longitude
                            val hasAnyLocation = displayLat != null && displayLng != null

                            if (hasAnyLocation) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = getOnMainColor(),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val mapUri =
                                                "https://www.google.com/maps/search/?api=1&query=${displayLat},${displayLng}".toUri()
                                            val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                            try {
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "Could not open map",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        .alpha(if (eventDraft.latitude == null) 0.6f else 1f)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "An event is a single fishing session — e.g. morning run, afternoon drift.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getOnVariantColor()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = eventDraft.name,
                            onValueChange = { name ->
                                viewModel.updateEventDraft { eventDraft.copy(name = name.trimStart()) }
                            },
                            label = { Text("Event Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
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
                                millis = eventDraft.startTime,
                                modifier = Modifier.weight(1f)
                            ) { new ->
                                when {
                                    new < trip.trip.startDate ->
                                        Toast.makeText(
                                            context,
                                            "Cannot be before trip start",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    new > trip.trip.endDate ->
                                        Toast.makeText(
                                            context,
                                            "Cannot be after trip end",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    else -> {
                                        viewModel.updateEventDraft {
                                            eventDraft.copy(startTime = new)
                                        }
                                        if (new > eventDraft.endTime) {
                                            viewModel.updateEventDraft {
                                                eventDraft.copy(endTime = new)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
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
                                millis = eventDraft.endTime,
                                modifier = Modifier.weight(1f)
                            ) { new ->
                                when {
                                    new < eventDraft.startTime ->
                                        Toast.makeText(
                                            context,
                                            "End must be after start",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    new > trip.trip.endDate ->
                                        Toast.makeText(
                                            context,
                                            "Cannot be after trip end",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    else ->
                                        viewModel.updateEventDraft {
                                            eventDraft.copy(endTime = new)
                                        }
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                    }

                    item {
                        TargetSpeciesRow(
                            items = targetSpecies,
                            description = """No target species selected.

Target species are what you're actively fishing for. 

Selecting them places those species at the top of the list when logging catches for quicker entry.""",
                            onAdd = { showSpeciesSelection = true },
                            onDelete = { species ->
                                viewModel.updateEventTargetSpecies(targetSpecies - species)
                            },
                            thumbnailProvider = { species, size ->
                                val thumbnailFlow = remember(species.id) {
                                    viewModel.speciesThumbnail(species.id)
                                }

                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.TargetFish,
                                    modifier = Modifier.size(size)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        HorizontalDivider()
                    }

                    item {
                        BodiesOfWaterRow(
                            items = bodiesOfWater,
                            description = """No bodies of water selected.

Bodies of water are where you're actively fishing. 

Selecting them places those bodies of water at the top of the list when logging catches for quicker entry.""",
                            onAdd = { showBodyOfWaterSelection = true },
                            onClick = {},
                            onDelete = { bodyOfWater ->
                                viewModel.updateEventBodiesOfWater(bodiesOfWater - bodyOfWater)
                            },
                            thumbnailProvider = { bodyOfWater ->
                                val thumbnailFlow = remember(bodyOfWater.id) {
                                    viewModel.bodyOfWaterThumbnail(bodyOfWater.id)
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
                    }

                    item {
                        HorizontalDivider()
                    }

                    item {
                        FishermanRow(
                            items = eventFishermen,
                            description = """No fishermen selected.

Fishermen are the people actively participating on this event, and there must be at least one selected.

After selecting fishermen, you can click on a fisherman to assign a tackle box.  Adding lures to the tackle box is not allowed at this time.  You will be allowed to add lures to the tackle box later.

When logging catches, options will be restricted to the fishermen selected here.""",
                            onAdd = { showFishermanSelection = true },
                            onClick = { item ->
                                selectedFisherman = item
                                showTackleBoxSelection = true
                            },
                            onDelete = { item ->
                                viewModel.updateEventFishermen(eventFishermen - item)
                                viewModel.updateEventTackleBoxMap(eventTackleBoxMap - item.id)
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
                            tackleBoxProvider = { item ->
                                if (eventTackleBoxMap.containsKey(item.id)) {
                                    ThumbnailBox(
                                        thumbnail = null,
                                        imageVector = AppIcons.Default.TackleBoxClosed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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
                            viewModel.updateEventBodiesOfWater(bodiesOfWater + selected)
                        },
                        onUnselected = { unselected ->
                            viewModel.updateEventBodiesOfWater(bodiesOfWater - unselected)
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
                        selectedItems = eventFishermen,
                        onSelected = { selected->
                            viewModel.updateEventFishermen(eventFishermen + selected)
                        },
                        onUnselected = { unselected ->
                            viewModel.updateEventFishermen(eventFishermen - unselected)
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
                        onSelected = { selectedSpecies ->
                            viewModel.updateEventTargetSpecies(targetSpecies + selectedSpecies)
                        },
                        onUnselected = { selectedSpecies ->
                            viewModel.updateEventTargetSpecies(targetSpecies - selectedSpecies)
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
        }
    }

    if (addNewBodyOfWater) {
        AddSpeciesDialog(
            onDismiss = { addNewBodyOfWater = false },
            onConfirm = { name ->
                viewModel.updateEventBodiesOfWater(BodyOfWater(name = name))
                addNewBodyOfWater = false
            }
        )
    }
    if (addNewFisherman) {
        AddFishermanDialog(
            onDismiss = { addNewFisherman = false },
            onAdd = { firstName, lastName, nickname ->
                viewModel.updateEventFishermen(Fisherman(firstName = firstName, lastName = lastName, nickname = nickname))
                addNewFisherman = false
            }
        )
    }
    if (addNewSpecies) {
        AddSpeciesDialog(
            onDismiss = { addNewSpecies = false },
            onConfirm = { name ->
                viewModel.updateEventTargetSpecies(Species(name = name))
                addNewSpecies = false
            }
        )
    }

    if (showSaveDialog) {
        // Track whether the user wants to add another event after saving
        var addAnotherEvent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Event",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The event will be saved.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { addAnotherEvent = !addAnotherEvent }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = addAnotherEvent,
                            onCheckedChange = { addAnotherEvent = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add another event after saving",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        viewModel.saveEvent()
                        if (!addAnotherEvent) {
                            navigateBack()
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedFisherman?.let { selectedFisherman ->
        val availableBoxes = viewModel.getTackleBoxesForFisherman(selectedFisherman.id)
            .collectAsState(initial = emptyList()).value

        if (addNewTackleBox) {
            AddTackleBoxDialog(
                fishermanId = selectedFisherman.id,
                onDismiss = { addNewTackleBox = false },
                onConfirm = { tackleBox ->
                    viewModel.createTackleBox(tackleBox) { createdBox ->
                        viewModel.updateEventTackleBoxMap(
                            eventTackleBoxMap + (selectedFisherman.id to createdBox.id)
                        )
                    }
                    addNewTackleBox = false
                }
            )
        }

        TackleBoxBottomSheet(
            showSheet = showTackleBoxSelection,
            items = availableBoxes,
            selectedItem = availableBoxes.find {
                it.id == eventTackleBoxMap[selectedFisherman.id]
            },
            onSelected = { tackleBox ->
                viewModel.updateEventTackleBoxMap(
                    eventTackleBoxMap + (selectedFisherman.id to tackleBox.id)
                )
            },
            onAdd = { addNewTackleBox = true },
            onClear = {
                viewModel.updateEventTackleBoxMap(eventTackleBoxMap - selectedFisherman.id)
            },
            onDismissRequest = { showTackleBoxSelection = false }
        )
    }
}