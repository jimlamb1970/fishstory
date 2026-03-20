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
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.SegmentDialog
import com.funjim.fishstory.ui.SegmentItem
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    viewModel: MainViewModel,
    initialTripId: Int = 0,
    navigateBack: () -> Unit,
    navigateToBoatLoad: (Int) -> Unit,
    navigateToSegmentDetails: (Int, Int) -> Unit
) {
    var tripId by remember { mutableIntStateOf(initialTripId) }
    var showDatePicker by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // For existing trips
    val tripWithDetails by if (tripId != 0) {
        viewModel.getTripWithDetails(tripId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var name by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(tripWithDetails) {
        tripWithDetails?.let {
            name = it.trip.name
            dateMillis = it.trip.startDate
            latitude = it.trip.latitude
            longitude = it.trip.longitude
        }
    }

    // For new trips (drafts)
    val draftSegments by viewModel.draftSegments.collectAsState()
    val draftFishermanIds by viewModel.draftFishermanIds.collectAsState()

    val dateFormatter = remember { 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

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
                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tripId == 0) "Add New Trip" else "Edit Trip") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearDrafts()
                        navigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navigateToBoatLoad(tripId) }) {
                        Icon(Icons.Default.DirectionsBoat, contentDescription = "Load Boat")
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Set GPS Location") },
                                onClick = {
                                    menuExpanded = false
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        scope.launch {
                                            val location = viewModel.getCurrentLocation(context)
                                            if (location != null) {
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
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (name.isNotBlank()) {
                FloatingActionButton(onClick = {
                    scope.launch {
                        val trip = Trip(
                            id = tripId, 
                            name = name, 
                            startDate = dateMillis,
                            latitude = latitude,
                            longitude = longitude
                        )
                        
                        if (tripId == 0) {
                            val id = viewModel.addTrip(trip)
                            val actualTripId = id.toInt()
                            // Save draft segments
                            draftSegments.forEach { draft ->
                                viewModel.addSegment(draft.copy(tripId = actualTripId))
                            }
                            // Save draft fishermen
                            draftFishermanIds.forEach { fishermanId ->
                                viewModel.addFishermanToTrip(actualTripId, fishermanId)
                            }
                            viewModel.clearDrafts()
                        } else {
                            viewModel.updateTrip(trip)
                        }
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
                onValueChange = { name = it },
                label = { Text("Trip Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Date: ${dateFormatter.format(Date(dateMillis))}")
            }

            if (latitude != null && longitude != null) {
                Text(
                    text = "Location: ${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Segments", style = MaterialTheme.typography.titleLarge)
                
                var showAddSegmentDialog by remember { mutableStateOf(false) }
                
                IconButton(onClick = { showAddSegmentDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Segment")
                }

                if (showAddSegmentDialog) {
                    SegmentDialog(
                        onDismiss = { showAddSegmentDialog = false },
                        onConfirm = { segmentName, startTime ->
                            if (tripId == 0) {
                                viewModel.addDraftSegment(segmentName, startTime)
                            } else {
                                scope.launch {
                                    viewModel.addSegment(Segment(tripId = tripId, name = segmentName, startTime = startTime))
                                }
                            }
                            showAddSegmentDialog = false
                        }
                    )
                }
            }

            // Display Segments (either draft or from DB)
            val segmentsToDisplay = if (tripId == 0) draftSegments else tripWithDetails?.segments ?: emptyList()

            if (segmentsToDisplay.isEmpty()) {
                Text("No segments added yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(segmentsToDisplay) { segment ->
                        SegmentItem(
                            segment = segment,
                            onEdit = { /* Edit logic */ },
                            onDelete = {
                                if (tripId == 0) {
                                    viewModel.removeDraftSegment(segment)
                                } else {
                                    scope.launch { viewModel.deleteSegment(segment) }
                                }
                            },
                            onClick = {
                                if (tripId != 0) {
                                    navigateToSegmentDetails(segment.id, tripId)
                                }
                            },
                            onSetLocation = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    scope.launch {
                                        val location = viewModel.getCurrentLocation(context)
                                        if (location != null) {
                                            if (tripId == 0) {
                                                viewModel.updateDraftSegment(segment.copy(latitude = location.latitude, longitude = location.longitude))
                                            } else {
                                                viewModel.updateSegment(segment.copy(latitude = location.latitude, longitude = location.longitude))
                                            }
                                            Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            }
                        )
                    }
                }
            }
            
            // Show boat summary
            val fishermanCount = if (tripId == 0) draftFishermanIds.size else tripWithDetails?.fishermen?.size ?: 0
            if (fishermanCount > 0) {
                Text(
                    "Boat contains $fishermanCount fisherman/men",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis = it }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
