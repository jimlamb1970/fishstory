package com.funjim.fishstory.ui.utils

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO -- update this card to work like TripItem card with regards to menu
@Composable
fun SegmentItem(
    eventSummary: EventSummary,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null
) {
    SegmentItem(
        event = eventSummary.event,
        modifier = modifier,
        index = index,
        totalItems = totalItems,
        fishermenCount = eventSummary.fishermanCount,
        tackleBoxCount = eventSummary.tackleBoxCount,
        fishCaught = eventSummary.fishCaught,
        fishKept = eventSummary.fishKept,
        onDelete = onDelete,
        onClick = onClick,
        onSetLocation = onSetLocation,
        onSelectLocation = onSelectLocation,
        onUseTripLocation = onUseTripLocation,
        onClearLocation = onClearLocation
    )
}

@Composable
fun SegmentItem(
    event: Event,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    fishermenCount: Int = 0,
    tackleBoxCount: Int = 0,
    fishCaught: Int = 0,
    fishKept: Int = 0,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null
) {
    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val now = System.currentTimeMillis()

    val startString = dateTimeFormatter.format(Date(event.startTime))
    val endString = dateTimeFormatter.format(Date(event.endTime))

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

    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    OutlinedCard(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.name,
                        style = MaterialTheme.typography.titleLarge)
                    if (event.latitude != null && event.longitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val mapUri =
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${event.latitude},${event.longitude}")
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
                        )
                    }
                }

                Text("$startString  →  $endString",
                    style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$fishermenCount ${if (fishermenCount == 1) "fisherman" else "fishermen"} • " +
                            "$tackleBoxCount with a tacklebox",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )

                if (fishCaught != 0 || now >= event.startTime) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fish Summary • $fishCaught Caught • $fishKept Kept",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if ((onSelectLocation != null) ||
                (onSetLocation != null) ||
                (onUseTripLocation != null) ||
                (onClearLocation != null) ||
                (onDelete != null)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
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
                                                if (event.latitude != null) Color(0xFF4CAF50)
                                                else LocalContentColor.current
                                        )
                                    }
                                )
                            }

                            if (onSelectLocation != null) {
                                DropdownMenuItem(
                                    text = { Text("Select Location") },
                                    onClick = {
                                        menuExpanded = false
                                        onSelectLocation()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint =
                                                if (event.latitude != null) Color(0xFF4CAF50)
                                                else LocalContentColor.current
                                        )
                                    }
                                )
                            }

                            if (onUseTripLocation != null) {
                                DropdownMenuItem(
                                    text = { Text("Use Trip Location") },
                                    onClick = {
                                        menuExpanded = false
                                        onUseTripLocation()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint =
                                                if (event.latitude != null) Color(0xFF4CAF50)
                                                else LocalContentColor.current
                                        )
                                    }
                                )
                            }

                            if (event.latitude != null && onClearLocation != null) {
                                DropdownMenuItem(
                                    text = { Text("Clear Location") },
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
        }
    }
}
