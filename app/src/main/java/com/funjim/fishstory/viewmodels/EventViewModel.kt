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

class EventViewModel(
    private val locationProvider: LocationProvider,
    private val fishermanRepo: FishermanRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    // --- (UI State) ---
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)
    private val _eventCrewOverride = MutableStateFlow(false)

    // --- Event Draft State ---
    private val _eventDraft = MutableStateFlow(Event(id = UUID.randomUUID().toString(), name = "", tripId = ""))
    val eventDraft = _eventDraft.asStateFlow()
    fun clearEventDraft() {
        _eventDraft.value = Event(id = UUID.randomUUID().toString(), name = "", tripId = "")
        _currentEventWizardStep.value = EventWizardStep.EventInfo
    }
    fun updateEventDraft(update: (Event) -> Event) {
        _eventDraft.update(update)
        _selectedEventId.value = _eventDraft.value.id
    }

    private val _eventTargetSpecies = MutableStateFlow<List<Species>>(emptyList())
    val eventTargetSpecies = _eventTargetSpecies.asStateFlow()
    fun clearEventTargetSpecies() {
        _eventTargetSpecies.value = emptyList()
    }
    fun updateEventTargetSpecies(ids: List<Species>) {
        _eventTargetSpecies.value = ids
    }

    // --- Data Streams ---
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTrip = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                tripRepo.getTripWithFishermenAndSpecies(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEventWithDetails = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                tripRepo.getEventWithDetails(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEventSummary = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                tripRepo.getEventSummary(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEventDetailedSummary = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                tripRepo.getEventDetailedSummary(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun getFishermenForTrip(tripId: String): Flow<List<Fisherman>> {
        return fishermanRepo.getFishermenForTrip(tripId)
    }

    fun getFishermenForEvent(eventId: String): Flow<List<Fisherman>> {
        return fishermanRepo.getFishermenForEvent(eventId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventPhotos: StateFlow<List<Photo>> = _selectedEventId
        .filterNotNull()
        .flatMapLatest { photoRepo.getPhotosForEvent(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getLuresInTackleBox(tackleBoxId: String?): Flow<List<LureWithColors>> {
        return fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "")
    }

    val allSpecies: StateFlow<List<Species>> = fishRepo.allSpecies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<EventDetailsUiState> = combine(
        selectedEventWithDetails,
        selectedEventDetailedSummary
    ) { event, summary ->
        // Guard clause: Ensure the database has returned valid data for everything
        if (event != null && summary != null) {
            EventDetailsUiState.Success(
                details = event,
                summary = summary
            )
        } else {
            EventDetailsUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventDetailsUiState.Loading
    )

    val uiAddEventState: StateFlow<AddEventUiState> = combine(
        selectedTrip,
        _eventDraft
    ) { trip, event ->
        if (trip != null) {
            val ids = trip.fishermen.map { it.id }.toSet()
            _tripFishermanIds.value = ids

            AddEventUiState.Success(
                trip = trip,
                event = event
            )
        } else {
            AddEventUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEventUiState.Loading
    )

    fun addEventTargetSpecies(eventId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.insertEventTargetSpecies(EventTargetSpecies(eventId = eventId, speciesId = speciesId))
        }
    }

    fun removeEventTargetSpecies(eventId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.deleteEventTargetSpecies(eventId = eventId, speciesId = speciesId)
        }
    }

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            fishRepo.addSpecies(species)
        }
    }

    // --- Actions ---
    fun upsertEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.upsertEvent(event)
        }
    }

    fun deleteEventById(eventId: String) {
        viewModelScope.launch {
            tripRepo.deleteEventById(eventId)
        }
    }

    fun upsertEventFishermanCrossRef(eventId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripRepo.upsertEventFishermanCrossRef(
                EventFishermanCrossRef(eventId, fishermanId, tackleBoxId)
            )
        }
    }

    fun deleteEventFishermanCrossRef(eventId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepo.deleteEventFishermanCrossRef(EventFishermanCrossRef(eventId, fishermanId))
        }
    }

    fun createAndAssignEventTackleBox(fishermanId: String, eventId: String, name: String) {
        viewModelScope.launch {
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
    }

    fun createAndAssignEventTackleBox(tackleBox: TackleBox, eventId: String) {
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
            tripRepo.upsertEventFishermanCrossRef(
                EventFishermanCrossRef(
                    eventId,
                    tackleBox.fishermanId,
                    tackleBox.id
                )
            )
        }
    }

    fun addEventPhoto(eventId: String, uri: Uri, selected: Boolean) {
        viewModelScope.launch {
            photoRepo.addEventPhoto(eventId, uri, selected)
                .onSuccess {  }
                .onFailure {  }
        }
    }

    fun deleteEventPhoto(eventId: String, photoId: String) {
        viewModelScope.launch { photoRepo.deleteEventPhoto(eventId, photoId) }
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

    fun updateEventCrewOverride(override: Boolean) {
        _eventCrewOverride.value = override
    }

    // --- Wizard Navigation State ---

    private val _currentEventWizardStep = MutableStateFlow(EventWizardStep.EventInfo)
    val currentEventWizardStep = _currentEventWizardStep.asStateFlow()

    fun updateEventWizardStep(step: EventWizardStep) {
        _currentEventWizardStep.value = step
    }

    // --- Crew Draft State ---
    private val _tripFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val tripFishermenIds = _tripFishermanIds.asStateFlow()

    private val _draftEventFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val eventFishermenIds = _draftEventFishermanIds.asStateFlow()

    fun toggleEventFisherman(id: String) {
        _draftEventFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun updateEventFishermanIds(ids: Set<String>) {
        _draftEventFishermanIds.value = ids
    }

    fun insertTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
        }
    }
}

sealed interface EventDetailsUiState {
    object Loading : EventDetailsUiState

    data class Success(
        val details: EventWithDetails,
        val summary: EventDetailedSummary,
    ) : EventDetailsUiState
}

class EventViewModelFactory(
    private val locationProvider: LocationProvider,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val photoRepository: PhotoRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(
                locationProvider,
                fishermanRepository,
                fishRepository,
                photoRepository,
                tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
