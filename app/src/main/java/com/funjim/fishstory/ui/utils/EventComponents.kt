package com.funjim.fishstory.ui.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.theme.AppIcons
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditEventDialog(
    event: Event,
    trip: Trip,
    onConfirm: (Event) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var origName by remember { mutableStateOf(event.name) }
    var origStart by remember { mutableLongStateOf(event.startTime) }
    var origEnd by remember { mutableLongStateOf(event.endTime) }
    var tripStart by remember { mutableLongStateOf(trip.startDate) }
    var tripEnd by remember { mutableLongStateOf(trip.endDate) }

    var name by remember { mutableStateOf(origName) }
    var startTime by remember { mutableStateOf(origStart) }
    var endTime by remember { mutableStateOf(origEnd) }

    val isChanged = name != origName ||
            startTime != origStart ||
            endTime != origEnd

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Event") },
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
                    if (newTime < tripStart) {
                        Toast.makeText(
                            context,
                            "Start date and time cannot be before trip start date and time",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else if (newTime > tripEnd) {
                        Toast.makeText(
                            context,
                            "Start date and time cannot be after trip end date and time",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        startTime = newTime
                        if (startTime > endTime)
                            endTime = startTime
                    }
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
                    } else if (newTime > tripEnd) {
                        Toast.makeText(
                            context,
                            "End date and time cannot be after trip end date and time",
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
                    onConfirm(event.copy(
                        name = name.trim(),
                        startTime = startTime,
                        endTime = endTime)
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
fun EventItem(
    item: EventSummary,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    showPhotoPicker: Boolean = false,
    index: Int = 0,
    totalItems: Int = 0,
    onClick: () -> Unit,
    onFishClick: ((String, String, Boolean) -> Unit)? = null,
    onFishermanClick: (() -> Unit)? = null,
    onPhotoAdded: ((Uri) -> Unit)? = null,
    onPhotoTaken: ((Uri) -> Unit)? = null,
    onSetThumbnail: ((Photo) -> Unit)? = null,
    onPhotoDeleted: ((Photo) -> Unit)? = null,
    onSetLocation: (() -> Unit)? = null,
    onSelectLocation: (() -> Unit)? = null,
    onUseTripLocation: (() -> Unit)? = null,
    onClearLocation: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())
    var showPhotos by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
    }
    val startString = dateTimeFormatter.format(Date(item.event.startTime))
    val endString = dateTimeFormatter.format(Date(item.event.endTime))

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    var menuExpanded by remember { mutableStateOf(false) }

    val hasMenuActions = (onPhotoAdded != null) || (onPhotoTaken != null) ||
            (onSelectLocation != null) || (onSetLocation != null) ||
            (onUseTripLocation != null) || (onClearLocation != null) ||
            (onDelete != null)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { if (hasMenuActions) menuExpanded = true }
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
                    imageVector = AppIcons.Default.CanoeEmpty,
                    modifier = Modifier.size(64.dp),
                    onClick =
                        if (showPhotoPicker) { { showPhotos = !showPhotos } }
                        else null
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
                                    .alpha(if (eventLat == null) 0.6f else 1f )
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
                        if (verbose) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Fisherman,
                                    value = item.fishermanCount.toString(),
                                    description = if (item.fishermanCount == 1) "Fisherman" else "Fishermen",
                                    onClick = onFishermanClick,
                                    contentColor = secondaryContentColor
                                )
                            }

                            if (item.tackleBoxCount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CardItemWithValue(
                                        icon = AppIcons.Default.TackleBox,
                                        value = item.tackleBoxCount.toString(),
                                        description = if (item.tackleBoxCount == 1) "Tackle Box" else "Tackle Boxes",
                                        onClick = onFishermanClick,
                                        contentColor = secondaryContentColor
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Fisherman,
                                    value = item.fishermanCount.toString(),
                                    onClick = onFishermanClick,
                                    contentColor = secondaryContentColor
                                )

                                CardItemWithValue(
                                    icon = AppIcons.Default.TackleBox,
                                    value = item.tackleBoxCount.toString(),
                                    onClick = onFishermanClick,
                                    contentColor = secondaryContentColor
                                )
                            }
                        }
                    }

                    if (item.fishCaught != 0) {
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFishWithFins,
                            caughtCount = item.fishCaught,
                            keptCount = item.fishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = onFishClick?.let { onClick ->
                                { onClick(item.trip.id, item.event.id, false) }
                            },
                            contentColor = secondaryContentColor
                        )
                    }

                    if (item.targetFishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.TargetFish,
                            caughtCount = item.targetFishCaught,
                            keptCount = item.targetFishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = onFishClick?.let { onClick ->
                                { onClick(item.trip.id, item.event.id, true) }
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                if (hasMenuActions) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Event options",
                                tint = contentColor
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    if (showPhotos) Text("Hide Photos")
                                    else Text("Show Photos") },
                                onClick = {
                                    menuExpanded = false
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

    var isGridView by remember { mutableStateOf(true) }

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
                        text = "Select Event",
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
                    label = { Text("Search events ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
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
                                    title = "Reset Event",
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
                                    title = "Reset Event",
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
