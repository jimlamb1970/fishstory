package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.EventWithSpecies
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.FishermanSummary
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.EventItem
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.TargetSpeciesRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.TripHighlightCard
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnSecondaryColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.TripDetailsUiState
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    viewModel: TripViewModel,
    tripId: String,
    navigateToSelectTripCrew: (String) -> Unit,
    navigateToFishList: (String?) -> Unit,
    navigateToAddEvent: (String) -> Unit,
    navigateToEventDetails: (String) -> Unit,
    navigateBack: () -> Unit
) {
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
    }

    var showEditTripDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    var addNewSpecies by remember { mutableStateOf(false) }
    var addSpeciesName by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val now = System.currentTimeMillis()

    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    var eventToDelete by remember { mutableStateOf<EventSummary?>(null) }
    var eventToUpdateLocation by remember { mutableStateOf<EventSummary?>(null) }
    var updateTripLocation by remember { mutableStateOf(false) }

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = selectedTrip?.latitude,  // Passed from your DB object
        existingLng = selectedTrip?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            selectedTrip?.let { trip ->
                scope.launch {
                    viewModel.saveTrip(trip.copy(latitude = lat, longitude = lng))
                }
            }
        }
    )

    val locationPickerEvent = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = eventToUpdateLocation?.event?.latitude,  // Passed from your DB object
        existingLng = eventToUpdateLocation?.event?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            eventToUpdateLocation?.event?.let { event ->
                scope.launch {
                    viewModel.upsertEvent(event.copy(latitude = lat, longitude = lng))
                }
            }
        }
    )

    val uiState by viewModel.uiDetailState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is TripDetailsUiState.Loading -> {
            // Keeps the screen entirely blank or showing a spinner
            // until all 3 database points arrive
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is TripDetailsUiState.Success -> {
            val trip = state.details.trip
            selectedTrip = trip
            val details = state.details
            val summary = state.summary
            val eventSummaries = state.eventSummaries

            val tripMasterTargets: List<Species> = details.targetSpecies
            val activeEvents: List<EventWithSpecies> = details.events

            val speciesUsageMap: Map<String, Int> = activeEvents
                .flatMap { it.targetSpecies }
                .groupingBy { it.id }
                .eachCount() // Returns a Map<String, Int> where Key = speciesId, Value = count

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Trip Details") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showEditTripDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Trip")
                            }
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    if (hasLocationPermission) {
                                        DropdownMenuItem(
                                            text = { Text("Use Current Location") },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    val location = viewModel.fetchLocation()
                                                    if (location != null) {
                                                        viewModel.saveTrip(
                                                            trip.copy(
                                                                latitude = location.latitude,
                                                                longitude = location.longitude
                                                            )
                                                        )
                                                        Toast.makeText(
                                                            context,
                                                            "Location updated",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Could not get location",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.MyLocation,
                                                    contentDescription = null,
                                                    tint =
                                                        if (trip.latitude != null) Color(0xFF4CAF50)
                                                        else LocalContentColor.current
                                                )
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text("Select on Map") },
                                        onClick = {
                                            menuExpanded = false
                                            locationPicker.openPicker()
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Map,
                                                contentDescription = null,
                                                tint =
                                                    if (trip.latitude != null) Color(0xFF4CAF50)
                                                    else LocalContentColor.current
                                            )
                                        }
                                    )

                                    if (trip.latitude != null) {
                                        DropdownMenuItem(
                                            text = { Text("Clear Location") },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    viewModel.saveTrip(
                                                        trip.copy(
                                                            latitude = null,
                                                            longitude = null
                                                        )
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Location cleared",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
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
                }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    LazyColumn(horizontalAlignment = Alignment.Start) {
                        item {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = details.trip.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getOnMainColor()
                                )
                                if (details.trip.latitude != null && details.trip.longitude != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "View on map",
                                        tint = getOnMainColor(),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                val mapUri =
                                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${details.trip.latitude},${details.trip.longitude}")
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
                                }
                            }
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "Start: ${dateTimeFormatter.format(Date(details.trip.startDate))}",
                                style = MaterialTheme.typography.titleMedium,
                                color = getOnSecondaryColor()
                            )
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "End: ${dateTimeFormatter.format(Date(details.trip.endDate))}",
                                style = MaterialTheme.typography.titleMedium,
                                color = getOnSecondaryColor()
                            )

                            PhotoPickerRow(
                                photos = details.photos,
                                onPhotoSelected = { uri ->
                                    viewModel.addTripPhoto(tripId = tripId, uri = uri, true)
                                },
                                onPhotoTaken = { uri ->
                                    viewModel.addTripPhoto(tripId = tripId, uri = uri, false)
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deleteTripPhoto(tripId, photo.id)
                                }
                            )

                            if (summary.fishCaught != 0 || now >= trip.startDate) {
                                HorizontalDivider()

                                TripHighlightCard(
                                    summary = summary,
                                    onClick = { navigateToFishList(tripId) }
                                )
                            }

                            HorizontalDivider()

                            TargetSpeciesRow(
                                items = details.targetSpecies,
                                onAdd = { showSpeciesSelection = true },
                                onDelete = { species ->
                                    viewModel.removeTripTargetSpecies(tripId, species.id)
                                },
                                thumbnailProvider = { species ->
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
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )

                            HorizontalDivider()

                            // The Boat Concept
                            FishermanSummary(
                                fishermanCount = summary.fishermanCount,
                                tackleBoxCount = summary.tackleBoxCount,
                                onClick = { navigateToSelectTripCrew(tripId) }
                            )

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Events",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "(${eventSummaries.size})",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        navigateToAddEvent(tripId)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = getMainButtonColor(),
                                        contentColor = getOnMainButtonColor()
                                    ),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                                }
                            }
                        }

                        val totalItems = eventSummaries.size
                        itemsIndexed(eventSummaries) { index, eventSummary ->
                            EventItem(
                                item = eventSummary,
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 4.dp
                                ),
                                index = index,
                                totalItems = totalItems,
                                thumbnailFlow = viewModel.eventThumbnail(eventSummary.event.id),
                                onClick = { navigateToEventDetails(eventSummary.event.id) },
                                onDelete = { eventToDelete = eventSummary },
                                onSetLocation = if (hasLocationPermission) {
                                    {
                                        scope.launch {
                                            val location = viewModel.fetchLocation()
                                            if (location != null) {
                                                viewModel.upsertEvent(
                                                    eventSummary.event.copy(
                                                        latitude = location.latitude,
                                                        longitude = location.longitude
                                                    )
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "Location updated",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else null,
                                onSelectLocation = {
                                    eventToUpdateLocation = eventSummary
                                    locationPickerEvent.openPicker()
                                },
                                onUseTripLocation = if (details.trip.latitude != null) {
                                    {
                                        scope.launch {
                                            viewModel.upsertEvent(
                                                eventSummary.event.copy(
                                                    latitude = details.trip.latitude,
                                                    longitude = details.trip.longitude
                                                )
                                            )
                                        }
                                    }
                                } else null,
                                onClearLocation = {
                                    scope.launch {
                                        viewModel.upsertEvent(
                                            eventSummary.event.copy(
                                                latitude = null,
                                                longitude = null
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }

                    if (showEditTripDialog) {
                        var tripName by remember { mutableStateOf(details.trip.name) }
                        var startDateMillis by remember { mutableLongStateOf(details.trip.startDate) }
                        var endDateMillis by remember { mutableLongStateOf(details.trip.endDate) }

                        AlertDialog(
                            onDismissRequest = { showEditTripDialog = false },
                            title = { Text("Edit Trip Details") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // TODO - put limit on number of characters
                                    OutlinedTextField(
                                        value = tripName,
                                        onValueChange = { tripName = it },
                                        label = { Text("Trip Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Text("Start", style = MaterialTheme.typography.labelLarge)
                                    DateTimePickerButton(
                                        label = "start",
                                        millis = startDateMillis,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { newMillis ->
                                        startDateMillis = newMillis
                                        if (startDateMillis > endDateMillis) endDateMillis =
                                            startDateMillis
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
                                        } else {
                                            endDateMillis = newMillis
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    scope.launch {
                                        viewModel.saveTrip(
                                            details.trip.copy(
                                                name = tripName,
                                                startDate = startDateMillis,
                                                endDate = endDateMillis
                                            )
                                        )
                                        showEditTripDialog = false
                                    }
                                }) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditTripDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }

            // DELETE CONFIRMATION
            eventToDelete?.let { item ->
                AlertDialog(
                    onDismissRequest = { eventToDelete = null },
                    title = { Text("Delete Event?") },
                    text = {
                        Text(
                            """Are you sure you want to delete '${item.event.name}'?

This cannot be undone.

All fish (${item.fishCaught}) associated with this event will also be deleted."""
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteEvent(item.event)
                                eventToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { eventToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showSpeciesSelection) {
                SpeciesSelection(
                    items = allSpecies,
                    selectedItems = details.targetSpecies,
                    onSelected = { selectedSpecies ->
                        viewModel.addTripTargetSpecies(tripId, selectedSpecies.id)
                    },
                    onUnselected = { selectedSpecies ->
                        viewModel.removeTripTargetSpecies(tripId, selectedSpecies.id)
                    },
                    onAdd = {
                        addNewSpecies = true
                    },
                    onDone = { showSpeciesSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    usageMap = speciesUsageMap,
                    maxUsage = details.events.size,
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
                            viewModel.addTripTargetSpecies(tripId, species.id)
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
