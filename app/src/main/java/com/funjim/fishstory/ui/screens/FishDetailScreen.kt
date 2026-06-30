package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.LureColorComposition
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.ReleasedChip
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.toDisplayString
import com.funjim.fishstory.viewmodels.FishViewModel
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishDetailScreen(
    viewModel: FishViewModel,
    initialFishId: String,
    navigateBack: () -> Unit
) {
    val fishList by viewModel.fishForScope.collectAsStateWithLifecycle()

    // Find initial index — if fish is deleted or list changes, clamp to valid range
    val initialIndex = remember(fishList, initialFishId) {
        fishList.indexOfFirst { it.fish.id == initialFishId }.coerceAtLeast(0)
    }

    var currentIndex by remember(initialIndex) { mutableIntStateOf(initialIndex) }

    // Clamp index if list shrinks
    val safeIndex = currentIndex.coerceIn(0, (fishList.size - 1).coerceAtLeast(0))
    if (safeIndex != currentIndex) currentIndex = safeIndex

    val fish = fishList.getOrNull(currentIndex)

    // Swipe gesture state
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = dragOffset, label = "swipe")
    val swipeThreshold = 120f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = fish?.species?.name ?: "Fish Detail",
                        style = MaterialTheme.typography.titleLarge
                    )
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
                    if (fishList.size > 1) {
                        Text(
                            text = "${currentIndex + 1} / ${fishList.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (fish == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No fish to display.")
            }
            return@Scaffold
        }

        val fishPhotos = remember(fish.fish.id) {
            viewModel.fishPhotos(fish.fish.id)
        }
        val bodyOfWaterThumbnailFlow = remember(fish.fish.bodyOfWaterId) {
            viewModel.bodyOfWaterThumbnail(fish.fish.bodyOfWaterId ?: "")
        }
        val eventThumbnailFlow = remember(fish.fish.eventId) {
            viewModel.eventThumbnail(fish.fish.eventId)
        }
        val fishThumbnailFlow = remember(fish.fish.id) {
            viewModel.fishThumbnail(fish.fish.id)
        }
        val fishermanThumbnailFlow = remember(fish.fish.fishermanId) {
            viewModel.fishermanThumbnail(fish.fish.fishermanId)
        }
        val lureThumbnailFlow = remember(fish.fish.lureId) {
            viewModel.lureThumbnail(fish.fish.lureId ?: "")
        }
        val speciesThumbnailFlow = remember(fish.species.id) {
            viewModel.speciesThumbnail(fish.species.id)
        }
        val tripThumbnailFlow = remember(fish.fish.tripId) {
            viewModel.tripThumbnail(fish.fish.tripId)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Swipeable content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(fishList.size) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                when {
                                    dragOffset < -swipeThreshold && currentIndex < fishList.size - 1 -> {
                                        currentIndex++
                                    }
                                    dragOffset > swipeThreshold && currentIndex > 0 -> {
                                        currentIndex--
                                    }
                                }
                                dragOffset = 0f
                            },
                            onDragCancel = { dragOffset = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                val newOffset = dragOffset + dragAmount
                                // Resist dragging at edges
                                val atStart = currentIndex == 0 && newOffset > 0
                                val atEnd = currentIndex == fishList.size - 1 && newOffset < 0
                                dragOffset = when {
                                    atStart -> newOffset * 0.2f
                                    atEnd -> newOffset * 0.2f
                                    else -> newOffset
                                }
                            }
                        )
                    }
                    .graphicsLayer {
                        translationX = animatedOffset
                        alpha = 1f - (animatedOffset.absoluteValue / (swipeThreshold * 4)).coerceIn(0f, 0.4f)
                    }
            ) {
                FishDetailContent(
                    fish = fish,
                    fishThumbnailFlow =
                        if (fish.photoCount == 0) speciesThumbnailFlow
                        else fishThumbnailFlow,
                    fishPhotoFlow = fishPhotos,
                    bodyOfWaterThumbnailFlow = bodyOfWaterThumbnailFlow,
                    eventThumbnailFlow = eventThumbnailFlow,
                    fishermanThumbnailFlow = fishermanThumbnailFlow,
                    lureThumbnailFlow = lureThumbnailFlow,
                    tripThumbnailFlow = tripThumbnailFlow,
                    onPhotoSelected = { uri ->
                        viewModel.addFishPhoto(fishId = fish.fish.id, uri = uri, true)
                    },
                    onPhotoTaken = { uri ->
                        viewModel.addFishPhoto(fishId = fish.fish.id, uri = uri, false)
                    },
                    onPhotoDeleted = { photo ->
                        viewModel.deleteFishPhoto(fishId = fish.fish.id, photo.id)
                    }
                )
            }

            if (fishList.size > 1) {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentIndex > 0) currentIndex-- },
                        enabled = currentIndex > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous fish")
                    }

                    Text(
                        text = "Swipe to navigate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { if (currentIndex < fishList.size - 1) currentIndex++ },
                        enabled = currentIndex < fishList.size - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next fish")
                    }
                }
            }
        }
    }
}

