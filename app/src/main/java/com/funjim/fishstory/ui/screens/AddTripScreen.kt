package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.DateTimePickerButton
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
    val fishermanIds: Set<String>,
    val tackleBoxSelections: Map<String, String?>
)

// ---------------------------------------------------------------------------
// AddTripScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    viewModel: MainViewModel,
    tripViewModel: TripViewModel,
    navigateBack: () -> Unit,
    // Kept for nav-compat but unused — wizard is self-contained
    navigateToLoadBoatForTrip: () -> Unit = {},
    navigateToAddSegment: (String) -> Unit = {},
    navigateToDraftSegmentDetails: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }

    // All fishermen from DB — used for crew selection
    val allFishermen by viewModel.fishermen.collectAsState(initial = emptyList())
    val sortedFishermen = remember(allFishermen) { allFishermen.sortedBy { it.fullName } }

    // ── Trip-level state ────────────────────────────────────────────────────
    var tripName by remember { mutableStateOf("") }
    var tripStart by remember { mutableLongStateOf(now) }
    var tripEnd by remember { mutableLongStateOf(now) }
    var tripLat by remember { mutableStateOf<Double?>(null) }
    var tripLng by remember { mutableStateOf<Double?>(null) }
    var tripFishermanIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    // Maps fishermanId -> selected tackleBoxId for trip crew step
    var tripTackleBoxSelections by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    // The Trip row in the DB — null until Step 1 is committed
    var newTripId by remember { mutableStateOf<String>(UUID.randomUUID().toString()) }
    var newSegmentId by remember { mutableStateOf<String>(UUID.randomUUID().toString()) }

    // ── Segment-level state (reused for each segment) ───────────────────────
    var segmentName by remember { mutableStateOf("") }
    var segmentStart by remember { mutableLongStateOf(now) }
    var segmentEnd by remember { mutableLongStateOf(now) }
    var segmentLat by remember { mutableStateOf<Double?>(null) }
    var segmentLng by remember { mutableStateOf<Double?>(null) }
    var segmentFishermanIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    // Maps fishermanId -> selected tackleBoxId for segment crew step
    var segmentTackleBoxSelections by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    // In-memory list of committed segments
    var committedSegments by remember { mutableStateOf<List<SegmentDraft>>(emptyList()) }

    // ── Wizard step ─────────────────────────────────────────────────────────
    var currentStep by remember { mutableStateOf(WizardStep.TripInfo) }
    var isFirstSegment by remember { mutableStateOf(true) }
    var fromReview by remember { mutableStateOf(false) }

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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            LinearProgressIndicator(
                progress = { (stepIndex + 1f) / stepLabels.size },
                modifier = Modifier.fillMaxWidth()
            )

            when (currentStep) {

                // ── Step 1: Trip info ────────────────────────────────────────
                WizardStep.TripInfo -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
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
                    var showAddFishermanDialog by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Crew & Tackle Boxes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Select who's on the boat and which tackle box each person will use.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${tripFishermanIds.size} selected", style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { showAddFishermanDialog = true }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New fisherman")
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(sortedFishermen, key = { it.id }) { fisherman ->
                                val isSelected = fisherman.id in tripFishermanIds
                                FishermanCrewRow(
                                    fisherman = fisherman,
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        tripFishermanIds = if (checked) tripFishermanIds + fisherman.id
                                        else {
                                            tripTackleBoxSelections = tripTackleBoxSelections - fisherman.id
                                            tripFishermanIds - fisherman.id
                                        }
                                    },
                                    selectedTackleBoxId = tripTackleBoxSelections[fisherman.id],
                                    onTackleBoxSelected = { boxId ->
                                        tripTackleBoxSelections = tripTackleBoxSelections + (fisherman.id to boxId)
                                    },
                                    tripViewModel = tripViewModel,
                                    enabled = isSelected
                                )
                                HorizontalDivider()
                            }
                            if (sortedFishermen.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("No fishermen yet. Add one above.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val tripId = newTripId ?: return@launch

                                    // The databases for trip fishmen and segment fishermen may have
                                    // already been written.  If a fisherman was removed from the trip,
                                    // it needs to be removed from the trip as well as each segment
                                    viewModel.removeTripFishermenNotInSet(
                                        tripId = tripId,
                                        newSet = tripFishermanIds)

                                    committedSegments = committedSegments.map { segment ->
                                        segment.copy(
                                            // Keep only IDs that still exist in the master trip list
                                            fishermanIds = segment.fishermanIds intersect tripFishermanIds,

                                            // You should also filter the tackleBoxSelections map
                                            // so you don't keep gear data for people who aren't there
                                            tackleBoxSelections = segment.tackleBoxSelections.filterKeys { it in tripFishermanIds }
                                        )
                                    }
                                    committedSegments.forEach { segment ->
                                        viewModel.removeSegmentFishermenNotInSet(
                                            segmentId = segment.id,
                                            newSet = tripFishermanIds)
                                    }

                                    // TODO - need to 'sync' the tripFisherman or simply delete all the trip entries first
                                    tripFishermanIds.forEach { fishermanId ->
                                        viewModel.upsertTripFishermanCrossRef(
                                            tripId = tripId,
                                            fishermanId = fishermanId,
                                            tackleBoxId = tripTackleBoxSelections[fishermanId])
                                    }
                                    // Seed segment crew and tackle box defaults from trip selections
                                    segmentFishermanIds = tripFishermanIds
                                    segmentTackleBoxSelections = tripTackleBoxSelections
                                    segmentName = ""
                                    segmentStart = tripStart
                                    segmentEnd = tripEnd
                                    segmentLat = null; segmentLng = null

                                    if (fromReview) {
                                        currentStep = WizardStep.Review
                                    } else {
                                        currentStep = WizardStep.SegmentInfo
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (fromReview) {
                                Text("Next: Review")
                            } else {
                                Text("Next: Add First Segment")
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    if (showAddFishermanDialog) {
                        AddFishermanDialog(
                            onDismiss = { showAddFishermanDialog = false },
                            onAdd = { first, last, nick ->
                                scope.launch { viewModel.addFisherman(first, last, nick) }
                                showAddFishermanDialog = false
                            }
                        )
                    }
                }

                // ── Step 3: Segment info ─────────────────────────────────────
                WizardStep.SegmentInfo -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
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

                        // TODO -- commit segment information here just like trip steps
                        Button(
                            onClick = { currentStep = WizardStep.SegmentCrew },
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
                    // Only trip crew members are eligible for segment
                    val eligibleFishermen = remember(sortedFishermen, tripFishermanIds) {
                        sortedFishermen.filter { it.id in tripFishermanIds }
                    }

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Segment Crew & Tackle Boxes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Who's fishing \"$segmentName\"? Tackle boxes default to trip selections.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(eligibleFishermen, key = { it.id }) { fisherman ->
                                val isSelected = fisherman.id in segmentFishermanIds
                                FishermanCrewRow(
                                    fisherman = fisherman,
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        segmentFishermanIds = if (checked) segmentFishermanIds + fisherman.id
                                        else {
                                            segmentTackleBoxSelections = segmentTackleBoxSelections - fisherman.id
                                            segmentFishermanIds - fisherman.id
                                        }
                                    },
                                    selectedTackleBoxId = segmentTackleBoxSelections[fisherman.id],
                                    onTackleBoxSelected = { boxId ->
                                        segmentTackleBoxSelections = segmentTackleBoxSelections + (fisherman.id to boxId)
                                    },
                                    tripViewModel = tripViewModel,
                                    enabled = isSelected
                                )
                                HorizontalDivider()
                            }
                        }

                        Button(
                            onClick = {
                                // Commit segment to DB
                                scope.launch {
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

                                    // The databases for segment fishermen may have already been
                                    // written.  If a fisherman was removed from the segment,
                                    // it needs to be removed from the database
                                    viewModel.removeSegmentFishermenNotInSet(
                                        segmentId = segment.id,
                                        newSet = segmentFishermanIds)

                                    // Save tackle box selections for this segment
                                    segmentFishermanIds.forEach { fishermanId ->
                                        viewModel.upsertSegmentFishermanCrossRef(
                                            segmentId = segment.id,
                                            fishermanId = fishermanId,
                                            tackleBoxId = segmentTackleBoxSelections[fishermanId])
                                    }

                                    val existing = committedSegments.find { it.id == newSegmentId }

                                    if (existing != null) {
                                        committedSegments = committedSegments - existing;
                                    }

                                    committedSegments = committedSegments + SegmentDraft(
                                        id = segment.id,
                                        name = segmentName,
                                        startTime = segmentStart,
                                        endTime = segmentEnd,
                                        latitude = segmentLat,
                                        longitude = segmentLng,
                                        fishermanIds = segmentFishermanIds,
                                        tackleBoxSelections = segmentTackleBoxSelections
                                    )

                                    currentStep = WizardStep.Review
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.padding(end = 8.dp))
                            Text("Save Segment")
                        }
                    }
                }

                // ── Step 5: Review ───────────────────────────────────────────
                WizardStep.Review -> {
                    val fmt = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
                    val shortFmt = remember { SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()) }

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Review Trip", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        // Trip summary card
                        // TODO - look into using same card (TripItem) from TripListScreen
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .clickable() {
                                fromReview = true
                                currentStep = WizardStep.TripInfo
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        tripName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    if (tripLat != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Trip Location",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text("${fmt.format(Date(tripStart))}  →  ${fmt.format(Date(tripEnd))}",
                                    style = MaterialTheme.typography.bodyMedium)
                                Text(text = "${tripFishermanIds.size} ${if (tripFishermanIds.size == 1) "fisherman" else "fishermen"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Segments (${committedSegments.size})", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = {
                                // Reset segment fields for a new one
                                newSegmentId = UUID.randomUUID().toString()
                                segmentName = ""
                                segmentStart = tripStart
                                segmentEnd = tripEnd
                                segmentLat = null; segmentLng = null
                                segmentFishermanIds = tripFishermanIds
                                segmentTackleBoxSelections = tripTackleBoxSelections
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
                                    .clickable() {
                                        fromReview = true

                                        newSegmentId = seg.id
                                        segmentName = seg.name
                                        segmentStart = seg.startTime
                                        segmentEnd = seg.endTime
                                        segmentLat = seg.latitude
                                        segmentLng = seg.longitude
                                        segmentFishermanIds = seg.fishermanIds
                                        segmentTackleBoxSelections = seg.tackleBoxSelections

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

@Composable
private fun FishermanCheckRow(
    fisherman: Fisherman,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text(fisherman.fullName, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FishermanCrewRow(
    fisherman: Fisherman,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    selectedTackleBoxId: String?,
    onTackleBoxSelected: (String) -> Unit,
    tripViewModel: TripViewModel,
    enabled: Boolean
) {
    val availableBoxes by tripViewModel.getTackleBoxesForFisherman(fisherman.id)
        .collectAsState(initial = emptyList())
    val lureCount by tripViewModel.getLureCountForTackleBox(selectedTackleBoxId)
        .collectAsState(initial = 0)
    val selectedBox = availableBoxes.find { it.id == selectedTackleBoxId }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        // Fisherman checkbox row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
            Spacer(Modifier.width(8.dp))
            Text(
                text = fisherman.fullName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        // Tackle box dropdown — only shown when fisherman is selected
        if (enabled) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { if (availableBoxes.isNotEmpty()) dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.padding(start = 56.dp, end = 8.dp, bottom = 4.dp)
            ) {
                OutlinedTextField(
                    value = selectedBox?.name ?: if (availableBoxes.isEmpty()) "No boxes available" else "Select tackle box",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tackle Box") },
                    trailingIcon = {
                        if (availableBoxes.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    },
                    supportingText = if (selectedTackleBoxId != null) {
                        { Text("$lureCount lure${if (lureCount != 1) "s" else ""}") }
                    } else null,
                    enabled = availableBoxes.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                if (availableBoxes.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        availableBoxes.forEach { box ->
                            DropdownMenuItem(
                                text = { Text(box.name) },
                                onClick = {
                                    onTackleBoxSelected(box.id)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFishermanDialog(
    onDismiss: () -> Unit,
    onAdd: (firstName: String, lastName: String, nickname: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fisherman") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Nickname (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(firstName, lastName, nickname) },
                enabled = firstName.isNotBlank() || lastName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
