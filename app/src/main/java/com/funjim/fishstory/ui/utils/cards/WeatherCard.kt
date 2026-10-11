package com.funjim.fishstory.ui.utils.cards

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.funjim.fishstory.ui.utils.FishCaughtItem
import com.funjim.fishstory.ui.utils.airHumidityDisplayString
import com.funjim.fishstory.ui.utils.airVisibilityDisplayString
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.tempDisplayString
import com.funjim.fishstory.ui.utils.toPressureDisplayString
import com.funjim.fishstory.ui.utils.windSpeedDisplayString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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