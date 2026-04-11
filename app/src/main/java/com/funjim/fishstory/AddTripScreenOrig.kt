package com.funjim.fishstory

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.DateTimePickerButton
import com.funjim.fishstory.ui.SegmentItem
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    viewModel: MainViewModel,
    navigateBack: () -> Unit,
    navigateToLoadBoatForTrip: () -> Unit,
    navigateToAddSegment: (String) -> Unit,
    navigateToDraftSegmentDetails: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val draftTripId by viewModel.draftTripId.collectAsState()
    val draftTripName by viewModel.draftTripName.collectAsState()
    val draftTripStartDate by viewModel.draftTripStartDate.collectAsState()
    val draftTripEndDate by viewModel.draftTripEndDate.collectAsState()
    val draftLatitude by viewModel.draftLatitude.collectAsState()
    val draftLongitude by viewModel.draftLongitude.collectAsState()

    var tripId by remember(draftTripId) {
        mutableStateOf(draftTripId)
    }
    var name by remember(draftTripName) {
        mutableStateOf(draftTripName)
    }
    var startDateMillis by remember(draftTripStartDate) {
        mutableLongStateOf( draftTripStartDate)
    }
    var endDateMillis by remember(draftTripEndDate) {
        mutableLongStateOf(draftTripEndDate)
    }
    var latitude by remember(draftLatitude) {
        mutableStateOf(draftLatitude)
    }
    var longitude by remember(draftLongitude) {
        mutableStateOf(draftLongitude)
    }

    LaunchedEffect(Unit) {
        if (tripId.isEmpty()) {
            viewModel.prepareNewTrip()
        }
    }

    val draftSegments by viewModel.draftSegments.collectAsState()
    val draftFishermanIds by viewModel.draftFishermanIds.collectAsState()
    val draftSegmentFishermanIds by viewModel.draftSegmentFishermanIds.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            scope.launch {
                val location = viewModel.getCurrentLocation(context)
                if (location != null) {
                    viewModel.updateDraftLocation(location.latitude, location.longitude)
                    latitude = location.latitude
                    longitude = location.longitude
                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = latitude,  // Passed from your DB object
        existingLng = longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            viewModel.updateDraftLocation(lat, lng)
            latitude = lat
            longitude = lng
        }
    )

    var segmentToUpdateLocation by remember { mutableStateOf<Segment?>(null) }

    val locationPickerSegment = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = segmentToUpdateLocation?.latitude,  // Passed from your DB object
        existingLng = segmentToUpdateLocation?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            segmentToUpdateLocation?.let { segment ->
                scope.launch {
                    viewModel.upsertDraftSegment(segment.copy(latitude = lat, longitude = lng))
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Trip") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearDrafts()
                        navigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Use Current Location") },
                                onClick = {
                                    menuExpanded = false
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        scope.launch {
                                            val location = viewModel.getCurrentLocation(context)
                                            if (location != null) {
                                                viewModel.updateDraftLocation(location.latitude, location.longitude)
                                                latitude = location.latitude
                                                longitude = location.longitude
                                                Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Select Location") },
                                onClick = {
                                    menuExpanded = false
                                    locationPicker.openPicker()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )

                            if (latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            viewModel.updateDraftLocation(null, null)
                                            latitude = null
                                            longitude = null
                                            Toast.makeText(context, "Location cleared", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (name.isNotBlank() && tripId.isNotEmpty()) {
                FloatingActionButton(onClick = {
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

                        // Save trip-level boat load
                        draftFishermanIds.forEach { fishermanId ->
                            viewModel.addFishermanToTrip(tripId, fishermanId)
                        }

                        // Save draft segments and their specific boat loads
                        draftSegments.forEach { draft ->
                            val segmentFishermanIds = draftSegmentFishermanIds[draft.id] ?: emptySet()
                            viewModel.addSegmentWithFishermen(
                                draft,
                                segmentFishermanIds
                            )
                        }

                        viewModel.clearDrafts()
                        navigateBack()
                    }
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Save Trip")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = name,
                onValueChange = {
                    name = it
                    viewModel.updateDraftTripName(it)
                },
                label = { Text("Trip Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                DateTimePickerButton(
                    label = "start",
                    millis = startDateMillis,
                    modifier = Modifier.weight(1f)
                ) { newMillis ->
                    startDateMillis = newMillis
                    if (startDateMillis > endDateMillis) {
                        endDateMillis = startDateMillis
                        viewModel.updateDraftTripEndDate(endDateMillis)
                    }
                    viewModel.updateDraftTripStartDate(startDateMillis)
                    viewModel.updateDraftSegmentStartDate(startDateMillis)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                DateTimePickerButton(
                    label = "end",
                    millis = endDateMillis,
                    modifier = Modifier.weight(1f)
                ) { newMillis ->
                    if (newMillis < startDateMillis) {
                        Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        endDateMillis = newMillis
                        viewModel.updateDraftTripEndDate(endDateMillis)
                        viewModel.updateDraftSegmentEndDate(endDateMillis)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val fishermanCount = draftFishermanIds.size
            BoatSummary(
                fishermanCount = fishermanCount,
                onBoatClick = {
                    viewModel.updateDraftTripName(name)
                    viewModel.updateDraftLocation(latitude, longitude)
                    navigateToLoadBoatForTrip()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Segments", style = MaterialTheme.typography.titleLarge)

                IconButton(onClick = {
                    viewModel.clearDraftSegment() // Ensure a clean slate for the new segment
                    viewModel.updateDraftSegmentStartDate(startDateMillis)
                    viewModel.updateDraftSegmentEndDate(endDateMillis)
                    viewModel.updateDraftSegmentId(UUID.randomUUID().toString())
                    viewModel.setDraftSegmentFisherman(draftFishermanIds)
                    navigateToAddSegment(tripId)
                },
                    enabled = tripId.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Segment")
                }
            }

            val segmentsToDisplay = draftSegments

            if (segmentsToDisplay.isEmpty()) {
                Text("No segments added yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(segmentsToDisplay) { segment ->
                        val fishermenCount = draftSegmentFishermanIds[segment.id]?.size ?: 0

                        SegmentItem(
                            segment = segment,
                            fishermenCount = fishermenCount,
                            onDelete = {
                                viewModel.removeDraftSegment(segment)
                            },
                            onClick = {
                                navigateToDraftSegmentDetails(segment.id)
                            },
                            onSetLocation = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    scope.launch {
                                        val location = viewModel.getCurrentLocation(context)
                                        if (location != null) {
                                            viewModel.upsertDraftSegment(segment.copy(latitude = location.latitude, longitude = location.longitude))
                                            Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            onSelectLocation = {
                                segmentToUpdateLocation = segment
                                locationPickerSegment.openPicker()
                            },
                            onUseTripLocation = if (latitude != null) {
                                {
                                    scope.launch {
                                        viewModel.upsertDraftSegment(
                                            segment.copy(
                                                latitude = latitude,
                                                longitude = longitude
                                            )
                                        )
                                    }
                                }
                            } else null,
                            onClearLocation = {
                                scope.launch {
                                    viewModel.upsertDraftSegment(segment.copy(latitude = null, longitude = null))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
