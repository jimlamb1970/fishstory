package com.funjim.fishstory.ui.utils

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.theme.FishstoryTheme
import com.funjim.fishstory.ui.utils.cards.FishermanItem
import com.funjim.fishstory.ui.utils.cards.FishermanSummaryCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun FishermanRow(
    items: List<Fisherman>,
    onAdd: () -> Unit,
    onClick: (Fisherman) -> Unit,
    onDelete: (Fisherman) -> Unit,
    thumbnailProvider: @Composable (Fisherman) -> Unit,
    tackleBoxProvider: @Composable (Fisherman) -> Unit,
    modifier: Modifier = Modifier,
    description: String = "No fishermen are selected."
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
                    text = "Fishermen",
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
                    contentDescription = "Add Fisherman"
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sortedItems = items.sortedBy { it.fullName }
                sortedItems.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { onClick(item) },
                        label = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(item.fullName)
                                tackleBoxProvider(item)
                            }
                        },
                        avatar = { thumbnailProvider(item) },
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
                                onClick = { onDelete(item) },
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
fun FishermanSummaries(
    list: List<FishermanSummary>,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    description: String = "No fishermen are selected.",
    thumbnailFlow: (Fisherman) -> Flow<ByteArray?> = { flowOf(null) },
    photosFlow: (Fisherman) -> Flow<List<Photo>> = { flowOf(emptyList()) },
    tackleBoxSelections: Map<String, String?>,
    getTackleBoxesForFisherman: @Composable (fishermanId: String) -> List<TackleBox>,
    getLureCount: @Composable (tackleBoxId: String?) -> Int,
    getLuresInTacklebox: @Composable (tackleBoxId: String?) -> List<LureWithColors>,
    onAdd: () -> Unit,
    onAddTackleBox: (TackleBox) -> Unit,
    onAddLuresToTackleBox: (TackleBox) -> Unit,
    onClick: (Fisherman) -> Unit = { _ -> },
    onFishClick: (Fisherman, Boolean) -> Unit = { _, _ -> },
    onTackleBoxSelected: (Fisherman, String?) -> Unit,
    onPhotoAdded: (Fisherman, Uri) -> Unit = { _, _ -> },
    onPhotoTaken: (Fisherman, Uri) -> Unit = { _, _ -> },
    onSetThumbnail: (Fisherman, Photo) -> Unit,
    onPhotoDeleted: (Fisherman, Photo) -> Unit = { _, _ -> },
    onDelete: ((Fisherman) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fishermen",
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
                    contentDescription = "Add Fisherman"
                )
            }
        }

        if (list.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                list.forEachIndexed { index, item ->
                    val selectedTackleBoxId = tackleBoxSelections[item.fisherman.id]
                    val availableBoxes = getTackleBoxesForFisherman(item.fisherman.id)
                    val lureCount = getLureCount(selectedTackleBoxId)
                    val lures = getLuresInTacklebox(selectedTackleBoxId).sortedBy { it.lure.name }

                    FishermanSummaryCard(
                        item = item,
                        verbose = verbose,
                        index = index,
                        totalItems = list.size,
                        thumbnailFlow = thumbnailFlow(item.fisherman),
                        photosFlow = photosFlow(item.fisherman),
                        selectedTackleBoxId = selectedTackleBoxId,
                        availableBoxes = availableBoxes,
                        lureCount = lureCount,
                        lures = lures,
                        onAddTackleBox = onAddTackleBox,
                        onAddLuresToTackleBox = onAddLuresToTackleBox,
                        onTackleBoxSelected = onTackleBoxSelected,
                        onClick = onClick,
                        onFishClick= onFishClick,
                        onPhotoAdded = { uri -> onPhotoAdded(item.fisherman, uri) },
                        onPhotoTaken = { uri -> onPhotoTaken(item.fisherman, uri) },
                        onSetThumbnail = { photo -> onSetThumbnail(item.fisherman, photo) },
                        onPhotoDeleted = { photo -> onPhotoDeleted(item.fisherman, photo) },
                        onDelete = onDelete
                    )
                }
            }
        } else {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FishermanItemPreview() {
    FishstoryTheme(selectedTheme = null) {
        FishermanItem(
            fisherman = FishermanSummary(
                fisherman = Fisherman(firstName = "John", lastName = "Doe", nickname = "Big Fish"),
                fishCaught = 10,
                fishKept = 2,
                targetFishCaught = 8,
                targetFishKept = 1,
                totalTrips = 5,
                totalTackleBoxes = 3,
                largestFish = 0,
                smallestFish = 0
            ),
            verbose = true,
            thumbnailFlow = flowOf(null),
            photosFlow = flowOf(emptyList()),
            onClick = {},
            onFishClick = { _, _ -> },
            onPhotoAdded = {},
            onPhotoTaken = {},
            onSetThumbnail = {},
            onPhotoDeleted = {},
            onDelete = {},
        )
    }
}
