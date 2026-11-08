package com.funjim.fishstory.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.viewmodels.LureViewModel
import androidx.core.graphics.toColorInt
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.BadgeCount
import com.funjim.fishstory.ui.crud.NewColorDialog
import com.funjim.fishstory.ui.crud.EditColorDialog
import com.funjim.fishstory.ui.utils.AdvancedColorPickerDialog
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageColorsScreen(
    viewModel: LureViewModel,
    navigateBack: () -> Unit
) {
    val context = LocalContext.current

    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val colorsList by viewModel.lureColors.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var colorToDelete by remember { mutableStateOf<LureColor?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var colorToEdit by remember { mutableStateOf<LureColor?>(null) }

    var showPickColorDialog by remember { mutableStateOf(false) }
    var colorToPickFor by remember { mutableStateOf<LureColor?>(null) }
    var pickMaxHexCodes by remember { mutableIntStateOf(1) } // Supports 1 or 4 hex values

    var showPendingNewColorName by remember { mutableStateOf(false) }
    var pendingNewColorName by remember { mutableStateOf<String?>(null) }

    val filteredColors = remember(searchQuery, colorsList) {
        colorsList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }.sortedBy { it.name }
    }

    val showAddButton = searchQuery.isNotBlank() &&
            colorsList.none { it.name.equals(searchQuery.trim(), ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Colors")
                        val total = colorsList.size
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
                            Icon(
                                AppIcons.Default.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp))

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp) // Adjust offset to position on the edge
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Color",
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
                placeholder = { Text("Search or add new color ...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (showAddButton) {
                        IconButton(onClick = {
                            pendingNewColorName = searchQuery.trim()
                            showPendingNewColorName = true
                        }) {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Add Color",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            val listState = rememberLazyListState()

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val filteredSize = filteredColors.size
                    itemsIndexed(filteredColors, key = { _, s -> s.id }) { index, item ->
                        var menuExpanded by remember { mutableStateOf(false) }
                        var thumbnailMenuExpanded by remember { mutableStateOf(false) }

                        val backgroundColor = getCardColor(index, filteredSize)
                        val borderColor = getCardBorderColor(index, filteredSize)

                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = borderColor,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clip(MaterialTheme.shapes.medium)
                                .combinedClickable(
                                    onClick = { /* do nothing */ },
                                    onLongClick = { menuExpanded = true }
                                ),
                            leadingContent = {
                                Box(
                                    modifier = Modifier.combinedClickable(
                                        onClick = { /* do nothing */ },
                                        onLongClick = {
                                            thumbnailMenuExpanded = true
                                        }
                                    )
                                ) {
                                    if (item.hexCode.isBlank()) {
                                        ThumbnailBox(
                                            thumbnail = null,
                                            imageVector = AppIcons.Default.Palette,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {
                                        val hexList = remember(item.hexCode) {
                                            item.hexCode.split(",").filter { it.isNotBlank() }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.onSurface,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Dynamic layout grid depending on hex count
                                            MultiColorCirclePreview(hexList = hexList)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = thumbnailMenuExpanded,
                                        onDismissRequest = { thumbnailMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Select Single Color") },
                                            onClick = {
                                                thumbnailMenuExpanded = false
                                                pickMaxHexCodes = 1
                                                colorToPickFor = item
                                                showPickColorDialog = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Select Multiple Colors (Up to 4)") },
                                            onClick = {
                                                thumbnailMenuExpanded = false
                                                pickMaxHexCodes = 4
                                                colorToPickFor = item
                                                showPickColorDialog = true
                                            }
                                        )
                                    }
                                }
                            },
                            headlineContent = { Text(item.name) },
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
                                            text = { Text("Rename") },
                                            onClick = {
                                                menuExpanded = false
                                                colorToEdit = item
                                                showEditDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Rename"
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Select Single Color") },
                                            onClick = {
                                                menuExpanded = false
                                                pickMaxHexCodes = 1
                                                colorToPickFor = item
                                                showPickColorDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    AppIcons.Default.Palette,
                                                    contentDescription = "Select Color",
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Select Multiple Colors (Up to 4)") },
                                            onClick = {
                                                menuExpanded = false
                                                pickMaxHexCodes = 4
                                                colorToPickFor = item
                                                showPickColorDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    AppIcons.Default.Palette,
                                                    contentDescription = "Select Colors",
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                menuExpanded = false
                                                colorToDelete = item
                                                showDeleteDialog = true
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Color",
                                                    tint = MaterialTheme.colorScheme.error
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
                VerticalScrollToItemBar(
                    state = listState,
                    imageVector = AppIcons.Default.Palette,
                    onToggleAlignment = {
                        viewModel.updateScrollbarOnLeft()
                    },
                    modifier = Modifier
                        .align(if (appSettings.display.scrollbarOnLeft) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp, horizontal = 0.dp)
                )
            }
        }
    }

    // DELETE CONFIRMATION
    if (showDeleteDialog) {
        colorToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = !showDeleteDialog },
                title = { Text("Delete Color?") },
                text = {
                    Text(
                        """Are you sure you want to delete '${item.name}'?

This cannot be undone.

Lures that were using this color may not have a color assigned to them.
"""
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteLureColor(item)
                            showDeleteDialog = !showDeleteDialog
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = !showDeleteDialog }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showAddDialog) {
        NewColorDialog(
            onConfirm = {
                pendingNewColorName = it
                showAddDialog = !showAddDialog
                showPendingNewColorName = true
            },
            onDismiss = {
                pendingNewColorName = ""
                showAddDialog = !showAddDialog
            }
        )
    }

    // EDIT DIALOG
    if (showEditDialog) {
        colorToEdit?.let { item ->
            EditColorDialog(
                item = item,
                onDismiss = { showEditDialog = !showEditDialog },
                onConfirm = {
                    viewModel.updateLureColor(it) {
                        showEditDialog = !showEditDialog
                    }
                }
            )
        }
    }

    if (showPickColorDialog) {
        colorToPickFor?.let { item ->
            AdvancedColorPickerDialog(
                initialColor = item.hexCode,
                maxAllowedColors = pickMaxHexCodes, // Tell picker if it allows multi-select
                onDismiss = {
                    showPickColorDialog = !showPickColorDialog
                },
                onConfirm = { finalizedCommaSeparatedString ->
                    viewModel.updateLureColor(item.copy(hexCode = finalizedCommaSeparatedString)) {
                        showPickColorDialog = !showPickColorDialog
                    }
                }
            )
        }
    }

    if (showPendingNewColorName) {
        pendingNewColorName?.let { name ->
            AdvancedColorPickerDialog(
                initialColor = null,
                maxAllowedColors = 1,
                onDismiss = {
                    showPendingNewColorName = !showPendingNewColorName
                },
                onConfirm = { hexCode ->
                    viewModel.addLureColor(LureColor(name = name, hexCode = hexCode)) {
                        showPendingNewColorName = !showPendingNewColorName
                        searchQuery = ""
                    }
                }
            )
        }
    }
}

@Composable
fun MultiColorCirclePreview(hexList: List<String>) {
    val colors = remember(hexList) {
        hexList.map { hex ->
            try {
                Color(hex.toColorInt())
            } catch (_: Exception) {
                Color.Transparent
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (colors.size) {
            1 -> {
                Box(modifier = Modifier.fillMaxSize().background(colors[0]))
            }
            2 -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(colors[0]))
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(colors[1]))
            }
            3 -> {
                // Split top row into half, bottom row solid or mixed
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[0]))
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[1]))
                }
                Box(modifier = Modifier.fillMaxWidth().weight(1f).background(colors[2]))
            }
            else -> { // 4 colors (Quadrants)
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[0]))
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[1]))
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[2]))
                    Box(modifier = Modifier.fillMaxHeight().weight(1f).background(colors[3]))
                }
            }
        }
    }
}