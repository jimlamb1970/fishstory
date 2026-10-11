package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.ui.utils.cards.SpeciesItem
import com.funjim.fishstory.ui.utils.cards.TargetSpeciesItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun TargetSpeciesRow(
    items: List<Species>,
    modifier: Modifier = Modifier,
    name: String = "Target Species",
    description: String = "No target species have been selected.",
    onAdd: () -> Unit,
    onDelete: (Species) -> Unit,
    thumbnailProvider: @Composable (Species, Dp) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                    text = name,
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
                    contentDescription = "Add ${name.lowercase()}"
                )
            }
        }

        if (items.isEmpty()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = getOnSecondaryColor(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val species = items.sortedBy { it.name }
                items(species) { species ->
                    InputChip(
                        selected = true,
                        onClick = {},
                        label = { Text(species.name) },
                        avatar = { thumbnailProvider(species, 18.dp) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = getCardColor().copy(alpha = 0.15f),
                            selectedLabelColor = getOnCardColor(),
                            selectedLeadingIconColor = getOnCardColor(),
                            selectedTrailingIconColor = MaterialTheme.colorScheme.error
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            selectedBorderColor = getCardBorderColor(),
                            selectedBorderWidth = 1.dp,
                            borderColor = getOnChipColor(),
                            borderWidth = 1.dp
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { onDelete(species) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TargetSpeciesColumn(
    items: List<Species>,
    verbose: Boolean,
    onAdd: () -> Unit,
    onDelete: (Species) -> Unit,
    onClick: (Species) -> Unit,
    modifier: Modifier = Modifier,
    description: String = "No target species have been selected.",
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
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = getOnSecondaryColor(),
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
fun SpeciesSummaries(
    items: List<SpeciesSummary>,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    description: String = "No target species have been selected.",
    thumbnailFlow: (Species) -> Flow<ByteArray?> = { flowOf(null) },
    onAdd: () -> Unit,
    onClick: (Species) -> Unit,
    onSetThumbnail: (Species) -> Unit,
    onClearThumbnail: (Species) -> Unit,
    onDelete: (Species) -> Unit
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
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = getOnSecondaryColor(),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
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