package com.funjim.fishstory.ui.screens

import com.funjim.fishstory.ui.dialogs.DeleteDialog
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
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitScope
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.sections.BodyOfWaterSummarySection
import com.funjim.fishstory.ui.sections.FishermanSummarySection
import com.funjim.fishstory.ui.sections.LimitSummarySection
import com.funjim.fishstory.ui.sections.NoteSection
import com.funjim.fishstory.ui.sections.SpeciesSummarySection
import com.funjim.fishstory.ui.sections.WaterSummarySection
import com.funjim.fishstory.ui.sections.WeatherSummarySection
import com.funjim.fishstory.ui.utils.CategoryChipConfig
import com.funjim.fishstory.ui.utils.CategoryRow
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.cards.EventItem
import com.funjim.fishstory.ui.utils.FishFilter
import com.funjim.fishstory.ui.utils.NotesIconButton
import com.funjim.fishstory.ui.utils.StatusGumball
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.cards.TripHighlightCard
import com.funjim.fishstory.ui.crud.AddBodyOfWaterDialog
import com.funjim.fishstory.ui.crud.AddFishermanDialog
import com.funjim.fishstory.ui.crud.AddNoteDialog
import com.funjim.fishstory.ui.crud.AddSkyConditionDialog
import com.funjim.fishstory.ui.crud.AddSpeciesDialog
import com.funjim.fishstory.ui.crud.AddWaterClarityDialog
import com.funjim.fishstory.ui.crud.EditNoteDialog
import com.funjim.fishstory.ui.crud.EditTripDialog
import com.funjim.fishstory.ui.crud.LimitDialog
import com.funjim.fishstory.ui.crud.NotesDialog
import com.funjim.fishstory.ui.crud.WaterDialog
import com.funjim.fishstory.ui.crud.WeatherDialog
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnSecondaryColor
import com.funjim.fishstory.ui.utils.rememberGumballStatus
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.ui.selection.BodyOfWaterSelection
import com.funjim.fishstory.ui.selection.FishermanSelection
import com.funjim.fishstory.ui.selection.SpeciesSelection
import com.funjim.fishstory.viewmodels.TripDetailsUiState
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.funjim.fishstory.ui.dialogs.GeneralDialog
import com.funjim.fishstory.ui.selection.LimitSelection
import com.funjim.fishstory.ui.utils.BadgeHelp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    viewModel: TripViewModel,
    tripId: String,
    navigateToFishList: (FishFilter) -> Unit,
    navigateToAddEvent: (String) -> Unit,
    navigateToEventDetails: (String, CategoryType) -> Unit,
    navigateToAddLures: (TackleBox) -> Unit,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }

    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    var bodyOfWaterToDelete by remember { mutableStateOf<BodyOfWater?>(null) }
    var eventToDelete by remember { mutableStateOf<EventSummary?>(null) }
    var fishermanToDelete by remember { mutableStateOf<Fisherman?>(null) }
    var limitToDelete by remember { mutableStateOf<Limit?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var speciesToDelete by remember { mutableStateOf<Species?>(null) }
    var waterToDelete by remember { mutableStateOf<Water?>(null) }
    var weatherToDelete by remember { mutableStateOf<Weather?>(null) }

    var limitToEdit by remember { mutableStateOf<Limit?>(null) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var waterToEdit by remember { mutableStateOf<Water?>(null) }
    var weatherToEdit by remember { mutableStateOf<Weather?>(null) }

    var eventToUpdateLocation by remember { mutableStateOf<EventSummary?>(null) }

    var showAddBodyOfWaterToUpdate by remember { mutableStateOf(false) }
    var bodyOfWaterToUpdateAll by remember { mutableStateOf<BodyOfWater?>(null) }

    var pendingWaterClarityId by remember { mutableStateOf<String?>(null) }
    var pendingSkyConditionId by remember { mutableStateOf<String?>(null) }

    var showAddLimitDialog by remember { mutableStateOf(false) }
    var showAddBodyOfWaterDialog by remember { mutableStateOf(false) }
    var showAddFishermanDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddSpeciesDialog by remember { mutableStateOf(false) }
    var showAddWaterClarityDialog by remember { mutableStateOf(false) }
    var showAddSkyConditionDialog by remember { mutableStateOf(false) }

    var showEditTripDialog by remember { mutableStateOf(false) }

    var showBodyOfWaterSelection by remember { mutableStateOf(false) }
    var showFishermanSelection by remember { mutableStateOf(false) }
    var showLimitSelection by remember { mutableStateOf(false) }
    var showSpeciesSelection by remember { mutableStateOf(false) }

    var showNotesDialog by remember { mutableStateOf(false) }

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

    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
    val allFishermen by viewModel.fishermen.collectAsStateWithLifecycle(emptyList())
    val allLimits by viewModel.allLimits.collectAsStateWithLifecycle(emptyList())
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    val allSkyConditions by viewModel.allSkyConditions.collectAsStateWithLifecycle()
    val allWaterClarity by viewModel.allWaterClarity.collectAsStateWithLifecycle()

    val bodyOfWaterSummaries by viewModel.bodyOfWaterSummaries.collectAsStateWithLifecycle()
    val fishermanSummaries by viewModel.fishermanSummaries.collectAsStateWithLifecycle()
    val limitSummaries by viewModel.limitSummaries.collectAsStateWithLifecycle()
    val targetSpeciesSummaries by viewModel.targetSpeciesSummaries.collectAsStateWithLifecycle()
    val waterSummaries by viewModel.waterSummaries.collectAsStateWithLifecycle()
    val weatherSummaries by viewModel.weatherSummaries.collectAsStateWithLifecycle()

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

            val limitUsageMap: Map<String, Int> = limitSummaries
                .associate { it.limit.id to it.summaryList.size }

            val speciesUsageMap: Map<String, Int> = events
                .flatMap { it.targetSpecies }
                .groupingBy { it.id }
                .eachCount() // Returns a Map<String, Int> where Key = speciesId, Value = count

            val sortedBodyOfWaterList = remember(bodyOfWaterSummaries) {
                bodyOfWaterSummaries.sortedBy { it.bodyOfWater.name }
            }
            val sortedFishermen = remember(allFishermen) {
                allFishermen.sortedBy { it.fullName }
            }
            val sortedLimits = remember(allLimits) {
                allLimits.sortedBy { it.name }
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

            val limitExceedCount = limitSummaries.sumOf { it.limitExceededCount }
            val limitReachedCount = limitSummaries.sumOf { it.limitReachedCount }
            val limitCount = limitSummaries.sumOf { it.limitCount }

            val gumballStatus = rememberGumballStatus(
                limitExceededCount = limitExceedCount,
                limitReachedCount = limitReachedCount,
                limitCount = limitCount
            )

            val categoryConfigs = remember(
                sortedBodyOfWaterList,
                sortedFishermanList,
                gumballStatus,
                limitSummaries,
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
                        category = CategoryType.LIMITS,
                        icon = {
                            StatusGumball(
                                status = gumballStatus,
                                size = 18.dp,
                                borderWidth = 2.dp,
                                borderColor = getOnCardSecondaryColor()
                            )
                        },
                        count = limitSummaries.size
                    ),
                    CategoryChipConfig(
                        category = CategoryType.NOTES,
                        icon = { Icon(
                            AppIcons.Default.Notes,
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
                                    IconButton(onClick = { showBodyOfWaterSelection = true }) {
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
                                                    .offset(x = 4.dp, y = 4.dp)
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
                                                    .offset(x = 4.dp, y = 4.dp)
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
                                                    .offset(x = 4.dp, y = 4.dp)
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
                                CategoryType.LIMITS -> {
                                    IconButton(onClick = { showLimitSelection = true }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            StatusGumball(
                                                status = gumballStatus,
                                                size = 28.dp,
                                                borderWidth = 2.dp,
                                                borderColor = getOnCardSecondaryColor()
                                            )
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .offset(x = 4.dp, y = 4.dp)
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
                                                AppIcons.Default.Notes,
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp))

                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .offset(x = 4.dp, y = 4.dp)
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
                                                    .offset(x = 4.dp, y = 4.dp)
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
                    val thumbnail by viewModel.getTripThumbnail().collectAsState(initial = null)

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
                                                    "https://www.google.com/maps/search/?api=1&query=${details.trip.latitude},${details.trip.longitude}".toUri()
                                                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                                try {
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {
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
                                    useImperial = appSettings.display.useImperialUnits,
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
                                        BodyOfWaterSummarySection(
                                            list = sortedBodyOfWaterList,
                                            verbose = appSettings.display.verboseCards,
                                            showHelp = appSettings.display.showHelp,
                                            modifier = Modifier.padding(
                                                vertical = 8.dp,
                                                horizontal = 16.dp
                                            ),
                                            emptyText = "No bodies of water selected.",
                                            helpText = """Bodies of water are where you're actively fishing. 

Selecting them places those bodies of water at the top of the list when logging catches for quicker entry.""",
                                            thumbnailFlow = { bodyOfWater ->
                                                viewModel.getBodyOfWaterThumbnail(bodyOfWater.id)
                                            },
                                            onAdd = { showBodyOfWaterSelection = true },
                                            onClearThumbnail = { bodyOfWater ->
                                                viewModel.deleteBodyOfWaterThumbnail(bodyOfWater.id)
                                            },
                                            onClick = { bodyOfWater ->
                                                bodyOfWaterToUpdateAll = bodyOfWater
                                                showAddBodyOfWaterToUpdate = true
                                            },
                                            onDelete = { bodyOfWater ->
                                                bodyOfWaterToDelete = bodyOfWater
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
                                            onToggleHelp = { viewModel.toggleHelp() }
                                        )
                                    }

                                    CategoryType.EVENTS -> {
                                        Column() {
                                            Row(
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
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
                                                    if (eventSummaries.size > 1) {
                                                        BadgeCount(
                                                            count = eventSummaries.size,
                                                            badgeColor = getMainButtonColor(),
                                                            textColor = getOnMainButtonColor()
                                                        )
                                                    }
                                                }

                                                BadgeHelp(
                                                    selected = appSettings.display.showHelp,
                                                    badgeColor = getMainButtonColor(),
                                                    textColor = getOnMainButtonColor(),
                                                    textStyle = MaterialTheme.typography.titleMedium,
                                                    size = 24.dp,
                                                    onClick = { viewModel.toggleHelp() }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))

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
                                            if (totalItems == 0) {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = """No events created.

Events mark the times you are actively fishing, allowing caught fish to be logged and linked to specific fishing sessions.
                                                    """.trimMargin(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = getOnSecondaryColor(),
                                                        modifier = Modifier.padding(horizontal = 16.dp)
                                                    )
                                                }
                                            } else {
                                                if (appSettings.display.showHelp) {
                                                    Text(
                                                        text = "Events mark the times you are actively fishing, allowing caught fish to be logged and linked to specific fishing sessions.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = getOnSecondaryColor(),
                                                        modifier = Modifier.padding(horizontal = 32.dp)
                                                    )
                                                }
                                                eventSummaries.forEachIndexed { index, eventSummary ->
                                                    EventItem(
                                                        item = eventSummary,
                                                        verbose = appSettings.display.verboseCards,
                                                        modifier = Modifier.padding(
                                                            horizontal = 16.dp,
                                                            vertical = 4.dp
                                                        ),
                                                        index = index,
                                                        totalItems = totalItems,
                                                        thumbnailFlow = viewModel.getEventThumbnail(
                                                            eventSummary.event.id
                                                        ),
                                                        photosFlow = viewModel.getEventPhotos(
                                                            eventSummary.event.id
                                                        ),
                                                        showPhotoPicker = true,
                                                        onClick = {
                                                            navigateToEventDetails(
                                                                eventSummary.event.id,
                                                                CategoryType.TARGET_SPECIES
                                                            )
                                                        },
                                                        onFishClick = { _, _, targetOnly ->
                                                            navigateToFishList(
                                                                FishFilter(
                                                                    tripId = trip.id,
                                                                    eventId = eventSummary.event.id,
                                                                    targetOnly = targetOnly
                                                                )
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
                                    }

                                    CategoryType.FISHERMEN -> {
                                        Column() {
                                            FishermanSummarySection(
                                                list = sortedFishermanList,
                                                verbose = appSettings.display.verboseCards,
                                                showHelp = appSettings.display.showHelp,
                                                modifier = Modifier.padding(
                                                    vertical = 8.dp,
                                                    horizontal = 16.dp
                                                ),
                                                emptyText = "No fishermen selected.",
                                                helpText = """At least one participant must be added to this trip.

Logging catches will be restricted to the selected fishermen. You can assign a tackle box to each fisherman to filter available lures, or leave it unassigned to make all lures accessible.""",
                                                thumbnailFlow = { fisherman ->
                                                    viewModel.getFishermanThumbnail(fisherman.id)
                                                },
                                                photosFlow = { fisherman ->
                                                    viewModel.getFishermanPhotos(fisherman.id)
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
                                                onAddLuresToTackleBox = { tackleBox ->
                                                    navigateToAddLures(tackleBox)
                                                },
                                                onAddTackleBox = { tackleBox ->
                                                    viewModel.createAndAssignTackleBox(tackleBox)
                                                },
                                                onClick = {},
                                                onDelete = { fisherman ->
                                                    fishermanToDelete = fisherman
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
                                                onPhotoAdded = { fisherman, uri ->
                                                    viewModel.addFishermanPhoto(
                                                        fisherman.id,
                                                        uri,
                                                        true
                                                    )
                                                },
                                                onPhotoDeleted = { fisherman, photo ->
                                                    viewModel.deleteFishermanPhoto(
                                                        fisherman.id,
                                                        photo.id
                                                    )
                                                },
                                                onPhotoTaken = { fisherman, uri ->
                                                    viewModel.addFishermanPhoto(
                                                        fisherman.id,
                                                        uri,
                                                        false
                                                    )
                                                },
                                                onSetThumbnail = { fisherman, photo ->
                                                    viewModel.setFishermanThumbnail(
                                                        fisherman.id,
                                                        photo.id
                                                    )
                                                },
                                                onTackleBoxSelected = { fisherman, tackleBoxId ->
                                                    viewModel.updateTripFisherman(
                                                        fisherman.id,
                                                        tackleBoxId
                                                    )
                                                },
                                                onToggleHelp = { viewModel.toggleHelp() }
                                            )
                                        }
                                    }
                                    CategoryType.LIMITS -> {
                                        LimitSummarySection(
                                            itemList = limitSummaries,
                                            verbose = appSettings.display.verboseCards,
                                            showHelp = appSettings.display.showHelp,
                                            useImperial = appSettings.display.useImperialUnits,
                                            emptyText = "No limits created.",
                                            helpText = """Limits track your total catches to help ensure compliance with local fishing regulations.
                                                |
                                                |Limits set here automatically apply to all events on this trip.""".trimMargin(),
                                            scope = LimitScope.TRIP,
                                            modifier = Modifier.padding(
                                                vertical = 8.dp,
                                                horizontal = 16.dp
                                            ),
                                            eventThumbnailProvider = { event ->
                                                val thumbnailFlow = remember(event.id) {
                                                    viewModel.getEventThumbnail(event.id)
                                                }

                                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                                ThumbnailBox(
                                                    thumbnail = thumbnail,
                                                    imageVector = AppIcons.Default.CanoeEmpty,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            speciesThumbnailProvider = { species ->
                                                val thumbnailFlow = remember(species.id) {
                                                    viewModel.getSpeciesThumbnail(species.id)
                                                }

                                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                                ThumbnailBox(
                                                    thumbnail = thumbnail,
                                                    imageVector = AppIcons.Default.LeapingFishWithFins,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onAdd = { showLimitSelection = true },
                                            onDelete = { limit ->
                                                limitToDelete = limit
                                            },
                                            onEdit = { limitToEdit = it },
                                            onToggleHelp = { viewModel.toggleHelp() }
                                        )
                                    }
                                    CategoryType.NOTES -> {
                                        NoteSection(
                                            noteList = sortedNotes,
                                            showHelp = appSettings.display.showHelp,
                                            emptyText = "No notes created.",
                                            helpText = "Notes help you remember specific details and conditions during this trip.",
                                            onAdd = { showAddNoteDialog = true },
                                            onDelete = { noteToDelete = it },
                                            onEdit = { noteToEdit = it },
                                            onToggleHelp = { viewModel.toggleHelp() }
                                        )
                                    }
                                    CategoryType.TARGET_SPECIES -> {
                                        SpeciesSummarySection(
                                            items = sortedTargetSpeciesList,
                                            verbose = appSettings.display.verboseCards,
                                            showHelp = appSettings.display.showHelp,
                                            emptyText = "No target species selected.",
                                            helpText = """Target species are what you're actively fishing for. 

Selecting them places those species at the top of the list when logging catches for quicker entry.""",
                                            modifier = Modifier.padding(
                                                vertical = 8.dp,
                                                horizontal = 16.dp
                                            ),
                                            thumbnailFlow = { species ->
                                                viewModel.getSpeciesThumbnail(species.id)
                                            },
                                            onAdd = { showSpeciesSelection = true },
                                            onClearThumbnail = { species ->
                                                viewModel.deleteSpeciesThumbnail(species.id)
                                            },
                                            onClick = { species ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    speciesId = species.id,
                                                    targetOnly = true)
                                                )
                                            },
                                            onDelete = { species ->
                                                speciesToDelete = species
                                            },
                                            onSetThumbnail = { species ->
                                                speciesForPhoto = species
                                                speciesPhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            onToggleHelp = { viewModel.toggleHelp() },
                                        )
                                    }
                                    CategoryType.WATER -> {
                                        WaterSummarySection(
                                            waterList = sortedWaterList,
                                            verbose = appSettings.display.verboseCards,
                                            showHelp = appSettings.display.showHelp,
                                            emptyText = "No water conditions specified.",
                                            helpText = "Adding water conditions will automatically apply them to any new catches logged for an event.",
                                            useImperial = appSettings.display.useImperialUnits,
                                            onDelete = { waterToDelete = it },
                                            onEdit = { waterToEdit = it },
                                            onFishClick = { water, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    waterId = water.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onThumbnailLongClick = {
                                                viewModel.toggleImperialUnits()
                                            },
                                            onToggleHelp = { viewModel.toggleHelp() }
                                        )
                                    }
                                    CategoryType.WEATHER -> {
                                        WeatherSummarySection(
                                            list = sortedWeatherList,
                                            verbose = appSettings.display.verboseCards,
                                            showHelp = appSettings.display.showHelp,
                                            useImperial = appSettings.display.useImperialUnits,
                                            emptyText = "No weather conditions specified.",
                                            helpText = "Adding weather conditions will automatically apply them to any new catches logged for an event.",
                                            onDelete = { weatherToDelete = it },
                                            onEdit = { weatherToEdit = it },
                                            onFishClick = { weather, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    weatherId = weather.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onThumbnailLongClick = {
                                                viewModel.toggleImperialUnits()
                                            },
                                            onToggleHelp = { viewModel.toggleHelp() },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DELETE CONFIRMATION
            bodyOfWaterToDelete?.let { bodyOfWater ->
                val summary = bodyOfWaterSummaries.find { it.bodyOfWater.id == bodyOfWater.id }
                summary?.fishCaught?.let { fishCaught ->
                    DeleteDialog(
                        title = "Remove Body of Water",
                        message =
                            if (fishCaught > 0)
                                """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with this body of water for this trip.
                                    |
                                    |Removing this body of water will not affect those fish.  It will only affect entry of future catches.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                            else
                                """Are you sure you want to remove this body of water? 
                                    |
                                    |No fish are currently associated with this body of water for this trip.
                                """.trimMargin(),
                        confirmButtonText = "Remove",
                        onConfirm = {
                            viewModel.removeTripBodyOfWater(
                                tripId,
                                bodyOfWater.id
                            )
                            bodyOfWaterToDelete = null
                        },
                        onDismiss = { bodyOfWaterToDelete = null }
                    )
                }
            }
            eventToDelete?.let { item ->
                DeleteDialog(
                    title = "Delete Event",
                    message =
                        if (item.fishCaught > 0)
                            """There ${if (item.fishCaught == 1) "is" else "are"} currently ${item.fishCaught} fish associated with this event.
                                |
                                |Deleting this event will delete all fish associated with it. 
                                |
                                |Are you sure you want to proceed?
                            """.trimMargin()
                        else
                            """Are you sure you want to delete this event? 
                                |
                                |No fish are currently associated with this event.
                                """.trimMargin(),
                    onConfirm = {
                        viewModel.deleteEvent(item.event)
                        eventToDelete = null
                    },
                    onDismiss = { eventToDelete = null }
                )
            }
            fishermanToDelete?.let { fisherman ->
                val summary = fishermanSummaries.find { it.fisherman.id == fisherman.id }
                summary?.fishCaught?.let { fishCaught ->
                    DeleteDialog(
                        title = "Remove Fisherman",
                        message =
                            if (fishCaught > 0)
                                """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with this fisherman for this trip.
                                    |
                                    |Removing this fisherman will not affect those fish.  It will only affect entry of future catches.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                            else
                                """Are you sure you want to remove this fisherman? 
                                    |
                                    |No fish are currently associated with this fisherman for this trip.
                                """.trimMargin(),
                        confirmButtonText = "Remove",
                        onConfirm = {
                            viewModel.removeTripFisherman(fisherman.id)
                            fishermanToDelete = null
                        },
                        onDismiss = { fishermanToDelete = null }
                    )
                }
            }
            limitToDelete?.let { limit ->
                DeleteDialog(
                    title = "Remove Limit",
                    message = "Are you sure you want to remove this limit?",
                    onConfirm = {
                        viewModel.removeLimitFromTrip(limit)
                        limitToDelete = null
                    },
                    onDismiss = { limitToDelete = null }
                )
            }
            noteToDelete?.let { note ->
                DeleteDialog(
                    title = "Delete Note",
                    message = "Are you sure you want to delete this note?",
                    onConfirm = {
                        viewModel.deleteNote(note.id)
                        noteToDelete = null
                    },
                    onDismiss = { noteToDelete = null }
                )
            }
            speciesToDelete?.let { species ->
                val summary = targetSpeciesSummaries.find { it.species.id == species.id }
                summary?.fishCaught?.let { fishCaught ->
                    DeleteDialog(
                        title = "Remove Species",
                        message =
                            if (fishCaught > 0)
                                """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with this species for this trip.
                                    |
                                    |Removing this species will not affect those fish.  It will only affect entry of future catches.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                            else
                                """Are you sure you want to remove this species? 
                                    |
                                    |No fish are currently associated with this species for this trip.
                                """.trimMargin(),
                        confirmButtonText = "Remove",
                        onConfirm = {
                            viewModel.removeTripTargetSpecies(
                                tripId,
                                species.id
                            )
                            speciesToDelete = null
                        },
                        onDismiss = { speciesToDelete = null }
                    )
                }

            }
            waterToDelete?.let { water ->
                val summary = waterSummaries.find { it.water.id == water.id }
                summary?.fishCaught?.let { fishCaught ->
                    DeleteDialog(
                        title = "Delete Water Conditions",
                        message =
                            if (fishCaught > 0)
                                """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with these water conditions for this trip.
                                    |
                                    |Deleting these water conditions will remove its data from those fish.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                            else
                                """Are you sure you want to delete these water conditions? 
                                    |
                                    |No fish are currently associated with these water conditions.
                                """.trimMargin(),
                        onConfirm = {
                            viewModel.deleteWater(water.id)
                            waterToDelete = null
                        },
                        onDismiss = { waterToDelete = null }
                    )
                }
            }
            weatherToDelete?.let { item ->
                val summary = weatherSummaries.find { it.weather.id == item.id }
                summary?.fishCaught?.let { fishCaught ->
                    DeleteDialog(
                        title = "Delete Weather Conditions",
                        message =
                            if (fishCaught > 0)
                                """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with these weather conditions for this trip.
                                    |
                                    |Deleting these weather conditions will remove its data from those fish.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                            else
                                """Are you sure you want to delete these weather conditions? 
                                    |
                                    |No fish are currently associated with these weather conditions.
                                """.trimMargin(),
                        onConfirm = {
                            viewModel.deleteWeather(item.id)
                            weatherToDelete = null
                        },
                        onDismiss = { weatherToDelete = null }
                    )
                }
            }

            limitToEdit?.let { limit ->
                LimitDialog(
                    limitToEdit = limit,
                    useImperial = appSettings.display.useImperialUnits,
                    species = allSpecies,
                    speciesThumbnailProvider = { species, size ->
                        val thumbnailFlow = remember(species.id) {
                            viewModel.getSpeciesThumbnail(species.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.LeapingFishWithFins,
                            modifier = Modifier.size(size)
                        )
                    },
                    onConfirm = { limit ->
                        viewModel.addLimit(limit)
                        limitToEdit = null
                    },
                    onDismiss = { limitToEdit = null }
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
                    pendingClarity = pendingWaterClarityId,
                    allClarity = allWaterClarity,
                    useImperial = appSettings.display.useImperialUnits,
                    title = "Edit Water Conditions",
                    thumbnailProvider = { clarity ->
                        val thumbnailFlow = remember(clarity.id) {
                            viewModel.getWaterClarityThumbnail(clarity.id)
                        }
                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.WaterSet,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    onToggleImperialUnits = { viewModel.toggleImperialUnits() },
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
                    pendingSkyCondition = pendingSkyConditionId,
                    allSkyConditions = allSkyConditions,
                    useImperial = appSettings.display.useImperialUnits,
                    title = "Edit Weather Conditions",
                    thumbnailProvider = { sky ->
                        val thumbnailFlow = remember(sky.id) {
                            viewModel.getSkyConditionThumbnail(sky.id)
                        }
                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.WeatherSet,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    onToggleImperialUnits = { viewModel.toggleImperialUnits() },
                    onDismiss = {
                        weatherToEdit = null
                        pendingSkyConditionId = null
                    },
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
                        pendingSkyConditionId = null
                    },
                    onAddSkyCondition = { showAddSkyConditionDialog = true }
                )
            }

            if (showBodyOfWaterSelection) {
                BodyOfWaterSelection(
                    items = allBodiesOfWater,
                    selectedItems = details.bodiesOfWater,
                    onSelected = { selectedBodyOfWater ->
                        viewModel.addTripBodyOfWater(tripId, selectedBodyOfWater.id)
                    },
                    onUnselected = { selectedBodyOfWater ->
                        bodyOfWaterToDelete = selectedBodyOfWater
                    },
                    onAdd = {
                        showAddBodyOfWaterDialog = true
                    },
                    onDone = { showBodyOfWaterSelection = !showBodyOfWaterSelection },
                    modifier = Modifier.fillMaxWidth(),
                    usageMap = bodyOfWaterUsageMap,
                    maxUsage = details.events.size,
                    thumbnailProvider = { bodyOfWater ->
                        val thumbnailFlow = remember(bodyOfWater.id) {
                            viewModel.getBodyOfWaterThumbnail(bodyOfWater.id)
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
                        fishermanToDelete = fisherman
                    },
                    onAdd = {
                        showAddFishermanDialog = true
                    },
                    onDone = { showFishermanSelection = !showFishermanSelection },
                    modifier = Modifier.fillMaxWidth(),
                    usageMap = fishermanUsageMap,
                    maxUsage = details.events.size,
                    thumbnailProvider = { fisherman ->
                        val thumbnailFlow = remember(fisherman.id) {
                            viewModel.getFishermanThumbnail(fisherman.id)
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
            if (showLimitSelection) {
                LimitSelection(
                    items = sortedLimits,
                    selectedItems = limitSummaries,
                    useImperial = appSettings.display.useImperialUnits,
                    speciesThumbnailProvider = { species ->
                        val thumbnailFlow = remember(species.id) {
                            viewModel.getSpeciesThumbnail(species.id)
                        }

                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.LeapingFishWithFins,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onSelected = { selected ->
                        viewModel.addLimitToTrip(selected)
                    },
                    onUnselected = { unselected ->
                        viewModel.removeLimitFromTrip(unselected)
                    },
                    onAdd = {
                        showAddLimitDialog = true
                    },
                    onDone = { showLimitSelection = !showLimitSelection },
                    usageMap = limitUsageMap,
                    maxUsage = details.events.size
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
                        speciesToDelete = selectedSpecies
                    },
                    onAdd = {
                        showAddSpeciesDialog = true
                    },
                    onDone = { showSpeciesSelection = !showSpeciesSelection },
                    modifier = Modifier.fillMaxWidth(),
                    usageMap = speciesUsageMap,
                    maxUsage = details.events.size,
                    thumbnailProvider = { species ->
                        val thumbnailFlow = remember(species.id) {
                            viewModel.getSpeciesThumbnail(species.id)
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
                    notes = sortedNotes,
                    onDismiss = { showNotesDialog = !showNotesDialog },
                    onAddNote = { noteId, content ->
                        viewModel.addNote(noteId, content)
                    },
                    onDeleteNote = { noteId ->
                        viewModel.deleteNote(noteId)
                    }
                )
            }
            if (showEditTripDialog) {
                val eventStart = details.events.minOfOrNull { it.event.startTime }
                val eventEnd = details.events.maxOfOrNull { it.event.endTime }
                EditTripDialog(
                    item = details.trip,
                    eventStart = eventStart,
                    eventEnd = eventEnd,
                    onDismiss = { showEditTripDialog = !showEditTripDialog },
                    onConfirm = { confirmedItem ->
                        viewModel.saveTrip(confirmedItem)
                        showEditTripDialog = !showEditTripDialog
                    }
                )
            }
        }
    }

    if (showAddBodyOfWaterDialog) {
        AddBodyOfWaterDialog(
            onDismiss = { showAddBodyOfWaterDialog = !showAddBodyOfWaterDialog },
            onConfirm = { name ->
                viewModel.addTripBodyOfWater(tripId, BodyOfWater(name = name))
                showAddBodyOfWaterDialog = !showAddBodyOfWaterDialog
            }
        )
    }
    if (showAddFishermanDialog) {
        AddFishermanDialog(
            onDismiss = { showAddFishermanDialog = !showAddFishermanDialog },
            onAdd = { first, last, nick ->
                val fisherman = Fisherman(
                    firstName = first.trim(),
                    lastName = last.trim(),
                    nickname = nick.trim()
                )
                viewModel.addFisherman(fisherman) {
                    // Do nothing on Success
                }
                showAddFishermanDialog = !showAddFishermanDialog
            }
        )
    }
    if (showAddLimitDialog) {
        LimitDialog(
            useImperial = appSettings.display.useImperialUnits,
            species = allSpecies,
            speciesThumbnailProvider = { species, size ->
                val thumbnailFlow = remember(species.id) {
                    viewModel.getSpeciesThumbnail(species.id)
                }

                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.LeapingFishWithFins,
                    modifier = Modifier.size(size)
                )
            },
            onDismiss = { showAddLimitDialog = !showAddLimitDialog },
            onConfirm = { limit ->
                viewModel.addLimit(limit)
                showAddLimitDialog = !showAddLimitDialog
            }
        )
    }
    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = !showAddNoteDialog },
            onConfirm = { content ->
                viewModel.addNote(null, content)
                showAddNoteDialog = !showAddNoteDialog
            }
        )
    }
    if (showAddSkyConditionDialog) {
        AddSkyConditionDialog(
            onDismiss = { showAddSkyConditionDialog = !showAddSkyConditionDialog },
            onConfirm = { name ->
                val newSkyCondition = SkyCondition(name = name)
                viewModel.addSkyCondition(newSkyCondition) {
                    pendingSkyConditionId = newSkyCondition.id
                }
                showAddSkyConditionDialog = !showAddSkyConditionDialog
            }
        )
    }
    if (showAddSpeciesDialog) {
        AddSpeciesDialog(
            onDismiss = { showAddSpeciesDialog = !showAddSpeciesDialog },
            onConfirm = { name ->
                viewModel.addTripTargetSpecies(tripId, Species(name = name))
                showAddSpeciesDialog = !showAddSpeciesDialog
            }
        )
    }
    if (showAddWaterClarityDialog) {
        AddWaterClarityDialog(
            onDismiss = { showAddWaterClarityDialog = !showAddWaterClarityDialog },
            onConfirm = { name ->
                val newWaterClarity = WaterClarity(name = name)
                viewModel.addWaterClarity(newWaterClarity) {
                    pendingWaterClarityId = newWaterClarity.id
                }
                showAddWaterClarityDialog = !showAddWaterClarityDialog
            }
        )
    }

    // Confirmation Dialog for long pressing a Body of Water
    if (showAddBodyOfWaterToUpdate) {
        bodyOfWaterToUpdateAll?.let { bodyOfWater ->
            GeneralDialog(
                title = "Update Catches",
                message = "Do you want to update all the fish caught for this trip to use the body of water '${bodyOfWater.name}'?",
                onDismiss = { showAddBodyOfWaterToUpdate = !showAddBodyOfWaterToUpdate },
                onConfirm = {
                    scope.launch {
                        viewModel.updateBodyOfWaterForTrip(
                            newBodyOfWaterId = bodyOfWater.id,
                            tripId = tripId
                        )
                        Toast.makeText(
                            context,
                            "Catches updated to ${bodyOfWater.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        showAddBodyOfWaterToUpdate = !showAddBodyOfWaterToUpdate
                    }
                },
                confirmButtonText = "Yes",
                dismissButtonText = "No"
            )
        }
    }
}