package com.funjim.fishstory

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.SegmentDialog
import com.funjim.fishstory.ui.SegmentItem
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerButton(
    label: String,
    millis: Long,
    modifier: Modifier = Modifier,
    onConfirm: (Long) -> Unit
) {
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())
    }
    var showDateStep by remember { mutableStateOf(false) }
    var showTimeStep by remember { mutableStateOf(false) }
    var pendingMillis by remember { mutableLongStateOf(millis) }

    OutlinedButton(onClick = { showDateStep = true }, modifier = modifier) {
        Text(dateTimeFormatter.format(Date(millis)))
    }

    if (showDateStep) {
        val localCal = Calendar.getInstance()
        localCal.timeInMillis = millis
        // Strip time in UTC (DatePicker expects UTC midnight for the selected date)
        val startOfDayUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(localCal.get(Calendar.YEAR), localCal.get(Calendar.MONTH), localCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startOfDayUtc)

        DatePickerDialog(
            onDismissRequest = { showDateStep = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { newDate ->
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        utcCal.timeInMillis = newDate
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        cal.set(
                            utcCal.get(Calendar.YEAR),
                            utcCal.get(Calendar.MONTH),
                            utcCal.get(Calendar.DAY_OF_MONTH)
                        )
                        pendingMillis = cal.timeInMillis
                    }
                    showDateStep = false
                    showTimeStep = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDateStep = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimeStep) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = pendingMillis
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimeStep = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select $label time",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimeStep = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            onConfirm(cal.timeInMillis)
                            showTimeStep = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }
}

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
    var menuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tripWithDetails by if (tripId != 0) {
        viewModel.getTripWithDetails(tripId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    val draftTripName by viewModel.draftTripName.collectAsState()
    val draftTripStartDate by viewModel.draftTripStartDate.collectAsState()
    val draftTripEndDate by viewModel.draftTripEndDate.collectAsState()
    val draftLatitude by viewModel.draftLatitude.collectAsState()
    val draftLongitude by viewModel.draftLongitude.collectAsState()

    var name by remember(draftTripName, tripWithDetails) {
        mutableStateOf(tripWithDetails?.trip?.name ?: draftTripName)
    }
    var startDateMillis by remember(draftTripStartDate, tripWithDetails) {
        mutableLongStateOf(tripWithDetails?.trip?.startDate ?: draftTripStartDate)
    }
    var endDateMillis by remember(draftTripEndDate, tripWithDetails) {
        mutableLongStateOf(tripWithDetails?.trip?.endDate ?: draftTripEndDate)
    }
    var latitude by remember(draftLatitude, tripWithDetails) {
        mutableStateOf(tripWithDetails?.trip?.latitude ?: draftLatitude)
    }
    var longitude by remember(draftLongitude, tripWithDetails) {
        mutableStateOf(tripWithDetails?.trip?.longitude ?: draftLongitude)
    }

    val draftSegments by viewModel.draftSegments.collectAsState()
    val draftFishermanIds by viewModel.draftFishermanIds.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            scope.launch {
                val location = viewModel.getCurrentLocation(context)
                if (location != null) {
                    if (tripId == 0) {
                        viewModel.updateDraftLocation(location.latitude, location.longitude)
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
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
                                                if (tripId == 0) {
                                                    viewModel.updateDraftLocation(location.latitude, location.longitude)
                                                } else {
                                                    latitude = location.latitude
                                                    longitude = location.longitude
                                                }
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
                            startDate = startDateMillis,
                            endDate = endDateMillis,
                            latitude = latitude,
                            longitude = longitude
                        )
                        if (tripId == 0) {
                            val id = viewModel.addTrip(trip)
                            val actualTripId = id.toInt()
                            draftSegments.forEach { draft ->
                                viewModel.addSegment(draft.copy(tripId = actualTripId))
                            }
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
                onValueChange = {
                    name = it
                    if (tripId == 0) viewModel.updateDraftTripName(it)
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
                        if (tripId == 0) viewModel.updateDraftTripEndDate(endDateMillis)
                    }
                    if (tripId == 0) viewModel.updateDraftTripStartDate(startDateMillis)
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
                        if (tripId == 0) viewModel.updateDraftTripEndDate(endDateMillis)
                    }
                }
            }

            if (latitude != null && longitude != null) {
                Text(
                    text = "Location: ${"%.4f".format(latitude)}, ${"%.4f".format(longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            val fishermanCount = if (tripId == 0) draftFishermanIds.size else tripWithDetails?.fishermen?.size ?: 0
            BoatSummary(
                fishermanCount = fishermanCount,
                onBoatClick = {
                    if (tripId == 0) {
                        viewModel.updateDraftTripName(name)
                        viewModel.updateDraftTripStartDate(startDateMillis)
                        viewModel.updateDraftTripEndDate(endDateMillis)
                        viewModel.updateDraftLocation(latitude, longitude)
                    }
                    navigateToBoatLoad(tripId)
                },
                onAddClick = {
                    if (tripId == 0) {
                        viewModel.updateDraftTripName(name)
                        viewModel.updateDraftTripStartDate(startDateMillis)
                        viewModel.updateDraftTripEndDate(endDateMillis)
                        viewModel.updateDraftLocation(latitude, longitude)
                    }
                    navigateToBoatLoad(tripId)
                }
            )

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
        }
    }
}
