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
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxWithLures
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.FishermanHighlightCard
import com.funjim.fishstory.ui.utils.LureCompositionWithColors
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItem
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.viewmodels.FishermanDetailsViewModel
import kotlinx.coroutines.launch

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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fishermanPhotos by viewModel.fishermanPhotos.collectAsStateWithLifecycle()

    var showAddTackleBoxDialog by remember { mutableStateOf(false) }
    var newTackleBoxName by remember { mutableStateOf("") }
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
                    val scope = rememberCoroutineScope()

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = details.fisherman.fullName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )

                            PhotoPickerRow(
                                photos = fishermanPhotos,
                                onPhotoSelected = { uri ->
                                    viewModel.addFishermanPhoto(fishermanId = fishermanId, uri, true)
                                },
                                onPhotoTaken = { uri ->
                                    viewModel.addFishermanPhoto(fishermanId = fishermanId, uri, false)
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deleteFishermanPhoto(fishermanId, photo.id)
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
                                                onDelete = { tackleBoxToDelete = tackleBox.tackleBox }
                                            )
                                        }
                                    }

                                    Row(
                                        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
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
                                        onDelete = { tackleBoxToDelete = tackleBoxWithLures.tackleBox }
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
                                    thumbnailFlow = viewModel.tripThumbnail(trip.trip.id),
                                    onClick = { navigateToTripDetails(trip.trip.id) },
                                    onLongClick = {},
                                    onFishClick = null,
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
                            val tripsSize = state.recentTrips.size
                            itemsIndexed(state.recentTrips) { index, trip ->
                                TripItem(
                                    trip = trip,
                                    index = index,
                                    totalItems = tripsSize,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    thumbnailFlow = viewModel.tripThumbnail(trip.trip.id),
                                    onClick = { navigateToTripDetails(trip.trip.id) },
                                    onLongClick = {},
                                    onFishClick = null,
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

    // DELETE CONFIRMATION
    tackleBoxToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { tackleBoxToDelete = null },
            title = { Text("Delete Tackle Box?") },
            text = { Text("""Are you sure you want to delete '${item.name}'?

This cannot be undone.

Tackle Boxes are mostly for historical purposes.  They are associated with trips and events. 

They are also used to selecting the 'Lure Used' when logging a fish/catch.

If you delete a tackle box, you may not be able to select a lure when logging a fish/catch for a particular event""") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTackleBox(item)
                        tackleBoxToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tackleBoxToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
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

@Composable
fun TackleBoxCard(
    tackleBoxWithLures: TackleBoxWithLures,
    index: Int = 0,
    totalItems: Int = 0,
    modifier: Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

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
                onClick = { expanded = !expanded },
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
                    AppIcons.Default.TackleBox,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tackleBoxWithLures.tackleBox.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row() {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Lures"
                        )
                        Text(
                            "${tackleBoxWithLures.lures.size} Lures",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryContentColor
                        )
                    }
                }
                Box {
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
                            text = { Text("Delete") },
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
                    sortedLures.forEach { lure ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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

