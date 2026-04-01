package com.funjim.fishstory

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoatLoadScreen(
    eligibleFishermen: List<Fisherman>,
    initialCrew: List<Fisherman>,
    canAddNewFisherman: Boolean, // Controls visibility of the "Add" button
    onAddFisherman: (String, String, String) -> Unit,
    onSave: (List<Fisherman>) -> Unit, // Called only when FAB is pressed
    onCancel: () -> Unit // Called when Back is pressed
) {
    var showAddFishermanDialog by remember { mutableStateOf(false) }

    val localCrew = remember(initialCrew) { mutableStateListOf<Fisherman>().apply { addAll(initialCrew) } }

    // Derived State: Available = Eligible - Current Crew
    val available = remember(eligibleFishermen, localCrew.toList()) {
        eligibleFishermen
            .filter { person -> localCrew.none { it.id == person.id } }
            .sortedBy { it.fullName }
    }

    val inBoatSorted = remember(localCrew.toList()) {
        localCrew.sortedBy { it.fullName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Load Boat") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onSave(localCrew.toList()) },
                icon = { Icon(Icons.Default.Check, "Save") },
                text = { Text("Confirm Crew") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header for Available Pool
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Fishermen",
                    style = MaterialTheme.typography.titleMedium
                )

                if (canAddNewFisherman) {
                    IconButton(onClick = { showAddFishermanDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create New")
                    }
                }
            }

            // Available List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(available, key = { it.id }) { fisherman ->
                    DraggableFishermanItem(
                        fisherman = fisherman,
                        onDroppedInBoat = {
                            if (localCrew.none { it.id == fisherman.id }) {
                                localCrew.add(fisherman)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Boat Visual
            BoatVisual(crewCount = inBoatSorted.size)

            Spacer(modifier = Modifier.height(16.dp))

            // Current Crew List
            Text(
                text = "In Boat (Tap to remove)",
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(inBoatSorted, key = { it.id }) { fisherman ->
                    FishermanCard(
                        fisherman = fisherman,
                        onClick = {
                            localCrew.removeAll { it.id == fisherman.id }
                        }
                    )
                }
            }
        }

        if (showAddFishermanDialog) {
            AddFishermanDialog(
                onDismiss = { showAddFishermanDialog = false },
                onConfirm = { first, last, nick ->
                    onAddFisherman(first, last, nick)
                    showAddFishermanDialog = false
                }
            )
        }
    }
}

@Composable
private fun BoatVisual(crewCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFF0077B6), RoundedCornerShape(topStart = 80.dp, topEnd = 80.dp, bottomStart = 16.dp, bottomEnd = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.DirectionsBoat, null, modifier = Modifier.size(48.dp), tint = Color.White)
            Text("THE BOAT", color = Color.White, fontWeight = FontWeight.Bold)
            Text("$crewCount Fishermen", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun FishermanCard(
    fisherman: Fisherman,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(56.dp), // Slightly taller for better touch targets
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar/Icon Section
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name Section
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fisherman.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Optional: Hint icon to show it's removable
            Icon(
                imageVector = Icons.Default.RemoveCircleOutline,
                contentDescription = "Remove",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            )
        }
    }
}
@Composable
fun AddFishermanDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Fisherman") },
        text = {
            Column {
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (firstName.isNotBlank() && lastName.isNotBlank()) {
                    onConfirm(firstName, lastName, nickname)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DraggableFishermanItem(
    fisherman: Fisherman,
    onDroppedInBoat: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(fisherman.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        if (offsetY > 150f) {
                            onDroppedInBoat()
                        }
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = if (isDragging) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(fisherman.fullName)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (isDragging) "Release to drop" else "Long press to drag",
                style = MaterialTheme.typography.labelSmall,
                color = if (isDragging) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}
