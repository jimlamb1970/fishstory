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
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentTackleBoxScreen(
    viewModel: TripViewModel,
    segmentId: String,
    navigateBack: () -> Unit
) {
    LaunchedEffect(segmentId) {
        viewModel.selectSegment(segmentId)
    }

    val fishermen by viewModel.getFishermenForSegment(segmentId).collectAsState(initial = null)
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

        if (fishermen == null) {
            CircularProgressIndicator()
        } else if (fishermen!!.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No fishermen on this segment.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(fishermen!!) { fisherman ->
                    SegmentFishermanTackleBoxCard(
                        fisherman = fisherman,
                        segmentId = segmentId,
                        viewModel = viewModel,
                        onTackleBoxChanged = { newTackleBoxId ->
                            scope.launch {
                                viewModel.upsertSegmentFishermanCrossRef(
                                    segmentId = segmentId,
                                    fishermanId = fisherman.id,
                                    tackleBoxId = newTackleBoxId
                                )
                            }
                        },
                        onCreateNewTackleBox = { name ->
                            scope.launch {
                                viewModel.createAndAssignSegmentTackleBox(
                                    fishermanId = fisherman.id,
                                    segmentId = segmentId,
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

// TODO - update tackle box card to be expandable and editable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentFishermanTackleBoxCard(
    fisherman: Fisherman,
    segmentId: String,
    viewModel: TripViewModel,
    onTackleBoxChanged: (String) -> Unit,
    onCreateNewTackleBox: (String) -> Unit
) {
    // The tackle box currently assigned to this fisherman for this trip
    val assignedTackleBoxId by viewModel.getSegmentFishermanTackleBoxId(segmentId, fisherman.id)
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
