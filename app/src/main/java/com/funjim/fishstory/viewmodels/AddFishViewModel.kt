package com.funjim.fishstory.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoMetadata
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProvider
import com.funjim.fishstory.ui.utils.inchesToStorage
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddFishViewModel(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    // UI State flows
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)
    private val _selectedFishId = MutableStateFlow<String?>(null)
    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    private val _selectedTackleBoxId = MutableStateFlow<String?>(null)
    private val _selectedLureId = MutableStateFlow<String?>(null)

    // Exposed State for the UI
    val species = fishRepo.allSpecies

    val selectedTripId = _selectedTripId.asStateFlow()
    val selectedEventId = _selectedEventId.asStateFlow()
    val selectedFishermanId = _selectedFishermanId.asStateFlow()
    val selectedTackleBoxId = _selectedTackleBoxId.asStateFlow()
    val selectedLureId = _selectedLureId.asStateFlow()

    val allBaits: StateFlow<List<Bait>> = lureRepo.allBaits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTrip = _selectedTripId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEvent = _selectedEventId
        .filterNotNull()
        .flatMapLatest { id -> tripRepo.getEventWithInfo(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedFish = _selectedFishId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getFishWithPhotos(id) }
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
        .flatMapLatest { eventId ->
            if (eventId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                tripRepo.getEventFishermen(eventId).flatMapLatest { eventCrew ->
                    if (eventCrew.isNotEmpty()) {
                        flowOf(eventCrew)
                    } else {
                        _selectedTripId.flatMapLatest { tripId ->
                            if (tripId.isNullOrBlank()) flowOf(emptyList())
                            else tripRepo.getTripFishermen(tripId)
                        }
                    }
                }
            }
        }.map { list ->
            list.sortedBy { it.fullName }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermanTackleBoxMap: StateFlow<Map<String, String?>> = _selectedEventId
        .flatMapLatest { eventId ->
            if (eventId.isNullOrBlank()) {
                flowOf(emptyMap())
            } else {
                tripRepo.getTackleBoxMapForEvent(eventId).flatMapLatest { eventMap ->
                    if (eventMap.isNotEmpty()) {
                        flowOf(eventMap)
                    } else {
                        _selectedTripId.flatMapLatest { tripId ->
                            if (tripId.isNullOrBlank()) flowOf(emptyMap())
                            else tripRepo.getTackleBoxMapForTrip(tripId)
                        }
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tackleBoxWithLures: StateFlow<List<LureWithColors>> = _selectedTackleBoxId
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

    private data class CoreParams(
        val trip: Trip?,
        val event: EventWithInfo?,
        val species: List<Species>,
        val fishermen: List<Fisherman>,
        val tackleBoxMap: Map<String, String?>
    )

    private data class FishParams(
        val fish: FishWithPhotos?,
        val fishId: String?
    )

    val uiState: StateFlow<AddFishUiState> = combine(
        selectedTrip,
        selectedEvent,
        species,
        eventFishermen,
        fishermanTackleBoxMap
    ) { trip, event, species, fishermen, tackleBoxMap ->
        // Helper to pass params
        CoreParams(trip, event, species, fishermen, tackleBoxMap)
    }.combine(combine(selectedFish, _selectedFishId) { fish, fishId ->
        FishParams(fish, fishId)
    }) { core, fish ->
        val isCoreDataLoaded = core.trip != null && core.event != null

        val isFishLoaded = fish.fishId == null || fish.fish != null

        if (isCoreDataLoaded && isFishLoaded) {
            val eventSpeciesIds = core.event.targetSpecies.map { it.id }.toSet()
            val sortedSpecies = core.species.sortedByDescending { species ->
                eventSpeciesIds.contains(species.id)
            }

            updateOriginalFish(fish.fish)
            AddFishUiState.Success(
                    trip = core.trip,
                    event = core.event,
                    species = sortedSpecies,
                    fishermen = core.fishermen,
                    tackleBoxMap = core.tackleBoxMap,
                    fish = fish.fish)
        } else {
            AddFishUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddFishUiState.Loading
    )

    // UI Events
    fun selectTrip(id: String?) {
        _selectedTripId.value = id
        _selectedEventId.value = null // Reset event if trip changes
    }
    fun selectEvent(id: String?) {
        _selectedEventId.value = id
    }
    fun selectFish(id: String?) {
        _selectedFishId.value = id
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

    fun addBait(bait: Bait) {
        viewModelScope.launch {
            lureRepo.addBait(bait)
        }
    }

    fun addBodyOfWater(bodyOfWater: BodyOfWater) {
        viewModelScope.launch {
            envRepo.addBodyOfWater(bodyOfWater)
        }
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            fishRepo.addSpecies(species)
        }
    }

    // In your FishViewModel
    private val _originalFish = MutableStateFlow<FishWithPhotos?>(null)
    val originalFish = _originalFish.asStateFlow()
    fun updateOriginalFish(fish: FishWithPhotos?) {
        _originalFish.value = fish
    }
    private val _draftFish = MutableStateFlow<Fish?>(null)
    val draftFish = _draftFish.asStateFlow()
    private val _fishPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val fishPhotos = _fishPhotos.asStateFlow()

    val hasChanges: Flow<Boolean> = combine(
        originalFish, draftFish, fishPhotos
    ) { original, draft, photos ->
        if (original == null) {
            !draft?.speciesId.isNullOrBlank() && !draft.fishermanId.isBlank()
        } else {
            draft != original.fish || photos != original.photos
        }
    }

    fun baitThumbnail(baitId: String): Flow<ByteArray?> {
        return photoRepo.fetchBaitThumbnail(baitId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun bodyOfWaterThumbnail(bodyOfWaterId: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(bodyOfWaterId)
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

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }

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

    fun updateBait(bait: Bait) {
        _draftFish.update { current ->
            current?.copy(baitId = bait.id)
        }
    }

    fun updateBodyOfWater(bodyOfWater: BodyOfWater) {
        _draftFish.update { current ->
            current?.copy(bodyOfWaterId = bodyOfWater.id)
        }
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

    fun updateLure(lure: LureWithColors?) {
        _draftFish.update { current ->
            current?.copy(lureId = lure?.lure?.id)
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

sealed interface AddFishUiState {
    object Loading : AddFishUiState

    data class Success(
        val trip: Trip,
        val event: EventWithInfo,
        val species: List<Species> = emptyList(),
        val fishermen: List<Fisherman> = emptyList(),
        val tackleBoxMap: Map<String, String?> = emptyMap(),
        val fish: FishWithPhotos? = null
    ) : AddFishUiState
}

class AddFishViewModelFactory(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
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
                envRepo,
                fishRepo,
                lureRepo,
                photoRepo,
                tripRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
