package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Event
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
    onNavigateToFishList: (tripId: String, eventId: String) -> Unit,
    navigateToManageSpecies: () -> Unit
) {
    val allTrips by viewModel.trips.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTripId by viewModel.selectedTripId.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()

    val selectedTrip = remember(allTrips, selectedTripId) {
        allTrips.find { it.id == selectedTripId }
    }

    var tripsExpanded by remember { mutableStateOf(false) }

    val tripEvents by produceState<List<Event>>(initialValue = emptyList(), key1 = selectedTrip) {
        selectedTrip?.let {
            viewModel.tripEvents.collect { value = it }
        } ?: run { value = emptyList() }
    }

    val selectedEvent = remember(tripEvents, selectedEventId) {
        tripEvents.find { it.id == selectedEventId }
    }

    var eventsExpanded by remember { mutableStateOf(false) }

    // Fish counts — trip-level or event-level depending on selection
    val fishForScope by viewModel.fishForScope.collectAsStateWithLifecycle()

    val caughtCount = fishForScope.size
    val keptCount = fishForScope.count { !it.isReleased }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fish Caught") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
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
                    IconButton(onClick = { navigateToManageSpecies() }) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "Manage Species")
                    }
                }
            )
        },
        floatingActionButton = {
            // Log Fish button is only enabled when a event is selected
            // TODO - enable all time (allow fish to just be associated with a trip or no trip at all
            if (selectedEvent != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val tripId = selectedEvent.tripId
                        val segId = selectedEvent.id
                        onAddFish(tripId, segId, null)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Log Fish") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Trip Dropdown
            ExposedDropdownMenuBox(
                expanded = tripsExpanded,
                onExpandedChange = { tripsExpanded = !tripsExpanded },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTrip?.name ?: "Select Trip",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Trip") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tripsExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = tripsExpanded,
                    onDismissRequest = { tripsExpanded = false }
                ) {
                    val numberOfTrips = allTrips.size
                    allTrips.forEachIndexed { index, trip ->
                        val itemBackground = if ((numberOfTrips < 4) || (index % 2 == 0)) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }

                        DropdownMenuItem(
                            text = { Text(trip.name) },
                            onClick = {
                                viewModel.updateSelectedTrip(trip.id)
                                viewModel.updateSelectedEvent(null)
                                tripsExpanded = false
                            },
                            modifier = Modifier.background(itemBackground)
                        )
                    }
                }
            }

            // Event Dropdown
            ExposedDropdownMenuBox(
                expanded = eventsExpanded,
                onExpandedChange = {
                    if (selectedTrip != null) eventsExpanded = !eventsExpanded
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedEvent?.name ?: "Select Event (optional)",
                    onValueChange = {},
                    readOnly = true,
                    enabled = selectedTrip != null,
                    label = { Text("Event") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventsExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = eventsExpanded,
                    onDismissRequest = { eventsExpanded = false }
                ) {
                    // Option to clear event selection
                    DropdownMenuItem(
                        text = { Text("All Events") },
                        onClick = {
                            viewModel.updateSelectedEvent(null)
                            eventsExpanded = false
                        }
                    )
                    val eventCount = tripEvents.size
                    tripEvents.forEachIndexed { index, event ->
                        val itemBackground = if ((eventCount < 3) || (index % 2 == 1)) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        }

                        DropdownMenuItem(
                            text = { Text(event.name) },
                            onClick = {
                                viewModel.updateSelectedEvent(event.id)
                                eventsExpanded = false
                            },
                            modifier = Modifier.background(itemBackground)
                        )
                    }
                }
            }

            HorizontalDivider()

            // Fish Visual — only shown when a trip is selected
            // TODO -- add more information to summaries so that more information can be displayed
            // TODO -- refactor the visual to be more color theme aware
            if (selectedTrip != null) {
                Spacer(modifier = Modifier.height(24.dp))
                FishVisual(
                    trip = selectedTrip,
                    event = selectedEvent,
                    caughtCount = caughtCount,
                    keptCount = keptCount,
                    onClick = {
                        val tripId = selectedEvent?.tripId ?: selectedTrip.id
                        val segId = selectedEvent?.id ?: ""
                        onNavigateToFishList(tripId, segId)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a trip to get started.")
                }
            }
        }
    }
}

@Composable
private fun FishVisual(
    trip: Trip,
    event: Event?,
    caughtCount: Int,
    keptCount: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val startDate = event?.startTime ?: trip.startDate
    val endDate = event?.endTime ?: trip.endDate
    val hasLocation = if (event != null) {
        event.latitude != null && event.longitude != null
    } else {
        trip.latitude != null && trip.longitude != null
    }

    val latitude = event?.latitude ?: trip.latitude
    val longitude = event?.longitude ?: trip.longitude

    val label = event?.name ?: trip.name

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fish icon — using drawable resource
                Icon(
                    imageVector = AppIcons.Default.LeapingFish2,
                    contentDescription = "Fish",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    if (hasLocation) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier
                                .size(24.dp)
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

                // Dates
                Text(
                    text = "${dateFormatter.format(Date(startDate))}  →  ${dateFormatter.format(Date(endDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
                )

                // Caught / Kept counts
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$caughtCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = "Caught",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$keptCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = "Kept",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to view fish",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
