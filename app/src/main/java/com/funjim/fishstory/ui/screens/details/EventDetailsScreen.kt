package com.funjim.fishstory.ui.screens.details

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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitScope
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBox
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
import com.funjim.fishstory.ui.cards.EventHighlightCard
import com.funjim.fishstory.ui.utils.FishFilter
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.StatusGumball
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.crud.AddBodyOfWaterDialog
import com.funjim.fishstory.ui.crud.AddFishermanDialog
import com.funjim.fishstory.ui.crud.AddNoteDialog
import com.funjim.fishstory.ui.crud.AddSkyConditionDialog
import com.funjim.fishstory.ui.crud.AddSpeciesDialog
import com.funjim.fishstory.ui.crud.AddWaterClarityDialog
import com.funjim.fishstory.ui.crud.EditEventDialog
import com.funjim.fishstory.ui.crud.EditNoteDialog
import com.funjim.fishstory.ui.crud.LimitDialog
import com.funjim.fishstory.ui.crud.NotesDialog
import com.funjim.fishstory.ui.crud.WaterDialog
import com.funjim.fishstory.ui.crud.WeatherDialog
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnSecondaryColor
import com.funjim.fishstory.ui.utils.rememberGumballStatus
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.ui.selection.BodyOfWaterSelection
import com.funjim.fishstory.ui.selection.FishermanSelection
import com.funjim.fishstory.ui.selection.SpeciesSelection
import com.funjim.fishstory.ui.dialogs.GeneralDialog
import com.funjim.fishstory.ui.selection.LimitSelection
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
    navigateToAddFish: () -> Unit,
    navigateToFishList: (FishFilter) -> Unit,
    navigateToAddLures: (TackleBox) -> Unit,
    navigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = rememberCoroutineScope().let { remember { SnackbarHostState() } }

    LaunchedEffect(eventId) {
        viewModel.selectTrip(tripId)
        viewModel.selectEvent(eventId)
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val now = System.currentTimeMillis()

    var menuExpanded by remember { mutableStateOf(false) }

    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    var bodyOfWaterToDelete by remember { mutableStateOf<BodyOfWater?>(null) }
    var showDeleteBodyOfWaterDialog by remember { mutableStateOf(false) }
    var fishermanToDelete by remember { mutableStateOf<Fisherman?>(null) }
    var showDeleteFishermanDialog by remember { mutableStateOf(false) }
    var limitToDelete by remember { mutableStateOf<Limit?>(null) }
    var showDeleteLimitDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var speciesToDelete by remember { mutableStateOf<Species?>(null) }
    var showDeleteSpeciesDialog by remember { mutableStateOf(false) }
    var waterToDelete by remember { mutableStateOf<Water?>(null) }
    var showDeleteWaterDialog by remember { mutableStateOf(false) }
    var weatherToDelete by remember { mutableStateOf<Weather?>(null) }
    var showDeleteWeatherDialog by remember { mutableStateOf(false) }

    var limitToEdit by remember { mutableStateOf<Limit?>(null) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var waterToEdit by remember { mutableStateOf<Water?>(null) }
    var weatherToEdit by remember { mutableStateOf<Weather?>(null) }

    var bodyOfWaterToUpdateAll by remember { mutableStateOf<BodyOfWater?>(null) }

    var pendingSkyConditionId by remember { mutableStateOf<String?>(null) }
    var pendingWaterClarityId by remember { mutableStateOf<String?>(null) }

    var showAddBodyOfWaterDialog by remember { mutableStateOf(false) }
    var showAddFishermanDialog by remember { mutableStateOf(false) }
    var showAddLimitDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddSpeciesDialog by remember { mutableStateOf(false) }
    var showAddSkyConditionDialog by remember { mutableStateOf(false) }
    var showAddWaterDialog by remember { mutableStateOf(false) }
    var showAddWaterClarityDialog by remember { mutableStateOf(false) }
    var showAddWeatherDialog by remember { mutableStateOf(false) }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var showDeleteEventDialog by remember { mutableStateOf(false) }

    var showNotesDialog by remember { mutableStateOf(false) }

    var showBodiesOfWaterSelection by remember { mutableStateOf(false) }
    var showFishermanSelection by remember { mutableStateOf(false) }
    var showLimitSelection by remember { mutableStateOf(false) }
    var showSpeciesSelection by remember { mutableStateOf(false) }

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

    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
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

    val fishermen by viewModel.fishermen.collectAsStateWithLifecycle(emptyList())

    val tackleBoxMap by viewModel.eventTackleBoxMap.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showPhotos by viewModel.showPhotos.collectAsStateWithLifecycle()

    var bodyOfWaterForPhoto by remember { mutableStateOf<BodyOfWater?>(null) }
    val bodyOfWaterPhotoPicker = rememberLauncherForActivityResult(
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

            val sortedBodyOfWaterList = remember(bodyOfWaterSummaries) {
                bodyOfWaterSummaries.sortedBy { it.bodyOfWater.name }
            }
            val sortedFishermen = remember(fishermen) {
                fishermen.sortedBy { it.fullName }
            }
            val sortedFishermanList = remember(fishermanSummaries) {
                fishermanSummaries.sortedBy { it.fisherman.fullName }
            }
            val sortedLimits = remember(allLimits) {
                allLimits.sortedBy { it.name }
            }
            val sortedNotes = remember(eventDetails.notes) {
                eventDetails.notes.sortedByDescending { it.timestamp }
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
                                CategoryType.WATER -> {
                                    IconButton(onClick = { showAddWaterDialog = true }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                AppIcons.Default.WaterSet,
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
                                CategoryType.WEATHER -> {
                                    IconButton(onClick = { showAddWeatherDialog = true }) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                AppIcons.Default.WeatherSet,
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

                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            menuExpanded = false
                                            showDeleteEventDialog = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Event"
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
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    horizontalAlignment = Alignment.Start
                ) {
                    val thumbnail by viewModel.eventThumbnail().collectAsState(initial = null)

                    LazyColumn(horizontalAlignment = Alignment.Start) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.CanoeEmpty,
                                    modifier = Modifier.size(64.dp),
                                    onClick = {
                                        viewModel.toggleShowPhotos()
                                    }
                                )
                                Text(
                                    text = event.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = getOnMainColor()
                                )
                            }

                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Start: ${dateTimeFormatter.format(Date(event.startTime))}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getOnSecondaryColor()
                                    )
                                    Text(
                                        text = "End: ${dateTimeFormatter.format(Date(event.endTime))}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = getOnSecondaryColor()
                                    )
                                }
                                val displayLat = event.latitude ?: trip.latitude
                                val displayLng = event.longitude ?: trip.longitude
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
                                            .alpha(if (event.latitude == null) 0.6f else 1f )
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = getOnMainColor()
                            )

                            AnimatedVisibility(visible = showPhotos) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    PhotoPickerRow(
                                        photos = eventDetails.photos,
                                        onPhotoSelected = { uri ->
                                            viewModel.addEventPhoto(
                                                eventId = eventId,
                                                uri = uri,
                                                true
                                            )
                                        },
                                        onPhotoTaken = { uri ->
                                            viewModel.addEventPhoto(
                                                eventId = eventId,
                                                uri = uri,
                                                false
                                            )
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

                                    HorizontalDivider(
                                        modifier = Modifier
                                            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                        thickness = 1.dp,
                                        color = getOnMainColor()
                                    )
                                }
                            }

                            if (eventSummary.fishCaught != 0 || now >= event.startTime) {
                                EventHighlightCard(
                                    summary = eventSummary,
                                    useImperial = appSettings.display.useImperialUnits,
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
                                                viewModel.bodyOfWaterThumbnail(bodyOfWater.id)
                                            },
                                            onAdd = { showBodiesOfWaterSelection = true },
                                            onClearThumbnail = { bodyOfWater ->
                                                viewModel.deleteBodyOfWaterThumbnail(bodyOfWater.id)
                                            },
                                            onClick = { bodyOfWater ->
                                                bodyOfWaterToUpdateAll = bodyOfWater
                                            },
                                            onDelete = { bodyOfWater ->
                                                bodyOfWaterToDelete = bodyOfWater
                                                showDeleteBodyOfWaterDialog = true
                                            },
                                            onFishClick = { bodyOfWater, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    eventId = event.id,
                                                    bodyOfWaterId = bodyOfWater.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onSetThumbnail = { bodyOfWater ->
                                                bodyOfWaterForPhoto = bodyOfWater
                                                bodyOfWaterPhotoPicker.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            onToggleHelp = { viewModel.toggleHelp() }
                                        )
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
                                                helpText = """At least one participant must be added to this event.

Logging catches will be restricted to the selected fishermen. You can assign a tackle box to each fisherman to filter available lures, or leave it unassigned to make all lures accessible.""",
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
                                                onDelete = { fisherman ->
                                                    fishermanToDelete = fisherman
                                                    showDeleteFishermanDialog = true
                                                },
                                                onFishClick = { fisherman, target ->
                                                    navigateToFishList(FishFilter(
                                                        tripId = trip.id,
                                                        eventId = event.id,
                                                        fishermanId = fisherman.id,
                                                        targetOnly = target)
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
                                                    viewModel.updateEventFisherman(
                                                        fishermanId = fisherman.id,
                                                        tackleBoxId = tackleBoxId
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
                                                |Limits set here apply only to this event.""".trimMargin(),
                                            scope = LimitScope.EVENT,
                                            modifier = Modifier.padding(
                                                vertical = 8.dp,
                                                horizontal = 16.dp
                                            ),
                                            speciesThumbnailProvider = { species ->
                                                val thumbnailFlow = remember(species.id) {
                                                    viewModel.getSpeciesThumbnail(species.id)
                                                }

                                                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                                                ThumbnailBox(
                                                    thumbnail = thumbnail,
                                                    imageVector = AppIcons.Default.LeapingFishWithFins,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            },
                                            onAdd = { showLimitSelection = true },
                                            onDelete = { limit ->
                                                limitToDelete = limit
                                                showDeleteLimitDialog = true
                                            },
                                            onEdit = { limitToEdit = it },
                                            onToggleHelp = { viewModel.toggleHelp() }                                        )
                                    }
                                    CategoryType.NOTES -> {
                                        NoteSection(
                                            noteList = sortedNotes,
                                            showHelp = appSettings.display.showHelp,
                                            emptyText = "No notes created.",
                                            helpText = "Notes help you remember specific details and conditions during this event.",
                                            onAdd = { showAddNoteDialog = true },
                                            onDelete = {
                                                noteToDelete = it
                                                showDeleteNoteDialog = true
                                            },
                                            onEdit = { noteToEdit = it },
                                            onToggleHelp = { viewModel.toggleHelp() },
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
                                                    eventId = event.id,
                                                    speciesId = species.id,
                                                    targetOnly = true)
                                                )
                                            },
                                            onDelete = { species ->
                                                speciesToDelete = species
                                                showDeleteSpeciesDialog = true
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
                                            useImperial = appSettings.display.useImperialUnits,
                                            emptyText = "No water conditions specified.",
                                            helpText = "Adding water conditions will automatically apply them to any new catches logged for this event.",
                                            onAdd = { showAddWaterDialog = true },
                                            onDelete = {
                                                waterToDelete = it
                                                showDeleteWaterDialog = true
                                            },
                                            onEdit = { waterToEdit = it },
                                            onFishClick = { water, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    eventId = event.id,
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
                                            helpText = "Adding weather conditions will automatically apply them to any new catches logged for this event.",
                                            onAdd = { showAddWeatherDialog = true },
                                            onDelete = {
                                                weatherToDelete = it
                                                showDeleteWeatherDialog = true
                                            },
                                            onEdit = { weatherToEdit = it },
                                            onFishClick = { weather, target ->
                                                navigateToFishList(FishFilter(
                                                    tripId = trip.id,
                                                    eventId = event.id,
                                                    weatherId = weather.id,
                                                    targetOnly = target)
                                                )
                                            },
                                            onThumbnailLongClick = {
                                                viewModel.toggleImperialUnits()
                                            },
                                            onToggleHelp = { viewModel.toggleHelp() }
                                        )
                                    }
                                    else -> {
                                        // DO NOTHING FOR NOW
                                    }
                                }
                            }
                        }
                    }

                    if (showDeleteEventDialog) {
                        DeleteDialog(
                            title = "Delete Event",
                            message =
                                if (eventSummary.fishCaught > 0)
                                    """There ${if (eventSummary.fishCaught == 1) "is" else "are"} currently ${eventSummary.fishCaught} fish associated with this event.
                                    |
                                    |Deleting this event will delete all fish associated with it. 
                                    |
                                    |Are you sure you want to proceed?""".trimMargin()
                                else
                                    """Are you sure you want to delete this event? 
                                    |
                                    |No fish are currently associated with this event.""".trimMargin(),
                            onConfirm = {
                                viewModel.deleteEvent()
                                showDeleteEventDialog = !showDeleteEventDialog
                                navigateBack()
                            },
                            onDismiss = { showDeleteEventDialog = !showDeleteEventDialog }
                        )
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
                }
            }

            if (showDeleteBodyOfWaterDialog) {
                bodyOfWaterToDelete?.let { bodyOfWater ->
                    val summary = bodyOfWaterSummaries.find { it.bodyOfWater.id == bodyOfWater.id }
                    summary?.fishCaught?.let { fishCaught ->
                        DeleteDialog(
                            title = "Remove Body of Water",
                            message =
                                if (fishCaught > 0)
                                    """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with this body of water for this event.
                                    |
                                    |Removing this body of water will not affect those fish.  It will only affect entry of future catches.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                                else
                                    """Are you sure you want to remove this body of water? 
                                    |
                                    |No fish are currently associated with this body of water for this event.
                                """.trimMargin(),
                            confirmButtonText = "Remove",
                            onConfirm = {
                                viewModel.removeEventBodyOfWater(eventId, bodyOfWater.id)
                                showDeleteBodyOfWaterDialog = !showDeleteBodyOfWaterDialog
                            },
                            onDismiss = { showDeleteBodyOfWaterDialog = !showDeleteBodyOfWaterDialog }
                        )
                    }
                }
            }
            if (showDeleteFishermanDialog) {
                fishermanToDelete?.let { fisherman ->
                    val summary = fishermanSummaries.find { it.fisherman.id == fisherman.id }
                    summary?.fishCaught?.let { fishCaught ->
                        DeleteDialog(
                            title = "Remove Fisherman",
                            message =
                                if (fishCaught > 0)
                                    """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with this fisherman for this event.
                                    |
                                    |Removing this fisherman will not affect those fish.  It will only affect entry of future catches.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                                else
                                    """Are you sure you want to remove this fisherman? 
                                    |
                                    |No fish are currently associated with this fisherman for this event.
                                """.trimMargin(),
                            confirmButtonText = "Remove",
                            onConfirm = {
                                viewModel.deleteFishermanFromEvent(fisherman.id)
                                showDeleteFishermanDialog = !showDeleteFishermanDialog
                            },
                            onDismiss = { showDeleteFishermanDialog = !showDeleteFishermanDialog }
                        )
                    }
                }
            }
            if (showDeleteLimitDialog) {
                limitToDelete?.let { limit ->
                    DeleteDialog(
                        title = "Remove Limit",
                        message = "Are you sure you want to remove this limit?",
                        onConfirm = {
                            viewModel.removeLimitFromEvent(limit)
                            showDeleteLimitDialog = !showDeleteLimitDialog
                        },
                        onDismiss = { showDeleteLimitDialog = !showDeleteLimitDialog }
                    )
                }
            }
            if (showDeleteNoteDialog) {
            noteToDelete?.let { note ->
                DeleteDialog(
                    title = "Delete Note",
                    message = "Are you sure you want to delete this note?",
                    onConfirm = {
                        viewModel.deleteNote(note.id)
                        showDeleteNoteDialog = !showDeleteNoteDialog
                    },
                    onDismiss = { showDeleteNoteDialog = !showDeleteNoteDialog }
                )
            }
                }
            if (showDeleteSpeciesDialog) {
                speciesToDelete?.let { species ->
                    val summary = targetSpeciesSummaries.find { it.species.id == species.id }
                    summary?.fishCaught?.let { fishCaught ->
                        DeleteDialog(
                            title = "Remove Species",
                            message =
                                if (fishCaught > 0)
                                    """There ${if (fishCaught == 1) "is" else "are"} currently $fishCaught fish associated with this species for this event.
                                    |
                                    |Removing this species will not affect those fish.  It will only affect entry of future catches.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                                else
                                    """Are you sure you want to remove this species? 
                                    |
                                    |No fish are currently associated with this species for this event.
                                """.trimMargin(),
                            confirmButtonText = "Remove",
                            onConfirm = {
                                viewModel.removeEventTargetSpecies(
                                    eventId,
                                    species.id
                                )
                                showDeleteSpeciesDialog = !showDeleteSpeciesDialog
                            },
                            onDismiss = { showDeleteSpeciesDialog = !showDeleteSpeciesDialog }
                        )
                    }
                }
            }
            if (showDeleteWaterDialog) {
                waterToDelete?.let { water ->
                    val summary = waterSummaries.find { it.water.id == water.id }
                    summary?.fishCaught?.let { fishCaught ->
                        DeleteDialog(
                            title = "Delete Water Conditions",
                            message =
                                if (fishCaught > 0)
                                    """Deleting this water condition will remove its data from $fishCaught ${if (fishCaught == 1) "fish record" else "fish records"}.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                                else
                                    """Are you sure you want to delete these water conditions? 
                                    |
                                    |No fish are currently associated with this condition.
                                """.trimMargin(),
                            onConfirm = {
                                viewModel.deleteWater(water.id)
                                showDeleteWaterDialog = !showDeleteWaterDialog
                            },
                            onDismiss = { showDeleteWaterDialog = !showDeleteWaterDialog }
                        )
                    }
                }
            }
            if (showDeleteWeatherDialog) {
                weatherToDelete?.let { item ->
                    val summary = weatherSummaries.find { it.weather.id == item.id }
                    summary?.fishCaught?.let { fishCaught ->
                        DeleteDialog(
                            title = "Delete Weather Conditions",
                            message =
                                if (fishCaught > 0)
                                    """Deleting this weather condition will remove its data from $fishCaught ${if (fishCaught == 1) "fish record" else "fish records"}.
                                    |
                                    |Are you sure you want to proceed?
                                """.trimMargin()
                                else
                                    """Are you sure you want to delete these weather conditions? 
                                    |
                                    |No fish are currently associated with this condition.
                                """.trimMargin(),
                            onConfirm = {
                                viewModel.deleteWeather(item.id)
                                showDeleteWeatherDialog = !showDeleteWeatherDialog
                            },
                            onDismiss = { showDeleteWeatherDialog = !showDeleteWeatherDialog }
                        )
                    }
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
                            viewModel.waterClarityThumbnail(clarity.id)
                        }
                        val thumbnail by thumbnailFlow.collectAsState(initial = null)

                        ThumbnailBox(
                            thumbnail = thumbnail,
                            imageVector = AppIcons.Default.WaterSet,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    onToggleImperialUnits = { viewModel.toggleImperialUnits() },
                    onDismiss = {
                        waterToEdit = null
                        pendingWaterClarityId = null
                    },
                    onConfirm = { temp, depth, clarity ->
                        viewModel.updateWater(
                            water.copy(
                                temperature = temp,
                                depth = depth,
                                clarityId = clarity?.id
                            )
                        )
                        waterToEdit = null
                        pendingWaterClarityId = null
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
                    pendingSkyCondition = pendingSkyConditionId,
                    useImperial = appSettings.display.useImperialUnits,
                    title = "Edit Weather Conditions",
                    thumbnailProvider = { skyCondition ->
                        val thumbnailFlow = remember(skyCondition.id) {
                            viewModel.skyConditionThumbnail(skyCondition.id)
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

            if (showBodiesOfWaterSelection) {
                BodyOfWaterSelection(
                    items = allBodiesOfWater,
                    selectedItems = eventDetails.bodiesOfWater,
                    onSelected = { selectedBodyOfWater ->
                        viewModel.addEventBodyOfWater(eventId, selectedBodyOfWater.id)
                    },
                    onUnselected = { selectedBodyOfWater ->
                        bodyOfWaterToDelete = selectedBodyOfWater
                        showDeleteBodyOfWaterDialog = true
                    },
                    onAdd = {
                        showAddBodyOfWaterDialog = true
                    },
                    onDone = { showBodiesOfWaterSelection = !showBodiesOfWaterSelection },
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
                        fishermanToDelete = fisherman
                        showDeleteFishermanDialog = true
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
                        viewModel.addLimitToEvent(selected)
                    },
                    onUnselected = { unselected ->
                        val limit = limitSummaries.find { it.limit.id == unselected.id }
                        if (limit != null) {
                            if (!limit.isTripLimit) {
                                viewModel.removeLimitFromEvent(unselected)
                            } else {
                                showLimitSelection = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Unable to remove (unselect) trip assigned limit.",
                                        actionLabel = "Dismiss",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        }
                    },
                    onAdd = {
                        showAddLimitDialog = true
                    },
                    onDone = { showLimitSelection = false }
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
                        speciesToDelete = selectedSpecies
                        showDeleteSpeciesDialog = true
                    },
                    onAdd = {
                        showAddSpeciesDialog = true
                    },
                    onDone = { showSpeciesSelection = false },
                    modifier = Modifier.fillMaxWidth(),
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
                    onDismiss = { showNotesDialog = false },
                    onAddNote = { noteId, content ->
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
                viewModel.addEventBodyOfWater(eventId, BodyOfWater(name = name))
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
            onDismiss = { showAddLimitDialog = false },
            onConfirm = { limit ->
                viewModel.addLimit(limit)
                showAddLimitDialog = false
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
                val newSkyCondition = SkyCondition(name = name)
                viewModel.addSkyCondition(newSkyCondition) {
                    pendingSkyConditionId = newSkyCondition.id
                }
                showAddSkyConditionDialog = false
            }
        )
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
    if (showAddWaterDialog) {
        WaterDialog(
            initialTemp = null,
            initialDepth = null,
            initialClarity = null,
            allClarity = allWaterClarity,
            pendingClarity = pendingWaterClarityId,
            useImperial = appSettings.display.useImperialUnits,
            title = "New Water Conditions",
            thumbnailProvider = { clarity ->
                val thumbnailFlow = remember(clarity.id) {
                    viewModel.waterClarityThumbnail(clarity.id)
                }
                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.WaterSet,
                    modifier = Modifier.size(48.dp)
                )
            },
            onToggleImperialUnits = { viewModel.toggleImperialUnits() },
            onDismiss = {
                showAddWaterDialog = false
                pendingWaterClarityId = null
            },
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
                pendingWaterClarityId = null
            },
            onAddWaterClarity = { showAddWaterClarityDialog = true}
        )
    }
    if (showAddWaterClarityDialog) {
        AddWaterClarityDialog(
            onDismiss = { showAddWaterClarityDialog = false },
            onConfirm = { name ->
                val newWaterClarity = WaterClarity(name = name)
                viewModel.addWaterClarity(newWaterClarity) {
                    pendingWaterClarityId = newWaterClarity.id
                }
                showAddWaterClarityDialog = false
            }
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
            pendingSkyCondition = pendingSkyConditionId,
            useImperial = appSettings.display.useImperialUnits,
            title = "New Weather Conditions",
            thumbnailProvider = { skyCondition ->
                val thumbnailFlow = remember(skyCondition.id) {
                    viewModel.skyConditionThumbnail(skyCondition.id)
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
                showAddWeatherDialog = false
                pendingSkyConditionId = null
            },
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
                pendingSkyConditionId = null
            },
            onAddSkyCondition = { showAddSkyConditionDialog = true }
        )
    }

    bodyOfWaterToUpdateAll?.let { bodyOfWater ->
        GeneralDialog(
            title = "Update Catches",
            message = "Do you want to update all the fish caught for this event to use the body of water '${bodyOfWater.name}'?",
            onDismiss = {
                bodyOfWaterToUpdateAll = null
            },
            onConfirm = {
                scope.launch {
                    viewModel.updateBodyOfWaterForEvent(
                        newBodyOfWaterId = bodyOfWater.id,
                        eventId = eventId
                    )
                    Toast.makeText(context, "Catches updated to '${bodyOfWater.name}'", Toast.LENGTH_SHORT).show()
                    bodyOfWaterToUpdateAll = null
                }
            },
            confirmButtonText = "Yes",
            dismissButtonText = "No"
        )
    }
}