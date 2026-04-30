package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.ui.utils.DateTimeUtils.toLocalDateTime
import com.funjim.fishstory.ui.utils.DateTimeUtils.updateDate
import com.funjim.fishstory.ui.utils.DateTimeUtils.updateTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.utils.getCurrentLocation
import com.funjim.fishstory.viewmodels.FishViewModel
import java.time.ZoneOffset

fun Long.toUtcMidnight(): Long =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishScreenNew(
    viewModel: FishViewModel,
    tripId: String,
    segmentId: String,
    fishId: String? = null, // Pass null for "Add", pass ID for "Edit"
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(segmentId) {
        viewModel.updateSelectedEvent(segmentId)
        viewModel.updateSelectedFisherman("")
        viewModel.updateSelectedTackleBox("")
    }

    // Data from ViewModel
    val speciesList by viewModel.species.collectAsState(initial = emptyList())
    val colors by viewModel.lureColors.collectAsState(initial = emptyList())

    val selectedEvent by viewModel.selectedEvent.collectAsState(initial = null)
    val eventFishermen by viewModel.eventFishermen.collectAsState(initial = emptyList())
    val fishermanTackleBoxMap by viewModel.fishermanTackleBoxMap.collectAsState(initial = emptyMap())
    val rawLures by viewModel.tackleBoxWithLures.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Form State
    var selectedSpecies by remember { mutableStateOf<Species?>(null) }
    var selectedFisherman by remember { mutableStateOf<Fisherman?>(null) }
    var selectedLure by remember { mutableStateOf<Lure?>(null) }

    var released by remember { mutableStateOf(true) }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Initialize length and hole number to appropriate values when adding
    // a fish. Could always set them because they will be overridden if editing
    var lengthStr by remember { mutableStateOf(if (fishId == null) "10.0" else "") }
    var holeNumberStr by remember { mutableStateOf(if (fishId == null) "1" else "") }

    // Check for both FINE and COARSE location permissions
    val hasLocationPermission = remember {
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Default to false
    // TODO - store last selected state in view model?
    var useCurrentLocation by remember { mutableStateOf(false) }

    // Load data if editing
    LaunchedEffect(fishId, speciesList, rawLures) {
        if (fishId != null) {
            val fish = viewModel.getFishById(fishId) // Ensure this exists in your ViewModel
            fish?.let { fish ->
                selectedSpecies = speciesList.find { it.id == fish.speciesId }
                selectedFisherman = eventFishermen.find { it.id == fish.fishermanId }
                selectedLure = rawLures.find { it.id == fish.lureId }
                lengthStr = fish.length.toString()
                released = fish.isReleased
                timestamp = fish.timestamp
                latitude = fish.latitude
                longitude = fish.longitude
                holeNumberStr = fish.holeNumber.toString()
                useCurrentLocation = false

                viewModel.updateSelectedEvent(fish.eventId)
                viewModel.updateSelectedFisherman(fish.fishermanId)
                viewModel.updateSelectedTackleBox(fishermanTackleBoxMap[fish.fishermanId])
            }
        }
    }

    val startTime = selectedEvent?.startTime ?: timestamp
    val endTime = selectedEvent?.endTime ?: timestamp

    if (timestamp < startTime) {
        timestamp = startTime
    }
    if (timestamp > endTime) {
        timestamp = endTime
    }

    val luresSorted = remember(rawLures, colors) {
        rawLures.map { lure ->
            val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
            val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
            val glowColorName = colors.find { it.id == lure.glowColorId }?.name
            lure to lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName)
        }.sortedBy { it.second }
    }

    var addNewSpecies by remember { mutableStateOf(false) }
    var addSpeciesName by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var permissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            permissionGranted = true
        }
    }

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

    // Wrap in a key so that when timestamp changes (e.g., after loading an existing fish),
    // the TimePickerState is completely recreated with the new values.
    val timePickerState = key(timestamp) {
        val localDateTime = timestamp.toLocalDateTime()
        rememberTimePickerState(
            initialHour = localDateTime.hour,
            initialMinute = localDateTime.minute,
            is24Hour = false
        )
    }

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
                title = { Text( if (fishId == null) "Log Fish" else "Edit Fish" ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
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

            SpeciesDropdown(
                speciesList,
                selectedSpecies,
                onSelected = { selectedSpecies = it },
                onAdd = { addNewSpecies = true },
                modifier = Modifier.fillMaxWidth()
            )

            FishermanDropdown(
                eventFishermen,
                selectedFisherman,
                onSelected = {
                    selectedFisherman = it
                    selectedLure = null
                    viewModel.updateSelectedFisherman(selectedFisherman?.id ?: "")
                    viewModel.updateSelectedTackleBox(fishermanTackleBoxMap[selectedFisherman?.id] ?: "")
                },
                modifier = Modifier.fillMaxWidth()
            )

            LureDropdown(
                luresSorted,
                selectedLure,
                onSelected = { selectedLure = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedFisherman != null && luresSorted.isNotEmpty(),
            )

            // Length and Hole Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StepperField(
                    label = "Length (in)",
                    value = lengthStr,
                    onValueChange = { lengthStr = it },
                    onIncrement = {
                        val current = lengthStr.toDoubleOrNull() ?: 0.0
                        lengthStr = (current + 0.25).toString()
                    },
                    onDecrement = {
                        val current = lengthStr.toDoubleOrNull() ?: 0.0
                        if (current > 0) lengthStr = (current - 0.25).toString()
                    },
                    modifier = Modifier.weight(1f)
                )

                StepperField(
                    label = "Hole #",
                    value = holeNumberStr,
                    onValueChange = { holeNumberStr = it },
                    onIncrement = {
                        val current = holeNumberStr.toIntOrNull() ?: 0
                        holeNumberStr = (current + 1).toString()
                    },
                    onDecrement = {
                        val current = holeNumberStr.toIntOrNull() ?: 0
                        if (current > 1) holeNumberStr = (current - 1).toString()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Date & Time Buttons
            // The Date & Time Buttons do not participate in focus switching with volume keys
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp)))
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp)))
                }
            }

            CheckBoxWithText(
                label = "Use Current Location",
                checked = useCurrentLocation,
                onCheckedChange = { useCurrentLocation = it },
                enabled = hasLocationPermission
            )

            CheckBoxWithText(
                label = "Released",
                checked = released,
                onCheckedChange = { released = it },
                enabled = true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedSpecies != null && selectedFisherman != null) {
                        scope.launch {
                            // Check both the state AND the actual permission
                            val location = if (useCurrentLocation && hasLocationPermission) {
                                getCurrentLocation(context)
                            } else {
                                null
                            }

                            viewModel.upsertFish(
                                Fish(
                                    id = fishId ?: UUID.randomUUID().toString(),
                                    speciesId = selectedSpecies?.id,
                                    fishermanId = selectedFisherman?.id,
                                    tripId = tripId,
                                    eventId = segmentId,
                                    lureId = selectedLure?.id,
                                    length = lengthStr.toDoubleOrNull() ?: 0.0,
                                    isReleased = released,
                                    timestamp = timestamp,
                                    holeNumber = holeNumberStr.toIntOrNull(),
                                    latitude = location?.first ?: latitude,
                                    longitude = location?.second ?: longitude
                                )
                            )

                            onNavigateBack()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = selectedSpecies != null && selectedFisherman != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text( if (fishId == null) "Add Fish" else "Edit Fish" )
            }
        }
    }

    // New Species Dialog
    if (addNewSpecies) {
        AlertDialog(
            onDismissRequest = { addNewSpecies = false },
            title = { Text("Add New Species") },
            text = {
                TextField(
                    value = addSpeciesName,
                    onValueChange = { addSpeciesName = it },
                    placeholder = { Text("Species Name (e.g. Walleye)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (addSpeciesName.isNotBlank()) {
                        scope.launch {
                            val species = Species(name = addSpeciesName)
                            viewModel.addSpecies(species)
                            selectedSpecies = species
                            addNewSpecies = false
                            addSpeciesName = ""
                        }
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { addNewSpecies = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StepperField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // DECREMENT BUTTON (-)
            OutlinedIconButton(
                onClick = onDecrement,
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    ),
                shape = MaterialTheme.shapes.small,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // INCREMENT BUTTON (+)
            OutlinedIconButton(
                onClick = onIncrement,
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    ),
                shape = MaterialTheme.shapes.small,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun CheckBoxWithText(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = { if (enabled) onCheckedChange(!checked) },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .alpha(if (enabled) 1f else 0.5f) // Visual cue for disabled state
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesDropdown(
    items: List<Species>,
    selectedItem: Species?,
    onSelected: (Species) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter { it.name.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            label = { Text("Species") },
            value = if (expanded) query else selectedItem?.name ?: "",
            onValueChange = { query = it },
            //placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            readOnly = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                query = ""
            }
        ) {
            if (filtered.isNotEmpty()) {
                val totalSpecies = filtered.size

                filtered.forEachIndexed { index, item ->
                    val backgroundColor = if ((index % 2 == 0) || (totalSpecies < 4)) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        // Use a very light tint of your primary or surfaceVariant
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.name,
                                fontWeight =
                                    if (item == selectedItem) FontWeight.Bold
                                    else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.background(backgroundColor),
                        onClick = {
                            onSelected(item)
                            expanded = false
                            query = ""
                        }
                    )
                }
            } else {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Add species...",
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        expanded = false
                        query = ""
                        // TODO - return what has been entered
                        onAdd()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanDropdown(
    items: List<Fisherman>,
    selectedItem: Fisherman?,
    onSelected: (Fisherman) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items
            .filter { it.fullName.contains(query, ignoreCase = true) }
    }.sortedBy { it.fullName }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            label = { Text("Fisherman") },
            value = if (expanded) query else selectedItem?.fullName ?: "",
            onValueChange = { query = it },
            //placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            readOnly = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        if (filtered.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    query = ""
                }
            ) {
                val totalSpecies = filtered.size

                filtered.forEachIndexed { index, item ->
                    val backgroundColor = if ((index % 2 == 0) || (totalSpecies < 4)) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        // Use a very light tint of your primary or surfaceVariant
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.fullName,
                                fontWeight =
                                    if (item == selectedItem) FontWeight.Bold
                                    else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.background(backgroundColor),
                        onClick = {
                            onSelected(item)
                            expanded = false
                            query = ""
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureDropdown(
    items: List<Pair<Lure, String>>,
    selectedItem: Lure?,
    onSelected: (Lure) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Search..."
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items
            .filter { it.second.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        val lureName = filtered.find { it.first == selectedItem }?.second

        OutlinedTextField(
            label = { Text("Lure") },
            value = if (expanded) query else lureName ?: "",
            onValueChange = { query = it },
            //placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        if (filtered.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    query = ""
                }
            ) {
                val totalSpecies = filtered.size

                filtered.forEachIndexed { index, (lure, displayName) ->
                    val backgroundColor = if ((index % 2 == 0) || (totalSpecies < 4)) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        // Use a very light tint of your primary or surfaceVariant
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = displayName ?: "",
                                fontWeight =
                                    if (lure == selectedItem) FontWeight.Bold
                                    else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.background(backgroundColor),
                        onClick = {
                            onSelected(lure)
                            expanded = false
                            query = ""
                        }
                    )
                }
            }
        }
    }
}