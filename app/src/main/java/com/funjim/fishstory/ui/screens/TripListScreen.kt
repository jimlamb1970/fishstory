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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.hasLocationPermission
import com.funjim.fishstory.ui.TripAction
import com.funjim.fishstory.ui.TripItem
import com.funjim.fishstory.ui.rememberLocationPickerState
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
                        viewModel.updateTrip(summary.trip.copy(latitude = it.latitude, longitude = it.longitude))
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
                scope.launch { viewModel.updateTrip(summary.trip.copy(latitude = lat, longitude = lng)) }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trips") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToAddTrip) {
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
                items(tripSummaries) { trip ->
                    TripItem(
                        trip = trip,
                        isMenuExpanded = showMenu && activeTrip?.trip?.id == trip.trip.id,
                        onAction = { action ->
                            when (action) {
                                is TripAction.View -> navigateToTripDetails(action.tripSummary.trip.id)
                                is TripAction.Menu -> {
                                    showMenu = true
                                    activeTrip = action.tripSummary
                                }
                                is TripAction.OpenMap -> {
                                    // Logic moved here, outside of the Item
                                    val mapUri = Uri.parse("geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                is TripAction.UseCurrentLocation -> {
                                    if (hasLocationPermission(context)) {
                                        scope.launch {
                                            @SuppressLint("MissingPermission")
                                            val location = viewModel.getTripCurrentLocation(context)
                                            if (location != null) {
                                                viewModel.updateTrip(
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
                                    locationPicker.openPicker()
                                }
                                is TripAction.ClearLocation -> {
                                    scope.launch {
                                        viewModel.updateTrip(
                                            action.tripSummary.trip.copy(latitude = null, longitude = null)
                                        )
                                        Toast.makeText(context, "Location cleared", Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                }
                                is TripAction.Delete -> {
                                    scope.launch {
                                        viewModel.deleteTrip(action.tripSummary.trip)
                                    }
                                }

                            }
                        },
                        onDismissMenu = { showMenu = false },
                    )
                }
            }
        }
    }
}
