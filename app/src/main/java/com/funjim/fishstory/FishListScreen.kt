package com.funjim.fishstory

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.ui.AddFishDialog
import com.funjim.fishstory.ui.FishItem
import com.funjim.fishstory.ui.getCurrentLocation
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    viewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    val activeSegments by viewModel.activeSegments.collectAsState(initial = emptyList())
    var selectedSegment by remember { mutableStateOf<Segment?>(null) }
    
    // Default to the most recent active segment
    LaunchedEffect(activeSegments) {
        if (selectedSegment == null && activeSegments.isNotEmpty()) {
            selectedSegment = activeSegments.first()
        }
    }

    val fishList by produceState<List<FishWithDetails>>(initialValue = emptyList(), key1 = selectedSegment) {
        selectedSegment?.let { segment ->
            viewModel.getFishForSegment(segment.id).collect { value = it }
        } ?: run { value = emptyList() }
    }
    
    val allSpecies by viewModel.species.collectAsState(initial = emptyList())
    val tripDetails by produceState<com.funjim.fishstory.model.TripWithDetails?>(initialValue = null, key1 = selectedSegment) {
        selectedSegment?.let { segment ->
            viewModel.getTripWithDetails(segment.tripId).collect { value = it }
        } ?: run { value = null }
    }

    var showAddFishDialog by remember { mutableStateOf(false) }
    var segmentExpanded by remember { mutableStateOf(false) }

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
                title = { Text("Fish Caught") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedSegment != null) {
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
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Segment Selector
            if (activeSegments.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = segmentExpanded,
                    onExpandedChange = { segmentExpanded = !segmentExpanded },
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    TextField(
                        value = selectedSegment?.name ?: "Select Active Segment",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Current Segment") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = segmentExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = segmentExpanded,
                        onDismissRequest = { segmentExpanded = false }
                    ) {
                        activeSegments.forEach { segment ->
                            DropdownMenuItem(
                                text = { Text(segment.name) },
                                onClick = {
                                    selectedSegment = segment
                                    segmentExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    "No active segments found. Please start a segment to log fish.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red
                )
            }

            HorizontalDivider()

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
                            // Map logic could be moved to shared utility if needed
                        },
                        onUpdateLocation = {
                            // fishToUpdateLocation = fish
                        }
                    )
                }
            }
        }

        if (showAddFishDialog && tripDetails != null && selectedSegment != null) {
            AddFishDialog(
                viewModel = viewModel,
                speciesList = allSpecies,
                fishermenList = tripDetails!!.fishermen,
                onDismiss = { showAddFishDialog = false },
                onConfirm = { speciesId, fishermanId, lureId, length, released ->
                    scope.launch {
                        val location = getCurrentLocation(context)
                        viewModel.addFish(
                            Fish(
                                speciesId = speciesId,
                                fishermanId = fishermanId,
                                tripId = selectedSegment!!.tripId,
                                segmentId = selectedSegment!!.id,
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
                onAddSpecies = { name, onAdded ->
                    scope.launch {
                        val newId = viewModel.addSpecies(name)
                        onAdded(newId.toInt())
                    }
                }
            )
        }
    }
}
