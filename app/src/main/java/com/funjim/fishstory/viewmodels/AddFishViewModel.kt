package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoMetadata
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import com.funjim.fishstory.ui.utils.inchesToStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddFishViewModel(
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

    // Exposed State for the UI
    val species = fishRepo.allSpecies

    private val _lureColors = lureRepo.allLureColors
    val lureColors = _lureColors

    val selectedTripId = _selectedTripId.asStateFlow()
    val selectedEventId = _selectedEventId.asStateFlow()
    val selectedFishermanId = _selectedFishermanId.asStateFlow()
    val selectedTackleBoxId = _selectedTackleBoxId.asStateFlow()
    val selectedLureId = _selectedLureId.asStateFlow()

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
    val tackleBoxWithLures: StateFlow<List<Lure>> = _selectedTackleBoxId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                lureRepo.getLuresInTackleBox(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI Events
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

    private data class FilterParams(
        val tripId: String?,
        val eventId: String?,
        val fishermanId: String?,
        val sortOrder: FishSortOrder,
        val isReversed: Boolean
    )

    suspend fun getFishById(id: String): Fish? {
        return fishRepo.getFish(id)
    }

    suspend fun getFishWithPhotos(id: String): FishWithPhotos? {
        return fishRepo.getFishWithPhotos(id)
    }

    suspend fun getPhotoMetadata(uri: Uri): PhotoMetadata {
        return photoRepo.getPhotoMetadata(uri)
    }

    fun addFishPhotos(fishId: String, photos: List<Photo>) {
        viewModelScope.launch {
            photoRepo.addFishPhotos(fishId, photos)
        }
    }
    fun deleteFishPhotos(fishId: String, photos: List<Photo>) {
        viewModelScope.launch {
            photoRepo.deleteFishPhotos(fishId, photos)
        }
    }

    fun upsertFish(fish: Fish) {
        viewModelScope.launch {
            fishRepo.upsertFish(fish)
        }
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            fishRepo.addSpecies(species)
        }
    }

    // In your FishViewModel
    private val _draftFish = MutableStateFlow<Fish?>(null)
    val draftFish = _draftFish.asStateFlow()
    private val _fishPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val fishPhotos = _fishPhotos.asStateFlow()

    fun clearDraftFish() {
        _draftFish.value = null
        _fishPhotos.value = emptyList()
    }

    fun initDraftFish(
        fish: Fish?,
        tripId: String,
        eventId: String,
        photos: List<Photo> = emptyList()
    ) {
        // If fish is null, create a default "new" fish
        _draftFish.value = fish ?: Fish(
            id = UUID.randomUUID().toString(),
            speciesId = "",
            fishermanId = "",
            tripId = tripId,
            eventId = eventId,
            caughtCount = 1,
            length = (10.0).inchesToStorage(),
            timestamp = System.currentTimeMillis(),
            holeNumber = 1
        )
        _fishPhotos.value = photos
    }

    fun updateFisherman(fisherman: Fisherman) {
        _draftFish.update { current ->
            current?.copy(fishermanId = fisherman.id)
        }
    }

    fun updateHoleNumber(holeNumber: Int) {
        _draftFish.update { current ->
            current?.copy(holeNumber = holeNumber)
        }
    }
    fun updateLength(length: Long) {
        _draftFish.update { current ->
            current?.copy(length = length)
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _draftFish.update { current ->
            current?.copy(latitude = latitude, longitude = longitude)
        }
    }

    fun updateLure(lure: Lure?) {
        _draftFish.update { current ->
            current?.copy(lureId = lure?.id)
        }
    }

    fun updateKeptCount(kept: Int) {
        _draftFish.update { current ->
            current?.copy(keptCount = kept)
        }
    }

    fun updateSpecies(species: Species) {
        _draftFish.update { current ->
            current?.copy(speciesId = species.id)
        }
    }

    fun updateTimestamp(timestamp: Long, startTime: Long, endTime: Long) {
        _draftFish.update { current ->
            current?.copy(timestamp = timestamp.coerceIn(startTime, endTime))
        }
    }

    fun addPhoto(photo: Photo) {
        _fishPhotos.update { current -> current + photo }
    }

    fun deletePhoto(photo: Photo) {
        _fishPhotos.update { current -> current - photo }
    }

    fun updateDraftFish(transform: (Fish) -> Fish) {
        _draftFish.value?.let { current ->
            _draftFish.value = transform(current)
        }
    }
}

class AddFishViewModelFactory(
    private val locationProvider: LocationProvider,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddFishViewModel(
                locationProvider,
                fishRepo,
                lureRepo,
                photoRepo,
                tripRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
