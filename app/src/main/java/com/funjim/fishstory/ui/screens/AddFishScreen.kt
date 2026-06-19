package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import com.funjim.fishstory.ui.utils.DateTimeUtils.toLocalDateTime
import com.funjim.fishstory.ui.utils.DateTimeUtils.updateDate
import com.funjim.fishstory.ui.utils.DateTimeUtils.updateTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithPhotos
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.FishermanSelectionField
import com.funjim.fishstory.ui.utils.LureSelectionField
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.inchesToStorage
import com.funjim.fishstory.ui.utils.sortLures
import com.funjim.fishstory.ui.utils.toInches
import com.funjim.fishstory.viewmodels.AddFishUiState
import com.funjim.fishstory.viewmodels.AddFishViewModel
import java.time.ZoneOffset
import kotlin.math.floor

fun Long.toUtcMidnight(): Long =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFishScreen(
    viewModel: AddFishViewModel,
    tripId: String,
    eventId: String,
    fishId: String? = null, // Pass null for "Add", pass ID for "Edit"
    navigateToSelectLures: (String, String) -> Unit,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val draftFish by viewModel.draftFish.collectAsStateWithLifecycle()
    val photos by viewModel.fishPhotos.collectAsStateWithLifecycle()

    var originalFish by remember { mutableStateOf<FishWithPhotos?>(null) }

    LaunchedEffect(tripId, eventId, fishId) {
        viewModel.selectTrip(tripId)
        viewModel.selectEvent(eventId)
        viewModel.selectFish(fishId)
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is AddFishUiState.Success && draftFish == null) {
            val startTime = state.event.event.startTime
            val endTime = state.event.event.endTime

            if (fishId != null && state.fish != null) {
                // Edit Mode Initialization
                originalFish = state.fish
                viewModel.initDraftFish(
                    state.fish.fish,
                    state.fish.fish.tripId,
                    state.fish.fish.eventId,
                    state.fish.photos
                )
                viewModel.selectFisherman(state.fish.fish.fishermanId)
                viewModel.selectTackleBox(state.tackleBoxMap[state.fish.fish.fishermanId])
            } else if (fishId == null) {
                // New Fish Mode Initialization
                viewModel.initDraftFish(null, tripId, eventId)
                viewModel.selectFisherman(null)
                viewModel.selectTackleBox(null)

                // TODO -- move this time clamping within the viewmodel entirely when initDraftFish is called
                val initialTime = System.currentTimeMillis()
                viewModel.updateTimestamp(initialTime, startTime, endTime)
            }
        }
    }

    // Check for both FINE and COARSE location permissions
    val hasLocationPermission = remember {
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    var addNewSpecies by remember { mutableStateOf(false) }
    var addSpeciesName by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is AddFishUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AddFishUiState.Success -> {
            val trip = state.trip
            val event = state.event
            val startTime = event.event.startTime
            val endTime = event.event.endTime

            val timestamp = draftFish?.timestamp ?: System.currentTimeMillis()

            val speciesList = state.species
            val selectedSpecies = remember(draftFish, speciesList) {
                speciesList.find { it.id == draftFish?.speciesId }
            }

            val eventFishermen = state.fishermen
            val tackleBoxMap = state.tackleBoxMap

            val selectedFisherman = remember(draftFish, eventFishermen) {
                eventFishermen.find { it.id == draftFish?.fishermanId }
            }

            val lures by viewModel.tackleBoxWithLures.collectAsStateWithLifecycle(initialValue = emptyList())
            val luresSorted by remember(lures) { derivedStateOf{ sortLures(lures) } }
            val selectedLure = remember(draftFish, lures) {
                lures.find { it.lure.id == draftFish?.lureId }
            }

            val hasChanges by viewModel.hasChanges.collectAsStateWithLifecycle(initialValue = false)

            val datePickerState = key(showDatePicker) {
                rememberDatePickerState(
                    initialSelectedDateMillis = draftFish?.timestamp?.toUtcMidnight(),
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
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val newDate = updateDate(timestamp, it)
                                viewModel.updateTimestamp(newDate, startTime, endTime)
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            val timePickerState = key(timestamp) {
                val localDateTime = timestamp.toLocalDateTime()
                rememberTimePickerState(
                    initialHour = localDateTime.hour,
                    initialMinute = localDateTime.minute,
                    is24Hour = false
                )
            }
            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val newTime = updateTime(timestamp, timePickerState.hour, timePickerState.minute)
                            viewModel.updateTimestamp(newTime, startTime, endTime)
                            showTimePicker = false
                        }) { Text("OK") }
                    },
                    text = {
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
                        title = { Text(if (fishId == null) "Log Fish" else "Edit Fish") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.clearDraftFish()
                                navigateBack()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                draftFish?.let { fish ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SpeciesSelection(
                            items = speciesList,
                            selectedItem = selectedSpecies,
                            onSelected = { species -> viewModel.updateSpecies(species) },
                            onAdd = { addNewSpecies = true },
                            modifier = Modifier.fillMaxWidth(),
                            thumbnailProvider = { species ->
                                val thumbnailFlow = remember(species.id) {
                                    viewModel.speciesThumbnail(species.id)
                                }

                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.LeapingFish,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        )

                        FishermanSelectionField(
                            items = eventFishermen,
                            selectedItem = selectedFisherman,
                            defaultText = "Select Fisherman",
                            onSelected = { fisherman ->
                                viewModel.updateFisherman(fisherman)
                                viewModel.updateLure(null)
                                viewModel.selectFisherman(fisherman.id)
                                viewModel.selectTackleBox(tackleBoxMap[fisherman.id] ?: "")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            thumbnailProvider = { fisherman ->
                                val thumbnailFlow = remember(fisherman.id) {
                                    viewModel.fishermanThumbnail(fisherman.id)
                                }

                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.Fisherman,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        )

                        selectedFisherman?.let { fisherman ->
                            tackleBoxMap[fisherman.id]?.let { tackleBoxId ->
                                LureSelectionField(
                                    items = luresSorted,
                                    selectedItem = selectedLure,
                                    onSelected = { lure -> viewModel.updateLure(lure) },
                                    onAdd = { navigateToSelectLures(fisherman.id, tackleBoxId) },
                                    onClear = { viewModel.updateLure(null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    thumbnailProvider = { lure ->
                                        val thumbnailFlow = remember(lure.lure.id) {
                                            viewModel.lureThumbnail(lure.lure.id)
                                        }

                                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                        ThumbnailBox(
                                            thumbnail = thumbnail,
                                            imageVector = AppIcons.Default.Lure,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                )
                            }
                        }

                        // Length and Hole Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentTotalInches = fish.length?.toInches() ?: 0.0
                            val wholeInches = floor(currentTotalInches).toInt().coerceAtLeast(0)
                            val remainingFraction = currentTotalInches - wholeInches

                            FractionalLengthField(
                                label = "Length (in)",
                                wholeValue = if (currentTotalInches == 0.0) "" else wholeInches.toString(),
                                fractionValue = remainingFraction,
                                onLengthChanged = { newWhole, newFraction ->
                                    val checkedWhole = newWhole.coerceAtLeast(0)
                                    val computedDouble = checkedWhole.toDouble() + newFraction
                                    viewModel.updateLength(computedDouble.inchesToStorage())
                                },
                                modifier = Modifier.weight(1f)
                            )

                            StepperField(
                                label = "Hole #",
                                value = fish.holeNumber.toString(),
                                onValueChange = { hole ->
                                    viewModel.updateHoleNumber(
                                        hole.toIntOrNull() ?: 0
                                    )
                                },
                                onIncrement = {
                                    viewModel.updateHoleNumber(fish.holeNumber?.plus(1) ?: 1)
                                },
                                onDecrement = {
                                    val current = fish.holeNumber ?: 0
                                    if (current > 1) {
                                        viewModel.updateHoleNumber(fish.holeNumber?.minus(1) ?: 1)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Date & Time Buttons
                        // The Date & Time Buttons do not participate in focus switching with volume keys
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    SimpleDateFormat(
                                        "MMM dd",
                                        Locale.getDefault()
                                    ).format(Date(timestamp))
                                )
                            }
                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    SimpleDateFormat(
                                        "hh:mm a",
                                        Locale.getDefault()
                                    ).format(Date(timestamp))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tripLat = trip.latitude
                            val eventLat = event.event.latitude
                            val fishLat = fish.latitude

                            // Precedence logic: Use Event if it exists, otherwise use Trip
                            val activeLat = fishLat ?: eventLat ?: tripLat

                            if (activeLat != null) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "GPS Location",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                if (fishLat == null) {
                                    Text(
                                        text = if (eventLat != null) "(Event)" else "(Trip)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            OutlinedButton(
                                enabled = hasLocationPermission,
                                onClick = {
                                    scope.launch {
                                        val location = viewModel.fetchLocation()
                                        viewModel.updateLocation(
                                            location?.latitude ?: 0.0,
                                            location?.longitude ?: 0.0
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Use Current Location")
                            }
                        }

                        CheckBoxWithText(
                            label = "Kept",
                            checked = (fish.keptCount > 0),
                            onCheckedChange = { kept -> viewModel.updateKeptCount(if (kept) 1 else 0) },
                            enabled = true,
                        )

                        PhotoPickerRow(
                            photos = photos,
                            onPhotoSelected = { uri ->
                                scope.launch {
                                    val metadata = viewModel.getPhotoMetadata(uri)
                                    val exists = photos.find { it.hashcode == metadata.hashcode }
                                    if (exists == null) {
                                        viewModel.addPhoto(
                                            Photo(
                                                uri = uri.toString(),
                                                hashcode = metadata.hashcode,
                                                thumbnail = metadata.thumbnail
                                            )
                                        )
                                    }
                                }
                            },
                            onPhotoTaken = { uri ->
                                scope.launch {
                                    val metadata = viewModel.getPhotoMetadata(uri)
                                    viewModel.addPhoto(
                                        Photo(
                                            uri = uri.toString(),
                                            hashcode = metadata.hashcode,
                                            thumbnail = metadata.thumbnail
                                        )
                                    )
                                }
                            },
                            onPhotoDeleted = { photo ->
                                viewModel.deletePhoto(photo)
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.upsertFish(fish)
                                    viewModel.clearDraftFish()

                                    // Logic to identify the changes
                                    val originalPhotos = originalFish?.photos ?: emptyList()
                                    val currentPhotos = photos

                                    val newPhotos = currentPhotos.filter { current ->
                                        originalPhotos.none { it.id == current.id }
                                    }

                                    val deletedPhotos = originalPhotos.filter { original ->
                                        currentPhotos.none { it.id == original.id }
                                    }

                                    viewModel.addFishPhotos(fish.id, newPhotos)
                                    viewModel.deleteFishPhotos(fish.id, deletedPhotos)

                                    navigateBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = hasChanges,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (fishId == null) "Add Fish" else "Edit Fish")
                        }
                    }
                }
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
                            viewModel.updateSpecies(species)
                            addNewSpecies = false
                            addSpeciesName = ""
                        }
                    }
                }) { Text("Add Species") }
            },
            dismissButton = {
                TextButton(onClick = { addNewSpecies = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FractionalLengthField(
    label: String,
    wholeValue: String,
    fractionValue: Double,
    onLengthChanged: (whole: Int, fraction: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val fractions = remember {
        listOf(
            "0" to 0.0,
            "⅛" to 0.125, // Unicode for 1/8
            "¼" to 0.25,  // Unicode for 1/4
            "⅜" to 0.375, // Unicode for 3/8
            "½" to 0.5,   // Unicode for 1/2
            "⅝" to 0.625, // Unicode for 5/8
            "¾" to 0.75,  // Unicode for 3/4
            "⅞" to 0.875  // Unicode for 7/8
        )
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    // Identify current active selected fraction label match
    val currentFractionLabel = remember(fractionValue) {
        fractions.minByOrNull { kotlin.math.abs(it.second - fractionValue) }?.first ?: "0"
    }

    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Whole Inches Input Box (Blocks negative inputs)
            OutlinedTextField(
                value = wholeValue,
                onValueChange = { input ->
                    val cleanInput = input.filter { it.isDigit() }
                    val wholeInt = cleanInput.toIntOrNull() ?: 0
                    onLengthChanged(wholeInt.coerceAtLeast(0), fractionValue)
                },
                modifier = Modifier.weight(0.5f),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("0", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            )

            // Fraction Item Selection Dropdown Trigger Box
            Box(modifier = Modifier.weight(0.5f)) {
                OutlinedTextField(
                    value = currentFractionLabel,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(textAlign = TextAlign.Right),
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Fraction",
                            modifier = Modifier
                                .size(20.dp)
                                .offset(x = (-2).dp)
                        )
                    },
                    // Wrapping the entire text area as a button click target
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect { interaction ->
                                    if (interaction is PressInteraction.Release) {
                                        dropdownExpanded = true
                                    }
                                }
                            }
                        }
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .requiredWidth(64.dp)
                        .fillMaxHeight(0.4f)
                ) {
                    fractions.forEach { (label, value) ->
                        // Check if this specific item is the one currently selected
                        // We use a small delta check (0.001) to safely compare Double values
                        val isSelected = kotlin.math.abs(value - fractionValue) < 0.001

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        // Optional: Make the text bold if it's selected
                                        fontWeight =
                                            if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold
                                            else androidx.compose.ui.text.font.FontWeight.Normal
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            modifier = Modifier.background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            // Highlight the background and text color of the selected item
                            colors = MenuDefaults.itemColors(
                                textColor =
                                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                            ),
                            onClick = {
                                val currentWhole = wholeValue.toIntOrNull() ?: 0
                                onLengthChanged(currentWhole.coerceAtLeast(0), value)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
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
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}