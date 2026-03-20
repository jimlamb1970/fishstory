package com.funjim.fishstory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.AddFishDialog
import com.funjim.fishstory.ui.BoatSummary
import com.funjim.fishstory.ui.FishItem
import com.funjim.fishstory.ui.FishermanItem
import com.funjim.fishstory.ui.MapPickerSelectionDialog
import com.funjim.fishstory.ui.PhotoPickerRow
import com.funjim.fishstory.ui.getCurrentLocation
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentDetailsScreen(
    viewModel: MainViewModel,
    segmentId: Int,
    tripId: Int,
    navigateToSegmentBoatLoad: (Int, Int) -> Unit,
    navigateToFishermanDetails: (Int) -> Unit,
    navigateBack: () -> Unit
) {
    val segmentWithDetails by viewModel.getSegmentWithDetails(segmentId).collectAsState(initial = null)
    val fishList by viewModel.getFishForSegment(segmentId).collectAsState(initial = emptyList())
    val allSpecies by viewModel.species.collectAsState(initial = emptyList())
    val segmentPhotos by viewModel.getPhotosForSegment(segmentId).collectAsState(initial = emptyList())
    
    var showAddFishDialog by remember { mutableStateOf(false) }
    var showFishermenDialog by remember { mutableStateOf(false) }
    var fishToUpdateLocation by remember { mutableStateOf<FishWithDetails?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            showAddFishDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(segmentWithDetails?.segment?.name ?: "Segment Details") },
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
                    val fineLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    val coarseLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )

                    if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
                        coarseLocationPermission == PackageManager.PERMISSION_GRANTED
                    ) {
                        showAddFishDialog = true
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            PhotoPickerRow(
                photos = segmentPhotos,
                onPhotoSelected = { uri ->
                    scope.launch {
                        viewModel.addPhoto(Photo(uri = uri.toString(), segmentId = segmentId))
                    }
                },
                onPhotoDeleted = { photo ->
                    scope.launch {
                        viewModel.deletePhoto(photo)
                    }
                },
                modifier = Modifier.padding(8.dp)
            )

            HorizontalDivider()

            segmentWithDetails?.let { details ->
                // The Boat Concept for Segment
                BoatSummary(
                    fishermanCount = details.fishermen.size,
                    onBoatClick = { showFishermenDialog = true },
                    onAddClick = { navigateToSegmentBoatLoad(segmentId, tripId) }
                )

                HorizontalDivider()

                Text(
                    text = "Fish Caught:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(fishList) { fish ->
                        FishItem(
                            fish = fish,
                            viewModel = viewModel,
                            onDelete = {
                                scope.launch {
                                    val fishObj = viewModel.getFishById(fish.id)
                                    if (fishObj != null) {
                                        viewModel.deleteFishObject(fishObj)
                                    }
                                }
                            },
                            onShowMap = {
                                if (fish.latitude != null && fish.longitude != null) {
                                    val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${fish.latitude},${fish.longitude}")
                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open map", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onUpdateLocation = {
                                fishToUpdateLocation = fish
                            }
                        )
                    }
                }

                if (showFishermenDialog) {
                    AlertDialog(
                        onDismissRequest = { showFishermenDialog = false },
                        title = { Text("Fishermen in Segment Boat") },
                        text = {
                            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                                items(details.fishermen) { fisherman ->
                                    FishermanItem(
                                        fisherman = fisherman,
                                        onDelete = {
                                            scope.launch {
                                                viewModel.deleteFishermanFromSegment(segmentId, fisherman.id)
                                            }
                                        },
                                        onClick = {
                                            showFishermenDialog = false
                                            navigateToFishermanDetails(fisherman.id)
                                        }
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showFishermenDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }

                if (showAddFishDialog) {
                    AddFishDialog(
                        viewModel = viewModel,
                        speciesList = allSpecies,
                        // Use segment fishermen if available, otherwise trip fishermen as fallback
                        fishermenList = details.fishermen,
                        onDismiss = { showAddFishDialog = false },
                        onConfirm = { speciesId, fishermanId, lureId, length, released ->
                            scope.launch {
                                val location = getCurrentLocation(context)
                                viewModel.addFish(
                                    Fish(
                                        speciesId = speciesId,
                                        fishermanId = fishermanId,
                                        tripId = tripId,
                                        segmentId = segmentId,
                                        lureId = lureId,
                                        length = length,
                                        isReleased = released,
                                        latitude = location?.first,
                                        longitude = location?.second
                                    )
                                )
                                showAddFishDialog = false
                            }
                        },
                        onAddSpecies = { name ->
                            scope.launch { viewModel.addSpecies(name) }
                        }
                    )
                }
            } ?: run {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            }
        }

        fishToUpdateLocation?.let { fishDetails ->
            MapPickerSelectionDialog(
                initialLatLng = if (fishDetails.latitude != null && fishDetails.longitude != null) 
                    LatLng(fishDetails.latitude, fishDetails.longitude) else null,
                onDismiss = { fishToUpdateLocation = null },
                onConfirm = { latLng ->
                    scope.launch {
                        val fish = viewModel.getFishById(fishDetails.id)
                        if (fish != null) {
                            viewModel.updateFish(fish.copy(latitude = latLng.latitude, longitude = latLng.longitude))
                        }
                        fishToUpdateLocation = null
                    }
                }
            )
        }
    }
}
