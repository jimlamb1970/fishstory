package com.funjim.fishstory.ui.sections

import android.net.Uri
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
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.cards.FishermanSummaryCard
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.utils.BadgeHelp
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun FishermanSummarySection(
    list: List<FishermanSummary>,
    verbose: Boolean,
    showHelp: Boolean,
    modifier: Modifier = Modifier,
    emptyText: String = "No fishermen are selected.",
    helpText: String = "You need help.",
    thumbnailFlow: (Fisherman) -> Flow<ByteArray?> = { flowOf(null) },
    photosFlow: (Fisherman) -> Flow<List<Photo>> = { flowOf(emptyList()) },
    tackleBoxSelections: Map<String, String?>,
    getTackleBoxesForFisherman: @Composable (fishermanId: String) -> List<TackleBox>,
    getLureCount: @Composable (tackleBoxId: String?) -> Int,
    getLuresInTacklebox: @Composable (tackleBoxId: String?) -> List<LureWithColors>,
    onAdd: () -> Unit,
    onAddLuresToTackleBox: (TackleBox) -> Unit,
    onAddTackleBox: (TackleBox) -> Unit,
    onClick: (Fisherman) -> Unit = { _ -> },
    onDelete: ((Fisherman) -> Unit)? = null,
    onFishClick: (Fisherman, Boolean) -> Unit = { _, _ -> },
    onPhotoAdded: (Fisherman, Uri) -> Unit = { _, _ -> },
    onPhotoDeleted: (Fisherman, Photo) -> Unit = { _, _ -> },
    onPhotoTaken: (Fisherman, Uri) -> Unit = { _, _ -> },
    onSetThumbnail: (Fisherman, Photo) -> Unit,
    onTackleBoxSelected: (Fisherman, String?) -> Unit,
    onToggleHelp: (() -> Unit)? = null
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
                if (showHelp) {
                    Text(
                        text = helpText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

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
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
