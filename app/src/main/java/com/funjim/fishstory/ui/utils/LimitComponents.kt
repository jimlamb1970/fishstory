package com.funjim.fishstory.ui.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun LimitSummaryRow(
    itemList: List<LimitSummary>,
    verbose: Boolean,
    useImperial: Boolean,
    scope: LimitScope,
    description: String = """No limits created.
        |
        |Limits track your total catches to help ensure compliance with local fishing regulations.
    """.trimMargin(),
    modifier: Modifier,
    eventThumbnailProvider: @Composable (Event) -> Unit = {},
    speciesThumbnailProvider: @Composable (Species) -> Unit,
    onAdd: (() -> Unit)? = null,
    onEdit: (Limit) -> Unit,
    onDelete: (Limit) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Limits",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )

                if (itemList.size > 1) {
                    BadgeCount(
                        count = itemList.size,
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
                        contentDescription = "Add Limit"
                    )
                }
            }
        }

        if (itemList.isNotEmpty()) {
            itemList.forEachIndexed { index, item ->
                LimitSummaryCard(
                    item = item,
                    verbose = verbose,
                    useImperial = useImperial,
                    scope = scope,
                    eventThumbnailProvider = eventThumbnailProvider,
                    speciesThumbnailProvider = speciesThumbnailProvider,
                    index = index,
                    totalItems = itemList.size,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        } else {
            Column(
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
fun rememberGumballStatus(
    limitExceededCount: Int,
    limitReachedCount: Int,
    limitCount: Int
): GumballStatus {
    var showCautionState by remember { mutableStateOf(false) }

    val primaryStatus = if (limitExceededCount > 0) GumballStatus.BAD else GumballStatus.GOOD

    LaunchedEffect(limitReachedCount, limitCount) {
        if (limitReachedCount > 0) {
            if (limitCount > limitReachedCount) {
                while (true) {
                    showCautionState = !showCautionState
                    kotlinx.coroutines.delay(1000L)
                }
            } else {
                showCautionState = true
            }
        } else {
            showCautionState = false
        }
    }

    return if (showCautionState) GumballStatus.CAUTION else primaryStatus
}