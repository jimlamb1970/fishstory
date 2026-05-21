package com.funjim.fishstory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun SelectEventCrewScreen(
    tripViewModel: TripViewModel,
    tripId: String,
    eventId: String,
    navigateToEditTackleBox: ((fishermanId: String, tackleBoxId: String) -> Unit),
    navigateBack: () -> Unit
) {
    LaunchedEffect(tripId) {
        tripViewModel.selectTrip(tripId)
        tripViewModel.selectEvent(eventId)
    }

    val tripSummary by tripViewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val eventSummary by tripViewModel.selectedEventSummary.collectAsStateWithLifecycle()
    val eventCrewOverride by tripViewModel.eventCrewOverride.collectAsStateWithLifecycle()

    val tripCrew by tripViewModel.getFishermenForTrip(tripId).collectAsState(emptyList())
    val eventCrew by tripViewModel.getFishermenForEvent(eventId).collectAsState(emptyList())

    val sortedFishermen = remember(tripCrew) { tripCrew.sortedBy { it.fullName } }
    var initialSet by remember(eventCrew) {
        mutableStateOf<Set<String>>(eventCrew.map { it.id }.toSet())
    }
    var addSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var removeSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState()
    val eventTackleBoxMap by tripViewModel.eventTackleBoxMap.collectAsState()
    val workingTackleBoxMap = remember(eventId) { mutableStateMapOf<String, String?>() }

    LaunchedEffect(eventTackleBoxMap, tripTackleBoxMap) {
        // Only initialize if the map is currently empty and we have data to put in it
        if (workingTackleBoxMap.isEmpty()) {
            if (eventTackleBoxMap.isNotEmpty()) {
                eventTackleBoxMap.forEach { (fishermanId, tackleBoxId) ->
                    workingTackleBoxMap[fishermanId] = tackleBoxId
                }
            } else if (tripTackleBoxMap.isNotEmpty()){
                tripViewModel.updateEventCrewOverride(true)

                tripTackleBoxMap.forEach { (fishermanId, tackleBoxId) ->
                    workingTackleBoxMap[fishermanId] = tackleBoxId
                    tripViewModel.upsertEventFishermanCrossRef(
                        eventId = eventId,
                        fishermanId = fishermanId,
                        tackleBoxId = tackleBoxId
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crew & Tackle Boxes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        tripViewModel.clearTrip()
                        tripViewModel.clearEvent()

                        workingTackleBoxMap.forEach { (fishermanId, tackleBoxId) ->
                            tripViewModel.deleteEventFishermanCrossRef(
                                eventId = eventId,
                                fishermanId = fishermanId
                            )
                        }
                        tripViewModel.updateEventCrewOverride(false)

                        navigateBack()
                    }) {
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
                title = eventSummary?.event?.name ?: "Crew & Tackle Boxes",
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
                navigateToEditTackleBox = navigateToEditTackleBox,
                tripViewModel = tripViewModel,
                confirmLabel = "Confirm Crew & Tackle Boxes",
                onConfirm = {
                    removeSet.forEach { fishermanId ->
                        tripViewModel.deleteEventFishermanCrossRef(
                            eventId = eventId,
                            fishermanId = fishermanId
                        )
                    }
                    addSet.forEach { fishermanId ->
                        tripViewModel.upsertEventFishermanCrossRef(
                            eventId = eventId,
                            fishermanId = fishermanId,
                            tackleBoxId = workingTackleBoxMap[fishermanId]
                        )
                    }
                    workingTackleBoxMap.forEach { (fishermanId, boxId) ->
                        if ((fishermanId !in addSet) && (fishermanId !in removeSet)) {
                            tripViewModel.upsertEventFishermanCrossRef(
                                eventId = eventId,
                                fishermanId = fishermanId,
                                tackleBoxId = boxId
                            )
                        }
                    }
                    tripViewModel.updateEventCrewOverride(false)
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
                    workingTackleBoxMap.toMutableMap().apply {
                        this[fishermanId] = boxId
                    }
                }
            )
        }
    }
}
