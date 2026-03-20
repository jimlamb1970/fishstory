package com.funjim.fishstory.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.SegmentWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SegmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Segment") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Segment Name (e.g. Morning, Spot 1)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, startTime)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SegmentItem(
    segmentWithDetails: SegmentWithDetails, 
    onEdit: (() -> Unit)? = null, 
    onDelete: () -> Unit, 
    onClick: () -> Unit
) {
    SegmentItem(
        segment = segmentWithDetails.segment,
        fishermenCount = segmentWithDetails.fishermen.size,
        fishCaught = segmentWithDetails.fish.size,
        fishKept = segmentWithDetails.fish.count { !it.isReleased },
        onEdit = onEdit,
        onDelete = onDelete,
        onClick = onClick
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
    onClick: () -> Unit
) {
    val dateTimeFormatter = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }
    var menuExpanded by remember { mutableStateOf(false) }
    
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
                Text(segment.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Started: ${dateTimeFormatter.format(Date(segment.startTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (fishermenCount > 0 || fishCaught > 0) {
                    Text(
                        text = "Fishermen: $fishermenCount | Fish: $fishCaught ($fishKept kept)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
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
