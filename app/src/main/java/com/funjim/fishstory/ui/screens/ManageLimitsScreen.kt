package com.funjim.fishstory.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.ui.cards.LimitCard
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.crud.LimitDialog
import com.funjim.fishstory.ui.utils.GumballStatus
import com.funjim.fishstory.ui.utils.StatusGumball
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.viewmodels.LimitViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ManageLimitsScreen(
    viewModel: LimitViewModel,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val useImperial = appSettings.display.useImperialUnits

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val allSpecies by viewModel.allSpecies.collectAsStateWithLifecycle()
    val allItems by viewModel.allLimits.collectAsStateWithLifecycle(initialValue = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<Limit?>(null) }
    var itemToEdit by remember { mutableStateOf<Limit?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val filteredItems = remember(searchQuery, allItems) {
        allItems.filter { limit ->
            limit.name.contains(searchQuery, ignoreCase = true) ||
                    limit.species.any { it.name.contains(searchQuery, ignoreCase = true) }
        }.sortedBy { it.name }
    }

    val showAddButton = searchQuery.isNotBlank() &&
            allItems.none { it.name.equals(searchQuery.trim(), ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Limits")
                        val total = allItems.size
                        BadgeCount(
                            count = total,
                            badgeColor = MaterialTheme.colorScheme.onPrimary,
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Box(contentAlignment = Alignment.Center) {
                            StatusGumball(
                                status = GumballStatus.GOOD,
                                size = 28.dp,
                                borderWidth = 2.dp,
                                borderColor = getOnCardSecondaryColor()
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.trimStart() },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search or add new limit ...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (showAddButton) {
                        IconButton(onClick = {
                            searchQuery = ""
                        }) {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add New",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                val filteredSize = filteredItems.size
                itemsIndexed(filteredItems, key = { _, s -> s.id }) { index, item ->
                    LimitCard(
                        item = item,
                        index = index,
                        count = filteredSize,
                        useImperial = useImperial,
                        speciesThumbnailProvider = { species ->
                            val thumbnailFlow = viewModel.getSpeciesThumbnail(species.id)

                            val thumbnail by thumbnailFlow.collectAsState(initial = null)

                            ThumbnailBox(
                                thumbnail = thumbnail,
                                imageVector = AppIcons.Default.LeapingFishWithFins,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onEdit = { itemToEdit = item },
                        onDelete = { itemToDelete = item },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        LimitDialog(
            useImperial = appSettings.display.useImperialUnits,
            species = allSpecies,
            speciesThumbnailProvider = { species, size ->
                val thumbnailFlow = remember(species.id) {
                    viewModel.getSpeciesThumbnail(species.id)
                }

                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.LeapingFishWithFins,
                    modifier = Modifier.size(size)
                )
            },
            onDismiss = { showAddDialog = false },
            onConfirm = { limit ->
                viewModel.addLimit(limit)
                showAddDialog = false
            }
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Limit?") },
            text = { Text("""Are you sure you want to delete '${item.name}'?

This cannot be undone."""
            ) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLimit(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    itemToEdit?.let { item ->
        LimitDialog(
            limitToEdit = item,
            useImperial = appSettings.display.useImperialUnits,
            species = allSpecies,
            speciesThumbnailProvider = { species, size ->
                val thumbnailFlow = remember(species.id) {
                    viewModel.getSpeciesThumbnail(species.id)
                }

                val thumbnail by thumbnailFlow.collectAsState(initial = null)

                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.LeapingFishWithFins,
                    modifier = Modifier.size(size)
                )
            },
            onConfirm = { limit ->
                viewModel.addLimit(limit)
                itemToEdit = null
            },
            onDismiss = { itemToEdit = null }
        )
    }

}
