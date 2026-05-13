package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.SortChip
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItemWithMenu
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.TripListFilter
import com.funjim.fishstory.viewmodels.TripListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    viewModel: TripListViewModel,
    navigateToTripDetails: (String) -> Unit,
    navigateToAddTrip: () -> Unit,
    navigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var tripToDelete by remember { mutableStateOf<TripSummary?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val filter by viewModel.tripFilter.collectAsStateWithLifecycle()

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
                    val location = viewModel.fetchLocation()
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
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
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
            is TripAction.OpenMap -> {
                val mapUri =
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${action.lat},${action.lng}")
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
            is TripAction.UseCurrentLocation -> {
                showMenu = false
                if (viewModel.hasLocationPermission()) {
                    scope.launch {
                        val location = viewModel.fetchLocation()
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
                        val total = state.liveTrips.size + state.upcomingTrips.size + state.completedTrips.size
                        Text(
                            text = "($total)",
                            style = MaterialTheme.typography.bodyMedium
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
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New")
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
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())) {
                    SortChip("Upcoming (${state.upcomingTrips.size})", filter == TripListFilter.UPCOMING) {
                        viewModel.updateTripFilter(TripListFilter.UPCOMING)
                    }
                    SortChip("Live (${state.liveTrips.size})", filter == TripListFilter.LIVE) {
                        viewModel.updateTripFilter(TripListFilter.LIVE)
                    }
                    SortChip("Completed (${state.completedTrips.size})", filter == TripListFilter.COMPLETED) {
                        viewModel.updateTripFilter(TripListFilter.COMPLETED)
                    }
                }

                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter Trips",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            val listState = rememberLazyListState()

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(state = listState) {
                    when (filter) {
                        TripListFilter.UPCOMING -> {
                            itemsIndexed(state.upcomingTrips) { index, trip ->
                                TripItemWithMenu(
                                    tripSummary = trip,
                                    index = index,
                                    totalItems = state.upcomingTrips.size,
                                    modifier = Modifier.padding(8.dp, 4.dp),
                                    onNavigateToDetails = navigateToTripDetails,
                                    onFetchThumbnail = { id -> viewModel.fetchThumbnail(id) },
                                    onAction = onAction,
                                    showMenu = showMenu && selectedTrip?.trip?.id == trip.trip.id,
                                    onMenuDismiss = { showMenu = false }
                                )
                            }
                        }

                        TripListFilter.LIVE -> {
                            itemsIndexed(state.liveTrips) { index, trip ->
                                TripItemWithMenu(
                                    tripSummary = trip,
                                    index = index,
                                    totalItems = state.liveTrips.size,
                                    modifier = Modifier.padding(8.dp, 4.dp),
                                    onNavigateToDetails = navigateToTripDetails,
                                    onFetchThumbnail = { id -> viewModel.fetchThumbnail(id) },
                                    onAction = onAction,
                                    showMenu = showMenu && selectedTrip?.trip?.id == trip.trip.id,
                                    onMenuDismiss = { showMenu = false }
                                )
                            }
                        }

                        TripListFilter.COMPLETED -> {
                            itemsIndexed(state.completedTrips) { index, trip ->
                                TripItemWithMenu(
                                    tripSummary = trip,
                                    index = index,
                                    totalItems = state.completedTrips.size,
                                    modifier = Modifier.padding(8.dp, 4.dp),
                                    onNavigateToDetails = navigateToTripDetails,
                                    onFetchThumbnail = { id -> viewModel.fetchThumbnail(id) },
                                    onAction = onAction,
                                    showMenu = showMenu && selectedTrip?.trip?.id == trip.trip.id,
                                    onMenuDismiss = { showMenu = false }
                                )
                            }
                        }
                    }
                }

                var isLeftAligned by remember { mutableStateOf(false) }

                VerticalScrollToItemBar(
                    state = listState,
                    imageVector = AppIcons.Default.Boat,
                    onToggleAlignment = { isLeftAligned = !isLeftAligned },
                    modifier = Modifier
                        .align(if (isLeftAligned) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp, horizontal = 0.dp)
                )
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

All events (${item.eventCount}) and fish (${item.fishCaught}) associated with this trip will also be deleted.""") },
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
