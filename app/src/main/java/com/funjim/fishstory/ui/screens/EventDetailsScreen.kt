package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.FishermanSummary
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.EventHighlightCard
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnChipColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnSecondaryColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.EventDetailsUiState
import com.funjim.fishstory.viewmodels.EventViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    viewModel: EventViewModel,
    tripId: String,
    eventId: String,
    navigateToSelectEventCrew: () -> Unit,
    navigateToAddFish: () -> Unit,
    navigateToFishList: (String?, String?) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(eventId) {
        viewModel.selectTrip(tripId)
        viewModel.selectEvent(eventId)
    }

    // State to show/hide the assignment dialog picker
    var showSpeciesSelection by remember { mutableStateOf(false) }

    // Assuming you have an all-species stream in the viewmodel to populate the picker options
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()

    var addNewSpecies by remember { mutableStateOf(false) }
    var addSpeciesName by remember { mutableStateOf("") }

    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val now = System.currentTimeMillis()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            val granted = permissions.entries.all { it.value }
            if (granted) {
                scope.launch {
                    val location = viewModel.fetchLocation()
                    if (location != null) {
                        selectedEvent?.let { event ->
                            viewModel.upsertEvent(
                                event.copy(
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            )
                            Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = selectedEvent?.latitude,  // Passed from your DB object
        existingLng = selectedEvent?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            selectedEvent?.let { event ->
                scope.launch {
                    viewModel.upsertEvent(
                        event.copy(
                            latitude = lat,
                            longitude = lng
                        )
                    )
                    Toast.makeText(context, "Event location updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is EventDetailsUiState.Loading -> {
            // Keeps the screen entirely blank or showing a spinner
            // until all 3 database points arrive
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EventDetailsUiState.Success -> {
            // Pull your cleanly separated objects out of the verified state frame
            val eventDetails = state.details
            val eventSummary = state.summary

            val event = eventDetails.event
            selectedEvent = event

            val trip = eventDetails.trip

            val eventLat = event.latitude
            val tripLat = trip.latitude

            // Precedence logic: Use Event if it exists, otherwise use Trip
            val activeLat = eventLat ?: tripLat

            Scaffold(
                topBar = {

                    TopAppBar(
                        title = { Text("Event Details") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { showEditEventDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Event")
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
                                        text = { Text("Use Current Location") },
                                        onClick = {
                                            menuExpanded = false
                                            if (ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                ) == PackageManager.PERMISSION_GRANTED) {
                                                scope.launch {
                                                    val location = viewModel.fetchLocation()
                                                    if (location != null) {
                                                        selectedEvent?.let { event ->
                                                            viewModel.upsertEvent(
                                                                event.copy(
                                                                    latitude = location.latitude,
                                                                    longitude = location.longitude
                                                                )
                                                            )
                                                            Toast.makeText(
                                                                context,
                                                                "Location updated",
                                                                Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Could not get location",
                                                            Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } else {
                                                permissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.MyLocation,
                                                contentDescription = null,
                                                tint = if (activeLat != null)
                                                    Color(0xFF4CAF50)
                                                else
                                                    LocalContentColor.current)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Select on Map") },
                                        onClick = {
                                            menuExpanded = false
                                            locationPicker.openPicker()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Map,
                                                contentDescription = null,
                                                tint = if (activeLat != null)
                                                    Color(0xFF4CAF50)
                                                else
                                                    LocalContentColor.current)
                                        }
                                    )

                                    if (activeLat != null && eventLat != null) {
                                        DropdownMenuItem(
                                            text = {
                                                if (tripLat == null) Text("Clear Location")
                                                else Text("Reset Location")
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                selectedEvent?.let { event ->
                                                    scope.launch {
                                                        viewModel.upsertEvent(
                                                            event.copy(latitude = null, longitude = null)
                                                        )
                                                        if (tripLat == null)
                                                            Toast.makeText(
                                                                context,
                                                                "Location cleared",
                                                                Toast.LENGTH_SHORT).show()
                                                        else
                                                            Toast.makeText(
                                                                context,
                                                                "Location reset",
                                                                Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.LocationOff,
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
                    ExtendedFloatingActionButton(
                        onClick = { navigateToAddFish() },
                        containerColor = getMainButtonColor(),
                        contentColor = getOnMainButtonColor(),
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Log Fish") }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    LazyColumn(horizontalAlignment = Alignment.Start) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getOnMainColor()

                                )
                                val displayLat = event.latitude ?: trip.latitude
                                val displayLng = event.longitude ?: event.longitude
                                val hasAnyLocation = displayLat != null && displayLng != null

                                if (hasAnyLocation) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "View on map",
                                        tint = getOnMainColor(),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                val mapUri =
                                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${displayLat},${displayLng}")
                                                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "Could not open map",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    )
                                    if (selectedEvent?.latitude == null) {
                                        Text(
                                            text = "(Trip)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = getOnMainColor()
                                        )
                                    }
                                }
                            }
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "Start: ${dateTimeFormatter.format(Date(event.startTime))}",
                                style = MaterialTheme.typography.titleMedium,
                                color = getOnSecondaryColor()
                            )
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "End: ${dateTimeFormatter.format(Date(event.endTime))}",
                                style = MaterialTheme.typography.titleMedium,
                                color = getOnSecondaryColor()
                            )

                            PhotoPickerRow(
                                photos = eventDetails.photos,
                                onPhotoSelected = { uri ->
                                    viewModel.addEventPhoto(eventId = eventId, uri = uri, true)
                                },
                                onPhotoTaken = { uri ->
                                    viewModel.addEventPhoto(eventId = eventId, uri = uri, false)
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deleteEventPhoto(eventId, photo.id)
                                }
                            )

                            if (eventSummary.fishCaught != 0 || now >= event.startTime) {
                                HorizontalDivider()

                                EventHighlightCard(
                                    summary = eventSummary,
                                    onClick = { navigateToFishList(trip.id, event.id) }
                                )
                            }

                            HorizontalDivider()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth().padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target Species",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getOnMainColor()
                                    )
                                    IconButton(
                                        onClick = { showSpeciesSelection = true },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = getMainButtonColor(),
                                            contentColor = getOnMainButtonColor()
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Target Species"
                                        )
                                    }
                                }

                                if (eventDetails.targetSpecies.isEmpty()) {
                                    Text(
                                        text = "No target species set for this event.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = getOnSecondaryColor(),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                } else {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val species = eventDetails.targetSpecies.sortedBy { it.name }
                                        items(species) { species ->
                                            InputChip(
                                                selected = true,
                                                onClick = {},
                                                label = { Text(species.name) },
                                                avatar = {
                                                    val thumbnailFlow = remember(species.id) {
                                                        viewModel.speciesThumbnail(species.id)
                                                    }

                                                    val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                                    ThumbnailBox(
                                                        thumbnail = thumbnail,
                                                        imageVector = AppIcons.Default.TargetFish,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                colors = InputChipDefaults.inputChipColors(
                                                    selectedContainerColor = getCardColor().copy(alpha = 0.15f),
                                                    selectedLabelColor = getOnCardColor(),
                                                    selectedLeadingIconColor = getOnCardColor(),
                                                    selectedTrailingIconColor = MaterialTheme.colorScheme.error
                                                ),
                                                border = FilterChipDefaults.filterChipBorder(
                                                    enabled = true,
                                                    selected = true,
                                                    selectedBorderColor = getCardBorderColor(),
                                                    selectedBorderWidth = 1.dp,
                                                    borderColor = getOnChipColor(),
                                                    borderWidth = 1.dp
                                                ),
                                                trailingIcon = {
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.removeEventTargetSpecies(eventId, species.id)
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Clear,
                                                            contentDescription = "Remove",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            FishermanSummary(
                                fishermanCount = eventSummary.fishermanCount,
                                tackleBoxCount = eventSummary.tackleBoxCount,
                                allowOverride = true,
                                onClick = { navigateToSelectEventCrew() }
                            )
                        }
                    }

                    if (showEditEventDialog) {
                        var eventName by remember { mutableStateOf(event.name) }
                        var startDateMillis by remember { mutableLongStateOf(event.startTime) }
                        var endDateMillis by remember { mutableLongStateOf(event.endTime) }

                        var tripStartDateMillis by remember { mutableLongStateOf(trip.startDate?: 0L) }
                        var tripEndDateMillis by remember { mutableLongStateOf(trip.endDate?: 0L) }

                        AlertDialog(
                            onDismissRequest = { showEditEventDialog = false },
                            title = { Text("Edit Event Details") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // TODO - put limit on number of characters
                                    OutlinedTextField(
                                        value = eventName,
                                        onValueChange = { eventName = it },
                                        label = { Text("Event Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Text("Start", style = MaterialTheme.typography.labelLarge)
                                    DateTimePickerButton(
                                        label = "start",
                                        millis = startDateMillis,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { newMillis ->
                                        if (newMillis < tripStartDateMillis) {
                                            Toast.makeText(
                                                context,
                                                "Start cannot be before trip start",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        } else if (newMillis > tripEndDateMillis) {
                                            Toast.makeText(
                                                context,
                                                "Start cannot be after trip end",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        } else {
                                            startDateMillis = newMillis
                                            if (startDateMillis > endDateMillis) endDateMillis =
                                                startDateMillis
                                        }
                                    }

                                    Text("End", style = MaterialTheme.typography.labelLarge)
                                    DateTimePickerButton(
                                        label = "end",
                                        millis = endDateMillis,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { newMillis ->
                                        if (newMillis < startDateMillis) {
                                            Toast.makeText(
                                                context,
                                                "End must be after start",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (newMillis > tripEndDateMillis) {
                                            Toast.makeText(
                                                context,
                                                "End cannot be after trip end",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        } else {
                                            endDateMillis = newMillis
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    scope.launch {
                                        viewModel.upsertEvent(event.copy(
                                            name = eventName,
                                            startTime = startDateMillis,
                                            endTime = endDateMillis
                                        ))
                                        showEditEventDialog = false
                                    }
                                }) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditEventDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }

            if (showSpeciesSelection) {
                SpeciesSelection(
                    items = allSpecies,
                    selectedItems = eventDetails.targetSpecies,
                    onSelected = { selectedSpecies ->
                        if (eventDetails.targetSpecies.contains(selectedSpecies)) {
                            viewModel.removeEventTargetSpecies(eventId, selectedSpecies.id)
                        } else {
                            viewModel.addEventTargetSpecies(eventId, selectedSpecies.id)
                        }
                    },
                    onAdd = {
                        addNewSpecies = true
                        // Route fallback if they need to create an entirely new species row on the fly
                        Toast.makeText(context, "Add species profile functionality", Toast.LENGTH_SHORT).show()
                    },
                    onDone = { showSpeciesSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    thumbnailProvider = { species ->
                        val thumbnailFlow = remember(species.id) {
                            viewModel.speciesThumbnail(species.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.TargetFish,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
            }
        }
    }

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
                            viewModel.addEventTargetSpecies(eventId, species.id)
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
