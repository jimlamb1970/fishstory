package com.funjim.fishstory.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FishViewModel(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    // UI State flows
    private val _selectedBodyOfWaterId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)
    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    private val _selectedTackleBoxId = MutableStateFlow<String?>(null)
    private val _selectedLureId = MutableStateFlow<String?>(null)
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _targetOnly = MutableStateFlow(false)

    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    private val _isReversed = MutableStateFlow(false)

    // Exposed State for the UI
    val speciesSummaries = fishRepo.speciesSummaries

    val selectedBodyOfWaterId = _selectedBodyOfWaterId.asStateFlow()
    val selectedEventId = _selectedEventId.asStateFlow()
    val selectedFishermanId = _selectedFishermanId.asStateFlow()
    val selectedTackleBoxId = _selectedTackleBoxId.asStateFlow()
    val selectedLureId = _selectedLureId.asStateFlow()
    val selectedTripId = _selectedTripId.asStateFlow()
    val targetOnly = _targetOnly.asStateFlow()

    val sortOrder = _sortOrder.asStateFlow()
    val isReversed = _isReversed.asStateFlow()

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedBodyOfWater = _selectedBodyOfWaterId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getBodyOfWater(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTrip = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getTrip(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEvent = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getEventById(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedFisherman = _selectedFishermanId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getFisherman(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedLure = _selectedLureId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                lureRepo.getLureWithColors(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishSummary: StateFlow<FishSummary> = combine(
        _selectedBodyOfWaterId,
        _selectedEventId,
        _selectedFishermanId,
        _selectedLureId,
        _selectedTripId
    ) { bodyOfWaterId, eventId, fishermanId, lureId, tripId ->
        val flow1 = fishRepo.getFishCounts(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId
        )

        val flow2 = fishRepo.getTopTrip(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId
        )

        val flow3 = fishRepo.getTopEvent(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId
        )

        // Combine 4-6
        val flow4 = fishRepo.getTopFisherman(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId
        )

        val flow5 = fishRepo.getTopSpecies(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId
        )

        val flow6 = fishRepo.getTopLure(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId
        )

        combine(flow1, flow2, flow3) { c1, c2, c3 ->
            Triple(c1, c2, c3)
        }.combine(combine(flow4, flow5, flow6) { c4, c5, c6 ->
            Triple(c4, c5, c6)
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
    val bodiesOfWaterWithFish: StateFlow<List<BodyOfWater>> = combine(
        _selectedEventId,
        _selectedFishermanId,
        _selectedLureId,
        _selectedTripId
    ) { eventId, fishermanId, lureId, tripId ->
        Quad(eventId, fishermanId, lureId, tripId)
    }.flatMapLatest { (eventId, fishermanId, lureId, tripId) ->
        fishRepo.getBodiesOfWater(
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)
    }.map { list ->
        list.sortedBy { it.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripsWithFish: StateFlow<List<Trip>> = combine(
        _selectedBodyOfWaterId,
        _selectedFishermanId,
        _selectedLureId
    ) { bodyOfWaterId, fishermanId, lureId ->
        Triple(bodyOfWaterId, fishermanId, lureId)
    }.flatMapLatest { (bodyOfWaterId, fishermanId, lureId) ->
        fishRepo.getTrips(
            bodyOfWaterId = bodyOfWaterId,
            fishermanId = fishermanId,
            lureId = lureId)
    }.map { list ->
        list.sortedByDescending { it.startDate }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithFish: StateFlow<List<Event>> = combine(
        _selectedBodyOfWaterId,
        _selectedFishermanId,
        _selectedLureId,
        _selectedTripId
    ) { bodyOfWaterId, fishermanId, lureId, tripId ->
        Quad(bodyOfWaterId, fishermanId, lureId, tripId)
    }.flatMapLatest { (bodyOfWaterId, fishermanId, lureId, tripId) ->
        fishRepo.getEvents(
            bodyOfWaterId = bodyOfWaterId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)
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
        _selectedBodyOfWaterId,
        _selectedEventId,
        _selectedLureId,
        _selectedTripId
    ) { bodyOfWaterId, eventId, lureId, tripId ->
        Quad(bodyOfWaterId, eventId, lureId, tripId)
    }.flatMapLatest { (bodyOfWaterId, eventId, lureId, tripId) ->
        fishRepo.getFishermen(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            lureId = lureId,
            tripId = tripId)
    }.map { list ->
        list.sortedBy { it.fullName }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val luresWithFish: StateFlow<List<LureWithColors>> = combine(
        _selectedBodyOfWaterId,
        _selectedEventId,
        _selectedFishermanId,
        _selectedTripId
    ) { bodyOfWaterId, eventId, fishermanId, tripId ->
        Quad(bodyOfWaterId, eventId, fishermanId, tripId)
    }.flatMapLatest { (bodyOfWaterId, eventId, fishermanId, tripId) ->
        fishRepo.getLures(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            tripId = tripId)
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
            else tripRepo.getTackleBoxMapForEvent(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishForScope: StateFlow<List<FishWithDetails>> = combine(
        _selectedBodyOfWaterId,
        _selectedEventId,
        _selectedFishermanId,
        _selectedLureId,
        _selectedTripId,
        _targetOnly
    ) { valuesArray ->
        // Helper to pass params
        val bodyOfWater = valuesArray[0] as String?
        val event = valuesArray[1] as String?
        val fish = valuesArray[2] as String?
        val lure = valuesArray[3] as String?
        val trip = valuesArray[4] as String?
        val targetOnly = valuesArray[5] as Boolean

        FishFilterParams(
            bodyOfWaterId = bodyOfWater,
            eventId = event,
            fishermanId = fish,
            lureId = lure,
            tripId = trip,
            targetOnly = targetOnly)
    }.combine(combine(_sortOrder, _isReversed) {
            sort, reversed -> FishSortParams(sort, reversed)
    }) { params, sort->
        fishRepo.getFilteredFish(
            bodyOfWaterId = params.bodyOfWaterId,
            eventId = params.eventId,
            fishermanId = params.fishermanId,
            lureId = params.lureId,
            tripId = params.tripId,
            targetOnly = params.targetOnly)
            .map { list -> applySorting(list, sort.sortOrder, sort.isReversed) }
    }.flatMapLatest {
        it
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun applySorting(list: List<FishWithDetails>, order: FishSortOrder, reversed: Boolean): List<FishWithDetails> {
        val getColorsSortingString = { colors: List<LureColor> ->
            colors.map { it.name }
                .sorted()
                .joinToString(separator = ",")
        }
        val sorted = when (order) {
            FishSortOrder.TIMESTAMP_NEWEST_FIRST -> list.sortedByDescending { it.fish.timestamp }
            FishSortOrder.TRIP_AZ -> list.sortedByDescending { it.trip.startDate }
            FishSortOrder.EVENT_AZ -> list.sortedByDescending { it.event.startTime }
            FishSortOrder.LENGTH_LONGEST_FIRST -> list.sortedByDescending { it.fish.length }
            FishSortOrder.SPECIES_AZ -> list.sortedBy { it.species.name ?: "" }
            FishSortOrder.FISHERMAN_AZ -> list.sortedBy { it.fisherman.fullName ?: "" }
            FishSortOrder.HOLE_NUMBER_ASC -> list.sortedBy { it.fish.holeNumber ?: 999 }
            FishSortOrder.KEPT -> list.sortedByDescending { it.fish.keptCount }

            FishSortOrder.LURE -> list.sortedWith(
                compareBy<FishWithDetails> { it.lure?.lure?.name }
                    .thenBy { getColorsSortingString(it.lure?.primaryColors ?: emptyList()) }
                    .thenBy { getColorsSortingString(it.lure?.secondaryColors ?: emptyList()) }
                    .thenBy { getColorsSortingString(it.lure?.glowColors ?: emptyList()) }
            )
        }
        return if (reversed) sorted.reversed() else sorted
    }

    // UI Events
    fun clearSelections() {
        selectBodyOfWater(null)
        selectTrip(null)
        selectEvent(null)
        selectFisherman(null)
        selectLure(null)
        selectTargetOnly(false)
        selectTackleBox(null)
    }

    fun selectBodyOfWater(id: String?) {
        _selectedBodyOfWaterId.value = id
    }
    fun selectTrip(tripId: String?, eventId: String? = null) {
        _selectedTripId.value = tripId
        _selectedEventId.value = eventId
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

    fun selectTargetOnly(targetOnly: Boolean) {
        _targetOnly.value = targetOnly
    }

    fun toggleReverse() { _isReversed.value = !_isReversed.value }
    fun updateSortOrder(order: FishSortOrder) { _sortOrder.value = order }

    private data class FishFilterParams(
        val bodyOfWaterId: String?,
        val eventId: String?,
        val fishermanId: String?,
        val lureId: String?,
        val tripId: String?,
        val targetOnly: Boolean
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

    fun fishPhotos(fishId: String): Flow<List<Photo>> {
        return photoRepo.getPhotosForFish(fishId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }


    fun addFishPhoto(fishId: String, uri: Uri, selected: Boolean) {
        viewModelScope.launch {
            photoRepo.addFishPhoto(fishId, uri, selected)
                .onSuccess {  }
                .onFailure {  }
        }
    }
    fun deleteFishPhoto(fishId: String, photoId: String) {
        viewModelScope.launch { photoRepo.deleteFishPhoto(fishId, photoId) }
    }

    fun baitThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchBaitThumbnail(id)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun bodyOfWaterThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(id)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
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

data class Quad<first, second, third, fourth>(
    val first: first,
    val second: second,
    val third: third,
    val fourth: fourth
)

class FishViewModelFactory(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
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
                envRepo,
                fishRepo,
                lureRepo,
                photoRepo,
                tripRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
