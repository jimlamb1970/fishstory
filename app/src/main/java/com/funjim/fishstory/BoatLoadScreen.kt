package com.funjim.fishstory

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoatLoadScreen(
    viewModel: MainViewModel,
    tripId: Int,
    navigateToManageFishermen: () -> Unit,
    navigateBack: () -> Unit
) {
    val allFishermen by viewModel.fishermen.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Determine current crew based on whether it's a draft or existing trip
    val inBoat: List<Fisherman>
    val draftFishermanIds by viewModel.draftFishermanIds.collectAsState()
    val tripWithFishermen by if (tripId != 0) {
        viewModel.getTripWithFishermen(tripId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    if (tripId == 0) {
        inBoat = allFishermen.filter { it.id in draftFishermanIds }
    } else {
        inBoat = tripWithFishermen?.fishermen ?: emptyList()
    }

    val available = allFishermen.filter { fisherman -> 
        inBoat.none { it.id == fisherman.id } 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Load Trip Boat") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Available Fishermen",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
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
                            if (tripId == 0) {
                                viewModel.addDraftFisherman(fisherman.id)
                            } else {
                                scope.launch {
                                    viewModel.addFishermanToTrip(tripId, fisherman.id)
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Boat
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFF0077B6), RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp, bottomStart = 20.dp, bottomEnd = 20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                    Text(
                        "THE BOAT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "${inBoat.size} Fishermen on board",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fishermen in Boat (Click to remove)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                items(inBoat, key = { it.id }) { fisherman ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(50.dp),
                        onClick = {
                            if (tripId == 0) {
                                viewModel.removeDraftFisherman(fisherman.id)
                            } else {
                                scope.launch {
                                    viewModel.deleteFishermanFromTrip(tripId, fisherman.id)
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(fisherman.fullName)
                        }
                    }
                }
            }
        }
    }
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
