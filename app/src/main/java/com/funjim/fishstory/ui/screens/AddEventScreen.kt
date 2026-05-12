package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.TripViewModelCrewPickerBridge
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.EventWizardStep
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.util.*


// ---------------------------------------------------------------------------
// AddEventScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    tripViewModel: TripViewModel,
    tripId: String,
    navigateToEditTackleBox: ((fishermanId: String, tackleBoxId: String) -> Unit),
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tripSummary by tripViewModel.selectedTripSummary.collectAsStateWithLifecycle()

    val eventDraft by tripViewModel.eventDraft.collectAsStateWithLifecycle()

    // tripFishermen and eventFishermen are not really used. But they are being collected
    // because when selecting a trip and/or event, those flows have a side effect of updating
    // the tripFishermenIds and eventFishermenIds to reflect the trip and event selections
    val tripFishermen by tripViewModel.tripFishermen.collectAsStateWithLifecycle()
    val tripFishermenIds by tripViewModel.tripFishermenIds.collectAsStateWithLifecycle()
    val eventFishermen by tripViewModel.eventFishermen.collectAsStateWithLifecycle()
    val eventFishermenIds by tripViewModel.eventFishermenIds.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        tripViewModel.selectTrip(tripId)
        tripViewModel.selectEvent(eventDraft.id)
    }

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val eventTackleBoxMap by tripViewModel.eventTackleBoxMap.collectAsState()

    LaunchedEffect(tripSummary) {
        tripSummary?.let { tripSummary ->
            tripViewModel.updateEventDraft {
                it.copy(
                    name = "",
                    tripId = tripSummary.trip.id,
                    startTime = tripSummary.trip.startDate,
                    endTime = tripSummary.trip.endDate,
                    latitude = null,
                    longitude = null
                )
            }
        }
    }

    // ── Wizard step ─────────────────────────────────────────────────────────
    val currentStep by tripViewModel.currentEventWizardStep.collectAsStateWithLifecycle()

    var locationMenuExpanded by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by tripViewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = eventDraft.latitude,
        existingLng = eventDraft.longitude,
        onFetchLocation = {
            scope.launch { tripViewModel.fetchDeviceLocationOnce() }
        },
        onLocationConfirmed = { lat, lng ->
            tripViewModel.updateEventDraft { it.copy(latitude = lat, longitude = lng) }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value }) {
            scope.launch {
                tripViewModel.fetchLocation()?.let { loc ->
                    if (currentStep == EventWizardStep.EventInfo) {
                        tripViewModel.updateEventDraft {
                            it.copy(
                                latitude = loc.latitude,
                                longitude = loc.longitude)
                        }
                    }
                } ?: Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper: delete the evert if user cancels mid-wizard
    fun cancelAndExit() {
        scope.launch {
            // CASCADE deletes will clean up fisherman cross-refs and event
            tripViewModel.deleteEventById(eventDraft.id)
        }
        tripViewModel.clearTrip()
        tripViewModel.clearTripDraft()
        tripViewModel.clearEvent()
        tripViewModel.clearEventDraft()
        navigateBack()
    }

    // Progress indicator
    val stepLabels = listOf("Event", "Event Crew & Boxes")
    val stepIndex = currentStep.ordinal.coerceAtMost(stepLabels.lastIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("New Event")
                        Text(
                            text = "Step ${stepIndex + 1} of ${stepLabels.size}: ${stepLabels[stepIndex]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            EventWizardStep.EventInfo    -> cancelAndExit()
                            EventWizardStep.EventCrew -> tripViewModel.updateEventWizardStep(EventWizardStep.EventInfo)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            DropdownMenu(expanded = locationMenuExpanded, onDismissRequest = { locationMenuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Use Current Location") },
                                    leadingIcon = { Icon(
                                        Icons.Default.MyLocation,
                                        null)
                                    },
                                    onClick = {
                                        locationMenuExpanded = false
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            scope.launch {
                                                tripViewModel.fetchLocation()?.let { loc ->
                                                    tripViewModel.updateEventDraft {
                                                        it.copy(
                                                            latitude = loc.latitude,
                                                            longitude = loc.longitude
                                                        )
                                                    }
                                                } ?: Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                        }
                                    }
                                )
                                if (tripSummary?.trip?.latitude != null) {
                                    DropdownMenuItem(
                                        text = { Text("Use Trip Location") },
                                        leadingIcon = {
                                            Icon(Icons.Default.LocationOn, null)
                                        },
                                        onClick = {
                                            locationMenuExpanded = false
                                            tripViewModel.updateEventDraft {
                                                it.copy(
                                                    latitude = tripSummary?.trip?.latitude,
                                                    longitude = tripSummary?.trip?.longitude
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
                                            tripViewModel.updateEventDraft {
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
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {

            LinearProgressIndicator(
                progress = { (stepIndex + 1f) / stepLabels.size },
                modifier = Modifier.fillMaxWidth()
            )
            tripSummary?.let { tripSummary ->
                when (currentStep) {
                    // ── Step 3: Event info ─────────────────────────────────────
                    EventWizardStep.EventInfo -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Event Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "An event is a single fishing session — e.g. morning run, afternoon drift.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = eventDraft.name,
                                onValueChange = { name ->
                                    tripViewModel.updateEventDraft { eventDraft.copy(name = name) }
                                },
                                label = { Text("Event Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                                DateTimePickerButton(label = "start", millis = eventDraft.startTime, modifier = Modifier.weight(1f)) { new ->
                                    when {
                                        new < tripSummary.trip.startDate ->
                                            Toast.makeText(context, "Cannot be before trip start", Toast.LENGTH_SHORT).show()
                                        new > tripSummary.trip.endDate ->
                                            Toast.makeText(context, "Cannot be after trip end", Toast.LENGTH_SHORT).show()
                                        else -> {
                                            tripViewModel.updateEventDraft {
                                                eventDraft.copy(startTime = new)
                                            }
                                            if (new > eventDraft.endTime) {
                                                tripViewModel.updateEventDraft {
                                                    eventDraft.copy(endTime = new)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                                DateTimePickerButton(label = "end", millis = eventDraft.endTime, modifier = Modifier.weight(1f)) { new ->
                                    when {
                                        new < eventDraft.startTime ->
                                            Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                        new > tripSummary.trip.endDate ->
                                            Toast.makeText(context, "Cannot be after trip end", Toast.LENGTH_SHORT).show()
                                        else ->
                                            tripViewModel.updateEventDraft {
                                                eventDraft.copy(endTime = new)
                                            }
                                    }
                                }
                            }

                            if (eventDraft.latitude != null) {
                                LocationSetRow()
                            }

                            Spacer(Modifier.weight(1f))

                            Button(
                                onClick = {
                                    scope.launch {
                                        // Add the new event
                                        tripViewModel.upsertEvent(eventDraft)

                                        if (eventFishermenIds.isEmpty()) {
                                            tripViewModel.updateEventFishermanIds(tripFishermenIds)
                                            tripFishermenIds.forEach { fishermanId ->
                                                tripViewModel.upsertEventFishermanCrossRef(
                                                    eventId = eventDraft.id,
                                                    fishermanId = fishermanId,
                                                    tackleBoxId = tripTackleBoxMap[fishermanId]
                                                )
                                            }
                                        }
                                    }

                                    tripViewModel.updateEventWizardStep(EventWizardStep.EventCrew)
                                },
                                // TODO -- add extra check for dates
                                enabled = eventDraft.name.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Next: Select Event Crew")
                                Icon(Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    // ── Step 4: Event crew + tackle boxes ──────────────────────
                    EventWizardStep.EventCrew -> {
                        Spacer(Modifier.height(16.dp))
                        TripViewModelCrewPickerBridge(
                            title = "Event Crew & Tackle Boxes",
                            subtitle = "Who's fishing \"${eventDraft.name}\"? Tackle boxes default to trip selections.",
                            eligibleFishermen = tripFishermen,
                            selectedIds = eventFishermenIds,
                            tackleBoxSelections = eventTackleBoxMap,
                            onSelectionChanged = { fishermanId, selected ->
                                tripViewModel.toggleEventFisherman(fishermanId)
                                if (selected) {
                                    tripViewModel.upsertEventFishermanCrossRef(
                                        eventId = eventDraft.id,
                                        fishermanId = fishermanId,
                                        tackleBoxId = eventTackleBoxMap[fishermanId]
                                    )
                                } else {
                                    tripViewModel.deleteEventFishermanCrossRef(
                                        eventId = eventDraft.id,
                                        fishermanId = fishermanId
                                    )
                                }
                            },
                            onTackleBoxChanged = { fishermanId, boxId ->
                                tripViewModel.upsertEventFishermanCrossRef(
                                    eventId = eventDraft.id,
                                    fishermanId = fishermanId,
                                    tackleBoxId = boxId
                                )
                            },
                            navigateToEditTackleBox = navigateToEditTackleBox,
                            tripViewModel = tripViewModel,
                            confirmLabel = "Done",
                            onConfirm = {
                                tripViewModel.clearTrip()
                                tripViewModel.clearTripDraft()
                                tripViewModel.clearEvent()
                                tripViewModel.clearEventDraft()
                                navigateBack()
                            },
                            onAddTackleBox = { tackleBoxName, fishermanId ->
                                scope.launch { tripViewModel.createAndAssignEventTackleBox(
                                    fishermanId = fishermanId,
                                    eventId = eventDraft.id,
                                    name = tackleBoxName
                                ) }
                            }
                        )
                    }
                }
            } ?: run {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Small shared composables
// ---------------------------------------------------------------------------

@Composable
private fun LocationSetRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text("Location set", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
    }
}
