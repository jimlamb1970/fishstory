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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.SegmentSummary
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.utils.FishermanSummary
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.SegmentItem
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
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
    navigateToFishList: () -> Unit,
    navigateToAddSegment: (String) -> Unit,
    navigateToSegmentDetails: (String) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
    }

    val tripSummary by viewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val segmentSummaries by viewModel.segmentSummaries.collectAsStateWithLifecycle()

    val tripPhotos by viewModel.tripPhotos.collectAsState(initial = emptyList())

    var showEditTripDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val now = System.currentTimeMillis()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            scope.launch {
                val location = viewModel.getTripCurrentLocation(context)
                if (location != null) {
                    tripSummary?.trip?.let { trip ->
                        viewModel.saveTrip(trip.copy(latitude = location.latitude, longitude = location.longitude))
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

    var segmentToUpdateLocation by remember { mutableStateOf<SegmentSummary?>(null) }

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = tripSummary?.trip?.latitude,  // Passed from your DB object
        existingLng = tripSummary?.trip?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            tripSummary?.trip?.let { trip ->
                scope.launch {
                    viewModel.saveTrip(trip.copy(latitude = lat, longitude = lng))
                }
            }
        }
    )

    val locationPickerSegment = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = segmentToUpdateLocation?.segment?.latitude,  // Passed from your DB object
        existingLng = segmentToUpdateLocation?.segment?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            segmentToUpdateLocation?.segment?.let { segment ->
                scope.launch {
                    viewModel.upsertSegment(segment.copy(latitude = lat, longitude = lng))
                }
            }
        }
    )

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                                                tripSummary?.trip?.let { trip ->
                                                    viewModel.saveTrip(
                                                        trip.copy(
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
                                        tint = if (tripSummary?.trip?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Select Location") },
                                onClick = {
                                    menuExpanded = false
                                    locationPicker.openPicker()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (tripSummary?.trip?.latitude != null)
                                            Color(0xFF4CAF50)
                                        else
                                            LocalContentColor.current)
                                }
                            )

                            if (tripSummary?.trip?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        tripSummary?.trip?.let { trip ->
                                            scope.launch {
                                                viewModel.saveTrip(
                                                    trip.copy(latitude = null, longitude = null)
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
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.Start) {
            tripSummary?.let { details ->
                LazyColumn(horizontalAlignment = Alignment.Start) {
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = details.trip.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (details.trip.latitude != null && details.trip.longitude != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = MaterialTheme.colorScheme.primary,
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "End: ${dateTimeFormatter.format(Date(details.trip.endDate))}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        PhotoPickerRow(
                            photos = tripPhotos,
                            onPhotoSelected = { uri ->
                                scope.launch {
                                    viewModel.addPhoto(Photo(uri = uri.toString(), tripId = tripId))
                                }
                            },
                            onPhotoDeleted = { photo ->
                                scope.launch {
                                    viewModel.deletePhoto(photo)
                                }
                            }
                        )

                        if (details.totalCaught != 0 || now >= details.trip.startDate) {
                            HorizontalDivider()

                            TripHighlightCard(
                                tripSummary = details,
                                onClick = { navigateToFishList() }
                            )
                        }

                        HorizontalDivider()

                        // The Boat Concept
                        FishermanSummary(
                            fishermanCount = details.fishermanCount,
                            tackleBoxCount = details.tackleBoxCount,
                            onClick = { navigateToSelectTripCrew(tripId) }
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Segments",
                                style = MaterialTheme.typography.titleLarge
                            )
                            IconButton(onClick = {
                                navigateToAddSegment(tripId)
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Segment")
                            }
                        }
                    }

                    items(segmentSummaries) { segmentSummary ->
                        SegmentItem(
                            segmentSummary = segmentSummary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteSegment(segmentSummary.segment)
                                }
                            },
                            onClick = {
                                navigateToSegmentDetails(segmentSummary.segment.id)
                            },
                            onSetLocation = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    scope.launch {
                                        val location = viewModel.getTripCurrentLocation(context)
                                        if (location != null) {
                                            viewModel.upsertSegment(segmentSummary.segment.copy(latitude = location.latitude, longitude = location.longitude))
                                            Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            onSelectLocation = {
                                segmentToUpdateLocation = segmentSummary
                                locationPickerSegment.openPicker()
                            },
                            onUseTripLocation = if (details.trip.latitude != null) {
                                {
                                    scope.launch {
                                        viewModel.upsertSegment(
                                            segmentSummary.segment.copy(
                                                latitude = details.trip.latitude,
                                                longitude = details.trip.longitude
                                            )
                                        )
                                    }
                                }
                            } else null,
                            onClearLocation = {
                                scope.launch {
                                    viewModel.upsertSegment(segmentSummary.segment.copy(latitude = null, longitude = null))
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
                                TextField(
                                    value = tripName,
                                    onValueChange = { tripName = it },
                                    label = { Text("Trip Name") },
                                    modifier = Modifier.fillMaxWidth()
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
                                    viewModel.saveTrip(details.trip.copy(
                                        name = tripName,
                                        startDate = startDateMillis,
                                        endDate = endDateMillis
                                    ))
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
            } ?: run {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun TripHighlightCard(
    tripSummary: TripSummary,
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
                    value = "${tripSummary.totalCaught}",
                    color = MaterialTheme.colorScheme.primary)
                StatItem(
                    label = "KEPT",
                    value = "${tripSummary.totalKept}",
                    color = Color(0xFF4CAF50)) // Harvest Green
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Bottom Row: The Achievements
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AchievementItem(
                    icon = Icons.Default.Person,
                    label = "Top Rod",
                    name = tripSummary.mostCaughtName,
                    modifier = Modifier.weight(1f))
                AchievementItem(
                    icon = Icons.Default.Star,
                    label = "Big Fish",
                    name = tripSummary.bigFishName,
                    modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun AchievementItem(
    icon: ImageVector,
    label: String,
    name: String?,
    description: String? = null,
    modifier: Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    // Parent Column to stack Label over the Icon/Name group
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier) {
        // 1. Label at the very top
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp) // Space before the icon/name
        )

        // 2. Row to keep Icon and Name on the same line
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp), // Slightly smaller to let name lead
                tint = color
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = name ?: "---",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = color
            )
        }

        // 3. Description (Optional) stays at the bottom
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}