package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.viewmodels.DashboardViewModel

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit, // e.g., "fishermen", "lures"
    viewModel: DashboardViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Scaffold automatically calculates the "Safe Area" for the Top Bar and Bottom Bar
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // This 'innerPadding' contains the safe area values
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Apply the safe padding here!
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. ACTIVE TRIP (The Hero)
            item {
                if (state.activeTrip != null) {
                    ActiveTripCard(
                        trip = state.activeTrip!!,
                        onLogFish = { /* Navigate to catch logging */ }
                    )
                } else {
                    // Empty state leads user to create new trip
                    NewTripHeroCard(onCreateClick = { onNavigate("create_trip") })
                }
            }

            // 2. UPCOMING TRIPS (Horizontal Row)
            if (state.upcomingTrips.isNotEmpty()) {
                item {
                    Text("Upcoming Adventures", style = MaterialTheme.typography.titleLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.upcomingTrips) { trip ->
                            UpcomingTripChip(trip) // Maize themed small cards
                        }
                    }
                }
            }

            // 3. NAVIGATION GRID (The "Get to Everything" section)
            item {
                Text("Management", style = MaterialTheme.typography.titleLarge)
                DashboardGrid(
                    onFishermenClick = { onNavigate("fishermen") },
                    onLuresClick = { onNavigate("lures") },
                    onTackleBoxClick = { onNavigate("tackleboxes") }
                )
            }

            // 4. PREVIOUS TRIPS
            item {
                Text("Recent History", style = MaterialTheme.typography.titleLarge)
            }
            items(state.recentTrips) { trip ->
                TripHistoryRow(trip)
            }
        }
    }
}

@Composable
fun ActiveTripCard(
    trip: Trip,
    onLogFish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFCB05), // Michigan Maize
            contentColor = Color(0xFF00274C)    // Michigan Blue
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Waves, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("LIVE TRIP", style = MaterialTheme.typography.labelLarge)
            }
            Text(
                text = trip.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onLogFish,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00274C), // Michigan Blue
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LOG A CATCH")
            }
        }
    }
}
@Composable
fun DashboardGrid(
    onFishermenClick: () -> Unit,
    onLuresClick: () -> Unit,
    onTackleBoxClick: () -> Unit
) {
    val items = listOf(
        Triple("Fishermen", Icons.Default.Groups, onFishermenClick),
        Triple("Tackle Box", Icons.Default.Inventory, onTackleBoxClick),
        Triple("Lures", Icons.Default.PhonelinkSetup, onLuresClick), // Or a custom hook icon
        Triple("Settings", Icons.Default.Settings, { /* TODO */ })
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridItem(items[0], Modifier.weight(1f))
            GridItem(items[1], Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GridItem(items[2], Modifier.weight(1f))
            GridItem(items[3], Modifier.weight(1f))
        }
    }
}

@Composable
fun GridItem(item: Triple<String, ImageVector, () -> Unit>, modifier: Modifier) {
    OutlinedCard(
        onClick = item.third,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.outlinedCardColors(contentColor = Color(0xFF00274C))
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
            containerColor = Color(0xFF00274C), // Michigan Blue
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = Color(0xFFFFCB05), // Maize accent
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
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun UpcomingTripChip(trip: Trip) {
    val dateString = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        .format(java.util.Date(trip.startDate))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFCB05).copy(alpha = 0.15f), // Faint Maize background
        border = BorderStroke(1.dp, Color(0xFFFFCB05)),
        modifier = Modifier.width(140.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF00274C)
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
                tint = Color(0xFF00274C)
            )
        }
    }
}