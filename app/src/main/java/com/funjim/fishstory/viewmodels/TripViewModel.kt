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
class TripViewModel(
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
    private val _eventCrewOverride = MutableStateFlow(false)
    val eventCrewOverride = _eventCrewOverride.asStateFlow()

    val allSpecies: StateFlow<List<Species>> = fishRepo.allSpecies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<TripUiState> = combine(
        tripRepo.getActiveTripSummaries(),
        tripRepo.getUpcomingTripSummaries(),
        tripRepo.getPreviousTripSummaries()
    ) { active, upcoming, previous ->
        TripUiState(
            liveTrips = active,
            upcomingTrips = upcoming,
            recentTrips = previous,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripUiState(isLoading = true)
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

    // TODO -- add sorting on Trip summaries
    val tripSummaries: StateFlow<List<TripSummary>> = allTripSummaries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTripSummary: StateFlow<TripSummary?> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                // We watch the master list and filter for the matching ID
                tripSummaries.map { list ->
                    list.find { it.trip.id == id }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripPhotos: StateFlow<List<Photo>> = _selectedTripId
        .filterNotNull()
        .flatMapLatest { photoRepo.getPhotosForTrip(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventPhotos: StateFlow<List<Photo>> = _selectedEventId
        .filterNotNull()
        .flatMapLatest { photoRepo.getPhotosForEvent(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun eventThumbnail(eventId: String): Flow<ByteArray?> {
        return photoRepo.fetchEventThumbnail(eventId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }

    fun getLuresInTackleBox(tackleBoxId: String?): Flow<List<LureWithColors>> {
        return fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "")
    }

    // --- Actions ---
    fun saveTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepo.upsertTrip(trip)
        }
    }

    fun deleteTripById(tripId: String) {
        viewModelScope.launch {
            tripRepo.deleteTripById(tripId)
        }
    }

    fun upsertEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.upsertEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.deleteEvent(event)
        }
    }

    fun deleteEventById(eventId: String) {
        viewModelScope.launch {
            tripRepo.deleteEventById(eventId)
        }
    }

    fun upsertTripFishermanCrossRef(tripId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripRepo.upsertTripFishermanCrossRef(
                TripFishermanCrossRef(tripId, fishermanId, tackleBoxId)
            )
        }
    }

    fun removeFishermanFromTripAndAllEvents(tripId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepo.removeFishermanFromTripAndAllEvents(tripId, fishermanId)
        }
    }

    fun upsertEventFishermanCrossRef(eventId: String, fishermanId: String, tackleBoxId: String?) {
        viewModelScope.launch {
            tripRepo.upsertSegmentFishermanCrossRef(
                EventFishermanCrossRef(eventId, fishermanId, tackleBoxId)
            )
        }
    }

    fun deleteEventFishermanCrossRef(eventId: String, fishermanId: String) {
        viewModelScope.launch {
            tripRepo.deleteSegmentFishermanCrossRef(EventFishermanCrossRef(eventId, fishermanId))
        }
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

    fun insertTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
        }
    }

    fun createAndAssignTackleBox(fishermanId: String, tripId: String, name: String) {
        viewModelScope.launch {
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
    }
    fun createAndAssignEventTackleBox(fishermanId: String, eventId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            fishermanRepo.insertTackleBox(tackleBox)
            tripRepo.upsertSegmentFishermanCrossRef(
                EventFishermanCrossRef(
                    eventId,
                    fishermanId,
                    tackleBox.id
                )
            )
        }
    }

    fun addTripPhoto(tripId: String, uri: Uri, selected: Boolean) {
        viewModelScope.launch {
            photoRepo.addTripPhoto(tripId, uri, selected)
                .onSuccess {  }
                .onFailure {  }
        }
    }
    fun deleteTripPhoto(tripId: String, photoId: String) {
        viewModelScope.launch { photoRepo.deleteTripPhoto(tripId, photoId) }
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            fishRepo.addSpecies(species)
        }
    }

    fun addTripTargetSpecies(tripId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.insertTripTargetSpecies(TripTargetSpecies(tripId = tripId, speciesId = speciesId))
        }
    }

    fun removeTripTargetSpecies(tripId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.deleteTripTargetSpecies(tripId = tripId, speciesId = speciesId)
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

data class TripUiState(
    val liveTrips: List<TripSummary> = emptyList(),
    val upcomingTrips: List<TripSummary> = emptyList(),
    val recentTrips: List<TripSummary> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface TripDetailsUiState {
    object Loading : TripDetailsUiState

    data class Success(
        val details: TripWithDetails,
        val summary: TripDetailedSummary,
        val eventSummaries: List<EventSummary> = emptyList()
    ) : TripDetailsUiState
}

class TripViewModelFactory(
    private val locationProvider: LocationProvider,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val photoRepository: PhotoRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(
                locationProvider,
                fishermanRepository,
                fishRepository,
                photoRepository,
                tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
