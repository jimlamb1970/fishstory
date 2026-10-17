package com.funjim.fishstory.ui.crud

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.ui.utils.celsiusToDbValue
import com.funjim.fishstory.ui.utils.fahrenheitToDbValue
import com.funjim.fishstory.ui.utils.inchesToStorage
import com.funjim.fishstory.ui.selection.WaterClaritySelectionField
import com.funjim.fishstory.ui.utils.toCelsiusDouble
import com.funjim.fishstory.ui.utils.toFahrenheitDouble
import com.funjim.fishstory.ui.utils.toInches
import com.funjim.fishstory.ui.utils.toMm
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@Composable
fun AddWaterClarityDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    EditWaterClarityDialog(
        item = WaterClarity(name = ""),
        title = "Add",
        onConfirm = { onConfirm(it.name) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditWaterClarityDialog(
    item: WaterClarity,
    title: String = "Rename",
    onConfirm: (WaterClarity) -> Unit,
    onDismiss: () -> Unit
) {
    val origName = remember(item) { item.name }
    var name by remember { mutableStateOf(origName) }

    val isValid = name.isNotBlank() && (origName != name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Water Clarity") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Water Clarity (e.g. Clear)") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(item.copy(name = name.trim())) },
                enabled = isValid
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WaterDialog(
    initialTemp: Long?,
    initialDepth: Long?,
    initialClarity: String?,
    pendingClarity: String?,
    allClarity: List<WaterClarity>,
    useImperial: Boolean,
    title: String,
    thumbnailProvider: @Composable (WaterClarity) -> Unit,
    onToggleImperialUnits: () -> Unit,
    onAddWaterClarity: () -> Unit,
    onConfirm: (Long?, Long?, WaterClarity?) -> Unit,
    onDismiss: () -> Unit
) {
    val originalTemp = remember(initialTemp, useImperial) {
        initialTemp?.let {
            val rawDouble = if (useImperial) it.toFahrenheitDouble() else it.toCelsiusDouble()

            val roundedDouble = BigDecimal(rawDouble)
                .setScale(1, RoundingMode.HALF_UP)
                .toDouble()

            // Strip trailing .0 for whole numbers (e.g., 72.0 -> "72")
            if (roundedDouble % 1.0 == 0.0) {
                roundedDouble.toLong().toString()
            } else {
                roundedDouble.toString()
            }
        } ?: ""
    }

    val originalFeet = remember(initialDepth) {
        initialDepth?.let { (it.toInches().toLong() / 12).toString() } ?: ""
    }

    val originalInches = remember(initialDepth) {
        initialDepth?.let { (it.toInches().toLong() % 12).toString() } ?: ""
    }

    val originalMeters = remember(initialDepth) {
        initialDepth?.let {
            val totalMM = it.toMm()
            val meters = totalMM / 1000
            String.format(Locale.US, "%.2f", meters).trimEnd('0').trimEnd('.')
        } ?: ""
    }

    val originalClarity = remember(initialClarity, allClarity) {
        allClarity.find { it.id == initialClarity }
    }

    var temp by remember(useImperial) { mutableStateOf(originalTemp) }
    var depthFeet by remember { mutableStateOf(originalFeet) }
    var depthInches by remember { mutableStateOf(originalInches) }
    var depthMeters by remember { mutableStateOf(originalMeters) }
    var clarity by remember { mutableStateOf(originalClarity) }

    // Automatically select the pending clarity once it arrives in allClarity
    LaunchedEffect(pendingClarity, allClarity) {
        if (pendingClarity != null) {
            allClarity.find { it.id == pendingClarity }?.let { match ->
                clarity = match
            }
        }
    }

    val isChanged = temp != originalTemp ||
            (useImperial && (depthFeet != originalFeet || depthInches != originalInches)) ||
            (!useImperial && depthMeters != originalMeters) ||
            clarity != originalClarity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onToggleImperialUnits
                    )
            ) {
                Text(title)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Temperature
                OutlinedTextField(
                    value = temp,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,1}$"""))) {
                            temp = input
                        }
                    },
                    label = { Text("Temperature") },
                    suffix = { Text(if (useImperial) "°F" else "°C") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Depth Field(s) based on system of measurement
                if (useImperial) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = depthFeet,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    depthFeet = input
                                }
                            },
                            label = { Text("Depth (ft)") },
                            suffix = { Text("ft") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = depthInches,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    depthInches = input
                                }
                            },
                            label = { Text("Depth (in)") },
                            suffix = { Text("in") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = depthMeters,
                        onValueChange = { input ->
                            // Allows up to 2 decimal places
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                                depthMeters = input
                            }
                        },
                        label = { Text("Depth (m)") },
                        suffix = { Text("m") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                WaterClaritySelectionField(
                    items = allClarity,
                    selectedItem = clarity,
                    onSelected = { clarity = it },
                    onAdd = onAddWaterClarity,
                    onClear = { clarity = null },
                    modifier = Modifier.fillMaxWidth(),
                    thumbnailProvider = thumbnailProvider
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tempValue =
                        if (useImperial) temp.fahrenheitToDbValue()
                        else temp.celsiusToDbValue()

                    val totalDepth = if (useImperial) {
                        if (depthFeet.isNotBlank() || depthInches.isNotBlank()) {
                            val feetValue = depthFeet.toLongOrNull() ?: 0L
                            val inchesValue = depthInches.toLongOrNull() ?: 0L
                            (feetValue * 12 + inchesValue).toDouble().inchesToStorage()
                        } else {
                            null
                        }
                    } else {
                        depthMeters.toDoubleOrNull()?.let { meters ->
                            val inches = meters / 0.0254
                            inches.inchesToStorage()
                        }
                    }

                    onConfirm(tempValue, totalDepth, clarity)
                },
                enabled = isChanged
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
}