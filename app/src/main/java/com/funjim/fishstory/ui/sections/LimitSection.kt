package com.funjim.fishstory.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitScope
import com.funjim.fishstory.model.LimitSummary
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.ui.cards.LimitSummaryCard
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.utils.getMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainButtonColor
import com.funjim.fishstory.ui.utils.getOnMainColor

@Composable
fun LimitSummarySection(
    itemList: List<LimitSummary>,
    verbose: Boolean,
    useImperial: Boolean,
    scope: LimitScope,
    emptyText: String = "No limits created.",
    helpText: String = "Limits track your total catches to help ensure compliance with local fishing regulations.",
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
        Spacer(modifier = Modifier.height(8.dp))
    }
}
