package com.funjim.fishstory.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterSummary
import com.funjim.fishstory.model.WaterWithDetails
import com.funjim.fishstory.ui.cards.WaterCard
import com.funjim.fishstory.ui.cards.WaterSummaryCard
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Converts Fahrenheit input (Double or String) to DB storage format.
 * Example: 72.5 -> 7250L
 */
fun Double.fahrenheitToDbValue(): Long = (this * 100.0).roundToLong()
fun String.fahrenheitToDbValue(): Long? = this.toDoubleOrNull()?.fahrenheitToDbValue()

/**
 * Converts Celsius input (Double or String) to DB storage format.
 * Example: 22.5 -> (22.5 * 180) + 3200 = 7250L
 */
fun Double.celsiusToDbValue(): Long = (this * 180.0 + 3200.0).roundToLong()
fun String.celsiusToDbValue(): Long? = this.toDoubleOrNull()?.celsiusToDbValue()

/**
 * Converts DB value (7250L) back to Fahrenheit Double (72.5).
 */
fun Long.toFahrenheitDouble(): Double = this / 100.0

/**
 * Converts DB value (7250L) back to Celsius Double (22.5).
 */
fun Long.toCelsiusDouble(): Double = (this - 3200.0) / 180.0

fun Water.tempDisplayString(useImperial: Boolean = true): String? {
    val tempDb = temperature ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%.1f°C", tempDb.toCelsiusDouble())
    } else {
        String.format(Locale.getDefault(), "%.1f°F", tempDb.toFahrenheitDouble())
    }
}

fun Water.depthDisplayString(useImperial: Boolean = true): String? {
    return depth?.let { depth ->
        if (useImperial) {
            val feet = depth.toInches().toLong() / 12
            val inches = depth.toInches().toLong() % 12

            when {
                feet > 0 && inches > 0 -> "${feet}' ${inches}\""
                feet > 0 -> "${feet}'"
                else -> "${inches}\""
            }
        } else {
            val totalCm = depth.toCm()
            val meters = totalCm / 100.0

            if (meters >= 1.0) {
                String.format(Locale.US, "%.1fm", meters).let {
                    if (it.endsWith(".0m")) it.replace(".0m", "m") else it
                }
            } else {
                "${totalCm.toLong()}cm"
            }
        }
    }
}

@Composable
fun WaterRow(
    waterList: List<WaterWithDetails>,
    useImperial: Boolean,
    onAddWater: () -> Unit,
    onEdit: (Water) -> Unit,
    onDelete: (Water) -> Unit
) {
    var isWaterSectionExpanded by remember { mutableStateOf(false) }

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
                if (waterList.size > 1) {
                    IconButton(
                        onClick = {
                            isWaterSectionExpanded = !isWaterSectionExpanded
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector =
                                if (isWaterSectionExpanded) Icons.Default.ExpandLess
                                else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                Text(
                    text = "Water Conditions",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )

                if (waterList.size > 1) {
                    BadgeCount(
                        count = waterList.size,
                        badgeColor = getMainButtonColor(),
                        textColor = getOnMainButtonColor()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onAddWater,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Water Condition"
                )
            }
        }

        if (waterList.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                WaterCard(
                    water = waterList.first(),
                    useImperial = useImperial,
                    index = 0,
                    totalItems = waterList.size,
                    onEdit = onEdit,
                    onDelete = onDelete
                )

                AnimatedVisibility(visible = isWaterSectionExpanded && waterList.size > 1) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        waterList.drop(1).forEachIndexed { index, water ->
                            WaterCard(
                                water = water,
                                useImperial = useImperial,
                                index = index + 1,
                                totalItems = waterList.size,
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
                    text = "No water conditions are set.",
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
fun WaterSummaryRow(
    waterList: List<WaterSummary>,
    verbose: Boolean,
    useImperial: Boolean,
    description: String = "No water conditions are specified.",
    onAddWater: (() -> Unit)? = null,
    onEdit: (Water) -> Unit,
    onFishClick: (Water, Boolean) -> Unit,
    onDelete: (Water) -> Unit,
    onThumbnailLongClick: (() -> Unit)? = null
) {
    var isWaterSectionExpanded by remember { mutableStateOf(false) }

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
                if (waterList.size > 1) {
                    IconButton(
                        onClick = {
                            isWaterSectionExpanded = !isWaterSectionExpanded
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector =
                                if (isWaterSectionExpanded) Icons.Default.ExpandLess
                                else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                Text(
                    text = "Water Conditions",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )

                if (waterList.size > 1) {
                    BadgeCount(
                        count = waterList.size,
                        badgeColor = getMainButtonColor(),
                        textColor = getOnMainButtonColor()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (onAddWater != null) {
                IconButton(
                    onClick = onAddWater,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Water Condition"
                    )
                }
            }
        }

        if (waterList.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                WaterSummaryCard(
                    water = waterList.first(),
                    verbose = verbose,
                    useImperial = useImperial,
                    index = 0,
                    totalItems = waterList.size,
                    onEdit = onEdit,
                    onFishClick= onFishClick,
                    onDelete = onDelete,
                    onThumbnailLongClick = onThumbnailLongClick
                )

                AnimatedVisibility(visible = isWaterSectionExpanded && waterList.size > 1) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        waterList.drop(1).forEachIndexed { index, water ->
                            WaterSummaryCard(
                                water = water,
                                useImperial = useImperial,
                                verbose = verbose,
                                index = index,
                                totalItems = waterList.size,
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