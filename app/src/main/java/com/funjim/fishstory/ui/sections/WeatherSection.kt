package com.funjim.fishstory.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.model.WeatherSummary
import com.funjim.fishstory.model.WeatherWithDetails
import com.funjim.fishstory.ui.cards.WeatherCard
import com.funjim.fishstory.ui.cards.WeatherSummaryCard
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.utils.BadgeHelp
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor

@Composable
fun WeatherSection(
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
fun WeatherSummarySection(
    list: List<WeatherSummary>,
    verbose: Boolean,
    showHelp: Boolean,
    useImperial: Boolean,
    emptyText: String = "No weather conditions are specified.",
    helpText: String = "You need help.",
    onAdd: (() -> Unit)? = null,
    onDelete: (Weather) -> Unit,
    onEdit: (Weather) -> Unit,
    onFishClick: (Weather, Boolean) -> Unit,
    onThumbnailLongClick: (() -> Unit)? = null,
    onToggleHelp: (() -> Unit)? = null
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

            if (onToggleHelp != null) {
                BadgeHelp(
                    selected = showHelp,
                    badgeColor = getMainButtonColor(),
                    textColor = getOnMainButtonColor(),
                    textStyle = MaterialTheme.typography.titleMedium,
                    size = 24.dp,
                    onClick = onToggleHelp
                )
                if (onAdd != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

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
                if (showHelp) {
                    Text(
                        text = helpText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                WeatherSummaryCard(
                    weather = list.first(),
                    verbose = verbose,
                    useImperial = useImperial,
                    index = 0,
                    totalItems = list.size,
                    onEdit = onEdit,
                    onFishClick= onFishClick,
                    onDelete = onDelete,
                    onThumbnailLongClick = onThumbnailLongClick
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
                                onDelete = onDelete,
                                onThumbnailLongClick = onThumbnailLongClick
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
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = helpText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
