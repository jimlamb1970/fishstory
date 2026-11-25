package com.funjim.fishstory.ui.screens.details

import com.funjim.fishstory.ui.dialogs.DeleteDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share // Added for fallback if needed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishSettings
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.ui.sections.NoteSection
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CategoryChipConfig
import com.funjim.fishstory.ui.utils.CategoryRow
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.LureColorComposition
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.ReleasedChip
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.cards.WaterCard
import com.funjim.fishstory.ui.cards.WeatherCard
import com.funjim.fishstory.ui.crud.NewNoteDialog
import com.funjim.fishstory.ui.crud.EditNoteDialog
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.toLengthDisplayString
import com.funjim.fishstory.viewmodels.FishDetailsViewModel
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishNote
import com.funjim.fishstory.model.FishPhoto
import com.funjim.fishstory.ui.utils.BadgeCount
import kotlinx.coroutines.launch

// Helper function to invoke the native share sheet
fun shareContent(context: Context, textToShare: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share your catch via:")
    context.startActivity(shareIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishDetailScreen(
    viewModel: FishDetailsViewModel,
    initialFishId: String,
    onEditFish: (tripId: String, eventId: String, fishId: String) -> Unit,
    navigateBack: () -> Unit
) {
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val fishList by viewModel.fishForScope.collectAsStateWithLifecycle()

    // 1. Set up PagerState instead of custom float dragOffset state
    val pagerState = rememberPagerState(pageCount = { fishList.size })
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    // Initialize pager position to the matching fish ID once list loads
    LaunchedEffect(fishList) {
        if (fishList.isNotEmpty() && !isInitialized) {
            val foundIndex = fishList.indexOfFirst { it.fish.id == initialFishId }
            if (foundIndex != -1) {
                pagerState.scrollToPage(foundIndex)
            }
            isInitialized = true
        }
    }

    val currentIndex = pagerState.currentPage
    val fish = if (fishList.isNotEmpty() && currentIndex in fishList.indices) {
        fishList[currentIndex]
    } else null

    var fishToDelete by remember { mutableStateOf<Fish?>(null) }
    var showDeleteFishDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (fish != null) {
                            Text(
                                text = fish.species.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (fishList.size > 1) {
                                BadgeCount(
                                    count = fishList.size,
                                    index = currentIndex + 1,
                                    badgeColor = MaterialTheme.colorScheme.onPrimary,
                                    textColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(
                                text = "Fish Details",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                },
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
                    if (fish != null) {
                        Box(modifier = Modifier.size(32.dp)) {
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
                                        onEditFish(fish.trip.id, fish.event.id, fish.fish.id)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit Fish") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        menuExpanded = false
                                        fishToDelete = fish.fish
                                        showDeleteFishDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Fish",
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
        if (fishList.isEmpty() || fish == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No fish to display.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 2. Replace manual pointerInput Box with HorizontalPager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val currentFish = fishList[pageIndex]

                // Retrieve flows for the current page item
                val fishPhotos = remember(currentFish.fish.id) { viewModel.fishPhotos(currentFish.fish.id) }
                val fishNotes = remember(currentFish.fish.id) { viewModel.fishNotes(currentFish.fish.id) }
                val baitThumbnailFlow = remember(currentFish.fish.baitId) { viewModel.baitThumbnail(currentFish.fish.baitId ?: "") }
                val bodyOfWaterThumbnailFlow = remember(currentFish.fish.bodyOfWaterId) { viewModel.bodyOfWaterThumbnail(currentFish.fish.bodyOfWaterId ?: "") }
                val eventThumbnailFlow = remember(currentFish.fish.eventId) { viewModel.eventThumbnail(currentFish.fish.eventId) }
                val fishThumbnailFlow = remember(currentFish.fish.id) { viewModel.fishThumbnail(currentFish.fish.id) }
                val fishermanThumbnailFlow = remember(currentFish.fish.fishermanId) { viewModel.fishermanThumbnail(currentFish.fish.fishermanId) }
                val lureThumbnailFlow = remember(currentFish.fish.lureId) { viewModel.lureThumbnail(currentFish.fish.lureId ?: "") }
                val speciesThumbnailFlow = remember(currentFish.species.id) { viewModel.speciesThumbnail(currentFish.species.id) }
                val tripThumbnailFlow = remember(currentFish.fish.tripId) { viewModel.tripThumbnail(currentFish.fish.tripId) }

                FishDetailContent(
                    fish = currentFish,
                    fishThumbnailFlow = if (currentFish.photoCount == 0) speciesThumbnailFlow else fishThumbnailFlow,
                    fishNoteFlow = fishNotes,
                    fishPhotoFlow = fishPhotos,
                    baitThumbnailFlow = baitThumbnailFlow,
                    bodyOfWaterThumbnailFlow = bodyOfWaterThumbnailFlow,
                    eventThumbnailFlow = eventThumbnailFlow,
                    fishermanThumbnailFlow = fishermanThumbnailFlow,
                    lureThumbnailFlow = lureThumbnailFlow,
                    tripThumbnailFlow = tripThumbnailFlow,
                    showHelp = appSettings.display.showHelp,
                    useImperial = appSettings.display.useImperialUnits,
                    fishSettings = appSettings.fish,
                    onAddNote = { noteId, content -> viewModel.addNote(currentFish.fish.id, noteId, content) },
                    onDeleteNote = { noteId -> viewModel.deleteNote(noteId) },
                    onPhotoSelected = { uri -> viewModel.addFishPhoto(currentFish.fish.id, uri) },
                    onPhotoTaken = { uri -> viewModel.addFishPhoto(currentFish.fish.id, uri) },
                    onPhotoDeleted = { photo -> viewModel.deleteFishPhoto(photo) },
                    onSetThumbnail = { photo -> viewModel.setFishThumbnail(photo) },
                    onToggleHelp = { viewModel.toggleHelp() },
                    onToggleImperialUnits = { viewModel.toggleImperialUnits() },
                    onToggleShowHole = { viewModel.toggleShowHole() }
                )
            }

            // Optional Bottom Navigation Controls Bar
            if (fishList.size > 1) {
                val coroutineScope = rememberCoroutineScope()
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous fish")
                    }

                    Text(
                        text = "Swipe to navigate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = {
                            if (pagerState.currentPage < fishList.size - 1) {
                                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            }
                        },
                        enabled = pagerState.currentPage < fishList.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next fish")
                    }
                }
            }
        }
    }

    if (showDeleteFishDialog) {
        fishToDelete?.let { fishToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteFishDialog = !showDeleteFishDialog },
                title = { Text("Delete Fish?") },
                text = { Text("Are you sure you want to delete this fish?\n\nThis cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteFish(fishToDelete)
                            showDeleteFishDialog = !showDeleteFishDialog
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteFishDialog = !showDeleteFishDialog }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
@Composable
private fun FishDetailContent(
    fish: FishWithDetails,
    fishThumbnailFlow: Flow<ByteArray?>,
    fishNoteFlow: Flow<List<FishNote>>,
    fishPhotoFlow: Flow<List<FishPhoto>>,
    baitThumbnailFlow: Flow<ByteArray?>,
    bodyOfWaterThumbnailFlow: Flow<ByteArray?>,
    eventThumbnailFlow: Flow<ByteArray?>,
    fishermanThumbnailFlow: Flow<ByteArray?>,
    lureThumbnailFlow: Flow<ByteArray?>,
    tripThumbnailFlow: Flow<ByteArray?>,
    showHelp: Boolean,
    useImperial: Boolean,
    fishSettings: FishSettings,
    onAddNote: (noteId: String?, text: String) -> Unit,
    onDeleteNote: (noteId: String) -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onPhotoDeleted: (FishPhoto) -> Unit,
    onSetThumbnail: (FishPhoto) -> Unit,
    onToggleImperialUnits: (() -> Unit)? = null,
    onToggleHelp: (() -> Unit)? = null,
    onToggleShowHole: (() -> Unit)? = null
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val fishNotes by fishNoteFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val fishPhotos by fishPhotoFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val sortedNotes = remember(fishNotes) {
        fishNotes.sortedByDescending { it.timestamp }
    }

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<FishNote?>(null) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<FishNote?>(null) }
    var showDeleteNoteDialog by remember { mutableStateOf(false) }

    val baitThumbnail by baitThumbnailFlow.collectAsState(initial = null)
    val bodyOfWaterThumbnail by bodyOfWaterThumbnailFlow.collectAsState(initial = null)
    val fishThumbnail by fishThumbnailFlow.collectAsState(initial = null)
    val tripThumbnail by tripThumbnailFlow.collectAsState(initial = null)
    val eventThumbnail by eventThumbnailFlow.collectAsState(initial = null)
    val fishermanThumbnail by fishermanThumbnailFlow.collectAsState(initial = null)
    val lureThumbnail by lureThumbnailFlow.collectAsState(initial = null)

    val context = LocalContext.current

    val trip = fish.trip
    val event = fish.event

    val fishLat = fish.fish.latitude

    // Precedence logic: Use Event if it exists, otherwise use Trip
    val activeLat = fish.fish.latitude ?: event.latitude ?: trip.latitude
    val activeLng = fish.fish.longitude ?: event.longitude ?: trip.longitude

    var selectedCategory by remember { mutableStateOf(CategoryType.NOTES) }

    val categoryConfigs = remember(
        fishNotes,
        fish.water,
        fish.weather
    ) {
        buildList {
            add(
                CategoryChipConfig(
                    category = CategoryType.NOTES,
                    icon = { Icon(
                        AppIcons.Default.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    ) },
                    count = fishNotes.size
                )
            )

            if (fish.water != null) {
                add (
                    CategoryChipConfig(
                        category = CategoryType.WATER,
                        icon = { Icon(
                            AppIcons.Default.WaterSet,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        ) },
                        count = 1
                    )
                )
            }

            if (fish.weather != null) {
                add (
                    CategoryChipConfig(
                        category = CategoryType.WEATHER,
                        icon = {
                            Icon(
                                AppIcons.Default.WeatherSet,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        count = 1
                    )
                )
            }
        }
    }

    LaunchedEffect(categoryConfigs) {
        if (categoryConfigs.none { it.category == selectedCategory }) {
            categoryConfigs.firstOrNull()?.let {
                selectedCategory = it.category
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Placed the Chip and Share button on the same horizontal line
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReleasedChip(fish.fish.keptCount == 0)

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    val lengthStr = fish.fish.length?.toLengthDisplayString(useImperial = useImperial, useFractions = true) ?: ""
                    val bodyOfWater = fish.bodyOfWater?.name ?: ""
                    val dateStr = dateFormatter.format(Date(fish.fish.timestamp))
                    val timeStr = timeFormatter.format(Date(fish.fish.timestamp))

                    val shareMessage = buildString {
                        append("${fish.fisherman.fullName} caught a ")
                        if (lengthStr.isNotEmpty()) append(" $lengthStr ")
                        append(fish.species.name)
                        append(" at $timeStr on $dateStr")
                        if (bodyOfWater.isNotEmpty()) append(" on $bodyOfWater")
                    }

                    shareContent(context, shareMessage)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Catch",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            thickness = 1.dp,
            color = getOnMainColor()
        )

        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(.25f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailBox(
                        thumbnail = fishThumbnail,
                        imageVector = AppIcons.Default.LeapingFishWithFins,
                        modifier = Modifier.size(64.dp),
                        onLongClick = onToggleShowHole
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(.75f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fish.species.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (activeLat != null && activeLng != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View on map",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        val mapUri =
                                            "https://www.google.com/maps/search/?api=1&query=${activeLat},${activeLng}".toUri()
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
                                    .alpha(if (fishLat == null) 0.6f else 1f)
                            )
                        }
                    }

                    if (fish.fish.length != null) {
                        DetailRow(
                            label = "Length",
                            value = fish.fish.length.toLengthDisplayString(
                                useImperial = useImperial,
                                useFractions = true
                            )
                        )
                    }

                    if (fishSettings.showHoleNumber) {
                        fish.fish.holeNumber?.let {
                            DetailRow(label = "Hole #", value = it.toString())
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(.25f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailBox(
                        thumbnail = fishermanThumbnail,
                        imageVector = AppIcons.Default.Fisherman,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(.75f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fish.fisherman.fullName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (fish.fish.bodyOfWaterId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(.25f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ThumbnailBox(
                            thumbnail = bodyOfWaterThumbnail,
                            imageVector = AppIcons.Default.BodyOfWater,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(.75f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = fish.bodyOfWater?.name ?: "Unknown",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier.weight(0.25f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailBox(
                        thumbnail = tripThumbnail,
                        imageVector = AppIcons.Default.Boat,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = fish.trip.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    Modifier.weight(0.25f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailBox(
                        thumbnail = eventThumbnail,
                        imageVector = AppIcons.Default.CanoeEmpty,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = fish.event.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    modifier = Modifier.weight(.5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DetailRow(
                        label = "Date",
                        value = dateFormatter.format(Date(fish.fish.timestamp))
                    )
                    DetailRow(
                        label = "Time",
                        value = timeFormatter.format(Date(fish.fish.timestamp))
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            thickness = 1.dp,
            color = getOnMainColor()
        )

        // Lure section
        if (fish.lure != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(.25f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailBox(
                        thumbnail = lureThumbnail,
                        imageVector = AppIcons.Default.Lure,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(.75f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fish.lure.lure.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LureColorComposition(
                            primary = fish.lure.primaryColors,
                            secondary = fish.lure.secondaryColors,
                            glows = fish.lure.lure.glows,
                            glow = fish.lure.glowColors
                        )
                    }
                }
            }
        }
        if (fish.fish.baitId != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(.25f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ThumbnailBox(
                        thumbnail = baitThumbnail,
                        imageVector = AppIcons.Default.Worm,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(.75f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fish.bait?.name ?: "Unknown",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (fish.lure != null || fish.bait != null) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                thickness = 1.dp,
                color = getOnMainColor()
            )
        }

        PhotoPickerRow(
            photos = fishPhotos,
            onPhotoSelected = onPhotoSelected,
            onPhotoTaken = onPhotoTaken,
            onPhotoDeleted = { photo ->
                onPhotoDeleted(photo as FishPhoto)
            },
            onSetThumbnail = { photo ->
                onSetThumbnail(photo as FishPhoto)
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            thickness = 1.dp,
            color = getOnMainColor()
        )

        if (categoryConfigs.size > 1) {
            CategoryRow(
                categories = categoryConfigs,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                }
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                thickness = 1.dp,
                color = getOnMainColor()
            )
        }

        Crossfade(
            targetState = selectedCategory,
            label = "CategoryTransition",
            modifier = Modifier.fillMaxWidth()
        ) { category ->
            when (category) {
                CategoryType.NOTES -> {
                    NoteSection(
                        noteList = sortedNotes,
                        showHelp = showHelp,
                        emptyText = "No notes created.",
                        helpText = "Notes help you remember specific details about this fish.",
                        onAdd = { showAddNoteDialog = true },
                        onEdit = {
                            noteToEdit = it as FishNote?
                            showEditNoteDialog = true
                        },
                        onDelete = {
                            noteToDelete = it as FishNote?
                            showDeleteNoteDialog = true
                        },
                        onToggleHelp = onToggleHelp,
                    )
                }
                CategoryType.WATER -> {
                    if (fish.water != null) {
                        Column(modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            WaterCard(
                                water = fish.water,
                                useImperial = useImperial,
                                onThumbnailLongClick = onToggleImperialUnits
                            )
                        }
                    }
                }
                CategoryType.WEATHER -> {
                    if (fish.weather != null) {
                        Column(modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            WeatherCard(
                                weather = fish.weather,
                                useImperial = useImperial,
                                onThumbnailLongClick = onToggleImperialUnits
                            )
                        }
                    }
                }
                else -> {
                    // DO NOTHING FOR NOW
                }
            }
        }
    }
    if (showAddNoteDialog) {
        NewNoteDialog(
            onDismiss = {
                showAddNoteDialog = !showAddNoteDialog
            },
            onConfirm = { content ->
                onAddNote(null, content)
                showAddNoteDialog = !showAddNoteDialog
            }
        )
    }
    if (showDeleteNoteDialog) {
        noteToDelete?.let { note ->
            DeleteDialog(
                title = "Delete Note",
                message = "Are you sure you want to delete this note?",
                onConfirm = {
                    onDeleteNote(note.id)
                    showDeleteNoteDialog = !showDeleteNoteDialog
                },
                onDismiss = { showDeleteNoteDialog = !showDeleteNoteDialog }
            )
        }
    }
    if (showEditNoteDialog) {
        noteToEdit?.let { note ->
            EditNoteDialog(
                content = note.content,
                onDismiss = {
                    showEditNoteDialog = !showEditNoteDialog
                },
                onConfirm = { content ->
                    onAddNote(note.id, content)
                    showEditNoteDialog = !showEditNoteDialog
                }
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.3f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(0.7f)
        )
    }
}