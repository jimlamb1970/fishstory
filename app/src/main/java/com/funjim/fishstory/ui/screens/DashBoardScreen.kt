package com.funjim.fishstory.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.SegmentSummary
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.ui.utils.TripAction
import com.funjim.fishstory.ui.utils.TripItem
import com.funjim.fishstory.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit, // e.g., "fishermen", "lures"
    viewModel: DashboardViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activeTripSegments by viewModel.activeTripSegments.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Scaffold automatically calculates the "Safe Area" for the Top Bar and Bottom Bar
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // This 'innerPadding' contains the safe area values
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Apply the safe padding here!
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. ACTIVE TRIP (The Hero)
            item {
                if (state.activeTrips.isNotEmpty() && activeTripSegments.active.isNotEmpty()) {
                    ActiveTripCard(
                        activeTrips = state.activeTrips,
                        activeSegments = activeTripSegments.active,
                        onTripClick = { tripId -> onNavigate("trip_details/$tripId") },
                        onSegmentClick = { tripId, segmentId -> onNavigate("segment_details/$segmentId/$tripId") },
                        onClick = { tripId, segmentId -> onNavigate("segment_details/$segmentId/$tripId") },
                        onLogFish = { tripId, segmentId -> onNavigate("add_fish/$tripId/$segmentId") }
                    )
                } else {
                    // Empty state leads user to create new trip
                    NewTripHeroCard(onCreateClick = { onNavigate("add_trip") })
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary // Michigan Blue
                )
            }

            // 2. UPCOMING TRIPS (Horizontal Row)
            if ((activeTripSegments.upcoming.isNotEmpty() || state.upcomingTrips.isNotEmpty())) {
                item {
                    Text("Upcoming Adventures", style = MaterialTheme.typography.titleLarge)

                    if (activeTripSegments.upcoming.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Upcoming Segments", style = MaterialTheme.typography.titleSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(activeTripSegments.upcoming) { segment ->
                                UpcomingSegmentChip(
                                    tripName = state.activeTrips.find { it.trip.id == segment.segment.tripId }?.trip?.name ?: "Unknown Trip",
                                    segment = segment.segment,
                                    onSegmentClick = { segmentId, tripId -> onNavigate("segment_details/${segmentId}/${tripId}") }
                                )
                            }
                        }
                    }

                    if (state.upcomingTrips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Upcoming Trips", style = MaterialTheme.typography.titleSmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.upcomingTrips) { trip ->
                                UpcomingTripChip(
                                    trip = trip,
                                    onTripClick = { onNavigate("trip_details/$it") }
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary // Michigan Blue
                    )
                }
            }

            // 3. NAVIGATION GRID (The "Get to Everything" section)
            item {
                Text("Management", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                DashboardGrid(
                    onFishClick = { onNavigate("fish") },
                    onFishermenClick = { onNavigate("fishermen") },
                    onLuresClick = { onNavigate("lures") },
                    onReportsClick = { onNavigate("reports") },
                    onSettingsClick = { onNavigate("settings") },
                    onTripsClick = { onNavigate("trips") }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.primary // Michigan Blue
                )
            }

            // 4. PREVIOUS TRIPS
            item {
                Text("Recent History", style = MaterialTheme.typography.titleLarge)
            }

            val totalItems = state.recentTrips.size
            itemsIndexed(state.recentTrips) { index, trip ->
                TripItem(
                    trip = trip,
                    index = index,
                    totalItems = totalItems,
                    modifier = Modifier.padding(),
                    onClick = { onNavigate("trip_details/${trip.trip.id}") },
                    onAction = { action ->
                        when (action) {
                            is TripAction.OpenMap -> {
                                val mapUri = Uri.parse("geo:${action.lat},${action.lng}?q=${action.lat},${action.lng}(Fishing Spot)")
                                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else -> {}
                        }
                    }
                )

                //TripHistoryRow(trip)
            }
        }
    }
}