@Composable
private fun FishDetailContent(
    fish: FishWithDetails,
    fishThumbnailFlow: Flow<ByteArray?>,
    fishPhotoFlow: Flow<List<Photo>>,
    bodyOfWaterThumbnailFlow: Flow<ByteArray?>,
    eventThumbnailFlow: Flow<ByteArray?>,
    fishermanThumbnailFlow: Flow<ByteArray?>,
    lureThumbnailFlow: Flow<ByteArray?>,
    tripThumbnailFlow: Flow<ByteArray?>,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onPhotoDeleted: (Photo) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val fishPhotos by fishPhotoFlow.collectAsState(initial = null)
    val bodyOfWaterThumbnail by bodyOfWaterThumbnailFlow.collectAsState(initial = null)
    val fishThumbnail by fishThumbnailFlow.collectAsState(initial = null)
    val tripThumbnail by tripThumbnailFlow.collectAsState(initial = null)
    val eventThumbnail by eventThumbnailFlow.collectAsState(initial = null)
    val fishermanThumbnail by fishermanThumbnailFlow.collectAsState(initial = null)
    val lureThumbnail by lureThumbnailFlow.collectAsState(initial = null)

    val context = LocalContext.current

    val trip = fish.trip
    val event = fish.event

    val eventLat = event.latitude
    val fishLat = fish.fish.latitude

    // Precedence logic: Use Event if it exists, otherwise use Trip
    val activeLat = fish.fish.latitude ?: event.latitude ?: trip.latitude
    val activeLng = fish.fish.longitude ?: event.longitude ?: trip.longitude

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TODO - need to redo this for multi catches
        ReleasedChip(fish.fish.keptCount == 0)

        HorizontalDivider()

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
                    modifier = Modifier.size(64.dp)
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
                        fontWeight = FontWeight.Bold,
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
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${activeLat},${activeLng}")
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
                        if (fishLat == null) {
                            Text(
                                text = if (eventLat != null) "(Event)" else "(Trip)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (fish.fish.length != null) {
                    DetailRow(
                        label = "Length",
                        value = fish.fish.length.toDisplayString(
                            useMetric = false,
                            useFractions = true
                        )
                    )
                }

                fish.fish.holeNumber?.let {
                    DetailRow(label = "Hole #", value = it.toString())
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
                horizontalAlignment = Alignment.CenterHorizontally) {
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
                horizontalAlignment = Alignment.CenterHorizontally) {
                ThumbnailBox(
                    thumbnail = eventThumbnail,
                    imageVector = AppIcons.Default.Boat,
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
                horizontalAlignment = Alignment.CenterHorizontally) {
                DetailRow(label = "Date", value = dateFormatter.format(Date(fish.fish.timestamp)))
                DetailRow(label = "Time", value = timeFormatter.format(Date(fish.fish.timestamp)))
            }
        }

        PhotoPickerRow(
            photos = fishPhotos ?: emptyList(),
            onPhotoSelected = onPhotoSelected,
            onPhotoTaken = onPhotoTaken,
            onPhotoDeleted = onPhotoDeleted
        )

        HorizontalDivider()

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
