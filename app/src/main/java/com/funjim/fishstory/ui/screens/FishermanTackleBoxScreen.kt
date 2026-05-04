package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.LureSummaryWithColors
import com.funjim.fishstory.viewmodels.LureSortOrder
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanTackleBoxScreen(
    viewModel: LureViewModel,
    fishermanId: String,
    tackleBoxId: String,
    onAdd: () -> Unit,
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
                    TextButton(
                        onClick = onAdd,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    SortChip("Name",
                        currentOrder == LureSortOrder.NAME) {
                        viewModel.setSortOrder(LureSortOrder.NAME)
                    }
                    SortChip("Primary Color",
                        currentOrder == LureSortOrder.PRIMARY_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.PRIMARY_COLOR)
                    }
                    SortChip("Secondary Color",
                        currentOrder == LureSortOrder.SECONDARY_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.SECONDARY_COLOR)
                    }
                    SortChip("Glow Color",
                        currentOrder == LureSortOrder.GLOW_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.GLOW_COLOR)
                    }
                    SortChip("Glows",
                        currentOrder == LureSortOrder.GLOW) {
                        viewModel.setSortOrder(LureSortOrder.GLOW)
                    }
                    SortChip("Hook Type",
                        currentOrder == LureSortOrder.HOOK_TYPE) {
                        viewModel.setSortOrder(LureSortOrder.HOOK_TYPE)
                    }
                }

                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = { viewModel.toggleReverse() },
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        ).size(34.dp)
                ) {
                    Icon(
                        imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Reverse Sort",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (allLures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No lures in inventory.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val totalItems = allLures.size
                    itemsIndexed(allLures, key = { _, item -> item.lureSummary.lure.id }) { index, item ->
                        val inBox = item.lureSummary.lure.id in luresInBoxIds
                        LureTackleBoxItem(
                            item = item,
                            index = index,
                            totalItems = totalItems,
                            inTackleBox = inBox,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    if (checked) {
                                        viewModel.addLureToTackleBox(tackleBoxId, item.lureSummary.lure.id)
                                    } else {
                                        viewModel.removeLureFromTackleBox(tackleBoxId, item.lureSummary.lure.id)
                                    }
                                }
                            }
                        )
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
    item: LureSummaryWithColors,
    index: Int = 0,
    totalItems: Int = 0,
    inTackleBox: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    val backgroundColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }
    val borderColor = if (index % 2 == 0 || totalItems <= 3) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        border = BorderStroke(1.dp, color = borderColor)
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
                    text = item.lureSummary.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (inTackleBox) FontWeight.Medium else FontWeight.Normal
                )
                // Secondary details row
                val details = buildList {
                    if (item.lureSummary.lure.glows) add("Glows${if (!item.glowColorName.isNullOrBlank()) " (${item.glowColorName})" else ""}")
                    add(if (item.lureSummary.lure.hasSingleHook) "Single hook" else "Treble hook")
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
}
