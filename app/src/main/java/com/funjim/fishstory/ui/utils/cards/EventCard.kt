package com.funjim.fishstory.ui.utils.cards

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.utils.FishCaughtItem
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventItem(
    item: EventSummary,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    showPhotoPicker: Boolean = false,
    index: Int = 0,
    totalItems: Int = 0,
    onClick: () -> Unit,
    onFishClick: ((String, String, Boolean) -> Unit)? = null,
    onFishermanClick: (() -> Unit)? = null,
    onPhotoAdded: ((Uri) -> Unit)? = null,
    onPhotoTaken: ((Uri) -> Unit)? = null,
    onSetThumbnail: ((Photo) -> Unit)? = null,
    onPhotoDeleted: ((Photo) -> Unit)? = null,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())
    var showPhotos by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
    }
    val startString = dateTimeFormatter.format(Date(item.event.startTime))
    val endString = dateTimeFormatter.format(Date(item.event.endTime))

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    var menuExpanded by remember { mutableStateOf(false) }

    val hasMenuActions = (onPhotoAdded != null) || (onPhotoTaken != null) ||
            (onSelectLocation != null) || (onSetLocation != null) ||
            (onUseTripLocation != null) || (onClearLocation != null) ||
            (onDelete != null)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { if (hasMenuActions) menuExpanded = true }
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
                    imageVector = AppIcons.Default.CanoeEmpty,
                    modifier = Modifier.size(64.dp),
                    onClick =
                        if (showPhotoPicker) {
                            { showPhotos = !showPhotos }
                        } else null
                )

                Spacer(modifier = Modifier.width(8.dp))
                val currentEvent = item.event
                val currentTrip = item.trip

                val eventLat = currentEvent.latitude
                val tripLat = currentTrip.latitude

                // Precedence logic: Use Event if it exists, otherwise use Trip
                val activeLat = eventLat ?: tripLat
                val activeLng = currentEvent.longitude ?: currentTrip.longitude

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.event.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (activeLat != null && activeLng != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View on map",
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
                                    .alpha(if (eventLat == null) 0.6f else 1f )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds space between icon and text
                    ) {
                        Text(
                            "$startString",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryContentColor
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            modifier = Modifier.size(16.dp),
                            tint = secondaryContentColor
                        )
                        Text(
                            "$endString",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryContentColor
                        )
                    }

                    if (item.fishermanCount > 0) {
                        if (verbose) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Fisherman,
                                    value = item.fishermanCount.toString(),
                                    description = if (item.fishermanCount == 1) "Fisherman" else "Fishermen",
                                    onClick = onFishermanClick,
                                    contentColor = secondaryContentColor
                                )
                            }

                            if (item.tackleBoxCount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.TackleBox,
                                        value = item.tackleBoxCount.toString(),
                                        description = if (item.tackleBoxCount == 1) "Tackle Box" else "Tackle Boxes",
                                        onClick = onFishermanClick,
                                        contentColor = secondaryContentColor
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Fisherman,
                                    value = item.fishermanCount.toString(),
                                    onClick = onFishermanClick,
                                    contentColor = secondaryContentColor
                                )

                                CardItemWithValue(
                                    icon = AppIcons.Default.TackleBox,
                                    value = item.tackleBoxCount.toString(),
                                    onClick = onFishermanClick,
                                    contentColor = secondaryContentColor
                                )
                            }
                        }
                    }

                    if (item.fishCaught != 0) {
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFishWithFins,
                            caughtCount = item.fishCaught,
                            keptCount = item.fishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = onFishClick?.let { onClick ->
                                { onClick(item.trip.id, item.event.id, false) }
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
                            extraText = if (verbose) "fish" else "",
                            onClick = onFishClick?.let { onClick ->
                                { onClick(item.trip.id, item.event.id, true) }
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                if (hasMenuActions) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Event options",
                                tint = contentColor
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
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

                            if (onSetLocation != null) {
                                DropdownMenuItem(
                                    text = { Text("Use Current Location") },
                                    onClick = {
                                        menuExpanded = false
                                        onSetLocation()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint =
                                                if (activeLat != null) Color(0xFF4CAF50)
                                                else LocalContentColor.current
                                        )
                                    }
                                )
                            }

                            if (onSelectLocation != null) {
                                DropdownMenuItem(
                                    text = { Text("Select on Map") },
                                    onClick = {
                                        menuExpanded = false
                                        onSelectLocation()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint =
                                                if (activeLat != null) Color(0xFF4CAF50)
                                                else LocalContentColor.current
                                        )
                                    }
                                )
                            }

                            if (eventLat != null && onClearLocation != null) {
                                DropdownMenuItem(
                                    text = {
                                        if (tripLat == null) Text("Clear Location")
                                        else Text("Reset Location")
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onClearLocation()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                            if (onDelete != null) {
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
                }
            }

            AnimatedVisibility(visible = showPhotos) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = borderColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    PhotoPickerRow(
                        photos = photos,
                        onPhotoSelected = { uri -> if (onPhotoAdded != null) onPhotoAdded(uri) },
                        onPhotoTaken = { uri -> if (onPhotoTaken != null) onPhotoTaken(uri) },
                        onSetThumbnail = { photo -> if (onSetThumbnail != null) onSetThumbnail(photo) },
                        onPhotoDeleted = { photo -> if (onPhotoDeleted != null) onPhotoDeleted(photo) }
                    )
                }
            }
        }
    }
}