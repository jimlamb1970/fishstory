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
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

    val startTime = segmentDetails?.segment?.startTime ?: timestamp
    val endTime = segmentDetails?.segment?.endTime ?: timestamp

    if (timestamp < startTime) {
        timestamp = startTime
    }
    if (timestamp > endTime) {
        timestamp = startTime
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

    // Date/Time Picker States
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = timestamp)
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.MINUTE)
    )

    // Reuse Dialog logic for Date/Time
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = timestamp
                            val hour = get(Calendar.HOUR_OF_DAY)
                            val minute = get(Calendar.MINUTE)
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                        }
                        timestamp = calendar.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = timestamp
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    timestamp = calendar.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timePickerState) }
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
                Checkbox(checked = released, onCheckedChange = { released = it })
                Text("Released")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        val location = getCurrentLocation(context)
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