@Composable
fun ActiveTripCard(
    activeTrips: List<TripSummary>,
    activeSegments: List<SegmentSummary>,
    onClick: (String, String) -> Unit,
    onTripClick: (String) -> Unit,
    onSegmentClick: (String, String) -> Unit,
    onLogFish: (String, String) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val segment = activeSegments.getOrNull(currentIndex) ?: return
    val trip = activeTrips.find { it.trip.id == segment.segment.tripId } ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(segment.segment.tripId, segment.segment.id) }
            .pointerInput(activeSegments.size) {
                var dragTotal = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onDragEnd = {
                        when {
                            dragTotal < -50f && currentIndex < activeSegments.size - 1 -> currentIndex++
                            dragTotal > 50f && currentIndex > 0 -> currentIndex--
                        }
                        dragTotal = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragTotal += dragAmount
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Waves, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("LIVE TRIP", style = MaterialTheme.typography.labelLarge)
                }
                if (activeSegments.size > 1) {
                    Text(
                        text = "${currentIndex + 1} / ${activeSegments.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = trip.trip.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable( onClick = { onTripClick(trip.trip.id) })
            )
            if (trip.totalCaught != segment.fishCaught) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "CAUGHT",
                        value = "${trip.totalCaught}",
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        label = "KEPT",
                        value = "${trip.totalKept}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AchievementItem(
                        icon = Icons.Default.Person,
                        label = "Most Caught",
                        name = trip.mostCaughtName,
                        description = "(${trip.mostCaught} fish)",
                        modifier = Modifier.weight(1f)
                    )
                    AchievementItem(
                        icon = Icons.Default.Person,
                        label = "Biggest Fish",
                        name = trip.bigFishName,
                        description = "(${trip.bigFishLength}\" : ${trip.bigFishSpecies})",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = segment.segment.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable( onClick = { onSegmentClick(segment.segment.tripId, segment.segment.id) })
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem(label = "CAUGHT", value = "${segment.fishCaught}", color = MaterialTheme.colorScheme.primary)
                StatItem(label = "KEPT", value = "${segment.fishKept}", color = MaterialTheme.colorScheme.primary) // Harvest Green
            }

            if (segment.mostCaught != 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AchievementItem(
                        icon = Icons.Default.Person,
                        label = "Most Caught",
                        name = segment.mostCaughtName,
                        description = "(${segment.mostCaught} fish)",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    AchievementItem(
                        icon = Icons.Default.Person,
                        label = "Biggest Fish",
                        name = segment.bigFishName,
                        description = "(${segment.bigFishLength}\" : ${segment.bigFishSpecies})",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onLogFish(segment.segment.tripId, segment.segment.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LOG A CATCH")
            }
            if (activeSegments.size > 1) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activeSegments.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (index == currentIndex) 8.dp else 6.dp)
                                .background(
                                    color = if (index == currentIndex)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DashboardGrid(
    onFishClick: () -> Unit,
    onFishermenClick: () -> Unit,
    onLuresClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTripsClick: () -> Unit
) {
    val items = listOf(
        Triple("Trips", Icons.Default.Map, onTripsClick),
        Triple("Fish", Icons.Default.Waves, onFishClick),
        Triple("Fishermen", Icons.Default.Groups, onFishermenClick),
        Triple("Lures", Icons.Default.Inventory, onLuresClick), // Or a custom hook icon
        Triple("Reports", Icons.Default.AutoGraph, onReportsClick), // Or a custom hook icon
        Triple("Settings", Icons.Default.Settings, onSettingsClick)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridItem(items[0], Modifier.weight(1f))
            GridItem(items[1], Modifier.weight(1f))
            GridItem(items[2], Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridItem(items[3], Modifier.weight(1f))
            GridItem(items[4], Modifier.weight(1f))
            GridItem(items[5], Modifier.weight(1f))
        }
    }
}

@Composable
fun GridItem(item: Triple<String, ImageVector, () -> Unit>, modifier: Modifier) {
    OutlinedCard(
        onClick = item.third,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.outlinedCardColors(contentColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(item.second, contentDescription = null, modifier = Modifier.size(32.dp))
            Text(item.first, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun NewTripHeroCard(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onCreateClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary, // Michigan Blue
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary, // Maize accent
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Start a New Adventure",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "No trip active. Tap to begin!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun UpcomingTripChip(
    trip: Trip,
    onTripClick: (String) -> Unit
) {
    val dateString = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(trip.startDate))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), // Faint Maize background
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.width(140.dp).clickable{onTripClick(trip.id)}
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = trip.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun UpcomingSegmentChip(
    tripName: String,
    segment: Segment,
    onSegmentClick: (String, String) -> Unit
) {
    val dateString = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(segment.startTime))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), // Faint Maize background
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
        modifier = Modifier.width(140.dp).clickable{onSegmentClick(segment.id, segment.tripId)}
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tripName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = segment.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TripHistoryRow(trip: Trip) {
    val dateRange = java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.getDefault())
        .format(java.util.Date(trip.startDate))

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // A small Maize "chevron" or icon to indicate you can view details
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}