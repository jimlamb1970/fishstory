package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.SegmentSummary
import com.funjim.fishstory.ui.FishItem
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.FishSortOrder
import com.funjim.fishstory.viewmodels.FishViewModel
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    viewModel: FishViewModel,
    tripId: String?,
    segmentId: String?,   // empty string means trip-level (no segment selected)
    fishermanId: String?,
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, segmentId: String, fishId: String?) -> Unit,
    navigateToFishDetails: (fishId: String) -> Unit
) {
    LaunchedEffect(tripId) {
        viewModel.updateSelectedTripIdForFilter(tripId)
        viewModel.updateSelectedSegmentIdForFilter(segmentId)
        viewModel.updateFishermanIdForFilter(fishermanId)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    /*
     TODO - need to look into location behavior for when trip and/or segment
      is not specified for location selection.  If they are not specified, those
      options can not be picked.
     */
    val tripWithDetails by viewModel.currentTrip.collectAsStateWithLifecycle()
    val segmentWithDetails by viewModel.currentSegment.collectAsStateWithLifecycle()
    val fishermanDetails by viewModel.currentFisherman.collectAsStateWithLifecycle()

    val screenTitle = "Fish Log"

    // Load fish for the appropriate scope
    val fishForScope by viewModel.fishForScope.collectAsStateWithLifecycle()
    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            permissionGranted = true
            if (tripId != null && segmentId != null) {
                onAddFish(tripId, segmentId, null)
            }
        }
    }

    var fishToUpdateLocation by remember { mutableStateOf<FishWithDetails?>(null) }

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPickerFish = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = fishToUpdateLocation?.latitude,  // Passed from your DB object
        existingLng = fishToUpdateLocation?.longitude,
        onFetchLocation = { viewModel.fetchDeviceLocationOnce(context) },
        onLocationConfirmed = { lat, lng ->
            fishToUpdateLocation?.let { fishDetails ->
                scope.launch {
                    val fish = viewModel.getFishById(fishDetails.id)
                    if (fish != null) {
                        viewModel.upsertFish(fish.copy(latitude = lat, longitude = lng))
                    }
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (fishForScope.size > 1) {
                        Text(
                            text = "${fishForScope.size} Fish",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!segmentId.isNullOrEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (permissionGranted) {
                            if (tripId != null && segmentId != null) {
                                onAddFish(tripId, segmentId, null)
                            }
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
        }
    ) { padding ->
        if (fishForScope.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No fish logged yet. Tap 'Log Fish' to add one.")
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!fishermanId.isNullOrEmpty()) {
                        Text(
                            text = fishermanDetails?.fisherman?.fullName ?: "All Fishermen",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    } else {
                        Text(
                            text = tripWithDetails?.trip?.name ?: "All Trips",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                // TODO - make this a drop down menu for all the segments in the trip?
                if (!segmentId.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically)
                    {
                        Text(
                            text = segmentWithDetails?.segment?.name ?: "All Segments",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                // Sort Buttons
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(8.dp)) {
                    SortChip("Time", currentOrder == FishSortOrder.TIMESTAMP_NEWEST_FIRST) {
                        viewModel.updateSortOrder(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
                    }
                    if (tripId.isNullOrEmpty()) {
                        SortChip("Trip", currentOrder == FishSortOrder.TRIP_AZ) {
                            viewModel.updateSortOrder(FishSortOrder.TRIP_AZ)
                        }
                    }
                    if (segmentId.isNullOrEmpty()) {
                        SortChip("Segment", currentOrder == FishSortOrder.SEGMENT_AZ) {
                            viewModel.updateSortOrder(FishSortOrder.SEGMENT_AZ)
                        }
                    }
                    if (fishermanId.isNullOrEmpty()) {
                        SortChip("Fisherman", currentOrder == FishSortOrder.FISHERMAN_AZ) {
                            viewModel.updateSortOrder(FishSortOrder.FISHERMAN_AZ)
                        }
                    }
                    SortChip("Species", currentOrder == FishSortOrder.SPECIES_AZ) {
                        viewModel.updateSortOrder(FishSortOrder.SPECIES_AZ)
                    }
                    SortChip("Length", currentOrder == FishSortOrder.LENGTH_LONGEST_FIRST) {
                        viewModel.updateSortOrder(FishSortOrder.LENGTH_LONGEST_FIRST)
                    }
                    SortChip("Released", currentOrder == FishSortOrder.RELEASED) {
                        viewModel.updateSortOrder(FishSortOrder.RELEASED)
                    }
                    SortChip("Lure", currentOrder == FishSortOrder.LURE) {
                        viewModel.updateSortOrder(FishSortOrder.LURE)
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = { viewModel.toggleReverse() }) {
                        Icon(
                            imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Reverse Sort",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val allPhotos by viewModel.fishPhotos.collectAsStateWithLifecycle()

                LazyColumn {
                    items(fishForScope) { fishDetails ->
                        val photos = allPhotos[fishDetails.id] ?: emptyList()
                        FishItem(
                            fish = fishDetails,
                            includeTrip = tripId.isNullOrEmpty(),
                            includeSegment = segmentId.isNullOrEmpty(),
                            includeFisherman = fishermanId.isNullOrEmpty(),
                            photos = photos,
                            onAddPhoto = null,
                            onDeletePhoto = null,
                            /* TODO - enable photos for fish cards
                            onAddPhoto = { photo ->
                                viewModel.addPhoto(photo)
                            },
                            onDeletePhoto = { photo ->
                                viewModel.deletePhoto(photo)
                            },
                            */
                            onClick = {
                                navigateToFishDetails(fishDetails.id)
                            },
                            onEdit = {
                                scope.launch {
                                    onAddFish(
                                        fishDetails.tripId,
                                        fishDetails.segmentId,
                                        fishDetails.id
                                    )
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    val fishObj = viewModel.getFishById(fishDetails.id)
                                    if (fishObj != null) {
                                        viewModel.deleteFishObject(fishObj)
                                    }
                                }
                            },
                            onSetLocation = {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    scope.launch {
                                        val location = viewModel.getFishCurrentLocation(context)
                                        if (location != null) {
                                            val fish = viewModel.getFishById(fishDetails.id)
                                            if (fish != null) {
                                                viewModel.upsertFish(fish.copy(
                                                    latitude = location.latitude,
                                                    longitude = location.longitude))
                                            }
                                            Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            onUseTripLocation = if (tripWithDetails?.trip?.latitude != null) {
                                {
                                    scope.launch {
                                        val fish = viewModel.getFishById(fishDetails.id)
                                        if (fish != null) {
                                            viewModel.upsertFish(fish.copy(
                                                latitude = tripWithDetails?.trip?.latitude,
                                                longitude = tripWithDetails?.trip?.longitude))
                                        }
                                        Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null,
                            onUseSegmentLocation = if (segmentWithDetails?.segment?.latitude != null) {
                                {
                                    scope.launch {
                                        val fish = viewModel.getFishById(fishDetails.id)
                                        if (fish != null) {
                                            viewModel.upsertFish(fish.copy(
                                                latitude = segmentWithDetails?.segment?.latitude,
                                                longitude = segmentWithDetails?.segment?.longitude))
                                        }
                                        Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null,
                            onSelectLocation = {
                                fishToUpdateLocation = fishDetails
                                locationPickerFish.openPicker()
                            },
                            onClearLocation = {
                                scope.launch {
                                    val fish = viewModel.getFishById(fishDetails.id)
                                    if (fish != null) {
                                        viewModel.upsertFish(fish.copy(latitude = null, longitude = null))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
