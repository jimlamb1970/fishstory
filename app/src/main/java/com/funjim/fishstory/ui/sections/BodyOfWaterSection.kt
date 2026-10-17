package com.funjim.fishstory.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.BodyOfWaterSummary
import com.funjim.fishstory.ui.cards.BodyOfWaterSummaryCard
import com.funjim.fishstory.ui.utils.getOnMainColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun BodyOfWaterSummarySection(
    list: List<BodyOfWaterSummary>,
    verbose: Boolean,
    modifier: Modifier = Modifier,
    description: String = "No bodies of water are selected.",
    thumbnailFlow: (BodyOfWater) -> Flow<ByteArray?> = { flowOf(null) },
    onAdd: () -> Unit,
    onClick: (BodyOfWater) -> Unit = { _ -> },
    onFishClick: (BodyOfWater, Boolean) -> Unit = { _, _ -> },
    onSetThumbnail: (BodyOfWater) -> Unit,
    onClearThumbnail: (BodyOfWater) -> Unit,
    onDelete: (BodyOfWater) -> Unit
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
                modifier = Modifier.fillMaxWidth()
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
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}