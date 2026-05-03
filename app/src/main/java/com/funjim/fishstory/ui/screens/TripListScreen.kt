package com.funjim.fishstory.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.funjim.fishstory.ui.utils.VerticalScrollbar
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tripToDelete by remember { mutableStateOf<TripSummary?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State to keep track of which trip we are currently modifying
    var selectedTrip by remember { mutableStateOf<TripSummary?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            selectedTrip?.let { summary ->
                scope.launch {
                    // We add the Suppress warning here because we just verified 'granted'
                    @SuppressLint("MissingPermission")
                    val location = viewModel.getTripCurrentLocation(context)
                    location?.let {
                        viewModel.saveTrip(
                            summary.trip.copy(latitude = it.latitude, longitude = it.longitude)
                        )
                    }
                }
            }
        }
    }

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()
    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = selectedTrip?.trip?.latitude,
        existingLng = selectedTrip?.trip?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            selectedTrip?.let { summary ->
                viewModel.saveTrip(summary.trip.copy(latitude = lat, longitude = lng))
            }
        }
    )

    val onAction: (TripAction) -> Unit = { action ->
        when (action) {
            is TripAction.View -> {}
            is TripAction.Menu -> {
                showMenu = true
                selectedTrip = action.tripSummary
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Trips")
                        Spacer(Modifier.width(4.dp))
                        val totalTrips = state.liveTrips.size + state.upcomingTrips.size + state.recentTrips.size
                        Text(
                            text = "($totalTrips trip${if (totalTrips != 1) "s" else ""})",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = navigateToAddTrip,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
        padding ->

        val listState = rememberLazyListState()

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            val upcomingPagerState = rememberPagerState(pageCount = { state.upcomingTrips.size })
            val livePagerState = rememberPagerState(pageCount = { state.liveTrips.size })

            LazyColumn(state = listState) {
                if (state.upcomingTrips.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp, 4.dp)
                        ) {
                            Text(
                                "Upcoming Trips",
                                style = MaterialTheme.typography.titleMedium)
                            if (state.upcomingTrips.size > 1) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "(${upcomingPagerState.currentPage + 1} of ${state.upcomingTrips.size})",
                                    style = MaterialTheme.typography.titleSmall)
                            }
                        }

                        Column {
                            HorizontalPager(
                                state = upcomingPagerState,
                                contentPadding = PaddingValues(horizontal = 32.dp), // Shows a peek of next/prev cards
                                pageSpacing = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val tripSummary = state.upcomingTrips[page]

                                TripItemWithMenu(
                                    tripSummary = tripSummary,
                                    index = page,
                                    totalItems = state.upcomingTrips.size,
                                    modifier = Modifier.padding(8.dp, 4.dp),
                                    onNavigateToDetails = navigateToTripDetails,
                                    onAction = onAction,
                                    showMenu = showMenu && selectedTrip?.trip?.id == tripSummary.trip.id,
                                    onMenuDismiss = { showMenu = false }
                                )
                            }
                        }
                    }
                }

                if (state.liveTrips.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp, 4.dp)
                        ) {
                            Text(
                                "Live Trips",
                                style = MaterialTheme.typography.titleMedium)
                            if (state.liveTrips.size > 1) {
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "(${livePagerState.currentPage + 1} of ${state.liveTrips.size})",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }

                        Column {
                            HorizontalPager(
                                state = livePagerState,
                                contentPadding = PaddingValues(horizontal = 32.dp), // Shows a peek of next/prev cards
                                pageSpacing = 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val tripSummary = state.liveTrips[page]

                                TripItemWithMenu(
                                    tripSummary = tripSummary,
                                    index = page,
                                    totalItems = state.liveTrips.size,
                                    modifier = Modifier.padding(8.dp, 4.dp),
                                    onNavigateToDetails = navigateToTripDetails,
                                    onAction = onAction,
                                    showMenu = showMenu && selectedTrip?.trip?.id == tripSummary.trip.id,
                                    onMenuDismiss = { showMenu = false }
                                )
                            }
                        }
                    }
                }

                if (state.recentTrips.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp, 4.dp)
                        ) {
                            Text(
                                "Past Trips",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "(${state.recentTrips.size})",
                                style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    itemsIndexed(state.recentTrips) { index, trip ->
                        TripItemWithMenu(
                            tripSummary = trip,
                            index = index,
                            totalItems = state.recentTrips.size,
                            modifier = Modifier.padding(16.dp, 4.dp),
                            onNavigateToDetails = navigateToTripDetails,
                            onAction = onAction,
                            showMenu = showMenu && selectedTrip?.trip?.id == trip.trip.id,
                            onMenuDismiss = { showMenu = false }
                        )
                    }
                }
            }

            // TODO - don't show scroll bar if everything is visible
            VerticalScrollbar(
                state = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 0.dp)
            )
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

@Composable
fun TripItemWithMenu(
    tripSummary: TripSummary,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
    onNavigateToDetails: (String) -> Unit,
    onAction: (TripAction) -> Unit,
    showMenu: Boolean,
    onMenuDismiss: () -> Unit
) {
    TripItem(
        trip = tripSummary,
        index = index,
        totalItems = totalItems,
        modifier = modifier,
        onClick = { onNavigateToDetails(tripSummary.trip.id) },
        onAction = { action -> /* Your existing OpenMap logic here */ }
    ) {
        TripMenu(
            expanded = showMenu,
            onMenuClick = { onAction(TripAction.Menu(tripSummary)) },
            onDismiss = onMenuDismiss
        ) {
            // Centralized Menu Actions
            val lat = tripSummary.trip.latitude
            DropdownMenuItem(
                text = { Text("Use Current Location") },
                onClick = { onAction(TripAction.UseCurrentLocation(tripSummary)) },
                leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = if (lat != null) Color(0xFF4CAF50) else LocalContentColor.current) }
            )
            DropdownMenuItem(
                text = { Text("Select on Map") },
                onClick = { onAction(TripAction.SelectLocation(tripSummary)) },
                leadingIcon = { Icon(Icons.Default.Map, null, tint = if (lat != null) Color(0xFF4CAF50) else LocalContentColor.current) }
            )
            if (lat != null) {
                DropdownMenuItem(
                    text = { Text("Clear Location") },
                    onClick = { onAction(TripAction.ClearLocation(tripSummary)) },
                    leadingIcon = { Icon(Icons.Default.LocationOff, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { onAction(TripAction.Delete(tripSummary)) },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            )
        }
    }
}
