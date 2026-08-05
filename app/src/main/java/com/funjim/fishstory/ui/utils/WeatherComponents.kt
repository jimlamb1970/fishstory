package com.funjim.fishstory.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.model.WeatherWithDetails
import com.funjim.fishstory.model.WindDirection
import com.funjim.fishstory.ui.theme.AppIcons
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 1 km = 1,000,000 mm (EXACT) */
fun Long.kmToDbValue(): Long = this * 1_000_000L

/** 1 mile = 1,609,344 mm (EXACT) */
fun Long.milesToDbValue(): Long = this * 1_609_344L

/** Converts DB Millimeters to Kilometers */
fun Long.toKm(): Long = this / 1_000_000L

/** Converts DB Millimeters to Miles */
fun Long.toMiles(): Long = this / 1_609_344L

private val MM_PER_MILE = BigDecimal("1609344")
private val MM_PER_KM = BigDecimal("1000000")

private val BASE_PER_MPH = BigDecimal("0.44704")
private val BASE_PER_KMH = BigDecimal("0.277778")

/**
 * Converts DB airVisibility (stored in Millimeters as Long) to UI string.
 * @param isMetric If true, converts to Kilometers (KM). If false, converts to Miles (mi).
 */
fun Long?.airVisibilityDisplayString(isMetric: Boolean = false): String {
    if (this == null) return ""
    val divisor = if (isMetric) MM_PER_KM else MM_PER_MILE

    return BigDecimal(this)
        .divide(divisor, 1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * Converts UI input string to DB Millimeters (Long).
 * @param isMetric If true, treats input as KM. If false, treats input as Miles.
 */
fun String.visibilityInputToDbValueOrNull(isMetric: Boolean = false): Long? {
    val decimalInput = this.toBigDecimalOrNull() ?: return null
    val multiplier = if (isMetric) MM_PER_KM else MM_PER_MILE

    return decimalInput.multiply(multiplier)
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

/**
 * Converts DB windSpeed (stored in base Long unit) to UI string.
 * @param isMetric If true, converts to KM/H. If false, converts to MPH.
 */
fun Long?.windSpeedDisplayString(isMetric: Boolean = false): String {
    if (this == null) return ""
    val divisor = if (isMetric) BASE_PER_KMH else BASE_PER_MPH

    return BigDecimal(this)
        .divide(divisor, 1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * Converts UI input string to DB Wind Speed (Long).
 * @param isMetric If true, treats input as KM/H. If false, treats input as MPH.
 */
fun String.windSpeedInputToDbValueOrNull(isMetric: Boolean = false): Long? {
    val decimalInput = this.toBigDecimalOrNull() ?: return null
    val multiplier = if (isMetric) BASE_PER_KMH else BASE_PER_MPH

    return decimalInput.multiply(multiplier)
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

private fun Weather.airHumidityDisplayString(): String? {
    val airHumidityDb = airHumidity ?: return null
    return String.format(Locale.getDefault(), "${airHumidityDb}%%")
}
private fun Weather.airVisibilityDisplayString(useKM: Boolean = false): String? {
    val visibilityDb = airVisibility ?: return null
    return if (useKM) {
        String.format(Locale.getDefault(), "%s km", visibilityDb.airVisibilityDisplayString())
    } else {
        String.format(Locale.getDefault(), "%s m", visibilityDb.airVisibilityDisplayString())
    }
}
private fun Weather.tempDisplayString(useCelsius: Boolean = false): String? {
    val tempDb = temperature ?: return null
    return if (useCelsius) {
        String.format(Locale.getDefault(), "%.1f°C", tempDb.toCelsiusDouble())
    } else {
        String.format(Locale.getDefault(), "%.1f°F", tempDb.toFahrenheitDouble())
    }
}
private fun Weather.windSpeedDisplayString(useKM: Boolean = false): String? {
    val speedDb = windSpeed ?: return null
    return if (useKM) {
        String.format(Locale.getDefault(), "%s km/h", speedDb.windSpeedDisplayString())
    } else {
        String.format(Locale.getDefault(), "%s mph", speedDb.windSpeedDisplayString())
    }
}

@Composable
fun WeatherCard(
    weather: WeatherWithDetails,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: (Weather) -> Unit,
    onDelete: (Weather) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    val dateTime = dateTimeFormatter.format(Date(weather.weather.timestamp))

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
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
            Icon(
                imageVector = AppIcons.Default.Weather,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (weather.weather.temperature != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weather.weather.tempDisplayString()?.let { temp ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Temperature: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = temp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (weather.skyCondition != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sky Condition: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = weather.skyCondition.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (weather.weather.windDirection != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        weather.weather.windDirection?.let { direction ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Wind Direction: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = direction.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (weather.weather.windSpeed != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        weather.weather.windSpeedDisplayString()?.let { speed ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Wind Speed: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = speed,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (weather.weather.airVisibility != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        weather.weather.airVisibilityDisplayString()?.let { visibility ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Air Visibility: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = visibility,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (weather.weather.airHumidity != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        weather.weather.airHumidityDisplayString()?.let { humidity ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Air Humidity: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = humidity,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Weather Snapshot Options"
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
                            onEdit(weather.weather)
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
                            onDelete(weather.weather)
                        }
                    )
                }
            }
        }
    }
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
    title: String,
    thumbnailProvider: @Composable (SkyCondition) -> Unit,
    isMetric: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (
        temp: Long?,
        skyConditionId: String?,
        windDirection: WindDirection?,
        windSpeed: Long?,
        atmosphericPressure: Long?,
        airVisibility: Long?,
        airHumidity: Long?
    ) -> Unit,
    onAddSkyCondition: () -> Unit
) {
    // 1. Initial Values
    val originalTemp = remember(initialTemp) {
        initialTemp?.let {
            val fahrenheit = it.toFahrenheitDouble()
            if (fahrenheit == fahrenheit.toLong().toDouble()) {
                fahrenheit.toLong().toString()
            } else {
                fahrenheit.toString()
            }
        } ?: ""
    }

    val originalSkyCondition = remember(initialSkyCondition, allSkyConditions) {
        allSkyConditions.find { it.id == initialSkyCondition }
    }

    val originalWindDirection = remember(initialWindDirection) { initialWindDirection }

    // Wind Speed: Converts DB Long to MPH string for initial display
    val originalPressure = remember(initialAtmosphericPressure) { initialAtmosphericPressure?.toString() ?: "" }

    // Visibility: Converts DB Millimeters to Miles string for initial display
    val originalHumidity = remember(initialAirHumidity) { initialAirHumidity?.toString() ?: "" }

    // Parse Initial Values using unit preference
    val originalWindSpeed = remember(initialWindSpeed, isMetric) {
        initialWindSpeed.windSpeedDisplayString(isMetric = isMetric)
    }

    val originalVisibility = remember(initialAirVisibility, isMetric) {
        initialAirVisibility.airVisibilityDisplayString(isMetric = isMetric)
    }

    // 2. Mutable State
    var temp by remember { mutableStateOf(originalTemp) }
    var skyCondition by remember { mutableStateOf(originalSkyCondition) }
    var windDirection by remember { mutableStateOf(originalWindDirection) }
    var windSpeed by remember { mutableStateOf(originalWindSpeed) }
    var pressure by remember { mutableStateOf(originalPressure) }
    var visibility by remember { mutableStateOf(originalVisibility) }
    var humidity by remember { mutableStateOf(originalHumidity) }

    var isWindDirectionExpanded by remember { mutableStateOf(false) }

    // 3. Change Detection
    val isChanged = temp != originalTemp ||
            skyCondition != originalSkyCondition ||
            windDirection != originalWindDirection ||
            windSpeed != originalWindSpeed ||
            pressure != originalPressure ||
            visibility != originalVisibility ||
            humidity != originalHumidity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
                    suffix = { Text("°F") },
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
                            .menuAnchor()
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
                    suffix = { Text(if (isMetric) "km/h" else "mph") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Atmospheric Pressure
                OutlinedTextField(
                    value = pressure,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            pressure = input
                        }
                    },
                    label = { Text("Atmospheric Pressure") },
                    suffix = { Text("inHg") },
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
                    suffix = { Text(if (isMetric) "km" else "mi") },
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
                    val tempValue = temp.fahrenheitToDbValue()

                    // Convert UI inputs back to DB Long values using unit preference
                    val speedValue = windSpeed.windSpeedInputToDbValueOrNull(isMetric = isMetric)
                    val pressureValue = pressure.toLongOrNull()
                    val visibilityValue = visibility.visibilityInputToDbValueOrNull(isMetric = isMetric)
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

@Composable
fun WeatherRow(
    weatherList: List<WeatherWithDetails>,
    onAddWeather: () -> Unit,
    onEdit: (Weather) -> Unit,
    onDelete: (Weather) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (weatherList.size > 1) {
                IconButton(
                    onClick = {
                        isExpanded = !isExpanded
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector =
                            if (isExpanded) Icons.Default.ExpandLess
                            else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            Text(
                text = "Weather Conditions",
                style = MaterialTheme.typography.titleMedium,
                color = getOnMainColor()
            )

            if (weatherList.size > 1) {
                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "(${weatherList.size})",
                    style = MaterialTheme.typography.titleSmall,
                    color = getOnMainColor()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onAddWeather,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Sky Condition"
            )
        }
    }

    if (weatherList.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            WeatherCard(
                weather = weatherList.first(),
                index = 0,
                totalItems = weatherList.size,
                onEdit = onEdit,
                onDelete = onDelete
            )

            AnimatedVisibility(visible = isExpanded && weatherList.size > 1) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    weatherList.drop(1).forEachIndexed { index, weather ->
                        WeatherCard(
                            weather = weather,
                            index = index + 1,
                            totalItems = weatherList.size,
                            onEdit = onEdit,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(
                text = "No weather conditions are set.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyConditionSelectionField(
    items: List<SkyCondition>,
    selectedItem: SkyCondition?,
    onSelected: (SkyCondition) -> Unit,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    thumbnailProvider: @Composable (SkyCondition) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = selectedItem?.name ?: "Select Sky Condition (optional)",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Sky Condition") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Select Sky Condition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier
                .fillMaxHeight(0.8f)
                .padding(start = 16.dp, end = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Sky Condition ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                val filteredSize = filtered.size

                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        gridItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                headlineContent = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        thumbnailProvider(item)

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (onClear != null && selectedItem != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider() }

                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ModalResetButton(
                                    title = "Reset Sky Condition",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }

                        if (onAdd != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HorizontalDivider()
                            }

                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ModalAddButton(
                                    title = "Add new sky condition ...",
                                    onAdd = { showSheet = false; onAdd() }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listItemsIndexed(filtered) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                leadingContent = { thumbnailProvider(item) },
                                headlineContent = {
                                    Text(
                                        item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight =
                                            if (isSelected) FontWeight.Bold
                                            else FontWeight.Normal,
                                        color = getOnCardColor()
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (onClear != null && selectedItem != null) {
                            item { HorizontalDivider() }
                            item {
                                ModalResetButton(
                                    title = "Reset Sky Condition",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }

                        if (onAdd != null) {
                            item { HorizontalDivider() }
                            item {
                                ModalAddButton(
                                    title = "Add new sky condition ...",
                                    onAdd = { showSheet = false; onAdd() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyConditionSelection(
    items: List<SkyCondition>,
    selectedItems: List<SkyCondition>,
    onSelected: (SkyCondition) -> Unit,
    onUnselected: (SkyCondition) -> Unit,
    onAdd: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    usageMap: Map<String, Int>? = null,
    maxUsage: Int? = null,
    thumbnailProvider: @Composable (SkyCondition) -> Unit
) {
    var showSheet by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                onDone()
            },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Select Sky Condition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                        onDone()
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxHeight(0.8f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search sky condition ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                val filteredSize = filtered.size

                if (isGridView) {
                    // ── GRID VIEW ───────────────────────────────────────────
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        gridItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isChecked = selectedItems.contains(item)

                            val state =
                                if (isChecked) {
                                    val usage = usageMap?.get(item.id) ?: 0
                                    if (maxUsage != null && usage < maxUsage) {
                                        ToggleableState.Indeterminate
                                    }
                                    else ToggleableState.On
                                } else ToggleableState.Off

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        width = if (isChecked) 2.dp else 0.dp,
                                        color =
                                            when (state) {
                                                ToggleableState.On -> getOnCardColor()
                                                ToggleableState.Indeterminate -> getOnCardColor().copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            },
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable(enabled = true) {
                                        if (state == ToggleableState.On) onUnselected(item)
                                        else onSelected(item)
                                    },
                                leadingContent = null,
                                headlineContent = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth() // Forces the column to span the whole grid cell width
                                            .padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        thumbnailProvider(item)

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (isChecked && maxUsage != null && maxUsage > 0) {
                                            val usage = usageMap?.get(item.id) ?: 0
                                            Text(
                                                "($usage / $maxUsage)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                },
                                trailingContent = null,
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize, isChecked),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ModalAddButton(
                                title = "Add new sky condition ...",
                                onAdd = { onAdd() }
                            )
                        }
                    }
                } else {
                    // ── LIST VIEW ───────────────────────────────────────────
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        listItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isChecked = selectedItems.contains(item)

                            val state =
                                if (isChecked) {
                                    val usage = usageMap?.get(item.id) ?: 0
                                    if (maxUsage != null && usage < maxUsage) {
                                        ToggleableState.Indeterminate
                                    }
                                    else ToggleableState.On
                                } else ToggleableState.Off

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        width = if (isChecked) 2.dp else 0.dp,
                                        color =
                                            when (state) {
                                                ToggleableState.On -> getOnCardColor()
                                                ToggleableState.Indeterminate -> getOnCardColor().copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            },
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable(enabled = true) {
                                        if (state == ToggleableState.On) onUnselected(item)
                                        else onSelected(item)
                                    },
                                leadingContent = {
                                    thumbnailProvider(item)
                                },
                                headlineContent = {
                                    Column() {
                                        Text(
                                            item.name,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isChecked && maxUsage != null && maxUsage > 0) {
                                            val usage = usageMap?.get(item.id) ?: 0
                                            Text(
                                                "($usage / $maxUsage)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                },
                                trailingContent = {
                                    TriStateCheckbox(
                                        state = state,
                                        onClick = null,
                                        enabled = true
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize, isChecked),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item {
                            ModalAddButton(
                                title = "Add new sky condition ...",
                                onAdd = { onAdd() }
                            )
                        }
                    }
                }
            }
        }
    }
}
