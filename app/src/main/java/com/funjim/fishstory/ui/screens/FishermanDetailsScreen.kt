package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBoxWithLures
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItem
import com.funjim.fishstory.viewmodels.FishermanDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanDetailsScreen(
    viewModel: FishermanDetailsViewModel,
    fishermanId: String,
    navigateToTripDetails: (String) -> Unit,
    navigateToFishList: (String) -> Unit,
    navigateToSelectLures: (String, String) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(fishermanId) {
        viewModel.selectFisherman(fishermanId)
    }

    val context = LocalContext.current
    var showEditFishermanDialog by remember { mutableStateOf(false) }

    // Expansion States for Accordion
    var tackleBoxesExpanded by remember { mutableStateOf(false) }
    var tripsExpanded by remember { mutableStateOf(true) }

    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val tripSummaries by viewModel.tripSummaries.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fishermanPhotos by viewModel.fishermanPhotos.collectAsStateWithLifecycle()

    var showAddTackleBoxDialog by remember { mutableStateOf(false) }
    var newTackleBoxName by remember { mutableStateOf("") }

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
                        IconButton(onClick = { showEditFishermanDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Fisherman")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                stats?.let { details ->
                    val totalTackleBoxes = details.tackleBoxesWithLures.size
                    val pagerState = rememberPagerState(pageCount = { totalTackleBoxes })

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = details.fisherman.fullName,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            PhotoPickerRow(
                                photos = fishermanPhotos,
                                onPhotoSelected = { uri ->
                                    viewModel.addPhoto(
                                        Photo(
                                            uri = uri.toString(),
                                            fishermanId = fishermanId
                                        )
                                    )
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deletePhoto(photo)
                                }
                            )

                            HorizontalDivider()

                            FishermanHighlightCard(stats!!) {
                                navigateToFishList(fishermanId)
                            }
                            HorizontalDivider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { tackleBoxesExpanded = !tackleBoxesExpanded }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                                            style = MaterialTheme.typography.titleLarge
                                        )

                                        if (!tackleBoxesExpanded && totalTackleBoxes > 1) {
                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "(${pagerState.currentPage + 1} of ${totalTackleBoxes})",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        newTackleBoxName = ""
                                        showAddTackleBoxDialog = true
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Tackle Box")
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
                                                viewModel,
                                                tackleBox,
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
                                                    viewModel.deleteTackleBox(tackleBox.tackleBox)
                                                }
                                            )
                                        }
                                    }

                                    Row(
                                        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        repeat(totalTackleBoxes) { iteration ->
                                            val isSelected = pagerState.currentPage == iteration
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
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(details.tackleBoxesWithLures) { index, tackleBoxWithLures ->
                                tackleBoxWithLures?.let {
                                    TackleBoxCard(
                                        viewModel,
                                        tackleBoxWithLures,
                                        index = index,
                                        totalItems = totalTackleBoxes,
                                        modifier = Modifier.padding(
                                            start = 8.dp,
                                            end = 8.dp,
                                            bottom = if (index == details.tackleBoxesWithLures.lastIndex) 8.dp else 0.dp
                                        ),
                                        onEdit = {
                                            navigateToSelectLures(
                                                fishermanId,
                                                tackleBoxWithLures.tackleBox.id
                                            )
                                        },
                                        onDelete = {
                                            viewModel.deleteTackleBox(tackleBoxWithLures.tackleBox)
                                        }
                                    )
                                }
                            }
                        }

                        item {
                            HorizontalDivider()

                            Text(
                                text = "Trips",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        item {
                            if (state.upcomingTrips.isNotEmpty()) {
                                Text(
                                    "Upcoming Trips",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                                Text(
                                    "Active Trips",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            val totalActiveItems = state.activeTrips.size
                            itemsIndexed(state.activeTrips) { index, trip ->
                                TripItem(
                                    trip = trip,
                                    index = index,
                                    totalItems = totalActiveItems,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    onClick = { navigateToTripDetails(trip.trip.id) },
                                    onAction = { action ->
                                        when (action) {
                                            is TripAction.OpenMap -> {
                                                val mapUri =
                                                    Uri.parse("geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)")
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

                                            else -> {}
                                        }
                                    }
                                )
                            }
                        }
                        if (state.recentTrips.isNotEmpty()) {
                            item {
                                Text(
                                    "Past Trips",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            val totalActiveItems = state.recentTrips.size
                            itemsIndexed(state.recentTrips) { index, trip ->
                                TripItem(
                                    trip = trip,
                                    index = index,
                                    totalItems = totalActiveItems,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    onClick = { navigateToTripDetails(trip.trip.id) },
                                    onAction = { action ->
                                        when (action) {
                                            is TripAction.OpenMap -> {
                                                val mapUri =
                                                    Uri.parse("geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)")
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
            // Create new tackle box dialog
            if (showAddTackleBoxDialog) {
                AlertDialog(
                    onDismissRequest = { showAddTackleBoxDialog = false },
                    title = { Text("New Tackle Box") },
                    text = {
                        OutlinedTextField(
                            value = newTackleBoxName,
                            onValueChange = { newTackleBoxName = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newTackleBoxName.isNotBlank()) {
                                    viewModel.createTackleBox(fishermanId, newTackleBoxName.trim())
                                    showAddTackleBoxDialog = false
                                }
                            },
                            enabled = newTackleBoxName.isNotBlank()
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddTackleBoxDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Edit Fisherman Dialog
            stats?.let { details ->
                if (showEditFishermanDialog) {
                    EditFishermanDialog(
                        initialFisherman = details.fisherman,
                        onDismiss = { showEditFishermanDialog = false },
                        onConfirm = { updatedFisherman ->
                            viewModel.updateFisherman(updatedFisherman)
                            showEditFishermanDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditFishermanDialog(
    initialFisherman: Fisherman,
    onDismiss: () -> Unit,
    onConfirm: (Fisherman) -> Unit
) {
    var firstName by remember { mutableStateOf(initialFisherman.firstName) }
    var lastName by remember { mutableStateOf(initialFisherman.lastName) }
    var nickname by remember { mutableStateOf(initialFisherman.nickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Fisherman") },
        text = {
            Column {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isNotBlank() && lastName.isNotBlank()) {
                    onConfirm(initialFisherman.copy(firstName = firstName, lastName = lastName, nickname = nickname))
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FishermanLoadingView() {
    // 1. Define the Shimmer colors (Michigan Blue as the base)
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_animation"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    // 2. The Skeleton Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Mock "Fisherman Name" header
        Box(modifier = Modifier.size(width = 200.dp, height = 32.dp).background(brush, RoundedCornerShape(4.dp)))

        // Mock "Stats" Row
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Box(modifier = Modifier.size(100.dp, 60.dp).background(brush, RoundedCornerShape(8.dp)))
            }
        }

        // Mock "Best Trip" Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(brush, RoundedCornerShape(12.dp))
        )

        // Mock "Smallest/Largest" Fish Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(width = 150.dp, height = 24.dp).background(brush, RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(brush, RoundedCornerShape(12.dp)))
        }
    }
}

// TODO -- remove viewmodel dependency
@Composable
fun TackleBoxCard(
    viewModel: FishermanDetailsViewModel,
    tackleBoxWithLures: TackleBoxWithLures,
    index: Int = 0,
    totalItems: Int = 0,
    modifier: Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val lureNames by viewModel.getFormattedLureList(tackleBoxWithLures.tackleBox.id)
        .collectAsState(initial = "")

    val backgroundColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }
    val borderColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onTertiary
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
                    Icons.Default.Inventory,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tackleBoxWithLures.tackleBox.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "${tackleBoxWithLures.lures.size} Lures",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
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
            AnimatedVisibility(visible = expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    thickness = 1.dp
                )
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = lureNames,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FishermanHighlightCard(
    stats: FishermanFullStatistics,
    onClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> HighlightsPage(stats)
                    1 -> LowlightsPage(stats)
                }
            }

            // Dot indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { i ->
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onTertiary
                                else MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightsPage(stats: FishermanFullStatistics) {
    Column {
        Text(
            text = "HIGHLIGHTS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "LARGEST FISH",
                value = "${stats.largestFishLength ?: 0.0}\"",
                description = stats.largestFishSpecies,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
        if (!stats.bestTripName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.Default.Celebration,
                    label = "Best Trip",
                    name = stats.bestTripName,
                    description = "(${stats.mostTripCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (!stats.bestSegmentName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Best Segment",
                    name = "${stats.bestSegmentName} - (${stats.bestSegmentTripName})",
                    description = "(${stats.mostSegmentCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LowlightsPage(stats: FishermanFullStatistics) {
    Column {
        Text(
            text = "LOWLIGHTS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "SMALLEST FISH",
                value = "${stats.smallestFishLength ?: 0.0}\"",
                description = stats.smallestFishSpecies,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }

        if (!stats.worstTripName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.Default.Warning,
                    label = "Worst Trip",
                    name = stats.worstTripName,
                    description = "(${stats.fewestTripCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (!stats.worstSegmentName.isNullOrEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AchievementItem(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = "Worst Segment",
                    name = "${stats.worstSegmentName} - (${stats.worstSegmentTripName})",
                    description = "(${stats.fewestSegmentCatches} fish)",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}