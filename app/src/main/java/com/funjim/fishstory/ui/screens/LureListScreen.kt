package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.ui.LureItem
import com.funjim.fishstory.ui.ManageColorsDialog
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureListScreen(
    viewModel: LureViewModel,
    initialFishermanId: String? = null,
    onAddLure: (String?) -> Unit, // Callback for Navigating to Add/Edit screen
    navigateBack: () -> Unit
) {
    val allLures by viewModel.lures.collectAsState(initial = emptyList())
    val colors by viewModel.lureColors.collectAsState(initial = emptyList())
    val fishermen by viewModel.fishermen.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showManageColorsDialog by remember { mutableStateOf(false) }

    var selectedFisherman by remember { mutableStateOf<Fisherman?>(null) }
    var fishermanDropdownExpanded by remember { mutableStateOf(false) }

    val sortedFishermen = remember(fishermen) {
        fishermen.sortedBy { it.fullName }
    }

    // Set initial fisherman if provided
    LaunchedEffect(fishermen, initialFishermanId) {
        if (initialFishermanId != null && selectedFisherman == null) {
            selectedFisherman = fishermen.find { it.id == initialFishermanId }
        }
    }

    val luresForFisherman by remember(selectedFisherman) {
        if (selectedFisherman != null) {
            viewModel.getLuresForFisherman(selectedFisherman!!.id)
        } else {
            flowOf(emptyList<Lure>())
        }
    }.collectAsState(initial = emptyList())

    // Transform lures into a displayable list with color names pre-resolved
    val displayLures = remember(allLures, colors) {
        allLures.map { lure ->
            val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
            val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
            val glowColorName = colors.find { it.id == lure.glowColorId }?.name

            LureWithDisplay(
                lure = lure,
                primaryColorName = primaryColorName,
                secondaryColorName = secondaryColorName,
                glowColorName = glowColorName,
                displayName = lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName)
            )
        }.sortedBy { it.displayName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lures") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showManageColorsDialog = true }) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "Manage Colors")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedFisherman == null) {
                FloatingActionButton(onClick = { onAddLure(null) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Lure")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ExposedDropdownMenuBox(
                expanded = fishermanDropdownExpanded,
                onExpandedChange = { fishermanDropdownExpanded = !fishermanDropdownExpanded },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                TextField(
                    value = selectedFisherman?.fullName ?: "All fishermen",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lures For:") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fishermanDropdownExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = fishermanDropdownExpanded,
                    onDismissRequest = { fishermanDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All fishermen") },
                        onClick = {
                            selectedFisherman = null
                            fishermanDropdownExpanded = false
                        }
                    )
                    sortedFishermen.forEach { fisherman ->
                        DropdownMenuItem(
                            text = { Text(fisherman.fullName) },
                            onClick = {
                                selectedFisherman = fisherman
                                fishermanDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (selectedFisherman == null) {
                // All Lures Mode
                if (allLures.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No lures found. Add one!")
                    }
                } else {
                    val allPhotos by viewModel.lurePhotos.collectAsStateWithLifecycle()
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(displayLures, key = { it.lure.id }) { item ->
                            val photos = allPhotos[item.lure.id] ?: emptyList()

                            LureItem(
                                lure = item.lure,
                                primaryColorName = item.primaryColorName,
                                secondaryColorName = item.secondaryColorName,
                                glowColorName = item.glowColorName,
                                photos = photos,
                                onEdit = { onAddLure(item.lure.id) },
                                onAddPhoto = { photo ->
                                    scope.launch { viewModel.addPhoto(photo) }
                                },
                                onDeletePhoto = { photo ->
                                    scope.launch { viewModel.deletePhoto(photo) }
                                },
                                onDelete = {
                                    scope.launch { viewModel.deleteLure(item.lure) }
                                }
                            )
                        }
                    }
                }
            } else {
                // Specific Fisherman Mode (Tackle Box management)
                val luresNotInTackleBoxSorted = remember(allLures, luresForFisherman, colors) {
                    allLures.filter { lure -> luresForFisherman.none { it.id == lure.id } }
                        .map { lure ->
                            val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
                            val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
                            val glowColorName = colors.find { it.id == lure.glowColorId }?.name
                            val displayName = lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName)
                            lure to displayName
                        }.sortedBy { it.second }
                }

                val luresForFishermanSorted = remember(luresForFisherman, colors) {
                    luresForFisherman.map { lure ->
                        val primaryColorName = colors.find { it.id == lure.primaryColorId }?.name
                        val secondaryColorName = colors.find { it.id == lure.secondaryColorId }?.name
                        val glowColorName = colors.find { it.id == lure.glowColorId }?.name
                        val displayName = lure.getDisplayName(primaryColorName, secondaryColorName, glowColorName)
                        lure to displayName
                    }.sortedBy { it.second }
                }

                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text(
                        "Available Lures (Click to add)",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Top List Box: Available Lures
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(luresNotInTackleBoxSorted, key = { it.first.id }) { (lure, displayName) ->
                                ListItem(
                                    headlineContent = { Text(displayName) },
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            viewModel.addLureToFishermanTackleBox(selectedFisherman!!.id, lure.id)
                                        }
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                        if (luresNotInTackleBoxSorted.isEmpty()) {
                            Text(
                                "All lures assigned",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    // Tacklebox Icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = "Tacklebox",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Tacklebox", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Text(
                        "In Tacklebox (Click to remove)",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Bottom List Box: In Tacklebox
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(luresForFishermanSorted, key = { it.first.id }) { (lure, displayName) ->
                                ListItem(
                                    headlineContent = { Text(displayName) },
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            viewModel.removeLureFromFishermanTackleBox(selectedFisherman!!.id, lure.id)
                                        }
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        }
                        if (luresForFishermanSorted.isEmpty()) {
                            Text(
                                "Tacklebox is empty",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

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
    }
}

private data class LureWithDisplay(
    val lure: Lure,
    val primaryColorName: String?,
    val secondaryColorName: String?,
    val glowColorName: String?,
    val displayName: String
)
