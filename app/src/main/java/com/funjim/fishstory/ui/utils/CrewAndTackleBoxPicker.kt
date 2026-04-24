package com.funjim.fishstory.ui.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.viewmodels.TripViewModel

// ---------------------------------------------------------------------------
// Data model for a single fisherman's crew entry
// ---------------------------------------------------------------------------

/**
 * Represents one fisherman's participation in a trip or segment, including
 * which tackle box they have selected.
 */
data class CrewEntry(
    val fisherman: Fisherman,
    val isSelected: Boolean,
    val selectedTackleBoxId: String?
)

// ---------------------------------------------------------------------------
// CrewAndTackleBoxPicker
//
// A reusable composable for selecting fishermen and their tackle boxes.
// Callers provide the full list of eligible fishermen, the current crew
// state, and callbacks for all mutations — no ViewModel is referenced here
// directly, keeping this composable fully decoupled from any specific screen.
//
// Usage:
//   - In the wizard: eligible = all fishermen (trip step) or trip crew (segment step)
//   - When editing an existing trip: eligible = all fishermen, initialise
//     crewEntries from the existing TripFishermanCrossRef rows
//   - When editing an existing segment: eligible = trip's fishermen, initialise
//     crewEntries from the existing SegmentFishermanCrossRef rows
// ---------------------------------------------------------------------------

@Composable
fun CrewAndTackleBoxPicker(
    title: String,
    subtitle: String,
    // The pool of fishermen that can be added. Pass all fishermen for a trip
    // step; pass only the trip's fishermen for a segment step.
    eligibleFishermen: List<Fisherman>,
    // Current selection state — one entry per eligible fisherman.
    crewEntries: List<CrewEntry>,
    // Called when a fisherman's checked state changes.
    onSelectionChanged: (fishermanId: String, selected: Boolean) -> Unit,
    // Called when a fisherman's tackle box selection changes.
    onTackleBoxChanged: (fishermanId: String, tackleBoxId: String) -> Unit,
    // Provides available tackle boxes for a given fisherman.
    // Kept as a lambda so callers can back it with any data source.
    getTackleBoxesForFisherman: @Composable (fishermanId: String) -> List<TackleBox>,
    // Provides the lure count for a given tackle box (null = no box selected).
    getLureCount: @Composable (tackleBoxId: String?) -> Int,
    getLuresForTacklebox: @Composable (tackleBoxId: String?) -> List<String>,
    // CTA label on the confirm button.
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    // Optional: allow adding a new fisherman inline (pass null to hide button).
    onAddFisherman: ((firstName: String, lastName: String, nickname: String) -> Unit)? = null,
    onAddTackleBox: ((tackleBoxName: String, fishermanId: String) -> Unit)? = null
) {
    var showAddFishermanDialog by remember { mutableStateOf(false) }
    val selectedCount = crewEntries.count { it.isSelected }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$selectedCount selected",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (onAddFisherman != null) {
                TextButton(onClick = { showAddFishermanDialog = true }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New fisherman")
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (eligibleFishermen.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No fishermen available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val totalItems = crewEntries.size
                itemsIndexed(crewEntries, key = { _, entry -> entry.fisherman.id }) { index, entry ->
                    val availableBoxes = getTackleBoxesForFisherman(entry.fisherman.id)
                    val lureCount = getLureCount(entry.selectedTackleBoxId)
                    val lures = getLuresForTacklebox(entry.selectedTackleBoxId)
                    FishermanCrewRow(
                        entry = entry,
                        index = index,
                        totalItems = totalItems,
                        availableBoxes = availableBoxes,
                        lureCount = lureCount,
                        lures = lures,
                        onSelectionChanged = { selected ->
                            onSelectionChanged(entry.fisherman.id, selected)
                        },
                        onTackleBoxChanged = { boxId ->
                            onTackleBoxChanged(entry.fisherman.id, boxId)
                        },
                        onAddTackleBox = onAddTackleBox
                    )
                    HorizontalDivider()
                }
            }
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(confirmLabel)
        }
    }

    if (showAddFishermanDialog && onAddFisherman != null) {
        AddFishermanDialog(
            onDismiss = { showAddFishermanDialog = false },
            onAdd = { first, last, nick ->
                onAddFisherman(first, last, nick)
                showAddFishermanDialog = false
            }
        )
    }
}

