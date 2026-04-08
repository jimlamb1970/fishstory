package com.funjim.fishstory.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.PhotoPickerRow
import com.funjim.fishstory.viewmodels.FishermanDetailsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanDetailsScreen(
    viewModel: FishermanDetailsViewModel,
    fishermanId: String,
    navigateToLureList: (String) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(fishermanId) {
        viewModel.selectFisherman(fishermanId)
    }

    val fishermanWithDetails by viewModel.getFishermanWithDetails(fishermanId).collectAsStateWithLifecycle(initialValue = null)
    val fishermanPhotos by viewModel.getPhotosForFisherman(fishermanId).collectAsStateWithLifecycle(initialValue = emptyList())

    var showEditFishermanDialog by remember { mutableStateOf(false) }

    val stats by viewModel.statistics.collectAsState()

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
                fishermanWithDetails?.let { details ->
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

                            LureSummary(details.tackleBoxWithLures?.lures?.size ?: 0) {
                                navigateToLureList(fishermanId)
                            }

                            HorizontalDivider()

                            FishermanHighlightCard(stats!!)

                            HorizontalDivider()

                            Text(
                                text = "Trips",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        items(details.trips) { trip ->
                            TripDetailItem(
                                trip = trip,
                                onDelete = {
                                    viewModel.deleteTripFromFisherman(trip.id, fishermanId)
                                }
                            )
                        }
                    }
                } ?: run {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                }
            }

            // Edit Fisherman Dialog
            if (showEditFishermanDialog && fishermanWithDetails != null) {
                EditFishermanDialog(
                    initialFisherman = fishermanWithDetails!!.fisherman,
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
fun TripDetailItem(trip: Trip, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(trip.name, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove from Fisherman", tint = Color.Red)
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
fun LureSummary(lureCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0077B6))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                    "TACKLEBOX",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "$lureCount Lures",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun FishermanHighlightCard(
    stats: FishermanFullStatistics
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
/*
@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AchievementItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF00274C), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
*/