package com.funjim.fishstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.DateTimePickerButton
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftSegmentDetailsScreen(
    viewModel: TripViewModel,
    segmentId: String,
    navigateToLoadBoatForSegment: () -> Unit,
    navigateBack: () -> Unit
) {
    var showEditSegmentDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }

    // Draft values from ViewModel — survive navigation
    val draftSegments by viewModel.draftSegments.collectAsState()
    val segment = remember(draftSegments, segmentId) {
        draftSegments.find { it.id == segmentId }
    }

    val draftSegmentFishermanIds by viewModel.draftSegmentFishermanIds.collectAsState()
    val fishermanCount = remember(draftSegmentFishermanIds, segmentId) {
        draftSegmentFishermanIds[segmentId]?.size ?: 0
    }

    // Draft trip values for pre-populating dates when tripId == 0
    val draftTripStartDate by viewModel.draftTripStartDate.collectAsState()
    val draftTripEndDate by viewModel.draftTripEndDate.collectAsState()
    val draftLatitude by viewModel.draftLatitude.collectAsState()
    val draftLongitude by viewModel.draftLongitude.collectAsState()

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
        existingLat = segment?.latitude,  // Passed from your DB object
        existingLng = segment?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            segment?.let {
                viewModel.upsertDraftSegment(it.copy(latitude = lat, longitude = lng))
                Toast.makeText(context, "Segment location updated", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segment Details (Draft)") },
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
                                            val location = viewModel.getCurrentLocation(context)
                                            if (location != null) {
                                                segment?.let {
                                                    viewModel.upsertDraftSegment(it.copy(latitude = location.latitude, longitude = location.longitude))
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
                                        tint = if (segment?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )

                            if (draftLatitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Use Trip Location") },
                                    onClick = {
                                        menuExpanded = false
                                        segment?.let {
                                            viewModel.upsertDraftSegment(it.copy(latitude = draftLatitude, longitude = draftLongitude))
                                            Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (segment?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
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
                                        tint = if (segment?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )

                            if (segment?.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        segment?.let {
                                            viewModel.upsertDraftSegment(it.copy(latitude = null, longitude = null))
                                            Toast.makeText(context, "Location cleared", Toast.LENGTH_SHORT).show()
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
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (segment != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = segment.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        if (segment.latitude != null && segment.longitude != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View on map",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        val mapUri =
                                            Uri.parse("https://www.google.com/maps/search/?api=1&query=${segment.latitude},${segment.longitude}")
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
                        text = "Start: ${dateTimeFormatter.format(Date(segment.startTime))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "End: ${dateTimeFormatter.format(Date(segment.endTime))}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }

                HorizontalDivider()

                // The Boat Concept for Segment
                BoatSummary(
                    fishermanCount = fishermanCount,
                    onBoatClick = {
                        viewModel.updateDraftSegmentId(segment.id)

                        navigateToLoadBoatForSegment()
                    }
                )

                if (showEditSegmentDialog) {
                    var segmentName by remember { mutableStateOf(segment.name) }
                    var startDateMillis by remember { mutableLongStateOf(segment.startTime) }
                    var endDateMillis by remember { mutableLongStateOf(segment.endTime) }

                    var tripStartDateMillis by remember { mutableLongStateOf(draftTripStartDate) }
                    var tripEndDateMillis by remember { mutableLongStateOf(draftTripEndDate) }

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
                                viewModel.upsertDraftSegment(segment.copy(
                                    name = segmentName,
                                    startTime = startDateMillis,
                                    endTime = endDateMillis
                                ))
                                showEditSegmentDialog = false
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
            } else {
                Text("Segment not found", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
