package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
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
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.FishFilter
import com.funjim.fishstory.ui.utils.LocationProvider
import com.funjim.fishstory.ui.utils.sortLures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

class FishDetailsViewModel(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // UI State flows
    private val _filter = MutableStateFlow(FishFilter())
    val filter: StateFlow<FishFilter> = _filter.asStateFlow()

    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    private val _isReversed = MutableStateFlow(false)

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
    fun selectWater(id: String?) {
        _filter.update { it.copy(waterId = id) }
    }
    fun selectWeather(id: String?) {
        _filter.update { it.copy(weatherId = id) }
    }

    fun selectTargetOnly(targetOnly: Boolean) {
        _filter.update { it.copy(targetOnly = targetOnly) }
    }

    fun fishNotes(fishId: String): Flow<List<Note>> {
        return fishRepo.getNotesForFish(fishId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun addNote(fishId: String, noteId: String?, content: String) {
        viewModelScope.launch {
            if (noteId == null) {
                tripRepo.addNoteToFish(fishId, content)
            } else {
                tripRepo.updateNote(noteId, content)
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            tripRepo.deleteNote(noteId)
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
    fun setFishThumbnail(fishId: String, photoId: String) {
        viewModelScope.launch { photoRepo.setFishThumbnail(fishId, photoId) }
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

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }
}

class FishDetailsViewModelFactory(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishDetailsViewModel(
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
