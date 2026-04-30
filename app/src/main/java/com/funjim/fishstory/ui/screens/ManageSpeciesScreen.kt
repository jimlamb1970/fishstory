package com.funjim.fishstory.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.viewmodels.FishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSpeciesScreen(
    viewModel: FishViewModel,
    navigateBack: () -> Unit
) {
//    val speciesList by viewModel.species.collectAsStateWithLifecycle(initialValue = emptyList())
    val speciesSummaries by viewModel.speciesSummaries.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var speciesToDelete by remember { mutableStateOf<Species?>(null) }
    var speciesToEdit by remember { mutableStateOf<Species?>(null) }
    var editName by remember { mutableStateOf("") }

    val context = LocalContext.current

    // 1. Logic: Filter list based on search query
    val filteredSpecies = remember(searchQuery, speciesSummaries) {
        speciesSummaries.filter {
            it.species.name.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.species.name }
    }

    // 2. Logic: Should we show the "Add" button?
    // Show if the query isn't empty and doesn't exactly match an existing name
    val showAddButton = searchQuery.isNotBlank() &&
            speciesSummaries.none { it.species.name.equals(searchQuery.trim(), ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Species") },
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
                placeholder = { Text("Search or add species...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (showAddButton) {
                        IconButton(onClick = {
                            viewModel.addSpecies(Species(name = searchQuery.trim()))
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                singleLine = true
            )

            // THE LIST
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(filteredSpecies, key = { _, s -> s.species.id }) { index, species ->
                    // Your Zebra Striping
                    val backgroundColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)

                    ListItem(
                        modifier = Modifier.background(backgroundColor),
                        headlineContent = { Text(species.species.name) },
                        supportingContent = {
                            Text("Caught: ${species.caughtCount}, Kept: ${species.keptCount}")
                        },
                        trailingContent = {
                            Row {
                                val preventDelete = species.caughtCount > 0 || species.keptCount > 0

                                IconButton(onClick = {
                                    speciesToEdit = species.species
                                    editName = species.species.name
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(
                                    onClick = {
                                        if (preventDelete) {
                                            Toast.makeText(
                                                context,
                                                "Can't delete this species. There are fish logged for it.",
                                                Toast.LENGTH_SHORT).show()
                                        } else {
                                            speciesToDelete = species.species
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint =
                                            if (!preventDelete) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                                    )
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

    // TODO -- prevent deletion of species that are in use
    // DELETE CONFIRMATION
    speciesToDelete?.let { species ->
        AlertDialog(
            onDismissRequest = { speciesToDelete = null },
            title = { Text("Delete Species?") },
            text = { Text("""Are you sure you want to delete '${species.name}'?

This cannot be undone."""
            ) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSpecies(species)
                        speciesToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { speciesToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // EDIT DIALOG
    speciesToEdit?.let { species ->
        AlertDialog(
            onDismissRequest = { speciesToEdit = null },
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
                    viewModel.upsertSpecies(species.copy(name = editName.trim()))
                    speciesToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { speciesToEdit = null }) { Text("Cancel") }
            }
        )
    }
}
