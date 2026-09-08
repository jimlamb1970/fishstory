package com.funjim.fishstory.ui.screens

import DeleteConfirmationDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.EventWithInfo
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.AddBodyOfWaterDialog
import com.funjim.fishstory.ui.utils.AddFishermanDialog
import com.funjim.fishstory.ui.utils.AddNoteDialog
import com.funjim.fishstory.ui.utils.AddSkyConditionDialog
import com.funjim.fishstory.ui.utils.AddSpeciesDialog
import com.funjim.fishstory.ui.utils.AddWaterClarityDialog
import com.funjim.fishstory.ui.utils.BodyOfWaterSelection
import com.funjim.fishstory.ui.utils.BodyOfWaterSummaries
import com.funjim.fishstory.ui.utils.CategoryChipConfig
import com.funjim.fishstory.ui.utils.CategoryRow
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.EditNoteDialog
import com.funjim.fishstory.ui.utils.FishermanSummary
import com.funjim.fishstory.ui.utils.EditTripDialog
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.EventItem
import com.funjim.fishstory.ui.utils.FishFilter
import com.funjim.fishstory.ui.utils.FishermanSelection
import com.funjim.fishstory.ui.utils.FishermanSummaries
import com.funjim.fishstory.ui.utils.NoteRow
import com.funjim.fishstory.ui.utils.NotesDialog
import com.funjim.fishstory.ui.utils.NotesIconButton
import com.funjim.fishstory.ui.utils.SpeciesSelection
import com.funjim.fishstory.ui.utils.SpeciesSummaries
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.TripHighlightCard
import com.funjim.fishstory.ui.utils.UpdateAllCatchesDialog
import com.funjim.fishstory.ui.utils.WaterDialog
import com.funjim.fishstory.ui.utils.WaterSummaryRow
import com.funjim.fishstory.ui.utils.WeatherDialog
import com.funjim.fishstory.ui.utils.WeatherSummaryRow
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnSecondaryColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.TripDetailsUiState
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    viewModel: TripViewModel,
    tripId: String,
    navigateToSelectTripCrew: (String) -> Unit,
    navigateToFishList: (FishFilter) -> Unit,
    navigateToAddEvent: (String) -> Unit,
    navigateToEventDetails: (String, CategoryType) -> Unit,
    navigateToAddLures: (TackleBox) -> Unit,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val now = System.currentTimeMillis()

    var showEditTripDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    var showAddSpeciesDialog by remember { mutableStateOf(false) }

    var showBodiesOfWaterSelection by remember { mutableStateOf(false) }
    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
    var showAddBodyOfWaterDialog by remember { mutableStateOf(false) }
    var showUpdateAllCatchesDialog by remember { mutableStateOf(false) }
    var bodyOfWaterToUpdateAll by remember { mutableStateOf<BodyOfWater?>(null) }

    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    var eventToDelete by remember { mutableStateOf<EventSummary?>(null) }
    var eventToUpdateLocation by remember { mutableStateOf<EventSummary?>(null) }

    var showFishermanSelection by remember { mutableStateOf(false) }
    var showAddFishermanDialog by remember { mutableStateOf(false) }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    var waterToEdit by remember { mutableStateOf<Water?>(null) }
    var waterToDelete by remember { mutableStateOf<Water?>(null) }
    val allWaterClarity by viewModel.allWaterClarity.collectAsStateWithLifecycle()
    var showAddWaterClarityDialog by remember { mutableStateOf(false) }

    var weatherToEdit by remember { mutableStateOf<Weather?>(null) }
    var weatherToDelete by remember { mutableStateOf<Weather?>(null) }
    val allSkyConditions by viewModel.allSkyConditions.collectAsStateWithLifecycle()
    var showAddSkyConditionDialog by remember { mutableStateOf(false) }

    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = selectedTrip?.latitude,  // Passed from your DB object
        existingLng = selectedTrip?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            selectedTrip?.let { trip ->
                scope.launch {
                    viewModel.saveTrip(trip.copy(latitude = lat, longitude = lng))
                }
            }
        }
    )

    val locationPickerEvent = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = eventToUpdateLocation?.event?.latitude,  // Passed from your DB object
        existingLng = eventToUpdateLocation?.event?.longitude,
        onFetchLocation = { scope.launch { viewModel.fetchDeviceLocationOnce() } },
        onLocationConfirmed = { lat, lng ->
            eventToUpdateLocation?.event?.let { event ->
                scope.launch {
                    viewModel.upsertEvent(event.copy(latitude = lat, longitude = lng))
                }
            }
        }
    )

    val uiState by viewModel.uiDetailState.collectAsStateWithLifecycle()

    val bodyOfWaterSummaries by viewModel.bodyOfWaterSummaries.collectAsStateWithLifecycle()
    val fishermanSummaries by viewModel.fishermanSummaries.collectAsStateWithLifecycle()
    val targetSpeciesSummaries by viewModel.targetSpeciesSummaries.collectAsStateWithLifecycle()
    val waterSummaries by viewModel.waterSummaries.collectAsStateWithLifecycle()
    val weatherSummaries by viewModel.weatherSummaries.collectAsStateWithLifecycle()

    val fishermen by viewModel.fishermen.collectAsStateWithLifecycle(emptyList())

    val tackleBoxMap by viewModel.tripTackleBoxMap.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showPhotos by viewModel.showPhotos.collectAsStateWithLifecycle()

    var bodyOfWaterForPhoto by remember { mutableStateOf<BodyOfWater?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val item = bodyOfWaterForPhoto
        if (uri != null && item != null) {
            viewModel.updateBodyOfWaterThumbnail(item.id, uri)
        }
        bodyOfWaterForPhoto = null
    }

    var speciesForPhoto by remember { mutableStateOf<Species?>(null) }
    val speciesPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val item = speciesForPhoto
        if (uri != null && item != null) {
            viewModel.updateSpeciesThumbnail(item.id, uri)
        }
        speciesForPhoto = null
    }

    when (val state = uiState) {
        is TripDetailsUiState.Loading -> {
            // Keeps the screen entirely blank or showing a spinner
            // until all 3 database points arrive
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is TripDetailsUiState.Success -> {
            val trip = state.details.trip
            selectedTrip = trip
            val details = state.details
            val summary = state.summary
            val eventSummaries = state.eventSummaries

            val events: List<EventWithInfo> = details.events

            val bodyOfWaterUsageMap: Map<String, Int> = events
                .flatMap { it.bodiesOfWater }
                .groupingBy { it.id }
                .eachCount() // Returns a Map<String, Int> where Key = bodyOfWaterId, Value = count

            val fishermanUsageMap: Map<String, Int> = events
                .flatMap { it.fishermen }
                .groupingBy { it.id }
                .eachCount() // Returns a Map<String, Int> where Key = fishermanId, Value = count

            val speciesUsageMap: Map<String, Int> = events
                .flatMap { it.targetSpecies }
                .groupingBy { it.id }
                .eachCount() // Returns a Map<String, Int> where Key = speciesId, Value = count


            val sortedBodyOfWaterList = remember(bodyOfWaterSummaries) {
                bodyOfWaterSummaries.sortedBy { it.bodyOfWater.name }
            }
            val sortedFishermen = remember(fishermen) {
                fishermen.sortedBy { it.fullName }
            }
            val sortedNotes = remember(details.notes) {
                details.notes.sortedByDescending { it.timestamp }
            }
            val sortedFishermanList = remember(fishermanSummaries) {
                fishermanSummaries.sortedBy { it.fisherman.fullName }
            }
            val sortedTargetSpeciesList = remember(targetSpeciesSummaries) {
                targetSpeciesSummaries.sortedBy { it.species.name }
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
                sortedNotes,
                sortedTargetSpeciesList,
                sortedWaterList,
                sortedWeatherList,
                details.targetSpecies,
                details.bodiesOfWater,
                summary.fishermanCount,
                eventSummaries.size
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
                        category = CategoryType.EVENTS,
                        icon = { Icon(
                            AppIcons.Default.CanoeEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = eventSummaries.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.FISHERMEN,
                        icon = { Icon(
                            AppIcons.Default.Fisherman,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = sortedFishermanList.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.NOTES,
                        icon = { Icon(
                            Icons.AutoMirrored.Filled.StickyNote2,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = sortedNotes.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.TARGET_SPECIES,
                        icon = { Icon(
                            AppIcons.Default.TargetFish,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = sortedTargetSpeciesList.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.WATER,
                        icon = { Icon(
                            AppIcons.Default.WaterSet,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = sortedWaterList.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.WEATHER,
                        icon = {
                            Icon(
                                AppIcons.Default.WeatherSet,
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
                        title = { Text("Trip Details") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = navigateBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            when (selectedCategory) {
                                CategoryType.BODIES_OF_WATER -> {
                                    IconButton(onClick = { showBodiesOfWaterSelection = true }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                AppIcons.Default.BodyOfWater,
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
                                CategoryType.EVENTS -> {
                                    IconButton(onClick = { navigateToAddEvent(tripId) }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                AppIcons.Default.CanoeEmpty,
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
                                CategoryType.FISHERMEN -> {
                                    IconButton(onClick = { showFishermanSelection = true }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                AppIcons.Default.Fisherman,
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
                                CategoryType.NOTES -> {
                                    IconButton(onClick = { showAddNoteDialog = true }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.StickyNote2,
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
                                CategoryType.TARGET_SPECIES -> {
                                    IconButton(onClick = { showSpeciesSelection = true }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                AppIcons.Default.TargetFish,
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
                                CategoryType.WATER -> {}
                                CategoryType.WEATHER -> {}
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
                                            showEditTripDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Trip"
                                            )
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            if (showPhotos) Text("Hide Photos")
                                            else Text("Show Photos") },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.toggleShowPhotos()
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (showPhotos) Icons.Default.VisibilityOff
                                                else Icons.Default.Visibility,
                                                contentDescription = null
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
                                                        viewModel.saveTrip(
                                                            trip.copy(
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
                                                    tint =
                                                        if (trip.latitude != null) Color(0xFF4CAF50)
                                                        else LocalContentColor.current
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
                                            Icon(
                                                Icons.Default.Map,
                                                contentDescription = null,
                                                tint =
                                                    if (trip.latitude != null) Color(0xFF4CAF50)
                                                    else LocalContentColor.current
                                            )
                                        }
                                    )

                                    if (trip.latitude != null) {
                                        DropdownMenuItem(
                                            text = { Text("Clear Location") },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    viewModel.saveTrip(
                                                        trip.copy(
                                                            latitude = null,
                                                            longitude = null
                                                        )
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Location cleared",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
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
                    val thumbnail by viewModel.tripThumbnail().collectAsState(initial = null)

                    LazyColumn(horizontalAlignment = Alignment.Start) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.Boat,
                                    modifier = Modifier.size(64.dp),
                                    onClick = { viewModel.toggleShowPhotos()
                                    }
                                )
                                Text(
                                    text = details.trip.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getOnMainColor(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Start: ${dateTimeFormatter.format(Date(details.trip.startDate))}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getOnSecondaryColor()
                                    )
                                    Text(
                                        text = "End: ${dateTimeFormatter.format(Date(details.trip.endDate))}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getOnSecondaryColor()
                                    )
                                }
                                if (details.trip.latitude != null && details.trip.longitude != null) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "View on map",
                                        tint = getOnMainColor(),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                val mapUri =
                                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${details.trip.latitude},${details.trip.longitude}")
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
                                NotesIconButton(
                                    noteCount = details.notes.size,
                                    onClick = { showNotesDialog = true }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = getOnMainColor()
                            )

                            AnimatedVisibility(visible = showPhotos) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    PhotoPickerRow(
                                        photos = details.photos,
                                        onPhotoSelected = { uri ->
                                            viewModel.addTripPhoto(tripId = tripId, uri = uri, true)
                                        },
                                        onPhotoTaken = { uri ->
                                            viewModel.addTripPhoto(
                                                tripId = tripId,
                                                uri = uri,
                                                false
                                            )
                                        },
                                        onSetThumbnail = { photo ->
                                            viewModel.setTripThumbnail(
                                                tripId = tripId,
                                                photoId = photo.id
                                            )
                                        },
                                        onPhotoDeleted = { photo ->
                                            viewModel.deleteTripPhoto(tripId, photo.id)
                                        }
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier
                                            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                        thickness = 1.dp,
                                        color = getOnMainColor()
                                    )
                                }
                            }

                            if (summary.fishCaught != 0 || now >= trip.startDate) {
                                TripHighlightCard(
                                    summary = summary,
                                    onClick = {
                                        navigateToFishList(FishFilter(
                                            tripId = trip.id)
                                        )
                                    },
                                    onFishClick = {
                                        navigateToFishList(FishFilter(
                                            tripId = trip.id)
                                        )
                                    },
                                    onTargetFishClick = {
                                        navigateToFishList(FishFilter(
                                            tripId = trip.id,
                                            targetOnly = true)
                                        )
                                    }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                    thickness = 1.dp,
                                    color = getOnMainColor()
                                )
                            }

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
                                                    bodyOfWaterId = bodyOfWater.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onSetThumbnail = { bodyOfWater ->
                                                bodyOfWaterForPhoto = bodyOfWater
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            onClearThumbnail = { bodyOfWater ->
                                                viewModel.deleteBodyOfWaterThumbnail(bodyOfWater.id)
                                            },
                                            onDelete = { bodyOfWater ->
                                                viewModel.removeTripBodyOfWater(
                                                    tripId,
                                                    bodyOfWater.id
                                                )
                                            }
                                        )
                                    }

                                    CategoryType.EVENTS -> {
                                        Column() {
                                            Row(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = "Events",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = "(${eventSummaries.size})",
                                                        style = MaterialTheme.typography.titleSmall
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        navigateToAddEvent(tripId)
                                                    },
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        containerColor = getMainButtonColor(),
                                                        contentColor = getOnMainButtonColor()
                                                    ),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Add Event"
                                                    )
                                                }
                                            }
                                            val totalItems = eventSummaries.size
                                            eventSummaries.forEachIndexed { index, eventSummary ->
                                                EventItem(
                                                    item = eventSummary,
                                                    modifier = Modifier.padding(
                                                        horizontal = 16.dp,
                                                        vertical = 4.dp
                                                    ),
                                                    index = index,
                                                    totalItems = totalItems,
                                                    thumbnailFlow = viewModel.eventThumbnail(
                                                        eventSummary.event.id
                                                    ),
                                                    photosFlow = viewModel.eventPhotos(eventSummary.event.id),
                                                    showPhotoPicker = true,
                                                    onClick = {
                                                        navigateToEventDetails(
                                                            eventSummary.event.id,
                                                            CategoryType.TARGET_SPECIES
                                                        )
                                                    },
                                                    onFishClick = { _, _, targetOnly ->
                                                        navigateToFishList(FishFilter(
                                                            tripId = trip.id,
                                                            eventId = eventSummary.event.id,
                                                            targetOnly = targetOnly)
                                                        )
                                                    },
                                                    onFishermanClick = {
                                                        navigateToEventDetails(
                                                            eventSummary.event.id,
                                                            CategoryType.FISHERMEN
                                                        )
                                                    },
                                                    onPhotoAdded = {
                                                        viewModel.addEventPhoto(
                                                            eventSummary.event.id,
                                                            it,
                                                            true
                                                        )
                                                    },
                                                    onPhotoTaken = {
                                                        viewModel.addEventPhoto(
                                                            eventSummary.event.id,
                                                            it,
                                                            false
                                                        )
                                                    },
                                                    onSetThumbnail = { photo ->
                                                        viewModel.setEventThumbnail(
                                                            eventSummary.event.id,
                                                            photo.id
                                                        )
                                                    },
                                                    onPhotoDeleted = { photo ->
                                                        viewModel.deleteEventPhoto(
                                                            eventSummary.event.id,
                                                            photo.id
                                                        )
                                                    },
                                                    onDelete = { eventToDelete = eventSummary },
                                                    onSetLocation = if (hasLocationPermission) {
                                                        {
                                                            scope.launch {
                                                                val location =
                                                                    viewModel.fetchLocation()
                                                                if (location != null) {
                                                                    viewModel.upsertEvent(
                                                                        eventSummary.event.copy(
                                                                            latitude = location.latitude,
                                                                            longitude = location.longitude
                                                                        )
                                                                    )
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
                                                        eventToUpdateLocation = eventSummary
                                                        locationPickerEvent.openPicker()
                                                    },
                                                    onUseTripLocation = if (details.trip.latitude != null) {
                                                        {
                                                            scope.launch {
                                                                viewModel.upsertEvent(
                                                                    eventSummary.event.copy(
                                                                        latitude = details.trip.latitude,
                                                                        longitude = details.trip.longitude
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    } else null,
                                                    onClearLocation = {
                                                        scope.launch {
                                                            viewModel.upsertEvent(
                                                                eventSummary.event.copy(
                                                                    latitude = null,
                                                                    longitude = null
                                                                )
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    CategoryType.FISHERMEN -> {
                                        Column() {
                                            FishermanSummary(
                                                fishermanCount = summary.fishermanCount,
                                                tackleBoxCount = summary.tackleBoxCount,
                                                onClick = { navigateToSelectTripCrew(tripId) }
                                            )
                                            FishermanSummaries(
                                                list = sortedFishermanList,
                                                thumbnailFlow = { fisherman ->
                                                    viewModel.fishermanThumbnail(fisherman.id)
                                                },
                                                photosFlow = { fisherman ->
                                                    viewModel.fishermanPhotos(fisherman.id)
                                                },
                                                tackleBoxSelections = tackleBoxMap,
                                                getTackleBoxesForFisherman = { fishermanId ->
                                                    viewModel.getTackleBoxesForFisherman(fishermanId)
                                                        .collectAsState(initial = emptyList()).value
                                                },
                                                getLureCount = { tackleBoxId ->
                                                    viewModel.getLureCountForTackleBox(tackleBoxId)
                                                        .collectAsState(initial = 0).value
                                                },
                                                getLuresInTacklebox = { tackleBoxId ->
                                                    viewModel.getLuresInTackleBox(tackleBoxId)
                                                        .collectAsState(initial = emptyList()).value
                                                },
                                                onAdd = {
                                                    showFishermanSelection = true
                                                },
                                                onAddTackleBox = { tackleBox ->
                                                    viewModel.createAndAssignTackleBox(tackleBox)
                                                },
                                                onAddLuresToTackleBox = { tackleBox ->
                                                    navigateToAddLures(tackleBox)
                                                },
                                                onClick = { fisherman -> /* do nothing */
                                                },
                                                onFishClick = { fisherman, target ->
                                                    navigateToFishList(
                                                        FishFilter(
                                                            tripId = trip.id,
                                                            fishermanId = fisherman.id,
                                                            targetOnly = target
                                                        )
                                                    )
                                                },
                                                onTackleBoxSelected = { fisherman, tackleBoxId ->
                                                    viewModel.updateTripFisherman(
                                                        fisherman.id,
                                                        tackleBoxId
                                                    )
                                                },
                                                onPhotoTaken = { fisherman, uri ->
                                                    viewModel.addFishermanPhoto(
                                                        fisherman.id,
                                                        uri,
                                                        false
                                                    )
                                                },
                                                onPhotoAdded = { fisherman, uri ->
                                                    viewModel.addFishermanPhoto(
                                                        fisherman.id,
                                                        uri,
                                                        true
                                                    )
                                                },
                                                onSetThumbnail = { fisherman, photo ->
                                                    viewModel.setFishermanThumbnail(
                                                        fisherman.id,
                                                        photo.id
                                                    )
                                                },
                                                onPhotoDeleted = { fisherman, photo ->
                                                    viewModel.deleteFishermanPhoto(
                                                        fisherman.id,
                                                        photo.id
                                                    )
                                                },
                                                onDelete = { fisherman ->
                                                    viewModel.removeTripFisherman(fisherman.id)
                                                }
                                            )
                                        }
                                    }
                                    CategoryType.NOTES -> {
                                        NoteRow(
                                            noteList = sortedNotes,
                                            onAdd = { showAddNoteDialog = true },
                                            onEdit = { noteToEdit = it },
                                            onDelete = { noteToDelete = it }
                                        )
                                    }
                                    CategoryType.TARGET_SPECIES -> {
                                        SpeciesSummaries(
                                            items = sortedTargetSpeciesList,
                                            modifier = Modifier.padding(
                                                vertical = 8.dp,
                                                horizontal = 16.dp
                                            ),
                                            thumbnailFlow = { species ->
                                                viewModel.speciesThumbnail(species.id)
                                            },
                                            onAdd = { showSpeciesSelection = true },
                                            onClick = { species ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    speciesId = species.id,
                                                    targetOnly = true)
                                                )
                                            },
                                            onSetThumbnail = { species ->
                                                speciesForPhoto = species
                                                speciesPhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            onClearThumbnail = { species ->
                                                viewModel.deleteSpeciesThumbnail(species.id)
                                            },
                                            onDelete = { species ->
                                                viewModel.removeTripTargetSpecies(
                                                    tripId,
                                                    species.id
                                                )
                                            }
                                        )
                                    }

                                    CategoryType.WATER -> {
                                        WaterSummaryRow(
                                            waterList = sortedWaterList,
                                            onEdit = { waterToEdit = it },
                                            onFishClick = { water, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
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
                                            onEdit = { weatherToEdit = it },
                                            onFishClick = { weather, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
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

                    if (showEditTripDialog) {
                        EditTripDialog(
                            item = details.trip,
                            onDismiss = { showEditTripDialog = false },
                            onConfirm = { confirmedItem ->
                                viewModel.saveTrip(confirmedItem)
                                showEditTripDialog = false
                            }
                        )
                    }
                }
            }

            // DELETE CONFIRMATION
            eventToDelete?.let { item ->
                DeleteConfirmationDialog(
                    title = "Delete Event",
                    message = """Are you sure you want to delete '${item.event.name}'?

This cannot be undone.

All fish (${item.fishCaught}) associated with this event will also be deleted.""",
                    onConfirm = {
                        viewModel.deleteEvent(item.event)
                        eventToDelete = null
                    },
                    onDismiss = { eventToDelete = null }
                )
            }
            noteToDelete?.let { note ->
                DeleteConfirmationDialog(
                    title = "Delete Note",
                    message = "Are you sure you want to delete this note?",
                    onConfirm = {
                        viewModel.deleteNote(note.id)
                        noteToDelete = null
                    },
                    onDismiss = { noteToDelete = null }
                )
            }
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

            noteToEdit?.let { note ->
                EditNoteDialog(
                    item = note,
                    onDismiss = { noteToEdit = null },
                    onConfirm = { note ->
                        viewModel.addNote(note.id, note.content)
                        noteToEdit = null
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
                            imageVector = AppIcons.Default.WaterSet,
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
                    onAddWaterClarity = { showAddWaterClarityDialog = true }
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
                            imageVector = AppIcons.Default.WaterSet,
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
                    onAddSkyCondition = { showAddSkyConditionDialog = true }
                )
            }

            if (showBodiesOfWaterSelection) {
                BodyOfWaterSelection(
                    items = allBodiesOfWater,
                    selectedItems = details.bodiesOfWater,
                    onSelected = { selectedBodyOfWater ->
                        viewModel.addTripBodyOfWater(tripId, selectedBodyOfWater.id)
                    },
                    onUnselected = { selectedBodyOfWater ->
                        viewModel.removeTripBodyOfWater(tripId, selectedBodyOfWater.id)
                    },
                    onAdd = {
                        showAddBodyOfWaterDialog = true
                    },
                    onDone = { showBodiesOfWaterSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    usageMap = bodyOfWaterUsageMap,
                    maxUsage = details.events.size,
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
                    selectedItems = details.fishermen,
                    onSelected = { fisherman ->
                        viewModel.addTripFisherman(fishermanId = fisherman.id, null)
                    },
                    onUnselected = { fisherman ->
                        viewModel.removeTripFisherman(fishermanId = fisherman.id)
                    },
                    onAdd = {
                        showAddFishermanDialog = true
                    },
                    onDone = { showFishermanSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    usageMap = fishermanUsageMap,
                    maxUsage = details.events.size,
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
            if (showSpeciesSelection) {
                SpeciesSelection(
                    items = allSpecies,
                    selectedItems = details.targetSpecies,
                    onSelected = { selectedSpecies ->
                        viewModel.addTripTargetSpecies(tripId, selectedSpecies.id)
                    },
                    onUnselected = { selectedSpecies ->
                        viewModel.removeTripTargetSpecies(tripId, selectedSpecies.id)
                    },
                    onAdd = {
                        showAddSpeciesDialog = true
                    },
                    onDone = { showSpeciesSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    usageMap = speciesUsageMap,
                    maxUsage = details.events.size,
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

            if (showNotesDialog) {
                NotesDialog(
                    notes = details.notes,
                    onDismiss = { showNotesDialog = false },
                    onSaveNote = { noteId, content ->
                        viewModel.addNote(noteId, content)
                    },
                    onDeleteNote = { noteId ->
                        viewModel.deleteNote(noteId)
                    }
                )
            }
        }
    }

    if (showAddBodyOfWaterDialog) {
        AddBodyOfWaterDialog(
            onDismiss = { showAddBodyOfWaterDialog = false },
            onConfirm = { name ->
                viewModel.addTripBodyOfWater(tripId, BodyOfWater(name = name))
                showAddBodyOfWaterDialog = false
            }
        )
    }
    if (showAddFishermanDialog) {
        AddFishermanDialog(
            onDismiss = { showAddFishermanDialog = false },
            onAdd = { first, last, nick ->
                val fisherman = Fisherman(
                    firstName = first.trim(),
                    lastName = last.trim(),
                    nickname = nick.trim()
                )
                viewModel.addFisherman(fisherman) {
                    // Do nothing on Success
                }
                showAddFishermanDialog = false
            }
        )
    }
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { content ->
                viewModel.addNote(null, content)
                showAddNoteDialog = false
            }
        )
    }
    if (showAddSkyConditionDialog) {
        AddSkyConditionDialog(
            onDismiss = { showAddSkyConditionDialog = false },
            onConfirm = { name ->
                viewModel.addSkyCondition(SkyCondition(name = name)) {
                    // Do nothing on success
                }
                showAddSkyConditionDialog = false
            }
        )
    }
    if (showAddSpeciesDialog) {
        AddSpeciesDialog(
            onDismiss = { showAddSpeciesDialog = false },
            onConfirm = { name ->
                viewModel.addTripTargetSpecies(tripId, Species(name = name))
                showAddSpeciesDialog = false
            }
        )
    }
    if (showAddWaterClarityDialog) {
        AddWaterClarityDialog(
            onDismiss = { showAddWaterClarityDialog = false },
            onConfirm = { name ->
                viewModel.addWaterClarity(WaterClarity(name = name)) {
                    // Do nothing on success
                }
                showAddWaterClarityDialog = false
            }
        )
    }


    // Confirmation Dialog for long pressing a Body of Water
    if (showUpdateAllCatchesDialog && bodyOfWaterToUpdateAll != null) {
        UpdateAllCatchesDialog(
            bodyOfWater = bodyOfWaterToUpdateAll,
            content = "Do you want to update all the fish logged for this trip to the body of water: ${bodyOfWaterToUpdateAll?.name}?",
            onDismiss = {
                bodyOfWaterToUpdateAll = null
            },
            onConfirm = { targetBody ->
                scope.launch {
                    // UI waits for the database operation to finish
                    viewModel.updateBodyOfWaterForTrip(
                        newBodyOfWaterId = targetBody.id,
                        tripId = tripId
                    )

                    // Runs only after the batch update successfully completes
                    Toast.makeText(context, "Catches updated to ${targetBody.name}", Toast.LENGTH_SHORT).show()

                    // Dismiss the dialog by clearing the state inside the coroutine block
                    bodyOfWaterToUpdateAll = null
                }
            }
        )
    }
}
