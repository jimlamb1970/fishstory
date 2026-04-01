package com.funjim.fishstory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.focus.focusRequester

import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import com.funjim.fishstory.model.Species

enum class FishField { Species, Fisherman, Lure, Length, Hole, Location, Released, LogCatch }

private const val TAG = "AddFishScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishScreen(
    viewModel: MainViewModel,
    tripId: String,
    segmentId: String,
    fishId: String? = null, // Pass null for "Add", pass ID for "Edit"
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
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

    // Prepare for control using volume button
    var speciesSelectedIndex by remember { mutableStateOf(0) }
    var fishermanSelectedIndex by remember { mutableStateOf(0) }
    var lureSelectedIndex by remember { mutableStateOf(0) }

    val keyEvent by viewModel.volumeKeyEvent.collectAsState(initial = 0)
    val selectEvent by viewModel.selectEvent.collectAsState()

    var focusedField by remember { mutableStateOf<FishField?>(null) }
    val firstItemFocusRequester = remember { FocusRequester() }
    val firstItemRequester = remember { FocusRequester() }
    val lengthDecrementRequester = remember { FocusRequester() }
    val lengthIncrementRequester = remember { FocusRequester() }
    val holeDecrementRequester = remember { FocusRequester() }
    val holeIncrementRequester = remember { FocusRequester() }
    val locationButtonRequester = remember { FocusRequester() }
    val keptButtonRequester = remember { FocusRequester() }
    val logCatchRequester = remember { FocusRequester() }

    // Form State
    var selectedSpeciesId by remember { mutableStateOf<String?>(null) }
    var selectedFishermanId by remember { mutableStateOf<String?>(null) }
    var selectedLureId by remember { mutableStateOf<String?>(null) }
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
        context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    // Default to true only if permission is actually granted
    var useCurrentLocation by remember { mutableStateOf(hasLocationPermission) }

    // Load data if editing
    LaunchedEffect(fishId) {
        if (fishId != null) {
            val fish = viewModel.getFishById(fishId) // Ensure this exists in your ViewModel
            fish?.let {
                selectedSpeciesId = it.speciesId
                selectedFishermanId = it.fishermanId
                selectedLureId = it.lureId
                lengthStr = it.length.toString()
                released = it.isReleased
                timestamp = it.timestamp
                latitude = it.latitude
                longitude = it.longitude
                holeNumberStr = it.holeNumber.toString()
                useCurrentLocation = false
                // TODO - what to do about tripId and segmentId?
            }
        }
    }

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

    LaunchedEffect(keyEvent) {
        Log.d(TAG, "keyEvent: $keyEvent, focusedFiled: $focusedField")
        if (keyEvent > 0) {
            when {
                speciesExpanded -> speciesSelectedIndex = (speciesSelectedIndex - 1).coerceAtLeast(0)
                fishermanExpanded -> fishermanSelectedIndex = (fishermanSelectedIndex - 1).coerceAtLeast(0)
                lureExpanded -> lureSelectedIndex = (lureSelectedIndex - 1).coerceAtLeast(0)
                else -> {
                    if (focusedField == FishField.Released && (selectedSpeciesId == null || selectedFishermanId == null)) {
                        firstItemFocusRequester.requestFocus()
                    } else if (focusedField == FishField.LogCatch) {
                        firstItemFocusRequester.requestFocus()
                    } else if (focusedField == FishField.Lure) {
                        lengthDecrementRequester.requestFocus()
                    } else if ((focusedField == FishField.Fisherman) && (selectedFishermanId == null)) {
                        lengthDecrementRequester.requestFocus()
                    } else {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                }
            }
        }
    }

    LaunchedEffect(selectEvent) {
        Log.d(TAG, "selectEvent: $selectEvent")
        if (selectEvent == 0) return@LaunchedEffect
        when {
            speciesExpanded -> {
                selectedSpeciesId = speciesList.getOrNull(speciesSelectedIndex)?.id
                speciesExpanded = false
            }

            fishermanExpanded -> {
                val fishermen = segmentDetails?.fishermen?.sortedBy { it.fullName } ?: emptyList()
                selectedFishermanId = fishermen.getOrNull(fishermanSelectedIndex)?.id
                selectedLureId = null
                fishermanExpanded = false
            }

            lureExpanded -> {
                selectedLureId = luresSorted.getOrNull(lureSelectedIndex)?.first?.id
                lureExpanded = false
            }

            else -> when (focusedField) {
                FishField.Species -> speciesExpanded = !speciesExpanded
                FishField.Fisherman -> fishermanExpanded = !fishermanExpanded
                FishField.Lure -> if (selectedFishermanId != null) lureExpanded = !lureExpanded
                FishField.Released -> released = !released
                FishField.Location -> if (hasLocationPermission) useCurrentLocation = !useCurrentLocation
                FishField.Length -> {
                    val current = lengthStr.toDoubleOrNull() ?: 0.0
                    lengthStr = (current + 0.25).toString()
                }
                FishField.Hole -> {
                    val current = holeNumberStr.toIntOrNull() ?: 0
                    holeNumberStr = (current + 1).toString()
                }
                FishField.LogCatch -> {
                    onNavigateBack()
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(speciesExpanded, fishermanExpanded, lureExpanded) {
        if (speciesExpanded || fishermanExpanded || lureExpanded) {
            kotlinx.coroutines.delay(50)
            firstItemRequester.requestFocus()
        }
    }

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
                onExpandedChange = {
                    speciesExpanded = !speciesExpanded
                    if (speciesExpanded) speciesSelectedIndex = 0
                }
            ) {
                // TODO look into making the drop down menu filterable
                val selectedName = speciesList.find { it.id == selectedSpeciesId }?.name ?: "Select Species"
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Species") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .onFocusChanged { if (it.isFocused) focusedField = FishField.Species }
                        .focusRequester(firstItemFocusRequester)
                )

                ExposedDropdownMenu(expanded = speciesExpanded, onDismissRequest = { speciesExpanded = false }) {
                    speciesList.forEachIndexed { index, species ->
                        DropdownMenuItem(
                            text = { Text(species.name) },
                            onClick = {
                                selectedSpeciesId = species.id
                                speciesExpanded = false
                                focusManager.clearFocus()
//                                focusRequester.requestFocus()
                            },
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstItemRequester) else Modifier)
                                .background(if (index == speciesSelectedIndex) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        Log.d(TAG, "type: $keyEvent.type, keycode: $keyEvent.nativeKeyEvent.keyCode")
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            // VOLUME UP: Move to the next item
                                            KeyEvent.KEYCODE_VOLUME_UP -> {
                                                if (speciesSelectedIndex == speciesList.lastIndex) {
                                                    speciesSelectedIndex = 0
                                                } else {
                                                    speciesSelectedIndex =
                                                        (speciesSelectedIndex + 1).coerceAtMost(
                                                            speciesList.lastIndex
                                                        )
                                                }
                                                true // Consumes the event so the system volume doesn't change
                                            }
                                            // VOLUME DOWN: Select the current item
                                            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                                // Trigger the selection logic
                                                selectedSpeciesId = speciesList.getOrNull(speciesSelectedIndex)?.id
                                                speciesExpanded = false
                                                true
                                            }
                                            else -> false
                                        }
                                    } else {
                                        false
                                    }                                    // Check if the key pressed is Volume Up or Down
                                }
                                .focusable()
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
                onExpandedChange = {
                    fishermanExpanded = !fishermanExpanded
                    if (fishermanExpanded) fishermanSelectedIndex = 0
                }
            ) {
                // TODO look into making the drop down menu filterable
                val fishermanName = segmentDetails?.fishermen?.find { it.id == selectedFishermanId }?.fullName ?: "Select Fisherman"
                OutlinedTextField(
                    value = fishermanName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Who caught it?") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fishermanExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor().onFocusChanged { if (it.isFocused) focusedField = FishField.Fisherman }
                )
                ExposedDropdownMenu(expanded = fishermanExpanded, onDismissRequest = { fishermanExpanded = false }) {
                    val fishermen = segmentDetails?.fishermen?.sortedBy { it.fullName } ?: emptyList()
                    fishermen.forEachIndexed { index, fisherman ->
                        DropdownMenuItem(
                            text = { Text(fisherman.fullName) },
                            onClick = {
                                selectedFishermanId = fisherman.id
                                selectedLureId = null
                                fishermanExpanded = false
                            },
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstItemRequester) else Modifier)
                                .background(
                                    if (index == fishermanSelectedIndex) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_VOLUME_UP -> {
                                                fishermanSelectedIndex = if (fishermanSelectedIndex == fishermen.lastIndex) 0
                                                else fishermanSelectedIndex + 1
                                                true
                                            }
                                            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                                selectedFishermanId = fishermen.getOrNull(fishermanSelectedIndex)?.id
                                                selectedLureId = null
                                                fishermanExpanded = false
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                                .focusable()
                        )
                    }
                }
            }

            // Lure Dropdown
            ExposedDropdownMenuBox(
                expanded = lureExpanded,
                onExpandedChange = {
                    if (selectedFishermanId != null) {
                        lureExpanded = !lureExpanded
                        if (lureExpanded) lureSelectedIndex = 0
                    }
                }
            ) {
                // TODO look into making the drop down menu filterable
                val lureName = luresSorted.find { it.first.id == selectedLureId }?.second ?: "Select Lure"
                OutlinedTextField(
                    value = lureName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lure Used") },
                    enabled = selectedFishermanId != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lureExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth().onFocusChanged { if (it.isFocused) focusedField = FishField.Lure }
                )
                ExposedDropdownMenu(expanded = lureExpanded, onDismissRequest = { lureExpanded = false }) {
                    luresSorted.forEachIndexed { index, (lure, displayName) ->
                        DropdownMenuItem(
                            text = { Text(displayName) },
                            onClick = { selectedLureId = lure.id; lureExpanded = false },
                            modifier = Modifier
                                .then(if (index == 0) Modifier.focusRequester(firstItemRequester) else Modifier)
                                .background(
                                    if (index == lureSelectedIndex) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_VOLUME_UP -> {
                                                lureSelectedIndex = if (lureSelectedIndex == luresSorted.lastIndex) 0
                                                else lureSelectedIndex + 1
                                                true
                                            }
                                            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                                selectedLureId = luresSorted.getOrNull(lureSelectedIndex)?.first?.id
                                                lureExpanded = false
                                                lengthDecrementRequester.requestFocus()
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                                .focusable()
                        )
                    }
                }
            }

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
                    modifier = Modifier.weight(1f),
                    onFocused = { focusedField = FishField.Length },
                    decrementRequester = lengthDecrementRequester,
                    incrementRequester = lengthIncrementRequester,
                    nextRequester = holeDecrementRequester
                )

                val targetRequester = if (hasLocationPermission) locationButtonRequester else keptButtonRequester

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
                    modifier = Modifier.weight(1f),
                    onFocused = { focusedField = FishField.Hole },
                    decrementRequester = holeDecrementRequester,
                    incrementRequester = holeIncrementRequester,
                    nextRequester = targetRequester
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

            NavigableCheckboxRow(
                label = "Use Current Location",
                checked = useCurrentLocation,
                onCheckedChange = { useCurrentLocation = it },
                onFocus = { focusedField = FishField.Location },
                focusRequester = locationButtonRequester,
                enabled = hasLocationPermission
            )

            NavigableCheckboxRow(
                label = "Released",
                checked = released,
                onCheckedChange = { released = it },
                onFocus = { focusedField = FishField.Released },
                focusRequester = keptButtonRequester,
                enabled = true
            )

            Spacer(modifier = Modifier.weight(1f))

            val logInteractionSource = remember { MutableInteractionSource() }
            val isLogFocused by logInteractionSource.collectIsFocusedAsState()
            Log.d(TAG, "isLogFocues: $isLogFocused")

            Button(
                onClick = {
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
                                speciesId = selectedSpeciesId,
                                fishermanId = selectedFishermanId,
                                tripId = tripId,
                                segmentId = segmentId,
                                lureId = selectedLureId,
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
                },
                interactionSource = logInteractionSource,
                colors = ButtonDefaults.buttonColors(
                    // Change color entirely when focused via Volume Keys
                    containerColor = if (focusedField == FishField.LogCatch) Color.Yellow else MaterialTheme.colorScheme.primary,
                    contentColor = if (focusedField == FishField.LogCatch) Color.Black else MaterialTheme.colorScheme.onPrimary
                ),
                enabled = selectedSpeciesId != null && selectedFishermanId != null,
                border = if (focusedField == FishField.LogCatch) BorderStroke(4.dp, Color.Blue) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    // 3. Attach your FocusRequester here if you have one
                    .focusRequester(logCatchRequester)
                    .onFocusChanged {
                        Log.d(TAG, "It is focused ${it.isFocused}")
                        if (it.isFocused) focusedField = FishField.LogCatch
                    }
                    // 4. ADD A VISUAL BORDER so you can tell it's focused
                    .border(
                        width = if (isLogFocused) 12.dp else 0.dp,
                        color = if (isLogFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = ButtonDefaults.shape // Matches the button's rounding
                    )
                    .focusable()
            ) {
                Text( if (fishId == null) "Add Fish" else "Edit Fish" )
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
                            val species = Species(name = newSpeciesName)
                            viewModel.addSpecies(species)
                            selectedSpeciesId = species.id
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

@Composable
fun StepperField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    onFocused: (() -> Unit)? = null,
    decrementRequester: FocusRequester = remember { FocusRequester() },
    incrementRequester: FocusRequester = remember { FocusRequester() },
    nextRequester: FocusRequester? = null
) {
    val decInteractionSource = remember { MutableInteractionSource() }
    val incInteractionSource = remember { MutableInteractionSource() }

    val isDecFocused by decInteractionSource.collectIsFocusedAsState()
    val isIncFocused by incInteractionSource.collectIsFocusedAsState()

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
                interactionSource = decInteractionSource, // Attach source
                modifier = Modifier
                    .focusRequester(decrementRequester)
                    .focusable(interactionSource = decInteractionSource) // Link focus
                    .size(48.dp)
                    .border(
                        width = if (isDecFocused) 3.dp else 1.dp,
                        color = if (isDecFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_VOLUME_UP -> {
                                    incrementRequester.requestFocus()
                                    true
                                }
                                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                    onDecrement()
                                    true
                                }
                                else -> false
                            }
                        } else false
                    }
                    .onFocusChanged { if (it.isFocused) onFocused?.invoke() },
                shape = MaterialTheme.shapes.small,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = if (isDecFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                )
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // INCREMENT BUTTON (+)
            OutlinedIconButton(
                onClick = onIncrement,
                interactionSource = incInteractionSource, // Attach source
                modifier = Modifier
                    .focusRequester(incrementRequester)
                    .focusable(interactionSource = incInteractionSource) // Link focus
                    .size(48.dp)
                    .border(
                        width = if (isIncFocused) 3.dp else 1.dp,
                        color = if (isIncFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_VOLUME_UP -> {
                                    nextRequester?.requestFocus()
                                    true
                                }
                                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                    onIncrement()
                                    true
                                }
                                else -> false
                            }
                        } else false
                    }
                    .onFocusChanged { if (it.isFocused) onFocused?.invoke() },
                shape = MaterialTheme.shapes.small,
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = if (isIncFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                )
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun NavigableCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onFocus: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() }, // Pass a requester
    enabled: Boolean = true // Add an enabled flag
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        // If disabled, onClick is null so it doesn't ripple or trigger
        onClick = { if (enabled) onCheckedChange(!checked) },
        interactionSource = interactionSource,
        enabled = enabled, // Surface handles the 'disabled' visual state
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester) // Attach the requester here
            .onFocusChanged {
                if (it.isFocused) onFocus()
            }
            // Surface is only focusable if enabled is true
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.5f) // Visual cue for disabled state
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
                modifier = Modifier.border(
                    width = if (isFocused && enabled) 4.dp else 0.dp,
                    color = if (isFocused) Color.Blue else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                ).size(24.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f) // Push any trailing icons to the end
            )
        }
    }
}