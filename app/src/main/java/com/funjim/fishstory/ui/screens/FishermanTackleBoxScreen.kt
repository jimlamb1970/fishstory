package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funjim.fishstory.model.LureWithColorsSummary
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.utils.EditTackleBoxDialog
import com.funjim.fishstory.ui.utils.LureColorComposition
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
import com.funjim.fishstory.viewmodels.FishSortOrder
import com.funjim.fishstory.viewmodels.LureSortOrder
import com.funjim.fishstory.viewmodels.LureViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanTackleBoxScreen(
    viewModel: LureViewModel,
    fishermanId: String,
    tackleBoxId: String,
    onAdd: () -> Unit,
    navigateBack: () -> Unit
) {
    LaunchedEffect(fishermanId) {
        viewModel.selectFisherman(fishermanId)
        viewModel.selectTackleBox(tackleBoxId)
    }

    val allLures by viewModel.luresWithDisplay.collectAsState(initial = emptyList())
    val fisherman by viewModel.selectedFisherman.collectAsStateWithLifecycle()
    val luresInBox by viewModel.tackleBoxWithLures.collectAsState(initial = emptyList())
    val tackleBox by viewModel.selectedTackleBox.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    var showRenameDialog by remember { mutableStateOf(false) }

    // Build a set of IDs in the tackle box for 'inBox'' lookup
    val luresInBoxIds = remember(luresInBox) { luresInBox.map { it.lure.id }.toSet() }

    val inBoxCount = luresInBoxIds.size

    val currentOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val reversed by viewModel.isReversed.collectAsStateWithLifecycle()

    val sortedLures = remember(allLures, currentOrder, luresInBoxIds) {
        sortLures(
            lureList = allLures,
            order = currentOrder,
            luresInBoxIds = luresInBoxIds
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    SortChip("Name",
                        currentOrder == LureSortOrder.NAME) {
                        viewModel.setSortOrder(LureSortOrder.NAME)
                    }
                    SortChip("Primary Color",
                        currentOrder == LureSortOrder.PRIMARY_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.PRIMARY_COLOR)
                    }
                    SortChip("Secondary Color",
                        currentOrder == LureSortOrder.SECONDARY_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.SECONDARY_COLOR)
                    }
                    SortChip("Glow Color",
                        currentOrder == LureSortOrder.GLOW_COLOR) {
                        viewModel.setSortOrder(LureSortOrder.GLOW_COLOR)
                    }
                    SortChip("Glows",
                        currentOrder == LureSortOrder.GLOW) {
                        viewModel.setSortOrder(LureSortOrder.GLOW)
                    }
                    SortChip("Hooks",
                        currentOrder == LureSortOrder.HOOKS) {
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
                                thumbnailFlow = viewModel.lureThumbnail(item.lure.id),
                                index = index,
                                totalItems = totalItems,
                                inTackleBox = inBox,
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
    thumbnailFlow: Flow<ByteArray?>,
    index: Int = 0,
    totalItems: Int = 0,
    inTackleBox: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = inTackleBox,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(8.dp))

            ThumbnailBox(
                thumbnail = thumbnail,
                imageVector = AppIcons.Default.Lure,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.lure.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (inTackleBox) FontWeight.Medium else FontWeight.Normal
                )
                LureColorComposition(
                    primary = item.primaryColors,
                    secondary = item.secondaryColors,
                    glows = item.lure.glows,
                    glow = item.glowColors
                )
                Text(
                    text = "Number of hooks: ${item.lure.hookCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryContentColor
                )
            }
        }
    }
}
