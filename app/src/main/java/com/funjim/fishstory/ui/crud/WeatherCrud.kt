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
    val oTemp = remember { initialTemp }
    var eTemp by remember { mutableStateOf(oTemp) }
    val oSkyCondition = remember { allSkyConditions.find { it.id == initialSkyCondition } }
    var eSkyCondition by remember { mutableStateOf(oSkyCondition) }
    val oWindDirection = remember { initialWindDirection }
    var eWindDirection by remember { mutableStateOf(oWindDirection) }
    val oWindSpeed = remember { initialWindSpeed }
    var eWindSpeed by remember { mutableStateOf(oWindSpeed) }
    val oAtmosphericPressure = remember { initialAtmosphericPressure }
    var eAtmosphericPressure by remember { mutableStateOf(oAtmosphericPressure) }
    val oAirVisibility = remember { initialAirVisibility }
    var eAirVisibility by remember { mutableStateOf(oAirVisibility) }
    val oAirHumidity = remember { initialAirHumidity }
    var eAirHumidity by remember { mutableStateOf(oAirHumidity) }

    LaunchedEffect(pendingSkyCondition, allSkyConditions) {
        if (pendingSkyCondition != null) {
            allSkyConditions.find { it.id == pendingSkyCondition }?.let { match ->
                eSkyCondition = match
            }
        }
    }

    var dTemp by remember(useImperial) { mutableStateOf(
        eTemp?.let {
            val rawDouble =
                if (useImperial) it.toFahrenheitDouble() else it.toCelsiusDouble()

            val roundedDouble = BigDecimal(rawDouble)
                .setScale(1, RoundingMode.HALF_UP)
                .toDouble()

            if (roundedDouble % 1.0 == 0.0) {
                roundedDouble.toLong().toString()
            } else {
                roundedDouble.toString()
            }
        } ?: ""
    ) }
    var dWindSpeed by remember(useImperial) {mutableStateOf(
        eWindSpeed?.windSpeedDisplayString(useImperial) ?: ""
    ) }
    var dAtmosphericPressure by remember(useImperial) { mutableStateOf(
        if (useImperial)
            eAtmosphericPressure?.toInHg()?.toString() ?: ""
        else
            eAtmosphericPressure?.toHPa()?.toString() ?: ""
    )}
    var dAirVisibility by remember(useImperial) { mutableStateOf(
        eAirVisibility?.airVisibilityDisplayString(useImperial) ?: ""
    )}
    var dAirHumidity by remember { mutableStateOf(
        eAirHumidity?.toString() ?: ""
    )}

    var isWindDirectionExpanded by remember { mutableStateOf(false) }

    val isChanged = eTemp != oTemp ||
            eSkyCondition != oSkyCondition ||
            eWindDirection != oWindDirection ||
            eWindSpeed != oWindSpeed ||
            eAtmosphericPressure != oAtmosphericPressure ||
            eAirVisibility != oAirVisibility ||
            eAirHumidity != oAirHumidity

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
                    value = dTemp,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,1}$"""))) {
                            dTemp = input
                            eTemp =
                                if (useImperial) dTemp.fahrenheitToDbValue()
                                else dTemp.celsiusToDbValue()
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
                    selectedItem = eSkyCondition,
                    onSelected = { eSkyCondition = it },
                    onAdd = onAddSkyCondition,
                    onClear = { eSkyCondition = null },
                    modifier = Modifier.fillMaxWidth(),
                    thumbnailProvider = thumbnailProvider
                )

                // Wind Direction Dropdown with Selected Item Highlight
                ExposedDropdownMenuBox(
                    expanded = isWindDirectionExpanded,
                    onExpandedChange = { isWindDirectionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = eWindDirection?.name ?: "Select Direction",
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
                                eWindDirection = null
                                isWindDirectionExpanded = false
                            },
                            modifier = Modifier.background(
                                if (eWindDirection == null) MaterialTheme.colorScheme.primaryContainer
                                else Color.Unspecified
                            )
                        )

                        WindDirection.entries.forEach { direction ->
                            val isSelected = direction == eWindDirection

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
                                    eWindDirection = direction
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
                    value = dWindSpeed,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,1}$"""))) {
                            dWindSpeed = input
                            eWindSpeed = dWindSpeed.windSpeedInputToDbValueOrNull(useImperial)

                        }
                    },
                    label = { Text("Wind Speed") },
                    suffix = { Text(if (!useImperial) "km/h" else "mph") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )


                // Atmospheric Pressure
                OutlinedTextField(
                    value = dAtmosphericPressure,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                            dAtmosphericPressure = input
                            eAtmosphericPressure = dAtmosphericPressure.toDoubleOrNull()?.let {
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
                    value = dAirVisibility,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,1}$"""))) {
                            dAirVisibility = input
                            eAirVisibility = dAirVisibility.visibilityInputToDbValueOrNull(useImperial)
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
                    value = dAirHumidity,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            dAirHumidity = input
                            eAirHumidity = dAirHumidity.toLongOrNull()
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
                    onConfirm(
                        eTemp,
                        eSkyCondition?.id,
                        eWindDirection,
                        eWindSpeed,
                        eAtmosphericPressure,
                        eAirVisibility,
                        eAirHumidity
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
