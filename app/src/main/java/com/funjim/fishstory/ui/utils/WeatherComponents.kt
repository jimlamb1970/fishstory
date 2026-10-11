package com.funjim.fishstory.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.model.WeatherSummary
import com.funjim.fishstory.model.WeatherWithDetails
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

private val BASE_PER_MPH = BigDecimal("16093440")
private val BASE_PER_KMH = BigDecimal("10000000")

/**
 * Converts DB airVisibility (stored in Millimeters as Long) to UI string.
 * @param useImperial If false, converts to Kilometers (KM). If true, converts to Miles (mi).
 */
fun Long?.airVisibilityDisplayString(useImperial: Boolean): String {
    if (this == null) return ""
    val divisor = if (!useImperial) MM_PER_KM else MM_PER_MILE

    return BigDecimal(this)
        .divide(divisor, 1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * Converts UI input string to DB Millimeters (Long).
 * @param useImperial If false, treats input as KM. If true, treats input as Miles.
 */
fun String.visibilityInputToDbValueOrNull(useImperial: Boolean): Long? {
    val decimalInput = this.toBigDecimalOrNull() ?: return null
    val multiplier = if (!useImperial) MM_PER_KM else MM_PER_MILE

    return decimalInput.multiply(multiplier)
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

/**
 * Converts DB windSpeed (stored in base Long unit) to UI string.
 * @param useImperial If false, converts to KM/H. If true, converts to MPH.
 */
fun Long?.windSpeedDisplayString(useImperial: Boolean): String {
    if (this == null) return ""
    val divisor = if (!useImperial) BASE_PER_KMH else BASE_PER_MPH

    return BigDecimal(this)
        .divide(divisor, 1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * Converts UI input string to DB Wind Speed (Long).
 * @param useImperial If false, treats input as KM/H. If true, treats input as MPH.
 */
fun String.windSpeedInputToDbValueOrNull(useImperial: Boolean): Long? {
    val decimalInput = this.toBigDecimalOrNull() ?: return null
    val multiplier = if (!useImperial) BASE_PER_KMH else BASE_PER_MPH

    return decimalInput.multiply(multiplier)
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

private fun Weather.airHumidityDisplayString(): String? {
    val airHumidityDb = airHumidity ?: return null
    return String.format(Locale.getDefault(), "${airHumidityDb}%%")
}
private fun Weather.airVisibilityDisplayString(useImperial: Boolean): String? {
    val visibilityDb = airVisibility ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%s km", visibilityDb.airVisibilityDisplayString(false))
    } else {
        String.format(Locale.getDefault(), "%s m", visibilityDb.airVisibilityDisplayString(true))
    }
}
private fun Weather.tempDisplayString(useImperial: Boolean): String? {
    val tempDb = temperature ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%.1f°C", tempDb.toCelsiusDouble())
    } else {
        String.format(Locale.getDefault(), "%.1f°F", tempDb.toFahrenheitDouble())
    }
}
private fun Weather.windSpeedDisplayString(useImperial: Boolean): String? {
    val speedDb = windSpeed ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%s km/h", speedDb.windSpeedDisplayString(false))
    } else {
        String.format(Locale.getDefault(), "%s mph", speedDb.windSpeedDisplayString(true))
    }
}


@Composable
fun WeatherCard(
    weather: WeatherWithDetails,
    useImperial: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: ((Weather) -> Unit)? = null,
    onDelete: ((Weather) -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    val dateTime = dateTimeFormatter.format(Date(weather.weather.timestamp))

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()

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
            Icon(
                imageVector = AppIcons.Default.WeatherSet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                weather.weather.tempDisplayString(useImperial)?.let { temp ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Temperature:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = temp,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (weather.skyCondition != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sky Condition:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = weather.skyCondition.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.windDirection?.let { direction ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wind Direction:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = direction.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.windSpeedDisplayString(useImperial)?.let { speed ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wind Speed:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = speed,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.atmosphericPressure?.toPressureDisplayString(useImperial)?.let {
                    pressureStr ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Atmospheric Pressure:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = pressureStr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.airVisibilityDisplayString(useImperial)?.let { visibility ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Air Visibility:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = visibility,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.airHumidityDisplayString()?.let { humidity ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Air Humidity:",
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

            if (onEdit != null || onDelete != null) {
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
                        if (onEdit != null) {
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
                        }
                        if (onDelete != null) {
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
    }
}

@Composable
fun WeatherSummaryCard(
    weather: WeatherSummary,
    verbose: Boolean,
    useImperial: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: (Weather) -> Unit,
    onFishClick: (Weather, Boolean) -> Unit,
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
            Icon(
                imageVector = AppIcons.Default.WeatherSet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                weather.weather.tempDisplayString(useImperial)?.let { temp ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Temperature:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = temp,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (weather.skyCondition != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sky Condition:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = weather.skyCondition.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.windDirection?.let { direction ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wind Direction:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = direction.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.windSpeedDisplayString(useImperial)?.let { speed ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wind Speed:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = speed,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.atmosphericPressure?.toPressureDisplayString(useImperial)?.let {
                    pressureStr ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Atmospheric Pressure:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = pressureStr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.airVisibilityDisplayString(useImperial)?.let { visibility ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Air Visibility:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = visibility,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                weather.weather.airHumidityDisplayString()?.let { humidity ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Air Humidity:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = humidity,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (weather.fishCaught != 0) {
                    Spacer(Modifier.height(4.dp))
                    FishCaughtItem(
                        icon = AppIcons.Default.LeapingFishWithFins,
                        caughtCount = weather.fishCaught,
                        keptCount = weather.fishKept,
                        extraText = if (verbose) "fish" else "",
                        onClick = {
                            onFishClick(weather.weather, false)
                        },
                        contentColor = secondaryContentColor
                    )
                }
                if (weather.targetFishCaught != 0) {
                    Spacer(Modifier.height(4.dp))
                    FishCaughtItem(
                        icon = AppIcons.Default.TargetFish,
                        caughtCount = weather.targetFishCaught,
                        keptCount = weather.targetFishKept,
                        extraText = if (verbose) "target fish" else "",
                        onClick = {
                            onFishClick(weather.weather, true)
                        },
                        contentColor = secondaryContentColor
                    )
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

@Composable
fun WeatherRow(
    weatherList: List<WeatherWithDetails>,
    useImperial: Boolean,
    onAddWeather: () -> Unit,
    onEdit: (Weather) -> Unit,
    onDelete: (Weather) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column() {
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
                    BadgeCount(
                        count = weatherList.size,
                        badgeColor = getMainButtonColor(),
                        textColor = getOnMainButtonColor()
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
                    useImperial = useImperial,
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
                                useImperial = useImperial,
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
}

@Composable
fun WeatherSummaryRow(
    list: List<WeatherSummary>,
    verbose: Boolean,
    useImperial: Boolean,
    description: String = "No weather conditions are specified.",
    onAdd: (() -> Unit)? = null,
    onEdit: (Weather) -> Unit,
    onFishClick: (Weather, Boolean) -> Unit,
    onDelete: (Weather) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (list.size > 1) {
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

                if (list.size > 1) {
                    BadgeCount(
                        count = list.size,
                        badgeColor = getMainButtonColor(),
                        textColor = getOnMainButtonColor()
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
                        contentDescription = "Add Weather"
                    )
                }
            }
        }

        if (list.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                WeatherSummaryCard(
                    weather = list.first(),
                    verbose = verbose,
                    useImperial = useImperial,
                    index = 0,
                    totalItems = list.size,
                    onEdit = onEdit,
                    onFishClick= onFishClick,
                    onDelete = onDelete
                )

                AnimatedVisibility(visible = isExpanded && list.size > 1) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        list.drop(1).forEachIndexed { index, item ->
                            WeatherSummaryCard(
                                weather = item,
                                verbose = verbose,
                                useImperial = useImperial,
                                index = index,
                                totalItems = list.size,
                                onEdit = onEdit,
                                onFishClick= onFishClick,
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
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}