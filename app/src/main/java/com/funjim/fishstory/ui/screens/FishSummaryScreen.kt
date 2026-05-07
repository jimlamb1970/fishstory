package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.FishSummary
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureWithName
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.viewmodels.FishViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishSummaryScreen(
    viewModel: FishViewModel,
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, eventId: String, fishId: String?) -> Unit,
    onNavigateToFishList: (String?, String?, String?, String?) -> Unit
) {
    val selectedTripId by viewModel.selectedTripId.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()
    val selectedFishermanId by viewModel.selectedFishermanId.collectAsStateWithLifecycle()
    val selectedLureId by viewModel.selectedLureId.collectAsStateWithLifecycle()

    val allTrips by viewModel.tripsWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTrip = remember(allTrips, selectedTripId) {
        allTrips.find { it.id == selectedTripId }
    }

    val events by viewModel.eventsWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedEvent = remember(events, selectedEventId) {
        events.find { it.id == selectedEventId }
    }

    val fishermen by viewModel.fishermenWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedFisherman = remember(fishermen, selectedFishermanId) {
        fishermen.find { it.id == selectedFishermanId }
    }

    val lures by viewModel.luresWithFish.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedLure = remember(lures, selectedLureId) {
        lures.find { it.lure.id == selectedLureId }
    }

    val summary by viewModel.fishSummary.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fish Caught") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearSelections()
                        navigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (selectedEvent != null) {
                        TextButton(
                            onClick = {
                                onAddFish(selectedEvent.tripId, selectedEvent.id, null)
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Fish")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            FishVisual(
                summary = summary,
                trip = selectedTrip,
                event = selectedEvent,
                fisherman = selectedFisherman,
                lure = selectedLure,
                onClick = {
                    onNavigateToFishList(
                        selectedTripId,
                        selectedEventId,
                        selectedFishermanId,
                        selectedLureId
                    )
                }
            )

            HorizontalDivider()

            TripSelectionField(
                items = allTrips,
                selectedItem = selectedTrip,
                onSelected = { trip ->
                    viewModel.selectTrip(trip.id)
                    viewModel.selectEvent(null)
                },
                onClear = {
                    viewModel.selectTrip(null)
                    viewModel.selectEvent(null)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )

            EventSelectionField(
                items = events,
                selectedItem = selectedEvent,
                onSelected = { event ->
                    viewModel.selectEvent(event.id)
                },
                onClear = {
                    viewModel.selectEvent(null)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )

            FishermanSelectionField(
                items = fishermen,
                selectedItem = selectedFisherman,
                onSelected = { fisherman ->
                    viewModel.selectFisherman(fisherman.id)
                },
                onClear = {
                    viewModel.selectFisherman(null)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )

            LureSelectionField(
                items = lures,
                selectedItem = selectedLure,
                onSelected = { lure ->
                    viewModel.selectLure(lure.lure.id)
                },
                onClear = {
                    viewModel.selectLure(null)
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FishVisual(
    summary: FishSummary,
    trip: Trip?,
    event: Event?,
    fisherman: Fisherman?,
    lure: LureWithName?,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val startDate = event?.startTime ?: trip?.startDate
    val endDate = event?.endTime ?: trip?.endDate
    val hasLocation = if (event != null) {
        event.latitude != null && event.longitude != null
    } else if (trip != null) {
        trip.latitude != null && trip.longitude != null
    } else {
        false
    }

    val latitude = event?.latitude ?: trip?.latitude
    val longitude = event?.longitude ?: trip?.longitude

    val names = listOfNotNull(trip?.name, event?.name, fisherman?.fullName, lure?.displayName)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Fish Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem(
                        label = "CAUGHT",
                        value = "${summary.counts.totalCaught}",
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        color = MaterialTheme.colorScheme.primary)
                    Icon(
                        imageVector = AppIcons.Default.LeapingFish2,
                        contentDescription = "Fish",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        label = "KEPT",
                        value = "${summary.counts.totalKept}",
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        color = MaterialTheme.colorScheme.primary)
                }

                names.forEachIndexed { index, string ->
                    if (index == 0) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(0.dp)) {
                            Text(
                                text = string,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (hasLocation) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "View on map",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            val mapUri =
                                                Uri.parse("https://www.google.com/maps/search/?api=1&query=${latitude},${longitude}")
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
                    } else {
                        Text(
                            text = string,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                }

                if (startDate != null && endDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Adds space between icon and text
                    ) {
                        Text(
                            "${dateFormatter.format(Date(startDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "${dateFormatter.format(Date(endDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                )

                Text(
                    text = "Tap to view fish",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSelectionField(
    items: List<Trip>,
    selectedItem: Trip?,
    onSelected: (Trip) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    OutlinedTextField(
        value = selectedItem?.name ?: "Select Trip (optional)",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false, // Prevents focus/keyboard on the main text field
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
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Trips...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    val filteredSize = filtered.size
                    itemsIndexed(filtered) { index, item ->
                        val backgroundColor = if ((index % 2 == 0) || (filteredSize < 4)) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }

                        ListItem(
                            headlineContent = { Text(item.name) },
                            modifier = Modifier.clickable {
                                onSelected(item)
                                showSheet = false
                                searchQuery = ""
                            },
                            colors = ListItemDefaults.colors(containerColor = backgroundColor)
                        )
                    }

                    item {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("All Trips", color = MaterialTheme.colorScheme.primary) },
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSelectionField(
    items: List<Event>,
    selectedItem: Event?,
    onSelected: (Event) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
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
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Trips...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    val filteredSize = filtered.size
                    itemsIndexed(filtered) { index, item ->
                        val backgroundColor = if ((index % 2 == 0) || (filteredSize < 4)) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }

                        ListItem(
                            headlineContent = { Text(item.name) },
                            modifier = Modifier.clickable {
                                onSelected(item)
                                showSheet = false
                                searchQuery = ""
                            },
                            colors = ListItemDefaults.colors(containerColor = backgroundColor)
                        )
                    }

                    item {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("All Events", color = MaterialTheme.colorScheme.primary) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanSelectionField(
    items: List<Fisherman>,
    selectedItem: Fisherman?,
    onSelected: (Fisherman) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    OutlinedTextField(
        value = selectedItem?.fullName ?: "Select Fisherman (optional)",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Fisherman") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Fishermen...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.fullName.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    val filteredSize = filtered.size
                    itemsIndexed(filtered) { index, item ->
                        val backgroundColor = if ((index % 2 == 0) || (filteredSize < 4)) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }

                        ListItem(
                            headlineContent = { Text(item.fullName) },
                            modifier = Modifier.clickable {
                                onSelected(item)
                                showSheet = false
                                searchQuery = ""
                            },
                            colors = ListItemDefaults.colors(containerColor = backgroundColor)
                        )
                    }

                    item {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("All Fishermen", color = MaterialTheme.colorScheme.primary) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureSelectionField(
    items: List<LureWithName>,
    selectedItem: LureWithName?,
    onSelected: (LureWithName) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    OutlinedTextField(
        value = selectedItem?.displayName ?: "Select Lure (optional)",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Lure") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Lures...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.displayName.contains(searchQuery, ignoreCase = true) }

                LazyColumn {
                    val filteredSize = filtered.size
                    itemsIndexed(filtered) { index, item ->
                        val backgroundColor = if ((index % 2 == 0) || (filteredSize < 4)) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }

                        ListItem(
                            headlineContent = { Text(item.displayName) },
                            modifier = Modifier.clickable {
                                onSelected(item)
                                showSheet = false
                                searchQuery = ""
                            },
                            colors = ListItemDefaults.colors(containerColor = backgroundColor)
                        )
                    }

                    item {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("All Fishermen", color = MaterialTheme.colorScheme.primary) },
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
