package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.ui.utils.LureItem
import com.funjim.fishstory.viewmodels.FishSortOrder
import com.funjim.fishstory.viewmodels.LureSortOrder
import com.funjim.fishstory.viewmodels.LureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureListScreen(
    viewModel: LureViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    navigateBack: () -> Unit
) {
    val allLures by viewModel.luresWithDisplay.collectAsState(initial = emptyList())
    var lureToDelete by remember { mutableStateOf<Lure?>(null) }

    val allPhotos by viewModel.lurePhotos.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lures") },
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
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
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
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    SortChip("Name",
                        currentOrder == LureSortOrder.NAME) {
                        viewModel.setSortOrder(LureSortOrder.NAME)
                    }
                    SortChip("Primary Color",
                        currentOrder == LureSortOrder.PRIMARY_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.PRIMARY_COLOR)
                    }
                    SortChip("Secondary Color",
                        currentOrder == LureSortOrder.SECONDARY_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.SECONDARY_COLOR)
                    }
                    SortChip("Glow Color",
                        currentOrder == LureSortOrder.GLOW_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.GLOW_COLOR)
                    }
                    SortChip("Glows",
                        currentOrder == LureSortOrder.GLOW) {
                        viewModel.setSortOrder(LureSortOrder.GLOW)
                    }
                    SortChip("Hook Type",
                        currentOrder == LureSortOrder.HOOK_TYPE) {
                        viewModel.setSortOrder(LureSortOrder.HOOK_TYPE)
                    }
                }

                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = { viewModel.toggleReverse() },
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        ).size(34.dp)
                ) {
                    Icon(
                        imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Reverse Sort",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (allLures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No lures found. Add one!")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val totalItems = allLures.size
                    itemsIndexed(allLures, key = { _, item -> item.lureSummary.lure.id }) { index, item ->
                        val photos = allPhotos[item.lureSummary.lure.id] ?: emptyList()
                        LureItem(
                            item = item.lureSummary,
                            index = index,
                            totalItems = totalItems,
                            primaryColorName = item.primaryColorName,
                            secondaryColorName = item.secondaryColorName,
                            glowColorName = item.glowColorName,
                            photos = photos,
                            onAddPhoto = null,
                            onDeletePhoto = null,
                            /* TODO - enable photos for lures
                            onAddPhoto = { photo -> scope.launch { viewModel.addPhoto(photo) } },
                            onDeletePhoto = { photo -> scope.launch { viewModel.deletePhoto(photo) } },
                             */
                            onEdit = { onEdit(item.lureSummary.lure.id) },
                            onDelete = { lureToDelete = item.lureSummary.lure }
                        )
                    }
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
            text = { Text("""Are you sure you want to delete '${item.name}'?

This cannot be undone.

If you delete this lure, it will be removed from all fish that were caught with it.

It will also be removed from all tackle boxes that contained it.
""") },
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
