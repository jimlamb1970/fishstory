package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.ui.utils.TripViewModelCrewPickerBridge
import com.funjim.fishstory.viewmodels.TripViewModel
import kotlinx.coroutines.launch
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTripCrewScreen(
    tripViewModel: TripViewModel,
    tripId: String,
    eligibleFishermen: List<Fisherman>,
    initialCrew: List<Fisherman>,
    navigateBack: () -> Unit
) {
    val sortedFishermen = remember(eligibleFishermen) { eligibleFishermen.sortedBy { it.fullName } }
    var initialSet by remember(initialCrew) { mutableStateOf<Set<String>>(initialCrew.map { it.id }.toSet()) }
    var addSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var removeSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tripId) {
        tripViewModel.selectTrip(tripId)
    }

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val workingTackleBoxMap = remember { mutableStateMapOf<String, String?>() }

    LaunchedEffect(tripTackleBoxMap) {
        // Only initialize if the map is currently empty and we have data to put in it
        if (workingTackleBoxMap.isEmpty() && tripTackleBoxMap.isNotEmpty()) {
            tripTackleBoxMap.forEach { (fisherman, tackleBoxId) ->
                workingTackleBoxMap[fisherman] = tackleBoxId
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Load Boat") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
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
        ) {
            Spacer(Modifier.height(16.dp))
            TripViewModelCrewPickerBridge(
                title = "Crew & Tackle Boxes",
                subtitle = "Select who's on the boat and which tackle box each person will use.",
                eligibleFishermen = sortedFishermen,
                selectedIds = initialSet + addSet - removeSet,
                tackleBoxSelections = workingTackleBoxMap,
                onSelectionChanged = { fishermanId, selected ->
                    if (selected) {
                        if (initialSet.contains(fishermanId)) {
                            removeSet = removeSet - fishermanId
                        } else {
                            addSet = addSet + fishermanId
                            workingTackleBoxMap[fishermanId] = null
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
                    workingTackleBoxMap[fishermanId] = boxId
                },
                tripViewModel = tripViewModel,
                confirmLabel = "Confirm Crew & Tackle Boxes",
                onConfirm = {
                    removeSet.forEach { fishermanId ->
                        // TODO - Need to refactor trip and segment fisherman cross references
                        // TODO - As soon as removed from trip, should be removed from segment
                        tripViewModel.deleteTripFishermanCrossRef(
                            tripId = tripId,
                            fishermanId = fishermanId
                        )
                    }
                    addSet.forEach { fishermanId ->
                        tripViewModel.upsertTripFishermanCrossRef(
                            tripId = tripId,
                            fishermanId = fishermanId,
                            tackleBoxId = workingTackleBoxMap[fishermanId]
                        )
                    }
                    workingTackleBoxMap.forEach { (fishermanId, boxId) ->
                        if ((fishermanId !in addSet) && (fishermanId !in removeSet)) {
                            tripViewModel.upsertTripFishermanCrossRef(
                                tripId = tripId,
                                fishermanId = fishermanId,
                                tackleBoxId = boxId
                            )
                        }
                    }
                    navigateBack()
                },
                onAddFisherman = { first, last, nick ->
                    scope.launch {
                        tripViewModel.addFisherman(first, last, nick)
                    }
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
                    workingTackleBoxMap.toMutableMap().apply {
                        this[fishermanId] = boxId
                    }
                }
            )
        }
    }
}
