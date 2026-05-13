package com.funjim.fishstory.ui.utils

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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.theme.AppIcons
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
    data class Delete(val tripSummary: TripSummary) : TripAction()
}

@Composable
fun TripItemWithMenu(
    tripSummary: TripSummary,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
    onNavigateToDetails: (String) -> Unit,
    onFetchThumbnail: suspend (String) -> ByteArray?,
    onAction: (TripAction) -> Unit,
    showMenu: Boolean,
    onMenuDismiss: () -> Unit
) {
    TripItem(
        trip = tripSummary,
        index = index,
        totalItems = totalItems,
        modifier = modifier,
        onClick = { onNavigateToDetails(tripSummary.trip.id) },
        onLongClick = { onAction(TripAction.Menu(tripSummary)) },
        onFetchThumbnail = onFetchThumbnail,
        onAction = onAction
    ) {
        TripMenu(
            expanded = showMenu,
            onDismiss = onMenuDismiss
        ) {
            // Centralized Menu Actions
            val lat = tripSummary.trip.latitude
            DropdownMenuItem(
                text = { Text("Use Current Location") },
                onClick = { onAction(TripAction.UseCurrentLocation(tripSummary)) },
                leadingIcon = { Icon(Icons.Default.MyLocation, null, tint = if (lat != null) Color(0xFF4CAF50) else LocalContentColor.current) }
            )
            DropdownMenuItem(
                text = { Text("Select on Map") },
                onClick = { onAction(TripAction.SelectLocation(tripSummary)) },
                leadingIcon = { Icon(Icons.Default.Map, null, tint = if (lat != null) Color(0xFF4CAF50) else LocalContentColor.current) }
            )
            if (lat != null) {
                DropdownMenuItem(
                    text = { Text("Clear Location") },
                    onClick = { onAction(TripAction.ClearLocation(tripSummary)) },
                    leadingIcon = { Icon(Icons.Default.LocationOff, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { onAction(TripAction.Delete(tripSummary)) },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            )
        }
    }
}

@Composable
fun TripItem(
    trip: TripSummary,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFetchThumbnail: suspend (String) -> ByteArray?,
    onAction: (TripAction) -> Unit,
    actions: @Composable () -> Unit = {}
) {
    var thumbnail by remember { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(trip.trip.id) {
        thumbnail = onFetchThumbnail(trip.trip.id)
    }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    val startString = dateTimeFormatter.format(Date(trip.trip.startDate))
    val endString = dateTimeFormatter.format(Date(trip.trip.endDate))

    val backgroundColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }
    val borderColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThumbnailBox(
                thumbnail = thumbnail,
                imageVector = AppIcons.Default.Boat
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trip.trip.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (trip.trip.latitude != null && trip.trip.longitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Arrow",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "$endString",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val eventCount = trip.eventCount
                val fishermanCount = trip.fishermanCount
                val tackleBoxCount = trip.tackleBoxCount
                if (eventCount != 0 || fishermanCount != -1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (eventCount != 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.Default.Boat,
                                    contentDescription = "Event",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "$eventCount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        if (fishermanCount != 1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.Default.Fisherman,
                                    contentDescription = "Fishermen count",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = fishermanCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.Default.TackleBox,
                                    contentDescription = "Tackle Box count",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = tackleBoxCount.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                val caughtCount = trip.fishCaught
                val keptCount = trip.fishKept
                val now = System.currentTimeMillis()
                if (caughtCount != 0  || now >= trip.trip.startDate) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds space between icon and text
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Default.LeapingFish,
                                contentDescription = "Fish",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            BoldingNumbersText(
                                text = "Kept $keptCount of $caughtCount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            actions()
        }
    }
}

@Composable
fun TripMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            content = content
        )
    }
}
