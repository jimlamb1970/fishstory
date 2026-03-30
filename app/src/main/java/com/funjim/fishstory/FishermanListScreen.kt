package com.funjim.fishstory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.ui.FishermanItem
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanListScreen(
    viewModel: MainViewModel,
    navigateToFishermanDetails: (String) -> Unit,
    navigateBack: () -> Unit
) {
    val fishermenList by viewModel.fishermen.collectAsState(initial = emptyList())
    val fishermen = remember(fishermenList) {
        fishermenList.sortedBy { it.fullName.lowercase() }
    }
    var showAddDialog by remember { mutableStateOf(false) }
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newNickname by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fishermen") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Fisherman")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn {
                items(fishermen) { fisherman ->
                    FishermanItem(
                        fisherman = fisherman,
                        onDelete = {
                            scope.launch {
                                viewModel.deleteFisherman(fisherman)
                            }
                        },
                        onClick = {
                            navigateToFishermanDetails(fisherman.id)
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Fisherman") },
                text = {
                    Column {
                        TextField(
                            value = newFirstName,
                            onValueChange = { newFirstName = it },
                            label = { Text("First Name") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newNickname,
                            onValueChange = { newNickname = it },
                            label = { Text("Nickname") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newLastName,
                            onValueChange = { newLastName = it },
                            label = { Text("Last Name") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newFirstName.isNotBlank() && newLastName.isNotBlank()) {
                            scope.launch {
                                viewModel.addFisherman(
                                    Fisherman(
                                        firstName = newFirstName,
                                        lastName = newLastName,
                                        nickname = newNickname
                                    )
                                )
                                newFirstName = ""
                                newLastName = ""
                                newNickname = ""
                                showAddDialog = false
                            }
                        }
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
