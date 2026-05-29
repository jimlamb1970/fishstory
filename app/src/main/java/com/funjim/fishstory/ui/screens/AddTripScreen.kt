package com.funjim.fishstory.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.EventItem
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.TargetSpeciesRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.TripViewModelCrewPickerBridge
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItem
import com.funjim.fishstory.ui.utils.TripMenu
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnVariantColor
import com.funjim.fishstory.ui.utils.getVariantColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.AddTripViewModel
import com.funjim.fishstory.viewmodels.TripViewModel
import com.funjim.fishstory.viewmodels.WizardStep
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------------------------------------
// AddTripScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    tripViewModel: AddTripViewModel,
    navigateToEditTackleBox: ((fishermanId: String, tackleBoxId: String) -> Unit),
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // All fishermen from DB — used for crew selection
    val allFishermen by tripViewModel.fishermen.collectAsState(initial = emptyList())
    val sortedFishermen = remember(allFishermen) { allFishermen.sortedBy { it.fullName } }

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

    val eventSummaries by tripViewModel.eventSummaries.collectAsStateWithLifecycle()

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val eventTackleBoxMap by tripViewModel.eventTackleBoxMap.collectAsState()

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by tripViewModel.allSpecies.collectAsStateWithLifecycle()
    var addNewSpecies by remember { mutableStateOf(false) }

    val tripTargetSpecies by tripViewModel.tripTargetSpecies.collectAsStateWithLifecycle()
    val eventTargetSpeciesMap by tripViewModel.eventTargetSpeciesMap.collectAsStateWithLifecycle()
    val eventTargetSpeciesUsageMap by tripViewModel.eventTargetSpeciesUsageMap.collectAsStateWithLifecycle()

    // ── Wizard step ─────────────────────────────────────────────────────────
    val currentStep by tripViewModel.currentWizardStep.collectAsStateWithLifecycle()
    var fromReview by remember { mutableStateOf(false) }

    var overrideEventCrew by remember { mutableStateOf(false) }

    var showTripMenu by remember { mutableStateOf(false) }
    var locationMenuExpanded by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by tripViewModel.deviceLocation.collectAsStateWithLifecycle()

    val tripLocationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = tripDraft.latitude,
        existingLng = tripDraft.longitude,
        onFetchLocation = { scope.launch { tripViewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            tripViewModel.updateTripDraft { it.copy(latitude = lat, longitude = lng) }
        }
    )

    val eventLocationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = eventDraft.latitude,
        existingLng = eventDraft.longitude,
        onFetchLocation = { scope.launch { tripViewModel.fetchDeviceLocationOnce() } },
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
        scope.launch {
            tripViewModel.deleteTripById(tripDraft.id)
            tripViewModel.clearTrip()
            tripViewModel.clearTripDraft()
            tripViewModel.clearEvent()
            tripViewModel.clearEventDraft()
            navigateBack()
        }
    }

    // Progress indicator
    val stepLabels = remember(overrideEventCrew) {
        if (overrideEventCrew) {
            listOf("Trip Details", "Trip Crew", "Event Details", "Event Crew Override", "Review & Done")
        } else {
            listOf("Trip Details", "Trip Crew", "Event Details", "Review & Done")
        }
    }
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
                if (tripViewModel.hasLocationPermission()) {
                    scope.launch {
                        @SuppressLint("MissingPermission")
                        val location = tripViewModel.fetchLocation()
                        if (location != null) {
                            tripViewModel.updateTripDraft {
                                it.copy(latitude = location.latitude, longitude = location.longitude)
                            }
                            tripViewModel.saveTrip()
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
                scope.launch { tripViewModel.saveTrip() }
            }
            is TripAction.ClearLocation -> {
                showTripMenu = false
                tripViewModel.updateTripDraft { it.copy(latitude = null, longitude = null) }

                scope.launch {
                    tripViewModel.saveTrip()
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
                            WizardStep.TripCrew    -> tripViewModel.updateWizardStep(WizardStep.TripInfo)
                            WizardStep.EventInfo -> {
                                // Logic depends on whether this is the start of the trip or a later addition
                                val nextStep = if (!fromReview) {
                                    WizardStep.TripCrew
                                } else {
                                    WizardStep.Review
                                }
                                tripViewModel.updateWizardStep(nextStep)
                            }
                            WizardStep.EventCrew -> tripViewModel.updateWizardStep(WizardStep.EventInfo)
                            WizardStep.Review -> {
                                val backStep =
                                    if (overrideEventCrew) WizardStep.EventCrew
                                    else WizardStep.EventInfo
                                tripViewModel.updateWizardStep(backStep)
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentStep == WizardStep.TripInfo || currentStep == WizardStep.EventInfo) {
                        val onTripStep = currentStep == WizardStep.TripInfo
                        val onEventStep = currentStep == WizardStep.EventInfo

                        val hasTripLocation = (tripDraft.latitude != null)
                        val hasEventLocation = (eventDraft.latitude != null)

                        val hasLocation = hasTripLocation || (onEventStep && hasEventLocation)

                        Box {
                            IconButton(onClick = { locationMenuExpanded = true }) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint =
                                        if (hasLocation) Color(0xFF4CAF50)
                                        else LocalContentColor.current
                                )
                            }

                            DropdownMenu(
                                expanded = locationMenuExpanded,
                                onDismissRequest = { locationMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Use Current Location") },
                                    leadingIcon = { Icon(Icons.Default.MyLocation, null) },
                                    onClick = {
                                        locationMenuExpanded = false
                                        if (tripViewModel.hasLocationPermission()) {
                                            scope.launch {
                                                tripViewModel.fetchLocation()?.let { loc ->
                                                    if (onTripStep) {
                                                        tripViewModel.updateTripDraft {
                                                            it.copy(
                                                                latitude = loc.latitude,
                                                                longitude = loc.longitude
                                                            )
                                                        }
                                                    } else {
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

                                DropdownMenuItem(
                                    text = { Text("Select on Map") },
                                    leadingIcon = { Icon(Icons.Default.Map, null) },
                                    onClick = {
                                        locationMenuExpanded = false
                                        if (onTripStep) tripLocationPicker.openPicker()
                                        else eventLocationPicker.openPicker()
                                    }
                                )

                                if ((onTripStep && hasTripLocation) ||
                                    (onEventStep && hasEventLocation)) {
                                    DropdownMenuItem(
                                        text = {
                                            if (onEventStep && hasTripLocation) Text("Reset Location")
                                            else Text("Clear Location")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.LocationOff,
                                                null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
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
                        Row(verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Trip Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            if (tripDraft.latitude != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = getOnMainColor(),
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            }
                        }

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

                        HorizontalDivider()

                        TargetSpeciesRow(
                            items = tripTargetSpecies,
                            onAdd = { showSpeciesSelection = true },
                            onDelete = { species ->
                                tripViewModel.removeTripTargetSpecies(species)
                            },
                            thumbnailProvider = { species ->
                                val thumbnailFlow = remember(species.id) {
                                    tripViewModel.speciesThumbnail(species.id)
                                }

                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.TargetFish,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.padding(0.dp)
                        )

                        // TODO -- add duration
                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                // Commit trip to DB now
                                scope.launch {
                                    tripViewModel.saveTrip()
                                    tripViewModel.updateWizardStep(WizardStep.TripCrew)
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
                    // TODO -- Add a Help Dialog to explain Tackle Boxes
                    Spacer(Modifier.height(16.dp))
                    TripViewModelCrewPickerBridge(
                        title = "Trip Crew & Tackle Boxes",
                        subtitle = """Select who's fishing and which tackle box each person will use.
                            |
                            |If a fisherman is removed from the trip, the fisherman will also be removed from all events."""
                            .trimMargin(),
                        eligibleFishermen = sortedFishermen,
                        selectedIds = tripFishermenIds,
                        tackleBoxSelections = tripTackleBoxMap,
                        getTackleBoxesForFisherman = { fishermanId ->
                            tripViewModel.getTackleBoxesForFisherman(fishermanId)
                                .collectAsState(initial = emptyList()).value
                        },
                        getLureCount = { tackleBoxId ->
                            tripViewModel.getLureCountForTackleBox(tackleBoxId)
                                .collectAsState(initial = 0).value
                        },
                        getLuresInTacklebox = { tackleBoxId ->
                            tripViewModel.getLuresInTackleBox(tackleBoxId).collectAsState(initial = emptyList()).value
                        },
                        onSelectionChanged = { fishermanId, selected ->
                            tripViewModel.toggleTripFisherman(fishermanId)
                            if (selected) {
                                scope.launch {
                                    tripViewModel.upsertTripFishermanCrossRef(
                                        tripId = tripDraft.id,
                                        fishermanId = fishermanId,
                                        tackleBoxId = tripTackleBoxMap[fishermanId]
                                    )
                                }
                            } else {
                                scope.launch {
                                    tripViewModel.removeFishermanFromTripAndAllEvents(
                                        tripId = tripDraft.id,
                                        fishermanId = fishermanId
                                    )
                                }
                            }
                        },
                        onTackleBoxChanged = { fishermanId, boxId ->
                            scope.launch {
                                tripViewModel.upsertTripFishermanCrossRef(
                                    tripId = tripDraft.id,
                                    fishermanId = fishermanId,
                                    tackleBoxId = boxId
                                )
                            }
                        },
                        navigateToEditTackleBox = navigateToEditTackleBox,
                        confirmLabel = if (fromReview) "Next: Review" else "Next: Add First Event",
                        onConfirm = {
                            if (!fromReview) {
                                tripViewModel.prepEventDraft(tripDraft)
                            }

                            tripViewModel.updateWizardStep(
                                if (fromReview) WizardStep.Review
                                else WizardStep.EventInfo)
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
                        Row(verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Event Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            if ((tripDraft.latitude != null) || (eventDraft.latitude != null)) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = getOnMainColor(),
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                                if (eventDraft.latitude == null) {
                                    Text(
                                        text = "(Trip)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = getOnMainColor()
                                    )
                                }
                            }
                        }

                        Text(
                            "An event is a single fishing session — e.g. morning run, afternoon drift.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getOnVariantColor()
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

                        HorizontalDivider()

                        TargetSpeciesRow(
                            items = eventTargetSpeciesMap[eventDraft.id] ?: emptyList(),
                            onAdd = { showSpeciesSelection = true },
                            onDelete = { species ->
                                tripViewModel.removeEventTargetSpecies(eventDraft.id, species)
                            },
                            thumbnailProvider = { species ->
                                val thumbnailFlow = remember(species.id) {
                                    tripViewModel.speciesThumbnail(species.id)
                                }

                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.TargetFish,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.padding(0.dp)
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
                                    .clickable { overrideEventCrew = !overrideEventCrew }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = overrideEventCrew,
                                    onCheckedChange = { overrideEventCrew = it }
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
                                    // Add/update the event
                                    tripViewModel.upsertEvent(eventDraft)

                                    if (overrideEventCrew) {
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
                                        tripViewModel.updateWizardStep(WizardStep.EventCrew)
                                    } else {
                                        eventFishermenIds.forEach { fishermanId ->
                                            tripViewModel.deleteEventFishermanCrossRef(
                                                eventId = eventDraft.id,
                                                fishermanId = fishermanId
                                            )
                                        }
                                        tripViewModel.updateEventFishermanIds(emptySet())

                                        // Directly jump to summary validation stage
                                        tripViewModel.updateWizardStep(WizardStep.Review)
                                    }
                                }

                                tripViewModel.updateWizardStep(WizardStep.EventCrew)
                            },
                            enabled = eventDraft.name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (overrideEventCrew) "Next: Select Event Crew"
                                else "Next: Review Trip")
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
                        subtitle = """Select who's fishing "${eventDraft.name}" and which tackle box each person will use.
                            |
                            |If no one is selected, the trip crew and tackle box assignments will be used."""
                            .trimMargin(),
                        eligibleFishermen = eligibleFishermen,
                        selectedIds = eventFishermenIds,
                        tackleBoxSelections = eventTackleBoxMap,
                        getTackleBoxesForFisherman = { fishermanId ->
                            tripViewModel.getTackleBoxesForFisherman(fishermanId)
                                .collectAsState(initial = emptyList()).value
                        },
                        getLureCount = { tackleBoxId ->
                            tripViewModel.getLureCountForTackleBox(tackleBoxId)
                                .collectAsState(initial = 0).value
                        },
                        getLuresInTacklebox = { tackleBoxId ->
                            tripViewModel.getLuresInTackleBox(tackleBoxId).collectAsState(initial = emptyList()).value
                        },
                        onSelectionChanged = { fishermanId, selected ->
                            tripViewModel.toggleEventFisherman(fishermanId)
                            if (selected) {
                                scope.launch {
                                    tripViewModel.upsertEventFishermanCrossRef(
                                        eventId = eventDraft.id,
                                        fishermanId = fishermanId,
                                        tackleBoxId = eventTackleBoxMap[fishermanId]
                                    )
                                }
                            } else {
                                scope.launch {
                                    tripViewModel.deleteEventFishermanCrossRef(
                                        eventId = eventDraft.id,
                                        fishermanId = fishermanId
                                    )
                                }
                            }
                        },
                        onTackleBoxChanged = { fishermanId, boxId ->
                            scope.launch {
                                tripViewModel.upsertEventFishermanCrossRef(
                                    eventId = eventDraft.id,
                                    fishermanId = fishermanId,
                                    tackleBoxId = boxId
                                )
                            }
                        },
                        navigateToEditTackleBox = navigateToEditTackleBox,
                        confirmLabel = "Review",
                        onConfirm = {
                            tripViewModel.updateWizardStep(WizardStep.Review)
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

                        // Have to count how many of the tackle boxes
                        val activeCount = remember(tripTackleBoxMap) {
                            tripTackleBoxMap.count { !it.value.isNullOrBlank() }
                        }

                        // Trip summary card
                        val currentTrip = TripSummary(
                            trip = tripDraft,
                            eventCount = eventSummaries.size,
                            fishCaught = 0,
                            fishKept = 0,
                            fishermanCount = tripFishermenIds.size,
                            tackleBoxCount = activeCount,
                            targetFishCaught = 0,
                            targetFishKept = 0
                        )

                        TripItem(
                            trip = currentTrip,
                            modifier = Modifier.padding(),
                            thumbnailFlow = flowOf(null),
                            onClick = {
                                fromReview = true
                                tripViewModel.updateWizardStep(WizardStep.TripInfo)
                            },
                            onLongClick = { showTripMenu = true },
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
                                    text = { Text("Select on Map") },
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
                                overrideEventCrew = false
                                tripViewModel.prepEventDraft(tripDraft)
                                tripViewModel.updateWizardStep(WizardStep.EventInfo)
                            }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add event")
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            val totalItems = eventSummaries.size

                            itemsIndexed(eventSummaries, key = { _, event -> event.event.id }) { index, event ->
                                EventItem(
                                    event,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    index = index,
                                    totalItems = totalItems,
                                    thumbnailFlow = flowOf(null),
                                    onClick = {
                                        fromReview = true

                                        tripViewModel.selectEvent(event.event.id)
                                        tripViewModel.updateEventDraft { event.event }

                                        overrideEventCrew = event.fishermanCount > 0
                                        tripViewModel.updateWizardStep(WizardStep.EventInfo)
                                    },
                                )
                            }

                            if (eventSummaries.isEmpty()) {
                                item {
                                    Text(
                                        "No events yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = getOnVariantColor(),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }

                        }

                        // Done — trip + crew + events are already in DB.
                        // Just navigate back; nothing left to save.
                        Button(
                            onClick = {
                                scope.launch {
                                    tripViewModel.persistTargetSpecies()

                                    tripViewModel.clearTrip()
                                    tripViewModel.clearTripDraft()
                                    tripViewModel.clearEvent()
                                    tripViewModel.clearEventDraft()
                                    navigateBack()
                                }
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

        if (showSpeciesSelection) {
            SpeciesSelection(
                items = allSpecies,
                selectedItems =
                    if (currentStep == WizardStep.TripInfo) tripTargetSpecies
                    else eventTargetSpeciesMap[eventDraft.id] ?: emptyList(),
                onSelected = { species ->
                    if (currentStep == WizardStep.TripInfo)
                        tripViewModel.addTripTargetSpecies(species)
                    else
                        tripViewModel.addEventTargetSpecies(eventDraft.id, species)
                },
                onUnselected = { species ->
                    if (currentStep == WizardStep.TripInfo)
                        tripViewModel.removeTripTargetSpecies(species)
                    else
                        tripViewModel.removeEventTargetSpecies(eventDraft.id, species)
                },
                onAdd = {
                    addNewSpecies = true
                },
                onDone = { showSpeciesSelection = false },
                modifier = Modifier.fillMaxWidth(),
                thumbnailProvider = { species ->
                    val thumbnailFlow = remember(species.id) {
                        tripViewModel.speciesThumbnail(species.id)
                    }

                    val thumbnail by thumbnailFlow.collectAsState(initial = null)

                    ThumbnailBox(
                        thumbnail = thumbnail,
                        imageVector = AppIcons.Default.TargetFish,
                        modifier = Modifier.size(48.dp)
                    )
                },
                usageMap =
                    if (currentStep == WizardStep.TripInfo) eventTargetSpeciesUsageMap
                    else null,
                maxUsage =
                    if (currentStep == WizardStep.TripInfo) eventSummaries.size
                    else null
            )
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
                            tripViewModel.addSpecies(species)
                            if (currentStep == WizardStep.TripInfo)
                                tripViewModel.addTripTargetSpecies(species)
                            else
                                tripViewModel.addEventTargetSpecies(eventDraft.id, species)
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
