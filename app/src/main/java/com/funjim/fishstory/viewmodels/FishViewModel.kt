package com.funjim.fishstory.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.toDomain
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.FishFilter
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.sortedBy

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
    private val _filter = MutableStateFlow(FishFilter())
    val filter: StateFlow<FishFilter> = _filter.asStateFlow()

    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    private val _isReversed = MutableStateFlow(false)

    // Exposed State for the UI
    val speciesSummaries = fishRepo.speciesSummaries

    val sortOrder = _sortOrder.asStateFlow()
    val isReversed = _isReversed.asStateFlow()

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedBodyOfWater: StateFlow<BodyOfWater?> = _filter
        .map { it.bodyOfWaterId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getBodyOfWater(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEvent: StateFlow<Event?> = _filter
        .map { it.eventId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getEventById(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedFisherman: StateFlow<Fisherman?> = _filter
        .map { it.fishermanId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getFisherman(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedLure: StateFlow<LureWithColors?> = _filter
        .map { it.lureId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                lureRepo.getLureWithColors(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSpecies: StateFlow<Species?> = _filter
        .map { it.speciesId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getSpecies(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTrip: StateFlow<Trip?> = _filter
        .map { it.tripId }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                fishRepo.getTrip(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val fishSummary: StateFlow<FishSummary> = _filter
        .flatMapLatest { filter ->
            val flow1 = fishRepo.getFishCounts(filter)
            val flow2 = fishRepo.getTopTrip(filter)
            val flow3 = fishRepo.getTopEvent(filter)
            val flow4 = fishRepo.getTopFisherman(filter)
            val flow5 = fishRepo.getTopSpecies(filter)
            val flow6 = fishRepo.getTopLure(filter)

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
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FishSummary()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val bodiesOfWaterWithFish: StateFlow<List<BodyOfWater>> = _filter
        .flatMapLatest { filter ->
            fishRepo.getBodiesOfWater(filter)
        }.map { list ->
            list.sortedBy { it.name }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripsWithFish: StateFlow<List<Trip>> = _filter
        .flatMapLatest { filter ->
            fishRepo.getTrips(filter)
        }.map { list ->
            list.sortedByDescending { it.startDate }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithFish: StateFlow<List<Event>> = _filter
        .flatMapLatest { filter ->
            fishRepo.getEvents(filter)
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
    val fishermenWithFish: StateFlow<List<Fisherman>> = _filter
        .flatMapLatest { filter ->
            fishRepo.getFishermen(filter)
        }.map { list ->
            list.sortedBy { it.fullName }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val luresWithFish: StateFlow<List<LureWithColors>> = _filter
        .flatMapLatest { filter ->
            fishRepo.getLures(filter)
        }.map { list -> sortLures(list)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val speciesWithFish: StateFlow<List<Species>> = _filter
        .flatMapLatest { filter ->
            fishRepo.getSpecies(filter)
        }.map { list ->
            list.sortedBy { it.name }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishForScope: StateFlow<List<FishWithDetails>> = combine(
        _filter,
        _sortOrder,
        _isReversed
    ) { filter, sortOrder, isReversed ->
        Triple(filter, sortOrder, isReversed)
    }.flatMapLatest { (filter, sortOrder, isReversed) ->
        fishRepo.getFilteredFish(filter)
            .map { list -> applySorting(list, sortOrder, isReversed) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
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
                    // FIX LATER
//                    .thenBy { getColorsSortingString(it.lure?.primaryColors ?: emptyList()) }
//                    .thenBy { getColorsSortingString(it.lure?.secondaryColors ?: emptyList()) }
//                    .thenBy { getColorsSortingString(it.lure?.glowColors ?: emptyList()) }
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
        selectSpecies(null)
        selectTargetOnly(false)
    }

    fun selectBodyOfWater(id: String?) {
        _filter.update { it.copy(bodyOfWaterId = id) }
    }
    fun selectEvent(id: String?) {
        _filter.update { it.copy(eventId = id) }
    }
    fun selectFisherman(id: String?) {
        _filter.update { it.copy(fishermanId = id) }
    }
    fun selectLure(id: String?) {
        _filter.update { it.copy(lureId = id) }
    }
    fun selectSpecies(id: String?) {
        _filter.update { it.copy(speciesId = id) }
    }
    fun selectTrip(tripId: String?, eventId: String? = null) {
        _filter.update { it.copy(tripId = tripId, eventId = eventId) }
    }

    fun selectTargetOnly(targetOnly: Boolean) {
        _filter.update { it.copy(targetOnly = targetOnly) }
    }

    fun toggleReverse() { _isReversed.value = !_isReversed.value }
    fun updateSortOrder(order: FishSortOrder) { _sortOrder.value = order }

    private data class FishFilterParams(
        val bodyOfWaterId: String?,
        val eventId: String?,
        val fishermanId: String?,
        val lureId: String?,
        val speciesId: String?,
        val tripId: String?,
        val targetOnly: Boolean
    )

    private data class FishSortParams(
        val sortOrder: FishSortOrder,
        val isReversed: Boolean
    )

    suspend fun getFishById(id: String): Fish? {
        return fishRepo.getFish(id)?.toDomain()
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
