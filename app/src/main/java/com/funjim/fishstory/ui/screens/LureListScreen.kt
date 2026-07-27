package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.LureItem
import com.funjim.fishstory.ui.utils.SortChip
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.getChipColor
import com.funjim.fishstory.ui.utils.getOnChipColor
import com.funjim.fishstory.viewmodels.LureSortOrder
import com.funjim.fishstory.viewmodels.LureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureListScreen(
    viewModel: LureViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    navigateToFishList: (String, Boolean) -> Unit,
    navigateBack: () -> Unit
) {
    val allLures by viewModel.luresWithDisplay.collectAsState(initial = emptyList())
    var lureToDelete by remember { mutableStateOf<Lure?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    // Filter lures based on the search query across common fields
    val filteredLures = remember(allLures, searchQuery) {
        if (searchQuery.isBlank()) {
            allLures
        } else {
            val query = searchQuery.trim()
            allLures.filter { item ->
                item.lure.name.contains(query, ignoreCase = true) ||
                // Check primary colors list
                item.primaryColors.any { color ->
                    color.name.contains(query, ignoreCase = true)
                } ||
                // Check secondary colors list
                item.secondaryColors.any { color ->
                    color.name.contains(query, ignoreCase = true)
                } ||
                // Check glow colors list
                item.glowColors.any { color ->
                    color.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lures")
                        Spacer(Modifier.width(4.dp))
                        val total = filteredLures.size
                        Text(
                            text = "($total)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    TextButton(
                        onClick = onAdd,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search / Filter Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search lures...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Sort Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    SortChip(
                        "Name",
                        currentOrder == LureSortOrder.NAME
                    ) {
                        viewModel.setSortOrder(LureSortOrder.NAME)
                    }
                    SortChip(
                        "Primary Color",
                        currentOrder == LureSortOrder.PRIMARY_COLOR
                    ) {
                        viewModel.setSortOrder(LureSortOrder.PRIMARY_COLOR)
                    }
                    SortChip(
                        "Secondary Color",
                        currentOrder == LureSortOrder.SECONDARY_COLOR
                    ) {
                        viewModel.setSortOrder(LureSortOrder.SECONDARY_COLOR)
                    }
                    SortChip(
                        "Glow Color",
                        currentOrder == LureSortOrder.GLOW_COLOR
                    ) {
                        viewModel.setSortOrder(LureSortOrder.GLOW_COLOR)
                    }
                    SortChip(
                        "Glows",
                        currentOrder == LureSortOrder.GLOW
                    ) {
                        viewModel.setSortOrder(LureSortOrder.GLOW)
                    }
                    SortChip(
                        "Hook Type",
                        currentOrder == LureSortOrder.HOOKS
                    ) {
                        viewModel.setSortOrder(LureSortOrder.HOOKS)
                    }
                }

                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = { viewModel.toggleReverse() },
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = getChipColor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Reverse Sort",
                        tint = getOnChipColor(),
                    )
                }
            }

            if (filteredLures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isBlank()) "No lures found. Add one!"
                        else "No lures match '$searchQuery'"
                    )
                }
            } else {
                val listState = rememberLazyListState()

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        val totalItems = filteredLures.size
                        itemsIndexed(
                            filteredLures,
                            key = { _, item -> item.lure.id }
                        ) { index, item ->
                            LureItem(
                                item = item,
                                thumbnailFlow = viewModel.lureThumbnail(item.lure.id),
                                photosFlow = viewModel.lurePhotos(item.lure.id),
                                index = index,
                                totalItems = totalItems,
                                onPhotoAdded = { uri ->
                                    viewModel.addLurePhoto(
                                        lureId = item.lure.id,
                                        uri = uri,
                                        selected = true
                                    )
                                },
                                onPhotoTaken = { uri ->
                                    viewModel.addLurePhoto(
                                        lureId = item.lure.id,
                                        uri = uri,
                                        selected = false
                                    )
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deleteLurePhoto(item.lure.id, photo)
                                },
                                onFishClick = { lureId, targetOnly ->
                                    navigateToFishList(lureId, targetOnly)
                                },
                                onEdit = { onEdit(item.lure.id) },
                                onDelete = { lureToDelete = item.lure }
                            )
                        }
                    }

                    var isLeftAligned by remember { mutableStateOf(false) }

                    VerticalScrollToItemBar(
                        state = listState,
                        imageVector = AppIcons.Default.Lure,
                        onToggleAlignment = { isLeftAligned = !isLeftAligned },
                        modifier = Modifier
                            .align(if (isLeftAligned) Alignment.CenterStart else Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp, horizontal = 0.dp)
                    )
                }
            }
        }
    }

    // TODO - get fish counts and tackle box counts for lures
    // DELETE CONFIRMATION
    lureToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { lureToDelete = null },
            title = { Text("Delete Lure?") },
            text = {
                Text(
                    """Are you sure you want to delete '${item.name}'?

This cannot be undone.

If you delete this lure, it will be removed from all fish that were caught with it.

It will also be removed from all tackle boxes that contained it.
"""
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLure(item)
                        lureToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { lureToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}