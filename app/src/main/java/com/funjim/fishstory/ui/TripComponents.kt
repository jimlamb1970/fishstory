package com.funjim.fishstory.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.TripSummary
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
fun TripItem(
    trip: TripSummary,
    isMenuExpanded: Boolean,
    onAction: (TripAction) -> Unit,
    onDismissMenu: () -> Unit
) {
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val startString = dateTimeFormatter.format(Date(trip.trip.startDate))
    val endString = dateTimeFormatter.format(Date(trip.trip.endDate))

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onAction(TripAction.View(trip)) }) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trip.trip.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black)
                    if (trip.trip.latitude != null && trip.trip.longitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    onAction(TripAction.OpenMap(trip.trip.latitude, trip.trip.longitude))
                                }
                        )
                    }
                }
                Text("$startString  →  $endString",
                    style = MaterialTheme.typography.bodyMedium)

                val fishermanCount = trip.fishermanCount
                val caughtCount = trip.totalCaught
                val keptCount = trip.totalKept

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$fishermanCount ${if (fishermanCount == 1) "fisherman" else "fishermen"} • $caughtCount Caught • $keptCount Kept",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box {
                IconButton(onClick = { onAction(TripAction.Menu(trip)) }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    // We will move the MenuItems into a reusable function
                    // so we don't bloat the TripItem file.
                    TripMenuContent(
                        trip = trip,
                        onAction = onAction,
                        onDismiss = onDismissMenu
                    )
                }
            }
        }
    }
}

@Composable
fun TripMenuContent(
    trip: TripSummary,
    onAction: (TripAction) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = { Text("Use Current Location") },
        onClick = {
            onDismiss()
            onAction(TripAction.UseCurrentLocation(trip))
        },
        leadingIcon = {
            Icon(Icons.Default.MyLocation,
                contentDescription = null,
                tint = if (trip.trip.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
        }
    )

    DropdownMenuItem(
        text = { Text("Select Location") },
        onClick = {
            onDismiss()
            onAction(TripAction.SelectLocation(trip))
        },
        leadingIcon = {
            Icon(Icons.Default.Map,
                contentDescription = null,
                tint = if (trip.trip?.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current)
        }
    )

    if (trip.trip.latitude != null) {
        DropdownMenuItem(
            text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
            onClick = {
                onDismiss()
                onAction(TripAction.ClearLocation(trip))
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

    DropdownMenuItem(
        text = { Text("Delete") },
        onClick = {
            onDismiss()
            onAction(TripAction.Delete(trip))
        },
        leadingIcon = {
            Icon(Icons.Default.Delete, contentDescription = null)
        }
    )
}
