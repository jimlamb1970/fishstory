package com.funjim.fishstory.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.EventViewModelCrewPickerBridge
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.TargetSpeciesRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.getOnVariantColor
import com.funjim.fishstory.ui.utils.getVariantColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.AddEventUiState
import com.funjim.fishstory.viewmodels.EventWizardStep
import com.funjim.fishstory.viewmodels.AddEventViewModel
import kotlinx.coroutines.launch


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

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    val targetSpecies by viewModel.eventTargetSpecies.collectAsStateWithLifecycle()
    var addNewSpecies by remember { mutableStateOf(false) }

    // tripFishermen and eventFishermen are not really used. But they are being collected
    // because when selecting a trip and/or event, those flows have a side effect of updating
    // the tripFishermenIds and eventFishermenIds to reflect the trip and event selections
    val tripFishermenIds by viewModel.tripFishermenIds.collectAsStateWithLifecycle()
    val eventFishermen by viewModel.eventFishermen.collectAsStateWithLifecycle()
    val eventFishermenIds by viewModel.eventFishermenIds.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
        viewModel.selectEvent(eventDraft.id)
    }

    val tripTackleBoxMap by viewModel.tripTackleBoxMap.collectAsState()
    val eventTackleBoxMap by viewModel.eventTackleBoxMap.collectAsState()

    var isDraftInitialized by rememberSaveable { mutableStateOf(false) }

    // ── Wizard step ─────────────────────────────────────────────────────────
    val currentStep by viewModel.currentEventWizardStep.collectAsStateWithLifecycle()
    val overrideTripCrew by viewModel.overrideTripCrew.collectAsStateWithLifecycle()

    var locationMenuExpanded by remember { mutableStateOf(false) }

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

    // Helper: delete the event if user cancels mid-wizard
    fun cancelAndExit() {
        scope.launch {
            // CASCADE deletes will clean up fisherman and species references
            viewModel.deleteEventById(eventDraft.id)
        }

        viewModel.clearTrip()
        viewModel.clearEvent()
        viewModel.clearEventDraft()

        navigateBack()
    }

    // Progress indicator
    val stepLabels = remember(overrideTripCrew) {
        if (overrideTripCrew) {
            listOf("Event", "Event Crew & Boxes")
        } else {
            listOf("Event")
        }
    }

    val stepIndex = currentStep.ordinal.coerceAtMost(stepLabels.lastIndex)

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
                viewModel.updateEventTargetSpecies(trip.targetSpecies)
                isDraftInitialized = true
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("New Event")
                                if (stepLabels.size > 1) {
                                    Text(
                                        text = "Step ${stepIndex + 1} of ${stepLabels.size}: ${stepLabels[stepIndex]}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
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
                                when (currentStep) {
                                    EventWizardStep.EventInfo ->
                                        cancelAndExit()

                                    EventWizardStep.EventCrew ->
                                        viewModel.updateEventWizardStep(EventWizardStep.EventInfo)
                                }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            if (currentStep == EventWizardStep.EventInfo) {
                                val hasLocation = eventDraft.latitude != null
                                Box {
                                    IconButton(onClick = { locationMenuExpanded = true }) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = "Location",
                                            tint = if (hasLocation) Color(0xFF4CAF50) else LocalContentColor.current
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = locationMenuExpanded,
                                        onDismissRequest = { locationMenuExpanded = false }) {
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
                                                    locationMenuExpanded = false
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
                                        if (trip.trip.latitude != null) {
                                            DropdownMenuItem(
                                                text = { Text("Use Trip Location") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.LocationOn, null)
                                                },
                                                onClick = {
                                                    locationMenuExpanded = false
                                                    viewModel.updateEventDraft {
                                                        it.copy(
                                                            latitude = trip.trip.latitude,
                                                            longitude = trip.trip.longitude
                                                        )
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
                                                locationMenuExpanded = false
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
                                                    locationMenuExpanded = false
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
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize()
                ) {
                    LinearProgressIndicator(
                        progress = { (stepIndex + 1f) / stepLabels.size },
                        modifier = Modifier.fillMaxWidth()
                    )
                    when (currentStep) {
                        // ── Step 1: Event info ─────────────────────────────────────
                        EventWizardStep.EventInfo -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Event Details",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "An event is a single fishing session — e.g. morning run, afternoon drift.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = getOnVariantColor()
                                )

                                OutlinedTextField(
                                    value = eventDraft.name,
                                    onValueChange = { name ->
                                        viewModel.updateEventDraft { eventDraft.copy(name = name) }
                                    },
                                    label = { Text("Event Name") },
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

                                if (eventDraft.latitude != null) {
                                    LocationSetRow()
                                }

                                HorizontalDivider()

                                TargetSpeciesRow(
                                    items = targetSpecies,
                                    onAdd = { showSpeciesSelection = true },
                                    onDelete = { species ->
                                        viewModel.updateEventTargetSpecies(targetSpecies - species)
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

                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = getVariantColor().copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateOverrideTripCrew(!overrideTripCrew)
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = overrideTripCrew,
                                            onCheckedChange = { viewModel.updateOverrideTripCrew(it) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Specify Event Crew",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Customize who is fishing this event. If unchecked, the Trip Crew will be used.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = getOnVariantColor()
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.weight(1f))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            // Add the new event
                                            viewModel.upsertEvent(eventDraft)
                                            viewModel.persistTargetSpecies()

                                            if (overrideTripCrew) {
                                                if (eventFishermenIds.isEmpty()) {
                                                    viewModel.updateEventFishermanIds(
                                                        tripFishermenIds
                                                    )
                                                    tripFishermenIds.forEach { fishermanId ->
                                                        viewModel.upsertEventFishermanCrossRef(
                                                            eventId = eventDraft.id,
                                                            fishermanId = fishermanId,
                                                            tackleBoxId = tripTackleBoxMap[fishermanId]
                                                        )
                                                    }
                                                }
                                                viewModel.updateEventWizardStep(EventWizardStep.EventCrew)
                                            } else {
                                                eventFishermenIds.forEach { fishermanId ->
                                                    viewModel.deleteEventFishermanCrossRef(
                                                        eventId = eventDraft.id,
                                                        fishermanId = fishermanId
                                                    )
                                                }

                                                viewModel.clearTrip()
                                                viewModel.clearEvent()
                                                viewModel.clearEventDraft()
                                                navigateBack()
                                            }
                                        }
                                    },
                                    enabled = eventDraft.name.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (overrideTripCrew) "Next: Select Event Crew"
                                        else "Done"
                                    )
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        null,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        // ── Step 2: Event crew + tackle boxes ──────────────────────
                        EventWizardStep.EventCrew -> {
                            Spacer(Modifier.height(16.dp))
                            EventViewModelCrewPickerBridge(
                                title = "Event Crew & Tackle Boxes",
                                subtitle = """Who's fishing "${eventDraft.name}"? Tackle boxes default to trip selections.
                            |
                            |If no one is selected, the trip crew and tackle box assignments will be used."""
                                    .trimMargin(),
                                eligibleFishermen = trip.fishermen,
                                selectedIds = eventFishermenIds,
                                tackleBoxSelections = eventTackleBoxMap,
                                getTackleBoxesForFisherman = { fishermanId ->
                                    viewModel.getTackleBoxesForFisherman(fishermanId)
                                        .collectAsState(initial = emptyList()).value
                                },
                                getLureCount = { tackleBoxId ->
                                    viewModel.getLureCountForTackleBox(tackleBoxId)
                                        .collectAsState(initial = 0).value
                                },
                                getLuresInTacklebox = { tackleBoxId ->
                                    viewModel.getLuresInTackleBox(tackleBoxId).collectAsState(initial = emptyList()).value
                                },
                                onSelectionChanged = { fishermanId, selected ->
                                    viewModel.toggleEventFisherman(fishermanId)
                                    if (selected) {
                                        viewModel.upsertEventFishermanCrossRef(
                                            eventId = eventDraft.id,
                                            fishermanId = fishermanId,
                                            tackleBoxId = eventTackleBoxMap[fishermanId]
                                        )
                                    } else {
                                        viewModel.deleteEventFishermanCrossRef(
                                            eventId = eventDraft.id,
                                            fishermanId = fishermanId
                                        )
                                    }
                                },
                                onTackleBoxChanged = { fishermanId, boxId ->
                                    viewModel.upsertEventFishermanCrossRef(
                                        eventId = eventDraft.id,
                                        fishermanId = fishermanId,
                                        tackleBoxId = boxId
                                    )
                                },
                                navigateToEditTackleBox = navigateToEditTackleBox,
                                confirmLabel = "Done",
                                onConfirm = {
                                    viewModel.clearTrip()
                                    viewModel.clearEvent()
                                    viewModel.clearEventDraft()
                                    navigateBack()
                                },
                                onAddTackleBox = { tackleBoxName, fishermanId ->
                                    viewModel.createAndAssignEventTackleBox(
                                        fishermanId = fishermanId,
                                        eventId = eventDraft.id,
                                        name = tackleBoxName
                                    )
                                }
                            )
                        }
                    }
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

    if (addNewSpecies) {
        var addSpeciesName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { addNewSpecies = false },
            title = { Text("Add New Species") },
            text = {
                TextField(
                    value = addSpeciesName,
                    onValueChange = { addSpeciesName = it },
                    placeholder = { Text("Species Name (e.g. Walleye)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (addSpeciesName.isNotBlank()) {
                        scope.launch {
                            val species = Species(name = addSpeciesName)
                            viewModel.addSpecies(species)
                            viewModel.updateEventTargetSpecies(targetSpecies + species)
                            addNewSpecies = false
                            addSpeciesName = ""
                        }
                    }
                }) { Text("Add Species") }
            },
            dismissButton = {
                TextButton(onClick = { addNewSpecies = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Small shared composables
// ---------------------------------------------------------------------------

// TODO - get rid of this
@Composable
private fun LocationSetRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text("Location set", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
    }
}
