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
import com.funjim.fishstory.model.SegmentSummary
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.DateTimePickerButton
import com.funjim.fishstory.ui.PhotoPickerRow
import com.funjim.fishstory.ui.TackleBoxSummary
import com.funjim.fishstory.ui.rememberLocationPickerState
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
    segmentId: String,
    navigateToSegmentBoatLoad: () -> Unit,
    navigateToTackleBoxes: (String) -> Unit,
    navigateToAddFish: () -> Unit,
    navigateToFishList: () -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(segmentId) {
        viewModel.selectSegment(segmentId)
    }

    val tripSummary by viewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val segmentSummary by viewModel.selectedSegmentSummary.collectAsStateWithLifecycle()

    val segmentPhotos by viewModel.segmentPhotos.collectAsState(initial = emptyList())
    
    var showEditSegmentDialog by remember { mutableStateOf(false) }
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
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = segmentSummary?.segment?.latitude,  // Passed from your DB object
        existingLng = segmentSummary?.segment?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            segmentSummary?.segment?.let { segment ->
                scope.launch {
                    viewModel.upsertSegment(
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
                                                segmentSummary?.segment?.let { segment ->
                                                    viewModel.upsertSegment(segment.copy(latitude = location.latitude, longitude = location.longitude))
                                                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
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
                                        tint = if (segmentSummary?.segment?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )

                            if (tripSummary?.trip?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Use Trip Location") },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            segmentSummary?.segment?.let { segment ->
                                                viewModel.upsertSegment(segment.copy(latitude = tripSummary?.trip?.latitude, longitude = tripSummary?.trip?.longitude))
                                                Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (segmentSummary?.segment?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
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
                                        tint = if (segmentSummary?.segment?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )

                            if (segmentSummary?.segment?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        segmentSummary?.segment?.let { segment ->
                                            scope.launch {
                                                viewModel.upsertSegment(
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
                        navigateToAddFish()
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
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.Start) {
            segmentSummary?.let { details ->
                LazyColumn(horizontalAlignment = Alignment.Start) {
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                            val mapUri =
                                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${details.segment.latitude},${details.segment.longitude}")
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
                            text = "Start: ${dateTimeFormatter.format(Date(details.segment.startTime))}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "End: ${dateTimeFormatter.format(Date(details.segment.endTime))}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )

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
                            }
                        )

                        HorizontalDivider()

                        // The Boat Concept for Segment
                        BoatSummary(
                            fishermanCount = details.fishermanCount,
                            onBoatClick = { navigateToSegmentBoatLoad() }
                        )

                        HorizontalDivider()

                        // The Gear Summary
                        TackleBoxSummary(
                            fishermanCount = details.tackleBoxCount,
                            onClick = { navigateToTackleBoxes(segmentId) }
                        )

                        HorizontalDivider()

                        // TODO -- add more information to summaries so that more information can be displayed
                        SegmentHighlightCard(
                            summary = details,
                            onClick = {
                                navigateToFishList()
                            }
                        )
                    }
                }

                if (showEditSegmentDialog) {
                    var segmentName by remember { mutableStateOf(details.segment.name) }
                    var startDateMillis by remember { mutableLongStateOf(details.segment.startTime) }
                    var endDateMillis by remember { mutableLongStateOf(details.segment.endTime) }

                    var tripStartDateMillis by remember { mutableLongStateOf(tripSummary?.trip?.startDate?: 0L) }
                    var tripEndDateMillis by remember { mutableLongStateOf(tripSummary?.trip?.endDate?: 0L) }

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
                                    viewModel.upsertSegment(details.segment.copy(
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
    summary: SegmentSummary,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                StatItem(label = "CAUGHT", value = "${summary.fishCaught}", color = MaterialTheme.colorScheme.primary)
                StatItem(label = "KEPT", value = "${summary.fishKept}", color = Color(0xFF4CAF50)) // Harvest Green
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Bottom Row: The Achievements
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AchievementItem(icon = Icons.Default.Person, label = "Top Rod", name = summary.mostFish)
                AchievementItem(icon = Icons.Default.Star, label = "Big Fish", name = summary.biggestFish)
            }
        }
    }
}
