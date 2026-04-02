package com.funjim.fishstory

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.FishItem
import com.funjim.fishstory.ui.ManageSpeciesDialog
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    viewModel: MainViewModel,
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, segmentId: String, fishId: String?) -> Unit
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

    val fishList by produceState<List<FishWithDetails>>(initialValue = emptyList(), key1 = selectedTripId, key2 = selectedSegmentId) {
        when {
            selectedSegmentId != null -> {
                viewModel.getFishForSegment(selectedSegmentId!!).collect { value = it }
            }
            selectedTripId != null -> {
                viewModel.getFishForTrip(selectedTripId!!).collect { value = it }
            }
            else -> {
                value = emptyList()
            }
        }
    }

    val allSpecies by viewModel.species.collectAsStateWithLifecycle(initialValue = emptyList())

    var permissionGranted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            permissionGranted = true
            selectedSegment?.let { segment ->
                onAddFish(segment.tripId, segment.id, null)
            }
        }
    }

    var showManageSpeciesDialog by remember { mutableStateOf(false) }

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
            if (selectedSegment != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (permissionGranted) {
                            onAddFish(selectedSegment.tripId, selectedSegment.id, null)
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
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Trip Selector
            ExposedDropdownMenuBox(
                expanded = tripExpanded,
                onExpandedChange = { tripExpanded = !tripExpanded },
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                TextField(
                    value = selectedTrip?.name ?: "Select Trip",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Trip") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tripExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
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
                                tripExpanded = false
                            }
                        )
                    }
                }
            }

            // Segment Selector - Populate based on selected trip
            ExposedDropdownMenuBox(
                expanded = segmentExpanded,
                onExpandedChange = { 
                    if (selectedTrip != null) segmentExpanded = !segmentExpanded 
                },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxWidth()
            ) {
                TextField(
                    value = selectedSegment?.name ?: "Select Segment",
                    onValueChange = {},
                    readOnly = true,
                    enabled = selectedTrip != null,
                    label = { Text("Segment") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = segmentExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = segmentExpanded,
                    onDismissRequest = { segmentExpanded = false }
                ) {
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

            if (selectedTrip == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a trip to see fish caught.")
                }
            } else if (fishList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (selectedSegment == null) "No fish caught on this trip." else "No fish caught in this segment.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(fishList) { fishDetails ->
                        FishItem(
                            fish = fishDetails,
                            viewModel = viewModel,
                            onEdit = {
                                scope.launch {
                                    onAddFish(fishDetails.tripId, fishDetails.segmentId, fishDetails.id)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    val fishObj = viewModel.getFishById(fishDetails.id)
                                    if (fishObj != null) {
                                        viewModel.deleteFishObject(fishObj)
                                    }
                                }
                            },
                            onShowMap = {
                                // Map logic
                            },
                            onUpdateLocation = {
                                // Update location logic
                            }
                        )
                    }
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
