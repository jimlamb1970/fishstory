package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.ui.utils.FishItem
import com.funjim.fishstory.ui.utils.SortChip
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.getChipColor
import com.funjim.fishstory.ui.utils.getOnChipColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.FishSortOrder
import com.funjim.fishstory.viewmodels.FishViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    viewModel: FishViewModel,
    tripId: String?,
    eventId: String?,
    fishermanId: String?,
    lureId: String?,
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, eventId: String, fishId: String?) -> Unit,
    navigateToFishDetails: (fishId: String) -> Unit
) {
    LaunchedEffect(key1 = listOf(tripId, eventId, fishermanId, lureId)) {
        viewModel.selectTrip(tripId)
        viewModel.selectEvent(eventId)
        viewModel.selectFisherman(fishermanId)
        viewModel.selectLure(lureId)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val trip by viewModel.selectedTrip.collectAsStateWithLifecycle()
    val event by viewModel.selectedEvent.collectAsStateWithLifecycle()
    val fisherman by viewModel.selectedFisherman.collectAsStateWithLifecycle()
    val lure by viewModel.selectedLure.collectAsStateWithLifecycle()

    val isLoading = (tripId != null && trip == null) ||
            (eventId != null && event == null) ||
            (fishermanId != null && fisherman == null) ||
            (lureId != null && lure == null)

    val names = if (isLoading) {
        emptyList() // Keep it clean while fetching
    } else {
        listOfNotNull(trip?.name, event?.name, fisherman?.fullName, lure?.lure?.name)
    }

    // Load fish for the appropriate scope
    val fishForScope by viewModel.fishForScope.collectAsStateWithLifecycle()
    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    var fishToDelete by remember { mutableStateOf<Fish?>(null) }

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
        existingLat = fishToUpdateLocation?.fish?.latitude,  // Passed from your DB object
        existingLng = fishToUpdateLocation?.fish?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            fishToUpdateLocation?.let { fishDetails ->
                scope.launch {
                    val fish = viewModel.getFishById(fishDetails.fish.id)
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Fish Log")
                        Spacer(Modifier.width(4.dp))
                        val total = fishForScope.size
                        Text(
                            text = "($total)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
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
                    if (tripId != null && eventId != null) {
                        TextButton(
                            onClick = {
                                onAddFish(tripId, eventId, null)
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Fish")
                            }
                        }
                    }
                }
            )
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
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    names.forEachIndexed { index, string ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(0.dp)) {
                            Text(
                                text = string,
                                style =
                                    if (index == 0) MaterialTheme.typography.titleMedium
                                    else MaterialTheme.typography.titleSmall,
                                fontWeight =
                                    if (index == 0) FontWeight.Bold
                                    else FontWeight.Normal,
                                color = getOnMainColor()
                            )
                        }
                    }
                }

                // Sort Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())) {
                        SortChip(
                            "Time",
                            currentOrder == FishSortOrder.TIMESTAMP_NEWEST_FIRST) {
                            viewModel.updateSortOrder(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
                        }
                        if (tripId.isNullOrEmpty()) {
                            SortChip(
                                "Trip",
                                currentOrder == FishSortOrder.TRIP_AZ) {
                                viewModel.updateSortOrder(FishSortOrder.TRIP_AZ)
                            }
                        }
                        if (eventId.isNullOrEmpty()) {
                            SortChip(
                                "Event",
                                currentOrder == FishSortOrder.EVENT_AZ) {
                                viewModel.updateSortOrder(FishSortOrder.EVENT_AZ)
                            }
                        }
                        if (fishermanId.isNullOrEmpty()) {
                            SortChip(
                                "Fisherman",
                                currentOrder == FishSortOrder.FISHERMAN_AZ) {
                                viewModel.updateSortOrder(FishSortOrder.FISHERMAN_AZ)
                            }
                        }
                        SortChip(
                            "Species",
                            currentOrder == FishSortOrder.SPECIES_AZ) {
                            viewModel.updateSortOrder(FishSortOrder.SPECIES_AZ)
                        }
                        SortChip(
                            "Length",
                            currentOrder == FishSortOrder.LENGTH_LONGEST_FIRST) {
                            viewModel.updateSortOrder(FishSortOrder.LENGTH_LONGEST_FIRST)
                        }
                        SortChip(
                            "Kept",
                            currentOrder == FishSortOrder.KEPT) {
                            viewModel.updateSortOrder(FishSortOrder.KEPT)
                        }
                        SortChip(
                            "Lure",
                            currentOrder == FishSortOrder.LURE) {
                            viewModel.updateSortOrder(FishSortOrder.LURE)
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    IconButton(
                        onClick = { viewModel.toggleReverse() },
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = getChipColor(),
                                shape = RoundedCornerShape(8.dp)
                            ).size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Reverse Sort",
                            tint = getOnChipColor(),
                        )
                    }
                }

                val listState = rememberLazyListState()

                Box(modifier = Modifier
                    .fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                    ) {
                        val totalItems = fishForScope.size
                        itemsIndexed(
                            fishForScope,
                            key = { _, item -> item.fish.id }
                        ) { index, fishDetails ->
                            FishItem(
                                fish = fishDetails,
                                index = index,
                                totalItems = totalItems,
                                includeTrip = tripId.isNullOrEmpty(),
                                includeEvent = eventId.isNullOrEmpty(),
                                includeFisherman = fishermanId.isNullOrEmpty(),
                                thumbnailFlow =
                                    if (fishDetails.photoCount == 0) viewModel.speciesThumbnail(fishDetails.fish.speciesId)
                                    else viewModel.fishThumbnail(fishDetails.fish.id),
                                onClick = {
                                    navigateToFishDetails(fishDetails.fish.id)
                                },
                                onEdit = {
                                    scope.launch {
                                        onAddFish(
                                            fishDetails.trip.id,
                                            fishDetails.event.id,
                                            fishDetails.fish.id
                                        )
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        fishToDelete = viewModel.getFishById(fishDetails.fish.id)
                                    }
                                },
                                onSetLocation = {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        scope.launch {
                                            val location = viewModel.fetchLocation()
                                            if (location != null) {
                                                val fish = viewModel.getFishById(fishDetails.fish.id)
                                                if (fish != null) {
                                                    viewModel.upsertFish(
                                                        fish.copy(
                                                            latitude = location.latitude,
                                                            longitude = location.longitude
                                                        )
                                                    )
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "Location updated",
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
                                },
                                onSelectLocation = {
                                    fishToUpdateLocation = fishDetails
                                    locationPickerFish.openPicker()
                                },
                                onClearLocation = {
                                    scope.launch {
                                        val fish = viewModel.getFishById(fishDetails.fish.id)
                                        if (fish != null) {
                                            viewModel.upsertFish(
                                                fish.copy(
                                                    latitude = null,
                                                    longitude = null
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    var isLeftAligned by remember { mutableStateOf(false) }

                    VerticalScrollToItemBar(
                        state = listState,
                        onToggleAlignment = { isLeftAligned = !isLeftAligned },
                        modifier = Modifier
                            .align(if (isLeftAligned) Alignment.CenterStart else Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp, horizontal = 0.dp)
                    )
                }
            }
        }
    }

    // DELETE CONFIRMATION
    fishToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { fishToDelete = null },
            title = { Text("Delete Fish?") },
            text = { Text("""Are you sure you want to delete this fish?

This cannot be undone.""") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFish(item)
                        fishToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fishToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
