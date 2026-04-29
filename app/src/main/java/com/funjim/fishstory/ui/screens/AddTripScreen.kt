package com.funjim.fishstory.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.EventItem
import com.funjim.fishstory.ui.utils.TripViewModelCrewPickerBridge
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItem
import com.funjim.fishstory.ui.utils.TripMenu
import com.funjim.fishstory.ui.utils.hasLocationPermission
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.TripViewModel
import com.funjim.fishstory.viewmodels.WizardStep
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------------------------------------
// AddTripScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    tripViewModel: TripViewModel,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // All fishermen from DB — used for crew selection
    val allFishermen by tripViewModel.fishermen.collectAsState(initial = emptyList())
    val sortedFishermen = remember(allFishermen) { allFishermen.sortedBy { it.fullName } }

    // TODO -- can tripDraft and eventDraft be replaced by tripSummary and eventSummary?
    val tripDraft by tripViewModel.tripDraft.collectAsStateWithLifecycle()
    val eventDraft by tripViewModel.eventDraft.collectAsStateWithLifecycle()

    // tripFishermen and eventFishermen are not really used. But they are being collected
    // because when selecting a trip and/or event, those flows have a side effect of updating
    // the tripFishermenIds and eventFishermenIds to reflect the trip and event selections
    val tripFishermen by tripViewModel.tripFishermen.collectAsStateWithLifecycle()
    val tripFishermenIds by tripViewModel.tripFishermenIds.collectAsStateWithLifecycle()
    val eventFishermen by tripViewModel.eventFishermen.collectAsStateWithLifecycle()
    val eventFishermenIds by tripViewModel.eventFishermenIds.collectAsStateWithLifecycle()

    LaunchedEffect(tripDraft.id) {
        tripViewModel.selectTrip(tripDraft.id)
        tripViewModel.selectEvent(eventDraft.id)
    }

    val tripSummary by tripViewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val eventSummaries by tripViewModel.eventSummaries.collectAsStateWithLifecycle()
    val eventSummary by tripViewModel.selectedEventSummary.collectAsStateWithLifecycle()

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val eventTackleBoxMap by tripViewModel.eventTackleBoxMap.collectAsState()

    // ── Wizard step ─────────────────────────────────────────────────────────
    var currentStep by remember { mutableStateOf(WizardStep.TripInfo) }
    var isFirstEvent by remember { mutableStateOf(true) }
    var fromReview by remember { mutableStateOf(false) }

    var showTripMenu by remember { mutableStateOf(false) }

    var locationMenuExpanded by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by tripViewModel.deviceLocation.collectAsStateWithLifecycle()

    val tripLocationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = tripDraft.latitude,
        existingLng = tripDraft.longitude,
        onFetchLocation = { tripViewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            tripViewModel.updateTripDraft { it.copy(latitude = lat, longitude = lng) }
        }
    )

    val eventLocationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = eventDraft.latitude,
        existingLng = eventDraft.longitude,
        onFetchLocation = { tripViewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            tripViewModel.updateEventDraft { it.copy(latitude = lat, longitude = lng) }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value }) {
            scope.launch {
                tripViewModel.getCurrentLocation(context)?.let { loc ->
                    if (currentStep == WizardStep.TripInfo) {
                        tripViewModel.updateTripDraft {
                            it.copy(
                                latitude = loc.latitude,
                                longitude = loc.longitude)
                        }
                    } else if (currentStep == WizardStep.EventInfo) {
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

    // Helper: delete the trip row if user cancels mid-wizard
    fun cancelAndExit() {
        // CASCADE deletes will clean up fisherman cross-refs and events
        tripViewModel.deleteTripById(tripDraft.id)
        tripViewModel.clearTrip()
        tripViewModel.clearTripDraft()
        tripViewModel.clearEvent()
        tripViewModel.clearEventDraft()
        navigateBack()
    }

    // Progress indicator
    val stepLabels = listOf("Trip", "Crew & Boxes", "Event", "Seg. Crew & Boxes", "Review")
    val stepIndex = currentStep.ordinal.coerceAtMost(stepLabels.lastIndex)

    val onTripAction: (TripAction) -> Unit = { action ->
        when (action) {
            is TripAction.View -> {}
            is TripAction.Menu -> {
                showTripMenu = true
            }
            is TripAction.OpenMap -> {}
            is TripAction.UseCurrentLocation -> {
                showTripMenu = false
                if (hasLocationPermission(context)) {
                    scope.launch {
                        @SuppressLint("MissingPermission")
                        val location = tripViewModel.getCurrentLocation(context)
                        if (location != null) {
                            tripViewModel.updateTripDraft {
                                it.copy(latitude = location.latitude, longitude = location.longitude)
                            }
                            tripViewModel.saveTrip(tripDraft)
                            Toast.makeText(
                                context,
                                "Location updated",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Could not get location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
            is TripAction.SelectLocation -> {
                showTripMenu = false
                tripLocationPicker.openPicker()
                scope.launch { tripViewModel.saveTrip(tripDraft) }
            }
            is TripAction.ClearLocation -> {
                showTripMenu = false
                tripViewModel.updateTripDraft { it.copy(latitude = null, longitude = null) }

                scope.launch {
                    tripViewModel.saveTrip(tripDraft)
                    Toast.makeText(context, "Location cleared", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            is TripAction.Delete -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("New Trip")
                        Text(
                            text = "Step ${stepIndex + 1} of ${stepLabels.size}: ${stepLabels[stepIndex]}",
                            style = MaterialTheme.typography.bodySmall
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
                            WizardStep.TripInfo    -> cancelAndExit()
                            WizardStep.TripCrew    -> currentStep = WizardStep.TripInfo
                            WizardStep.EventInfo -> {
                                // Logic depends on whether this is the start of the trip or a later addition
                                currentStep = if (isFirstEvent) {
                                    WizardStep.TripCrew
                                } else {
                                    WizardStep.Review
                                }
                            }
                            WizardStep.EventCrew -> currentStep = WizardStep.EventInfo
                            WizardStep.Review    -> currentStep = WizardStep.EventCrew
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentStep == WizardStep.TripInfo || currentStep == WizardStep.EventInfo) {
                        val onTripStep = currentStep == WizardStep.TripInfo
                        val hasLocation = if (onTripStep) tripDraft.latitude != null else eventDraft.latitude != null
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
                                    leadingIcon = { Icon(Icons.Default.MyLocation, null) },
                                    onClick = {
                                        locationMenuExpanded = false
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            scope.launch {
                                                tripViewModel.getCurrentLocation(context)?.let { loc ->
                                                    if (onTripStep) {
                                                        tripViewModel.updateTripDraft {
                                                            it.copy(
                                                                latitude = loc.latitude,
                                                                longitude = loc.longitude
                                                            )
                                                        }
                                                    }
                                                    else {
                                                        tripViewModel.updateEventDraft {
                                                            it.copy(
                                                                latitude = loc.latitude,
                                                                longitude = loc.longitude
                                                            )
                                                        }
                                                    }
                                                } ?: Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                        }
                                    }
                                )
                                if (!onTripStep && tripDraft.latitude != null) {
                                    DropdownMenuItem(
                                        text = { Text("Use Trip Location") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                        onClick = {
                                            locationMenuExpanded = false
                                            tripViewModel.updateEventDraft {
                                                it.copy(
                                                    latitude = tripDraft.latitude,
                                                    longitude = tripDraft.longitude
                                                )
                                            }
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Select on Map") },
                                    leadingIcon = { Icon(Icons.Default.Map, null) },
                                    onClick = {
                                        locationMenuExpanded = false
                                        if (onTripStep) tripLocationPicker.openPicker() else eventLocationPicker.openPicker()
                                    }
                                )
                                if (hasLocation) {
                                    DropdownMenuItem(
                                        text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.LocationOff, null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            locationMenuExpanded = false
                                            if (onTripStep) {
                                                tripViewModel.updateTripDraft { it.copy(latitude = null, longitude = null) }
                                            }
                                            else {
                                                tripViewModel.updateEventDraft { it.copy(latitude = null, longitude = null) }
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

            when (currentStep) {

                // ── Step 1: Trip info ────────────────────────────────────────
                WizardStep.TripInfo -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Trip Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = tripDraft.name,
                            onValueChange = { name ->
                                tripViewModel.updateTripDraft { tripDraft.copy(name = name) }
                            },
                            label = { Text("Trip Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "start", millis = tripDraft.startDate, modifier = Modifier.weight(1f)) { new ->
                                if (new > tripDraft.endDate) {
                                    tripViewModel.updateTripDraft { tripDraft.copy(startDate = new, endDate = new) }
                                } else {
                                    tripViewModel.updateTripDraft { tripDraft.copy(startDate = new) }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "end", millis = tripDraft.endDate, modifier = Modifier.weight(1f)) { new ->
                                if (new < tripDraft.startDate)
                                    Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                else tripViewModel.updateTripDraft { tripDraft.copy(endDate = new) }
                            }
                        }

                        // TODO -- add duration

                        if (tripDraft.latitude != null) {
                            LocationSetRow()
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                // Commit trip to DB now
                                scope.launch {
                                    tripViewModel.saveTrip(tripDraft)

                                    // Seed event defaults
                                    tripViewModel.updateEventDraft{
                                        eventDraft.copy(startTime = tripDraft.startDate, endTime = tripDraft.endDate)
                                    }
                                    currentStep = WizardStep.TripCrew
                                }
                            },
                            enabled = tripDraft.name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next: Select Crew")
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                // ── Step 2: Trip crew + tackle boxes ────────────────────────
                WizardStep.TripCrew -> {
                    Spacer(Modifier.height(16.dp))
                    TripViewModelCrewPickerBridge(
                        title = "Crew & Tackle Boxes",
                        subtitle = """
Select who's on the boat and which tackle box each person will use.
If a fisherman is removed from the trip, the fisherman will also be removed from all events.
""",
                        eligibleFishermen = sortedFishermen,
                        selectedIds = tripFishermenIds,
                        tackleBoxSelections = tripTackleBoxMap,
                        onSelectionChanged = { fishermanId, selected ->
                            tripViewModel.toggleTripFisherman(fishermanId)
                            if (selected) {
                                tripViewModel.upsertTripFishermanCrossRef(
                                    tripId = tripDraft.id,
                                    fishermanId = fishermanId,
                                    tackleBoxId = tripTackleBoxMap[fishermanId]
                                )
                            } else {
                                tripViewModel.removeFishermanCrossRefFromTripAndAllEvents(
                                    tripId = tripDraft.id,
                                    fishermanId = fishermanId
                                )
                            }
                        },
                        onTackleBoxChanged = { fishermanId, boxId ->
                            tripViewModel.upsertTripFishermanCrossRef(
                                tripId = tripDraft.id,
                                fishermanId = fishermanId,
                                tackleBoxId = boxId
                            )
                        },
                        tripViewModel = tripViewModel,
                        confirmLabel = if (fromReview) "Next: Review" else "Next: Add First Event",
                        onConfirm = {
                            if (!fromReview) {
                                tripViewModel.updateEventDraft {
                                    it.copy(
                                        name = "",
                                        tripId = tripDraft.id,
                                        startTime = tripDraft.startDate,
                                        endTime = tripDraft.endDate,
                                        latitude = null,
                                        longitude = null
                                    )
                                }
                            }

                            currentStep =
                                if (fromReview) WizardStep.Review else WizardStep.EventInfo
                        },
                        onAddFisherman = { first, last, nick ->
                            scope.launch { tripViewModel.addFisherman(first, last, nick) }
                        },
                        onAddTackleBox = { tackleBoxName, fishermanId ->
                            scope.launch {
                                tripViewModel.createAndAssignTackleBox(
                                    fishermanId = fishermanId,
                                    tripId = tripDraft.id,
                                    name = tackleBoxName
                                )
                            }
                        }
                    )
                }

                // ── Step 3: Event info ─────────────────────────────────────
                WizardStep.EventInfo -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    new < tripDraft.startDate ->
                                        Toast.makeText(context, "Cannot be before trip start", Toast.LENGTH_SHORT).show()
                                    new > tripDraft.endDate ->
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
                                    new > tripDraft.endDate ->
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

                                currentStep = WizardStep.EventCrew
                            },
                            enabled = eventDraft.name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next: Select Event Crew")
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                null,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                // ── Step 4: Event crew + tackle boxes ──────────────────────
                WizardStep.EventCrew -> {
                    val eligibleFishermen = remember(sortedFishermen, tripFishermenIds) {
                        sortedFishermen.filter { it.id in tripFishermenIds }
                    }
                    Spacer(Modifier.height(16.dp))
                    TripViewModelCrewPickerBridge(
                        title = "Event Crew & Tackle Boxes",
                        subtitle = "Who's fishing \"${eventDraft.name}\"? Tackle boxes default to trip selections.",
                        eligibleFishermen = eligibleFishermen,
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
                        tripViewModel = tripViewModel,
                        confirmLabel = "Review",
                        onConfirm = {
                            currentStep = WizardStep.Review
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

                // ── Step 5: Review ───────────────────────────────────────────
                WizardStep.Review -> {
                    val fmt = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Review Trip",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)

                        // Trip summary card
                        val currentTrip = TripSummary(
                            trip = tripDraft,
                            eventCount = 0,
                            totalCaught = 0,
                            totalKept = 0,
                            fishermanCount = tripFishermenIds.size,
                            tackleBoxCount = tripTackleBoxMap.size,
                            bigFishName = null,
                            bigFishSpecies = "",
                            bigFishLength = 0.0,
                            mostCaughtName = null,
                            mostCaught = 0
                        )

                        TripItem(
                            trip = currentTrip,
                            modifier = Modifier.padding(),
                            onClick = {
                                fromReview = true
                                currentStep = WizardStep.TripInfo
                            },
                            onAction = { action ->
                                when (action) {
                                    is TripAction.OpenMap -> {
                                        val mapUri = Uri.parse("geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)")
                                        val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        ) {
                            // Define the dropdown menu to be used with the TripItem card
                            TripMenu(
                                expanded = showTripMenu,
                                onMenuClick = { onTripAction(TripAction.Menu(tripSummary = currentTrip)) },
                                onDismiss = { showTripMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Use Current Location") },
                                    onClick = {
                                        onTripAction(TripAction.UseCurrentLocation(tripSummary = currentTrip))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = if (tripDraft.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Select Location") },
                                    onClick = {
                                        onTripAction(TripAction.SelectLocation(tripSummary = currentTrip))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Map,
                                            contentDescription = null,
                                            tint = if (tripDraft.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
                                        )
                                    }
                                )
                                if (tripDraft.latitude != null) {
                                    DropdownMenuItem(
                                        text = { Text("Clear Location") },
                                        onClick = {
                                            onTripAction(TripAction.ClearLocation(tripSummary = currentTrip))
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.LocationOff,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Events (${eventSummaries.size})", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = {
                                // Reset event fields for a new one
                                tripViewModel.updateEventDraft {
                                    it.copy(
                                        id = UUID.randomUUID().toString(),
                                        name = "",
                                        startTime = tripDraft.startDate,
                                        endTime = tripDraft.endDate,
                                        latitude = null,
                                        longitude = null
                                    )
                                }
                                tripViewModel.selectEvent(eventDraft.id)

                                currentStep = WizardStep.EventInfo
                            }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add event")
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            val totalItems = eventSummaries.size

                            itemsIndexed(eventSummaries, key = { _, seg -> seg.event.id }) { index, seg ->
                                val currentSummary = EventSummary(
                                    event = seg.event,
                                    fishCaught = 0,
                                    fishKept = 0,
                                    fishermanCount = seg.fishermanCount,
                                    tackleBoxCount = seg.tackleBoxCount,
                                    bigFishName = null,
                                    bigFishSpecies = "",
                                    bigFishLength = 0.0,
                                    mostCaughtName = null,
                                    mostCaught = 0
                                )

                                EventItem(
                                    currentSummary,
                                    modifier = Modifier.padding(
                                        vertical = 4.dp
                                    ),
                                    index = index,
                                    totalItems = totalItems,
                                    onClick = {
                                        fromReview = true

                                        tripViewModel.selectEvent(seg.event.id)
                                        tripViewModel.updateEventDraft { seg.event }

                                        currentStep = WizardStep.EventInfo
                                    }
                                )
                            }

                            if (eventSummaries.isEmpty()) {
                                item {
                                    Text(
                                        "No events yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }

                        }

                        // Done — trip + crew + events are already in DB.
                        // Just navigate back; nothing left to save.
                        Button(
                            onClick = {
                                tripViewModel.clearTrip()
                                tripViewModel.clearTripDraft()
                                tripViewModel.clearEvent()
                                tripViewModel.clearEventDraft()
                                navigateBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = eventSummaries.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.padding(end = 8.dp))
                            Text("Done")
                        }

                        OutlinedButton(
                            onClick = { cancelAndExit() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Discard Entire Trip")
                        }
                    }
                }
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
