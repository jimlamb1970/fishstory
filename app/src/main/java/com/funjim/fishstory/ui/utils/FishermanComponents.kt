package com.funjim.fishstory.ui.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as listItemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.theme.AppIcons
import com.funjim.fishstory.ui.theme.FishstoryTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun AddFishermanDialog(
    onDismiss: () -> Unit,
    onAdd: (firstName: String, lastName: String, nickname: String) -> Unit
) {
    EditFishermanDialog(
        fisherman = Fisherman(firstName = "", lastName = "", nickname = ""),
        title = "Add",
        onDismiss = onDismiss,
        onConfirm = { fisherman ->
            onAdd(fisherman.firstName, fisherman.lastName, fisherman.nickname)
        }
    )
}

@Composable
fun EditFishermanDialog(
    fisherman: Fisherman,
    title: String = "Rename",
    onDismiss: () -> Unit,
    onConfirm: (Fisherman) -> Unit
) {
    val origFirstName = remember(fisherman) { fisherman.firstName }
    val origLastName = remember(fisherman) { fisherman.lastName }
    val origNickName = remember(fisherman) { fisherman.nickname }

    var firstName by remember { mutableStateOf(origFirstName) }
    var lastName by remember { mutableStateOf(origLastName) }
    var nickname by remember { mutableStateOf(origNickName) }

    val isChanged = firstName != origFirstName ||
            lastName != origLastName ||
            nickname != origNickName

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Fisherman") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = firstName, onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = lastName, onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = nickname, onValueChange = { nickname = it },
                    label = { Text("Nickname (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fisherman.copy(
                    firstName = firstName,
                    lastName = lastName,
                    nickname = nickname))
                },
                enabled = (firstName.isNotBlank() || lastName.isNotBlank()) && isChanged
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FishermanItem(
    fisherman: FishermanSummary,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    photosFlow: Flow<List<Photo>>,
    onClick: () -> Unit,
    onFishClick: (String, Boolean) -> Unit,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoTaken: (Uri) -> Unit,
    onSetThumbnail: (Photo) -> Unit,
    onPhotoDeleted: (Photo) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val thumbnail by thumbnailFlow.collectAsState(initial = null)
    val photos by photosFlow.collectAsState(initial = emptyList())

    var isExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // This can happen if the provider doesn't support persistable permissions
                }
                onPhotoAdded(it)
            }
        }
    )

    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempUri?.let { onPhotoTaken(it) }
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createPublicImageUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { expanded = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThumbnailBox(
                    thumbnail = thumbnail,
                    imageVector = AppIcons.Default.Fisherman,
                    modifier = Modifier.size(64.dp),
                    onClick = { isExpanded = !isExpanded }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        fisherman.fisherman.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (fisherman.totalTrips != 0 || fisherman.totalTackleBoxes != 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (fisherman.totalTrips != 0) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Boat,
                                    value = fisherman.totalTrips.toString(),
                                    contentColor = secondaryContentColor
                                )
                            }
                            if (fisherman.totalTackleBoxes != 0) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.TackleBox,
                                    value = fisherman.totalTackleBoxes.toString(),
                                    contentColor = secondaryContentColor
                                )
                            }
                        }
                    }

                    if (fisherman.fishCaught != 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FishCaughtItem(
                                icon = AppIcons.Default.LeapingFishWithFins,
                                caughtCount = fisherman.fishCaught,
                                keptCount = fisherman.fishKept,
                                onClick = { onFishClick(fisherman.fisherman.id, false) },
                                contentColor = secondaryContentColor,
                            )
                        }
                    }

                    if (fisherman.targetFishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FishCaughtItem(
                                icon = AppIcons.Default.TargetFish,
                                caughtCount = fisherman.targetFishCaught,
                                keptCount = fisherman.targetFishKept,
                                onClick = { onFishClick(fisherman.fisherman.id, true) },
                                contentColor = secondaryContentColor
                            )
                        }
                    }

                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Fisherman options",
                            tint = contentColor
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Photo") },
                            onClick = {
                                expanded = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Add Photo From Gallery"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Take Photo") },
                            onClick = {
                                expanded = false
                                val permissionCheckResult = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                )
                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                    val uri = createPublicImageUri(context)
                                    tempUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Take Photo"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            if (isExpanded) {
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

