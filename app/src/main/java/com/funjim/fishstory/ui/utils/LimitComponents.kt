package com.funjim.fishstory.ui.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitScope
import com.funjim.fishstory.model.LimitSummary
import com.funjim.fishstory.model.LimitType
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.screens.FractionalLengthField
import com.funjim.fishstory.ui.theme.AppIcons
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitDialog(
    species: List<Species>,
    speciesThumbnailProvider: @Composable (Species) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Limit) -> Unit,
    limitToEdit: Limit? = null
) {
    // Determine mode
    val isEditing = limitToEdit != null

    // Initialize state with limitToEdit values if present
    var selectedType by remember(limitToEdit) {
        mutableStateOf(limitToEdit?.type ?: LimitType.BAG_LIMIT)
    }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    var selectedSpecies by remember(limitToEdit) {
        mutableStateOf<List<Species>>(limitToEdit?.species ?: emptyList())
    }
    var showSpeciesSelection by remember { mutableStateOf(false) }

    var countText by remember(limitToEdit) {
        mutableStateOf(limitToEdit?.count?.toString() ?: "")
    }
    var lowerSize by remember(limitToEdit) {
        mutableLongStateOf(limitToEdit?.lowerSize ?: 0L)
    }
    var lowerInclusive by remember(limitToEdit) {
        mutableStateOf(limitToEdit?.lowerInclusive ?: true)
    }
    var upperSize by remember(limitToEdit) {
        mutableLongStateOf(limitToEdit?.upperSize ?: 0L)
    }
    var upperInclusive by remember(limitToEdit) {
        mutableStateOf(limitToEdit?.upperInclusive ?: true)
    }

    val isValid = selectedSpecies.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Limit" else "Add Limit") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Limit Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        LimitType.entries.forEachIndexed { index, type ->
                            if (index >= 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = type.label,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = type.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    expandedTypeDropdown = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                TargetSpeciesRow(
                    items = selectedSpecies,
                    name = "Species",
                    onAdd = { showSpeciesSelection = true },
                    onDelete = { species ->
                        selectedSpecies = selectedSpecies - species
                    },
                    thumbnailProvider = speciesThumbnailProvider,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it.filter { char -> char.isDigit() } },
                    label = { Text("Count Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (selectedType == LimitType.MIN_SIZE ||
                    selectedType == LimitType.SLOT_LIMIT) {
                    val currentTotalInches = lowerSize.toInches()
                    val wholeInches = floor(currentTotalInches).toInt().coerceAtLeast(0)
                    val remainingFraction = currentTotalInches - wholeInches

                    FractionalLengthField(
                        label =
                            if (selectedType == LimitType.MIN_SIZE)
                                "Minimum Length Limit (in)"
                            else
                                "Lower Slot Length Limit (in)",
                        wholeValue = if (currentTotalInches == 0.0) "" else wholeInches.toString(),
                        fractionValue = remainingFraction,
                        onLengthChanged = { newWhole, newFraction ->
                            val checkedWhole = newWhole.coerceAtLeast(0)
                            val computedDouble = checkedWhole.toDouble() + newFraction
                            lowerSize = computedDouble.inchesToStorage()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        wholeWeight = 0.6f
                    )
                }

                if (selectedType == LimitType.MAX_SIZE ||
                    selectedType == LimitType.SLOT_LIMIT ||
                    selectedType == LimitType.TROPHY_LIMIT) {
                    val currentTotalInches = upperSize.toInches()
                    val wholeInches = floor(currentTotalInches).toInt().coerceAtLeast(0)
                    val remainingFraction = currentTotalInches - wholeInches

                    FractionalLengthField(
                        label =
                            if (selectedType == LimitType.MAX_SIZE)
                                "Maximum Length Limit (in)"
                            else if (selectedType == LimitType.SLOT_LIMIT)
                                "Upper Slot Length Limit (in)"
                            else
                                "Trophy Length Limit (in)",
                        wholeValue = if (currentTotalInches == 0.0) "" else wholeInches.toString(),
                        fractionValue = remainingFraction,
                        onLengthChanged = { newWhole, newFraction ->
                            val checkedWhole = newWhole.coerceAtLeast(0)
                            val computedDouble = checkedWhole.toDouble() + newFraction
                            upperSize = computedDouble.inchesToStorage()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        wholeWeight = 0.6f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val countVal = countText.toIntOrNull() ?: 0

                    val limit = (limitToEdit?.copy(
                        type = selectedType,
                        species = selectedSpecies,
                        count = countVal,
                        lowerSize = if (selectedType == LimitType.MIN_SIZE || selectedType == LimitType.SLOT_LIMIT) lowerSize else null,
                        lowerInclusive = lowerInclusive,
                        upperSize = if (selectedType == LimitType.MAX_SIZE || selectedType == LimitType.SLOT_LIMIT || selectedType == LimitType.TROPHY_LIMIT) upperSize else null,
                        upperInclusive = upperInclusive
                    ) ?: Limit(
                        type = selectedType,
                        name = "",
                        species = selectedSpecies,
                        count = countVal,
                        lowerSize = if (selectedType == LimitType.MIN_SIZE || selectedType == LimitType.SLOT_LIMIT) lowerSize else null,
                        lowerInclusive = lowerInclusive,
                        upperSize = if (selectedType == LimitType.MAX_SIZE || selectedType == LimitType.SLOT_LIMIT || selectedType == LimitType.TROPHY_LIMIT) upperSize else null,
                        upperInclusive = upperInclusive
                    ))

                    onConfirm(limit)
                },
                enabled = isValid
            ) {
                Text(if (isEditing) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showSpeciesSelection) {
        SpeciesSelection(
            items = species,
            selectedItems = selectedSpecies,
            onSelected = { selected ->
                selectedSpecies = selectedSpecies + selected
            },
            onUnselected = { unselected ->
                selectedSpecies = selectedSpecies - unselected
            },
            onAdd = null,
            onDone = { showSpeciesSelection = false },
            modifier = Modifier.fillMaxWidth(),
            thumbnailProvider = speciesThumbnailProvider
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLimitDialog(
    species: List<Species>,
    speciesThumbnailProvider: @Composable (Species) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Limit) -> Unit
) {
    var selectedType by remember { mutableStateOf(LimitType.BAG_LIMIT) }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    var selectedSpecies by remember { mutableStateOf<List<Species>>(emptyList()) }
    var showSpeciesSelection by remember { mutableStateOf(false) }

    var countText by remember { mutableStateOf("") }
    var lowerSize by remember { mutableLongStateOf(0) }
    var lowerInclusive by remember { mutableStateOf(true) }
    var upperSize by remember { mutableLongStateOf(0) }
    var upperInclusive by remember { mutableStateOf(true) }

    val isValid = selectedSpecies.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Limit") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Limit Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        LimitType.entries.forEachIndexed { index, type ->
                            if (index >= 1) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = type.label,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = type.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    expandedTypeDropdown = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                TargetSpeciesRow(
                    items = selectedSpecies,
                    name = "Species",
                    onAdd = { showSpeciesSelection = true },
                    onDelete = { species ->
                        selectedSpecies = selectedSpecies - species
                    },
                    thumbnailProvider = speciesThumbnailProvider,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = countText,
                    onValueChange = { countText = it.filter { char -> char.isDigit() } },
                    label = { Text("Count Limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (selectedType == LimitType.MIN_SIZE ||
                    selectedType == LimitType.SLOT_LIMIT) {
                    val currentTotalInches = lowerSize.toInches()
                    val wholeInches = floor(currentTotalInches).toInt().coerceAtLeast(0)
                    val remainingFraction = currentTotalInches - wholeInches

                    FractionalLengthField(
                        label =
                            if (selectedType == LimitType.MIN_SIZE)
                                "Minimum Length Limit (in)"
                            else
                                "Lower Slot Length Limit (in)"
                        ,
                        wholeValue = if (currentTotalInches == 0.0) "" else wholeInches.toString(),
                        fractionValue = remainingFraction,
                        onLengthChanged = { newWhole, newFraction ->
                            val checkedWhole = newWhole.coerceAtLeast(0)
                            val computedDouble = checkedWhole.toDouble() + newFraction
                            lowerSize = (computedDouble.inchesToStorage())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        wholeWeight = 0.6f
                    )
                }

                if (selectedType == LimitType.MAX_SIZE ||
                    selectedType == LimitType.SLOT_LIMIT ||
                    selectedType == LimitType.TROPHY_LIMIT) {
                    val currentTotalInches = upperSize.toInches()
                    val wholeInches = floor(currentTotalInches).toInt().coerceAtLeast(0)
                    val remainingFraction = currentTotalInches - wholeInches

                    FractionalLengthField(
                        label =
                            if (selectedType == LimitType.MAX_SIZE)
                                "Maximum Length Limit (in)"
                            else if (selectedType == LimitType.SLOT_LIMIT)
                                "Upper Slot Length Limit (in)"
                            else
                                "Trophy Length Limit (in)"
                        ,
                        wholeValue = if (currentTotalInches == 0.0) "" else wholeInches.toString(),
                        fractionValue = remainingFraction,
                        onLengthChanged = { newWhole, newFraction ->
                            val checkedWhole = newWhole.coerceAtLeast(0)
                            val computedDouble = checkedWhole.toDouble() + newFraction
                            upperSize = (computedDouble.inchesToStorage())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        wholeWeight = 0.6f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val countVal = countText.toIntOrNull() ?: 0

                    val limit = Limit(
                        type = selectedType,
                        name = "",
                        species = selectedSpecies,
                        count = countVal,
                        lowerSize =
                            if (selectedType == LimitType.MIN_SIZE ||
                                selectedType == LimitType.SLOT_LIMIT)
                                lowerSize
                            else
                                null,
                        lowerInclusive = lowerInclusive,
                        upperSize =
                            if (selectedType == LimitType.MAX_SIZE ||
                                selectedType == LimitType.SLOT_LIMIT ||
                                selectedType == LimitType.TROPHY_LIMIT)
                                upperSize
                            else
                                null,
                        upperInclusive = upperInclusive
                    )
                    onConfirm(limit)
                },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showSpeciesSelection) {
        SpeciesSelection(
            items = species,
            selectedItems = selectedSpecies,
            onSelected = { selected ->
                selectedSpecies = selectedSpecies + selected
            },
            onUnselected = { unselected ->
                selectedSpecies = selectedSpecies - unselected
            },
            onAdd = null,
            onDone = { showSpeciesSelection = false },
            modifier = Modifier.fillMaxWidth(),
            thumbnailProvider = speciesThumbnailProvider
        )
    }
}

@Composable
fun LimitSummaryRow(
    itemList: List<LimitSummary>,
    scope: LimitScope,
    modifier: Modifier,
    eventThumbnailProvider: @Composable (Event) -> Unit = {},
    speciesThumbnailProvider: @Composable (Species) -> Unit,
    onAdd: (() -> Unit)? = null,
    onEdit: (Limit) -> Unit,
    onDelete: (Limit) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Limits",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )

                if (itemList.size > 1) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(${itemList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = getOnMainColor()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (onAdd != null) {
                IconButton(
                    onClick = onAdd,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Limit"
                    )
                }
            }
        }

        if (itemList.isNotEmpty()) {
            itemList.forEachIndexed { index, item ->
                LimitSummaryCard(
                    item = item,
                    scope = scope,
                    eventThumbnailProvider = eventThumbnailProvider,
                    speciesThumbnailProvider = speciesThumbnailProvider,
                    index = index,
                    totalItems = itemList.size,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "No limits are set.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun LimitSummaryCard(
    item: LimitSummary,
    scope: LimitScope,
    modifier: Modifier = Modifier,
    eventThumbnailProvider: (@Composable (Event) -> Unit),
    speciesThumbnailProvider: @Composable (Species) -> Unit,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: (Limit) -> Unit,
    onDelete: (Limit) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    val allowMenu = (scope == LimitScope.EVENT && !item.isTripLimit) ||
            (scope == LimitScope.TRIP && item.isTripLimit)

    val isMultiEvent = item.summaryList.size > 1

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = {
                    if (isMultiEvent) {
                        isExpanded = !isExpanded
                    }
                },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusGumball(
                    status = if (item.isLimitExceeded) GumballStatus.BAD else GumballStatus.GOOD,
                    size = 32.dp,
                    borderWidth = 2.dp,
                    borderColor = secondaryContentColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.limit.type.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (isMultiEvent) {
                        CardItemWithValue(
                            icon = AppIcons.Default.CanoeEmpty,
                            value = "${item.summaryList.size} Events",
                            contentColor = secondaryContentColor
                        )
                    } else {
                        item.summaryList.firstOrNull()?.event?.let { event ->
                            Column {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    eventThumbnailProvider(event)
                                    Text(
                                        text = event.name,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                            CardItemWithValue(
                                icon = AppIcons.Default.Fisherman,
                                value = "${item.summaryList.first().fishermanCount} Fishermen",
                                contentColor = secondaryContentColor
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.summaryList.size == 1) {
                            item.summaryList.first().let { eventSummary ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Limit: ${item.limit.count * eventSummary.fishermanCount}") },
                                    modifier = Modifier.height(24.dp)
                                )
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Kept: ${eventSummary.caughtCount}") },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        } else {
                            AssistChip(
                                onClick = { },
                                label = { Text("Base Limit: ${item.limit.count}") },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    // Format & Display Size Range Constraints
                    val sizeDetails = remember(item.limit) { formatSizeConstraint(item.limit) }
                    if (sizeDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sizeDetails,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Display Associated Species List
                    if (item.limit.species.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item.limit.species.forEach { species ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        speciesThumbnailProvider(species)
                                        Text(
                                            text = species.name,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (allowMenu) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Limit Options"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit(item.limit)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete(item.limit)
                                }
                            )
                        }
                    }
                }
            }

            // Expanded Section: Event Summaries Breakdown
            if (isMultiEvent && isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = secondaryContentColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.summaryList.forEach { eventSummary ->
                        val isEventExceeded = eventSummary.caughtCount > (item.limit.count * eventSummary.fishermanCount)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, borderColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusGumball(
                                    status = if (isEventExceeded) GumballStatus.BAD else GumballStatus.GOOD,
                                    size = 20.dp,
                                    borderWidth = 1.dp,
                                    borderColor = secondaryContentColor
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    eventSummary.event?.let { event ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            eventThumbnailProvider(event)
                                            Text(
                                                text = event.name,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    CardItemWithValue(
                                        icon = AppIcons.Default.Fisherman,
                                        value = "${eventSummary.fishermanCount} Fishermen",
                                        contentColor = secondaryContentColor
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Limit: ${item.limit.count * eventSummary.fishermanCount}") },
                                            modifier = Modifier.height(22.dp)
                                        )
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Kept: ${eventSummary.caughtCount}") },
                                            modifier = Modifier.height(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LimitSummaryCard2(
    item: LimitSummary,
    scope: LimitScope,
    modifier: Modifier = Modifier,
    eventThumbnailProvider: (@Composable (Event) -> Unit),
    speciesThumbnailProvider: @Composable (Species) -> Unit,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: (Limit) -> Unit,
    onDelete: (Limit) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    val allowMenu = (scope == LimitScope.EVENT && !item.isTripLimit) ||
            (scope == LimitScope.TRIP && item.isTripLimit)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = {},
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusGumball(
                status = if (item.isLimitExceeded) GumballStatus.BAD else GumballStatus.GOOD,
                size = 32.dp,
                borderWidth = 2.dp,
                borderColor = secondaryContentColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.limit.type.label,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (item.summaryList.size > 1) {
                    CardItemWithValue(
                        icon = AppIcons.Default.CanoeEmpty,
                        value = "${item.summaryList.size} Events",
                        contentColor = secondaryContentColor
                    )
                } else {
                    item.summaryList.first().event?.let {
                        Column() {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                eventThumbnailProvider(it)
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                        CardItemWithValue(
                            icon = AppIcons.Default.Fisherman,
                            value = "${item.summaryList.first().fishermanCount} Fishermen",
                            contentColor = secondaryContentColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // When there is only 1 item in the summary list and the event is null,
                    // the card is being displayed for the event details -- so, show both
                    // the limit count and caught count
                    if (item.summaryList.size == 1) {
                        item.summaryList.first().let {
                            AssistChip(
                                onClick = { },
                                label = { Text("Limit: ${item.limit.count * it.fishermanCount}") },
                                modifier = Modifier.height(24.dp)
                            )
                            AssistChip(
                                onClick = { },
                                label = { Text("Kept: ${it.caughtCount}") },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    } else {
                        AssistChip(
                            onClick = { },
                            label = { Text("Limit: ${item.limit.count}") },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }

                // Format & Display Size Range Constraints
                val sizeDetails = remember(item.limit) { formatSizeConstraint(item.limit) }
                if (sizeDetails.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sizeDetails,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Display Associated Species List
                if (item.limit.species.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.limit.species.forEach { species ->
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    speciesThumbnailProvider(species)
                                    Text(
                                        text = species.name,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (allowMenu) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Limit Options"
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit(item.limit)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete(item.limit)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatSizeConstraint(limit: Limit): String {
    val lowerInches = limit.lowerSize?.toDisplayString(useMetric = false, useFractions = true)
    val upperInches = limit.upperSize?.toDisplayString(useMetric = false, useFractions = true)

    return when (limit.type) {
        LimitType.BAG_LIMIT -> ""
        LimitType.MIN_SIZE -> lowerInches?.let { "Min Length: $lowerInches" } ?: ""
        LimitType.MAX_SIZE -> upperInches?.let { "Max Length: $upperInches" } ?: ""
        LimitType.SLOT_LIMIT -> {
            if (lowerInches != null && upperInches != null) {
                "Slot Range: $lowerInches - $upperInches"
            } else ""
        }
        LimitType.TROPHY_LIMIT -> upperInches?.let { "Trophy Threshold: > $upperInches" } ?: ""
    }
}