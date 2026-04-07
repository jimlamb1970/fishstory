package com.funjim.fishstory.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.R
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.ManageSpeciesDialog
import com.funjim.fishstory.viewmodels.FishViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishSummaryScreen(
    viewModel: FishViewModel,
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, segmentId: String, fishId: String?) -> Unit,
    onNavigateToFishList: (tripId: String, segmentId: String) -> Unit
) {
    val allTrips by viewModel.trips.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedTripId by viewModel.selectedTripIdForFilter.collectAsStateWithLifecycle()
    val selectedSegmentId by viewModel.selectedSegmentIdForFilter.collectAsStateWithLifecycle()

    val selectedTrip = remember(allTrips, selectedTripId) {
        allTrips.find { it.id == selectedTripId }
    }

    var tripExpanded by remember { mutableStateOf(false) }

    val segmentsForTrip by produceState<List<Segment>>(initialValue = emptyList(), key1 = selectedTrip) {
        selectedTrip?.let { trip ->
            viewModel.getSegmentsForTrip(trip.id).collect { value = it }
        } ?: run { value = emptyList() }
    }

    val selectedSegment = remember(segmentsForTrip, selectedSegmentId) {
        segmentsForTrip.find { it.id == selectedSegmentId }
    }

    var segmentExpanded by remember { mutableStateOf(false) }

    // Fish counts — trip-level or segment-level depending on selection
    val fishForScope by viewModel.fishForScope.collectAsStateWithLifecycle()

    val caughtCount = fishForScope.size
    val keptCount = fishForScope.count { !it.isReleased }

    val allSpecies by viewModel.species.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var permissionGranted by remember { mutableStateOf(false) }
    var showManageSpeciesDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            permissionGranted = true
            val tripId = selectedSegment?.tripId ?: selectedTrip?.id
            val segId = selectedSegment?.id ?: ""
            if (tripId != null) onAddFish(tripId, segId, null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fish Caught") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showManageSpeciesDialog = true }) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "Manage Species")
                    }
                }
            )
        },
        floatingActionButton = {
            // Log Fish button is only enabled when a segment is selected
            // TODO - enable all time (allow fish to just be associated with a trip or no trip at all
            if (selectedSegment != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val tripId = selectedSegment.tripId
                        val segId = selectedSegment.id
                        if (permissionGranted) {
                            onAddFish(tripId, segId, null)
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
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
                expanded = tripExpanded,
                onExpandedChange = { tripExpanded = !tripExpanded },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    value = selectedTrip?.name ?: "Select Trip",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Trip") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tripExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = tripExpanded,
                    onDismissRequest = { tripExpanded = false }
                ) {
                    allTrips.forEach { trip ->
                        DropdownMenuItem(
                            text = { Text(trip.name) },
                            onClick = {
                                viewModel.updateSelectedTripIdForFilter(trip.id)
                                viewModel.updateSelectedSegmentIdForFilter(null)
                                tripExpanded = false
                            }
                        )
                    }
                }
            }

            // Segment Dropdown
            ExposedDropdownMenuBox(
                expanded = segmentExpanded,
                onExpandedChange = {
                    if (selectedTrip != null) segmentExpanded = !segmentExpanded
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    value = selectedSegment?.name ?: "Select Segment (optional)",
                    onValueChange = {},
                    readOnly = true,
                    enabled = selectedTrip != null,
                    label = { Text("Segment") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = segmentExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = segmentExpanded,
                    onDismissRequest = { segmentExpanded = false }
                ) {
                    // Option to clear segment selection
                    DropdownMenuItem(
                        text = { Text("All segments") },
                        onClick = {
                            viewModel.updateSelectedSegmentIdForFilter(null)
                            segmentExpanded = false
                        }
                    )
                    segmentsForTrip.forEach { segment ->
                        DropdownMenuItem(
                            text = { Text(segment.name) },
                            onClick = {
                                viewModel.updateSelectedSegmentIdForFilter(segment.id)
                                segmentExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Fish Visual — only shown when a trip is selected
            // TODO -- add more information to summaries so that more information can be displayed
            if (selectedTrip != null) {
                Spacer(modifier = Modifier.height(24.dp))
                FishVisual(
                    trip = selectedTrip,
                    segment = selectedSegment,
                    caughtCount = caughtCount,
                    keptCount = keptCount,
                    onClick = {
                        val tripId = selectedSegment?.tripId ?: selectedTrip.id
                        val segId = selectedSegment?.id ?: ""
                        onNavigateToFishList(tripId, segId)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a trip to get started.")
                }
            }
        }

        if (showManageSpeciesDialog) {
            ManageSpeciesDialog(
                species = allSpecies,
                onDismiss = { showManageSpeciesDialog = false },
                onAddSpecies = { speciesName ->
                    scope.launch { viewModel.addSpecies(Species(name = speciesName)) }
                },
                onDeleteSpecies = { species ->
                    scope.launch { viewModel.deleteSpecies(species) }
                }
            )
        }
    }
}

@Composable
private fun FishVisual(
    trip: Trip,
    segment: Segment?,
    caughtCount: Int,
    keptCount: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val startDate = segment?.startTime ?: trip.startDate
    val endDate = segment?.endTime ?: trip.endDate
    val hasLocation = if (segment != null) {
        segment.latitude != null && segment.longitude != null
    } else {
        trip.latitude != null && trip.longitude != null
    }

    val latitude = segment?.latitude ?: trip.latitude
    val longitude = segment?.longitude ?: trip.longitude

    val label = segment?.name ?: trip.name

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
                .background(Color(0xFF023E58))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fish icon — using drawable resource
                Icon(
                    painter = painterResource(id = R.mipmap.fish_foreground),
                    contentDescription = "Fish",
                    modifier = Modifier.size(120.dp),
                    tint = Color.Unspecified
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                    color = Color.White.copy(alpha = 0.8f)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.White.copy(alpha = 0.3f)
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
                            color = Color.White
                        )
                        Text(
                            text = "Caught",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$keptCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Kept",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to view fish",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