@Composable
fun FishermanSummaryCard(
    item: FishermanSummary,
    modifier: Modifier = Modifier,
    index: Int = 0,
    totalItems: Int = 0,
    thumbnailFlow: Flow<ByteArray?>,
    selectedTackleBoxId: String?,
    availableBoxes: List<TackleBox>,
    lureCount: Int,
    lures: List<LureWithColors>,
    onAddTackleBox: (TackleBox) -> Unit,
    onAddLuresToTackleBox: (TackleBox) -> Unit,
    onTackleBoxSelected: (Fisherman, String?) -> Unit,
    onClick: (Fisherman) -> Unit,
    onFishClick: (Fisherman, Boolean) -> Unit,
    onDelete: (Fisherman) -> Unit
) {
    val thumbnail by thumbnailFlow.collectAsState(initial = null)

    val backgroundColor = getCardColor(index, totalItems)
    val borderColor = getCardBorderColor(index, totalItems)
    val contentColor = getOnCardColor()
    val secondaryContentColor = getOnCardSecondaryColor()

    val selectedTackleBox = availableBoxes.find { it.id == selectedTackleBoxId }
    var tackleBoxOpen by remember { mutableStateOf(false) }
    var showAddTackleBoxDialog by remember { mutableStateOf(false) }
    var showTackleBoxSelection by remember { mutableStateOf(false) }
    var luresExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val sortedLures by remember(lures) { derivedStateOf { sortLures(lures) } }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                onClick = { onClick(item.fisherman) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThumbnailBox(
                    thumbnail = thumbnail,
                    modifier = Modifier.size(48.dp),
                    imageVector = AppIcons.Default.Fisherman
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.fisherman.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector =
                                if (tackleBoxOpen) AppIcons.Default.TackleBox
                                else AppIcons.Default.TackleBoxClosed,
                            contentDescription = "Tackle Box",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    enabled = (selectedTackleBox != null),
                                ) {
                                    tackleBoxOpen = !tackleBoxOpen
                                    luresExpanded = tackleBoxOpen
                                }
                        )

                        Text(
                            text = selectedTackleBox?.name ?: "No Tackle Box selected",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    if (selectedTackleBox != null) {
                        if (lureCount != 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CardItemWithValue(
                                    icon = AppIcons.Default.Lure,
                                    value = lureCount.toString(),
                                    onClick = {
                                        onAddLuresToTackleBox(selectedTackleBox)
                                    },
                                    contentColor = secondaryContentColor
                                )
                            }
                        }
                    }

                    if (item.fishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.LeapingFishWithFins,
                            caughtCount = item.fishCaught,
                            keptCount = item.fishKept,
                            onClick = {
                                onFishClick(item.fisherman, false)
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                    if (item.targetFishCaught != 0) {
                        Spacer(Modifier.height(4.dp))
                        FishCaughtItem(
                            icon = AppIcons.Default.TargetFish,
                            caughtCount = item.targetFishCaught,
                            keptCount = item.targetFishKept,
                            onClick = {
                                onFishClick(item.fisherman, true)
                            },
                            contentColor = secondaryContentColor
                        )
                    }
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Fisherman options",
                            tint = contentColor
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select Tackle Box") },
                            onClick = {
                                expanded = false
                                showTackleBoxSelection = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = AppIcons.Default.TackleBoxClosed,
                                    contentDescription = "Select Tackle Box",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )
                        if (selectedTackleBox != null) {
                            DropdownMenuItem(
                                text = { Text("Add Lures") },
                                onClick = {
                                    expanded = false
                                    onAddLuresToTackleBox(selectedTackleBox)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = AppIcons.Default.Lure,
                                        contentDescription = "Add Lures",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            )

                        }
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDelete(item.fisherman)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            if (luresExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    thickness = 1.dp,
                    color = getOnCardColor()
                )
            }

            AnimatedVisibility(visible = luresExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (sortedLures.isNotEmpty()) {
                        sortedLures.forEach { lure ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                LureCompositionWithColors(
                                    name = "• ${lure.lure.name}",
                                    lure.primaryColors,
                                    lure.secondaryColors,
                                    lure.lure.glows,
                                    lure.glowColors,
                                    style = MaterialTheme.typography.bodySmall,
                                    contentColor = getOnCardSecondaryColor(),
                                    modifier = Modifier.padding(start = 50.dp),
                                    colorBadgeSize = 20.dp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Tackle Box is empty",
                            style = MaterialTheme.typography.bodySmall,
                            color = getOnCardSecondaryColor(),
                            modifier = Modifier.padding(start = 50.dp)
                        )
                    }
                }
            }
        }
    }

    TackleBoxBottomSheet(
        showSheet = showTackleBoxSelection,
        items = availableBoxes,
        selectedItem = selectedTackleBox,
        onSelected = { tackleBox -> onTackleBoxSelected(item.fisherman, tackleBox.id) },
        onAdd = { showAddTackleBoxDialog = true },
        onClear = {
            tackleBoxOpen = false
            onTackleBoxSelected(item.fisherman, null)
        },
        onDismissRequest = { showTackleBoxSelection = false }
    )

    if (showAddTackleBoxDialog) {
        AddTackleBoxDialog(
            fishermanId = item.fisherman.id,
            onDismiss = { showAddTackleBoxDialog = false },
            onConfirm = { tackleBox ->
                onAddTackleBox(tackleBox)
                showAddTackleBoxDialog = false
            }
        )
    }
}

