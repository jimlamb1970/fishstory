package com.funjim.fishstory.ui.sections

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.ui.cards.SpeciesItem
import com.funjim.fishstory.ui.cards.TargetSpeciesItem
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.utils.BadgeHelp
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import com.funjim.fishstory.ui.utils.getOnSecondaryColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun TargetSpeciesSection(
    items: List<Species>,
    verbose: Boolean,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (Species) -> Unit,
    onClick: (Species) -> Unit,
    emptyText: String = "No target species have been selected.",
    helpText: String = "You need help.",
    summaryProvider: (Species) -> Flow<SpeciesSummary?> = { flowOf(null) },
    thumbnailFlow: (Species) -> Flow<ByteArray?> = { flowOf(null) }
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
                    text = "Target Species",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )
                if (items.size > 1) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(${items.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = getOnMainColor()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { onAdd() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = getMainButtonColor(),
                    contentColor = getOnMainButtonColor()
                ),
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Target Species"
                )
            }
        }

        if (items.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = getOnSecondaryColor(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            val species = items.sortedBy { it.name }

            species.forEachIndexed { index, item ->
                TargetSpeciesItem(
                    summary = summaryProvider(item),
                    verbose = verbose,
                    thumbnailFlow = thumbnailFlow(item),
                    index = index,
                    totalItems = items.size,
                    onClick = { species -> onClick(species) },
                    onFishClick = { species -> onClick(species) },
                    onDelete = { species -> onDelete(species) }
                )
            }
        }
    }
}

@Composable
fun SpeciesSummarySection(
    items: List<SpeciesSummary>,
    verbose: Boolean,
    showHelp: Boolean,
    modifier: Modifier = Modifier,
    emptyText: String = "No target species have been selected.",
    helpText: String = "You need help.",
    thumbnailFlow: (Species) -> Flow<ByteArray?> = { flowOf(null) },
    onAdd: () -> Unit,
    onClearThumbnail: (Species) -> Unit,
    onClick: (Species) -> Unit,
    onDelete: (Species) -> Unit,
    onSetThumbnail: (Species) -> Unit,
    onToggleHelp: (() -> Unit)? = null
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
                    text = "Target Species",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )
                if (items.size > 1) {
                    BadgeCount(
                        count = items.size,
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
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = { onAdd() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = getMainButtonColor(),
                    contentColor = getOnMainButtonColor()
                ),
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Target Species"
                )
            }
        }

        if (items.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = getOnSecondaryColor(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            if (showHelp) {
                Text(
                    text = helpText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            items.forEachIndexed { index, item ->
                SpeciesItem(
                    summary = item,
                    verbose = verbose,
                    thumbnailFlow = thumbnailFlow(item.species),
                    index = index,
                    totalItems = items.size,
                    onClick = { species -> onClick(species) },
                    onFishClick = { species -> onClick(species) },
                    onSetThumbnail = { species -> onSetThumbnail(species) },
                    onClearThumbnail = { species -> onClearThumbnail(species) },
                    onDelete = { species -> onDelete(species) }
                )
            }
        }
    }
}