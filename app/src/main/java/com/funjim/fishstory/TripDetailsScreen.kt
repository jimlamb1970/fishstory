package com.funjim.fishstory

import android.Manifest
import android.app.DatePickerDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.FishermanItem
import com.funjim.fishstory.ui.PhotoPickerRow
import com.funjim.fishstory.ui.SegmentItem
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    viewModel: MainViewModel, 
    tripId: Int, 
    navigateToSegmentDetails: (Int) -> Unit,
    navigateToFishermanDetails: (Int) -> Unit,
    navigateToBoatLoad: (Int) -> Unit,
    navigateBack: () -> Unit
) {
    val tripWithDetails by viewModel.getTripWithDetails(tripId).collectAsState(initial = null)
    val segmentsWithDetails by viewModel.getSegmentsWithDetailsForTrip(tripId).collectAsState(initial = emptyList())
    val tripPhotos by viewModel.getPhotosForTrip(tripId).collectAsState(initial = emptyList())
    
    var showFishermenDialog by remember { mutableStateOf(false) }
    var showEditTripDialog by remember { mutableStateOf(false) }
    var showAddSegmentDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormatter = remember { 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            scope.launch {
                val location = viewModel.getCurrentLocation(context)
                if (location != null) {
                    tripWithDetails?.trip?.let { trip ->
                        viewModel.updateTrip(trip.copy(latitude = location.latitude, longitude = location.longitude))
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Details") },
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
                                text = { Text("Set GPS Location") },
                                onClick = {
                                    menuExpanded = false
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        scope.launch {
                                            val location = viewModel.getCurrentLocation(context)
                                            if (location != null) {
                                                tripWithDetails?.trip?.let { trip ->
                                                    viewModel.updateTrip(trip.copy(latitude = location.latitude, longitude = location.longitude))
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
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            tripWithDetails?.let { details ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Trip: ${details.trip.name}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        if (details.trip.latitude != null && details.trip.longitude != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View on map",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${details.trip.latitude},${details.trip.longitude}")
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
                        text = "Date: ${dateFormatter.format(Date(details.trip.startDate))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }

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
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // The Boat Concept
                BoatSummary(
                    fishermanCount = details.fishermen.size,
                    onBoatClick = { showFishermenDialog = true },
                    onAddClick = { navigateToBoatLoad(tripId) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Segments",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { showAddSegmentDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Segment")
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(segmentsWithDetails) { segmentDetails ->
                        SegmentItem(
                            segmentWithDetails = segmentDetails,
                            onEdit = null,
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteSegment(segmentDetails.segment)
                                }
                            },
                            onClick = {
                                navigateToSegmentDetails(segmentDetails.segment.id)
                            },
                            onSetLocation = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    scope.launch {
                                        val location = viewModel.getCurrentLocation(context)
                                        if (location != null) {
                                            viewModel.updateSegment(segmentDetails.segment.copy(latitude = location.latitude, longitude = location.longitude))
                                            Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            }
                        )
                    }
                }

                if (showEditTripDialog) {
                    var tripName by remember { mutableStateOf(details.trip.name) }
                    var tripDate by remember { mutableLongStateOf(details.trip.startDate) }
                    
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = tripDate
                    }
                    
                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            tripDate = selectedCalendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    AlertDialog(
                        onDismissRequest = { showEditTripDialog = false },
                        title = { Text("Edit Trip Details") },
                        text = {
                            Column {
                                TextField(
                                    value = tripName,
                                    onValueChange = { tripName = it },
                                    label = { Text("Trip Name") }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { datePickerDialog.show() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Change Date: ${dateFormatter.format(Date(tripDate))}")
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                scope.launch {
                                    viewModel.updateTrip(details.trip.copy(name = tripName, startDate = tripDate))
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

                if (showAddSegmentDialog) {
                    var segmentName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showAddSegmentDialog = false },
                        title = { Text("Add New Segment") },
                        text = {
                            TextField(
                                value = segmentName,
                                onValueChange = { segmentName = it },
                                label = { Text("Segment Name") }
                            )
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (segmentName.isNotBlank()) {
                                    scope.launch {
                                        viewModel.addSegment(
                                            Segment(
                                                tripId = tripId,
                                                name = segmentName,
                                                startTime = System.currentTimeMillis()
                                            )
                                        )
                                        showAddSegmentDialog = false
                                    }
                                }
                            }) {
                                Text("Add")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddSegmentDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showFishermenDialog) {
                    AlertDialog(
                        onDismissRequest = { showFishermenDialog = false },
                        title = { Text("Fishermen in Boat") },
                        text = {
                            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                                items(details.fishermen) { fisherman ->
                                    FishermanItem(
                                        fisherman = fisherman,
                                        onDelete = {
                                            scope.launch {
                                                viewModel.deleteFishermanFromTrip(tripId, fisherman.id)
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
