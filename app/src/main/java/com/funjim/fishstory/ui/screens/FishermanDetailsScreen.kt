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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanTripSummary
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBoxWithLures
import com.funjim.fishstory.ui.PhotoPickerRow
import com.funjim.fishstory.viewmodels.FishermanDetailsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanDetailsScreen(
    viewModel: FishermanDetailsViewModel,
    fishermanId: String,
    navigateToTripDetails: (String) -> Unit,
    navigateToFishList: (String) -> Unit,
    navigateToLureList: (String, String) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(fishermanId) {
        viewModel.selectFisherman(fishermanId)
    }

    val fishermanPhotos by viewModel.getPhotosForFisherman(fishermanId).collectAsStateWithLifecycle(initialValue = emptyList())

    var showEditFishermanDialog by remember { mutableStateOf(false) }

    val stats by viewModel.statistics.collectAsState()
    val tripSummaries by viewModel.tripSummaries.collectAsStateWithLifecycle()

    var showAddTackleBoxDialog by remember { mutableStateOf(false) }
    var newTackleBoxName by remember { mutableStateOf("") }

    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00274C)) // Michigan Blue
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Fisherman Details") },
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
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = details.fisherman.fullName,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
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

                            // Use a Row to align the title and the '+' button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tackle Boxes",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                // The new '+' button
                                IconButton(
                                    onClick = {
                                        newTackleBoxName = ""
                                        showAddTackleBoxDialog = true
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color(0xFFFFCB05), // Michigan Maize
                                        contentColor = Color(0xFF00274C)    // Michigan Blue
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Tackle Box")
                                }
                            }
                        }

                        itemsIndexed(details.tackleBoxesWithLures) { index, tackleBoxWithLures ->
                            if (tackleBoxWithLures != null) {
                                TackleBoxCard(
                                    viewModel,
                                    tackleBoxWithLures,
                                    modifier = Modifier.padding(
                                        bottom = if (index == details.tackleBoxesWithLures.lastIndex) 8.dp else 0.dp
                                    ),
                                    onEdit = {
                                        navigateToLureList(fishermanId, tackleBoxWithLures.tackleBox.id)
                                    },
                                    onDelete = {
                                        viewModel.deleteTackleBox(tackleBoxWithLures.tackleBox)
                                    }
                                )
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

                        items(tripSummaries) { trip ->
                            TripDetailItem(
                                trip = trip,
                                onClick = {
                                    navigateToTripDetails(trip.trip.id)
                                }
                            )
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
fun TripDetailItem(
    trip: FishermanTripSummary,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(enabled = true, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trip.trip.name, style = MaterialTheme.typography.titleMedium)
                    if (trip.trip.latitude != null && trip.trip.longitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val mapUri =
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${trip.trip.latitude},${trip.trip.longitude}")
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
                }
                Text(
                    text = "Start: ${dateTimeFormatter.format(Date(trip.trip.startDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "End: ${dateTimeFormatter.format(Date(trip.trip.endDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fish • ${trip.totalCaught} Caught • ${trip.totalKept} Kept",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FishermanLoadingView() {
    // 1. Define the Shimmer colors (Michigan Blue as the base)
    val shimmerColors = listOf(
        Color(0xFF00274C).copy(alpha = 0.6f),
        Color(0xFF00274C).copy(alpha = 0.2f),
        Color(0xFF00274C).copy(alpha = 0.6f),
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
    viewModel: FishermanDetailsViewModel,
    tackleBoxWithLures: TackleBoxWithLures,
    modifier: Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0077B6)),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingBag,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tackleBoxWithLures.tackleBox.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "${tackleBoxWithLures.lures.size} Lures",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
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
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    tackleBoxWithLures.lures.forEach { lure ->
                        val colors by viewModel.lureColors.collectAsState(initial = emptyList())

                        val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
                        val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
                        val glowColorName = colors.find { it.id == lure.glowColorId }?.name

                        Text(
                            text = "• ${lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName)}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
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
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),

    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "HIGHLIGHTS",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF00274C), // Michigan Blue
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            // Top Section: The Big/Small Trophy Fish
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "LARGEST FISH",
                    value = "${stats.largestFishLength ?: 0.0}\"",
                    color = Color(0xFF00274C), // Michigan Blue
                )
                StatItem(
                    label = "SMALLEST FISH",
                    value = "${stats.smallestFishLength ?: 0.0}\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!stats.bestTripName.isNullOrEmpty()) {
                    AchievementItem(
                        icon = Icons.Default.Celebration,
                        label = "Best Trip (${stats.mostTripCatches} fish)",
                        name = stats.bestTripName
                    )
                }
                if (!stats.bestSegmentName.isNullOrEmpty()) {
                    AchievementItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "Best Segment (${stats.mostSegmentCatches} fish)",
                        name = "${stats.bestSegmentName}\n(${stats.bestSegmentTripName})"
                    )
                }
                if (!stats.worstTripName.isNullOrEmpty()) {
                    AchievementItem(
                        icon = Icons.Default.Warning,
                        label = "Worst Trip (${stats.fewestTripCatches} fish)",
                        name = stats.worstTripName
                    )
                }
                if (!stats.worstSegmentName.isNullOrEmpty()) {
                    AchievementItem(
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        label = "Worst Segment (${stats.fewestSegmentCatches} fish)",
                        name = "${stats.worstSegmentName}\n(${stats.worstSegmentTripName})"
                    )
                }
            }
        }
    }
}