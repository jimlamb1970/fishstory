package com.funjim.fishstory.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.cards.FishItem
import com.funjim.fishstory.ui.utils.FishFilter
import com.funjim.fishstory.ui.utils.LureCompositionWithColors
import com.funjim.fishstory.ui.utils.SortChip
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.getChipColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnChipColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.FishSortOrder
import com.funjim.fishstory.viewmodels.FishViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    viewModel: FishViewModel,
    filter: FishFilter,
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, eventId: String, fishId: String?) -> Unit,
    navigateToFishDetails: (
        fishId: String,
        sortOrder:FishSortOrder,
            sortReversed: Boolean
    ) -> Unit
) {
    LaunchedEffect(filter) {
        viewModel.selectBodyOfWater(filter.bodyOfWaterId)
        viewModel.selectFisherman(filter.fishermanId)
        viewModel.selectLure(filter.lureId)
        viewModel.selectSpecies(filter.speciesId)
        viewModel.selectTargetOnly(filter.targetOnly)
        viewModel.selectTrip(filter.tripId, filter.eventId)
        viewModel.selectWater(filter.waterId)
        viewModel.selectWeather(filter.weatherId)
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    val bodyOfWater by viewModel.selectedBodyOfWater.collectAsStateWithLifecycle()
    val event by viewModel.selectedEvent.collectAsStateWithLifecycle()
    val fisherman by viewModel.selectedFisherman.collectAsStateWithLifecycle()
    val lure by viewModel.selectedLure.collectAsStateWithLifecycle()
    val species by viewModel.selectedSpecies.collectAsStateWithLifecycle()
    val trip by viewModel.selectedTrip.collectAsStateWithLifecycle()

    val isLoading = (filter.bodyOfWaterId != null && bodyOfWater == null) ||
            (filter.eventId != null && event == null) ||
            (filter.fishermanId != null && fisherman == null) ||
            (filter.lureId != null && lure == null) ||
            (filter.speciesId != null && species == null) ||
            (filter.tripId != null && trip == null)

    val displayItems = if (isLoading) {
        emptyList() // Keep it clean while fetching
    } else {
        listOfNotNull(
            trip?.let { Triple(it.name, AppIcons.Default.Boat, false) },
            event?.let { Triple(it.name, AppIcons.Default.CanoeEmpty, false) },
            species?.let { Triple(it.name, AppIcons.Default.Species, false) },
            bodyOfWater?.let { Triple(it.name, AppIcons.Default.BodyOfWater, false) },
            fisherman?.let { Triple(it.fullName, AppIcons.Default.Fisherman, false) },
            lure?.let { Triple(it.lure.name, AppIcons.Default.Lure, true) }
        )
    }

    // Load fish for the appropriate scope
    val fishForScope by viewModel.fishForScope.collectAsStateWithLifecycle()
    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    var fishToDelete by remember { mutableStateOf<Fish?>(null) }

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
                        val total = fishForScope.size
                        BadgeCount(
                            count = total,
                            badgeColor = MaterialTheme.colorScheme.onPrimary,
                            textColor = MaterialTheme.colorScheme.primary
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
                    if (filter.tripId != null && filter.eventId != null) {
                        IconButton(
                            onClick = { onAddFish(filter.tripId, filter.eventId, null) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    AppIcons.Default.LeapingFishWithFins,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp))

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp) // Adjust offset to position on the edge
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
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
                    displayItems.forEachIndexed { index, (text, iconVector, isLure) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = getOnCardColor().copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            if (isLure) {
                                lure.let { lure ->
                                    LureCompositionWithColors(
                                        name = lure?.lure?.name ?: "",
                                        lure?.primaryColors ?: emptyList(),
                                        lure?.secondaryColors ?: emptyList(),
                                        lure?.lure?.glows ?: false,
                                        lure?.glowColors ?: emptyList(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        contentColor = getOnCardColor(),
                                        colorBadgeSize = 16.dp
                                    )
                                }
                            } else {
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = getOnCardColor()
                                )
                            }
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
                            currentOrder == FishSortOrder.TIMESTAMP_NEWEST_FIRST,
                            onClick = {
                                viewModel.updateSortOrder(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
                            }
                        )
                        SortChip(
                            "Length",
                            currentOrder == FishSortOrder.LENGTH_LONGEST_FIRST,
                            onClick = {
                                viewModel.updateSortOrder(FishSortOrder.LENGTH_LONGEST_FIRST)
                            }
                        )
                        if (filter.tripId.isNullOrEmpty()) {
                            SortChip(
                                "Trip",
                                currentOrder == FishSortOrder.TRIP_AZ,
                                onClick = {
                                    viewModel.updateSortOrder(FishSortOrder.TRIP_AZ)
                                }
                            )
                        }
                        if (filter.eventId.isNullOrEmpty()) {
                            SortChip(
                                "Event",
                                currentOrder == FishSortOrder.EVENT_AZ,
                                onClick = {
                                    viewModel.updateSortOrder(FishSortOrder.EVENT_AZ)
                                }
                            )
                        }
                        if (filter.speciesId.isNullOrEmpty()) {
                            SortChip(
                                "Species",
                                currentOrder == FishSortOrder.SPECIES_AZ,
                                onClick = {
                                viewModel.updateSortOrder(FishSortOrder.SPECIES_AZ)
                                }
                            )
                        }
                        if (filter.fishermanId.isNullOrEmpty()) {
                            SortChip(
                                "Fisherman",
                                currentOrder == FishSortOrder.FISHERMAN_AZ,
                                onClick = {
                                viewModel.updateSortOrder(FishSortOrder.FISHERMAN_AZ)
                                }
                            )
                        }
                        if (filter.lureId.isNullOrEmpty()) {
                            SortChip(
                                "Lure",
                                currentOrder == FishSortOrder.LURE,
                                onClick = {
                                viewModel.updateSortOrder(FishSortOrder.LURE)
                                }
                            )
                        }
                        SortChip(
                            "Kept",
                            currentOrder == FishSortOrder.KEPT,
                            onClick = {
                                viewModel.updateSortOrder(FishSortOrder.KEPT)
                            }
                        )
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
                                includeTrip = filter.tripId.isNullOrEmpty(),
                                includeEvent = filter.eventId.isNullOrEmpty(),
                                includeFisherman = filter.fishermanId.isNullOrEmpty(),
                                useImperial = appSettings.display.useImperialUnits,
                                thumbnailFlow =
                                    if (fishDetails.photoCount == 0) viewModel.speciesThumbnail(fishDetails.fish.speciesId)
                                    else viewModel.fishThumbnail(fishDetails.fish.id),
                                photosFlow = viewModel.fishPhotos(fishDetails.fish.id),
                                onClick = {
                                    navigateToFishDetails(
                                        fishDetails.fish.id,
                                        currentOrder,
                                        reversed
                                    )
                                },
                                onEdit = {
                                    onAddFish(
                                        fishDetails.trip.id,
                                        fishDetails.event.id,
                                        fishDetails.fish.id
                                    )
                                },
                                onPhotoAdded = { uri ->
                                    viewModel.addFishPhoto(
                                        fishId = fishDetails.fish.id,
                                        uri = uri,
                                        selected = true)
                                },
                                onPhotoTaken = { uri ->
                                    viewModel.addFishPhoto(
                                        fishId = fishDetails.fish.id,
                                        uri = uri,
                                        selected = false)
                                },
                                onSetThumbnail = { photo ->
                                    viewModel.setFishThumbnail(fishDetails.fish.id, photo.id)
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deleteFishPhoto(fishDetails.fish.id, photo.id)
                                },
                                onDelete = { fishToDelete = fishDetails.fish },
                                onSetLocation = if (hasLocationPermission) {
                                    {
                                        scope.launch {
                                            val location = viewModel.fetchLocation()
                                            if (location != null) {
                                                val fish =
                                                    viewModel.getFishById(fishDetails.fish.id)
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
                                    }
                                } else null,
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

                    VerticalScrollToItemBar(
                        state = listState,
                        onToggleAlignment = {
                            viewModel.updateScrollbarOnLeft()
                        },
                        modifier = Modifier
                            .align(if (appSettings.display.scrollbarOnLeft) Alignment.CenterStart else Alignment.CenterEnd)
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
