package com.funjim.fishstory.ui.utils.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitType
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.screens.FractionalLengthField
import com.funjim.fishstory.ui.utils.TargetSpeciesRow
import com.funjim.fishstory.ui.utils.cmToStorage
import com.funjim.fishstory.ui.utils.inchesToStorage
import com.funjim.fishstory.ui.utils.selection.SpeciesSelection
import com.funjim.fishstory.ui.utils.toCm
import com.funjim.fishstory.ui.utils.toInches
import java.util.Locale
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitDialog(
    limitToEdit: Limit? = null,
    useImperial: Boolean,
    species: List<Species>,
    speciesThumbnailProvider: @Composable (Species, Dp) -> Unit,
    onConfirm: (Limit) -> Unit,
    onDismiss: () -> Unit
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

    val isValid = selectedSpecies.isNotEmpty() && countText.isNotEmpty()

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
                    description = "No species selected",
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
                    if (useImperial) {
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
                    } else {
                        var lowerSizeText by remember(limitToEdit) {
                            mutableStateOf(
                                limitToEdit?.lowerSize?.let { storageValue ->
                                    val cm = storageValue.toCm()
                                    if (cm > 0) String.format(Locale.US, "%.2f", cm).trimEnd('0').trimEnd('.') else ""
                                } ?: ""
                            )
                        }

                        OutlinedTextField(
                            value = lowerSizeText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                    lowerSizeText = input

                                    // Convert safely to Double and then to storage Long
                                    val doubleCm = input.toDoubleOrNull()
                                    lowerSize = doubleCm?.cmToStorage() ?: 0L
                                }
                            },
                            label = {
                                if (selectedType == LimitType.MIN_SIZE)
                                    Text("Minimum Length Limit (cm)")
                                else
                                    Text("Lower Slot Length Limit (cm)")
                            },
                            suffix = { Text("cm") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                if (selectedType == LimitType.MAX_SIZE ||
                    selectedType == LimitType.SLOT_LIMIT ||
                    selectedType == LimitType.TROPHY_LIMIT) {
                    if (useImperial) {
                        val currentTotalInches = upperSize.toInches()
                        val wholeInches = floor(currentTotalInches).toInt().coerceAtLeast(0)
                        val remainingFraction = currentTotalInches - wholeInches

                        FractionalLengthField(
                            label =
                                when (selectedType) {
                                    LimitType.MAX_SIZE -> "Maximum Length Limit (in)"
                                    LimitType.SLOT_LIMIT -> "Upper Slot Length Limit (in)"
                                    else -> "Trophy Length Limit (in)"
                                },
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
                    } else {
                        var upperSizeText by remember(limitToEdit) {
                            mutableStateOf(
                                limitToEdit?.upperSize?.let { storageValue ->
                                    val cm = storageValue.toCm()
                                    if (cm > 0) String.format(Locale.US, "%.2f", cm).trimEnd('0').trimEnd('.') else ""
                                } ?: ""
                            )
                        }

                        OutlinedTextField(
                            value = upperSizeText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                    upperSizeText = input

                                    // Convert safely to Double and then to storage Long
                                    val doubleCm = input.toDoubleOrNull()
                                    upperSize = doubleCm?.cmToStorage() ?: 0L
                                }
                            },
                            label = {
                                when (selectedType) {
                                    LimitType.MAX_SIZE -> Text("Maximum Length Limit (cm)")
                                    LimitType.SLOT_LIMIT -> Text("Upper Slot Length Limit (cm)")
                                    else -> Text("Trophy Length Limit (cm)")
                                }
                            },
                            suffix = { Text("cm") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
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
            thumbnailProvider = { species ->
                speciesThumbnailProvider(species, 48.dp)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLimitDialog(
    species: List<Species>,
    speciesThumbnailProvider: @Composable (Species, Dp) -> Unit,
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
                    description = "No species selected",
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
            thumbnailProvider = { species ->
                speciesThumbnailProvider(species, 48.dp)
            }
        )
    }
}