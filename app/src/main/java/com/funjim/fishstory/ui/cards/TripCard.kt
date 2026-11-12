package com.funjim.fishstory.ui.cards

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
import com.funjim.fishstory.model.LureWithColorsSummary
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.FishCaughtItem
import com.funjim.fishstory.ui.utils.LureCompositionWithColors
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
fun TripCardWithMenu(
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
    onFishClick: ((String, String?, Boolean) -> Unit)? = null,
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

    TripCard(
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
                                "Delete Trip",
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
fun TripCard(
    trip: TripSummary,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    lureCount: Int = 0,
    lures: List<LureWithColorsSummary> = emptyList(),
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    allowPhotos: Boolean = false,
    showPhotos: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onThumbnailLongClick: (() -> Unit)? = null,
    onEventClick: (() -> Unit)? = null,
    onFishClick: ((String, String?, Boolean) -> Unit)? = null,
    onFishermanClick: (() -> Unit)? = null,
    onPhotoClick: (() -> Unit)? = null,
    onPhotoAdded: ((Uri) -> Unit)? = null,
    onPhotoTaken: ((Uri) -> Unit)? = null,
    onSetThumbnail: ((Photo) -> Unit)? = null,
    onPhotoDeleted: ((Photo) -> Unit)? = null,
    onAction: (TripAction) -> Unit,
    actions: @Composable () -> Unit = {}
) {
    val context = LocalContext.current

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

    var tackleBoxOpen by remember { mutableStateOf(false) }

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
                        if (allowPhotos) {
                            onPhotoClick
                        } else null,
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

                                    if (tackleBoxCount > 0) {
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
                    }

                    if (lureCount != 0) {
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
                                        tackleBoxOpen = !tackleBoxOpen
                                    }
                            )

                            Text(
                                text = "<Smart Tackle Box>",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
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
                                    if (lureCount > 0) {
                                        Toast.makeText(
                                            context,
                                            "Lures cannot be added to a 'Smart Tackle Box'",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentColor = secondaryContentColor
                            )
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
                                { onClick(trip.trip.id, null, false) }
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
                                { onClick(trip.trip.id, null, true) }
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                actions()
            }

            AnimatedVisibility(visible = tackleBoxOpen) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(color = borderColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (lures.isNotEmpty()) {
                        lures.forEach { lure ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 50.dp)
                                ) {
                                    LureCompositionWithColors(
                                        name = "• ${lure.lure.name}",
                                        lure.primaryColors,
                                        lure.secondaryColors,
                                        lure.lure.glows,
                                        lure.glowColors,
                                        style = MaterialTheme.typography.bodyMedium,
                                        contentColor = getOnCardSecondaryColor(),
                                        colorBadgeSize = 20.dp
                                    )
                                    if (lure.fishCaught > 0) {
                                        FishCaughtItem(
                                            icon = AppIcons.Default.LeapingFishWithFins,
                                            caughtCount = lure.fishCaught,
                                            keptCount = lure.fishKept,
                                            extraText = if (verbose) "fish" else "",
                                            modifier = Modifier.padding(start = 8.dp),
                                            onClick = onFishClick?.let { onClick ->
                                                { onClick(trip.trip.id, lure.lure.id, false) }
                                            },
                                            contentColor = getOnCardSecondaryColor()
                                        )
                                    }
                                    if (lure.targetFishKept > 0) {
                                        FishCaughtItem(
                                            icon = AppIcons.Default.TargetFish,
                                            caughtCount = lure.targetFishCaught,
                                            keptCount = lure.targetFishKept,
                                            extraText = if (verbose) "target fish" else "",
                                            modifier = Modifier.padding(start = 8.dp),
                                            onClick = onFishClick?.let { onClick ->
                                                { onClick(trip.trip.id, lure.lure.id, true) }
                                            },
                                            contentColor = getOnCardSecondaryColor()
                                        )
                                    }
                                }
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