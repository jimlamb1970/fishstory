package com.funjim.fishstory.ui.cards

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitScope
import com.funjim.fishstory.model.LimitSummary
import com.funjim.fishstory.model.LimitType
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.utils.GumballStatus
import com.funjim.fishstory.ui.utils.StatusGumball
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.rememberGumballStatus
import com.funjim.fishstory.ui.utils.toLengthDisplayString

private fun formatSizeConstraint(
    limit: Limit,
    useImperial: Boolean
): String {
    val lowerInches = limit.lowerSize?.toLengthDisplayString(useImperial = useImperial, useFractions = true)
    val upperInches = limit.upperSize?.toLengthDisplayString(useImperial = useImperial, useFractions = true)

    return when (limit.type) {
        LimitType.BAG_LIMIT -> ""
        LimitType.MIN_SIZE -> lowerInches?.let { "Min Length: $lowerInches" } ?: ""
        LimitType.MAX_SIZE -> upperInches?.let { "Max Length: $upperInches" } ?: ""
        LimitType.SLOT_LIMIT -> {
            if (lowerInches != null && upperInches != null) {
                "Slot Range: $lowerInches - $upperInches"
            } else ""
        }
        LimitType.TROPHY_LIMIT -> upperInches?.let { "Trophy Threshold: > $upperInches" } ?: ""
    }
}

@Composable
fun LimitSummaryCard(
    item: LimitSummary,
    verbose: Boolean,
    useImperial: Boolean,
    scope: LimitScope,
    modifier: Modifier = Modifier,
    eventThumbnailProvider: (@Composable (Event) -> Unit),
    speciesThumbnailProvider: @Composable (Species) -> Unit,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: (Limit) -> Unit,
    onDelete: (Limit) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    val allowMenu = (scope == LimitScope.EVENT && !item.isTripLimit) ||
            (scope == LimitScope.TRIP && item.isTripLimit)

    val limitExceedCount = item.limitExceededCount
    val limitReachedCount = item.limitReachedCount
    val limitCount = item.limitCount

    val gumballStatus = rememberGumballStatus(
        limitExceededCount = limitExceedCount,
        limitReachedCount = limitReachedCount,
        limitCount = limitCount
    )

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = {
                    if (item.summaryList.size > 1) {
                        isExpanded = !isExpanded
                    }
                },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusGumball(
                    status = gumballStatus,
                    size = 32.dp,
                    borderWidth = 2.dp,
                    borderColor = secondaryContentColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.limit.type.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (item.summaryList.size > 1) {
                        CardItemWithValue(
                            icon = AppIcons.Default.CanoeEmpty,
                            value = "${item.summaryList.size}",
                            description = if (verbose) "Events" else null,
                            contentColor = secondaryContentColor
                        )
                    } else {
                        item.summaryList.firstOrNull()?.event?.let { event ->
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    eventThumbnailProvider(event)
                                    Text(
                                        text = event.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = secondaryContentColor
                                    )
                                }
                            }
                            CardItemWithValue(
                                icon = AppIcons.Default.Fisherman,
                                value = "${item.summaryList.first().fishermanCount}",
                                description = if (verbose) "Fishermen" else null,
                                contentColor = secondaryContentColor
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.summaryList.size == 1) {
                            item.summaryList.first().let { eventSummary ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Limit: ${item.limit.count * eventSummary.fishermanCount}") },
                                    modifier = Modifier.height(24.dp)
                                )
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Kept: ${eventSummary.caughtCount}") },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        } else {
                            AssistChip(
                                onClick = { },
                                label = { Text("Base Limit: ${item.limit.count}") },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }

                    // Format & Display Size Range Constraints
                    val sizeDetails = remember(item.limit) { formatSizeConstraint(item.limit, useImperial = useImperial) }
                    if (sizeDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = sizeDetails,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Display Associated Species List
                    if (item.limit.species.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item.limit.species.forEach { species ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        speciesThumbnailProvider(species)
                                        Text(
                                            text = species.name,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (allowMenu) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Limit Options"
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
                                    onEdit(item.limit)
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
                                    onDelete(item.limit)
                                }
                            )
                        }
                    }
                }
            }

            // Expanded Section: Event Summaries Breakdown
            if ((item.summaryList.size > 1) && isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = secondaryContentColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.summaryList.forEach { eventSummary ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, borderColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusGumball(
                                    status =
                                        if (eventSummary.isLimitExceeded) {
                                            GumballStatus.BAD
                                        } else if (eventSummary.isLimitReached) {
                                            GumballStatus.CAUTION
                                        } else {
                                            GumballStatus.GOOD
                                        },
                                    size = 20.dp,
                                    borderWidth = 1.dp,
                                    borderColor = secondaryContentColor
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    eventSummary.event?.let { event ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            eventThumbnailProvider(event)
                                            Text(
                                                text = event.name,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    CardItemWithValue(
                                        icon = AppIcons.Default.Fisherman,
                                        value = "${eventSummary.fishermanCount}",
                                        description = if (verbose) "Fishermen" else null,
                                        contentColor = secondaryContentColor
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Limit: ${item.limit.count * eventSummary.fishermanCount}") },
                                            modifier = Modifier.height(22.dp)
                                        )
                                        AssistChip(
                                            onClick = { },
                                            label = { Text("Kept: ${eventSummary.caughtCount}") },
                                            modifier = Modifier.height(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
