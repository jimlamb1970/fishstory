package com.funjim.fishstory.ui.utils

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.SegmentSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SegmentItem(
    segmentSummary: SegmentSummary,
    onEdit: (() -> Unit)? = null, 
    onDelete: () -> Unit, 
    onClick: () -> Unit,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null
) {
    SegmentItem(
        segment = segmentSummary.segment,
        fishermenCount = segmentSummary.fishermanCount,
        fishCaught = segmentSummary.fishCaught,
        fishKept = segmentSummary.fishKept,
        onEdit = onEdit,
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
    segment: Segment,
    fishermenCount: Int = 0,
    fishCaught: Int = 0,
    fishKept: Int = 0,
    onEdit: (() -> Unit)? = null, 
    onDelete: () -> Unit, 
    onClick: () -> Unit,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null
) {
    val dateTimeFormatter = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(segment.name, style = MaterialTheme.typography.titleMedium)
                    if (segment.latitude != null && segment.longitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val mapUri =
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${segment.latitude},${segment.longitude}")
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
                Text(
                    text = "Started: ${dateTimeFormatter.format(Date(segment.startTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$fishermenCount Fisherman • $fishCaught Caught • $fishKept Kept",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
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
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (segment.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
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
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (segment.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
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
                                        tint = if (segment.latitude != null) Color(0xFF4CAF50) else LocalContentColor.current
                                    )
                                }
                            )
                        }

                        if (segment.latitude != null && onClearLocation != null) {
                            DropdownMenuItem(
                                text = { Text("Clear Location", color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    onClearLocation()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
