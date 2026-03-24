package com.funjim.fishstory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.ui.LureItem
import com.funjim.fishstory.ui.LureDialog
import com.funjim.fishstory.ui.ManageColorsDialog
import com.funjim.fishstory.ui.PhotoPickerRow
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanDetailsScreen(viewModel: MainViewModel, fishermanId: Int, navigateBack: () -> Unit) {
    val fishermanWithDetails by viewModel.getFishermanWithDetails(fishermanId).collectAsState(initial = null)
    val allTrips by viewModel.trips.collectAsState(initial = emptyList())
    val allLures by viewModel.lures.collectAsState(initial = emptyList())
    val colors by viewModel.lureColors.collectAsState(initial = emptyList())
    val fishermanPhotos by viewModel.getPhotosForFisherman(fishermanId).collectAsState(initial = emptyList())

    var showEditFishermanDialog by remember { mutableStateOf(false) }
    var showAddTripSelectionDialog by remember { mutableStateOf(false) }
    var showAddTripDialog by remember { mutableStateOf(false) }
    var showLureSelectionDialog by remember { mutableStateOf(false) }
    var showAddLureDialog by remember { mutableStateOf(false) }
    var showManageColorsDialog by remember { mutableStateOf(false) }
    var lureToEdit by remember { mutableStateOf<Lure?>(null) }
    
    var newTripName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fisherman Details") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditFishermanDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Fisherman")
                    }
                    IconButton(onClick = { showManageColorsDialog = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Manage Colors")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    onClick = { showLureSelectionDialog = true },
                    modifier = Modifier.padding(bottom = 8.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Lure") }
                )
                ExtendedFloatingActionButton(
                    onClick = { showAddTripSelectionDialog = true },
                    icon = { Icon(Icons.Default.Event, contentDescription = null) },
                    text = { Text("Add Trip") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            fishermanWithDetails?.let { details ->
                Text(
                    text = "Fisherman: ${details.fisherman.fullName}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                PhotoPickerRow(
                    photos = fishermanPhotos,
                    onPhotoSelected = { uri ->
                        scope.launch {
                            viewModel.addPhoto(Photo(uri = uri.toString(), fishermanId = fishermanId))
                        }
                    },
                    onPhotoDeleted = { photo ->
                        scope.launch {
                            viewModel.deletePhoto(photo)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    text = "Tackle Box:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                val fishermanLures = details.tackleBoxWithLures?.lures ?: emptyList()
                
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(fishermanLures) { lure ->
                        val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name ?: "Unknown Color"
                        val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name ?: "Unknown Color"
                        val glowColorName = colors.find { it.id == lure.glowColorId }?.name
                        LureItem(
                            lure = lure,
                            primaryColorName = primaryColorName,
                            secondaryColorName = secondaryColorName,
                            glowColorName = glowColorName,
                            viewModel = viewModel,
                            onEdit = { lureToEdit = lure },
                            onDelete = {
                                scope.launch { viewModel.removeLureFromFishermanTackleBox(fishermanId, lure.id) }
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    text = "Trips Joined:",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(details.trips) { trip ->
                        TripDetailItem(
                            trip = trip,
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteTripFromFisherman(trip.id, fishermanId)
                                }
                            }
                        )
                    }
                }
            } ?: run {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            }
        }

        // Edit Fisherman Dialog
        if (showEditFishermanDialog && fishermanWithDetails != null) {
            EditFishermanDialog(
                initialFisherman = fishermanWithDetails!!.fisherman,
                onDismiss = { showEditFishermanDialog = false },
                onConfirm = { updatedFisherman ->
                    scope.launch {
                        viewModel.updateFisherman(updatedFisherman)
                    }
                    showEditFishermanDialog = false
                }
            )
        }

        // Lure Selection Dialog (Similar to LureListScreen)
        if (showLureSelectionDialog) {
            val fishermanLures = fishermanWithDetails?.tackleBoxWithLures?.lures ?: emptyList()
            val luresNotInTackleBox = allLures.filter { lure -> fishermanLures.none { it.id == lure.id } }
            
            AlertDialog(
                onDismissRequest = { showLureSelectionDialog = false },
                title = { Text("Add Lure to Tackle Box") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showLureSelectionDialog = false
                                    showAddLureDialog = true
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Create brand new lure...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        HorizontalDivider()

                        LazyColumn {
                            items(luresNotInTackleBox) { lure ->
                                val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
                                val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
                                val glowColorName = colors.find { it.id == lure.glowColorId }?.name
                                Text(
                                    text = lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                viewModel.addLureToFishermanTackleBox(fishermanId, lure.id)
                                            }
                                            showLureSelectionDialog = false
                                        }
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    Button(onClick = { showLureSelectionDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Add Lure Dialog
        if (showAddLureDialog) {
            LureDialog(
                colors = colors,
                onDismiss = { showAddLureDialog = false },
                onConfirm = { name, primaryColorId, secondaryColorId, isSingleHook, glows, glowColorId ->
                    scope.launch {
                        val newLureId = viewModel.addLure(Lure(name = name, primaryColorId = primaryColorId, secondaryColorId = secondaryColorId, hasSingleHook = isSingleHook, glows = glows, glowColorId = glowColorId))
                        viewModel.addLureToFishermanTackleBox(fishermanId, newLureId.toInt())
                        showAddLureDialog = false
                    }
                },
                onAddColor = { colorName, onComplete ->
                    scope.launch {
                        val newId = viewModel.addLureColor(LureColor(name = colorName))
                        onComplete(newId.toInt())
                    }
                }
            )
        }

        // Edit Lure Dialog
        lureToEdit?.let { lure ->
            LureDialog(
                initialName = lure.name,
                initialPrimaryColorId = lure.primaryColorId,
                initialSecondaryColorId = lure.secondaryColorId,
                initialIsSingleHook = lure.hasSingleHook,
                initialGlows = lure.glows,
                initialGlowColorId = lure.glowColorId,
                title = "Edit Lure",
                colors = colors,
                onDismiss = { lureToEdit = null },
                onConfirm = { name, primaryColorId, secondaryColorId, isSingleHook, glows, glowColorId ->
                    scope.launch {
                        viewModel.addLure(lure.copy(name = name, primaryColorId = primaryColorId, secondaryColorId = secondaryColorId, hasSingleHook = isSingleHook, glows = glows, glowColorId = glowColorId))
                        lureToEdit = null
                    }
                },
                onAddColor = { colorName, onComplete ->
                    scope.launch {
                        val newId = viewModel.addLureColor(LureColor(name = colorName))
                        onComplete(newId.toInt())
                    }
                }
            )
        }

        // Manage Colors Dialog
        if (showManageColorsDialog) {
            ManageColorsDialog(
                colors = colors,
                onDismiss = { showManageColorsDialog = false },
                onAddColor = { colorName ->
                    scope.launch { viewModel.addLureColor(LureColor(name = colorName)) }
                },
                onDeleteColor = { color ->
                    scope.launch { viewModel.deleteLureColor(color) }
                }
            )
        }

        // Selection Dialog (Existing or New Trip)
        if (showAddTripSelectionDialog) {
            val currentTripIds = fishermanWithDetails?.trips?.map { it.id } ?: emptyList()
            val availableTrips = allTrips.filter { it.id !in currentTripIds }

            AlertDialog(
                onDismissRequest = { showAddTripSelectionDialog = false },
                title = { Text("Add Trip") },
                confirmButton = {},
                dismissButton = {
                    Button(onClick = { showAddTripSelectionDialog = false }) { Text("Cancel") }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddTripSelectionDialog = false
                                    showAddTripDialog = true
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Add new...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                        }

                        HorizontalDivider()

                        LazyColumn {
                            items(availableTrips) { trip ->
                                Text(
                                    text = trip.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                viewModel.addFishermanToTrip(trip.id, fishermanId)
                                            }
                                            showAddTripSelectionDialog = false
                                        }
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            )
        }

        // Dialog for creating a brand new Trip
        if (showAddTripDialog) {
            AlertDialog(
                onDismissRequest = { showAddTripDialog = false },
                title = { Text("New Trip") },
                text = {
                    TextField(
                        value = newTripName,
                        onValueChange = { newTripName = it },
                        placeholder = { Text("Trip Name (e.g. Lake Superior)") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (newTripName.isNotBlank()) {
                            scope.launch {
                                val tripId = viewModel.addTrip(Trip(name = newTripName))
                                viewModel.addFishermanToTrip(tripId.toInt(), fishermanId)
                                showAddTripDialog = false
                                newTripName = ""
                            }
                        }
                    }) { Text("Add") }
                },
                dismissButton = {
                    Button(onClick = { showAddTripDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun EditFishermanDialog(
    initialFisherman: Fisherman,
    onDismiss: () -> Unit,
    onConfirm: (Fisherman) -> Unit
) {
    var firstName by remember { mutableStateOf(initialFisherman.firstName) }
    var lastName by remember { mutableStateOf(initialFisherman.lastName) }
    var nickname by remember { mutableStateOf(initialFisherman.nickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Fisherman") },
        text = {
            Column {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isNotBlank() && lastName.isNotBlank()) {
                    onConfirm(initialFisherman.copy(firstName = firstName, lastName = lastName, nickname = nickname))
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TripDetailItem(trip: Trip, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(trip.name, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove from Fisherman", tint = Color.Red)
            }
        }
    }
}
