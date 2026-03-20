package com.funjim.fishstory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.ui.LureItem
import com.funjim.fishstory.ui.LureDialog
import com.funjim.fishstory.ui.ManageColorsDialog
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureListScreen(viewModel: MainViewModel, navigateBack: () -> Unit) {
    val allLures by viewModel.lures.collectAsState(initial = emptyList())
    val colors by viewModel.lureColors.collectAsState(initial = emptyList())
    val fishermen by viewModel.fishermen.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var showAddLureDialog by remember { mutableStateOf(false) }
    var lureToEdit by remember { mutableStateOf<Lure?>(null) }
    var showManageColorsDialog by remember { mutableStateOf(false) }
    var showLureSelectionDialog by remember { mutableStateOf(false) }

    var selectedFisherman by remember { mutableStateOf<Fisherman?>(null) }
    var fishermanDropdownExpanded by remember { mutableStateOf(false) }

    val luresForFisherman by remember(selectedFisherman) {
        if (selectedFisherman != null) {
            viewModel.getLuresForFisherman(selectedFisherman!!.id)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList<Lure>())
        }
    }.collectAsState(initial = emptyList())

    LaunchedEffect(fishermen) {
        if (selectedFisherman == null && fishermen.isNotEmpty()) {
            selectedFisherman = fishermen.first()
        }
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
                        Icon(Icons.Default.Edit, contentDescription = "Manage Colors")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedFisherman != null) {
                FloatingActionButton(onClick = { showLureSelectionDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Lure to Tackle Box")
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
                    value = selectedFisherman?.fullName ?: "Select Fisherman",
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
                    fishermen.forEach { fisherman ->
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

            if (luresForFisherman.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No lures found for ${selectedFisherman?.fullName ?: "this fisherman"}. Add one!")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(luresForFisherman) { lure ->
                        val colorName = colors.find { it.id == lure.colorId }?.name ?: "Unknown Color"
                        LureItem(
                            lure = lure,
                            colorName = colorName,
                            viewModel = viewModel,
                            onEdit = { lureToEdit = lure },
                            onDelete = {
                                scope.launch { viewModel.deleteLure(lure) }
                            }
                        )
                    }
                }
            }
        }

        if (showLureSelectionDialog && selectedFisherman != null) {
            val luresNotInTackleBox = allLures.filter { lure -> luresForFisherman.none { it.id == lure.id } }
            
            AlertDialog(
                onDismissRequest = { showLureSelectionDialog = false },
                title = { Text("Add Lure to ${selectedFisherman?.fullName}'s Tackle Box") },
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
                                Text(
                                    text = lure.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            scope.launch {
                                                viewModel.addLureToFishermanTackleBox(selectedFisherman!!.id, lure.id)
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

        if (showAddLureDialog) {
            LureDialog(
                colors = colors,
                onDismiss = { showAddLureDialog = false },
                onConfirm = { name, colorId, isSingleHook ->
                    scope.launch {
                        val newLureId = viewModel.addLure(Lure(name = name, colorId = colorId, hasSingleHook = isSingleHook))
                        selectedFisherman?.let {
                            viewModel.addLureToFishermanTackleBox(it.id, newLureId.toInt())
                        }
                        showAddLureDialog = false
                    }
                }
            )
        }

        lureToEdit?.let { lure ->
            LureDialog(
                initialName = lure.name,
                initialColorId = lure.colorId,
                initialIsSingleHook = lure.hasSingleHook,
                title = "Edit Lure",
                colors = colors,
                onDismiss = { lureToEdit = null },
                onConfirm = { name, colorId, isSingleHook ->
                    scope.launch {
                        viewModel.addLure(lure.copy(name = name, colorId = colorId, hasSingleHook = isSingleHook))
                        lureToEdit = null
                    }
                }
            )
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
