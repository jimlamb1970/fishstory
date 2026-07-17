package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
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
    private val envRepo: EnvironmentRepository,
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

    // --- Data Streams ---
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

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    fun addBodyOfWater(bodyOfWater: BodyOfWater) {
        viewModelScope.launch {
            envRepo.addBodyOfWater(bodyOfWater)
        }
    }

    fun addEventBodyOfWater(eventId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.insertEventBodyOfWater(
                EventBodyOfWater(eventId = eventId, bodyOfWaterId = bodyOfWaterId)
            )
        }
    }

    fun addEventTargetSpecies(eventId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.insertEventTargetSpecies(
                EventTargetSpecies(eventId = eventId, speciesId = speciesId)
            )
        }
    }

    fun removeEventBodyOfWater(eventId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.deleteEventBodyOfWater(eventId = eventId, bodyOfWaterId = bodyOfWaterId)
        }
    }

    fun removeEventTargetSpecies(eventId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.deleteEventTargetSpecies(eventId = eventId, speciesId = speciesId)
        }
    }

    fun bodyOfWaterThumbnail(bodyOfWaterId: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(bodyOfWaterId)
            .flowOn(Dispatchers.IO)
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

    suspend fun updateBodyOfWaterForEvent(eventId: String, newBodyOfWaterId: String?) {
        fishRepo.updateFishBodyOfWater(
            newBodyOfWaterId = newBodyOfWaterId,
            eventId = eventId
        )
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
    private val environmentRepository: EnvironmentRepository,
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
                environmentRepository,
                fishermanRepository,
                fishRepository,
                photoRepository,
                tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
