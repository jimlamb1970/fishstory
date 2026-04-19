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
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.TripViewModelCrewPickerBridge
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItem
import com.funjim.fishstory.ui.utils.TripMenu
import com.funjim.fishstory.ui.utils.hasLocationPermission
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.MainViewModel
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ---------------------------------------------------------------------------
// Wizard steps
// ---------------------------------------------------------------------------
private enum class SegmentWizardStep {
    SegmentInfo,        // Step 1 – segment name, dates, location
    SegmentCrew        // Step 2 – fishermen + tackle boxes for segment
}

// ---------------------------------------------------------------------------
// AddTripScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSegmentScreen(
    tripViewModel: TripViewModel,
    tripId: String,
    navigateBack: () -> Unit
) {
    val tripSummary by tripViewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val tripFishermen by tripViewModel.tripFishermen.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val now = remember { System.currentTimeMillis() }

    var newSegmentId by remember { mutableStateOf<String>(UUID.randomUUID().toString()) }

    LaunchedEffect(tripId) {
        tripViewModel.selectTrip(tripId)
        tripViewModel.selectSegment(newSegmentId)
    }

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val segmentTackleBoxMap by tripViewModel.segmentTackleBoxMap.collectAsState()

    var segmentName by remember { mutableStateOf("") }

    var segmentStart by remember(tripSummary) {
        mutableLongStateOf(tripSummary?.trip?.startDate ?: System.currentTimeMillis())
    }
    var segmentEnd by remember(tripSummary) {
        mutableLongStateOf(tripSummary?.trip?.startDate ?: System.currentTimeMillis())
    }
    var segmentLat by remember { mutableStateOf<Double?>(null) }
    var segmentLng by remember { mutableStateOf<Double?>(null) }

    var segmentFishermanIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // ── Wizard step ─────────────────────────────────────────────────────────
    var currentStep by remember { mutableStateOf(SegmentWizardStep.SegmentInfo) }

    var showTripMenu by remember { mutableStateOf(false) }

    var locationMenuExpanded by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by tripViewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = segmentLat,
        existingLng = segmentLng,
        onFetchLocation = { tripViewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng -> segmentLat = lat; segmentLng = lng }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value }) {
            scope.launch {
                tripViewModel.getCurrentLocation(context)?.let { loc ->
                    if (currentStep == SegmentWizardStep.SegmentInfo) {
                        segmentLat = loc.latitude; segmentLng = loc.longitude
                    }
                } ?: Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper: delete the segment row if user cancels mid-wizard
    fun cancelAndExit() {
        scope.launch {
            // CASCADE deletes will clean up fisherman cross-refs and segments
            tripViewModel.deleteSegment(newSegmentId)
        }
        navigateBack()
    }

    // Progress indicator
    val stepLabels = listOf("Segment", "Seg. Crew & Boxes")
    val stepIndex = currentStep.ordinal.coerceAtMost(stepLabels.lastIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("New Segment")
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
                            SegmentWizardStep.SegmentInfo    -> cancelAndExit()
                            SegmentWizardStep.SegmentCrew -> currentStep = SegmentWizardStep.SegmentInfo
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentStep == SegmentWizardStep.SegmentInfo) {
                        val hasLocation = segmentLat != null
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
                                                    segmentLat = loc.latitude; segmentLng = loc.longitude
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
                                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                        onClick = {
                                            locationMenuExpanded = false
                                            segmentLat = tripSummary?.trip?.latitude; segmentLng = tripSummary?.trip?.longitude
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Select on Map") },
                                    leadingIcon = { Icon(Icons.Default.Map, null) },
                                    onClick = {
                                        locationMenuExpanded = false
                                        locationPicker.openPicker()
                                    }
                                )
                                if (hasLocation) {
                                    DropdownMenuItem(
                                        text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.LocationOff, null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            locationMenuExpanded = false
                                            segmentLat = null; segmentLng = null
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
                    // ── Step 3: Segment info ─────────────────────────────────────
                    SegmentWizardStep.SegmentInfo -> {
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
                                        new < tripSummary.trip.startDate ->
                                            Toast.makeText(context, "Cannot be before trip start", Toast.LENGTH_SHORT).show()
                                        new > tripSummary.trip.endDate ->
                                            Toast.makeText(context, "Cannot be after trip end", Toast.LENGTH_SHORT).show()
                                        else -> { segmentStart = new; if (new > segmentEnd) segmentEnd = new }
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                                DateTimePickerButton(label = "end", millis = segmentEnd, modifier = Modifier.weight(1f)) { new ->
                                    when {
                                        new < segmentStart ->
                                            Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                        new > tripSummary.trip.endDate ->
                                            Toast.makeText(context, "Cannot be after trip end", Toast.LENGTH_SHORT).show()
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
                                            tripId = tripId,
                                            id = newSegmentId,
                                            name = segmentName,
                                            startTime = segmentStart,
                                            endTime = segmentEnd,
                                            latitude = segmentLat,
                                            longitude = segmentLng
                                        )
                                        tripViewModel.upsertSegment(segment)

                                        if (segmentTackleBoxMap.isEmpty()) {
                                            segmentFishermanIds.forEach { fishermanId ->
                                                tripViewModel.upsertSegmentFishermanCrossRef(
                                                    segmentId = newSegmentId,
                                                    fishermanId = fishermanId,
                                                    tackleBoxId = tripTackleBoxMap[fishermanId]
                                                )
                                            }
                                        }
                                    }

                                    currentStep = SegmentWizardStep.SegmentCrew
                                },
                                // TODO -- add extra check for dates
                                enabled = segmentName.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Next: Select Segment Crew")
                                Icon(Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    // ── Step 4: Segment crew + tackle boxes ──────────────────────
                    SegmentWizardStep.SegmentCrew -> {
                        Spacer(Modifier.height(16.dp))
                        TripViewModelCrewPickerBridge(
                            title = "Segment Crew & Tackle Boxes",
                            subtitle = "Who's fishing \"$segmentName\"? Tackle boxes default to trip selections.",
                            eligibleFishermen = tripFishermen,
                            selectedIds = segmentFishermanIds,
                            tackleBoxSelections = segmentTackleBoxMap,
                            onSelectionChanged = { fishermanId, selected ->
                                if (selected) {
                                    segmentFishermanIds = segmentFishermanIds + fishermanId
                                    tripViewModel.upsertSegmentFishermanCrossRef(
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
                                tripViewModel.upsertSegmentFishermanCrossRef(
                                    segmentId = newSegmentId,
                                    fishermanId = fishermanId,
                                    tackleBoxId = boxId
                                )
                            },
                            tripViewModel = tripViewModel,
                            confirmLabel = "Review",
                            onConfirm = { navigateBack() },
                            onAddTackleBox = { tackleBoxName, fishermanId ->
                                scope.launch { tripViewModel.createAndAssignSegmentTackleBox(
                                    fishermanId = fishermanId,
                                    segmentId = newSegmentId,
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
