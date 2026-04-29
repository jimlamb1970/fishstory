package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.ui.utils.LureItem
import com.funjim.fishstory.viewmodels.LureSortOrder
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LureListScreen(
    viewModel: LureViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    navigateToManageColors: () -> Unit,
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
                    IconButton(onClick = { navigateToManageColors() }) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "Manage Colors")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAdd() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lure")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chips = listOf(
                    LureSortOrder.NAME to "Name",
                    LureSortOrder.PRIMARY_COLOR to "Primary Color",
                    LureSortOrder.SECONDARY_COLOR to "Secondary Color",
                    LureSortOrder.GLOW_COLOR to "Glow Color",
                    LureSortOrder.GLOW to "Glows",
                    LureSortOrder.HOOK_TYPE to "Hook Type"
                )

                // TODO -- need to make this consistent with other screens
                items(chips) { (field, label) ->
                    val selected = currentOrder == field
                    FilterChip(
                        selected = selected,
                        onClick = {
                            if (currentOrder == field) {
                                viewModel.toggleReverse()
                            } else {
                                viewModel.setSortOrder(field)
                                if (reversed) {
                                    viewModel.toggleReverse()
                                }
                            }
                        },
                        label = {
                            Text(if (selected) "$label ${if (reversed) "↑" else "↓"}" else label)
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = MaterialTheme.colorScheme.tertiary,
                            selectedBorderWidth = 2.dp,
                            borderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        modifier = Modifier.padding(end = 4.dp)
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
                    itemsIndexed(allLures, key = { _, item -> item.lure.id }) { index, item ->
                        val photos = allPhotos[item.lure.id] ?: emptyList()
                        LureItem(
                            lure = item.lure,
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
                            onEdit = { onEdit(item.lure.id) },
                            onDelete = { lureToDelete = item.lure }
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

data class LureWithDisplay(
    val lure: com.funjim.fishstory.model.Lure,
    val primaryColorName: String?,
    val secondaryColorName: String?,
    val glowColorName: String?,
    val displayName: String
)
