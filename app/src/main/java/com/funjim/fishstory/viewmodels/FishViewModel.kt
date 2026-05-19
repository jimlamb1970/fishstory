package com.funjim.fishstory.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import com.funjim.fishstory.ui.utils.sortLures
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
import kotlinx.coroutines.launch

class FishViewModel(
    private val locationProvider: LocationProvider,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    // UI State flows
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)
    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    private val _selectedTackleBoxId = MutableStateFlow<String?>(null)
    private val _selectedLureId = MutableStateFlow<String?>(null)

    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    private val _isReversed = MutableStateFlow(false)

    // Exposed State for the UI
    val species = fishRepo.allSpecies
    val speciesSummaries = fishRepo.speciesSummaries

    val selectedTripId = _selectedTripId.asStateFlow()
    val selectedEventId = _selectedEventId.asStateFlow()
    val selectedFishermanId = _selectedFishermanId.asStateFlow()
    val selectedTackleBoxId = _selectedTackleBoxId.asStateFlow()
    val selectedLureId = _selectedLureId.asStateFlow()

    val sortOrder = _sortOrder.asStateFlow()
    val isReversed = _isReversed.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTrip = _selectedTripId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEvent = _selectedEventId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getEventById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedFisherman = _selectedFishermanId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getFisherman(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedLure = _selectedLureId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getLure(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishSummary: StateFlow<FishSummary> = combine(
        _selectedTripId,
        _selectedEventId,
        _selectedFishermanId,
        _selectedLureId
    ) { tripId, eventId, fishermanId, lureId ->
        val flow1 = fishRepo.getFishCounts(tripId, eventId, fishermanId, lureId)
        val flow2 = fishRepo.getTopTrip(tripId, eventId, fishermanId, lureId)
        val flow3 = fishRepo.getTopEvent(tripId, eventId, fishermanId, lureId)

        // Combine 4-6
        val flow4 = fishRepo.getTopFisherman(tripId, eventId, fishermanId, lureId)
        val flow5 = fishRepo.getTopSpecies(tripId, eventId, fishermanId, lureId)
        val flow6 = fishRepo.getTopLure(tripId, eventId, fishermanId, lureId)

        combine(flow1, flow2, flow3) {
            c1, c2, c3 -> Triple(c1, c2, c3)
        }.combine(combine(flow4, flow5, flow6) {
            c4, c5, c6 -> Triple(c4, c5, c6)
        }) { t1, t2 ->
            FishSummary(
                counts = t1.first,
                topTrip = t1.second,
                topEvent = t1.third,
                topFisherman = t2.first,
                topSpecies = t2.second,
                topLure = t2.third
            )
        }
    }.flatMapLatest { it }
    .flowOn(Dispatchers.IO)
    .onEach { summary ->
        Log.d("FishSummaryDebug", "Summary emitted: for something $summary")
    }
    .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FishSummary()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripsWithFish: StateFlow<List<Trip>> = combine(
        _selectedFishermanId,
        _selectedLureId
    ) { fishermanId, lureId ->
        Pair(fishermanId, lureId)
    }.flatMapLatest { (fishermanId, lureId) ->
        fishRepo.getTrips(fishermanId, lureId)
    }.map { list ->
        list.sortedByDescending { it.startDate }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithFish: StateFlow<List<Event>> = combine(
        _selectedTripId,
        _selectedFishermanId,
        _selectedLureId
    ) { tripId, fishermanId, lureId ->
        Triple(tripId, fishermanId, lureId)
    }.flatMapLatest { (tripId, fishermanId, lureId) ->
        fishRepo.getEvents(tripId, fishermanId, lureId)
    }
    .map { list ->
        list.sortedBy { it.startTime }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermenWithFish: StateFlow<List<Fisherman>> = combine(
        _selectedTripId,
        _selectedEventId,
        _selectedLureId
    ) { tripId, eventId, lureId ->
        Triple(tripId, eventId, lureId)
    }.flatMapLatest { (tripId, eventId, lureId) ->
        fishRepo.getFishermen(tripId, eventId, lureId)
    }.map { list ->
        list.sortedBy { it.fullName }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val luresWithFish: StateFlow<List<LureWithColors>> = combine(
        _selectedTripId,
        _selectedEventId,
        _selectedFishermanId
    ) { tripId, eventId, fishermanId ->
        Triple(tripId, eventId, fishermanId)
    }.flatMapLatest { (tripId, eventId, fishermanId) ->
        fishRepo.getLures(tripId, eventId, fishermanId)
    }.map { list -> sortLures(list)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventFishermen: StateFlow<List<Fisherman>> = _selectedEventId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else tripRepo.getEventFishermen(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermanTackleBoxMap: StateFlow<Map<String, String?>> = _selectedEventId
        .flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyMap())
            else tripRepo.getFishermanTackleBoxMapping(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishForScope: StateFlow<List<FishWithDetails>> = combine(
        _selectedTripId,
        _selectedEventId,
        _selectedFishermanId,
        _selectedLureId
    ) { trip, seg, fish, lure ->
        // Helper to pass params
        FishFilterParams(trip, seg, fish, lure)
    }.combine(combine(_sortOrder, _isReversed) {
            sort, reversed -> FishSortParams(sort, reversed)
    }) { params, sort->
        fishRepo.getFilteredFish(params.tripId, params.eventId, params.fishermanId, params.lureId)
            .map { list -> applySorting(list, sort.sortOrder, sort.isReversed) }
    }.flatMapLatest {
        it
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun applySorting(list: List<FishWithDetails>, order: FishSortOrder, reversed: Boolean): List<FishWithDetails> {
        val sorted = when (order) {
            FishSortOrder.TIMESTAMP_NEWEST_FIRST -> list.sortedByDescending { it.fish.timestamp }
            FishSortOrder.LENGTH_LONGEST_FIRST -> list.sortedByDescending { it.fish.length }
            FishSortOrder.LURE -> list.sortedBy { it.fullLureName }
            FishSortOrder.SPECIES_AZ -> list.sortedBy { it.species.name ?: "" }
            FishSortOrder.FISHERMAN_AZ -> list.sortedBy { it.fisherman.fullName ?: "" }
            FishSortOrder.HOLE_NUMBER_ASC -> list.sortedBy { it.fish.holeNumber ?: 999 }
            else -> list
        }
        return if (reversed) sorted.reversed() else sorted
    }

    // UI Events
    fun clearSelections() {
        selectTrip(null)
        selectEvent(null)
        selectFisherman(null)
        selectLure(null)
        selectTackleBox(null)
    }

    fun selectTrip(id: String?) {
        _selectedTripId.value = id
        _selectedEventId.value = null // Reset event if trip changes
    }
    fun selectEvent(id: String?) {
        _selectedEventId.value = id
    }
    fun selectFisherman(id: String?) {
        _selectedFishermanId.value = id
    }
    fun selectTackleBox(id: String?) {
        _selectedTackleBoxId.value = id
    }
    fun selectLure(id: String?) {
        _selectedLureId.value = id
    }

    fun toggleReverse() { _isReversed.value = !_isReversed.value }
    fun updateSortOrder(order: FishSortOrder) { _sortOrder.value = order }

    private data class FishFilterParams(
        val tripId: String?,
        val eventId: String?,
        val fishermanId: String?,
        val lureId: String?
    )

    private data class FishSortParams(
        val sortOrder: FishSortOrder,
        val isReversed: Boolean
    )

    suspend fun getFishById(id: String): Fish? {
        return fishRepo.getFish(id)
    }

    fun upsertFish(fish: Fish) {
        viewModelScope.launch {
            fishRepo.upsertFish(fish)
        }
    }

    fun deleteFish(fish: Fish) {
        viewModelScope.launch {
            fishRepo.deleteFish(fish)
        }
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            fishRepo.addSpecies(species)
        }
    }

    fun upsertSpecies(species: Species) {
        viewModelScope.launch {
            fishRepo.upsertSpecies(species)
        }
    }

    fun deleteSpecies(species: Species) {
        viewModelScope.launch {
            fishRepo.deleteSpecies(species)
        }
    }

    fun eventThumbnail(eventId: String): Flow<ByteArray?> {
        return photoRepo.fetchEventThumbnail(eventId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun fishThumbnail(fishId: String): Flow<ByteArray?> {
        return photoRepo.fetchFishThumbnail(fishId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun fishermanThumbnail(fishermanId: String): Flow<ByteArray?> {
        return photoRepo.fetchFishermanThumbnail(fishermanId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun lureThumbnail(lureId: String): Flow<ByteArray?> {
        return photoRepo.fetchLureThumbnail(lureId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun tripThumbnail(tripId: String): Flow<ByteArray?> {
        return photoRepo.fetchTripThumbnail(tripId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun deleteSpeciesThumbnail(speciesId: String) {
        viewModelScope.launch {
            photoRepo.deleteSpeciesThumbnail(speciesId)
        }
    }

    fun updateSpeciesThumbnail(speciesId: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateSpeciesThumbnail(speciesId, uri)
        }
    }

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }
}

class FishViewModelFactory(
    private val locationProvider: LocationProvider,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishViewModel(
                locationProvider,
                fishRepo,
                lureRepo,
                photoRepo,
                tripRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
