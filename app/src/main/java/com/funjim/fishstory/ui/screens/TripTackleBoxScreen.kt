package com.funjim.fishstory.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripTackleBoxScreen(
    viewModel: TripViewModel,
    tripId: String,
    navigateBack: () -> Unit
) {
    LaunchedEffect(tripId) {
        viewModel.selectTrip(tripId)
    }

    val tripSummary by viewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tackle Boxes") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val tripWithFishermen by viewModel.getTripWithFishermen(tripId).collectAsState(initial = null)
//        val fishermen = tripSummary?.fishermen ?: emptyList()
        val fishermen = tripWithFishermen?.fishermen ?: emptyList()

        if (fishermen.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No fishermen on this trip.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(fishermen) { fisherman ->
                    FishermanTackleBoxCard(
                        fisherman = fisherman,
                        tripId = tripId,
                        viewModel = viewModel,
                        onTackleBoxChanged = { newTackleBoxId ->
                            scope.launch {
                                viewModel.updateTripFishermanTackleBox(
                                    tripId = tripId,
                                    fishermanId = fisherman.id,
                                    tackleBoxId = newTackleBoxId
                                )
                            }
                        },
                        onCreateNewTackleBox = { name ->
                            scope.launch {
                                viewModel.createAndAssignTackleBox(
                                    fishermanId = fisherman.id,
                                    tripId = tripId,
                                    name = name
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanTackleBoxCard(
    fisherman: Fisherman,
    tripId: String,
    viewModel: TripViewModel,
    onTackleBoxChanged: (String) -> Unit,
    onCreateNewTackleBox: (String) -> Unit
) {
    // The tackle box currently assigned to this fisherman for this trip
    val assignedTackleBoxId by viewModel.getTripFishermanTackleBoxId(tripId, fisherman.id)
        .collectAsState(initial = null)

    // All tackle boxes belonging to this fisherman
    val availableTackleBoxes by viewModel.getTackleBoxesForFisherman(fisherman.id)
        .collectAsState(initial = emptyList())

    // Lure count for the currently assigned tackle box
    val lureCount by viewModel.getLureCountForTackleBox(assignedTackleBoxId)
        .collectAsState(initial = 0)

    val assignedBox = availableTackleBoxes.find { it.id == assignedTackleBoxId }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTackleBoxName by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fisherman name
            Text(
                text = fisherman.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tackle box dropdown
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = assignedBox?.name ?: "No tackle box assigned",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tackle Box") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    availableTackleBoxes.forEach { box ->
                        DropdownMenuItem(
                            text = { Text(box.name) },
                            onClick = {
                                onTackleBoxChanged(box.id)
                                dropdownExpanded = false
                            }
                        )
                    }

                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Create new tackle box...", color = MaterialTheme.colorScheme.primary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            dropdownExpanded = false
                            newTackleBoxName = ""
                            showCreateDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lure count
            Text(
                text = if (assignedTackleBoxId != null) "$lureCount lure${if (lureCount != 1) "s" else ""} in this box"
                       else "Select a tackle box to see lures",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Create new tackle box dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Tackle Box") },
            text = {
                OutlinedTextField(
                    value = newTackleBoxName,
                    onValueChange = { newTackleBoxName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTackleBoxName.isNotBlank()) {
                            onCreateNewTackleBox(newTackleBoxName.trim())
                            showCreateDialog = false
                        }
                    },
                    enabled = newTackleBoxName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
