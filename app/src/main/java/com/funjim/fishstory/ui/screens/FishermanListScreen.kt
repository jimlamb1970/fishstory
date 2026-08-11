package com.funjim.fishstory.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.AddFishermanDialog
import com.funjim.fishstory.ui.utils.FishermanItem
import com.funjim.fishstory.ui.utils.SortChip
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.getChipColor
import com.funjim.fishstory.ui.utils.getOnChipColor
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import com.funjim.fishstory.viewmodels.FishermanListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanListScreen(
    viewModel: FishermanListViewModel,
    navigateToFishermanDetails: (String) -> Unit,
    navigateToFishList: (String, Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    val fishermanSummaries by viewModel.fishermanSummaries.collectAsStateWithLifecycle()
    var fishermanToDelete by remember { mutableStateOf<FishermanSummary?>(null) }

    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Fishermen")
                        val total = fishermanSummaries.size
                        Text(
                            text = " ($total)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
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
                                    .offset(x = 4.dp, y = 4.dp) // Adjust offset to position on the edge
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
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            // Sort Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())) {
                    SortChip("Name", currentOrder == FishermanSortOrder.NAME_AZ) {
                        viewModel.updateSortOrder(FishermanSortOrder.NAME_AZ)
                    }
                    SortChip("Caught", currentOrder == FishermanSortOrder.MOST_CATCHES) {
                        viewModel.updateSortOrder(FishermanSortOrder.MOST_CATCHES)
                    }
                    SortChip("Kept", currentOrder == FishermanSortOrder.MOST_KEPT) {
                        viewModel.updateSortOrder(FishermanSortOrder.MOST_KEPT)
                    }
                    SortChip("Trips", currentOrder == FishermanSortOrder.MOST_TRIPS) {
                        viewModel.updateSortOrder(FishermanSortOrder.MOST_TRIPS)
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleReverse() },
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = getChipColor(),
                            shape = RoundedCornerShape(8.dp)
                        ).size(34.dp)
                ) {
                    Icon(
                        imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Reverse Sort",
                        tint = getOnChipColor()
                    )
                }
            }
            val listState = rememberLazyListState()

            Box(modifier = Modifier
                .fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) {
                    val totalItems = fishermanSummaries.size
                    itemsIndexed(fishermanSummaries) { index, fisherman ->
                        FishermanItem(
                            fisherman = fisherman,
                            index = index,
                            totalItems = totalItems,
                            thumbnailFlow = viewModel.fishermanThumbnail(fisherman.fisherman.id),
                            photosFlow = viewModel.fishermanPhotos(fisherman.fisherman.id),
                            onClick = {
                                navigateToFishermanDetails(fisherman.fisherman.id)
                            },
                            onFishClick = { fishermanId, targetOnly ->
                                navigateToFishList(fishermanId, targetOnly)
                            },
                            onPhotoAdded = {
                                viewModel.addFishermanPhoto(fisherman.fisherman.id, it, true)
                            },
                            onPhotoTaken = {
                                viewModel.addFishermanPhoto(fisherman.fisherman.id, it, false)
                            },
                            onSetThumbnail = { photo ->
                                viewModel.setFishermanThumbnail(
                                    fishermanId = fisherman.fisherman.id,
                                    photoId = photo.id)
                            },
                            onPhotoDeleted = { photo ->
                                viewModel.deleteFishermanPhoto(fisherman.fisherman.id, photo.id)
                            },
                            onDelete = { fishermanToDelete = fisherman }
                        )
                    }
                }
                var isLeftAligned by remember { mutableStateOf(false) }

                VerticalScrollToItemBar(
                    state = listState,
                    imageVector = AppIcons.Default.Fisherman,
                    onToggleAlignment = { isLeftAligned = !isLeftAligned },
                    modifier = Modifier
                        .align(if (isLeftAligned) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp, horizontal = 0.dp)
                )
            }
        }

        if (showAddDialog) {
            AddFishermanDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { first, last, nick ->
                    val fisherman = Fisherman(
                        firstName = first.trim(),
                        lastName = last.trim(),
                        nickname = nick.trim()
                    )

                    viewModel.addFisherman(fisherman) {
                        // Do nothing on Success
                    }

                    showAddDialog = false
                }
            )
        }
    }

    // DELETE CONFIRMATION
    fishermanToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { fishermanToDelete = null },
            title = { Text("Delete Fisherman?") },
            text = { Text("""Are you sure you want to delete '${item.fisherman.fullName}'?

This cannot be undone.

If you delete ${item.fisherman.fullName}, they will be removed from all trips (${item.totalTrips}) and events.

Tackle boxes and fish (${item.fishCaught}) logged for ${item.fisherman.fullName} will also be deleted.""") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFisherman(item.fisherman)
                        fishermanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fishermanToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun SortingArrow(
    isReversed: Boolean,
    onClick: () -> Unit
) {
    // 1. Calculate the rotation angle based on the boolean state
    val rotationAngle by animateFloatAsState(
        targetValue = if (isReversed) 180f else 0f,
        label = "ArrowRotation"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.ArrowDownward, // Use a single base icon
            contentDescription = "Toggle Sort Direction",
            modifier = Modifier.rotate(rotationAngle), // 2. Apply the rotation
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
