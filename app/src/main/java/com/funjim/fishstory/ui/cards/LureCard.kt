package com.funjim.fishstory.ui.cards

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.LureWithColorsSummary
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.utils.FishCaughtItem
import com.funjim.fishstory.ui.utils.LureColorComposition
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LureItem(
    item: LureWithColorsSummary,
    verbose: Boolean,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    index: Int = 0,
    totalItems: Int = 0,
    onFishClick: ((String, Boolean) -> Unit)? = null,
    onEdit: () -> Unit,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onSetThumbnail: (Photo) -> Unit,
    onPhotoDeleted: (Photo) -> Unit,
    onDelete: () -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())

    var showPhotos by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

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
                onClick = { showPhotos = !showPhotos },
                onLongClick = { menuExpanded = true }
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.Lure,
                    modifier = Modifier.size(64.dp),
                    onClick = { showPhotos = !showPhotos }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.lure.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    LureColorComposition(
                        primary = item.primaryColors,
                        secondary = item.secondaryColors,
                        glows = item.lure.glows,
                        glow = item.glowColors
                    )

                    if (item.lure.hookCount > 0) {
                        CardItemWithValue(
                            icon =
                                if (item.lure.hookCount == 1) AppIcons.Default.Hook
                                else AppIcons.Default.Hooks,
                            value = item.lure.hookCount.toString(),
                            description =
                                if (verbose) {
                                    if (item.lure.hookCount == 1) "Hook"
                                    else "Hooks"
                                } else "",
                            contentColor = secondaryContentColor
                        )
                    }

                    if (item.fishCaught != 0) {
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFishWithFins,
                            caughtCount = item.fishCaught,
                            keptCount = item.fishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = onFishClick?.let { onClick ->
                                { onClick(item.lure.id, false) }
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
                            onClick = onFishClick?.let { onClick ->
                                { onClick(item.lure.id, true) }
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Lure options",
                            tint = contentColor
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                if (showPhotos) Text("Hide Photos")
                                else Text("Show Photos") },
                            onClick = {
                                menuExpanded = false
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
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
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

