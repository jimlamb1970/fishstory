package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.BodyOfWaterSummary
import com.funjim.fishstory.ui.utils.cards.BodyOfWaterSummaryCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun BodiesOfWaterRow(
    items: List<BodyOfWater>,
    onAdd: () -> Unit,
    onClick: (BodyOfWater) -> Unit,
    onDelete: (BodyOfWater) -> Unit,
    thumbnailProvider: @Composable (BodyOfWater) -> Unit,
    modifier: Modifier = Modifier,
    description: String = "No bodies of water are selected.",
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
                    text = "Bodies of Water",
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
                    contentDescription = "Add Body of Water"
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
                val bodyOfWater = items.sortedBy { it.name }
                items(bodyOfWater) { bodyOfWater ->
                    InputChip(
                        selected = true,
                        onClick = { onClick(bodyOfWater) },
                        label = { Text(bodyOfWater.name) },
                        avatar = { thumbnailProvider(bodyOfWater) },
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
                                onClick = { onDelete(bodyOfWater) },
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
fun BodyOfWaterSummaries(
    list: List<BodyOfWaterSummary>,
    verbose: Boolean,
    description: String = "No bodies of water are selected.",
    thumbnailFlow: (BodyOfWater) -> Flow<ByteArray?> = { flowOf(null) },
    onAdd: () -> Unit,
    onClick: (BodyOfWater) -> Unit,
    onFishClick: (BodyOfWater, Boolean) -> Unit,
    onSetThumbnail: (BodyOfWater) -> Unit,
    onClearThumbnail: (BodyOfWater) -> Unit,
    onDelete: (BodyOfWater) -> Unit
) {
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
                Text(
                    text = "Bodies of Water",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )

                if (list.size > 1) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(${list.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = getOnMainColor()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                    contentDescription = "Add Body of Water"
                )
            }
        }

        if (list.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                list.forEachIndexed { index, item ->
                    BodyOfWaterSummaryCard(
                        item = item,
                        verbose = verbose,
                        thumbnailFlow = thumbnailFlow(item.bodyOfWater),
                        index = index,
                        totalItems = list.size,
                        onClick = onClick,
                        onFishClick= onFishClick,
                        onSetThumbnail = onSetThumbnail,
                        onClearThumbnail = onClearThumbnail,
                        onDelete = onDelete
                    )
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

@Composable
fun UpdateAllCatchesDialog(
    bodyOfWater: BodyOfWater?,
    content: String,
    onDismiss: () -> Unit,
    onConfirm: (BodyOfWater) -> Unit
) {
    // Only render the AlertDialog if there is a valid body of water target
    if (bodyOfWater != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Update Catches?") },
            text = { Text(text = content) },
            confirmButton = {
                TextButton(onClick = { onConfirm(bodyOfWater) }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}