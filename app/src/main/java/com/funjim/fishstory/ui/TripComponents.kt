package com.funjim.fishstory.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
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
    onClick: () -> Unit,
    onAction: (TripAction) -> Unit,
    actions: @Composable () -> Unit = {}
) {
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val startString = dateTimeFormatter.format(Date(trip.trip.startDate))
    val endString = dateTimeFormatter.format(Date(trip.trip.endDate))

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }) {
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

            actions()
        }
    }
}

@Composable
fun TripMenu(
    expanded: Boolean,
    onMenuClick: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            content = content
        )
    }
}
