package com.funjim.fishstory.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.viewmodels.WaterClarityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageWaterClarityScreen(
    viewModel: WaterClarityViewModel,
    navigateBack: () -> Unit
) {
    val allItems by viewModel.allWaterClarity.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<WaterClarity?>(null) }
    var itemToEdit by remember { mutableStateOf<WaterClarity?>(null) }
    var editName by remember { mutableStateOf("") }

    var currentItemForPhoto by remember { mutableStateOf<WaterClarity?>(null) }

    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val item = currentItemForPhoto
        if (uri != null && item != null) {
            viewModel.updateWaterClarityThumbnail(item.id, uri)
        }
        currentItemForPhoto = null
    }

    val filteredItems = remember(searchQuery, allItems) {
        allItems.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.name }
    }

    val showAddButton = searchQuery.isNotBlank() &&
                allItems.none { it.name.equals(searchQuery.trim(), ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Water Clarity") },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search or add new water clarity ...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (showAddButton) {
                        IconButton(onClick = {
                            viewModel.addWaterClarity(WaterClarity(name = searchQuery.trim()))
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
                    val thumbnail by viewModel.waterClarityThumbnail(item.id).collectAsStateWithLifecycle(initialValue = null)

                    var menuExpanded by remember { mutableStateOf(false) }
                    var thumbnailMenuExpanded by remember { mutableStateOf(false) }

                    // TODO - support fish caught/kept with water clarity
                    //val preventDelete = item.fishCaught > 0 || item.fishKept > 0
                    val preventDelete = false

                    val backgroundColor = getCardColor(index, filteredSize)
                    val borderColor = getCardBorderColor(index, filteredSize)
                    val contentColor = getOnCardColor()

                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = borderColor,
                                shape = MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium)
                            .combinedClickable(
                                onClick = { /* do nothing */ },
                                onLongClick = { menuExpanded = true }
                            ),
                        leadingContent = {
                            // Wrapped in a Box with combinedClickable for long-press registration
                            Box(
                                modifier = Modifier.combinedClickable(
                                    onClick = { /* Do nothing */ },
                                    onLongClick = {
                                        currentItemForPhoto = item
                                        thumbnailMenuExpanded = true
                                    }
                                )
                            ) {
                                ThumbnailBox(
                                    thumbnail = thumbnail,
                                    imageVector = AppIcons.Default.Water,
                                    modifier = Modifier.size(48.dp)
                                )
                                DropdownMenu(
                                    expanded = thumbnailMenuExpanded,
                                    onDismissRequest = { thumbnailMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            if (thumbnail != null) Text("Update Thumbnail")
                                            else Text("Select Thumbnail")
                                        },
                                        onClick = {
                                            currentItemForPhoto = item
                                            thumbnailMenuExpanded = false
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) }
                                    )
                                    if (thumbnail != null) {
                                        DropdownMenuItem(
                                            text = { Text("Reset Thumbnail") },
                                            onClick = {
                                                thumbnailMenuExpanded = false
                                                // Save out a null reference to clear
                                                viewModel.deleteWaterClarityThumbnail(item.id)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.HideImage,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        headlineContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(item.name)
                                /*
                                if (item.fishCaught > 0) {
                                    FishCaughtItem(
                                        icon = AppIcons.Default.LeapingFishWithFins,
                                        caughtCount = item.fishCaught,
                                        keptCount = item.fishKept,
                                        onFishClick = null,
                                        contentColor = getOnCardSecondaryColor()
                                    )
                                }
                                if (item.targetFishCaught > 0) {
                                    FishCaughtItem(
                                        icon = AppIcons.Default.TargetFish,
                                        caughtCount = item.targetFishCaught,
                                        keptCount = item.targetFishKept,
                                        onFishClick = null,
                                        contentColor = getOnCardSecondaryColor()
                                    )
                                }
                                */
                            }
                        },
                        supportingContent = {
                        },
                        trailingContent = {
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options"
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = {
                                            menuExpanded = false
                                            itemToEdit = item
                                            editName = item.name
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            if (thumbnail != null) Text("Update Thumbnail")
                                            else Text("Select Thumbnail")
                                        },
                                        onClick = {
                                            currentItemForPhoto = item
                                            menuExpanded = false
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Default.AddAPhoto, contentDescription = null) }
                                    )
                                    if (thumbnail != null) {
                                        DropdownMenuItem(
                                            text = {
                                                Text("Reset Thumbnail")
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                // Save out a null reference to clear
                                                viewModel.deleteWaterClarityThumbnail(item.id)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.HideImage,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            menuExpanded = false
                                            if (preventDelete) {
                                                Toast.makeText(
                                                    context,
                                                    "Can't delete this body of water. There are fish logged for it.",
                                                    Toast.LENGTH_SHORT).show()
                                            } else {
                                                itemToDelete = item
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint =
                                                    if (!preventDelete) MaterialTheme.colorScheme.error
                                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = backgroundColor)
                    )
                }
            }
        }
    }

    // DELETE CONFIRMATION
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Water Clarity?") },
            text = { Text("""Are you sure you want to delete '${item.name}'?

This cannot be undone."""
            ) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWaterClarity(item)
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

    // EDIT DIALOG
    itemToEdit?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Rename Water Clarity") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    label = { Text("Water Clarity") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.upsertWaterClarity(item.copy(name = editName.trim()))
                    itemToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) { Text("Cancel") }
            }
        )
    }
}