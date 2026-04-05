package com.funjim.fishstory.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.ui.FishItem
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentFishListScreen(
    viewModel: MainViewModel,
    tripId: String,
    segmentId: String,   // empty string means trip-level (no segment selected)
    navigateBack: () -> Unit,
    onAddFish: (tripId: String, segmentId: String, fishId: String?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Resolve title from segment name or trip name
    val allTrips by viewModel.trips.collectAsStateWithLifecycle(initialValue = emptyList())
    val trip = remember(allTrips, tripId) { allTrips.find { it.id == tripId } }

    val segmentsForTrip by produceState<List<Segment>>(initialValue = emptyList(), key1 = tripId) {
        viewModel.getSegmentsForTrip(tripId).collect { value = it }
    }
    val segment = remember(segmentsForTrip, segmentId) {
        segmentsForTrip.find { it.id == segmentId }
    }

    val screenTitle = segment?.name ?: trip?.name ?: "Fish Caught"

    // Load fish for the appropriate scope
    val fishList by produceState<List<FishWithDetails>>(initialValue = emptyList(), key1 = tripId, key2 = segmentId) {
        when {
            segmentId.isNotEmpty() -> viewModel.getFishForSegment(segmentId).collect { value = it }
            else -> viewModel.getFishForTrip(tripId).collect { value = it }
        }
    }

    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            permissionGranted = true
            onAddFish(tripId, segmentId, null)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (permissionGranted) {
                        onAddFish(tripId, segmentId, null)
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
    ) { padding ->
        if (fishList.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No fish logged yet. Tap 'Log Fish' to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
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
}
