package com.funjim.fishstory.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.ui.theme.themeMap
import com.funjim.fishstory.ui.selection.ThemeSelectionField
import com.funjim.fishstory.viewmodels.EventImportResolution
import com.funjim.fishstory.viewmodels.ImportConflict
import com.funjim.fishstory.viewmodels.ImportViewModel
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    importViewModel: ImportViewModel,
    onThemeChange: (String) -> Unit,
    navigateToManageColors: () -> Unit,
    navigateToManageSkyConditions: () -> Unit,
    navigateToManageWaterClarity: () -> Unit,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }

    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    val conflict = importViewModel.currentConflict

    LaunchedEffect(Unit) {
        importViewModel.snackBarMessage.collect { message ->
            hostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        importViewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val backupFileLauncher: ActivityResultLauncher<String> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                importViewModel.exportFullDatabase(context, uri).let { }
            }
        }
    }
    val restoreFileLauncher: ActivityResultLauncher<Array<String>> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                importViewModel.importFullDatabase(context, uri).let { }
            }
        }
    }

    val pickEventFileLauncher: ActivityResultLauncher<Array<String>> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            importViewModel.importEvent(context, uri)
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
                .padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    backupFileLauncher.launch("fishstory-export-v1.json")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Backup Database")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    restoreFileLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Database")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    pickEventFileLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Event")
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
                onClick = { navigateToManageSkyConditions() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Sky Conditions")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navigateToManageWaterClarity() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Water Clarity")
            }

            Spacer(modifier = Modifier.height(16.dp))
            ThemeSelectionField(
                items = themeMap,
                selectedItem = appSettings.display.colorTheme,
                label = "Theme",
                modifier = Modifier.fillMaxWidth(),
                onSelected = { theme ->
                    onThemeChange(theme)
                    viewModel.updateTheme(theme)
                },
                onClear = {
                    onThemeChange("")
                    viewModel.updateTheme("App Default")
                }
            )
        }
    }

    when (conflict) {
        is ImportConflict.ExistingEvent -> {
            var selectedOption by remember { mutableIntStateOf(2) }

            val options = listOf(
                "Do not import this event",
                "Merge with the existing exist",
                "Replace the existing event"
            )

            AlertDialog(
                onDismissRequest = {
                    conflict.onResolve(EventImportResolution.SKIP)
                },
                title = {
                    Text(
                        text = "Event Already Exists",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = """The event '${conflict.eventName}' already exists.
                            |
                            |How would you like to proceed with the import?""".trimMargin(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        options.forEachIndexed { index, optionText ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOption = index }
                            ) {
                                RadioButton(
                                    selected = (selectedOption == index),
                                    onClick = { selectedOption = index }
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            when (selectedOption) {
                                0 -> {
                                    conflict.onResolve(EventImportResolution.SKIP)
                                }
                                1 -> {
                                    conflict.onResolve(EventImportResolution.MERGE)
                                }
                                2 -> {
                                    conflict.onResolve(EventImportResolution.REPLACE)
                                }
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { conflict.onResolve(EventImportResolution.SKIP) }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        else -> { /* No-op or handle other conflicts */ }
    }
}
