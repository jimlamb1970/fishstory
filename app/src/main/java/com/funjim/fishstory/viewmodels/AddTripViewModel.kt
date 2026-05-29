package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.map

enum class WizardStep {
    TripInfo,           // Step 1 – name, dates, location
    TripCrew,           // Step 2 – fishermen + tackle boxes for trip
    EventInfo,          // Step 3 – event name, dates, location
    EventCrew,          // Step 4 – fishermen + tackle boxes for event
    Review              // Step 5 – list events, add another or finish
}
class AddTripViewModel(
    private val locationProvider: LocationProvider,
    private val fishermanRepo: FishermanRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    // --- (UI State) ---
    private val _selectedTripId = MutableStateFlow<String?>(null)
    val selectedTripId = _selectedTripId.asStateFlow()
    private val _selectedEventId = MutableStateFlow<String?>(null)

    private val _addedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val addedEventIds = _addedEventIds.asStateFlow()

    private val _eventCrewOverride = MutableStateFlow(false)
    val eventCrewOverride = _eventCrewOverride.asStateFlow()

    val allSpecies: StateFlow<List<Species>> = fishRepo.allSpecies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Data Streams ---
    private val allTripSummaries = tripRepo.allTripSummaries
    val allTrips = tripRepo.allTrips

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTripDetailedSummary = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                tripRepo.getTripDetailedSummary(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTripWithDetails = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                tripRepo.getTripWithDetails(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventSummaries: StateFlow<List<EventSummary>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                tripRepo.getEventSummaries(id)
            }
        }
        .map { list ->
            // Sort by whatever property makes sense for your events
            list.sortedBy { it.event.startTime }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiDetailState: StateFlow<TripDetailsUiState> = combine(
        selectedTripWithDetails,
        selectedTripDetailedSummary,
        eventSummaries
    ) { trip, summary, events ->
        // Guard clause: Ensure the database has returned valid data for everything
        if (trip != null && summary != null) {
            TripDetailsUiState.Success(
                details = trip,
                summary = summary,
                eventSummaries = events
            )
        } else {
            TripDetailsUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripDetailsUiState.Loading
    )

    val fishermen: Flow<List<Fisherman>> = fishermanRepo.allFishermen

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripFishermen: StateFlow<List<Fisherman>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                fishermanRepo.getFishermenForTrip(id)
            }
        }
        .map { list ->
            // Sort by Last Name, then First Name
            list.sortedWith(compareBy({ it.fullName }))
        }
        .onEach { list ->
            // SIDE EFFECT: Update the draft IDs whenever the list changes
            // This ensures the wizard state is "set" automatically
            val ids = list.map { it.id }.toSet()
            _tripFishermanIds.value = ids
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getFishermenForTrip(tripId: String): Flow<List<Fisherman>> {
        return fishermanRepo.getFishermenForTrip(tripId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventFishermen: StateFlow<List<Fisherman>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                fishermanRepo.getFishermenForEvent(id)
            }
        }
        .map { list ->
            // Sort by Last Name, then First Name
            list.sortedWith(compareBy({ it.fullName }))
        }
        .onEach { list ->
            // SIDE EFFECT: Update the draft IDs whenever the list changes
            // This ensures the wizard state is "set" automatically
            val ids = list.map { it.id }.toSet()
            _draftEventFishermanIds.value = ids
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getFishermenForEvent(eventId: String): Flow<List<Fisherman>> {
        return fishermanRepo.getFishermenForEvent(eventId)
    }

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return fishermanRepo.getTackleBoxesForFisherman(fishermanId)
    }

    fun getLureCountForTackleBox(tackleBoxId: String?): Flow<Int> {
        return fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "").map { it.size }
    }

    // TODO -- check flows where filterNotNul is and see if they need to be changed
    @OptIn(ExperimentalCoroutinesApi::class)
    val tripTackleBoxMap: StateFlow<Map<String, String?>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyMap()) // This clears the map when you set id to null
            } else {
                getTackleBoxMapForTrip(tripId = id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // TODO -- check flows where filterNotNul is and see if they need to be changed
    @OptIn(ExperimentalCoroutinesApi::class)
    val eventTackleBoxMap: StateFlow<Map<String, String?>> = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyMap()) // This clears the map when you set id to null
            } else {
                getTackleBoxMapForEvent(eventId = id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun getTackleBoxMapForTrip(tripId: String): Flow<Map<String, String?>> {
        return tripRepo.getTackleBoxMapForTrip(tripId)
    }

    fun getTackleBoxMapForEvent(eventId: String): Flow<Map<String, String?>> {
        return tripRepo.getTackleBoxMapForEvent(eventId)
    }

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }

    fun getLuresInTackleBox(tackleBoxId: String?): Flow<List<LureWithColors>> {
        return fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "")
    }

    // --- Actions ---
    suspend fun saveTrip() {
        tripRepo.upsertTrip(tripDraft.value)
    }

    suspend fun deleteTripById(tripId: String) {
        tripRepo.deleteTripById(tripId)
    }

    suspend fun upsertEvent(event: Event) {
        tripRepo.upsertEvent(event)
        _addedEventIds.update { it + event.id }
    }

    suspend fun upsertTripFishermanCrossRef(
        tripId: String,
        fishermanId: String, tackleBoxId: String?
    ) {
        tripRepo.upsertTripFishermanCrossRef(
            TripFishermanCrossRef(tripId, fishermanId, tackleBoxId)
        )
    }

    suspend fun removeFishermanFromTripAndAllEvents(
        tripId: String,
        fishermanId: String
    ) {
        tripRepo.removeFishermanFromTripAndAllEvents(tripId, fishermanId)
    }

    suspend fun upsertEventFishermanCrossRef(
        eventId: String,
        fishermanId: String, tackleBoxId: String?
    ) {
        tripRepo.upsertEventFishermanCrossRef(
            EventFishermanCrossRef(eventId, fishermanId, tackleBoxId)
        )
    }

    suspend fun deleteEventFishermanCrossRef(
        eventId: String,
        fishermanId: String
    ) {
        tripRepo.deleteEventFishermanCrossRef(EventFishermanCrossRef(eventId, fishermanId))
    }

    suspend fun addFisherman(firstName: String, lastName: String, nickname: String) {
        // Check if the fisherman already exists
        val fisherman = fishermanRepo.getFishermanByName(firstName, lastName, nickname)

        // If the fisherman does not exist, add the fisherman (this will also create a tackle box)
        if (fisherman == null) {
            val fisherman = Fisherman(firstName = firstName, lastName = lastName, nickname = nickname)
            fishermanRepo.addFisherman(fisherman)
        }
    }

    suspend fun insertTackleBox(tackleBox: TackleBox) {
        fishermanRepo.insertTackleBox(tackleBox)
    }

    suspend fun createAndAssignTackleBox(fishermanId: String, tripId: String, name: String) {
        val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
        fishermanRepo.insertTackleBox(tackleBox)
        tripRepo.upsertTripFishermanCrossRef(
            TripFishermanCrossRef(
                tripId,
                fishermanId,
                tackleBox.id
            )
        )
    }
    suspend fun createAndAssignEventTackleBox(fishermanId: String, eventId: String, name: String) {
        val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
        fishermanRepo.insertTackleBox(tackleBox)
        tripRepo.upsertEventFishermanCrossRef(
            EventFishermanCrossRef(
                eventId,
                fishermanId,
                tackleBox.id
            )
        )
    }

    private val _tripTargetSpecies = MutableStateFlow<List<Species>>(emptyList())
    val tripTargetSpecies = _tripTargetSpecies.asStateFlow()

    private val _eventTargetSpeciesMap = MutableStateFlow<Map<String, List<Species>>>(emptyMap())
    val eventTargetSpeciesMap = _eventTargetSpeciesMap.asStateFlow()

    val eventTargetSpeciesUsageMap = _eventTargetSpeciesMap
        .map { currentMap ->
            currentMap
                .filterKeys { eventId -> eventId in addedEventIds.value }
                .values               // Get all the List<Species> (ignores the event ID keys)
                .flatten()            // Combine all lists into a single List<Species>
                .groupingBy { it.id } // Group identical Species objects together
                .eachCount()          // Count how many of each Species exist
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun removeTripTargetSpecies(species: Species) {
        // Remove the species from the trip target species list
        _tripTargetSpecies.update { it - species }

        // Remove the species from any existing event target species lists
        _eventTargetSpeciesMap.update { currentMap ->
            currentMap.mapValues { (_, speciesList) ->
                speciesList - species
            }
        }
    }
    fun addTripTargetSpecies(species: Species) {
        // Add the species to the trip target species list
        _tripTargetSpecies.update { it + species }

        // Add the species to any existing event target species lists
        _eventTargetSpeciesMap.update { currentMap ->
            currentMap.mapValues { (_, speciesList) ->
                speciesList + species
            }
        }
    }

    fun removeEventTargetSpecies(eventId: String, species: Species) {
        // Remove the species from the event target species list
        _eventTargetSpeciesMap.update { currentMap ->
            currentMap.mapValues { (id, speciesList) ->
                if (id == eventId) {
                    speciesList - species
                } else {
                    speciesList
                }
            }
        }
        // Removing from event does nothing for the trip target species
    }

    fun addEventTargetSpecies(eventId: String, species: Species) {
        // Add the species to the event target species list
        _eventTargetSpeciesMap.update { currentMap ->
            currentMap.mapValues { (id, speciesList) ->
                if (id == eventId) {
                    speciesList + species
                } else {
                    speciesList
                }
            }
        }
        // Adding to event adds it for the trip target species
        _tripTargetSpecies.update { it + species }
    }

    suspend fun addSpecies(species: Species) {
        fishRepo.addSpecies(species)
    }

    suspend fun persistTargetSpecies() {
        val tripId = tripDraft.value.id
        val addedEventIds = addedEventIds.value
        val tripSpecies = tripTargetSpecies.value
        val eventSpeciesMap = eventTargetSpeciesMap.value

        tripSpecies.forEach { species ->
            tripRepo.insertTripTargetSpecies(
                TripTargetSpecies(
                    tripId = tripId,
                    speciesId = species.id),
                cascade = false)
        }
        eventSpeciesMap.forEach { (eventId, speciesList) ->
            if (eventId in addedEventIds) {
                speciesList.forEach { species ->
                    tripRepo.insertEventTargetSpecies(
                        EventTargetSpecies(
                            eventId = eventId,
                            speciesId = species.id
                        ),
                        cascade = false
                    )
                }
            }
        }
    }

    fun clearTrip() {
        _selectedTripId.value = null
    }
    fun clearEvent() {
        _selectedEventId.value = null
    }

    fun selectTrip(id: String) {
        _selectedTripId.value = id
    }

    fun selectEvent(id: String) {
        _selectedEventId.value = id
    }

    // --- Wizard Navigation State ---
    private val _currentWizardStep = MutableStateFlow(WizardStep.TripInfo)
    val currentWizardStep = _currentWizardStep.asStateFlow()

    fun updateWizardStep(step: WizardStep) {
        _currentWizardStep.value = step
    }

    // --- Trip Draft State ---
    private val _tripDraft = MutableStateFlow(Trip(id = UUID.randomUUID().toString(), name = ""))
    val tripDraft = _tripDraft.asStateFlow()

    fun clearTripDraft() {
        _tripDraft.value = Trip(id = UUID.randomUUID().toString(), name = "")
        _currentWizardStep.value = WizardStep.TripInfo
    }

    fun updateTripDraft(update: (Trip) -> Trip) {
        _tripDraft.update(update)
        // Synchronize the selected ID for your other flows
        _selectedTripId.value = _tripDraft.value.id
    }

    // --- Event Draft State ---
    private val _eventDraft = MutableStateFlow(Event(id = UUID.randomUUID().toString(), name = "", tripId = ""))
    val eventDraft = _eventDraft.asStateFlow()

    fun prepEventDraft(trip: Trip) {
        _eventDraft.value = Event(
            id = UUID.randomUUID().toString(),
            name = "",
            tripId = trip.id,
            startTime = trip.startDate,
            endTime = trip.endDate)
        _selectedEventId.value = _eventDraft.value.id

        _eventTargetSpeciesMap.update { currentMap ->
            currentMap + (_eventDraft.value.id to tripTargetSpecies.value)
        }
    }

    fun clearEventDraft() {
        _eventDraft.value = Event(id = UUID.randomUUID().toString(), name = "", tripId = "")
    }

    fun updateEventDraft(update: (Event) -> Event) {
        _eventDraft.update(update)
        _selectedEventId.value = _eventDraft.value.id
    }

    // --- Crew Draft State ---
    private val _tripFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val tripFishermenIds = _tripFishermanIds.asStateFlow()

    fun toggleTripFisherman(id: String) {
        _tripFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    private val _draftEventFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val eventFishermenIds = _draftEventFishermanIds.asStateFlow()

    fun toggleEventFisherman(id: String) {
        _draftEventFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun updateEventFishermanIds(ids: Set<String>) {
        _draftEventFishermanIds.value = ids
    }
}

class AddTripViewModelFactory(
    private val locationProvider: LocationProvider,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val photoRepository: PhotoRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTripViewModel(
                locationProvider,
                fishermanRepository,
                fishRepository,
                photoRepository,
                tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
