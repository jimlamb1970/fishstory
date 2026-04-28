package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.viewmodels.LureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageColorsScreen(
    viewModel: LureViewModel,
    navigateBack: () -> Unit
) {
    val colorsList by viewModel.lureColors.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var colorToDelete by remember { mutableStateOf<LureColor?>(null) }
    var colorToEdit by remember { mutableStateOf<LureColor?>(null) }
    var editName by remember { mutableStateOf("") }

    // 1. Logic: Filter list based on search query
    val filteredColors = remember(searchQuery, colorsList) {
        colorsList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.name }
    }

    // 2. Logic: Should we show the "Add" button?
    // Show if the query isn't empty and doesn't exactly match an existing name
    val showAddButton = searchQuery.isNotBlank() &&
            colorsList.none { it.name.equals(searchQuery.trim(), ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Colors") },
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
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // SEARCH & ADD SECTION
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search or add color...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (showAddButton) {
                        IconButton(onClick = {
                            viewModel.addLureColor(LureColor(name = searchQuery.trim()))
                            searchQuery = ""
                        }) {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                singleLine = true
            )

            // THE LIST
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(filteredColors, key = { _, s -> s.id }) { index, item ->
                    // Your Zebra Striping
                    val backgroundColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)

                    ListItem(
                        modifier = Modifier.background(backgroundColor),
                        headlineContent = { Text(item.name) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = {
                                    colorToEdit = item
                                    editName = item.name
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { colorToDelete = item }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = backgroundColor
                        )
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }

    // DELETE CONFIRMATION
    colorToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { colorToDelete = null },
            title = { Text("Delete Color?") },
            text = { Text("Are you sure you want to delete '${item.name}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLureColor(item)
                        colorToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { colorToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // EDIT DIALOG
    colorToEdit?.let { item ->
        AlertDialog(
            onDismissRequest = { colorToEdit = null },
            title = { Text("Rename Species") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    label = { Text("Species Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.upsertLureColor(item.copy(name = editName.trim()))
                    colorToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { colorToEdit = null }) { Text("Cancel") }
            }
        )
    }
}
