package com.funjim.fishstory.ui.utils.cards

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.utils.FishCaughtItem
import com.funjim.fishstory.ui.utils.LureCompositionWithColors
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.crud.AddTackleBoxDialog
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.selection.TackleBoxBottomSheet
import com.funjim.fishstory.ui.utils.sortLures
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FishermanItem(
    fisherman: FishermanSummary,
    verbose: Boolean,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    onClick: () -> Unit,
    onFishClick: (String, Boolean) -> Unit,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onSetThumbnail: (Photo) -> Unit,
    onPhotoDeleted: (Photo) -> Unit,
    onDelete: () -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())
    var showPhotos by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { expanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.Fisherman,
                    modifier = Modifier.size(64.dp),
                    onClick = { showPhotos = !showPhotos }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        fisherman.fisherman.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (fisherman.totalTrips != 0 || fisherman.totalTackleBoxes != 0) {
                        if (verbose) {
                            if (fisherman.totalTrips != 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.Boat,
                                        value = fisherman.totalTrips.toString(),
                                        description = if (fisherman.totalTrips == 1) "Trip" else "Trips",
                                        contentColor = secondaryContentColor
                                    )
                                }
                            }
                            if (fisherman.totalTackleBoxes != 0) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.TackleBox,
                                    value = fisherman.totalTackleBoxes.toString(),
                                    description =
                                        if (fisherman.totalTackleBoxes == 1) "Tackle Box"
                                        else "Tackle Boxes",
                                    contentColor = secondaryContentColor
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (fisherman.totalTrips != 0) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.Boat,
                                        value = fisherman.totalTrips.toString(),
                                        contentColor = secondaryContentColor
                                    )
                                }
                                if (fisherman.totalTackleBoxes != 0) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.TackleBox,
                                        value = fisherman.totalTackleBoxes.toString(),
                                        contentColor = secondaryContentColor
                                    )
                                }
                            }
                        }
                    }

                    if (fisherman.fishCaught != 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FishCaughtItem(
                                icon = AppIcons.Default.LeapingFishWithFins,
                                caughtCount = fisherman.fishCaught,
                                keptCount = fisherman.fishKept,
                                extraText = if (verbose) "fish" else "",
                                onClick = { onFishClick(fisherman.fisherman.id, false) },
                                contentColor = secondaryContentColor,
                            )
                        }
                    }

                    if (fisherman.targetFishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FishCaughtItem(
                                icon = AppIcons.Default.TargetFish,
                                caughtCount = fisherman.targetFishCaught,
                                keptCount = fisherman.targetFishKept,
                                extraText = if (verbose) "target fish" else "",
                                onClick = { onFishClick(fisherman.fisherman.id, true) },
                                contentColor = secondaryContentColor
                            )
                        }
                    }

                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Fisherman options",
                            tint = contentColor
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                if (showPhotos) Text("Hide Photos")
                                else Text("Show Photos") },
                            onClick = {
                                expanded = false
                                showPhotos = !showPhotos
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
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showPhotos) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = borderColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    PhotoPickerRow(
                        photos = photos,
                        onPhotoSelected = { uri -> onPhotoAdded(uri) },
                        onPhotoTaken = { uri -> onPhotoTaken(uri) },
                        onSetThumbnail = { photo -> onSetThumbnail(photo) },
                        onPhotoDeleted = { photo -> onPhotoDeleted(photo) }
                    )
                }
            }
        }
    }
}

