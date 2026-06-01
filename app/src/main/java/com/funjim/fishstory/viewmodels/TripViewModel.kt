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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?> {
        return tripRepo.getTripWithFishermen(tripId)
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

    fun addFisherman(firstName: String, lastName: String, nickname: String) {
        viewModelScope.launch {
            // Check if the fisherman already exists
            val fisherman = fishermanRepo.getFishermanByName(firstName, lastName, nickname)

            // If the fisherman does not exist, add the fisherman (this will also create a tackle box)
            if (fisherman == null) {
                val fisherman =
                    Fisherman(firstName = firstName, lastName = lastName, nickname = nickname)
                fishermanRepo.addFisherman(fisherman)
            }
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

    fun selectTrip(id: String) {
        _selectedTripId.value = id
    }

    fun selectEvent(id: String) {
        _selectedEventId.value = id
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
