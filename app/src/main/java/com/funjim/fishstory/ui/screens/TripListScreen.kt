package com.funjim.fishstory.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.utils.hasLocationPermission
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItem
import com.funjim.fishstory.ui.utils.TripMenu
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    viewModel: TripViewModel,
    navigateToTripDetails: (String) -> Unit,
    navigateToAddTrip: () -> Unit,
    navigateBack: () -> Unit
) {
    val tripSummaries by viewModel.tripSummaries.collectAsStateWithLifecycle()
    var tripToDelete by remember { mutableStateOf<TripSummary?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State to keep track of which trip we are currently modifying
    var activeTrip by remember { mutableStateOf<TripSummary?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            activeTrip?.let { summary ->
                scope.launch {
                    // We add the suppress warning here because we just verified 'granted'
                    @SuppressLint("MissingPermission")
                    val location = viewModel.getTripCurrentLocation(context)
                    location?.let {
                        viewModel.saveTrip(summary.trip.copy(latitude = it.latitude, longitude = it.longitude))
                    }
                }
            }
        }
    }
    // SINGLE Location Picker
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()
    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = activeTrip?.trip?.latitude,
        existingLng = activeTrip?.trip?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            activeTrip?.let { summary ->
                scope.launch { viewModel.saveTrip(summary.trip.copy(latitude = lat, longitude = lng)) }
            }
        }
    )

    val onAction: (TripAction) -> Unit = { action ->
        when (action) {
            is TripAction.View -> {}
            is TripAction.Menu -> {
                showMenu = true
                activeTrip = action.tripSummary
            }
            is TripAction.OpenMap -> {}
            is TripAction.UseCurrentLocation -> {
                showMenu = false
                if (hasLocationPermission(context)) {
                    scope.launch {
                        @SuppressLint("MissingPermission")
                        val location = viewModel.getTripCurrentLocation(context)
                        if (location != null) {
                            viewModel.saveTrip(
                                action.tripSummary.trip.copy(
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
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
            is TripAction.SelectLocation -> {
                showMenu = false
                locationPicker.openPicker()
            }
            is TripAction.ClearLocation -> {
                showMenu = false
                scope.launch {
                    viewModel.saveTrip(
                        action.tripSummary.trip.copy(latitude = null, longitude = null)
                    )
                    Toast.makeText(context, "Location cleared", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            is TripAction.Delete -> {
                showMenu = false
                tripToDelete = action.tripSummary
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trips") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToAddTrip,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Trip")
            }
        }
    ) {
        padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            LazyColumn {
                val totalItems = tripSummaries.size
                itemsIndexed(tripSummaries) { index, trip ->
                    TripItem(
                        trip = trip,
                        index = index,
                        totalItems = totalItems,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        onClick = { navigateToTripDetails(trip.trip.id) },
                        onAction = { action ->
                            when (action) {
                                is TripAction.OpenMap -> {
                                    val mapUri = Uri.parse("geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else -> {}
                            }
                        }
                    ) {
                        // Define the dropdown menu to be used with the TripItem card
                        TripMenu(
                            expanded = showMenu && activeTrip?.trip?.id == trip.trip.id,
                            onMenuClick = { onAction(TripAction.Menu(tripSummary = trip)) },
                            onDismiss = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Use Current Location") },
                                onClick = {
                                    onAction(TripAction.UseCurrentLocation(trip))
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = if (trip.trip.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Select Location") },
                                onClick = {
                                    onAction(TripAction.SelectLocation(trip))
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Map,
                                        contentDescription = null,
                                        tint = if (trip.trip.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
                                }
                            )
                            if (trip.trip.latitude != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location") },
                                    onClick = {
                                        onAction(TripAction.ClearLocation(trip))
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
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    onAction(TripAction.Delete(trip))
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    // DELETE CONFIRMATION
    tripToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            title = { Text("Delete Trip?") },
            text = { Text("""Are you sure you want to delete '${item.trip.name}'?

This cannot be undone.

All events (${item.eventCount}) and fish (${item.totalCaught}) associated with this trip will also be deleted.""") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTrip(item.trip)
                        tripToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
