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
    }
    LaunchedEffect(eventId) {
        tripViewModel.selectEvent(eventId)
    }

    val scope = rememberCoroutineScope()

    val tripSummary by tripViewModel.selectedTripSummary.collectAsStateWithLifecycle()
    val eventSummary by tripViewModel.selectedEventSummary.collectAsStateWithLifecycle()

    val tripCrew by tripViewModel.getFishermenForTrip(tripId).collectAsState(initial = null)
    val eventCrew by tripViewModel.getFishermenForEvent(eventId).collectAsState(initial = null)

    val tripTackleBoxMap by tripViewModel.tripTackleBoxMap.collectAsState(initial = null)
    val eventTackleBoxMap by tripViewModel.eventTackleBoxMap.collectAsState(initial = null)

    val tripSet = remember(tripCrew) { tripCrew?.map { it.id }?.toSet() ?: emptySet() }
    val eventSet = remember(eventCrew) { eventCrew?.map { it.id }?.toSet() ?: emptySet() }

    val sortedFishermen = remember(tripCrew) { tripCrew?.sortedBy { it.fullName } ?: emptyList()}

    var addSet by remember { mutableStateOf<Set<String>>(emptySet()) }
    var removeSet by remember { mutableStateOf<Set<String>>(emptySet()) }

    val workingTackleBoxMap = remember(eventId) { mutableStateMapOf<String, String?>() }

    var hasInitialized by remember(eventId) { mutableStateOf(false) }

    LaunchedEffect(eventTackleBoxMap, tripTackleBoxMap, tripSet, hasInitialized) {
        if (hasInitialized) return@LaunchedEffect

        if (eventTackleBoxMap == null || tripTackleBoxMap == null || tripCrew == null) return@LaunchedEffect

        if (eventTackleBoxMap!!.isNotEmpty()) {
            workingTackleBoxMap.clear()
            addSet = emptySet()
            workingTackleBoxMap.putAll(eventTackleBoxMap!!)
            hasInitialized = true
        } else {
            workingTackleBoxMap.clear()
            addSet = tripSet
            workingTackleBoxMap.putAll(tripTackleBoxMap!!)
            hasInitialized = true
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
                subtitle = """Select who's on the boat and which tackle box each person will use.
                    |
                    |If no one is selected, the trip crew and tackle box assignments will be used."""
                    .trimMargin(),
                eligibleFishermen = sortedFishermen,
                selectedIds = eventSet + addSet - removeSet,
                tackleBoxSelections = workingTackleBoxMap,
                onSelectionChanged = { fishermanId, selected ->
                    if (selected) {
                        if (eventSet.contains(fishermanId)) {
                            removeSet = removeSet - fishermanId
                        } else {
                            addSet = addSet + fishermanId
                            workingTackleBoxMap[fishermanId] = null
                        }
                    } else {
                        if (eventSet.contains(fishermanId)) {
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
