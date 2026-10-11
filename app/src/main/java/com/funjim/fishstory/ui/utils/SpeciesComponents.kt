package com.funjim.fishstory.ui.utils

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.ui.theme.AppIcons
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

@Composable
fun SpeciesItem(
    summary: SpeciesSummary,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    thumbnailFlow: Flow<ByteArray?>,
    index: Int = 0,
    totalItems: Int = 0,
    onClick: (Species) -> Unit,
    onFishClick: (Species) -> Unit,
    onSetThumbnail: (Species) -> Unit,
    onClearThumbnail: (Species) -> Unit,
    onDelete: (Species) -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    var menuExpanded by remember { mutableStateOf(false) }
    var thumbnailMenuExpanded by remember { mutableStateOf(false) }

    val species = summary.species

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick(species) },
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
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.combinedClickable(
                        onClick = { /* Do nothing */ },
                        onLongClick = {
                            thumbnailMenuExpanded = true
                        }
                    )
                ) {
                    ThumbnailBox(
                        thumbnail = thumbnail,
                        modifier = Modifier.size(48.dp),
                        imageVector = AppIcons.Default.Species
                    )

                    DropdownMenu(
                        expanded = thumbnailMenuExpanded,
                        onDismissRequest = { thumbnailMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                if (thumbnail != null) Text("Update Thumbnail")
                                else Text("Select Thumbnail")
                            },
                            onClick = {
                                thumbnailMenuExpanded = false
                                onSetThumbnail(species)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription =
                                        null
                                )
                            }
                        )
                        if (thumbnail != null) {
                            DropdownMenuItem(
                                text = { Text("Reset Thumbnail") },
                                onClick = {
                                    thumbnailMenuExpanded = false
                                    onClearThumbnail(species)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.HideImage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            species.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (summary.targetFishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.TargetFish,
                            caughtCount = summary.targetFishCaught,
                            keptCount = summary.targetFishKept,
                            extraText = if (verbose) "fish" else "",
                            onClick = {
                                onFishClick(species)
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                if (thumbnail != null) Text("Update Thumbnail")
                                else Text("Select Thumbnail")
                            },
                            onClick = {
                                menuExpanded = false
                                onSetThumbnail(species)
                            },
                            leadingIcon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) }
                        )
                        if (thumbnail != null) {
                            DropdownMenuItem(
                                text = {
                                    Text("Reset Thumbnail")
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClearThumbnail(species)
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.HideImage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Remove") },
                            onClick = {
                                menuExpanded = false
                                onDelete(species)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TargetSpeciesItem(
    summary: Flow<SpeciesSummary?>,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    thumbnailFlow: Flow<ByteArray?>,
    index: Int = 0,
    totalItems: Int = 0,
    onClick: (Species) -> Unit,
    onFishClick: (Species) -> Unit,
    onDelete: (Species) -> Unit
) {
    val item by summary.collectAsState(initial = null)
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    item?.let { summary ->
        val species = summary.species

        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable(
                    onClick = { onClick(species) }
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
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThumbnailBox(
                        thumbnail = thumbnail,
                        modifier = Modifier.size(48.dp),
                        imageVector = AppIcons.Default.Species
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                species.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (summary.targetFishCaught != 0) {
                            Spacer(Modifier.height(4.dp))
                            FishCaughtItem(
                                icon = AppIcons.Default.TargetFish,
                                caughtCount = summary.targetFishCaught,
                                keptCount = summary.targetFishKept,
                                extraText = if (verbose) "target fish" else "",
                                onClick = {
                                    onFishClick(species)
                                },
                                contentColor = secondaryContentColor
                            )
                        }
                    }

                    IconButton(
                        onClick = { onDelete(species) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
