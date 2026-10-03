package com.funjim.fishstory.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TextField
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
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.WaterSummary
import com.funjim.fishstory.model.WaterWithDetails
import com.funjim.fishstory.ui.theme.AppIcons
import java.text.SimpleDateFormat
import java.util.Date
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

private fun Water.tempDisplayString(useImperial: Boolean = true): String? {
    val tempDb = temperature ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%.1f°C", tempDb.toCelsiusDouble())
    } else {
        String.format(Locale.getDefault(), "%.1f°F", tempDb.toFahrenheitDouble())
    }
}

private fun Water.depthDisplayString(): String? {
    return depth?.let { totalInches ->
        val feet = totalInches.toInches().toLong() / 12
        val inches = totalInches.toInches().toLong() % 12

        when {
            feet > 0 && inches > 0 -> "${feet}' ${inches}\""
            feet > 0 -> "${feet}'"
            else -> "${inches}\""
        }
    }
}

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
fun WaterCard(
    water: WaterWithDetails,
    useImperial: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: ((Water) -> Unit)? = null,
    onDelete: ((Water) -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    val dateTime = dateTimeFormatter.format(Date(water.water.timestamp))

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
                imageVector = AppIcons.Default.WaterSet,
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
                if (water.water.temperature != null || water.water.depth != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        water.water.tempDisplayString(useImperial)?.let { temp ->
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
                        water.water.depthDisplayString()?.let { depth ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Depth: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = depth,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (water.clarity != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Clarity: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = water.clarity.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if ((onEdit != null) || (onDelete != null)) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Water Snapshot Options"
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
                                    onEdit(water.water)
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
                                    onDelete(water.water)
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
fun WaterSummaryCard(
    water: WaterSummary,
    verbose: Boolean,
    useImperial: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: (Water) -> Unit,
    onFishClick: (Water, Boolean) -> Unit,
    onDelete: (Water) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    val dateTime = dateTimeFormatter.format(Date(water.water.timestamp))

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
                imageVector = AppIcons.Default.WaterSet,
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
                if (water.water.temperature != null || water.water.depth != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        water.water.tempDisplayString(useImperial)?.let { temp ->
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
                        water.water.depthDisplayString()?.let { depth ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Depth: ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = depth,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                if (water.clarity != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Clarity: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = water.clarity.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (water.fishCaught != 0) {
                    Spacer(Modifier.height(4.dp))
                    FishCaughtItem(
                        icon = AppIcons.Default.LeapingFishWithFins,
                        caughtCount = water.fishCaught,
                        keptCount = water.fishKept,
                        extraText = if (verbose) "fish" else "",
                        onClick = {
                            onFishClick(water.water, false)
                        },
                        contentColor = secondaryContentColor
                    )
                }
                if (water.targetFishCaught != 0) {
                    Spacer(Modifier.height(4.dp))
                    FishCaughtItem(
                        icon = AppIcons.Default.TargetFish,
                        caughtCount = water.targetFishCaught,
                        keptCount = water.targetFishKept,
                        extraText = if (verbose) "target fish" else "",
                        onClick = {
                            onFishClick(water.water, true)
                        },
                        contentColor = secondaryContentColor
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Water Snapshot Options"
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
                            onEdit(water.water)
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
                            onDelete(water.water)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WaterDialog(
    initialTemp: Long?,
    initialDepth: Long?,
    initialClarity: String?,
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
            if (useImperial) {
                val fahrenheit = it.toFahrenheitDouble()
                if (fahrenheit == fahrenheit.toLong().toDouble()) {
                    fahrenheit.toLong().toString()
                } else {
                    fahrenheit.toString()
                }
            } else {
                val celsius = it.toCelsiusDouble()
                if (celsius == celsius.toLong().toDouble()) {
                    celsius.toLong().toString()
                } else {
                    celsius.toString()
                }
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
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(${waterList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = getOnMainColor()
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
    onAddWater: (() -> Unit)? = null,
    onEdit: (Water) -> Unit,
    onFishClick: (Water, Boolean) -> Unit,
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
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(${waterList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = getOnMainColor()
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                WaterSummaryCard(
                    water = waterList.first(),
                    verbose = verbose,
                    useImperial = useImperial,
                    index = 0,
                    totalItems = waterList.size,
                    onEdit = onEdit,
                    onFishClick= onFishClick,
                    onDelete = onDelete
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterClaritySelectionField(
    items: List<WaterClarity>,
    selectedItem: WaterClarity?,
    onSelected: (WaterClarity) -> Unit,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    thumbnailProvider: @Composable (WaterClarity) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = selectedItem?.name ?: "Select Water Clarity (optional)",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Water Clarity") },
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
                        text = "Select Water Clarity",
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
                    label = { Text("Search Water Clarity ...") },
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
                                    title = "Reset Water Clarity",
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
                                    title = "Add water clarity ...",
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
                                    title = "Reset Water Clarity",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }

                        if (onAdd != null) {
                            item { HorizontalDivider() }
                            item {
                                ModalAddButton(
                                    title = "Add water clarity ...",
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
fun WaterClaritySelection(
    items: List<WaterClarity>,
    selectedItems: List<WaterClarity>,
    onSelected: (WaterClarity) -> Unit,
    onUnselected: (WaterClarity) -> Unit,
    onAdd: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    usageMap: Map<String, Int>? = null,
    maxUsage: Int? = null,
    thumbnailProvider: @Composable (WaterClarity) -> Unit
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
                        text = "Select Water Clarity",
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
                    label = { Text("Search water clarity ...") },
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
                                title = "Add water clarity ...",
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
                                title = "Add water clarity ...",
                                onAdd = { onAdd() }
                            )
                        }
                    }
                }
            }
        }
    }
}
