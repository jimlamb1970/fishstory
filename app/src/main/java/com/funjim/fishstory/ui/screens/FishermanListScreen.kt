package com.funjim.fishstory.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.ui.utils.FishermanItem
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import com.funjim.fishstory.viewmodels.FishermanListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanListScreen(
    viewModel: FishermanListViewModel,
    navigateToFishermanDetails: (String) -> Unit,
    navigateBack: () -> Unit
) {
    val fishermanSummaries by viewModel.fishermanSummaries.collectAsStateWithLifecycle()
    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newNickname by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fishermen") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Fisherman")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Sort Buttons
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(8.dp)) {
                SortChip("Name", currentOrder == FishermanSortOrder.NAME_AZ) {
                    viewModel.updateSortOrder(FishermanSortOrder.NAME_AZ)
                }
                SortChip("Most Catches", currentOrder == FishermanSortOrder.MOST_CATCHES) {
                    viewModel.updateSortOrder(FishermanSortOrder.MOST_CATCHES)
                }
                SortChip("Most Released", currentOrder == FishermanSortOrder.MOST_RELEASED) {
                    viewModel.updateSortOrder(FishermanSortOrder.MOST_RELEASED)
                }
                SortChip("Most Trips", currentOrder == FishermanSortOrder.MOST_TRIPS) {
                    viewModel.updateSortOrder(FishermanSortOrder.MOST_TRIPS)
                }
                
                Spacer(Modifier.weight(1f))

                IconButton(onClick = { viewModel.toggleReverse() }) {
                    Icon(
                        imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Reverse Sort",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn {
                val totalItems = fishermanSummaries.size
                itemsIndexed(fishermanSummaries) { index, fisherman ->
                    FishermanItem(
                        fisherman = fisherman,
                        index = index,
                        totalItems = totalItems,
                        onDelete = {
                            viewModel.deleteFisherman(fisherman.fisherman)
                        },
                        onClick = {
                            navigateToFishermanDetails(fisherman.fisherman.id)
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Fisherman") },
                text = {
                    Column {
                        TextField(
                            value = newFirstName,
                            onValueChange = { newFirstName = it },
                            label = { Text("First Name") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newNickname,
                            onValueChange = { newNickname = it },
                            label = { Text("Nickname") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newLastName,
                            onValueChange = { newLastName = it },
                            label = { Text("Last Name") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newFirstName.isNotBlank() && newLastName.isNotBlank()) {
                            scope.launch {
                                val fisherman = Fisherman(
                                    firstName = newFirstName.trim(),
                                    lastName = newLastName.trim(),
                                    nickname = newNickname.trim()
                                )

                                viewModel.addFisherman(fisherman)

                                newFirstName = ""
                                newLastName = ""
                                newNickname = ""
                                showAddDialog = false
                            }
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = MaterialTheme.colorScheme.tertiary,
            selectedBorderWidth = 2.dp,
            borderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
        ),
        modifier = Modifier.padding(end = 4.dp)
    )
}

@Composable
fun SortingArrow(
    isReversed: Boolean,
    onClick: () -> Unit
) {
    // 1. Calculate the rotation angle based on the boolean state
    val rotationAngle by animateFloatAsState(
        targetValue = if (isReversed) 180f else 0f,
        label = "ArrowRotation"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.ArrowDownward, // Use a single base icon
            contentDescription = "Toggle Sort Direction",
            modifier = Modifier.rotate(rotationAngle), // 2. Apply the rotation
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
