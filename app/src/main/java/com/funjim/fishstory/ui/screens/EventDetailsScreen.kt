package com.funjim.fishstory.ui.screens

import DeleteConfirmationDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.AddBodyOfWaterDialog
import com.funjim.fishstory.ui.utils.AddSkyConditionDialog
import com.funjim.fishstory.ui.utils.AddSpeciesDialog
import com.funjim.fishstory.ui.utils.AddWaterClarityDialog
import com.funjim.fishstory.ui.utils.BodyOfWaterSelection
import com.funjim.fishstory.ui.utils.BodyOfWaterSummaries
import com.funjim.fishstory.ui.utils.CategoryChipConfig
import com.funjim.fishstory.ui.utils.CategoryRow
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.FishermanSummary
import com.funjim.fishstory.ui.utils.EditEventDialog
import com.funjim.fishstory.ui.utils.EventHighlightCard
import com.funjim.fishstory.ui.utils.FishFilter
import com.funjim.fishstory.ui.utils.FishermanSelection
import com.funjim.fishstory.ui.utils.FishermanSummaries
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.TargetSpeciesColumn
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.UpdateAllCatchesDialog
import com.funjim.fishstory.ui.utils.WaterDialog
import com.funjim.fishstory.ui.utils.WaterSummaryRow
import com.funjim.fishstory.ui.utils.WeatherDialog
import com.funjim.fishstory.ui.utils.WeatherSummaryRow
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnSecondaryColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.EventDetailsUiState
import com.funjim.fishstory.viewmodels.EventViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    viewModel: EventViewModel,
    tripId: String,
    eventId: String,
    navigateToSelectEventCrew: () -> Unit,
    navigateToAddFish: () -> Unit,
    navigateToFishList: (FishFilter) -> Unit,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) {
        viewModel.selectTrip(tripId)
        viewModel.selectEvent(eventId)
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    var showAddSpeciesDialog by remember { mutableStateOf(false) }
    var showSpeciesSelection by remember { mutableStateOf(false) }

    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
    var showAddBodyOfWaterDialog by remember { mutableStateOf(false) }
    var showBodiesOfWaterSelection by remember { mutableStateOf(false) }

    var showFishermanSelection by remember { mutableStateOf(false) }
    var showAddFishermanDialog by remember { mutableStateOf(false) }

    // Water snapshot state
    var showAddWaterDialog by remember { mutableStateOf(false) }
    var waterToEdit by remember { mutableStateOf<Water?>(null) }
    var waterToDelete by remember { mutableStateOf<Water?>(null) }
    val allWaterClarity by viewModel.allWaterClarity.collectAsStateWithLifecycle()
    var addWaterClarityDialog by remember { mutableStateOf(false) }

    var showAddWeatherDialog by remember { mutableStateOf(false) }
    var weatherToEdit by remember { mutableStateOf<Weather?>(null) }
    var weatherToDelete by remember { mutableStateOf<Weather?>(null) }
    val allSkyConditions by viewModel.allSkyConditions.collectAsStateWithLifecycle()
    var addSkyConditionDialog by remember { mutableStateOf(false) }

    // Dialog state for updating all catches for this body of water
    var showUpdateAllCatchesDialog by remember { mutableStateOf(false) }
    var bodyOfWaterToUpdateAll by remember { mutableStateOf<BodyOfWater?>(null) }

    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }

    val now = System.currentTimeMillis()

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = selectedEvent?.latitude,
        existingLng = selectedEvent?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            selectedEvent?.let { event ->
                scope.launch {
                    viewModel.upsertEvent(
                        event.copy(
                            latitude = lat,
                            longitude = lng
                        )
                    )
                    Toast.makeText(context, "Event location updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val bodyOfWaterSummaries by viewModel.bodyOfWaterSummaries.collectAsStateWithLifecycle()
    val fishermanSummaries by viewModel.fishermanSummaries.collectAsStateWithLifecycle()
    val waterSummaries by viewModel.waterSummaries.collectAsStateWithLifecycle()
    val weatherSummaries by viewModel.weatherSummaries.collectAsStateWithLifecycle()

    val fishermen by viewModel.fishermen.collectAsStateWithLifecycle(emptyList())

    when (val state = uiState) {
        is EventDetailsUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EventDetailsUiState.Success -> {
            val eventDetails = state.details
            val eventSummary = state.summary

            val event = eventDetails.event
            selectedEvent = event

            val trip = eventDetails.trip

            val eventLat = event.latitude
            val tripLat = trip.latitude

            val activeLat = eventLat ?: tripLat

            // Sort water snapshots descending (most recent first)
            val sortedBodyOfWaterList = remember(bodyOfWaterSummaries) {
                bodyOfWaterSummaries.sortedBy { it.bodyOfWater.name }
            }
            val sortedFishermen = remember(fishermen) {
                fishermen.sortedBy { it.fullName }
            }
            val sortedFishermanList = remember(fishermanSummaries) {
                fishermanSummaries.sortedBy { it.fisherman.fullName }
            }
            val sortedWaterList = remember(waterSummaries) {
                waterSummaries.sortedByDescending { it.water.timestamp }
            }
            val sortedWeatherList = remember(weatherSummaries) {
                weatherSummaries.sortedByDescending { it.weather.timestamp }
            }

            val categoryConfigs = remember(
                sortedBodyOfWaterList,
                sortedFishermanList,
                sortedWaterList,
                sortedWeatherList,
                eventDetails.targetSpecies,
                eventDetails.bodiesOfWater,
                eventSummary.fishermanCount
            ) {
                listOf(
                    CategoryChipConfig(
                        category = CategoryType.BODIES_OF_WATER,
                        icon = { Icon(
                            AppIcons.Default.BodyOfWater,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = sortedBodyOfWaterList.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.FISHERMEN,
                        icon = { Icon(
                            AppIcons.Default.Fisherman,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = eventSummary.fishermanCount
                    ),
                    CategoryChipConfig(
                        category = CategoryType.TARGET_SPECIES,
                        icon = { Icon(
                            AppIcons.Default.TargetFish,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = eventDetails.targetSpecies.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.WATER,
                        icon = { Icon(
                            AppIcons.Default.Water,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = sortedWaterList.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.WEATHER,
                        icon = {
                            Icon(
                                AppIcons.Default.Weather,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        count = sortedWeatherList.size
                    )
                )
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Event Details") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navigateToAddFish() }) {
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
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            menuExpanded = false
                                            showEditEventDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Event"
                                            )
                                        }
                                    )

                                    if (hasLocationPermission) {
                                        DropdownMenuItem(
                                            text = { Text("Use Current Location") },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    val location = viewModel.fetchLocation()
                                                    if (location != null) {
                                                        viewModel.upsertEvent(
                                                            event.copy(
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
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.MyLocation,
                                                    contentDescription = null,
                                                    tint = if (activeLat != null)
                                                        Color(0xFF4CAF50)
                                                    else
                                                        LocalContentColor.current
                                                )
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text("Select on Map") },
                                        onClick = {
                                            menuExpanded = false
                                            locationPicker.openPicker()
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Map,
                                                contentDescription = null,
                                                tint = if (activeLat != null)
                                                    Color(0xFF4CAF50)
                                                else
                                                    LocalContentColor.current)
                                        }
                                    )

                                    if (activeLat != null && eventLat != null) {
                                        DropdownMenuItem(
                                            text = {
                                                if (tripLat == null) Text("Clear Location")
                                                else Text("Reset Location")
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    viewModel.upsertEvent(
                                                        event.copy(latitude = null, longitude = null)
                                                    )
                                                    if (tripLat == null)
                                                        Toast.makeText(
                                                            context,
                                                            "Location cleared",
                                                            Toast.LENGTH_SHORT).show()
                                                    else
                                                        Toast.makeText(
                                                            context,
                                                            "Location reset",
                                                            Toast.LENGTH_SHORT).show()
                                                }
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
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

                    LazyColumn(horizontalAlignment = Alignment.Start) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getOnMainColor()
                                )
                                val displayLat = event.latitude ?: trip.latitude
                                val displayLng = event.longitude ?: event.longitude
                                val hasAnyLocation = displayLat != null && displayLng != null

                                if (hasAnyLocation) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "View on map",
                                        tint = getOnMainColor(),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                val mapUri =
                                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${displayLat},${displayLng}")
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
                                    if (event.latitude == null) {
                                        Text(
                                            text = "(Trip)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = getOnMainColor()
                                        )
                                    }
                                }
                            }
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "Start: ${dateTimeFormatter.format(Date(event.startTime))}",
                                style = MaterialTheme.typography.titleMedium,
                                color = getOnSecondaryColor()
                            )
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "End: ${dateTimeFormatter.format(Date(event.endTime))}",
                                style = MaterialTheme.typography.titleMedium,
                                color = getOnSecondaryColor()
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = getOnMainColor()
                            )

                            PhotoPickerRow(
                                photos = eventDetails.photos,
                                onPhotoSelected = { uri ->
                                    viewModel.addEventPhoto(eventId = eventId, uri = uri, true)
                                },
                                onPhotoTaken = { uri ->
                                    viewModel.addEventPhoto(eventId = eventId, uri = uri, false)
                                },
                                onSetThumbnail = { photo ->
                                    viewModel.setEventThumbnail(
                                        eventId = eventId,
                                        photoId = photo.id
                                    )
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deleteEventPhoto(eventId, photo.id)
                                }
                            )

                            if (eventSummary.fishCaught != 0 || now >= event.startTime) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                    thickness = 1.dp,
                                    color = getOnMainColor()
                                )

                                EventHighlightCard(
                                    summary = eventSummary,
                                    onClick = {
                                        navigateToFishList(FishFilter(
                                            tripId = trip.id,
                                            eventId = event.id)
                                        )
                                    },
                                    onFishClick = {
                                        navigateToFishList(FishFilter(
                                            tripId = trip.id,
                                            eventId = event.id)
                                        )
                                    },
                                    onTargetFishClick = {
                                        navigateToFishList(FishFilter(
                                            tripId = trip.id,
                                            eventId = event.id,
                                            targetOnly = true)
                                        )
                                    }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = getOnMainColor()
                            )

                            CategoryRow(
                                categories = categoryConfigs,
                                selectedCategory = selectedCategory,
                                onCategorySelected = viewModel::onCategorySelected
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = getOnMainColor()
                            )

                            Crossfade(
                                targetState = selectedCategory,
                                label = "CategoryTransition",
                                modifier = Modifier.fillMaxWidth()
                            ) { category ->
                                when (category) {
                                    CategoryType.BODIES_OF_WATER -> {
                                        BodyOfWaterSummaries(
                                            list = sortedBodyOfWaterList,
                                            thumbnailFlow = { bodyOfWater ->
                                                viewModel.bodyOfWaterThumbnail(bodyOfWater.id)
                                            },
                                            onAdd = { showBodiesOfWaterSelection = true },
                                            onClick = { bodyOfWater ->
                                                bodyOfWaterToUpdateAll = bodyOfWater
                                                showUpdateAllCatchesDialog = true
                                            },
                                            onFishClick = { bodyOfWater, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    eventId = event.id,
                                                    bodyOfWaterId = bodyOfWater.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onDelete = { bodyOfWater ->
                                                viewModel.removeEventBodyOfWater(
                                                    eventId,
                                                    bodyOfWater.id
                                                )
                                            }
                                        )
                                    }

                                    CategoryType.FISHERMEN -> {
                                        Column() {
                                            FishermanSummary(
                                                fishermanCount = eventSummary.fishermanCount,
                                                tackleBoxCount = eventSummary.tackleBoxCount,
                                                allowOverride = true,
                                                onClick = { navigateToSelectEventCrew() }
                                            )
                                            FishermanSummaries(
                                                list = sortedFishermanList,
                                                thumbnailFlow = { fisherman ->
                                                    viewModel.fishermanThumbnail(fisherman.id)
                                                },
                                                onAdd = {
                                                    showFishermanSelection = true
                                                },
                                                onClick = { fisherman ->
                                                    Toast.makeText(
                                                        context,
                                                        "Not yet implemented",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                onFishClick = { fisherman, target ->
                                                    navigateToFishList(FishFilter(
                                                        tripId = trip.id,
                                                        eventId = event.id,
                                                        fishermanId = fisherman.id,
                                                        targetOnly = target)
                                                    )
                                                },
                                                onDelete = { fisherman ->
                                                    viewModel.deleteFishermanFromEvent(fisherman.id)
                                                }
                                            )
                                        }
                                    }

                                    CategoryType.TARGET_SPECIES -> {
                                        TargetSpeciesColumn(
                                            items = eventDetails.targetSpecies,
                                            onAdd = { showSpeciesSelection = true },
                                            onDelete = { species ->
                                                viewModel.removeEventTargetSpecies(
                                                    eventId,
                                                    species.id
                                                )
                                            },
                                            onClick = { species ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    eventId = event.id,
                                                    speciesId = species.id,
                                                    targetOnly = true)
                                                )
                                            },
                                            summaryProvider = { species ->
                                                viewModel.speciesSummary(
                                                    eventId = eventId,
                                                    speciesId = species.id)
                                            },
                                            thumbnailFlow = { species ->
                                                viewModel.speciesThumbnail(species.id)
                                            },
                                            modifier = Modifier.padding(
                                                vertical = 8.dp,
                                                horizontal = 16.dp
                                            )
                                        )
                                    }

                                    CategoryType.WATER -> {
                                        WaterSummaryRow(
                                            waterList = sortedWaterList,
                                            onAddWater = { showAddWaterDialog = true },
                                            onEdit = { waterToEdit = it },
                                            onFishClick = { water, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    eventId = event.id,
                                                    waterId = water.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onDelete = { waterToDelete = it }
                                        )
                                    }

                                    CategoryType.WEATHER -> {
                                        WeatherSummaryRow(
                                            list = sortedWeatherList,
                                            onAdd = { showAddWeatherDialog = true },
                                            onEdit = { weatherToEdit = it },
                                            onFishClick = { weather, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    eventId = event.id,
                                                    weatherId = weather.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onDelete = { weatherToDelete = it }
                                        )
                                    }

                                    else -> {
                                        // DO NOTHING FOR NOW
                                    }
                                }
                            }
                        }
                    }

                    if (showEditEventDialog) {
                        EditEventDialog(
                            event = event,
                            trip = trip,
                            onDismiss = { showEditEventDialog = false },
                            onConfirm = { confirmedItem ->
                                viewModel.upsertEvent(confirmedItem)
                                showEditEventDialog = false
                            }
                        )
                    }

                    waterToEdit?.let { water ->
                        WaterDialog(
                            initialTemp = water.temperature,
                            initialDepth = water.depth,
                            initialClarity = water.clarityId,
                            allClarity = allWaterClarity,
                            title = "Edit Water Conditions",
                            thumbnailProvider = { clarity ->
                                val thumbnailFlow = remember(clarity.id) {
                                    viewModel.waterClarityThumbnail(clarity.id)
                                }
                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.Water,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            onDismiss = { waterToEdit = null },
                            onConfirm = { temp, depth, clarity ->
                                viewModel.updateWater(
                                    water.copy(
                                        temperature = temp,
                                        depth = depth,
                                        clarityId = clarity?.id
                                    )
                                )
                                waterToEdit = null
                            },
                            onAddWaterClarity = { addWaterClarityDialog = true }
                        )
                    }

                    // Delete Water Confirmation Dialog
                    waterToDelete?.let { water ->
                        DeleteConfirmationDialog(
                            title = "Delete Water Conditions",
                            message = "Are you sure you want to delete these water conditions?",
                            onConfirm = {
                                viewModel.deleteWater(water.id)
                                waterToDelete = null
                            },
                            onDismiss = { waterToDelete = null }
                        )
                    }

                    weatherToEdit?.let { item ->
                        WeatherDialog(
                            initialTemp = item.temperature,
                            initialSkyCondition = item.skyConditionId,
                            initialWindDirection = item.windDirection,
                            initialWindSpeed = item.windSpeed,
                            initialAtmosphericPressure = item.atmosphericPressure,
                            initialAirVisibility = item.airVisibility,
                            initialAirHumidity = item.airHumidity,
                            allSkyConditions = allSkyConditions,
                            title = "Edit Weather Conditions",
                            thumbnailProvider = { sky ->
                                val thumbnailFlow = remember(sky.id) {
                                    viewModel.skyConditionThumbnail(sky.id)
                                }
                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.Water,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            onDismiss = { weatherToEdit = null },
                            onConfirm = { temp, skyCondition, windDirection, windSpeed, atmosphericPressure, airVisibility, airHumidity ->
                                viewModel.updateWeather(
                                    item.copy(
                                        temperature = temp,
                                        skyConditionId = skyCondition,
                                        windDirection = windDirection,
                                        windSpeed = windSpeed,
                                        atmosphericPressure = atmosphericPressure,
                                        airVisibility = airVisibility,
                                        airHumidity = airHumidity
                                    )
                                )
                                weatherToEdit = null
                            },
                            onAddSkyCondition = { addSkyConditionDialog = true }
                        )
                    }

                    weatherToDelete?.let { item ->
                        DeleteConfirmationDialog(
                            title = "Delete Weather Conditions",
                            message = "Are you sure you want to delete these Weather conditions?",
                            onConfirm = {
                                viewModel.deleteWeather(item.id)
                                waterToDelete = null
                            },
                            onDismiss = { waterToDelete = null }
                        )
                    }
                }
            }

            if (showAddWaterDialog) {
                WaterDialog(
                    initialTemp = null,
                    initialDepth = null,
                    initialClarity = null,
                    allClarity = allWaterClarity,
                    title = "New Water Conditions",
                    thumbnailProvider = { clarity ->
                        val thumbnailFlow = remember(clarity.id) {
                            viewModel.waterClarityThumbnail(clarity.id)
                        }
                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.Water,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onDismiss = { showAddWaterDialog = false },
                    onConfirm = { temp, depth, clarity ->
                        val newWater = Water(
                            tripId = tripId,
                            eventId = eventId,
                            temperature = temp,
                            depth = depth,
                            clarityId = clarity?.id
                        )

                        viewModel.addWater(newWater)
                        showAddWaterDialog = false

                    },
                    onAddWaterClarity = { addWaterClarityDialog = true}
                )
            }

            if (showAddWeatherDialog) {
                WeatherDialog(
                    initialTemp = null,
                    initialSkyCondition = null,
                    initialWindDirection = null,
                    initialWindSpeed = null,
                    initialAtmosphericPressure = null,
                    initialAirVisibility = null,
                    initialAirHumidity = null,
                    allSkyConditions = allSkyConditions,
                    title = "New Weather Conditions",
                    thumbnailProvider = { clarity ->
                        val thumbnailFlow = remember(clarity.id) {
                            viewModel.waterClarityThumbnail(clarity.id)
                        }
                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.BodyOfWater,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onDismiss = { showAddWeatherDialog = false },
                    onConfirm = { temp, skyCondition, windDirection, windSpeed, atmosphericPressure, airVisibility, airHumidity ->
                        viewModel.addWeather(
                            Weather(
                                tripId = tripId,
                                eventId = eventId,
                                temperature = temp,
                                skyConditionId = skyCondition,
                                windDirection = windDirection,
                                windSpeed = windSpeed,
                                atmosphericPressure = atmosphericPressure,
                                airVisibility = airVisibility,
                                airHumidity = airHumidity)
                        )
                        showAddWeatherDialog = false

                    },
                    onAddSkyCondition = { addSkyConditionDialog = true }
                )
            }

            if (showSpeciesSelection) {
                SpeciesSelection(
                    items = allSpecies,
                    selectedItems = eventDetails.targetSpecies,
                    onSelected = { selectedSpecies ->
                        viewModel.addEventTargetSpecies(eventId, selectedSpecies.id)
                    },
                    onUnselected = { selectedSpecies ->
                        viewModel.removeEventTargetSpecies(eventId, selectedSpecies.id)
                    },
                    onAdd = {
                        showAddSpeciesDialog = true
                    },
                    onDone = { showSpeciesSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    thumbnailProvider = { species ->
                        val thumbnailFlow = remember(species.id) {
                            viewModel.speciesThumbnail(species.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.TargetFish,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
            }

            if (showBodiesOfWaterSelection) {
                BodyOfWaterSelection(
                    items = allBodiesOfWater,
                    selectedItems = eventDetails.bodiesOfWater,
                    onSelected = { selectedBodyOfWater ->
                        viewModel.addEventBodyOfWater(eventId, selectedBodyOfWater.id)
                    },
                    onUnselected = { selectedBodyOfWater ->
                        viewModel.removeEventBodyOfWater(eventId, selectedBodyOfWater.id)
                    },
                    onAdd = {
                        showAddBodyOfWaterDialog = true
                    },
                    onDone = { showBodiesOfWaterSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    thumbnailProvider = { bodyOfWater ->
                        val thumbnailFlow = remember(bodyOfWater.id) {
                            viewModel.bodyOfWaterThumbnail(bodyOfWater.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.BodyOfWater,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
            }

            if (showFishermanSelection) {
                FishermanSelection(
                    items = sortedFishermen,
                    selectedItems = eventDetails.fishermen,
                    onSelected = { fisherman ->
                        viewModel.addFishermanToEvent(fishermanId = fisherman.id)
                    },
                    onUnselected = { fisherman ->
                        viewModel.deleteFishermanFromEvent(fishermanId = fisherman.id)
                    },
                    onAdd = {
                        showAddFishermanDialog = true
                    },
                    onDone = { showFishermanSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    thumbnailProvider = { fisherman ->
                        val thumbnailFlow = remember(fisherman.id) {
                            viewModel.fishermanThumbnail(fisherman.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.Fisherman,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                )
            }
        }
    }

    if (showAddSpeciesDialog) {
        AddSpeciesDialog(
            onDismiss = { showAddSpeciesDialog = false },
            onConfirm = { name ->
                viewModel.addEventTargetSpecies(eventId, Species(name = name))
                showAddSpeciesDialog = false
            }
        )
    }

    if (showAddBodyOfWaterDialog) {
        AddBodyOfWaterDialog(
            onDismiss = { showAddBodyOfWaterDialog = false },
            onConfirm = { name ->
                viewModel.addEventBodyOfWater(eventId, BodyOfWater(name = name))
                showAddBodyOfWaterDialog = false
            }
        )
    }

    if (addSkyConditionDialog) {
        AddSkyConditionDialog(
            onDismiss = { addSkyConditionDialog = false },
            onConfirm = { name ->
                viewModel.addSkyCondition(SkyCondition(name = name)) {
                    // Do nothing on success
                }
                addSkyConditionDialog = false
            }
        )
    }

    if (addWaterClarityDialog) {
        AddWaterClarityDialog(
            onDismiss = { addWaterClarityDialog = false },
            onConfirm = { name ->
                viewModel.addWaterClarity(WaterClarity(name = name)) {
                    // Do nothing on success
                }
                addWaterClarityDialog = false
            }
        )
    }

    // Confirmation Dialog for long pressing a Body of Water
    if (showUpdateAllCatchesDialog && bodyOfWaterToUpdateAll != null) {
        UpdateAllCatchesDialog(
            bodyOfWater = bodyOfWaterToUpdateAll,
            content = "Do you want to update all the fish logged for this event to the body of water: ${bodyOfWaterToUpdateAll?.name}?",
            onDismiss = {
                bodyOfWaterToUpdateAll = null
            },
            onConfirm = { targetBody ->
                scope.launch {
                    viewModel.updateBodyOfWaterForEvent(
                        newBodyOfWaterId = targetBody.id,
                        eventId = eventId
                    )
                    Toast.makeText(context, "Catches updated to ${targetBody.name}", Toast.LENGTH_SHORT).show()
                    bodyOfWaterToUpdateAll = null
                }
            }
        )
    }
}