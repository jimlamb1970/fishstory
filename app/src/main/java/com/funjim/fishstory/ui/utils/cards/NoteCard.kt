package com.funjim.fishstory.ui.utils.cards

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteCard(
    item: Note,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    onEdit: (Note) -> Unit,
    onDelete: (Note) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    val dateTime = dateTimeFormatter.format(Date(item.timestamp))

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                onClick = {},
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.Default.Notes,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.content,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Note Options"
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
                            onEdit(item)
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
                            onDelete(item)
                        }
                    )
                }
            }
        }
    }
}