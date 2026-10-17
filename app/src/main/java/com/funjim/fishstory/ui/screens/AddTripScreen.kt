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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.funjim.fishstory.ui.utils.crud.AddFishermanDialog
import com.funjim.fishstory.ui.utils.crud.AddSpeciesDialog
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.rememberLocationPickerState
import com.funjim.fishstory.ui.utils.selection.BodyOfWaterSelection
import com.funjim.fishstory.ui.utils.selection.FishermanSelection
import com.funjim.fishstory.ui.utils.selection.SpeciesSelection
import com.funjim.fishstory.viewmodels.AddTripViewModel
import kotlinx.coroutines.launch
import kotlin.collections.minus
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
import com.funjim.fishstory.ui.utils.StatusGumball
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor

// ---------------------------------------------------------------------------
// AddTripScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    viewModel: AddTripViewModel,
    navigateToAddLures: (TackleBox) -> Unit,
    navigateToAddEvent: ((tripId: String) -> Unit),
    navigateBack: () -> Unit
) {
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val tripDraft by viewModel.tripDraft.collectAsStateWithLifecycle()

    var showBodyOfWaterSelection by remember { mutableStateOf(false) }
    val allBodiesOfWater by viewModel.allBodiesOfWater.collectAsStateWithLifecycle()
    val bodiesOfWater by viewModel.tripBodiesOfWater.collectAsStateWithLifecycle()
    var addNewBodyOfWater by remember { mutableStateOf(false) }

    var showSpeciesSelection by remember { mutableStateOf(false) }
    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    val targetSpecies by viewModel.tripTargetSpecies.collectAsStateWithLifecycle()
    var addNewSpecies by remember { mutableStateOf(false) }

    var showFishermanSelection by remember { mutableStateOf(false) }
    val tripFishermen by viewModel.tripFishermen.collectAsStateWithLifecycle()
    val allFishermen by viewModel.allFishermen.collectAsStateWithLifecycle()
    var addNewFisherman by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val tripTackleBoxMap by viewModel.tripTackleBoxMap.collectAsState()

    var menuExpanded by remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }

    // Location pickers
    val deviceLocation by viewModel.deviceLocation.collectAsStateWithLifecycle()

    val locationPicker = rememberLocationPickerState(
        deviceLocation = deviceLocation?.let { it.latitude to it.longitude },
        existingLat = tripDraft.latitude,
        existingLng = tripDraft.longitude,
        onFetchLocation = {
            scope.launch { viewModel.fetchDeviceLocationOnce() }
        },
        onLocationConfirmed = { lat, lng ->
            viewModel.updateTripDraft { it.copy(latitude = lat, longitude = lng) }
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
        tripFishermen,
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
                count = tripFishermen.size
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("New Trip")
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
                        onClick = { showSaveDialog = true }, // Intercept save to present choices
                        enabled = tripDraft.name.isNotBlank() && tripFishermen.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = AppIcons.Default.Save,
                            contentDescription = "Save Trip",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    val hasLocation = tripDraft.latitude != null
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
                                                viewModel.updateTripDraft {
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
                                        viewModel.updateTripDraft {
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
                        enabled = tripDraft.name.isNotBlank() && tripFishermen.isNotEmpty()
                    ) {
                        Text("Add Trip")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Trip Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    val displayLat = tripDraft.latitude
                    val displayLng = tripDraft.longitude
                    val hasLocation = displayLat != null && displayLng != null

                    if (hasLocation) {
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
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = tripDraft.name,
                    onValueChange = { name ->
                        viewModel.updateTripDraft { tripDraft.copy(name = name.trimStart()) }
                    },
                    label = { Text("Trip Name") },
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
                        millis = tripDraft.startDate,
                        modifier = Modifier.weight(1f)
                    ) { new ->
                        viewModel.updateTripDraft {
                            tripDraft.copy(startDate = new)
                        }
                        if (new > tripDraft.endDate) {
                            viewModel.updateTripDraft {
                                tripDraft.copy(endDate = new)
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
                        millis = tripDraft.endDate,
                        modifier = Modifier.weight(1f)
                    ) { new ->
                        when {
                            new < tripDraft.startDate ->
                                Toast.makeText(
                                    context,
                                    "End must be after start",
                                    Toast.LENGTH_SHORT
                                ).show()
                            else ->
                                viewModel.updateTripDraft {
                                    tripDraft.copy(endDate = new)
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
                                    viewModel.updateTripBodiesOfWater(bodiesOfWater - bodyOfWater)
                                }
                            )
                        }

                        CategoryType.FISHERMEN -> {
                            val fishermanSummaries: List<FishermanSummary> = tripFishermen.map { fisherman ->
                                FishermanSummary(fisherman = fisherman)
                            }.sortedBy { it.fisherman.fullName }

                            Column() {
                                FishermanSummaries(
                                    list = fishermanSummaries,
                                    verbose = false,
                                    description = """No fishermen selected. 

Fishermen are the people actively participating on this trip, and there must be at least one selected. 

After selecting fishermen, you can assign a tackle box and add lures.

When logging catches, options will be restricted to the fishermen selected here.""",
                                    thumbnailFlow = { fisherman ->
                                        viewModel.getFishermanThumbnail(fisherman.id)
                                    },
                                    photosFlow = { fisherman ->
                                        viewModel.getFishermanPhotos(fisherman.id)
                                    },
                                    tackleBoxSelections = tripTackleBoxMap,
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
                                            viewModel.updateTripTackleBoxMap(
                                                tripTackleBoxMap + (tackleBox.fishermanId to tackleBox.id)
                                            )
                                        }
                                    },
                                    onAddLuresToTackleBox = { tackleBox ->
                                        navigateToAddLures(tackleBox)
                                    },
                                    onTackleBoxSelected = { fisherman, tackleBoxId ->
                                        viewModel.updateTripTackleBoxMap(
                                            tripTackleBoxMap + (fisherman.id to tackleBoxId)
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
                                            viewModel.updateTripFishermen(tripFishermen - fisherman)
                                            viewModel.updateTripTackleBoxMap(tripTackleBoxMap - fisherman.id)
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
                                    viewModel.updateTripTargetSpecies(targetSpecies - species)
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
                    viewModel.updateTripBodiesOfWater(bodiesOfWater + selected)
                },
                onUnselected = { unselected ->
                    viewModel.updateTripBodiesOfWater(bodiesOfWater - unselected)
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
                selectedItems = tripFishermen,
                onSelected = { selected->
                    viewModel.updateTripFishermen(tripFishermen + selected)
                },
                onUnselected = { unselected ->
                    viewModel.updateTripFishermen(tripFishermen - unselected)
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
                onSelected = { selected ->
                    viewModel.updateTripTargetSpecies(targetSpecies + selected)
                },
                onUnselected = { unselected ->
                    viewModel.updateTripTargetSpecies(targetSpecies - unselected)
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

    if (addNewBodyOfWater) {
        AddSpeciesDialog(
            onDismiss = { addNewBodyOfWater = false },
            onConfirm = { name ->
                viewModel.updateTripBodiesOfWater(BodyOfWater(name = name))
                addNewBodyOfWater = false
            }
        )
    }
    if (addNewFisherman) {
        AddFishermanDialog(
            onDismiss = { addNewFisherman = false },
            onAdd = { firstName, lastName, nickname ->
                viewModel.updateTripFishermen(Fisherman(
                    firstName = firstName,
                    lastName = lastName,
                    nickname = nickname))
                addNewFisherman = false
            }
        )
    }
    if (addNewSpecies) {
        AddSpeciesDialog(
            onDismiss = { addNewSpecies = false },
            onConfirm = { name ->
                viewModel.updateTripTargetSpecies(Species(name = name))
                addNewSpecies = false
            }
        )
    }

    if (showSaveDialog) {
        // Track the currently selected option (0 = No events, 1 = Full trip event, 2 = Manual events)
        var selectedOption by remember { mutableIntStateOf(0) }

        val options = listOf(
            "I do not want to add events at this time for this trip.",
            "I want to add a single event spanning this entire trip.",
            "I want to manually add events for this trip."
        )

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Trip",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = """The trip will be saved.
                            |
                            |Events mark the times you are actively fishing, allowing caught fish to be logged and linked to specific fishing sessions.
                            |
                            |Please select how you would like to handle events for this trip.""".trimMargin(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    options.forEachIndexed { index, optionText ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = index }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (selectedOption == index),
                                onClick = { selectedOption = index }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        when (selectedOption) {
                            0 -> {
                                viewModel.saveTrip()
                                navigateBack()
                            }
                            1 -> {
                                viewModel.saveTripWithEvent()
                                navigateBack()
                            }
                            2 -> {
                                val tripId = tripDraft.id
                                viewModel.saveTrip()
                                navigateBack()
                                navigateToAddEvent(tripId)
                            }
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
}