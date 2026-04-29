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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.ui.utils.FishermanSummary
import com.funjim.fishstory.ui.utils.DateTimePickerButton
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
fun SegmentDetailsScreen(
    viewModel: TripViewModel,
    tripId: String,
    segmentId: String,
    navigateToSelectSegmentCrew: () -> Unit,
    navigateToAddFish: () -> Unit,
    navigateToFishList: () -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(segmentId) {
        viewModel.selectTrip(tripId)
        viewModel.selectSegment(segmentId)
    }

    val tripSummary by viewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val segmentSummary by viewModel.selectedEventSummary.collectAsStateWithLifecycle()

    val segmentPhotos by viewModel.segmentPhotos.collectAsState(initial = emptyList())
    
    var showEditSegmentDialog by remember { mutableStateOf(false) }
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
                    val location = viewModel.getTripCurrentLocation(context)
                    if (location != null) {
                        segmentSummary?.event?.let { segment ->
                            viewModel.upsertEvent(
                                segment.copy(
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
        existingLat = segmentSummary?.event?.latitude,  // Passed from your DB object
        existingLng = segmentSummary?.event?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            segmentSummary?.event?.let { segment ->
                scope.launch {
                    viewModel.upsertEvent(
                        segment.copy(
                            latitude = lat,
                            longitude = lng
                        )
                    )
                    Toast.makeText(context, "Segment location updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segment Details") },
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
                    IconButton(onClick = { showEditSegmentDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Segment")
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
                                            val location = viewModel.getTripCurrentLocation(context)
                                            if (location != null) {
                                                segmentSummary?.event?.let { segment ->
                                                    viewModel.upsertEvent(
                                                        segment.copy(
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
                                    Icon(Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (segmentSummary?.event?.latitude != null)
                                            Color(0xFF4CAF50)
                                        else
                                            LocalContentColor.current)
                                }
                            )

                            if (tripSummary?.trip?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Use Trip Location") },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            segmentSummary?.event?.let { segment ->
                                                viewModel.upsertEvent(
                                                    segment.copy(
                                                        latitude = tripSummary?.trip?.latitude,
                                                        longitude = tripSummary?.trip?.longitude
                                                    )
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "Location updated",
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (segmentSummary?.event?.latitude != null)
                                                Color(0xFF4CAF50)
                                            else
                                                LocalContentColor.current)
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Select Location") },
                                onClick = {
                                    menuExpanded = false
                                    locationPicker.openPicker()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (segmentSummary?.event?.latitude != null)
                                            Color(0xFF4CAF50)
                                        else
                                            LocalContentColor.current)
                                }
                            )

                            if (segmentSummary?.event?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        segmentSummary?.event?.let { segment ->
                                            scope.launch {
                                                viewModel.upsertEvent(
                                                    segment.copy(latitude = null, longitude = null)
                                                )
                                                Toast.makeText(context, "Location cleared", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOn,
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
            segmentSummary?.let { details ->
                LazyColumn(horizontalAlignment = Alignment.Start) {
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = details.event.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (details.event.latitude != null && details.event.longitude != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val mapUri =
                                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${details.event.latitude},${details.event.longitude}")
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
                            text = "Start: ${dateTimeFormatter.format(Date(details.event.startTime))}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "End: ${dateTimeFormatter.format(Date(details.event.endTime))}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        PhotoPickerRow(
                            photos = segmentPhotos,
                            onPhotoSelected = { uri ->
                                scope.launch {
                                    viewModel.addPhoto(Photo(uri = uri.toString(), eventId = segmentId))
                                }
                            },
                            onPhotoDeleted = { photo ->
                                scope.launch {
                                    viewModel.deletePhoto(photo)
                                }
                            }
                        )

                        if (details.fishCaught != 0 || now >= details.event.startTime) {
                            HorizontalDivider()

                            SegmentHighlightCard(
                                summary = details,
                                onClick = { navigateToFishList() }
                            )
                        }

                        HorizontalDivider()

                        FishermanSummary(
                            fishermanCount = details.fishermanCount,
                            tackleBoxCount = details.tackleBoxCount,
                            onClick = { navigateToSelectSegmentCrew() }
                        )
                    }
                }

                if (showEditSegmentDialog) {
                    var segmentName by remember { mutableStateOf(details.event.name) }
                    var startDateMillis by remember { mutableLongStateOf(details.event.startTime) }
                    var endDateMillis by remember { mutableLongStateOf(details.event.endTime) }

                    var tripStartDateMillis by remember { mutableLongStateOf(tripSummary?.trip?.startDate?: 0L) }
                    var tripEndDateMillis by remember { mutableLongStateOf(tripSummary?.trip?.endDate?: 0L) }

                    AlertDialog(
                        onDismissRequest = { showEditSegmentDialog = false },
                        title = { Text("Edit Segment Details") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // TODO - put limit on number of characters
                                OutlinedTextField(
                                    value = segmentName,
                                    onValueChange = { segmentName = it },
                                    label = { Text("Segment Name") },
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
                                        name = segmentName,
                                        startTime = startDateMillis,
                                        endTime = endDateMillis
                                    ))
                                    showEditSegmentDialog = false
                                }
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditSegmentDialog = false }) {
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
@Composable
fun SegmentHighlightCard(
    summary: EventSummary,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Fish Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            )

            // Top Row: The Numbers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(
                    label = "CAUGHT",
                    value = "${summary.fishCaught}",
                    color = MaterialTheme.colorScheme.primary)
                StatItem(
                    label = "KEPT",
                    value = "${summary.fishKept}",
                    color = Color(0xFF4CAF50)) // Harvest Green
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Bottom Row: The Achievements
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AchievementItem(
                    icon = Icons.Default.Person,
                    label = "Top Rod",
                    name = summary.mostCaughtName,
                    description = "(${summary.mostCaught} fish)",
                    modifier = Modifier.weight(1f))
                AchievementItem(
                    icon = Icons.Default.Star,
                    label = "Big Fish",
                    name = summary.bigFishName,
                    description = "(${summary.bigFishLength}\" : ${summary.bigFishSpecies})",
                    modifier = Modifier.weight(1f))
            }
        }
    }
}
