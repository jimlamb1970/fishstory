package com.funjim.fishstory

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSegmentScreen(
    viewModel: MainViewModel,
    tripId: String,
    navigateBack: () -> Unit,
    navigateToLoadBoatForSegment: () -> Unit
) {
    val draftTripId by viewModel.draftTripId.collectAsStateWithLifecycle()
    val draftSegmentId by viewModel.draftSegmentId.collectAsStateWithLifecycle()

    val tripWithDetails by if (tripId != draftTripId) {
        viewModel.getTripWithDetails(tripId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    // Draft values from ViewModel — survive navigation
    val draftSegmentName by viewModel.draftSegmentName.collectAsState()
    val draftSegmentStartDate by viewModel.draftSegmentStartDate.collectAsState()
    val draftSegmentEndDate by viewModel.draftSegmentEndDate.collectAsState()
    val draftSegmentLatitude by viewModel.draftSegmentLatitude.collectAsState()
    val draftSegmentLongitude by viewModel.draftSegmentLongitude.collectAsState()

    // Draft trip values for pre-populating dates when tripId == 0
    val draftTripStartDate by viewModel.draftTripStartDate.collectAsState()
    val draftTripEndDate by viewModel.draftTripEndDate.collectAsState()
    val draftFishermanIds by viewModel.draftFishermanIds.collectAsState()
    val draftSegmentFishermanIds by viewModel.draftSegmentFishermanIds.collectAsState()

    // Local state backed by ViewModel draft — survives navigation
    var name by remember(draftSegmentName) { mutableStateOf(draftSegmentName) }
    var startDateMillis by remember(draftSegmentStartDate) { mutableLongStateOf(draftSegmentStartDate) }
    var endDateMillis by remember(draftSegmentEndDate) { mutableLongStateOf(draftSegmentEndDate) }
    var latitude by remember(draftSegmentLatitude) { mutableStateOf(draftSegmentLatitude) }
    var longitude by remember(draftSegmentLongitude) { mutableStateOf(draftSegmentLongitude) }

    val eligibleIds: Set<String> = when {
        (tripId == draftTripId) -> draftFishermanIds
        else -> tripWithDetails?.fishermen?.map { it.id }?.toSet() ?: emptySet()
    }

    // Pre-populate the boat for this draft segment from trip fishermen (only once)
    LaunchedEffect(eligibleIds) {
        if (eligibleIds.isNotEmpty() &&
            viewModel.draftSegmentFishermanIds.value[draftSegmentId] == null) {
            eligibleIds.forEach { fishermanId ->
                viewModel.addDraftSegmentFisherman(draftSegmentId, fishermanId)
            }
        }
    }

    val fishermanCount = draftSegmentFishermanIds[draftSegmentId]?.size ?: 0

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            scope.launch {
                val location = viewModel.getCurrentLocation(context)
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    viewModel.updateDraftSegmentLocation(location.latitude, location.longitude)
                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPicker = rememberLocationPickerState(
        viewModel = viewModel,
        existingLat = latitude,  // Passed from your DB object
        existingLng = longitude,
        onLocationConfirmed = { lat, lng ->
            viewModel.updateDraftSegmentLocation(lat, lng)
            latitude = lat
            longitude = lng
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Segment") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearDraftSegment()
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
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        scope.launch {
                                            val location = viewModel.getCurrentLocation(context)
                                            if (location != null) {
                                                latitude = location.latitude
                                                longitude = location.longitude
                                                viewModel.updateDraftSegmentLocation(location.latitude, location.longitude)
                                                Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
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
                                },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Select Location") },
                                onClick = {
                                    menuExpanded = false
                                    locationPicker.openPicker()
                                },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                            )

                            if (latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            viewModel.updateDraftSegmentLocation(null, null)
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
            if (name.isNotBlank()) {
                FloatingActionButton(onClick = {
                    if (tripId == draftTripId) {
                        viewModel.addDraftSegment(
                            name = name,
                            startTime = startDateMillis,
                            endTime = endDateMillis,
                            latitude = latitude,
                            longitude = longitude
                        )
                        viewModel.clearDraftSegment()
                        navigateBack()
                    } else {
                        scope.launch {
                            val currentBoatFishermen = viewModel.draftSegmentFishermanIds.value[draftSegmentId] ?: eligibleIds
                            viewModel.addSegmentWithFishermen(
                                Segment(
                                    tripId = tripId,
                                    name = name,
                                    startTime = startDateMillis,
                                    endTime = endDateMillis,
                                    latitude = latitude,
                                    longitude = longitude
                                ),
                                currentBoatFishermen
                            )
                            viewModel.clearDraftSegment()
                            navigateBack()
                        }
                    }
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Save Segment")
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
                    viewModel.updateDraftSegmentName(it)
                },
                label = { Text("Segment Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Start", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                DateTimePickerButton(
                    label = "start",
                    millis = startDateMillis,
                    modifier = Modifier.weight(1f)
                ) { newMillis ->
                    if (newMillis < draftTripStartDate) {
                        Toast.makeText(context, "Start cannot be before trip start", Toast.LENGTH_SHORT)
                            .show()
                    } else if (newMillis > draftTripEndDate) {
                        Toast.makeText(context, "Start cannot be after trip end", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        startDateMillis = newMillis
                        viewModel.updateDraftSegmentStartDate(newMillis)
                        if (startDateMillis > endDateMillis) {
                            endDateMillis = startDateMillis
                            viewModel.updateDraftSegmentEndDate(startDateMillis)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("End", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(48.dp))
                DateTimePickerButton(
                    label = "end",
                    millis = endDateMillis,
                    modifier = Modifier.weight(1f)
                ) { newMillis ->
                    if (newMillis < startDateMillis) {
                        Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                    } else {
                        if (newMillis > draftTripEndDate) {
                            Toast.makeText(context, "End cannot be after trip end", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            endDateMillis = newMillis
                            viewModel.updateDraftSegmentEndDate(newMillis)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            BoatSummary(
                fishermanCount = fishermanCount,
                onBoatClick = { navigateToLoadBoatForSegment() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
