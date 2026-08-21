package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.theme.AppIcons

@Composable
fun AddTackleBoxDialog(
    fishermanId: String,
    onConfirm: (TackleBox) -> Unit,
    onDismiss: () -> Unit,
) {
    EditTackleBoxDialog(
        item = TackleBox(name = "", fishermanId = fishermanId),
        title = "Add",
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditTackleBoxDialog(
    item: TackleBox,
    title: String = "Rename",
    onConfirm: (TackleBox) -> Unit,
    onDismiss: () -> Unit
) {
    val origName = remember(TackleBox) { item.name }
    var name by remember { mutableStateOf(origName) }

    val isValid = name.isNotBlank() && (origName != name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Tackle Box") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Tackle Box Name (e.g. Jim's Tackle Box)") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(item.copy(name = name.trim())) },
                enabled = isValid
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TackleBoxSelectionField(
    items: List<TackleBox>,
    selectedItem: TackleBox?,
    onSelected: (TackleBox) -> Unit,
    onAdd: (() -> Unit)?,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = selectedItem?.name ?: "Select Tackle Box",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Tackle Box") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    TackleBoxBottomSheet(
        showSheet = showSheet,
        items = items,
        selectedItem = selectedItem,
        onSelected = onSelected,
        onAdd = onAdd,
        onClear = onClear,
        onDismissRequest = { showSheet = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TackleBoxBottomSheet(
    showSheet: Boolean,
    items: List<TackleBox>,
    selectedItem: TackleBox?,
    onSelected: (TackleBox) -> Unit,
    onAdd: (() -> Unit)?,
    onClear: () -> Unit,
    onDismissRequest: () -> Unit
) {
    if (!showSheet) return

    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest },
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Select Tackle Box",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                        contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            TextButton(
                onClick = {
                    onDismissRequest()
                    searchQuery = ""
                }
            ) {
                Text("Done")
            }
        }

        Column(modifier = Modifier
            .fillMaxHeight(0.8f)
            .padding(start = 16.dp, end = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search tackle boxes ...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filtered = items.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }.sortedBy { it.name }
            val filteredSize = filtered.size

            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    gridItemsIndexed(
                        items = filtered,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        val isSelected = item == selectedItem

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color =
                                        if (isSelected) getOnCardColor()
                                        else Color.Transparent,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    onSelected(item)
                                    onDismissRequest()
                                    searchQuery = ""
                                },
                            headlineContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    ThumbnailBox(
                                        thumbnail = null,
                                        imageVector = AppIcons.Default.TackleBox,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight =
                                            if (isSelected) FontWeight.Bold
                                            else FontWeight.Normal,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = getGridCardColor(index, filteredSize, isSelected),
                                headlineColor = getOnCardColor()
                            )
                        )
                    }

                    if (selectedItem != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider() }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ModalResetButton(
                                title = "Reset Tackle Box",
                                onClear = { onDismissRequest(); onClear() }
                            )
                        }
                    }

                    if (onAdd != null) {
                        item(span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider() }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ModalAddButton(
                                title = "Create new tackle box...",
                                onAdd = { onDismissRequest(); onAdd() }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listItemsIndexed(filtered) { index, item ->
                        val isSelected = item == selectedItem

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color =
                                        if (isSelected) getOnCardColor()
                                        else Color.Transparent,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    onSelected(item)
                                    onDismissRequest()
                                    searchQuery = ""
                                },
                            leadingContent = {
                                ThumbnailBox(
                                    thumbnail = null,
                                    imageVector = AppIcons.Default.TackleBox,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight =
                                        if (isSelected) FontWeight.Bold
                                        else FontWeight.Normal,
                                    color = getOnCardColor()
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = getCardColor(index, filteredSize, isSelected),
                                headlineColor = getOnCardColor()
                            )
                        )
                    }

                    if (selectedItem != null) {
                        item { HorizontalDivider() }
                        item { ModalResetButton(
                            title = "Reset Tackle Box",
                            onClear = { onDismissRequest(); onClear() }
                        ) }
                    }

                    if (onAdd != null) {
                        item { HorizontalDivider() }
                        item { ModalAddButton(
                            title = "Create new tackle box...",
                            onAdd = { onDismissRequest(); onAdd() }
                        ) }
                    }
                }
            }
        }
    }
}
