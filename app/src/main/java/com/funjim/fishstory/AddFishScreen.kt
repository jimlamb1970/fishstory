package com.funjim.fishstory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.ui.DateTimeUtils.toLocalDateTime
import com.funjim.fishstory.ui.DateTimeUtils.updateDate
import com.funjim.fishstory.ui.DateTimeUtils.updateTime
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishScreen(
    viewModel: MainViewModel,
    tripId: Int,
    segmentId: Int,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Data from ViewModel
    val speciesList by viewModel.species.collectAsState(initial = emptyList())
    val tripDetails by produceState<com.funjim.fishstory.model.TripWithDetails?>(initialValue = null) {
        viewModel.getTripWithDetails(tripId).collect { value = it }
    }
    val segmentDetails by produceState<com.funjim.fishstory.model.SegmentWithDetails?>(initialValue = null) {
        viewModel.getSegmentWithDetails(segmentId).collect { value = it }
    }

    val colors by viewModel.lureColors.collectAsState(initial = emptyList())

    // Form State
    var selectedSpeciesId by remember { mutableStateOf<Int?>(null) }
    var selectedFishermanId by remember { mutableStateOf<Int?>(null) }
    var selectedLureId by remember { mutableStateOf<Int?>(null) }
    var lengthStr by remember { mutableStateOf("") }
    var released by remember { mutableStateOf(true) }
    var holeNumberStr by remember { mutableStateOf("") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Check for both FINE and COARSE location permissions
    val hasLocationPermission = remember {
        context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    // Default to true only if permission is actually granted
    var useCurrentLocation by remember { mutableStateOf(hasLocationPermission) }

    val startTime = segmentDetails?.segment?.startTime ?: timestamp
    val endTime = segmentDetails?.segment?.endTime ?: timestamp


    if (timestamp < startTime) {
        timestamp = startTime
    }
    if (timestamp > endTime) {
        timestamp = endTime
    }

    // Lure Logic (Filtered by Fisherman)
    val rawLures by if (selectedFishermanId != null) {
        viewModel.getLuresForFisherman(selectedFishermanId!!).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<Lure>()) }
    }

    val luresSorted = remember(rawLures, colors) {
        rawLures.map { lure ->
            val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
            val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
            val glowColorName = colors.find { it.id == lure.glowColorId }?.name
            lure to lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName)
        }.sortedBy { it.second }
    }

    // UI States for Dropdowns and Pickers
    var speciesExpanded by remember { mutableStateOf(false) }
    var fishermanExpanded by remember { mutableStateOf(false) }
    var lureExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showNewSpeciesDialog by remember { mutableStateOf(false) }
    var newSpeciesName by remember { mutableStateOf("") }

    val localDateTime = remember(timestamp) { timestamp.toLocalDateTime() }

    val datePickerState = key(showDatePicker) {
        rememberDatePickerState(
            initialSelectedDateMillis = timestamp.toUtcMidnight(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val startMidnight = startTime.toUtcMidnight()
                    val endMidnight = endTime.toUtcMidnight()
                    return utcTimeMillis in startMidnight..endMidnight
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val startYear =
                        Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).year
                    val endYear = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).year
                    return year in startYear..endYear
                }
            }
        )
    }

    val timePickerState = rememberTimePickerState(
        initialHour = localDateTime.hour,
        initialMinute = localDateTime.minute
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        timestamp = updateDate(timestamp, it)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val candidate = updateTime(timestamp, timePickerState.hour, timePickerState.minute)
                    // Clamp to the valid range
                    timestamp = candidate.coerceIn(startTime, endTime)
                    showTimePicker = false
                }) { Text("OK") }
            },
            text = {
                // Wrapped in a Box for alignment and better tap targets
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log New Catch") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Species Dropdown
            ExposedDropdownMenuBox(
                expanded = speciesExpanded,
                onExpandedChange = { speciesExpanded = !speciesExpanded }
            ) {
                val selectedName = speciesList.find { it.id == selectedSpeciesId }?.name ?: "Select Species"
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Species") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = speciesExpanded, onDismissRequest = { speciesExpanded = false }) {
                    speciesList.forEach { species ->
                        DropdownMenuItem(
                            text = { Text(species.name) },
                            onClick = { selectedSpeciesId = species.id; speciesExpanded = false }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Add species...", color = MaterialTheme.colorScheme.primary) },
                        onClick = { speciesExpanded = false; showNewSpeciesDialog = true },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }

            // Fisherman Dropdown
            ExposedDropdownMenuBox(
                expanded = fishermanExpanded,
                onExpandedChange = { fishermanExpanded = !fishermanExpanded }
            ) {
                val fishermanName = segmentDetails?.fishermen?.find { it.id == selectedFishermanId }?.fullName ?: "Select Fisherman"
                OutlinedTextField(
                    value = fishermanName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Who caught it?") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fishermanExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = fishermanExpanded, onDismissRequest = { fishermanExpanded = false }) {
                    segmentDetails?.fishermen?.sortedBy { it.fullName }?.forEach { fisherman ->
                        DropdownMenuItem(
                            text = { Text(fisherman.fullName) },
                            onClick = {
                                selectedFishermanId = fisherman.id
                                selectedLureId = null
                                fishermanExpanded = false
                            }
                        )
                    }
                }
            }

            // Lure Dropdown
            ExposedDropdownMenuBox(
                expanded = lureExpanded,
                onExpandedChange = { if (selectedFishermanId != null) lureExpanded = !lureExpanded }
            ) {
                val lureName = luresSorted.find { it.first.id == selectedLureId }?.second ?: "Select Lure"
                OutlinedTextField(
                    value = lureName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lure Used") },
                    enabled = selectedFishermanId != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lureExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = lureExpanded, onDismissRequest = { lureExpanded = false }) {
                    luresSorted.forEach { (lure, displayName) ->
                        DropdownMenuItem(
                            text = { Text(displayName) },
                            onClick = { selectedLureId = lure.id; lureExpanded = false }
                        )
                    }
                }
            }

            // Length and Hole Number
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lengthStr,
                    onValueChange = { lengthStr = it },
                    label = { Text("Length (in)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = holeNumberStr,
                    onValueChange = { holeNumberStr = it },
                    label = { Text("Hole #") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Date & Time Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp)))
                }
                OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp)))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = useCurrentLocation,
                    onCheckedChange = { useCurrentLocation = it },
                    // Disable the checkbox if permissions aren't set
                    enabled = hasLocationPermission
                )
                Text("Use current location")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = released, onCheckedChange = { released = it })
                Text("Released")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        // Check both the state AND the actual permission
                        val location = if (useCurrentLocation && hasLocationPermission) {
                            getCurrentLocation(context)
                        } else {
                            null
                        }
                        viewModel.addFish(
                            Fish(
                                speciesId = selectedSpeciesId ?: 0,
                                fishermanId = selectedFishermanId ?: 0,
                                tripId = tripId,
                                segmentId = segmentId,
                                lureId = selectedLureId,
                                length = lengthStr.toDoubleOrNull() ?: 0.0,
                                isReleased = released,
                                timestamp = timestamp,
                                holeNumber = holeNumberStr.toIntOrNull(),
                                latitude = location?.first,
                                longitude = location?.second
                            )
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSpeciesId != null && selectedFishermanId != null
            ) {
                Text("Log Catch")
            }
        }
    }

    // New Species Dialog
    if (showNewSpeciesDialog) {
        AlertDialog(
            onDismissRequest = { showNewSpeciesDialog = false },
            title = { Text("Add New Species") },
            text = {
                TextField(
                    value = newSpeciesName,
                    onValueChange = { newSpeciesName = it },
                    placeholder = { Text("Species Name (e.g. Walleye)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newSpeciesName.isNotBlank()) {
                        scope.launch {
                            val newId = viewModel.addSpecies(newSpeciesName)
                            selectedSpeciesId = newId.toInt()
                            showNewSpeciesDialog = false
                            newSpeciesName = ""
                        }
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showNewSpeciesDialog = false }) { Text("Cancel") } }
        )
    }
}

fun Long.toUtcMidnight(): Long =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()