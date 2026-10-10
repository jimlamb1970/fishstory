package com.funjim.fishstory.ui.utils

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class TripAction {
    data class View(val tripSummary: TripSummary) : TripAction()
    data class Menu(val tripSummary: TripSummary) : TripAction()
    data class OpenMap(val lat: Double, val lng: Double) : TripAction()
    data class UseCurrentLocation(val tripSummary: TripSummary) : TripAction()
    data class SelectLocation(val tripSummary: TripSummary) : TripAction()
    data class ClearLocation(val tripSummary: TripSummary) : TripAction()
    data class SelectPhoto(val tripSummary: TripSummary) : TripAction()
    data class TakePhoto(val tripSummary: TripSummary) : TripAction()
    data class Delete(val tripSummary: TripSummary) : TripAction()
}

@Composable
fun EditTripDialog(
    item: Trip,
    onConfirm: (Trip) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var origName by remember { mutableStateOf(item.name) }
    var origStart by remember { mutableLongStateOf(item.startDate) }
    var origEnd by remember { mutableLongStateOf(item.endDate) }

    var name by remember { mutableStateOf(origName) }
    var startTime by remember { mutableStateOf(origStart) }
    var endTime by remember { mutableStateOf(origEnd) }

    val isChanged = name != origName ||
            startTime != origStart ||
            endTime != origEnd

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Start", style = MaterialTheme.typography.labelLarge)
                DateTimePickerButton(
                    label = "Start",
                    millis = startTime,
                    modifier = Modifier.fillMaxWidth()
                ) { newTime ->
                    startTime = newTime
                    if (startTime > endTime)
                        endTime = startTime
                }

                Text("End", style = MaterialTheme.typography.labelLarge)
                DateTimePickerButton(
                    label = "End",
                    millis = endTime,
                    modifier = Modifier.fillMaxWidth()
                ) { newTime ->
                    if (newTime < startTime) {
                        Toast.makeText(
                            context,
                            "End date and time must be after start date and time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        endTime = newTime
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(item.copy(
                        name = name.trim(),
                        startDate = startTime,
                        endDate = endTime)
                    )
                },
               enabled = name.isNotBlank() && isChanged
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TripItemWithMenu(
    tripSummary: TripSummary,
    verbose: Boolean,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean = true,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    onNavigateToDetails: (String, CategoryType) -> Unit,
    onThumbnailLongClick: (() -> Unit)? = null,
    onEventClick: (() -> Unit)? = null,
    onFishClick: ((String, Boolean) -> Unit)? = null,
    onFishermanClick: (() -> Unit)? = null,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onSetThumbnail: (Photo) -> Unit,
    onPhotoDeleted: (Photo) -> Unit,
    onAction: (TripAction) -> Unit,
    showMenu: Boolean,
    onMenuDismiss: () -> Unit
) {
    var showPhotos by remember { mutableStateOf(false) }

    TripItem(
        trip = tripSummary,
        verbose = verbose,
        index = index,
        totalItems = totalItems,
        modifier = modifier,
        thumbnailFlow = thumbnailFlow,
        photosFlow = photosFlow,
        allowPhotos = true,
        showPhotos = showPhotos,
        onClick = { onNavigateToDetails(tripSummary.trip.id, CategoryType.TARGET_SPECIES) },
        onLongClick = { onAction(TripAction.Menu(tripSummary)) },
        onThumbnailLongClick = onThumbnailLongClick,
        onEventClick = onEventClick,
        onFishClick = onFishClick,
        onFishermanClick = onFishermanClick,
        onPhotoClick = { showPhotos = !showPhotos },
        onPhotoAdded = onPhotoAdded,
        onPhotoTaken = onPhotoTaken,
        onSetThumbnail = onSetThumbnail,
        onPhotoDeleted = onPhotoDeleted,
        onAction = onAction,
        actions = {
            Box {
                IconButton(onClick = { onAction(TripAction.Menu(tripSummary)) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Trip options"
                    )
                }

                TripMenu(
                    expanded = showMenu,
                    onDismiss = onMenuDismiss
                ) {
                    DropdownMenuItem(
                        text = {
                            if (showPhotos) Text("Hide Photos")
                            else Text("Show Photos") },
                        onClick = {
                            onMenuDismiss()
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

                    // Centralized Menu Actions
                    val lat = tripSummary.trip.latitude
                    if (hasLocationPermission) {
                        DropdownMenuItem(
                            text = { Text("Use Current Location") },
                            onClick = {
                                onMenuDismiss()
                                onAction(TripAction.UseCurrentLocation(tripSummary))
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.MyLocation,
                                    null,
                                    tint =
                                        if (lat != null) Color(0xFF4CAF50)
                                        else LocalContentColor.current
                                )
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Select on Map") },
                        onClick = {
                            onMenuDismiss()
                            onAction(TripAction.SelectLocation(tripSummary))
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Map,
                                null,
                                tint =
                                    if (lat != null) Color(0xFF4CAF50)
                                    else LocalContentColor.current
                            )
                        }
                    )
                    if (lat != null) {
                        DropdownMenuItem(
                            text = { Text("Clear Location") },
                            onClick = {
                                onMenuDismiss()
                                onAction(TripAction.ClearLocation(tripSummary))
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOff,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onMenuDismiss()
                            onAction(TripAction.Delete(tripSummary))
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun TripItem(
    trip: TripSummary,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    allowPhotos: Boolean = false,
    showPhotos: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onThumbnailLongClick: (() -> Unit)? = null,
    onEventClick: (() -> Unit)? = null,
    onFishClick: ((String, Boolean) -> Unit)? = null,
    onFishermanClick: (() -> Unit)? = null,
    onPhotoClick: (() -> Unit)? = null,
    onPhotoAdded: ((Uri) -> Unit)? = null,
    onPhotoTaken: ((Uri) -> Unit)? = null,
    onSetThumbnail: ((Photo) -> Unit)? = null,
    onPhotoDeleted: ((Photo) -> Unit)? = null,
    onAction: (TripAction) -> Unit,
    actions: @Composable () -> Unit = {}
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    val startString = dateTimeFormatter.format(Date(trip.trip.startDate))
    val endString = dateTimeFormatter.format(Date(trip.trip.endDate))

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
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
                    imageVector = AppIcons.Default.Boat,
                    modifier = Modifier.size(64.dp),
                    onClick =
                        if (allowPhotos) { onPhotoClick }
                        else null,
                    onLongClick = onThumbnailLongClick
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            trip.trip.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (trip.trip.latitude != null && trip.trip.longitude != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View on map",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        onAction(
                                            TripAction.OpenMap(
                                                trip.trip.latitude,
                                                trip.trip.longitude
                                            )
                                        )
                                    }
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
                            tint = secondaryContentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "$endString",
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryContentColor
                        )
                    }

                    val eventCount = trip.eventCount
                    val fishermanCount = trip.fishermanCount
                    val tackleBoxCount = trip.tackleBoxCount

                    if (verbose) {
                        if (eventCount != 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.CanoeEmpty,
                                    value = eventCount.toString(),
                                    description = if (eventCount > 1) "Events" else "Event",
                                    onClick = onEventClick,
                                    contentColor = secondaryContentColor
                                )
                            }
                        }

                        if (fishermanCount >= 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Fisherman,
                                    value = fishermanCount.toString(),
                                    description = if (fishermanCount == 1) "Fisherman" else "Fishermen",
                                    onClick = onFishermanClick,
                                    contentColor = secondaryContentColor
                                )
                            }

                            if (tackleBoxCount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.TackleBox,
                                        value = tackleBoxCount.toString(),
                                        description = if (tackleBoxCount == 1) "Tackle Box" else "Tackle Boxes",
                                        onClick = onFishermanClick,
                                        contentColor = secondaryContentColor
                                    )
                                }
                            }
                        }
                    } else {
                        if (eventCount != 0 || fishermanCount != -1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (eventCount != 0) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.CanoeEmpty,
                                        value = eventCount.toString(),
                                        onClick = onEventClick,
                                        contentColor = secondaryContentColor
                                    )
                                }

                                if (fishermanCount != -1) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.Fisherman,
                                        value = fishermanCount.toString(),
                                        onClick = onFishermanClick,
                                        contentColor = secondaryContentColor
                                    )

                                    CardItemWithValue(
                                        icon = AppIcons.Default.TackleBox,
                                        value = tackleBoxCount.toString(),
                                        onClick = onFishermanClick,
                                        contentColor = secondaryContentColor
                                    )
                                }
                            }
                        }
                    }

                    // Verbose or not, the fish and target fish appear on their own lines
                    if (trip.fishCaught != 0) {
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFishWithFins,
                            caughtCount = trip.fishCaught,
                            keptCount = trip.fishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = onFishClick?.let { onClick ->
                                { onClick(trip.trip.id, false) }
                            },
                            contentColor = secondaryContentColor
                        )
                    }

                    if (trip.targetFishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.TargetFish,
                            description = "Target Fish Caught",
                            caughtCount = trip.targetFishCaught,
                            keptCount = trip.targetFishKept,
                            extraText = if (verbose) "target fish" else "",
                            onClick = onFishClick?.let { onClick ->
                                { onClick(trip.trip.id, true) }
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                actions()
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

@Composable
fun TripMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        content = content
    )
}