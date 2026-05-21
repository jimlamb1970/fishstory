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
import com.funjim.fishstory.ui.utils.FishermanSummary
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.EventHighlightCard
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    viewModel: TripViewModel,
    tripId: String,
    eventId: String,
    navigateToSelectEventCrew: () -> Unit,
    navigateToAddFish: () -> Unit,
    navigateToFishList: (String?, String?) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(eventId) {
        // TODO -- selectTrip needs to be done as well as selectEvent
        viewModel.selectTrip(tripId)
        viewModel.selectEvent(eventId)
    }

    val eventSummary by viewModel.selectedEventSummary.collectAsStateWithLifecycle()

    val eventPhotos by viewModel.eventPhotos.collectAsState(initial = emptyList())
    
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
                        eventSummary?.event?.let { event ->
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
        existingLat = eventSummary?.event?.latitude,  // Passed from your DB object
        existingLng = eventSummary?.event?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            eventSummary?.event?.let { event ->
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

    Scaffold(
        topBar = {
            val currentEvent = eventSummary?.event
            val currentTrip = eventSummary?.trip

            val eventLat = currentEvent?.latitude
            val tripLat = currentTrip?.latitude

            // Precedence logic: Use Event if it exists, otherwise use Trip
            val activeLat = eventLat ?: tripLat

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
                                                eventSummary?.event?.let { event ->
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
                                        eventSummary?.event?.let { event ->
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Fish") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.Start) {
            eventSummary?.let { details ->
                LazyColumn(horizontalAlignment = Alignment.Start) {
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = details.event.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary

                            )
                            val displayLat = details.event.latitude ?: details.trip.latitude
                            val displayLng = details.event.longitude ?: details.trip.longitude
                            val hasAnyLocation = displayLat != null && displayLng != null

                            if (hasAnyLocation) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = MaterialTheme.colorScheme.primary,
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
                                if (details.event.latitude == null) {
                                    Text(
                                        text = "(Trip)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Start: ${dateTimeFormatter.format(Date(details.event.startTime))}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "End: ${dateTimeFormatter.format(Date(details.event.endTime))}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        PhotoPickerRow(
                            photos = eventPhotos,
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

                        if (details.fishCaught != 0 || now >= details.event.startTime) {
                            HorizontalDivider()

                            EventHighlightCard(
                                summary = details,
                                onClick = { navigateToFishList(details.trip.id, details.event.id) }
                            )
                        }

                        HorizontalDivider()

                        FishermanSummary(
                            fishermanCount = details.fishermanCount,
                            tackleBoxCount = details.tackleBoxCount,
                            allowOverride = true,
                            onClick = { navigateToSelectEventCrew() }
                        )
                    }
                }

                if (showEditEventDialog) {
                    var eventName by remember { mutableStateOf(details.event.name) }
                    var startDateMillis by remember { mutableLongStateOf(details.event.startTime) }
                    var endDateMillis by remember { mutableLongStateOf(details.event.endTime) }

                    var tripStartDateMillis by remember { mutableLongStateOf(eventSummary?.trip?.startDate?: 0L) }
                    var tripEndDateMillis by remember { mutableLongStateOf(eventSummary?.trip?.endDate?: 0L) }

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
                                    viewModel.upsertEvent(details.event.copy(
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
            } ?: run {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
