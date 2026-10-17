package com.funjim.fishstory.ui.crud

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.WindDirection
import com.funjim.fishstory.ui.utils.airVisibilityDisplayString
import com.funjim.fishstory.ui.utils.celsiusToDbValue
import com.funjim.fishstory.ui.utils.fahrenheitToDbValue
import com.funjim.fishstory.ui.utils.hPaToStorage
import com.funjim.fishstory.ui.utils.inHgToStorage
import com.funjim.fishstory.ui.selection.SkyConditionSelectionField
import com.funjim.fishstory.ui.utils.toCelsiusDouble
import com.funjim.fishstory.ui.utils.toFahrenheitDouble
import com.funjim.fishstory.ui.utils.toHPa
import com.funjim.fishstory.ui.utils.toInHg
import com.funjim.fishstory.ui.utils.visibilityInputToDbValueOrNull
import com.funjim.fishstory.ui.utils.windSpeedDisplayString
import com.funjim.fishstory.ui.utils.windSpeedInputToDbValueOrNull
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun AddSkyConditionDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    EditSkyConditionDialog(
        item = SkyCondition(name = ""),
        title = "Add",
        onConfirm = { onConfirm(it.name) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditSkyConditionDialog(
    item: SkyCondition,
    title: String = "Rename",
    onConfirm: (SkyCondition) -> Unit,
    onDismiss: () -> Unit
) {
    val origName = remember(item) { item.name }
    var name by remember { mutableStateOf(origName) }

    val isValid = name.isNotBlank() && (origName != name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Sky Condition") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Sky Condition (e.g. Clear)") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDialog(
    initialTemp: Long?,
    initialSkyCondition: String?,
    initialWindDirection: WindDirection?,
    initialWindSpeed: Long?,
    initialAtmosphericPressure: Long?,
    initialAirVisibility: Long?,
    initialAirHumidity: Long?,
    allSkyConditions: List<SkyCondition>,
    pendingSkyCondition: String?,
    useImperial: Boolean,
    title: String,
    thumbnailProvider: @Composable (SkyCondition) -> Unit,
    onToggleImperialUnits: () -> Unit,
    onAddSkyCondition: () -> Unit,
    onConfirm: (
        temp: Long?,
        skyConditionId: String?,
        windDirection: WindDirection?,
        windSpeed: Long?,
        atmosphericPressure: Long?,
        airVisibility: Long?,
        airHumidity: Long?
    ) -> Unit,
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

    val originalSkyCondition = remember(initialSkyCondition, allSkyConditions) {
        allSkyConditions.find { it.id == initialSkyCondition }
    }

    val originalWindDirection = remember(initialWindDirection) { initialWindDirection }

    // Wind Speed: Converts DB Long to MPH string for initial display
    val originalPressure = remember(initialAtmosphericPressure) { initialAtmosphericPressure }

    // Visibility: Converts DB Millimeters to Miles string for initial display
    val originalHumidity = remember(initialAirHumidity) { initialAirHumidity?.toString() ?: "" }

    // Parse Initial Values using unit preference
    val originalWindSpeed = remember(initialWindSpeed, useImperial) {
        initialWindSpeed.windSpeedDisplayString(useImperial)
    }

    val originalVisibility = remember(initialAirVisibility, useImperial) {
        initialAirVisibility.airVisibilityDisplayString(useImperial)
    }

    var temp by remember(useImperial) { mutableStateOf(originalTemp) }
    var skyCondition by remember { mutableStateOf(originalSkyCondition) }
    var windDirection by remember { mutableStateOf(originalWindDirection) }
    var windSpeed by remember(useImperial) { mutableStateOf(originalWindSpeed) }
    var pressure by remember { mutableStateOf(originalPressure) }
    var visibility by remember(useImperial) { mutableStateOf(originalVisibility) }
    var humidity by remember { mutableStateOf(originalHumidity) }

    // Automatically select the pending clarity once it arrives in allClarity
    LaunchedEffect(pendingSkyCondition, allSkyConditions) {
        if (pendingSkyCondition != null) {
            allSkyConditions.find { it.id == pendingSkyCondition }?.let { match ->
                skyCondition = match
            }
        }
    }

    var isWindDirectionExpanded by remember { mutableStateOf(false) }

    val isChanged = temp != originalTemp ||
            skyCondition != originalSkyCondition ||
            windDirection != originalWindDirection ||
            windSpeed != originalWindSpeed ||
            pressure != originalPressure ||
            visibility != originalVisibility ||
            humidity != originalHumidity

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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
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

                // Sky Condition
                SkyConditionSelectionField(
                    items = allSkyConditions,
                    selectedItem = skyCondition,
                    onSelected = { skyCondition = it },
                    onAdd = onAddSkyCondition,
                    onClear = { skyCondition = null },
                    modifier = Modifier.fillMaxWidth(),
                    thumbnailProvider = thumbnailProvider
                )

                // Wind Direction Dropdown with Selected Item Highlight
                ExposedDropdownMenuBox(
                    expanded = isWindDirectionExpanded,
                    onExpandedChange = { isWindDirectionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = windDirection?.name ?: "Select Direction",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wind Direction") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWindDirectionExpanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = isWindDirectionExpanded,
                        onDismissRequest = { isWindDirectionExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                windDirection = null
                                isWindDirectionExpanded = false
                            },
                            modifier = Modifier.background(
                                if (windDirection == null) MaterialTheme.colorScheme.primaryContainer
                                else Color.Unspecified
                            )
                        )

                        WindDirection.entries.forEach { direction ->
                            val isSelected = direction == windDirection

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = direction.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    windDirection = direction
                                    isWindDirectionExpanded = false
                                },
                                modifier = Modifier.background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Unspecified
                                )
                            )
                        }
                    }
                }

                // Wind Speed (Allows decimals e.g., "12" or "12.5")
                OutlinedTextField(
                    value = windSpeed,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,1}$"""))) {
                            windSpeed = input
                        }
                    },
                    label = { Text("Wind Speed") },
                    suffix = { Text(if (!useImperial) "km/h" else "mph") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                var pressureText by remember(initialAtmosphericPressure, useImperial) {
                    mutableStateOf(
                        if (useImperial) initialAtmosphericPressure?.toInHg()?.toString() ?: ""
                        else initialAtmosphericPressure?.toHPa()?.toString() ?: ""
                    )
                }

                // Atmospheric Pressure
                OutlinedTextField(
                    value = pressureText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            pressureText = input
                            val doubleVal = input.toDoubleOrNull()
                            pressure = doubleVal?.let {
                                if (useImperial) {
                                    it.inHgToStorage()
                                } else {
                                    it.hPaToStorage()
                                }
                            }
                        }
                    },
                    label = { Text("Atmospheric Pressure") },
                    suffix = { Text(if (useImperial) "inHg" else "hPa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Air Visibility Text Field
                OutlinedTextField(
                    value = visibility,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,1}$"""))) {
                            visibility = input
                        }
                    },
                    label = { Text("Air Visibility") },
                    suffix = { Text(if (!useImperial) "km" else "mi") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Air Humidity
                OutlinedTextField(
                    value = humidity,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            humidity = input
                        }
                    },
                    label = { Text("Air Humidity") },
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tempValue =
                        if (useImperial) temp.fahrenheitToDbValue()
                        else temp.celsiusToDbValue()

                    val speedValue = windSpeed.windSpeedInputToDbValueOrNull(useImperial)
                    val pressureValue = pressure
                    val visibilityValue = visibility.visibilityInputToDbValueOrNull(useImperial)
                    val humidityValue = humidity.toLongOrNull()

                    onConfirm(
                        tempValue,
                        skyCondition?.id,
                        windDirection,
                        speedValue,
                        pressureValue,
                        visibilityValue,
                        humidityValue
                    )
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
