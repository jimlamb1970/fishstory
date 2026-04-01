package com.funjim.fishstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripWithDetails
import com.funjim.fishstory.ui.MapPickerSelectionDialog
import com.funjim.fishstory.ui.rememberLocationPickerState
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    viewModel: MainViewModel, 
    navigateToTripDetails: (String) -> Unit,
    navigateToAddTrip: () -> Unit,
    navigateBack: () -> Unit
) {
    val trips by viewModel.trips.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trips") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
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
                        viewModel = viewModel,
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
fun TripItem(
    trip: Trip, 
    details: TripWithDetails?, 
    viewModel: MainViewModel,
    onClick: () -> Unit, 
    onDelete: () -> Unit
) {
    val dateTimeFormatter = remember { 
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }
    val startString = dateTimeFormatter.format(Date(trip.startDate))
    val endString = dateTimeFormatter.format(Date(trip.endDate))
    
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            scope.launch {
                val location = viewModel.getCurrentLocation(context)
                if (location != null) {
                    viewModel.updateTrip(trip.copy(latitude = location.latitude, longitude = location.longitude))
                    Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPicker = rememberLocationPickerState(
        viewModel = viewModel,
        existingLat = trip.latitude,  // Passed from your DB object
        existingLng = trip.longitude,
        onLocationConfirmed = { lat, lng ->
            scope.launch {
                viewModel.updateTrip(trip.copy(latitude = lat, longitude = lng))
            }
        }
    )

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trip.name, style = MaterialTheme.typography.titleLarge, color = Color.Black)
                    if (trip.latitude != null && trip.longitude != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "View on map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${trip.latitude},${trip.longitude}")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        )
                    }
                }
                Text("Start: $startString", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("End:   $endString", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
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
                        text = { Text("Use Current Location") },
                        onClick = {
                            showMenu = false
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    val location = viewModel.getCurrentLocation(context)
                                    if (location != null) {
                                        viewModel.updateTrip(trip.copy(latitude = location.latitude, longitude = location.longitude))
                                        Toast.makeText(context, "Location updated", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Could not get location", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = { Text("Select Location") },
                        onClick = {
                            showMenu = false
                            locationPicker.openPicker()
                        },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )

                    if (trip.latitude != null) {
                        DropdownMenuItem(
                            text = { Text("Clear Location", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    viewModel.updateTrip(
                                        trip.copy(latitude = null, longitude = null)
                                    )
                                    Toast.makeText(context, "Location cleared", Toast.LENGTH_SHORT).show()
                                }
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
