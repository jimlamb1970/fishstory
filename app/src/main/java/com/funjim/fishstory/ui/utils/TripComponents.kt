package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    data class Delete(val tripSummary: TripSummary) : TripAction()
}

@Composable
fun TripItemWithMenu(
    tripSummary: TripSummary,
    index: Int,
    totalItems: Int,
    modifier: Modifier = Modifier,
    hasLocationPermission: Boolean = true,
    thumbnailFlow: Flow<ByteArray?>,
    onNavigateToDetails: (String) -> Unit,
    onFishClick: ((String, Boolean) -> Unit)? = null,
    onAction: (TripAction) -> Unit,
    showMenu: Boolean,
    onMenuDismiss: () -> Unit
) {
    TripItem(
        trip = tripSummary,
        index = index,
        totalItems = totalItems,
        modifier = modifier,
        thumbnailFlow = thumbnailFlow,
        onClick = { onNavigateToDetails(tripSummary.trip.id) },
        onLongClick = { onAction(TripAction.Menu(tripSummary)) },
        onFishClick = onFishClick,
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
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFishClick: ((String, Boolean) -> Unit)? = null,
    onAction: (TripAction) -> Unit,
    actions: @Composable () -> Unit = {}
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

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
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThumbnailBox(
                thumbnail = thumbnail,
                imageVector = AppIcons.Default.Boat,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trip.trip.name,
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
                if (eventCount != 0 || fishermanCount != -1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (eventCount != 0) {
                            CardItemWithValue(
                                icon = AppIcons.Default.Boat,
                                value = eventCount.toString(),
                                contentColor = secondaryContentColor
                            )
                        }

                        if (fishermanCount != -1) {
                            CardItemWithValue(
                                icon = AppIcons.Default.Fisherman,
                                value = fishermanCount.toString(),
                                contentColor = secondaryContentColor
                            )

                            CardItemWithValue(
                                icon = AppIcons.Default.TackleBox,
                                value = tackleBoxCount.toString(),
                                contentColor = secondaryContentColor
                            )
                        }
                    }
                }

                // Only show the fish caught counts when something has been caught
                if (trip.fishCaught != 0) {
                    FishCaughtItem(
                        icon = AppIcons.Default.LeapingFishWithFins,
                        caughtCount = trip.fishCaught,
                        keptCount = trip.fishKept,
                        onFishClick = onFishClick?.let { onClick ->
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
                        onFishClick = onFishClick?.let { onClick ->
                            { onClick(trip.trip.id, true) }
                        },
                        contentColor = secondaryContentColor
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSelectionField(
    items: List<Trip>,
    selectedItem: Trip?,
    onSelected: (Trip) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailProvider: @Composable (Trip) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = selectedItem?.name ?: "Select Trip (optional)",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Trip") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Select Trip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier
                .fillMaxHeight(0.8f)
                .padding(start = 16.dp, end = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Trips ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                val filteredSize = filtered.size

                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        gridItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                headlineContent = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        thumbnailProvider(item)

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (selectedItem != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider() }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ModalResetButton(
                                    title = "Reset Trip",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listItemsIndexed(filtered) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                leadingContent = {
                                    thumbnailProvider(item)
                                },
                                headlineContent = {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight =
                                            if (isSelected) FontWeight.Bold
                                            else FontWeight.Normal,
                                        color = getOnCardColor()
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (selectedItem != null) {
                            item { HorizontalDivider() }
                            item {
                                ModalResetButton(
                                    title = "Reset Trip",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
