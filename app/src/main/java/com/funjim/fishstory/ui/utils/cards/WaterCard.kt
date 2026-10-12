package com.funjim.fishstory.ui.utils.cards

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterSummary
import com.funjim.fishstory.model.WaterWithDetails
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.FishCaughtItem
import com.funjim.fishstory.ui.utils.depthDisplayString
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.tempDisplayString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WaterCard(
    water: WaterWithDetails,
    useImperial: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: ((Water) -> Unit)? = null,
    onDelete: ((Water) -> Unit)? = null,
    onThumbnailLongClick: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    val dateTime = dateTimeFormatter.format(Date(water.water.timestamp))

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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .combinedClickable(
                        onClick = { /* Do nothing */ },
                        onLongClick = onThumbnailLongClick,
                        role = Role.Button
                    )
            ) {
                Icon(
                    imageVector = AppIcons.Default.WaterSet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                water.clarity?.let {
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
    onDelete: (Water) -> Unit,
    onThumbnailLongClick: (() -> Unit)? = null
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .combinedClickable(
                        onClick = { /* Do nothing */ },
                        onLongClick = onThumbnailLongClick,
                        role = Role.Button
                    )
            ) {
                Icon(
                    imageVector = AppIcons.Default.WaterSet,
                    contentDescription = "Water Conditions",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
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
                water.clarity?.let {
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