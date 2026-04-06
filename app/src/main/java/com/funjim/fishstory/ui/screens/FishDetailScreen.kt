package com.funjim.fishstory.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.viewmodels.FishViewModel
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
        fishList.indexOfFirst { it.id == initialFishId }.coerceAtLeast(0)
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
                        text = if (fish != null) fish.speciesName else "Fish Detail",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                FishDetailContent(fish = fish)
            }

            // Navigation row
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
private fun FishDetailContent(fish: FishWithDetails) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Released / Kept badge
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (fish.isReleased)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (fish.isReleased) Icons.Default.RemoveCircle else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (fish.isReleased)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (fish.isReleased) "Released" else "Kept",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (fish.isReleased)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        HorizontalDivider()

        // Core details
        DetailRow(label = "Species", value = fish.speciesName)
        DetailRow(label = "Fisherman", value = fish.fishermanName)
        DetailRow(label = "Length", value = "${fish.length}\"")
        DetailRow(label = "Caught", value = dateFormatter.format(Date(fish.timestamp)))

        fish.holeNumber?.let {
            DetailRow(label = "Hole #", value = it.toString())
        }

        HorizontalDivider()

        // Lure section
        if (fish.lureName != null) {
            Text(
                text = "Lure",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            DetailRow(label = "Name", value = fish.lureName)
            fish.lurePrimaryColorName?.let { DetailRow(label = "Primary Color", value = it) }
            fish.lureSecondaryColorName?.let { DetailRow(label = "Secondary Color", value = it) }
            if (fish.lureGlows == true) {
                DetailRow(
                    label = "Glows",
                    value = fish.lureGlowColorName?.let { "Yes — $it" } ?: "Yes"
                )
            }
        } else {
            DetailRow(label = "Lure", value = "None recorded")
        }

        // Location section
        if (fish.latitude != null && fish.longitude != null) {
            HorizontalDivider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            DetailRow(label = "Lat", value = "%.5f".format(fish.latitude))
            DetailRow(label = "Lon", value = "%.5f".format(fish.longitude))
        }

        Spacer(modifier = Modifier.height(16.dp))
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
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}
