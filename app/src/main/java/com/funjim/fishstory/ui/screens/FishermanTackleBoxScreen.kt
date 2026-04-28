package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.viewmodels.LureSortOrder
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanTackleBoxScreen(
    viewModel: LureViewModel,
    fishermanId: String,
    tackleBoxId: String,
    navigateBack: () -> Unit
) {
    LaunchedEffect(fishermanId) {
        viewModel.selectFisherman(fishermanId)
        viewModel.selectTackleBox(tackleBoxId)
    }

    val allLures by viewModel.luresWithDisplay.collectAsState(initial = emptyList())
    val fisherman by viewModel.selectedFisherman.collectAsStateWithLifecycle()
    val luresInBox by viewModel.tackleBoxWithLures.collectAsState(initial = emptyList())
    val tackleBox by viewModel.selectedTackleBox.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    var showRenameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    // Build a set of IDs in the tackle box for 'inBox'' lookup
    val luresInBoxIds = remember(luresInBox) { luresInBox.map { it.id }.toSet() }

    val inBoxCount = luresInBoxIds.size

    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tackleBox?.name ?: "Tackle Box",
                            maxLines = 1
                        )
                        Text(
                            text = fisherman?.fullName ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
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
                },
                actions = {
                    IconButton(onClick = {
                        editedName = tackleBox?.name ?: ""
                        showRenameDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename Tackle Box")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Summary chip
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
            ) {
                Text(
                    text = "$inBoxCount lure${if (inBoxCount != 1) "s" else ""} in tackle box",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Sort chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    LureSortOrder.NAME to "Name",
                    LureSortOrder.PRIMARY_COLOR to "Primary Color",
                    LureSortOrder.SECONDARY_COLOR to "Secondary Color",
                    LureSortOrder.GLOW_COLOR to "Glow Color",
                    LureSortOrder.GLOW to "Glows",
                    LureSortOrder.HOOK_TYPE to "Hook Type"
                )
                items(chips) { (field, label) ->
                    val selected = currentOrder == field
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (currentOrder == field) {
                                viewModel.toggleReverse()
                            } else {
                                viewModel.setSortOrder(field)
                                if (reversed) {
                                    viewModel.toggleReverse()
                                }
                            }
                        },
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
                        label = {
                            Text(if (selected) "$label ${if (reversed) "↑" else "↓"}" else label)
                        }
                    )
                }
            }

            if (allLures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No lures in inventory.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(allLures, key = { it.lure.id }) { item ->
                        val inBox = item.lure.id in luresInBoxIds
                        LureTackleBoxItem(
                            item = item,
                            inTackleBox = inBox,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    if (checked) {
                                        viewModel.addLureToFishermanTackleBox(fishermanId, item.lure.id)
                                    } else {
                                        viewModel.removeLureFromFishermanTackleBox(fishermanId, item.lure.id)
                                    }
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Tackle Box") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Tackle Box Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tackleBox?.let {
                            scope.launch {
                                viewModel.updateTackleBox(it.copy(name = editedName.trim()))
                                showRenameDialog = false
                            }
                        }
                    },
                    enabled = editedName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LureTackleBoxItem(
    item: LureWithDisplay,
    inTackleBox: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = inTackleBox,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (inTackleBox) FontWeight.Medium else FontWeight.Normal
            )
            // Secondary details row
            val details = buildList {
                if (item.lure.glows) add("Glows${if (!item.glowColorName.isNullOrBlank()) " (${item.glowColorName})" else ""}")
                add(if (item.lure.hasSingleHook) "Single hook" else "Treble hook")
            }
            if (details.isNotEmpty()) {
                Text(
                    text = details.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
