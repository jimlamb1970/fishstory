package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.ui.utils.FishItem
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.FishSortOrder
import com.funjim.fishstory.viewmodels.FishViewModel
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
    LaunchedEffect(key1 = tripId, key2 = segmentId, key3 = fishermanId) {
        viewModel.updateSelectedTrip(tripId)
        viewModel.updateSelectedSegment(segmentId)
        viewModel.updateSelectedFisherman(fishermanId)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    /*
     TODO - need to look into location behavior for when trip and/or segment
      is not specified for location selection.  If they are not specified, those
      options can not be picked.
     */
    val trip by viewModel.selectedTrip.collectAsStateWithLifecycle()
    val segment by viewModel.selectedSegment.collectAsStateWithLifecycle()
    val fisherman by viewModel.selectedFisherman.collectAsStateWithLifecycle()
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
            // TODO -- add actions
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
                    val fish = viewModel.getFish(fishDetails.id)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
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
                        if (tripId != null) {
                            onAddFish(tripId, segmentId, null)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
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
            Column(modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!fishermanId.isNullOrEmpty()) {
                        Text(
                            text = fisherman?.fullName ?: "All Fishermen",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = trip?.name ?: "All Trips",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
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
                            text = segment?.name ?: "All Segments",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                // Sort Buttons
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
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
                    val totalItems = fishForScope.size
                    itemsIndexed(fishForScope) { index, fishDetails ->
                        val photos = allPhotos[fishDetails.id] ?: emptyList()
                        FishItem(
                            fish = fishDetails,
                            index = index,
                            totalItems = totalItems,
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
                                        fishDetails.eventId,
                                        fishDetails.id
                                    )
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    val fishObj = viewModel.getFish(fishDetails.id)
                                    if (fishObj != null) {
                                        viewModel.deleteFish(fishObj)
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
                                            val fish = viewModel.getFish(fishDetails.id)
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
                            onUseTripLocation = if (trip?.latitude != null) {
                                {
                                    scope.launch {
                                        val fish = viewModel.getFish(fishDetails.id)
                                        if (fish != null) {
                                            viewModel.upsertFish(fish.copy(
                                                latitude = trip?.latitude,
                                                longitude = trip?.longitude))
                                        }
                                        Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null,
                            onUseSegmentLocation = if (segment?.latitude != null) {
                                {
                                    scope.launch {
                                        val fish = viewModel.getFish(fishDetails.id)
                                        if (fish != null) {
                                            viewModel.upsertFish(fish.copy(
                                                latitude = segment?.latitude,
                                                longitude = segment?.longitude))
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
                                    val fish = viewModel.getFish(fishDetails.id)
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
