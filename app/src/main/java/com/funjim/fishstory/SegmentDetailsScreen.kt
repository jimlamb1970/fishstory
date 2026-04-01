package com.funjim.fishstory

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.FishItem
import com.funjim.fishstory.ui.FishermanItem
import com.funjim.fishstory.ui.MapPickerSelectionDialog
import com.funjim.fishstory.ui.PhotoPickerRow
import com.funjim.fishstory.ui.getCurrentLocation
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentDetailsScreen(
    viewModel: MainViewModel,
    segmentId: String,
    tripId: String,
    navigateToSegmentBoatLoad: (String, String) -> Unit,
    navigateToFishermanDetails: (String) -> Unit,
    navigateToAddFish: (tripId: String, segmentId: String, fishId: String?) -> Unit,
    navigateBack: () -> Unit
) {
    val tripWithDetails by viewModel.getTripWithDetails(tripId).collectAsState(initial = null)
    val segmentWithDetails by viewModel.getSegmentWithDetails(segmentId).collectAsState(initial = null)
    val fishList by viewModel.getFishForSegment(segmentId).collectAsState(initial = emptyList())
    val segmentPhotos by viewModel.getPhotosForSegment(segmentId).collectAsState(initial = emptyList())
    
    var showFishermenDialog by remember { mutableStateOf(false) }
    var showEditSegmentDialog by remember { mutableStateOf(false) }
    var fishToUpdateLocation by remember { mutableStateOf<FishWithDetails?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permission granted
        }
    }

    val locationPicker = rememberLocationPickerState(
        viewModel = viewModel,
        existingLat = segmentWithDetails?.segment?.latitude,  // Passed from your DB object
        existingLng = segmentWithDetails?.segment?.longitude,
        onLocationConfirmed = { lat, lng ->
            segmentWithDetails?.segment?.let { segment ->
                scope.launch {
                    viewModel.updateSegment(
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

    val locationPickerFish = rememberLocationPickerState(
        viewModel = viewModel,
        existingLat = fishToUpdateLocation?.latitude,  // Passed from your DB object
        existingLng = fishToUpdateLocation?.longitude,
        onLocationConfirmed = { lat, lng ->
            fishToUpdateLocation?.let { fishDetails ->
                scope.launch {
                    val fish = viewModel.getFishById(fishDetails.id)
                    if (fish != null) {
                        viewModel.updateFish(fish.copy(latitude = lat, longitude = lng))
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segment Details") },
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
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        scope.launch {
                                            val location = viewModel.getCurrentLocation(context)
                                            if (location != null) {
                                                segmentWithDetails?.segment?.let { segment ->
                                                    viewModel.updateSegment(segment.copy(latitude = location.latitude, longitude = location.longitude))
                                                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        )
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                            )

                            if (tripWithDetails?.trip?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Use Trip Location") },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            segmentWithDetails?.segment?.let { segment ->
                                                viewModel.updateSegment(segment.copy(latitude = tripWithDetails?.trip?.latitude, longitude = tripWithDetails?.trip?.longitude))
                                                Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = { Text("Select Location") },
                                onClick = {
                                    menuExpanded = false
                                    locationPicker.openPicker()
                                },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                            )

                            if (segmentWithDetails?.segment?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        segmentWithDetails?.segment?.let { segment ->
                                            scope.launch {
                                                viewModel.updateSegment(
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
                onClick = {
                    val fineLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    val coarseLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )

                    if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
                        coarseLocationPermission == PackageManager.PERMISSION_GRANTED
                    ) {
                        navigateToAddFish(tripId, segmentId, null)
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Fish") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            segmentWithDetails?.let { details ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = details.segment.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        if (details.segment.latitude != null && details.segment.longitude != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View on map",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${details.segment.latitude},${details.segment.longitude}")
                                        val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            )
                        }
                    }
                    Text(
                        text = "Start: ${dateTimeFormatter.format(Date(details.segment.startTime))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "End: ${dateTimeFormatter.format(Date(details.segment.endTime))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }

                PhotoPickerRow(
                    photos = segmentPhotos,
                    onPhotoSelected = { uri ->
                        scope.launch {
                            viewModel.addPhoto(Photo(uri = uri.toString(), segmentId = segmentId))
                        }
                    },
                    onPhotoDeleted = { photo ->
                        scope.launch {
                            viewModel.deletePhoto(photo)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider()

                // The Boat Concept for Segment
                BoatSummary(
                    fishermanCount = details.fishermen.size,
                    onBoatClick = { navigateToSegmentBoatLoad(segmentId, tripId) }
                )

                HorizontalDivider()

                Text(
                    text = "Fish Caught:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(fishList) { fish ->
                        FishItem(
                            fish = fish,
                            viewModel = viewModel,
                            onEdit = {
                                navigateToAddFish(tripId, segmentId, fish.id)
                            },
                            onDelete = {
                                scope.launch {
                                    val fishObj = viewModel.getFishById(fish.id)
                                    if (fishObj != null) {
                                        viewModel.deleteFishObject(fishObj)
                                    }
                                }
                            },
                            onShowMap = {
                                if (fish.latitude != null && fish.longitude != null) {
                                    val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${fish.latitude},${fish.longitude}")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onUpdateLocation = {
                                fishToUpdateLocation = fish
                                locationPickerFish.openPicker()
                            }
                        )
                    }
                }

                if (showEditSegmentDialog) {
                    var segmentName by remember { mutableStateOf(details.segment.name) }
                    var startDateMillis by remember { mutableLongStateOf(details.segment.startTime) }
                    var endDateMillis by remember { mutableLongStateOf(details.segment.endTime) }

                    var tripStartDateMillis by remember { mutableLongStateOf(tripWithDetails?.trip?.startDate?: 0L) }
                    var tripEndDateMillis by remember { mutableLongStateOf(tripWithDetails?.trip?.endDate?: 0L) }

                    AlertDialog(
                        onDismissRequest = { showEditSegmentDialog = false },
                        title = { Text("Edit Segment Details") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextField(
                                    value = segmentName,
                                    onValueChange = { segmentName = it },
                                    label = { Text("Segment Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text("Start", style = MaterialTheme.typography.labelLarge)
                                DateTimePickerButton(
                                    label = "start",
                                    millis = startDateMillis,
                                    modifier = Modifier.fillMaxWidth()
                                ) { newMillis ->
                                    if (newMillis < tripStartDateMillis) {
                                        Toast.makeText(context, "Start cannot be before trip start", Toast.LENGTH_SHORT)
                                            .show()
                                    } else if (newMillis > tripEndDateMillis) {
                                        Toast.makeText(context, "Start cannot be after trip end", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        startDateMillis = newMillis
                                        if (startDateMillis > endDateMillis) endDateMillis = startDateMillis
                                    }
                                }

                                Text("End", style = MaterialTheme.typography.labelLarge)
                                DateTimePickerButton(
                                    label = "end",
                                    millis = endDateMillis,
                                    modifier = Modifier.fillMaxWidth()
                                ) { newMillis ->
                                    if (newMillis < startDateMillis) {
                                        Toast.makeText(context, "End must be after start", Toast.LENGTH_SHORT).show()
                                    } else if (newMillis > tripEndDateMillis) {
                                        Toast.makeText(context, "End cannot be after trip end", Toast.LENGTH_SHORT)
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
                                    viewModel.updateSegment(details.segment.copy(
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

                if (showFishermenDialog) {
                    AlertDialog(
                        onDismissRequest = { showFishermenDialog = false },
                        title = { Text("Fishermen in Segment Boat") },
                        text = {
                            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                                items(details.fishermen) { fisherman ->
                                    FishermanItem(
                                        fisherman = fisherman,
                                        onDelete = {
                                            scope.launch {
                                                viewModel.deleteFishermanFromSegment(segmentId, fisherman.id)
                                            }
                                        },
                                        onClick = {
                                            showFishermenDialog = false
                                            navigateToFishermanDetails(fisherman.id)
                                        }
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showFishermenDialog = false }) {
                                Text("Close")
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
