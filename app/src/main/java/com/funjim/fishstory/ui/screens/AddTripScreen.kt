package com.funjim.fishstory

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.funjim.fishstory.ui.SegmentItem
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------------------------------------
// Wizard step enum
// ---------------------------------------------------------------------------
private enum class WizardStep {
    TripInfo,       // Step 1: name, dates, location
    TripCrew,       // Step 2: fishermen + tackle boxes for trip
    SegmentInfo,    // Step 3: segment name, dates, location
    SegmentCrew,    // Step 4: fishermen + tackle boxes for segment
    Review          // Step 5: review all segments, add another or finish
}

// ---------------------------------------------------------------------------
// AddTripScreen – wizard
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreenNew(
    viewModel: MainViewModel,
    navigateBack: () -> Unit,
    // These params kept for nav-compat but unused inside wizard
    navigateToLoadBoatForTrip: () -> Unit = {},
    navigateToAddSegment: (String) -> Unit = {},
    navigateToDraftSegmentDetails: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Draft state from ViewModel
    val draftTripId by viewModel.draftTripId.collectAsState()
    val draftTripName by viewModel.draftTripName.collectAsState()
    val draftTripStartDate by viewModel.draftTripStartDate.collectAsState()
    val draftTripEndDate by viewModel.draftTripEndDate.collectAsState()
    val draftLatitude by viewModel.draftLatitude.collectAsState()
    val draftLongitude by viewModel.draftLongitude.collectAsState()
    val draftSegments by viewModel.draftSegments.collectAsState()
    val draftFishermanIds by viewModel.draftFishermanIds.collectAsState()
    val draftSegmentFishermanIds by viewModel.draftSegmentFishermanIds.collectAsState()
    val draftSegmentId by viewModel.draftSegmentId.collectAsStateWithLifecycle()
    val allFishermen by viewModel.fishermen.collectAsState(initial = emptyList())

    // Local mirrors
    var tripId by remember(draftTripId) { mutableStateOf(draftTripId) }
    var name by remember(draftTripName) { mutableStateOf(draftTripName) }
    var startDateMillis by remember(draftTripStartDate) { mutableLongStateOf(draftTripStartDate) }
    var endDateMillis by remember(draftTripEndDate) { mutableLongStateOf(draftTripEndDate) }
    var latitude by remember(draftLatitude) { mutableStateOf(draftLatitude) }
    var longitude by remember(draftLongitude) { mutableStateOf(draftLongitude) }

    // Segment local state
    var segmentName by remember { mutableStateOf("") }
    var segmentStartMillis by remember { mutableLongStateOf(draftTripStartDate) }
    var segmentEndMillis by remember { mutableLongStateOf(draftTripEndDate) }
    var segmentLatitude by remember { mutableStateOf<Double?>(null) }
    var segmentLongitude by remember { mutableStateOf<Double?>(null) }

    // Wizard step
    var currentStep by remember { mutableStateOf(WizardStep.TripInfo) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Init a fresh trip id on first composition
    LaunchedEffect(Unit) {
        if (tripId.isEmpty()) viewModel.prepareNewTrip()
    }

    // Keep local tripId in sync
    LaunchedEffect(draftTripId) { if (draftTripId.isNotEmpty()) tripId = draftTripId }

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = latitude,
        existingLng = longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            viewModel.updateDraftLocation(lat, lng)
            latitude = lat; longitude = lng
        }
    )

    val segmentLocationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = segmentLatitude,
        existingLng = segmentLongitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            segmentLatitude = lat; segmentLongitude = lng
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value }) {
            scope.launch {
                viewModel.getCurrentLocation(context)?.let { loc ->
                    when (currentStep) {
                        WizardStep.TripInfo -> {
                            viewModel.updateDraftLocation(loc.latitude, loc.longitude)
                            latitude = loc.latitude; longitude = loc.longitude
                        }
                        WizardStep.SegmentInfo -> {
                            segmentLatitude = loc.latitude; segmentLongitude = loc.longitude
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    // Step labels for the progress indicator
    val stepLabels = listOf("Trip", "Crew", "Segment", "Seg. Crew", "Review")
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
                            WizardStep.TripInfo -> { viewModel.clearDrafts(); navigateBack() }
                            WizardStep.TripCrew -> currentStep = WizardStep.TripInfo
                            WizardStep.SegmentInfo -> currentStep = WizardStep.Review
                            WizardStep.SegmentCrew -> currentStep = WizardStep.SegmentInfo
                            WizardStep.Review -> currentStep = WizardStep.TripCrew
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Location menu on steps that support it
                    if (currentStep == WizardStep.TripInfo || currentStep == WizardStep.SegmentInfo) {
                        val hasLocation = if (currentStep == WizardStep.TripInfo) latitude != null else segmentLatitude != null
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = if (hasLocation) Color(0xFF4CAF50) else LocalContentColor.current
                                )
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Use Current Location") },
                                    onClick = {
                                        menuExpanded = false
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                            scope.launch {
                                                viewModel.getCurrentLocation(context)?.let { loc ->
                                                    if (currentStep == WizardStep.TripInfo) {
                                                        viewModel.updateDraftLocation(loc.latitude, loc.longitude)
                                                        latitude = loc.latitude; longitude = loc.longitude
                                                    } else {
                                                        segmentLatitude = loc.latitude; segmentLongitude = loc.longitude
                                                    }
                                                } ?: Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                                )
                                if (currentStep == WizardStep.SegmentInfo && latitude != null) {
                                    DropdownMenuItem(
                                        text = { Text("Use Trip Location") },
                                        onClick = {
                                            menuExpanded = false
                                            segmentLatitude = latitude; segmentLongitude = longitude
                                        },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Select on Map") },
                                    onClick = {
                                        menuExpanded = false
                                        if (currentStep == WizardStep.TripInfo) locationPicker.openPicker()
                                        else segmentLocationPicker.openPicker()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Map, null) }
                                )
                                if (hasLocation) {
                                    DropdownMenuItem(
                                        text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            menuExpanded = false
                                            if (currentStep == WizardStep.TripInfo) {
                                                viewModel.updateDraftLocation(null, null)
                                                latitude = null; longitude = null
                                            } else {
                                                segmentLatitude = null; segmentLongitude = null
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->

        // Step progress bar
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LinearProgressIndicator(
                progress = { (stepIndex + 1f) / stepLabels.size },
                modifier = Modifier.fillMaxWidth()
            )

            when (currentStep) {

                // ── Step 1: Trip info ───────────────────────────────────────
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
                            value = name,
                            onValueChange = { name = it; viewModel.updateDraftTripName(it) },
                            label = { Text("Trip Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "start", millis = startDateMillis, modifier = Modifier.weight(1f)) { newMillis ->
                                startDateMillis = newMillis
                                viewModel.updateDraftTripStartDate(newMillis)
                                if (newMillis > endDateMillis) {
                                    endDateMillis = newMillis
                                    viewModel.updateDraftTripEndDate(newMillis)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "end", millis = endDateMillis, modifier = Modifier.weight(1f)) { newMillis ->
                                if (newMillis < startDateMillis) {
                                    Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                } else {
                                    endDateMillis = newMillis
                                    viewModel.updateDraftTripEndDate(newMillis)
                                }
                            }
                        }

                        if (latitude != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Location set", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = { currentStep = WizardStep.TripCrew },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next: Select Crew")
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                // ── Step 2: Trip crew ───────────────────────────────────────
                WizardStep.TripCrew -> {
                    val sortedFishermen = remember(allFishermen) { allFishermen.sortedBy { it.fullName } }
                    var addFishermanDialog by remember { mutableStateOf(false) }

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Crew for Trip", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Choose who's on the boat for this trip.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${draftFishermanIds.size} selected", style = MaterialTheme.typography.labelMedium)
                            TextButton(onClick = { addFishermanDialog = true }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add new fisherman")
                            }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(sortedFishermen, key = { it.id }) { fisherman ->
                                val checked = fisherman.id in draftFishermanIds
                                FishermanCheckRow(
                                    fisherman = fisherman,
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) viewModel.addDraftFisherman(fisherman.id)
                                        else viewModel.removeDraftFisherman(fisherman.id)
                                    }
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
                                segmentName = ""
                                segmentStartMillis = startDateMillis
                                segmentEndMillis = endDateMillis
                                segmentLatitude = null; segmentLongitude = null
                                val newSegId = UUID.randomUUID().toString()
                                viewModel.updateDraftSegmentId(newSegId)
                                viewModel.updateDraftSegmentName("")
                                viewModel.updateDraftSegmentStartDate(startDateMillis)
                                viewModel.updateDraftSegmentEndDate(endDateMillis)
                                viewModel.setDraftSegmentFisherman(draftFishermanIds)
                                currentStep = WizardStep.SegmentInfo
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next: Add First Segment")
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }

                    if (addFishermanDialog) {
                        AddFishermanDialogNew(
                            onDismiss = { addFishermanDialog = false },
                            onAdd = { firstName, lastName, nickname ->
                                scope.launch { viewModel.addFisherman(firstName, lastName, nickname) }
                                addFishermanDialog = false
                            }
                        )
                    }
                }

                // ── Step 3: Segment info ────────────────────────────────────
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
                            "A segment is a single fishing session within the trip (e.g. morning run, afternoon drift).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = segmentName,
                            onValueChange = { segmentName = it; viewModel.updateDraftSegmentName(it) },
                            label = { Text("Segment Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "start", millis = segmentStartMillis, modifier = Modifier.weight(1f)) { newMillis ->
                                if (newMillis < startDateMillis) {
                                    Toast.makeText(context, "Cannot be before trip start", Toast.LENGTH_SHORT).show()
                                } else {
                                    segmentStartMillis = newMillis
                                    viewModel.updateDraftSegmentStartDate(newMillis)
                                    if (newMillis > segmentEndMillis) { segmentEndMillis = newMillis; viewModel.updateDraftSegmentEndDate(newMillis) }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                            DateTimePickerButton(label = "end", millis = segmentEndMillis, modifier = Modifier.weight(1f)) { newMillis ->
                                when {
                                    newMillis < segmentStartMillis -> Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                    newMillis > endDateMillis -> Toast.makeText(context, "Cannot be after trip end", Toast.LENGTH_SHORT).show()
                                    else -> { segmentEndMillis = newMillis; viewModel.updateDraftSegmentEndDate(newMillis) }
                                }
                            }
                        }

                        if (segmentLatitude != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Location set", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = { currentStep = WizardStep.SegmentCrew },
                            enabled = segmentName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next: Select Segment Crew")
                            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                // ── Step 4: Segment crew ────────────────────────────────────
                WizardStep.SegmentCrew -> {
                    // Only show fishermen who are on the trip
                    val eligibleFishermen = remember(allFishermen, draftFishermanIds) {
                        allFishermen.filter { it.id in draftFishermanIds }.sortedBy { it.fullName }
                    }
                    val segmentCrewIds = draftSegmentFishermanIds[draftSegmentId] ?: draftFishermanIds

                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Segment Crew", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Choose who's fishing this segment. Defaults to all trip crew.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("\"${segmentName}\"", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(eligibleFishermen, key = { it.id }) { fisherman ->
                                val checked = fisherman.id in segmentCrewIds
                                FishermanCheckRow(
                                    fisherman = fisherman,
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) viewModel.addDraftSegmentFisherman(draftSegmentId, fisherman.id)
                                        else viewModel.removeDraftSegmentFisherman(draftSegmentId, fisherman.id)
                                    }
                                )
                                HorizontalDivider()
                            }
                        }

                        Button(
                            onClick = {
                                // Commit segment to draft list
                                val finalCrewIds = draftSegmentFishermanIds[draftSegmentId] ?: draftFishermanIds
                                val segment = Segment(
                                    id = draftSegmentId,
                                    tripId = tripId,
                                    name = segmentName,
                                    startTime = segmentStartMillis,
                                    endTime = segmentEndMillis,
                                    latitude = segmentLatitude,
                                    longitude = segmentLongitude
                                )
                                viewModel.upsertDraftSegment(segment)
                                currentStep = WizardStep.Review
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Segment")
                            Icon(Icons.Default.Check, null, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }

                // ── Step 5: Review ──────────────────────────────────────────
                WizardStep.Review -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Review Trip", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                        // Trip summary card
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                val fmt = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
                                Text("${fmt.format(Date(startDateMillis))} → ${fmt.format(Date(endDateMillis))}", style = MaterialTheme.typography.bodySmall)
                                Text("${draftFishermanIds.size} fishermen on trip", style = MaterialTheme.typography.bodySmall)
                                if (latitude != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                        Text(" Location set", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Segments (${draftSegments.size})", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = {
                                // Start a fresh segment draft
                                segmentName = ""
                                segmentStartMillis = startDateMillis
                                segmentEndMillis = endDateMillis
                                segmentLatitude = null; segmentLongitude = null
                                val newSegId = UUID.randomUUID().toString()
                                viewModel.updateDraftSegmentId(newSegId)
                                viewModel.updateDraftSegmentName("")
                                viewModel.updateDraftSegmentStartDate(startDateMillis)
                                viewModel.updateDraftSegmentEndDate(endDateMillis)
                                viewModel.setDraftSegmentFisherman(draftFishermanIds)
                                currentStep = WizardStep.SegmentInfo
                            }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add segment")
                            }
                        }

                        if (draftSegments.isEmpty()) {
                            Text("No segments added.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(draftSegments, key = { it.id }) { segment ->
                                val crewCount = draftSegmentFishermanIds[segment.id]?.size ?: 0
                                ReviewSegmentRow(segment = segment, crewCount = crewCount)
                                HorizontalDivider()
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val trip = Trip(
                                        id = tripId,
                                        name = name,
                                        startDate = startDateMillis,
                                        endDate = endDateMillis,
                                        latitude = latitude,
                                        longitude = longitude
                                    )
                                    viewModel.addTrip(trip)
                                    draftFishermanIds.forEach { fishermanId ->
                                        viewModel.addFishermanToTrip(tripId, fishermanId)
                                    }
                                    draftSegments.forEach { draft ->
                                        val segFishermen = draftSegmentFishermanIds[draft.id] ?: emptySet()
                                        viewModel.addSegmentWithFishermen(draft, segFishermen)
                                    }
                                    viewModel.clearDrafts()
                                    navigateBack()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = name.isNotBlank() && draftSegments.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.padding(end = 8.dp))
                            Text("Save Trip")
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearDrafts(); navigateBack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Discard")
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Shared composables
// ---------------------------------------------------------------------------

@Composable
private fun FishermanCheckRow(
    fisherman: Fisherman,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text(fisherman.fullName, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ReviewSegmentRow(segment: Segment, crewCount: Int) {
    val fmt = remember { SimpleDateFormat("MMM dd HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(segment.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                "${fmt.format(Date(segment.startTime))} → ${fmt.format(Date(segment.endTime))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("$crewCount fisherman${if (crewCount != 1) "en" else ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (segment.latitude != null) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AddFishermanDialogNew(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
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