@Composable
fun FishermanSummaryCard(
    item: FishermanSummary,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    selectedTackleBoxId: String?,
    availableBoxes: List<TackleBox>,
    lureCount: Int,
    lures: List<LureWithColors>,
    onAddTackleBox: (TackleBox) -> Unit,
    onAddLuresToTackleBox: (TackleBox) -> Unit,
    onTackleBoxSelected: (Fisherman, String?) -> Unit,
    onClick: (Fisherman) -> Unit,
    onFishClick: (Fisherman, Boolean) -> Unit,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onSetThumbnail: (Photo) -> Unit,
    onPhotoDeleted: (Photo) -> Unit,
    onDelete: ((Fisherman) -> Unit)? = null
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    var expanded by remember { mutableStateOf(false) }

    val selectedTackleBox = availableBoxes.find { it.id == selectedTackleBoxId }
    var tackleBoxOpen by remember { mutableStateOf(false) }
    var showAddTackleBoxDialog by remember { mutableStateOf(false) }
    var showTackleBoxSelection by remember { mutableStateOf(false) }
    var luresExpanded by remember { mutableStateOf(false) }

    val sortedLures by remember(lures) { derivedStateOf { sortLures(lures) } }

    var showPhotos by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick(item.fisherman) },
                onLongClick = { expanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThumbnailBox(
                    thumbnail = thumbnail,
                    modifier = Modifier.size(48.dp),
                    imageVector = AppIcons.Default.Fisherman,
                    onClick = {
                        if (!showPhotos) {
                            tackleBoxOpen = false
                            luresExpanded = false
                        }
                        showPhotos = !showPhotos
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.fisherman.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector =
                                if (tackleBoxOpen) AppIcons.Default.TackleBox
                                else AppIcons.Default.TackleBoxClosed,
                            contentDescription = "Tackle Box",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable() {
                                    if (selectedTackleBox != null) {
                                        tackleBoxOpen = !tackleBoxOpen
                                        if (tackleBoxOpen) {
                                            showPhotos = false
                                        }
                                        luresExpanded = tackleBoxOpen
                                    } else {
                                        showTackleBoxSelection = true
                                    }
                                }
                        )

                        Text(
                            text = selectedTackleBox?.name ?: "No Tackle Box selected",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    if (selectedTackleBox != null) {
                        if (lureCount != 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Lure,
                                    value = lureCount.toString(),
                                    description = if (verbose) {
                                        if (lureCount == 1) "Lure" else "Lures"
                                    } else null,
                                    onClick = {
                                        onAddLuresToTackleBox(selectedTackleBox)
                                    },
                                    contentColor = secondaryContentColor
                                )
                            }
                        }
                    }

                    if (item.fishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFishWithFins,
                            caughtCount = item.fishCaught,
                            keptCount = item.fishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = {
                                onFishClick(item.fisherman, false)
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                    if (item.targetFishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.TargetFish,
                            caughtCount = item.targetFishCaught,
                            keptCount = item.targetFishKept,
                            extraText = if (verbose) "target fish" else "",
                            onClick = {
                                onFishClick(item.fisherman, true)
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Fisherman options",
                            tint = contentColor
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                if (showPhotos) Text("Hide Photos")
                                else Text("Show Photos") },
                            onClick = {
                                expanded = false
                                if (!showPhotos) {
                                    tackleBoxOpen = false
                                    luresExpanded = false
                                }
                                showPhotos = !showPhotos
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
                            text = { Text("Select Tackle Box") },
                            onClick = {
                                expanded = false
                                showTackleBoxSelection = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = AppIcons.Default.TackleBoxClosed,
                                    contentDescription = "Select Tackle Box",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )

                        if (selectedTackleBox != null) {
                            DropdownMenuItem(
                                text = { Text("Add Lures") },
                                onClick = {
                                    expanded = false
                                    onAddLuresToTackleBox(selectedTackleBox)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = AppIcons.Default.Lure,
                                        contentDescription = "Add Lures",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )

                            if (lureCount > 0) {
                                DropdownMenuItem(
                                    text = {
                                        if (luresExpanded) Text("Hide Lures")
                                        else Text("Show Lures")
                                    },
                                    onClick = {
                                        expanded = false
                                        if (!luresExpanded) {
                                            showPhotos = false
                                            tackleBoxOpen = true
                                            luresExpanded = true
                                        } else {
                                            tackleBoxOpen = false
                                            luresExpanded = false
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (showPhotos) Icons.Default.VisibilityOff
                                            else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }

                        onDelete?.let {
                            DropdownMenuItem(
                                text = { Text("Remove") },
                                onClick = {
                                    expanded = false
                                    onDelete(item.fisherman)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (luresExpanded || showPhotos) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    thickness = 1.dp,
                    color = getOnCardColor()
                )
            }

            AnimatedVisibility(visible = luresExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (sortedLures.isNotEmpty()) {
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
                                    contentColor = getOnCardSecondaryColor(),
                                    modifier = Modifier.padding(start = 50.dp),
                                    colorBadgeSize = 20.dp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Tackle Box is empty",
                            style = MaterialTheme.typography.bodySmall,
                            color = getOnCardSecondaryColor(),
                            modifier = Modifier.padding(start = 50.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showPhotos) {
                PhotoPickerRow(
                    photos = photos,
                    onPhotoSelected = { uri -> onPhotoAdded(uri) },
                    onPhotoTaken = { uri -> onPhotoTaken(uri) },
                    onSetThumbnail = { photo -> onSetThumbnail(photo) },
                    onPhotoDeleted = { photo -> onPhotoDeleted(photo) }
                )
            }
        }
    }

    TackleBoxBottomSheet(
        showSheet = showTackleBoxSelection,
        items = availableBoxes,
        selectedItem = selectedTackleBox,
        onSelected = { tackleBox -> onTackleBoxSelected(item.fisherman, tackleBox.id) },
        onAdd = { showAddTackleBoxDialog = true },
        onClear = {
            tackleBoxOpen = false
            onTackleBoxSelected(item.fisherman, null)
        },
        onDismissRequest = { showTackleBoxSelection = false }
    )

    if (showAddTackleBoxDialog) {
        AddTackleBoxDialog(
            fishermanId = item.fisherman.id,
            onDismiss = { showAddTackleBoxDialog = false },
            onConfirm = { tackleBox ->
                onAddTackleBox(tackleBox)
                showAddTackleBoxDialog = false
            }
        )
    }
}
