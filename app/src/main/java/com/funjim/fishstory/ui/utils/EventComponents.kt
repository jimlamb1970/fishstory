package com.funjim.fishstory.ui.utils

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventItem(
    item: EventSummary,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
    }
    val now = System.currentTimeMillis()
    val startString = dateTimeFormatter.format(Date(item.event.startTime))
    val endString = dateTimeFormatter.format(Date(item.event.endTime))

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getCardContentColor()
    val secondaryContentColor = getCardSecondaryContentColor()

    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThumbnailBox(
                thumbnail = thumbnail,
                imageVector = AppIcons.Default.Boat,
                modifier = Modifier.size(72.dp)
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
                        )
                        if (eventLat == null) {
                            Text(
                                text = "(Trip)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CardItemWithValue(
                            icon = AppIcons.Default.Fisherman,
                            value = item.fishermanCount.toString(),
                            contentColor = secondaryContentColor
                        )

                        CardItemWithValue(
                            icon = AppIcons.Default.TackleBox,
                            value = item.tackleBoxCount.toString(),
                            contentColor = secondaryContentColor
                        )
                    }
                }

                if (item.fishCaught != 0 || now >= item.event.startTime) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFish,
                            caughtCount = item.fishCaught,
                            keptCount = item.fishKept,
                            contentColor = secondaryContentColor
                        )
                    }
                }


                if ((onSelectLocation != null) ||
                    (onSetLocation != null) ||
                    (onUseTripLocation != null) ||
                    (onClearLocation != null) ||
                    (onDelete != null)
                ) {
                    Box {
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSelectionField(
    items: List<Event>,
    selectedItem: Event?,
    onSelected: (Event) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailProvider: @Composable (Event) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    OutlinedTextField(
        value = selectedItem?.name ?: "Select Event (optional)",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Event") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Event",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxHeight(0.8f)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Events...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    val filteredSize = filtered.size
                    itemsIndexed(filtered) { index, item ->
                        val backgroundColor = getCardColor(index, filteredSize)
                        val contentColor = getCardContentColor()

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                // TODO -- hide the border for now
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    onSelected(item)
                                    showSheet = false
                                    searchQuery = ""
                                },
                            leadingContent = {
                                thumbnailProvider(item)
                            },
                            headlineContent = { Text(item.name) },
                            colors = ListItemDefaults.colors(
                                containerColor = backgroundColor,
                                headlineColor = contentColor
                            )
                        )
                    }

                    if (selectedItem != null) {
                        item {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "Reset Event",
                                        color = getCardContentColor()
                                    )
                                },
                                modifier = Modifier.clickable {
                                    showSheet = false
                                    onClear()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
