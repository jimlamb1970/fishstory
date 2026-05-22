package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Species

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesSelectionField(
    items: List<Species>,
    selectedItem: Species?,
    onSelected: (Species) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailProvider: @Composable (Species) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = selectedItem?.name ?: "Select Species",
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Species") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
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
                        text = "Select Species",
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
                        showSheet = false
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
                    label = { Text("Search Species...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
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
                                        thumbnailProvider(item)

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize),
                                    headlineColor = getCardContentColor()
                                )
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AddSpeciesButton(onAdd = { showSheet = false; onAdd() } )
                        }
                    }
                } else {
                    LazyColumn {
                        listItemsIndexed(filtered) { index, item ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                leadingContent = { thumbnailProvider(item) },
                                headlineContent = { Text(item.name) },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize),
                                    headlineColor = getCardContentColor()
                                )
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item {
                            AddSpeciesButton(onAdd = { showSheet = false; onAdd() } )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSpeciesButton(onAdd: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                "Add new species...",
                color = getCardContentColor(),
                fontWeight = FontWeight.SemiBold
            )
        },
        leadingContent = { Icon(Icons.Default.Add, null) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onAdd() }
    )
}
