package com.funjim.fishstory.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.funjim.fishstory.viewmodels.ImportViewModel
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    importViewModel: ImportViewModel,
    navigateToManageColors: () -> Unit,
    navigateToManageSpecies: () -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }

    // Launcher for saving a file (Export to JSON)
    val saveFileLauncher: ActivityResultLauncher<String> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                // TODO: Implement JSON serialization logic inside the ViewModel
                viewModel.exportDatabaseAsJson(context, uri).let { success ->
                    val message = if (success) "Database exported as JSON successfully!" else "Database export failed."
                    hostState.showSnackbar(message)
                }
            }
        }
    }

    // Launcher for picking a file (Import JSON)
    val pickFileLauncher: ActivityResultLauncher<Array<String>> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                // TODO: Implement JSON deserialization and Room import logic inside the ViewModel
                viewModel.importDatabaseFromJson(context, uri).let { success ->
                    val message = if (success) "Database imported successfully! Restart app to see changes." else "Database import failed."
                    hostState.showSnackbar(message)
                }
            }
        }
    }

    // 1. Define the launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        // 2. Handle the result
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            inputStream?.let { stream -> importViewModel.startImport(stream) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = hostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    // Launch the file saver dialog, suggesting a JSON file name
                    saveFileLauncher.launch("fishstory_export.json")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Database as JSON")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Trigger the file picker to select a JSON database file for import
                    pickFileLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Database from JSON")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Trigger the file picker to select a JSON database file for import
                    launcher.launch(arrayOf("text/csv"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Fish from CSV")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navigateToManageColors() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Colors")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navigateToManageSpecies() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Species")
            }
        }
    }
}