package com.funjim.fishstory.ui.screens.details

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureWithColorsSummary
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxWithLures
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.cards.FishermanHighlightCard
import com.funjim.fishstory.ui.utils.LureCompositionWithColors
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.cards.TripAction
import com.funjim.fishstory.ui.cards.TripCard
import com.funjim.fishstory.ui.crud.NewTackleBox
import com.funjim.fishstory.ui.crud.EditFishermanDialog
import com.funjim.fishstory.ui.screens.UpcomingTripChip
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.viewmodels.FishermanDetailsViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanDetailsScreen(
    viewModel: FishermanDetailsViewModel,
    fishermanId: String,
    navigateToTripDetails: (String) -> Unit,
    navigateToFishList: (String, String?, String?, Boolean) -> Unit,
    navigateToSelectLures: (String, String) -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    LaunchedEffect(fishermanId) {
        viewModel.selectFisherman(fishermanId)
    }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val showPhotos by viewModel.showPhotos.collectAsStateWithLifecycle()

    var showDeleteFishermanDialog by remember { mutableStateOf(false) }
    var showEditFishermanDialog by remember { mutableStateOf(false) }

    var tackleBoxesExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fishermanPhotos by viewModel.fishermanPhotos.collectAsStateWithLifecycle()

    var showAddTackleBoxDialog by remember { mutableStateOf(false) }
    var showDeleteTackleBoxDialog by remember { mutableStateOf(false) }
    var tackleBoxToDelete by remember { mutableStateOf<TackleBox?>(null) }

    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) // Michigan Blue
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Fisherman Details") },
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
                        IconButton(onClick = { showAddTackleBoxDialog = true }) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    AppIcons.Default.TackleBox,
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
                                        contentDescription = "Add Tackle Box",
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
                                    text = { Text("Rename") },
                                    onClick = {
                                        menuExpanded = false
                                        showEditFishermanDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Rename"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        if (showPhotos) Text("Hide Photos")
                                        else Text("Show Photos")
                                    },
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
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteFishermanDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Fisherman",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .fillMaxSize()) {
                stats?.let { details ->
                    val totalTackleBoxes = details.tackleBoxesWithLures.size
                    val pagerState = rememberPagerState(pageCount = { totalTackleBoxes })
                    val scope = rememberCoroutineScope()
                    val thumbnail by viewModel.fishermanThumbnail().collectAsState(initial = null)

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.Fisherman,
                                    modifier = Modifier.size(64.dp),
                                    onClick = {
                                        viewModel.toggleShowPhotos()
                                    }
                                )
                                Text(
                                    text = details.fisherman.fullName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.primary
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
                                        photos = fishermanPhotos,
                                        onPhotoSelected = { uri ->
                                            viewModel.addFishermanPhoto(
                                                fishermanId = fishermanId,
                                                uri,
                                                true
                                            )
                                        },
                                        onPhotoTaken = { uri ->
                                            viewModel.addFishermanPhoto(
                                                fishermanId = fishermanId,
                                                uri,
                                                false
                                            )
                                        },
                                        onSetThumbnail = { photo ->
                                            viewModel.setFishermanThumbnail(
                                                fishermanId = fishermanId,
                                                photoId = photo.id
                                            )
                                        },
                                        onPhotoDeleted = { photo ->
                                            viewModel.deleteFishermanPhoto(fishermanId, photo.id)
                                        }
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                        thickness = 1.dp,
                                        color = getOnMainColor()
                                    )
                                }
                            }

                            FishermanHighlightCard(
                                stats!!,
                                useImperial = appSettings.display.useImperialUnits
                            ) {
                                navigateToFishList(fishermanId, null, null, false)
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = getOnMainColor()
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tackleBoxesExpanded = !tackleBoxesExpanded }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (totalTackleBoxes > 1) {
                                        Icon(
                                            imageVector =
                                                if (tackleBoxesExpanded) Icons.Default.ExpandLess
                                                else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Tackle Boxes",
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        if (totalTackleBoxes > 1) {
                                            if (!tackleBoxesExpanded) {
                                                BadgeCount(
                                                    count = totalTackleBoxes,
                                                    index = pagerState.currentPage + 1,
                                                    badgeColor = getMainButtonColor(),
                                                    textColor = getOnMainButtonColor()
                                                )
                                            } else {
                                                BadgeCount(
                                                    count = totalTackleBoxes,
                                                    badgeColor = getMainButtonColor(),
                                                    textColor = getOnMainButtonColor()
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        showAddTackleBoxDialog = true
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Tackle Box"
                                    )
                                }
                            }
                        }

                        if (!tackleBoxesExpanded && totalTackleBoxes > 1) {
                            item {
                                Column {
                                    HorizontalPager(
                                        state = pagerState,
                                        contentPadding = PaddingValues(horizontal = 32.dp), // Shows a peek of next/prev cards
                                        pageSpacing = 0.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { page ->
                                        val tackleBox = details.tackleBoxesWithLures[page]

                                        if (tackleBox != null) {
                                            TackleBoxCard(
                                                tackleBox,
                                                verbose = appSettings.display.verboseCards,
                                                index = page,
                                                totalItems = totalTackleBoxes,
                                                modifier = Modifier.padding(horizontal = 4.dp),
                                                onEdit = {
                                                    navigateToSelectLures(
                                                        fishermanId,
                                                        tackleBox.tackleBox.id
                                                    )
                                                },
                                                onDelete = {
                                                    tackleBoxToDelete = tackleBox.tackleBox
                                                    showDeleteTackleBoxDialog = true
                                                }
                                            )
                                        }
                                    }

                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        repeat(totalTackleBoxes) { index ->
                                            val isSelected = pagerState.currentPage == index
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 3.dp)
                                                    .size(if (isSelected) 8.dp else 6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.3f
                                                        )
                                                    )
                                                    .clickable {
                                                        scope.launch {
                                                            pagerState.animateScrollToPage(index)
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(details.tackleBoxesWithLures) { index, tackleBoxWithLures ->
                                tackleBoxWithLures?.let {
                                    TackleBoxCard(
                                        tackleBoxWithLures,
                                        verbose = appSettings.display.verboseCards,
                                        index = index,
                                        totalItems = totalTackleBoxes,
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            bottom = if (index == details.tackleBoxesWithLures.lastIndex) 8.dp else 0.dp
                                        ),
                                        onEdit = {
                                            navigateToSelectLures(
                                                fishermanId,
                                                tackleBoxWithLures.tackleBox.id
                                            )
                                        },
                                        onDelete = {
                                            tackleBoxToDelete = tackleBoxWithLures.tackleBox
                                            showDeleteTackleBoxDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                thickness = 1.dp,
                                color = getOnMainColor()
                            )

                            Row(
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trips",
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                if (state.recentTrips.size + state.activeTrips.size + state.upcomingTrips.size > 1) {
                                    val total = state.recentTrips.size +
                                            state.activeTrips.size +
                                            state.upcomingTrips.size
                                    BadgeCount(
                                        count = total,
                                        badgeColor = getMainButtonColor(),
                                        textColor = getOnMainButtonColor()
                                    )
                                }
                            }
                        }

                        item {
                            if (state.upcomingTrips.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Upcoming Trips",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    if (state.upcomingTrips.size > 1) {
                                        BadgeCount(
                                            count = state.upcomingTrips.size,
                                            badgeColor = getMainButtonColor(),
                                            textColor = getOnMainButtonColor()
                                        )
                                    }
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    items(state.upcomingTrips) { trip ->
                                        UpcomingTripChip(
                                            trip = trip.trip,
                                            onTripClick = { navigateToTripDetails(trip.trip.id) }
                                        )
                                    }
                                }
                            }
                        }

                        if (state.activeTrips.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Active Trips",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    if (state.activeTrips.size > 1) {
                                        BadgeCount(
                                            count = state.activeTrips.size,
                                            badgeColor = getMainButtonColor(),
                                            textColor = getOnMainButtonColor()
                                        )
                                    }
                                }
                            }
                            val totalActiveItems = state.activeTrips.size
                            itemsIndexed(state.activeTrips) { index, trip ->
                                TripCard(
                                    trip = trip,
                                    verbose = appSettings.display.verboseCards,
                                    index = index,
                                    totalItems = totalActiveItems,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    thumbnailFlow = viewModel.tripThumbnail(trip.trip.id),
                                    photosFlow = flowOf(emptyList()),
                                    onClick = { navigateToTripDetails(trip.trip.id) },
                                    onLongClick = {},
                                    onFishClick = { tripId, lureId, targetOnly ->
                                        navigateToFishList(fishermanId, tripId, lureId, targetOnly)
                                    },
                                    onAction = { action ->
                                        when (action) {
                                            is TripAction.OpenMap -> {
                                                val mapUri =
                                                    "geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)".toUri()
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
                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                        if (state.recentTrips.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Past Trips",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    if (state.recentTrips.size > 1) {
                                        BadgeCount(
                                            count = state.recentTrips.size,
                                            badgeColor = getMainButtonColor(),
                                            textColor = getOnMainButtonColor()
                                        )
                                    }
                                }
                            }
                            val tripsSize = state.recentTrips.size
                            itemsIndexed(state.recentTrips) { index, trip ->
                                val lures = viewModel.getLuresInTackleBox(
                                    tripId = trip.trip.id,
                                    fishermanId = fishermanId).collectAsState(initial = emptyList()).value
                                val sortedLures = lures.sortedBy { it.lure.name }
                                val lureCount = lures.size

                                TripCard(
                                    trip = trip,
                                    verbose = appSettings.display.verboseCards,
                                    index = index,
                                    totalItems = tripsSize,
                                    lureCount = lureCount,
                                    lures = sortedLures,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    thumbnailFlow = viewModel.tripThumbnail(trip.trip.id),
                                    photosFlow = flowOf(emptyList()),
                                    onClick = { navigateToTripDetails(trip.trip.id) },
                                    onLongClick = {},
                                    onFishClick = { tripId, lureId, targetOnly ->
                                        navigateToFishList(fishermanId, tripId, lureId, targetOnly)
                                    },
                                    onAction = { action ->
                                        when (action) {
                                            is TripAction.OpenMap -> {
                                                val mapUri =
                                                    "geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)".toUri()
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

                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                    }
                } ?: run {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                }
            }

        }
        stats?.let { details ->
            if (showDeleteFishermanDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteFishermanDialog = !showDeleteFishermanDialog },
                    title = { Text("Delete Fisherman?") },
                    text = {
                        Text(
                            """Are you sure you want to delete '${details.fisherman.fullName}'?

This cannot be undone.

If you delete ${details.fisherman.fullName}, they will be removed from all trips (${details.totalTrips}) and events.

Tackle boxes and fish (${details.totalFishCaught}) logged for ${details.fisherman.fullName} will also be deleted."""
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteFishermanDialog = !showDeleteFishermanDialog
                                viewModel.deleteFisherman()
                                navigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteFishermanDialog = !showDeleteFishermanDialog }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showEditFishermanDialog) {
                EditFishermanDialog(
                    fisherman = details.fisherman,
                    onDismiss = { showEditFishermanDialog = !showEditFishermanDialog },
                    onConfirm = { fisherman ->
                        viewModel.updateFisherman(fisherman) {
                            showEditFishermanDialog = !showEditFishermanDialog
                        }
                    }
                )
            }
        }
    }

    // Create new tackle box dialog
    if (showAddTackleBoxDialog) {
        NewTackleBox(
            fishermanId = fishermanId,
            onDismiss = { showAddTackleBoxDialog = !showAddTackleBoxDialog },
            onConfirm = { tackleBox ->
                viewModel.createTackleBox(tackleBox) {
                    showAddTackleBoxDialog = !showAddTackleBoxDialog
                }
            }
        )
    }
    if (showDeleteTackleBoxDialog) {
        tackleBoxToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { showDeleteTackleBoxDialog = !showDeleteTackleBoxDialog },
                title = { Text("Delete Tackle Box?") },
                text = {
                    Text(
                        """Are you sure you want to delete '${item.name}'?

This cannot be undone.

Tackle Boxes are mostly for historical purposes.  They are associated with trips and events. 

They are also used to selecting the 'Lure Used' when logging a fish/catch.

If you delete a tackle box, you may not be able to select a lure when logging a fish/catch for a particular event"""
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTackleBox(item)
                            showDeleteTackleBoxDialog = !showDeleteTackleBoxDialog
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteTackleBoxDialog = !showDeleteTackleBoxDialog }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TackleBoxCard(
    tackleBoxWithLures: TackleBoxWithLures,
    verbose: Boolean,
    index: Int = 0,
    totalItems: Int = 0,
    modifier: Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    var luresExpanded by remember { mutableStateOf(false) }

    val sortedLures by remember(tackleBoxWithLures.lures) {
        derivedStateOf {
            val getColorsSortingString = { colors: List<LureColor> ->
                colors.map { it.name }.sorted().joinToString(",")
            }

            tackleBoxWithLures.lures.sortedWith(
                compareBy<LureWithColors> { it.lure.name }
                    .thenBy { getColorsSortingString(it.primaryColors) }
                    .thenBy { getColorsSortingString(it.secondaryColors) }
                    .thenBy { getColorsSortingString(it.glowColors) }
            )
        }
    }

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { luresExpanded = !luresExpanded },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, color = borderColor),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        if (luresExpanded) AppIcons.Default.TackleBox
                        else AppIcons.Default.TackleBoxClosed,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tackleBoxWithLures.tackleBox.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    CardItemWithValue(
                        icon = AppIcons.Default.Lure,
                        value = tackleBoxWithLures.lures.size.toString(),
                        description = if (verbose) {
                            if (tackleBoxWithLures.lures.size == 1) "Lure"
                            else "Lures"
                        } else null,
                        onClick = onEdit,
                        contentColor = secondaryContentColor
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Fisherman options",
                            tint = contentColor
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Lures") },
                            onClick = {
                                menuExpanded = false
                                luresExpanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = AppIcons.Default.Lure,
                                    contentDescription = "Add Lures",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )

                        if (tackleBoxWithLures.lures.isNotEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    if (luresExpanded) Text("Hide Lures")
                                    else Text("Show Lures")
                                },
                                onClick = {
                                    menuExpanded = false
                                    luresExpanded = !luresExpanded
                                },
                                leadingIcon = {
                                    Icon(
                                        if (luresExpanded) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Tackle Box",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            // Expandable lure list
            AnimatedVisibility(visible = luresExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    thickness = 1.dp
                )
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    sortedLures.forEach { lure ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // FIX LATER
                            LureCompositionWithColors(
                                name = "• ${lure.lure.name}",
                                lure.primaryColors,
                                lure.secondaryColors,
                                lure.lure.glows,
                                lure.glowColors,
                                style = MaterialTheme.typography.bodySmall,
                                contentColor = secondaryContentColor,
                                modifier = Modifier
                                    .padding(start = 50.dp, bottom = 4.dp),
                                colorBadgeSize = 20.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

