package com.funjim.fishstory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripWithDetails
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    viewModel: MainViewModel, 
    navigateToTripDetails: (Int) -> Unit,
    navigateToAddTrip: () -> Unit
) {
    val trips by viewModel.trips.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Fishstory Trips") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = navigateToAddTrip) {
                Icon(Icons.Default.Add, contentDescription = "Add Trip")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn {
                items(trips) { trip ->
                    val details by viewModel.getTripWithDetails(trip.id).collectAsState(initial = null)
                    
                    TripItem(
                        trip = trip,
                        details = details,
                        onClick = {
                            navigateToTripDetails(trip.id)
                        },
                        onDelete = {
                            scope.launch {
                                viewModel.deleteTrip(trip)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TripItem(trip: Trip, details: TripWithDetails?, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormatter = remember { 
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val dateString = dateFormatter.format(Date(trip.startDate))
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.name, style = MaterialTheme.typography.titleLarge, color = Color.Black)
                Text(dateString, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                details?.let {
                    val fishermanCount = it.fishermen.size
                    val caughtCount = it.fish.size
                    val keptCount = it.fish.count { f -> !f.isReleased }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$fishermanCount Fisherman • $caughtCount Caught • $keptCount Kept",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}
