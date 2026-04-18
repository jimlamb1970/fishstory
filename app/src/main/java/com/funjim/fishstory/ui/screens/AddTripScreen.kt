package com.funjim.fishstory.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.DateTimePickerButton
import com.funjim.fishstory.ui.TripViewModelCrewPickerBridge
import com.funjim.fishstory.ui.TripAction
import com.funjim.fishstory.ui.TripItem
import com.funjim.fishstory.ui.TripMenu
import com.funjim.fishstory.ui.hasLocationPermission
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.MainViewModel
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------------------------------------
// Wizard steps
// ---------------------------------------------------------------------------
private enum class WizardStep {
    TripInfo,           // Step 1 – name, dates, location
    TripCrew,           // Step 2 – fishermen + tackle boxes for trip
    SegmentInfo,        // Step 3 – segment name, dates, location
    SegmentCrew,        // Step 4 – fishermen + tackle boxes for segment
    Review              // Step 5 – list segments, add another or finish
}

// Lightweight in-memory segment draft — no ViewModel needed
private data class SegmentDraft(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startTime: Long,
    val endTime: Long,
    val latitude: Double?,
    val longitude: Double?,
    val fishermanIds: Set<String>
)

// ---------------------------------------------------------------------------
// AddTripScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    viewModel: MainViewModel,
    tripViewModel: TripViewModel,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }

    // All fishermen from DB — used for crew selection
    val allFishermen by tripViewModel.fishermen.collectAsState(initial = emptyList())
    val sortedFishermen = remember(allFishermen) { allFishermen.sortedBy { it.fullName } }

    // TODO -- look into using TripSummary and SegmentSummary

    // ── Trip-level state ────────────────────────────────────────────────────
    var tripName by remember { mutableStateOf("") }
    var tripStart by remember { mutableLongStateOf(now) }
    var tripEnd by remember { mutableLongStateOf(now) }
    var tripLat by remember { mutableStateOf<Double?>(null) }
    var tripLng by remember { mutableStateOf<Double?>(null) }
    var tripFishermanIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    // Maps fishermanId -> selected tackleBoxId for trip crew step

    // The Trip row in the DB — null until Step 1 is committed
    var newTripId by remember { mutableStateOf<String>(UUID.randomUUID().toString()) }
    var newSegmentId by remember { mutableStateOf<String>(UUID.randomUUID().toString()) }

    LaunchedEffect(newTripId) {
        tripViewModel.selectTrip(newTripId)
        tripViewModel.selectSegment(newSegmentId)
    }
    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val segmentTackleBoxMap by tripViewModel.segmentTackleBoxMap.collectAsState()

    // ── Segment-level state (reused for each segment) ───────────────────────
    var segmentName by remember { mutableStateOf("") }
    var segmentStart by remember { mutableLongStateOf(now) }
    var segmentEnd by remember { mutableLongStateOf(now) }
    var segmentLat by remember { mutableStateOf<Double?>(null) }
    var segmentLng by remember { mutableStateOf<Double?>(null) }
    var segmentFishermanIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    // Maps fishermanId -> selected tackleBoxId for segment crew step

    // In-memory list of committed segments
    var committedSegments by remember { mutableStateOf<List<SegmentDraft>>(emptyList()) }

    // ── Wizard step ─────────────────────────────────────────────────────────
    var currentStep by remember { mutableStateOf(WizardStep.TripInfo) }
    var isFirstSegment by remember { mutableStateOf(true) }
    var fromReview by remember { mutableStateOf(false) }

    var showTripMenu by remember { mutableStateOf(false) }

    var locationMenuExpanded by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val tripLocationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = tripLat,
        existingLng = tripLng,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng -> tripLat = lat; tripLng = lng }
    )

    val segmentLocationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = segmentLat,
        existingLng = segmentLng,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng -> segmentLat = lat; segmentLng = lng }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value }) {
            scope.launch {
                viewModel.getCurrentLocation(context)?.let { loc ->
                    if (currentStep == WizardStep.TripInfo) {
                        tripLat = loc.latitude; tripLng = loc.longitude
                    } else if (currentStep == WizardStep.SegmentInfo) {
                        segmentLat = loc.latitude; segmentLng = loc.longitude
                    }
                } ?: Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper: delete the trip row if user cancels mid-wizard
    fun cancelAndExit() {
        scope.launch {
            // CASCADE deletes will clean up fisherman cross-refs and segments
            viewModel.deleteTripById(newTripId)
        }
        navigateBack()
    }

    // Progress indicator
    val stepLabels = listOf("Trip", "Crew & Boxes", "Segment", "Seg. Crew & Boxes", "Review")
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
                        val location = viewModel.getCurrentLocation(context)
                        if (location != null) {
                            viewModel.updateTrip(
                                action.tripSummary.trip.copy(
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            )
                            tripLat = location.latitude; tripLng = location.longitude
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
                scope.launch {
                    viewModel.updateTrip(
                        action.tripSummary.trip.copy(
                            latitude = tripLat,
                            longitude = tripLng
                        )
                    )
                }
            }
            is TripAction.ClearLocation -> {
                showTripMenu = false
                scope.launch {
                    viewModel.updateTrip(
                        action.tripSummary.trip.copy(latitude = null, longitude = null)
                    )
                    tripLat = null; tripLng = null
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentStep) {
                            WizardStep.TripInfo    -> cancelAndExit()
                            WizardStep.TripCrew    -> currentStep = WizardStep.TripInfo
                            WizardStep.SegmentInfo -> {
                                // Logic depends on whether this is the start of the trip or a later addition
                                currentStep = if (isFirstSegment) {
                                    WizardStep.TripCrew
                                } else {
                                    WizardStep.Review
                                }
                            }
                            WizardStep.SegmentCrew -> currentStep = WizardStep.SegmentInfo
                            WizardStep.Review      -> currentStep = WizardStep.SegmentInfo
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentStep == WizardStep.TripInfo || currentStep == WizardStep.SegmentInfo) {
                        val onTripStep = currentStep == WizardStep.TripInfo
                        val hasLocation = if (onTripStep) tripLat != null else segmentLat != null
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
                                                viewModel.getCurrentLocation(context)?.let { loc ->
                                                    if (onTripStep) { tripLat = loc.latitude; tripLng = loc.longitude }
                                                    else { segmentLat = loc.latitude; segmentLng = loc.longitude }
                                                } ?: Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                        }
                                    }
                                )
                                if (!onTripStep && tripLat != null) {
                                    DropdownMenuItem(
                                        text = { Text("Use Trip Location") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                        onClick = {
                                            locationMenuExpanded = false
                                            segmentLat = tripLat; segmentLng = tripLng
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Select on Map") },
                                    leadingIcon = { Icon(Icons.Default.Map, null) },
                                    onClick = {
                                        locationMenuExpanded = false
                                        if (onTripStep) tripLocationPicker.openPicker() else segmentLocationPicker.openPicker()
                                    }
                                )
                                if (hasLocation) {
                                    DropdownMenuItem(
                                        text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.LocationOff, null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            locationMenuExpanded = false
                                            if (onTripStep) { tripLat = null; tripLng = null }
                                            else { segmentLat = null; segmentLng = null }
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
                            value = tripName,
                            onValueChange = { tripName = it },
                            label = { Text("Trip Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "start", millis = tripStart, modifier = Modifier.weight(1f)) { new ->
                                tripStart = new
                                if (new > tripEnd) tripEnd = new
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "end", millis = tripEnd, modifier = Modifier.weight(1f)) { new ->
                                if (new < tripStart) Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                else tripEnd = new
                            }
                        }

                        if (tripLat != null) {
                            LocationSetRow()
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                // Commit trip to DB now
                                scope.launch {
                                    val trip = Trip(
                                        id = newTripId,
                                        name = tripName,
                                        startDate = tripStart,
                                        endDate = tripEnd,
                                        latitude = tripLat,
                                        longitude = tripLng
                                    )
                                    viewModel.upsertTrip(trip)

                                    // Seed segment defaults
                                    segmentStart = tripStart
                                    segmentEnd = tripEnd
                                    currentStep = WizardStep.TripCrew
                                }
                            },
                            enabled = tripName.isNotBlank(),
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
                        subtitle = "Select who's on the boat and which tackle box each person will use.",
                        eligibleFishermen = sortedFishermen,
                        selectedIds = tripFishermanIds,
                        tackleBoxSelections = tripTackleBoxMap,
                        onSelectionChanged = { fishermanId, selected ->
                            if (selected) {
                                tripFishermanIds = tripFishermanIds + fishermanId
                                viewModel.upsertTripFishermanCrossRef(
                                    tripId = newTripId,
                                    fishermanId = fishermanId,
                                    tackleBoxId = tripTackleBoxMap[fishermanId]
                                )
                            } else {
                                tripViewModel.deleteTripFishermanCrossRef(
                                    tripId = newTripId,
                                    fishermanId = fishermanId
                                )
                                tripFishermanIds = tripFishermanIds - fishermanId
                            }
                        },
                        onTackleBoxChanged = { fishermanId, boxId ->
                            viewModel.upsertTripFishermanCrossRef(
                                tripId = newTripId,
                                fishermanId = fishermanId,
                                tackleBoxId = boxId
                            )
                        },
                        tripViewModel = tripViewModel,
                        confirmLabel = if (fromReview) "Next: Review" else "Next: Add First Segment",
                        onConfirm = {
                            scope.launch {
                                // TODO - Need to refactor trip and segment fisherman cross references
                                // TODO - As soon as removed from trip, should be removed from segment
                                committedSegments = committedSegments.map { segment ->
                                    segment.copy(
                                        fishermanIds = segment.fishermanIds intersect tripFishermanIds
                                    )
                                }
                                committedSegments.forEach { segment ->
                                    viewModel.removeSegmentFishermenNotInSet(
                                        segmentId = segment.id,
                                        newSet = tripFishermanIds
                                    )
                                }
                                segmentFishermanIds = tripFishermanIds
                                segmentName = ""
                                segmentStart = tripStart
                                segmentEnd = tripEnd
                                segmentLat = null; segmentLng = null
                                currentStep =
                                    if (fromReview) WizardStep.Review else WizardStep.SegmentInfo
                            }
                        },
                        onAddFisherman = { first, last, nick ->
                            scope.launch { viewModel.addFisherman(first, last, nick) }
                        },
                        onAddTackleBox = { tackleBoxName, fishermanId ->
                            scope.launch {
                                tripViewModel.createAndAssignTackleBox(
                                    fishermanId = fishermanId,
                                    tripId = newTripId,
                                    name = tackleBoxName
                                )
                            }
                        }
                    )
                }

                // ── Step 3: Segment info ─────────────────────────────────────
                WizardStep.SegmentInfo -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Segment Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "A segment is a single fishing session — e.g. morning run, afternoon drift.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = segmentName,
                            onValueChange = { segmentName = it },
                            label = { Text("Segment Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "start", millis = segmentStart, modifier = Modifier.weight(1f)) { new ->
                                when {
                                    new < tripStart -> Toast.makeText(context, "Cannot be before trip start", Toast.LENGTH_SHORT).show()
                                    new > tripEnd   -> Toast.makeText(context, "Cannot be after trip end", Toast.LENGTH_SHORT).show()
                                    else -> { segmentStart = new; if (new > segmentEnd) segmentEnd = new }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "end", millis = segmentEnd, modifier = Modifier.weight(1f)) { new ->
                                when {
                                    new < segmentStart -> Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                    new > tripEnd      -> Toast.makeText(context, "Cannot be after trip end", Toast.LENGTH_SHORT).show()
                                    else -> segmentEnd = new
                                }
                            }
                        }

                        if (segmentLat != null) {
                            LocationSetRow()
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                scope.launch {
                                    // Add the new segment
                                    val segment = Segment(
                                        tripId = newTripId,
                                        id = newSegmentId,
                                        name = segmentName,
                                        startTime = segmentStart,
                                        endTime = segmentEnd,
                                        latitude = segmentLat,
                                        longitude = segmentLng
                                    )
                                    viewModel.upsertSegment(segment)

                                    if (segmentTackleBoxMap.isEmpty()) {
                                        segmentFishermanIds.forEach { fishermanId ->
                                            viewModel.upsertSegmentFishermanCrossRef(
                                                segmentId = newSegmentId,
                                                fishermanId = fishermanId,
                                                tackleBoxId = tripTackleBoxMap[fishermanId]
                                            )
                                        }
                                    }
                                }

                                currentStep = WizardStep.SegmentCrew
                            },
                            enabled = segmentName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next: Select Segment Crew")
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                // ── Step 4: Segment crew + tackle boxes ──────────────────────
                WizardStep.SegmentCrew -> {
                    val eligibleFishermen = remember(sortedFishermen, tripFishermanIds) {
                        sortedFishermen.filter { it.id in tripFishermanIds }
                    }
                    Spacer(Modifier.height(16.dp))
                    TripViewModelCrewPickerBridge(
                        title = "Segment Crew & Tackle Boxes",
                        subtitle = "Who's fishing \"$segmentName\"? Tackle boxes default to trip selections.",
                        eligibleFishermen = eligibleFishermen,
                        selectedIds = segmentFishermanIds,
                        tackleBoxSelections = segmentTackleBoxMap,
                        onSelectionChanged = { fishermanId, selected ->
                            if (selected) {
                                segmentFishermanIds = segmentFishermanIds + fishermanId
                                viewModel.upsertSegmentFishermanCrossRef(
                                    segmentId = newSegmentId,
                                    fishermanId = fishermanId,
                                    tackleBoxId = segmentTackleBoxMap[fishermanId]
                                )
                            } else {
                                segmentFishermanIds = segmentFishermanIds - fishermanId
                                tripViewModel.deleteSegmentFishermanCrossRef(
                                    segmentId = newSegmentId,
                                    fishermanId = fishermanId
                                )
                            }
                        },
                        onTackleBoxChanged = { fishermanId, boxId ->
                            viewModel.upsertSegmentFishermanCrossRef(
                                segmentId = newSegmentId,
                                fishermanId = fishermanId,
                                tackleBoxId = boxId
                            )
                        },
                        tripViewModel = tripViewModel,
                        confirmLabel = "Review",
                        onConfirm = {
                            scope.launch {
                                val existing = committedSegments.find { it.id == newSegmentId }
                                if (existing != null) committedSegments = committedSegments - existing
                                committedSegments = committedSegments + SegmentDraft(
                                    id = newSegmentId,
                                    name = segmentName,
                                    startTime = segmentStart,
                                    endTime = segmentEnd,
                                    latitude = segmentLat,
                                    longitude = segmentLng,
                                    fishermanIds = segmentFishermanIds
                                )
                                currentStep = WizardStep.Review
                            }
                        },
                        onAddTackleBox = { tackleBoxName, fishermanId ->
                            scope.launch { tripViewModel.createAndAssignSegmentTackleBox(
                                fishermanId = fishermanId,
                                segmentId = newSegmentId,
                                name = tackleBoxName
                            ) }
                        }

                    )
                }

                // ── Step 5: Review ───────────────────────────────────────────
                WizardStep.Review -> {
                    val fmt = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
                    val shortFmt = remember { SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()) }

                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Review Trip",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)

                        // Trip summary card
                        // TODO - look into using same card (TripItem) from TripListScreen
                        val currentTrip = TripSummary(
                            trip = Trip(
                                id = newTripId,
                                name = tripName,
                                startDate = tripStart,
                                endDate = tripEnd,
                                latitude = tripLat,
                                longitude = tripLng
                            ),
                            totalCaught = 0,
                            totalKept = 0,
                            fishermanCount = tripFishermanIds.size,
                            tackleBoxCount = tripTackleBoxMap.size,
                            bigFishWinner = null,
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
                                            tint = if (tripLat != null) Color(0xFF4CAF50) else LocalContentColor.current
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
                                            tint = if (tripLat != null) Color(0xFF4CAF50) else LocalContentColor.current
                                        )
                                    }
                                )
                                if (tripLat != null) {
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
                            Text("Segments (${committedSegments.size})", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = {
                                // Reset segment fields for a new one
                                newSegmentId = UUID.randomUUID().toString()
                                segmentName = ""
                                segmentStart = tripStart
                                segmentEnd = tripEnd
                                segmentLat = null; segmentLng = null
                                segmentFishermanIds = tripFishermanIds

                                tripViewModel.selectSegment(newSegmentId)
                                currentStep = WizardStep.SegmentInfo
                            }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add segment")
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(committedSegments, key = { it.id }) { seg ->
                                // TODO - look into using same card (SegmentItem) from SegmentComponents
                                Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                                    .clickable() {
                                        fromReview = true

                                        newSegmentId = seg.id
                                        segmentName = seg.name
                                        segmentStart = seg.startTime
                                        segmentEnd = seg.endTime
                                        segmentLat = seg.latitude
                                        segmentLng = seg.longitude
                                        segmentFishermanIds = seg.fishermanIds

                                        tripViewModel.selectSegment(newSegmentId)
                                        currentStep = WizardStep.SegmentInfo
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                seg.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.Black
                                            )
                                            if (seg.latitude != null) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = "Trip Location",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Text("${fmt.format(Date(seg.startTime))}  →  ${fmt.format(Date(seg.endTime))}",
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "${seg.fishermanIds.size} ${if (seg.fishermanIds.size == 1) "fisherman" else "fishermen"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            if (committedSegments.isEmpty()) {
                                item {
                                    Text(
                                        "No segments yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }

                        }

                        // Done — trip + crew + segments are already in DB.
                        // Just navigate back; nothing left to save.
                        Button(
                            onClick = { navigateBack() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = committedSegments.isNotEmpty()
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