// ---------------------------------------------------------------------------
// FishermanCrewRow — one fisherman with checkbox + tackle box dropdown
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FishermanCrewRow(
    entry: CrewEntry,
    index: Int = 0,
    totalItems: Int = 0,
    availableBoxes: List<TackleBox>,
    lureCount: Int,
    lures: List<String>,
    onSelectionChanged: (Boolean) -> Unit,
    onTackleBoxChanged: (String) -> Unit,
    onAddTackleBox: ((tackleBoxName: String, fishermanId: String) -> Unit)? = null
) {
    val selectedBox = availableBoxes.find { it.id == entry.selectedTackleBoxId }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newTackleBoxName by remember { mutableStateOf("") }

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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(), // Smoothly animates the expansion
        onClick = { if (selectedBox != null) expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            // Checkbox + name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = entry.isSelected,
                    onCheckedChange = onSelectionChanged
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = entry.fisherman.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (entry.isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            // Tackle box dropdown — only visible when fisherman is selected
            if (entry.isSelected) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = {
                        if (availableBoxes.isNotEmpty()) dropdownExpanded = !dropdownExpanded
                    },
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp, bottom = 4.dp)
                ) {
                    OutlinedTextField(
                        value = selectedBox?.name
                            ?: if (availableBoxes.isEmpty()) "No boxes available" else "Select tackle box",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tackle Box") },
                        trailingIcon = {
                            if (availableBoxes.isNotEmpty())
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        supportingText = if (entry.selectedTackleBoxId != null) {
                            { Text("$lureCount lure${if (lureCount != 1) "s" else ""}") }
                        } else null,
                        enabled = availableBoxes.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    if (availableBoxes.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            availableBoxes.forEach { box ->
                                DropdownMenuItem(
                                    text = { Text(box.name) },
                                    onClick = {
                                        onTackleBoxChanged(box.id)
                                        dropdownExpanded = false
                                    }
                                )
                            }

                            if (onAddTackleBox != null) {
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
                    }
                }

                if (expanded) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    lures.forEach { lure ->
                        Text(
                            text = "• $lure",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 32.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
    // Create new tackle box dialog
    if (showCreateDialog && onAddTackleBox != null) {
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
                            onAddTackleBox(newTackleBoxName.trim(), entry.fisherman.id)
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

// ---------------------------------------------------------------------------
// AddFishermanDialog
// ---------------------------------------------------------------------------

@Composable
fun AddFishermanDialog(
    onDismiss: () -> Unit,
    onAdd: (firstName: String, lastName: String, nickname: String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fisherman") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = firstName, onValueChange = { firstName = it },
                    label = { Text("First Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName, onValueChange = { lastName = it },
                    label = { Text("Last Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nickname, onValueChange = { nickname = it },
                    label = { Text("Nickname (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(firstName, lastName, nickname) },
                enabled = firstName.isNotBlank() || lastName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ---------------------------------------------------------------------------
// Helper: build CrewEntries from raw state
//
// Call this to derive the list that CrewAndTackleBoxPicker expects, given
// the raw sets your wizard/edit screen already tracks.
// ---------------------------------------------------------------------------

fun buildCrewEntries(
    eligibleFishermen: List<Fisherman>,
    selectedIds: Set<String>,
    tackleBoxSelections: Map<String, String?>
): List<CrewEntry> = eligibleFishermen.map { fisherman ->
    CrewEntry(
        fisherman = fisherman,
        isSelected = fisherman.id in selectedIds,
        selectedTackleBoxId = tackleBoxSelections[fisherman.id]
    )
}

// ---------------------------------------------------------------------------
// TripViewModelCrewPickerBridge
//
// A thin wrapper that connects CrewAndTackleBoxPicker to TripViewModel's
// flows, so call sites don't have to wire up the lambda boilerplate by hand.
// ---------------------------------------------------------------------------

@Composable
fun TripViewModelCrewPickerBridge(
    title: String,
    subtitle: String,
    eligibleFishermen: List<Fisherman>,
    selectedIds: Set<String>,
    tackleBoxSelections: Map<String, String?>,
    onSelectionChanged: (fishermanId: String, selected: Boolean) -> Unit,
    onTackleBoxChanged: (fishermanId: String, tackleBoxId: String) -> Unit,
    tripViewModel: TripViewModel,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onAddFisherman: ((String, String, String) -> Unit)? = null,
    onAddTackleBox: ((String, String) -> Unit)? = null
) {
    val crewEntries by remember(eligibleFishermen, selectedIds, key3 = tackleBoxSelections) {
        derivedStateOf { buildCrewEntries(eligibleFishermen, selectedIds, tackleBoxSelections) }
    }

    CrewAndTackleBoxPicker(
        title = title,
        subtitle = subtitle,
        eligibleFishermen = eligibleFishermen,
        crewEntries = crewEntries,
        onSelectionChanged = onSelectionChanged,
        onTackleBoxChanged = onTackleBoxChanged,
        getTackleBoxesForFisherman = { fishermanId ->
            tripViewModel.getTackleBoxesForFisherman(fishermanId)
                .collectAsState(initial = emptyList()).value
        },
        getLureCount = { tackleBoxId ->
            tripViewModel.getLureCountForTackleBox(tackleBoxId)
                .collectAsState(initial = 0).value
        },
        getLuresForTacklebox = { tackleBoxId ->
            tripViewModel.getLureNamesInTackleBox(tackleBoxId).collectAsState(initial = emptyList()).value
        },
        confirmLabel = confirmLabel,
        onConfirm = onConfirm,
        modifier = modifier,
        onAddFisherman = onAddFisherman,
        onAddTackleBox = onAddTackleBox
    )
}
