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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.ui.FishItem
import com.funjim.fishstory.ui.ManageSpeciesDialog
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishListScreen(
    viewModel: MainViewModel,
    navigateBack: () -> Unit,
    onAddFish: (tripId: Int, segmentId: Int, fishId: Int?) -> Unit
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

    var permissionGranted by remember { mutableStateOf(false) }
    var segmentExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            permissionGranted = true
            // TRIGGER NAVIGATION HERE after permission is granted
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
            selectedSegment?.let { segment ->
                ExtendedFloatingActionButton(
                    onClick = {
                        if (permissionGranted) {
                            onAddFish(segment.tripId, segment.id, null)
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
                items(fishList) { fishDetails ->
                    FishItem(
                        fish = fishDetails,
                        viewModel = viewModel,
                        onEdit = {
                            scope.launch {
                                onAddFish(selectedSegment?.tripId ?: -1, selectedSegment?.id ?: -1, fishDetails.id)
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
                            // Map logic could be moved to shared utility if needed
                        },
                        onUpdateLocation = {
                            // handled by common logic or could be triggered here
                        }
                    )
                }
            }
        }

        if (showManageSpeciesDialog) {
            ManageSpeciesDialog(
                species = allSpecies,
                onDismiss = { showManageSpeciesDialog = false },
                onAddSpecies = { speciesName ->
                    scope.launch { viewModel.addSpecies(speciesName) }
                },
                onDeleteSpecies = { speciesName ->
                    scope.launch { viewModel.deleteSpecies(speciesName) }
                }
            )
        }
    }
}
