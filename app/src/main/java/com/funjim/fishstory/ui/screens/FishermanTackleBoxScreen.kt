package com.funjim.fishstory.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.LureWithColorsSummary
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.CardItemWithValue
import com.funjim.fishstory.ui.utils.EditTackleBoxDialog
import com.funjim.fishstory.ui.utils.LureColorComposition
import com.funjim.fishstory.ui.utils.PhotoPickerRow
import com.funjim.fishstory.ui.utils.SortChip
import com.funjim.fishstory.ui.utils.ThumbnailBox
import com.funjim.fishstory.ui.utils.VerticalScrollToItemBar
import com.funjim.fishstory.ui.utils.getCardBorderColor
import com.funjim.fishstory.ui.utils.getCardColor
import com.funjim.fishstory.ui.utils.getChipColor
import com.funjim.fishstory.ui.utils.getOnCardColor
import com.funjim.fishstory.ui.utils.getOnCardSecondaryColor
import com.funjim.fishstory.ui.utils.getOnChipColor
import com.funjim.fishstory.ui.utils.sortLures
import com.funjim.fishstory.viewmodels.LureSortOrder
import com.funjim.fishstory.viewmodels.LureUiState
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class LureFilterOption(val label: String) {
    ALL("All"),
    SELECTED("In Tackle Box"),
    UNSELECTED("Not In Tackle Box")
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanTackleBoxScreen(
    viewModel: LureViewModel,
    fishermanId: String,
    tackleBoxId: String,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(fishermanId) {
        viewModel.selectFisherman(fishermanId)
        viewModel.selectTackleBox(tackleBoxId)
    }

    val displaySettings by viewModel.displaySettings.collectAsStateWithLifecycle()

    val allLures by viewModel.luresWithDisplay.collectAsState(initial = emptyList())
    val fisherman by viewModel.selectedFisherman.collectAsStateWithLifecycle()
    val tackleBox by viewModel.selectedTackleBox.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    var showRenameDialog by remember { mutableStateOf(false) }

    val tackleBoxState by viewModel.tackleBoxUiState.collectAsStateWithLifecycle()

    val luresInBox = (tackleBoxState as? LureUiState.Success)?.lures ?: emptyList()
    val luresInBoxIds = remember(luresInBox) { luresInBox.map { it.lure.id }.toSet() }
    val inBoxCount = luresInBoxIds.size

    var currentFilter by remember { mutableStateOf(LureFilterOption.SELECTED) }

    LaunchedEffect(tackleBoxState) {
        if (tackleBoxState is LureUiState.Success && (tackleBoxState as LureUiState.Success).lures.isEmpty()) {
            currentFilter = LureFilterOption.ALL
        }
    }

    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    val sortedLures = remember(allLures, currentOrder, luresInBoxIds, currentFilter) {
        val filteredList = when (currentFilter) {
            LureFilterOption.ALL -> allLures
            LureFilterOption.SELECTED -> allLures.filter { it.lure.id in luresInBoxIds }
            LureFilterOption.UNSELECTED -> allLures.filter { it.lure.id !in luresInBoxIds }
        }

        sortLures(
            lureList = filteredList,
            order = currentOrder
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tackleBox?.name ?: "Tackle Box",
                            maxLines = 1
                        )
                        Text(
                            text = fisherman?.fullName ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showRenameDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename Tackle Box")
                    }

                    IconButton(onClick = onAdd) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                AppIcons.Default.Lure,
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
            // Summary chip
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 0.dp, start = 16.dp, end = 16.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
            ) {
                Text(
                    text = "$inBoxCount lure${if (inBoxCount != 1) "s" else ""} in tackle box",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 0.dp, start = 16.dp, end = 16.dp),
                ) {
                    LureFilterOption.entries.forEach { option ->
                        SortChip(
                            label = option.label,
                            selected = (currentFilter == option),
                            onClick = { currentFilter = option },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        SortChip(
                            "Name",
                            currentOrder == LureSortOrder.NAME
                        ) {
                            viewModel.setSortOrder(LureSortOrder.NAME)
                        }
                        SortChip(
                            "Primary Color",
                            currentOrder == LureSortOrder.PRIMARY_COLOR
                        ) {
                            viewModel.setSortOrder(LureSortOrder.PRIMARY_COLOR)
                        }
                        SortChip(
                            "Secondary Color",
                            currentOrder == LureSortOrder.SECONDARY_COLOR
                        ) {
                            viewModel.setSortOrder(LureSortOrder.SECONDARY_COLOR)
                        }
                        SortChip(
                            "Glow Color",
                            currentOrder == LureSortOrder.GLOW_COLOR
                        ) {
                            viewModel.setSortOrder(LureSortOrder.GLOW_COLOR)
                        }
                        SortChip(
                            "Glows",
                            currentOrder == LureSortOrder.GLOW
                        ) {
                            viewModel.setSortOrder(LureSortOrder.GLOW)
                        }
                        SortChip(
                            "Hooks",
                            currentOrder == LureSortOrder.HOOKS
                        ) {
                            viewModel.setSortOrder(LureSortOrder.HOOKS)
                        }
                    }

                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = { viewModel.toggleReverse() },
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = getChipColor(),
                                shape = RoundedCornerShape(8.dp)
                            ).size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (reversed) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = "Reverse Sort",
                            tint = getOnChipColor(),
                        )
                    }
                }
            }

            if (allLures.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No lures in inventory.")
                }
            } else {
                val listState = rememberLazyListState()

                Box(modifier = Modifier
                    .fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val totalItems = sortedLures.size
                        itemsIndexed(
                            sortedLures,
                            key = { _, item -> item.lure.id }) { index, item ->
                            val inBox = item.lure.id in luresInBoxIds
                            LureTackleBoxItem(
                                item = item,
                                verbose = displaySettings.verboseCards,
                                thumbnailFlow = viewModel.lureThumbnail(item.lure.id),
                                photosFlow = viewModel.lurePhotos(item.lure.id),
                                index = index,
                                totalItems = totalItems,
                                inTackleBox = inBox,
                                onEdit = { onEdit(item.lure.id) },
                                onPhotoAdded = { uri ->
                                    viewModel.addLurePhoto(
                                        lureId = item.lure.id,
                                        uri = uri,
                                        selected = true
                                    )
                                },
                                onPhotoTaken = { uri ->
                                    viewModel.addLurePhoto(
                                        lureId = item.lure.id,
                                        uri = uri,
                                        selected = false
                                    )
                                },
                                onSetThumbnail = { photo ->
                                    viewModel.setLureThumbnail(
                                        lureId = item.lure.id,
                                        photoId = photo.id)
                                },
                                onPhotoDeleted = { photo ->
                                    viewModel.deleteLurePhoto(item.lure.id, photo)
                                },
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        if (checked) {
                                            viewModel.addLureToTackleBox(
                                                tackleBoxId,
                                                item.lure.id
                                            )
                                        } else {
                                            viewModel.removeLureFromTackleBox(
                                                tackleBoxId,
                                                item.lure.id
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    var isLeftAligned by remember { mutableStateOf(false) }

                    VerticalScrollToItemBar(
                        state = listState,
                        imageVector = AppIcons.Default.Lure,
                        onToggleAlignment = { isLeftAligned = !isLeftAligned },
                        modifier = Modifier
                            .align(if (isLeftAligned) Alignment.CenterStart else Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp, horizontal = 0.dp)
                    )
                }
            }
        }
    }

    if (showRenameDialog && (tackleBox != null)) {
        EditTackleBoxDialog(
            item = tackleBox!!,
            onConfirm = {
                showRenameDialog = false
                viewModel.updateTackleBox(it) },
            onDismiss = { showRenameDialog = false }
        )
    }
}

@Composable
private fun LureTackleBoxItem(
    item: LureWithColorsSummary,
    verbose: Boolean,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    index: Int = 0,
    totalItems: Int = 0,
    inTackleBox: Boolean,
    onEdit: () -> Unit,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onSetThumbnail: (Photo) -> Unit,
    onPhotoDeleted: (Photo) -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())

    var showPhotos by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val backgroundColor = getCardColor(index, totalItems, selected = inTackleBox)

    val borderColor = getCardBorderColor(index, totalItems)
    val borderWidth = if (inTackleBox) 3.dp else 1.dp

    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize()
            .combinedClickable(
                onClick = { onCheckedChange(!inTackleBox) },
                onLongClick = { menuExpanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(borderWidth, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.Lure,
                    modifier = Modifier.size(48.dp),
                    onClick = { showPhotos = !showPhotos }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.lure.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (inTackleBox) FontWeight.Bold else FontWeight.Normal
                    )
                    LureColorComposition(
                        primary = item.primaryColors,
                        secondary = item.secondaryColors,
                        glows = item.lure.glows,
                        glow = item.glowColors
                    )
                    if (item.lure.hookCount > 0) {
                        CardItemWithValue(
                            icon =
                                if (item.lure.hookCount == 1) AppIcons.Default.Hook
                                else AppIcons.Default.Hooks,
                            value = item.lure.hookCount.toString(),
                            description =
                                if (verbose) {
                                    if (item.lure.hookCount == 1) "Hook"
                                    else "Hooks"
                                } else "",
                            contentColor = secondaryContentColor
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Lure options",
                            tint = contentColor
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
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                if (showPhotos) Text("Hide Photos")
                                else Text("Show Photos")
                            },
                            onClick = {
                                menuExpanded = false
                                showPhotos = !showPhotos
                            },
                            leadingIcon = {
                                Icon(
                                    if (showPhotos) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showPhotos) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = borderColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    PhotoPickerRow(
                        photos = photos,
                        onPhotoSelected = { uri -> onPhotoAdded(uri) },
                        onPhotoTaken = { uri -> onPhotoTaken(uri) },
                        onSetThumbnail = { photo -> onSetThumbnail(photo) },
                        onPhotoDeleted = { photo -> onPhotoDeleted(photo) }
                    )
                }
            }
        }
    }
}