@Composable
fun FishermanSummaries(
    list: List<FishermanSummary>,
    thumbnailFlow: (Fisherman) -> Flow<ByteArray?> = { flowOf(null) },
    tackleBoxSelections: Map<String, String?>,
    getTackleBoxesForFisherman: @Composable (fishermanId: String) -> List<TackleBox>,
    getLureCount: @Composable (tackleBoxId: String?) -> Int,
    getLuresInTacklebox: @Composable (tackleBoxId: String?) -> List<LureWithColors>,
    onAdd: () -> Unit,
    onAddTackleBox: (TackleBox) -> Unit,
    onAddLuresToTackleBox: (TackleBox) -> Unit,
    onClick: (Fisherman) -> Unit,
    onFishClick: (Fisherman, Boolean) -> Unit,
    onTackleBoxSelected: (Fisherman, String?) -> Unit,
    onDelete: (Fisherman) -> Unit
) {
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fishermen",
                    style = MaterialTheme.typography.titleMedium,
                    color = getOnMainColor()
                )

                if (list.size > 1) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(${list.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = getOnMainColor()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onAdd,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Fisherman"
                )
            }
        }

        if (list.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                list.forEachIndexed { index, item ->
                    val selectedTackleBoxId = tackleBoxSelections[item.fisherman.id]
                    val availableBoxes = getTackleBoxesForFisherman(item.fisherman.id)
                    val lureCount = getLureCount(selectedTackleBoxId)
                    val lures = getLuresInTacklebox(selectedTackleBoxId).sortedBy { it.lure.name }

                    FishermanSummaryCard(
                        item = item,
                        index = index,
                        totalItems = list.size,
                        thumbnailFlow = thumbnailFlow(item.fisherman),
                        selectedTackleBoxId = selectedTackleBoxId,
                        availableBoxes = availableBoxes,
                        lureCount = lureCount,
                        lures = lures,
                        onAddTackleBox = onAddTackleBox,
                        onAddLuresToTackleBox = onAddLuresToTackleBox,
                        onTackleBoxSelected = onTackleBoxSelected,
                        onClick = onClick,
                        onFishClick= onFishClick,
                        onDelete = onDelete
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "No fisherman are set.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}



@Composable
fun FishermanSummary(
    fishermanCount: Int,
    tackleBoxCount: Int,
    modifier: Modifier = Modifier,
    allowOverride: Boolean = false,
    onClick: () -> Unit) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = getCardColor(),
            contentColor = getOnCardColor()
        ),
        border = BorderStroke(1.dp, color = getCardBorderColor())
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                AppIcons.Default.Fisherman,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "THE CREW AND TACKLE BOXES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center, // Centers the text within the middle space
                )
                if (fishermanCount == 0 && allowOverride) {
                    Text(
                        "Trip Crew is being used\nTap to override",
                        color = getOnCardSecondaryColor().copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        "$fishermanCount ${if (fishermanCount == 1) "fisherman" else "fishermen"} on board",
                        color = getOnCardSecondaryColor().copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        "$tackleBoxCount ${if (tackleBoxCount == 1) "tackle box" else "tackle boxes"} assigned",
                        color = getOnCardSecondaryColor().copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                AppIcons.Default.TackleBox,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanSelectionField(
    items: List<Fisherman>,
    modifier: Modifier = Modifier,
    defaultText : String = "Select Fisherman (optional)",
    selectedItem: Fisherman?,
    onSelected: (Fisherman) -> Unit,
    onClear: (() -> Unit)? = null,
    thumbnailProvider: @Composable (Fisherman) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    OutlinedTextField(
        value = selectedItem?.fullName ?: defaultText,
        onValueChange = {},
        readOnly = true,
        modifier = modifier.clickable { showSheet = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        label = { Text("Fisherman") },
        trailingIcon = { Icon(Icons.AutoMirrored.Filled.List, "Open Selector") }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Select Fisherman",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier
                .fillMaxHeight(0.8f)
                .padding(start = 16.dp, end = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search fishermen ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter {
                    it.fullName.contains(searchQuery, ignoreCase = true)
                }
                val filteredSize = filtered.size

                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        gridItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                headlineContent = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        thumbnailProvider(item)

                                        Text(
                                            text = item.fullName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (onClear != null && selectedItem != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider() }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ModalResetButton(
                                    title = "Reset Fisherman",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listItemsIndexed(filtered) { index, item ->
                            val isSelected = item == selectedItem

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color =
                                            if (isSelected) getOnCardColor()
                                            else Color.Transparent,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchQuery = ""
                                    },
                                leadingContent = { thumbnailProvider(item) },
                                headlineContent = {
                                    Text(
                                        item.fullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight =
                                            if (isSelected) FontWeight.Bold
                                            else FontWeight.Normal,
                                        color = getOnCardColor()
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize, isSelected),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        if (onClear != null && selectedItem != null) {
                            item { HorizontalDivider() }
                            item {
                                ModalResetButton(
                                    title = "Reset Fisherman",
                                    onClear = { showSheet = false; onClear() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishermanSelection(
    items: List<Fisherman>,
    selectedItems: List<Fisherman>,
    onSelected: (Fisherman) -> Unit,
    onUnselected: (Fisherman) -> Unit,
    onAdd: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    usageMap: Map<String, Int>? = null,
    maxUsage: Int? = null,
    thumbnailProvider: @Composable (Fisherman) -> Unit
) {
    var showSheet by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                onDone()
            },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Select Fisherman",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                TextButton(
                    onClick = {
                        showSheet = false
                        searchQuery = ""
                        onDone()
                    }
                ) {
                    Text("Done")
                }
            }

            Column(modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxHeight(0.8f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search fishermen ...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filtered = items.filter { it.fullName.contains(searchQuery, ignoreCase = true) }
                val filteredSize = filtered.size

                if (isGridView) {
                    // ── GRID VIEW ───────────────────────────────────────────
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        gridItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isChecked = selectedItems.contains(item)

                            val state =
                                if (isChecked) {
                                    val usage = usageMap?.get(item.id) ?: 0
                                    if (maxUsage != null && usage < maxUsage) {
                                        ToggleableState.Indeterminate
                                    }
                                    else ToggleableState.On
                                } else ToggleableState.Off

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        width = if (isChecked) 2.dp else 0.dp,
                                        color =
                                            when (state) {
                                                ToggleableState.On -> getOnCardColor()
                                                ToggleableState.Indeterminate -> getOnCardColor().copy(
                                                    alpha = 0.5f
                                                )

                                                else -> Color.Transparent
                                            },
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable(enabled = true) {
                                        if (state == ToggleableState.On) onUnselected(item)
                                        else onSelected(item)
                                    },
                                leadingContent = null,
                                headlineContent = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth() // Forces the column to span the whole grid cell width
                                            .padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        thumbnailProvider(item)

                                        Text(
                                            text = item.fullName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (isChecked && maxUsage != null && maxUsage > 0) {
                                            val usage = usageMap?.get(item.id) ?: 0
                                            Text(
                                                "($usage / $maxUsage)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                },
                                trailingContent = null,
                                colors = ListItemDefaults.colors(
                                    containerColor = getGridCardColor(index, filteredSize, isChecked),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ModalAddButton(
                                title = "Add fisherman ...",
                                onAdd = { onAdd() }
                            )
                        }
                    }
                } else {
                    // ── LIST VIEW ───────────────────────────────────────────
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        listItemsIndexed(
                            items = filtered,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val isChecked = selectedItems.contains(item)

                            val state =
                                if (isChecked) {
                                    val usage = usageMap?.get(item.id) ?: 0
                                    if (maxUsage != null && usage < maxUsage) {
                                        ToggleableState.Indeterminate
                                    }
                                    else ToggleableState.On
                                } else ToggleableState.Off

                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        width = if (isChecked) 2.dp else 0.dp,
                                        color =
                                            when (state) {
                                                ToggleableState.On -> getOnCardColor()
                                                ToggleableState.Indeterminate -> getOnCardColor().copy(
                                                    alpha = 0.5f
                                                )

                                                else -> Color.Transparent
                                            },
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .clickable(enabled = true) {
                                        if (state == ToggleableState.On) onUnselected(item)
                                        else onSelected(item)
                                    },
                                leadingContent = {
                                    thumbnailProvider(item)
                                },
                                headlineContent = {
                                    Column() {
                                        Text(
                                            item.fullName,
                                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (isChecked && maxUsage != null && maxUsage > 0) {
                                            val usage = usageMap?.get(item.id) ?: 0
                                            Text(
                                                "($usage / $maxUsage)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                },
                                trailingContent = {
                                    TriStateCheckbox(
                                        state = state,
                                        onClick = null,
                                        enabled = true
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = getCardColor(index, filteredSize, isChecked),
                                    headlineColor = getOnCardColor()
                                )
                            )
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item {
                            ModalAddButton(
                                title = "Add fisherman ...",
                                onAdd = { onAdd() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FishermanItemPreview() {
    FishstoryTheme(selectedTheme = null) {
        FishermanItem(
            fisherman = FishermanSummary(
                fisherman = Fisherman(firstName = "John", lastName = "Doe", nickname = "Big Fish"),
                fishCaught = 10,
                fishKept = 2,
                targetFishCaught = 8,
                targetFishKept = 1,
                totalTrips = 5,
                totalTackleBoxes = 3,
                largestFish = 0,
                smallestFish = 0
            ),
            thumbnailFlow = flowOf(null),
            photosFlow = flowOf(emptyList()),
            onClick = {},
            onFishClick = { _, _ -> },
            onPhotoAdded = {},
            onPhotoTaken = {},
            onSetThumbnail = {},
            onPhotoDeleted = {},
            onDelete = {},
        )
    }
}
