package com.funjim.fishstory.ui.utils.cards

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.BodyOfWaterSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.FishCaughtItem
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import kotlinx.coroutines.flow.Flow

@Composable
fun BodyOfWaterSummaryCard(
    item: BodyOfWaterSummary,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    thumbnailFlow: Flow<ByteArray?>,
    index: Int = 0,
    totalItems: Int = 0,
    onClick: (BodyOfWater) -> Unit,
    onFishClick: (BodyOfWater, Boolean) -> Unit,
    onSetThumbnail: (BodyOfWater) -> Unit,
    onClearThumbnail: (BodyOfWater) -> Unit,
    onDelete: (BodyOfWater) -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    var menuExpanded by remember { mutableStateOf(false) }
    var thumbnailMenuExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick(item.bodyOfWater) },
                onLongClick = { menuExpanded = true }
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
                Box(
                    modifier = Modifier.combinedClickable(
                        onClick = { /* Do nothing */ },
                        onLongClick = {
                            thumbnailMenuExpanded = true
                        }
                    )
                ) {
                    ThumbnailBox(
                        thumbnail = thumbnail,
                        modifier = Modifier.size(48.dp),
                        imageVector = AppIcons.Default.BodyOfWater
                    )

                    DropdownMenu(
                        expanded = thumbnailMenuExpanded,
                        onDismissRequest = { thumbnailMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                if (thumbnail != null) Text("Update Thumbnail")
                                else Text("Select Thumbnail")
                            },
                            onClick = {
                                thumbnailMenuExpanded = false
                                onSetThumbnail(item.bodyOfWater)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription =
                                        null
                                )
                            }
                        )
                        if (thumbnail != null) {
                            DropdownMenuItem(
                                text = { Text("Reset Thumbnail") },
                                onClick = {
                                    thumbnailMenuExpanded = false
                                    onClearThumbnail(item.bodyOfWater)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.HideImage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.bodyOfWater.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (item.fishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFishWithFins,
                            caughtCount = item.fishCaught,
                            keptCount = item.fishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = {
                                onFishClick(item.bodyOfWater, false)
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
                                onFishClick(item.bodyOfWater, true)
                            },
                            contentColor = secondaryContentColor
                        )
                    }
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
                            text = {
                                if (thumbnail != null) Text("Update Thumbnail")
                                else Text("Select Thumbnail")
                            },
                            onClick = {
                                menuExpanded = false
                                onSetThumbnail(item.bodyOfWater)
                            },
                            leadingIcon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) }
                        )
                        if (thumbnail != null) {
                            DropdownMenuItem(
                                text = {
                                    Text("Reset Thumbnail")
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClearThumbnail(item.bodyOfWater)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.HideImage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Remove") },
                            onClick = {
                                menuExpanded = false
                                onDelete(item.bodyOfWater)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}