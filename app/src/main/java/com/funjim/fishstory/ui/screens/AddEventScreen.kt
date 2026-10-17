package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.DateTimePickerButton
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.crud.AddFishermanDialog
import com.funjim.fishstory.ui.crud.AddSpeciesDialog
import com.funjim.fishstory.ui.crud.AddTackleBoxDialog
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnVariantColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.ui.selection.BodyOfWaterSelection
import com.funjim.fishstory.ui.selection.FishermanSelection
import com.funjim.fishstory.ui.selection.SpeciesSelection
import com.funjim.fishstory.ui.selection.TackleBoxBottomSheet
import com.funjim.fishstory.viewmodels.AddEventUiState
import com.funjim.fishstory.viewmodels.AddEventViewModel
import kotlinx.coroutines.launch
import kotlin.collections.plus
import androidx.core.net.toUri
import com.funjim.fishstory.model.BodyOfWaterSummary
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.utils.BodyOfWaterSummaries
import com.funjim.fishstory.ui.utils.CategoryChipConfig
import com.funjim.fishstory.ui.utils.CategoryRow
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.FishermanSummaries
import com.funjim.fishstory.ui.utils.SpeciesSummaries
import kotlin.collections.minus

// ---------------------------------------------------------------------------
// AddEventScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    viewModel: AddEventViewModel,
    tripId: String,
    navigateToAddLures: (TackleBox) -> Unit,
    navigateBack: () -> Unit
) {
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val uiState by viewModel.uiAddEventState.collectAsStateWithLifecycle()

    val eventDraft by viewModel.eventDraft.collectAsStateWithLifecycle()

    var showBodyOfWaterSelection by remember { mutableStateOf(false) }
    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
    val bodiesOfWater by viewModel.eventBodiesOfWater.collectAsStateWithLifecycle()
    var addNewBodyOfWater by remember { mutableStateOf(false) }

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    val targetSpecies by viewModel.eventTargetSpecies.collectAsStateWithLifecycle()
    var addNewSpecies by remember { mutableStateOf(false) }

    var showFishermanSelection by remember { mutableStateOf(false) }
    val eventFishermen by viewModel.eventFishermen.collectAsStateWithLifecycle()
    val allFishermen by viewModel.allFishermen.collectAsStateWithLifecycle()
    var addNewFisherman by remember { mutableStateOf(false) }

    var showTackleBoxSelection by remember { mutableStateOf(false) }
    var addNewTackleBox by remember { mutableStateOf(false) }
    var selectedFisherman by remember { mutableStateOf<Fisherman?>(null) }

    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val eventTackleBoxMap by viewModel.eventTackleBoxMap.collectAsState()

    var isDraftInitialized by rememberSaveable { mutableStateOf(false) }

    var menuExpanded by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = eventDraft.latitude,
        existingLng = eventDraft.longitude,
        onFetchLocation = {
            scope.launch { viewModel.fetchDeviceLocationOnce() }
        },
        onLocationConfirmed = { lat, lng ->
            viewModel.updateEventDraft { it.copy(latitude = lat, longitude = lng) }
        }
    )

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

    fun cancelAndExit() {
        navigateBack()
    }

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categoryConfigs = remember(
        allBodiesOfWater,
        bodiesOfWater,
        allFishermen,
        eventFishermen,
        allSpecies,
        targetSpecies
    ) {
        listOf(
            CategoryChipConfig(
                category = CategoryType.BODIES_OF_WATER,
                icon = { Icon(
                    AppIcons.Default.BodyOfWater,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                ) },
                count = bodiesOfWater.size
            ),
            CategoryChipConfig(
                category = CategoryType.FISHERMEN,
                icon = { Icon(
                    AppIcons.Default.Fisherman,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                ) },
                count = eventFishermen.size
            ),
            CategoryChipConfig(
                category = CategoryType.TARGET_SPECIES,
                icon = { Icon(
                    AppIcons.Default.TargetFish,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                ) },
                count = targetSpecies.size
            )
        )
    }

    when (val state = uiState) {
        is AddEventUiState.Loading -> {
            // Keeps the screen entirely blank or showing a spinner
            // until all 3 database points arrive
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is AddEventUiState.Success -> {
            val trip = state.trip

            if (!isDraftInitialized) {
                viewModel.updateEventDraft {
                    it.copy(
                        name = "",
                        tripId = trip.trip.id,
                        startTime = trip.trip.startDate,
                        endTime = trip.trip.endDate,
                        latitude = null,
                        longitude = null
                    )
                }

                viewModel.updateEventFishermen(trip.fishermen)
                viewModel.updateEventBodiesOfWater(trip.bodiesOfWater)
                viewModel.updateEventTackleBoxMap(state.tripTackleBoxMap)
                viewModel.updateEventTargetSpecies(trip.targetSpecies)

                isDraftInitialized = true
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("New Event")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                cancelAndExit()
                            }) {
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
                                else -> {}
                            }
                            IconButton(
                                onClick = {
                                    showSaveDialog = true
                                },
                                enabled = eventDraft.name.isNotBlank() && eventFishermen.isNotEmpty()
                            ) {
                                Icon(
                                    imageVector = AppIcons.Default.Save,
                                    contentDescription = "Save Event",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            val hasLocation = eventDraft.latitude != null
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }) {
                                    if (hasLocationPermission) {
                                        DropdownMenuItem(
                                            text = { Text("Use Current Location") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.MyLocation,
                                                    null
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    viewModel.fetchLocation()?.let { loc ->
                                                        viewModel.updateEventDraft {
                                                            it.copy(
                                                                latitude = loc.latitude,
                                                                longitude = loc.longitude
                                                            )
                                                        }
                                                    } ?: Toast.makeText(
                                                        context,
                                                        "Could not get location",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Select on Map") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Map, null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            locationPicker.openPicker()
                                        }
                                    )
                                    if (hasLocation) {
                                        DropdownMenuItem(
                                            text = { Text("Clear Location") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.LocationOff,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                viewModel.updateEventDraft {
                                                    it.copy(
                                                        latitude = null,
                                                        longitude = null
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showSaveDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = eventDraft.name.isNotBlank() && eventFishermen.isNotEmpty()
                            ) {
                                Text("Add Event")
                            }
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Event Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            val displayLat = eventDraft.latitude ?: trip.trip.latitude
                            val displayLng = eventDraft.longitude ?: trip.trip.longitude
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
                                                "https://www.google.com/maps/search/?api=1&query=${displayLat},${displayLng}".toUri()
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
                                        .alpha(if (eventDraft.latitude == null) 0.6f else 1f)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "An event is a single fishing session — e.g. morning run, afternoon drift.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = getOnVariantColor()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = eventDraft.name,
                            onValueChange = { name ->
                                viewModel.updateEventDraft { eventDraft.copy(name = name.trimStart()) }
                            },
                            label = { Text("Event Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Start",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.width(48.dp)
                            )
                            DateTimePickerButton(
                                label = "start",
                                millis = eventDraft.startTime,
                                modifier = Modifier.weight(1f)
                            ) { new ->
                                when {
                                    new < trip.trip.startDate ->
                                        Toast.makeText(
                                            context,
                                            "Cannot be before trip start",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    new > trip.trip.endDate ->
                                        Toast.makeText(
                                            context,
                                            "Cannot be after trip end",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    else -> {
                                        viewModel.updateEventDraft {
                                            eventDraft.copy(startTime = new)
                                        }
                                        if (new > eventDraft.endTime) {
                                            viewModel.updateEventDraft {
                                                eventDraft.copy(endTime = new)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "End",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.width(48.dp)
                            )
                            DateTimePickerButton(
                                label = "end",
                                millis = eventDraft.endTime,
                                modifier = Modifier.weight(1f)
                            ) { new ->
                                when {
                                    new < eventDraft.startTime ->
                                        Toast.makeText(
                                            context,
                                            "End must be after start",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    new > trip.trip.endDate ->
                                        Toast.makeText(
                                            context,
                                            "Cannot be after trip end",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    else ->
                                        viewModel.updateEventDraft {
                                            eventDraft.copy(endTime = new)
                                        }
                                }
                            }
                        }
                    }

                    item { HorizontalDivider() }

                    item {
                        CategoryRow(
                            categories = categoryConfigs,
                            selectedCategory = selectedCategory,
                            onCategorySelected = viewModel::onCategorySelected,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        )
                    }

                    item { HorizontalDivider() }

                    item {
                        Crossfade(
                            targetState = selectedCategory,
                            label = "CategoryTransition",
                            modifier = Modifier.fillMaxWidth()
                        ) { category ->
                            when (category) {
                                CategoryType.BODIES_OF_WATER -> {
                                    val bowSummaries: List<BodyOfWaterSummary> = bodiesOfWater.map { bow ->
                                        BodyOfWaterSummary(bodyOfWater = bow)
                                    }.sortedBy { it.bodyOfWater.name.lowercase() }

                                    BodyOfWaterSummaries(
                                        list = bowSummaries,
                                        description = """No bodies of water selected.

Bodies of water are where you're actively fishing. 

Selecting them places those bodies of water at the top of the list when logging catches for quicker entry.""",
                                        verbose = false,
                                        thumbnailFlow = { bodyOfWater ->
                                            viewModel.getBodyOfWaterThumbnail(bodyOfWater.id)
                                        },
                                        onAdd = { showBodyOfWaterSelection = true },
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
                                            viewModel.updateEventBodiesOfWater(bodiesOfWater - bodyOfWater)
                                        }
                                    )
                                }

                                CategoryType.FISHERMEN -> {
                                    val fishermanSummaries: List<FishermanSummary> = eventFishermen.map { fisherman ->
                                        FishermanSummary(fisherman = fisherman)
                                    }.sortedBy { it.fisherman.fullName }

                                    Column() {
                                        FishermanSummaries(
                                            list = fishermanSummaries,
                                            verbose = false,
                                            description = """No fishermen selected. 

Fishermen are the people actively participating on this event, and there must be at least one selected. 

After selecting fishermen, you can assign a tackle box and add lures.

When logging catches, options will be restricted to the fishermen selected here.""",
                                            thumbnailFlow = { fisherman ->
                                                viewModel.getFishermanThumbnail(fisherman.id)
                                            },
                                            photosFlow = { fisherman ->
                                                viewModel.getFishermanPhotos(fisherman.id)
                                            },
                                            tackleBoxSelections = eventTackleBoxMap,
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
                                                viewModel.createTackleBox(tackleBox) { tackleBox ->
                                                    viewModel.updateEventTackleBoxMap(
                                                        eventTackleBoxMap + (tackleBox.fishermanId to tackleBox.id)
                                                    )
                                                }
                                            },
                                            onAddLuresToTackleBox = { tackleBox ->
                                                navigateToAddLures(tackleBox)
                                            },
                                            onTackleBoxSelected = { fisherman, tackleBoxId ->
                                                viewModel.updateEventTackleBoxMap(
                                                    eventTackleBoxMap + (fisherman.id to tackleBoxId)
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
                                            onDelete =
                                                if (fishermanSummaries.size > 1) { fisherman ->
                                                    viewModel.updateEventFishermen(eventFishermen - fisherman)
                                                    viewModel.updateEventTackleBoxMap(eventTackleBoxMap - fisherman.id)
                                                }
                                                else null
                                        )
                                    }
                                }

                                CategoryType.TARGET_SPECIES -> {
                                    val speciesSummaries: List<SpeciesSummary> = targetSpecies.map { species ->
                                        SpeciesSummary(species = species)
                                    }.sortedBy { it.species.name.lowercase() }

                                    SpeciesSummaries(
                                        items = speciesSummaries,
                                        verbose = false,
                                        description = """No target species selected.

Target species are what you're actively fishing for. 

Selecting them places those species at the top of the list when logging catches for quicker entry.""",
                                        thumbnailFlow = { species ->
                                            viewModel.getSpeciesThumbnail(species.id)
                                        },
                                        onAdd = { showSpeciesSelection = true },
                                        onClick = { /* Do nothing */ },
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
                                            viewModel.updateEventTargetSpecies(targetSpecies - species)
                                        }
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }

                if (showBodyOfWaterSelection) {
                    BodyOfWaterSelection(
                        items = allBodiesOfWater,
                        selectedItems = bodiesOfWater,
                        onSelected = { selected ->
                            viewModel.updateEventBodiesOfWater(bodiesOfWater + selected)
                        },
                        onUnselected = { unselected ->
                            viewModel.updateEventBodiesOfWater(bodiesOfWater - unselected)
                        },
                        onAdd = {
                            addNewBodyOfWater = true
                        },
                        onDone = { showBodyOfWaterSelection = false },
                        modifier = Modifier.fillMaxWidth(),
                        thumbnailProvider = { item ->
                            val thumbnailFlow = remember(item.id) {
                                viewModel.getBodyOfWaterThumbnail(item.id)
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
                    val sorted = allFishermen.sortedBy { it.fullName }
                    FishermanSelection(
                        items = sorted,
                        selectedItems = eventFishermen,
                        onSelected = { selected->
                            viewModel.updateEventFishermen(eventFishermen + selected)
                        },
                        onUnselected = { unselected ->
                            viewModel.updateEventFishermen(eventFishermen - unselected)
                        },
                        onAdd = {
                            addNewFisherman = true
                        },
                        onDone = { showFishermanSelection = false },
                        modifier = Modifier.fillMaxWidth(),
                        thumbnailProvider = { item ->
                            val thumbnailFlow = remember(item.id) {
                                viewModel.getFishermanThumbnail(item.id)
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
                        selectedItems = targetSpecies,
                        onSelected = { selectedSpecies ->
                            viewModel.updateEventTargetSpecies(targetSpecies + selectedSpecies)
                        },
                        onUnselected = { selectedSpecies ->
                            viewModel.updateEventTargetSpecies(targetSpecies - selectedSpecies)
                        },
                        onAdd = {
                            addNewSpecies = true
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
            }
        }
    }

    if (addNewBodyOfWater) {
        AddSpeciesDialog(
            onDismiss = { addNewBodyOfWater = false },
            onConfirm = { name ->
                viewModel.updateEventBodiesOfWater(BodyOfWater(name = name))
                addNewBodyOfWater = false
            }
        )
    }
    if (addNewFisherman) {
        AddFishermanDialog(
            onDismiss = { addNewFisherman = false },
            onAdd = { firstName, lastName, nickname ->
                viewModel.updateEventFishermen(Fisherman(firstName = firstName, lastName = lastName, nickname = nickname))
                addNewFisherman = false
            }
        )
    }
    if (addNewSpecies) {
        AddSpeciesDialog(
            onDismiss = { addNewSpecies = false },
            onConfirm = { name ->
                viewModel.updateEventTargetSpecies(Species(name = name))
                addNewSpecies = false
            }
        )
    }

    if (showSaveDialog) {
        // Track whether the user wants to add another event after saving
        var addAnotherEvent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Event",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The event will be saved.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { addAnotherEvent = !addAnotherEvent }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = addAnotherEvent,
                            onCheckedChange = { addAnotherEvent = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add another event after saving",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        viewModel.saveEvent()
                        if (!addAnotherEvent) {
                            navigateBack()
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedFisherman?.let { selectedFisherman ->
        val availableBoxes = viewModel.getTackleBoxesForFisherman(selectedFisherman.id)
            .collectAsState(initial = emptyList()).value

        if (addNewTackleBox) {
            AddTackleBoxDialog(
                fishermanId = selectedFisherman.id,
                onDismiss = { addNewTackleBox = false },
                onConfirm = { tackleBox ->
                    viewModel.createTackleBox(tackleBox) { createdBox ->
                        viewModel.updateEventTackleBoxMap(
                            eventTackleBoxMap + (selectedFisherman.id to createdBox.id)
                        )
                    }
                    addNewTackleBox = false
                }
            )
        }

        TackleBoxBottomSheet(
            showSheet = showTackleBoxSelection,
            items = availableBoxes,
            selectedItem = availableBoxes.find {
                it.id == eventTackleBoxMap[selectedFisherman.id]
            },
            onSelected = { tackleBox ->
                viewModel.updateEventTackleBoxMap(
                    eventTackleBoxMap + (selectedFisherman.id to tackleBox.id)
                )
            },
            onAdd = { addNewTackleBox = true },
            onClear = {
                viewModel.updateEventTackleBoxMap(eventTackleBoxMap - selectedFisherman.id)
            },
            onDismissRequest = { showTackleBoxSelection = false }
        )
    }
}