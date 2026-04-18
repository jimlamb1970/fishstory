package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.utils.TripViewModelCrewPickerBridge
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentBoatLoadScreen(
    tripViewModel: TripViewModel,
    tripId: String,
    segmentId: String,
    navigateBack: () -> Unit
) {
    LaunchedEffect(tripId) {
        tripViewModel.selectTrip(tripId)
        tripViewModel.selectSegment(segmentId)
    }

    val eligibleFishermen by tripViewModel.getFishermenForTrip(tripId).collectAsState(emptyList())
    val initialCrew by tripViewModel.getFishermenForSegment(segmentId).collectAsState(emptyList())

    val sortedFishermen = remember(eligibleFishermen) { eligibleFishermen.sortedBy { it.fullName } }
    var initialSet by remember(initialCrew) { mutableStateOf<Set<String>>(initialCrew.map { it.id }.toSet()) }
    var addSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var removeSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val segmentTackleBoxMap by tripViewModel.segmentTackleBoxMap.collectAsState()
    var workingTripTackleBoxMap by remember { mutableStateOf(segmentTackleBoxMap) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Load Boat") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
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
            TripViewModelCrewPickerBridge(
                title = "Crew & Tackle Boxes",
                subtitle = "Select who's on the boat and which tackle box each person will use.",
                eligibleFishermen = sortedFishermen,
                selectedIds = initialSet + addSet - removeSet,
                tackleBoxSelections = workingTripTackleBoxMap,
                onSelectionChanged = { fishermanId, selected ->
                    if (selected) {
                        if (initialSet.contains(fishermanId)) {
                            removeSet = removeSet - fishermanId
                        } else {
                            addSet = addSet + fishermanId
                            workingTripTackleBoxMap = workingTripTackleBoxMap.toMutableMap().apply {
                                this[fishermanId] = null
                            }
                        }
                    } else {
                        if (initialSet.contains(fishermanId)) {
                            removeSet = removeSet + fishermanId
                        } else {
                            addSet = addSet - fishermanId
                        }
                        addSet = addSet - fishermanId
                        removeSet = removeSet + fishermanId
                    }
                },
                onTackleBoxChanged = { fishermanId, boxId ->
                    workingTripTackleBoxMap = workingTripTackleBoxMap.toMutableMap().apply {
                        this[fishermanId] = boxId
                    }
                },
                tripViewModel = tripViewModel,
                confirmLabel = "Confirm Crew & Tackle Boxes",
                onConfirm = {
                    removeSet.forEach { fishermanId ->
                        // TODO - Need to refactor trip and segment fisherman cross references
                        // TODO - As soon as removed from trip, should be removed from segment
                        tripViewModel.deleteSegmentFishermanCrossRef(
                            segmentId = segmentId,
                            fishermanId = fishermanId
                        )
                    }
                    addSet.forEach { fishermanId ->
                        tripViewModel.upsertSegmentFishermanCrossRef(
                            segmentId = segmentId,
                            fishermanId = fishermanId,
                            tackleBoxId = workingTripTackleBoxMap[fishermanId]
                        )
                    }
                    workingTripTackleBoxMap.forEach { (fishermanId, boxId) ->
                        if ((fishermanId !in addSet) && (fishermanId !in removeSet)) {
                            tripViewModel.upsertSegmentFishermanCrossRef(
                                segmentId = segmentId,
                                fishermanId = fishermanId,
                                tackleBoxId = boxId
                            )
                        }
                    }
                    navigateBack()
                },
                onAddTackleBox = { tackleBoxName, fishermanId ->
                    val boxId = UUID.randomUUID().toString()
                    scope.launch {
                        tripViewModel.insertTackleBox(
                            TackleBox(
                                id = boxId,
                                fishermanId = fishermanId,
                                name = tackleBoxName
                            )
                        )
                    }
                    workingTripTackleBoxMap.toMutableMap().apply {
                        this[fishermanId] = boxId
                    }
                }
            )
        }
    }
}
