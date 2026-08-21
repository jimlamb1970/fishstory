package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
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
import com.funjim.fishstory.ui.utils.CategoryType
import com.funjim.fishstory.ui.utils.LocationProvider
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripViewModel(
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
    val selectedTripId = _selectedTripId.asStateFlow()
    private val _selectedEventId = MutableStateFlow<String?>(null)

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _selectedCategory = MutableStateFlow(CategoryType.TARGET_SPECIES)
    val selectedCategory: StateFlow<CategoryType> = _selectedCategory.asStateFlow()

    fun onCategorySelected(category: CategoryType) {
        _selectedCategory.value = category
    }

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSkyConditions: StateFlow<List<SkyCondition>> = envRepo.allSkyConditions
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

    val allWaterClarity: StateFlow<List<WaterClarity>> = envRepo.allWaterClarity
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
    val bodyOfWaterSummaries: StateFlow<List<BodyOfWaterSummary>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList()) // This clears the map when you set id to null
            } else {
                fishRepo.getTripBodyOfWaterSummaries(tripId = id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermanSummaries: StateFlow<List<FishermanSummary>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList()) // Clear if either ID is missing
            } else {
                fishRepo.getTripFishermanSummaries(tripId = id)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )


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

    @OptIn(ExperimentalCoroutinesApi::class)
    val targetSpeciesSummaries: StateFlow<List<SpeciesSummary>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList()) // Clear if either ID is missing
            } else {
                fishRepo.getTripTargetSpeciesSummaries(tripId = id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @OptIn(ExperimentalCoroutinesApi::class)
    val waterSummaries: StateFlow<List<WaterSummary>> = _selectedTripId
        .flatMapLatest { id ->
            fishRepo.getWaterSummaries(tripId = id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val weatherSummaries: StateFlow<List<WeatherSummary>> = _selectedTripId
        .flatMapLatest { id ->
            fishRepo.getWeatherSummaries(tripId = id)
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

    fun bodyOfWaterThumbnail(bodyOfWaterId: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(bodyOfWaterId)
            .flowOn(Dispatchers.IO)
    }

    fun eventPhotos(eventId: String): Flow<List<Photo>> {
        return photoRepo.getPhotosForEvent(eventId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun eventThumbnail(eventId: String): Flow<ByteArray?> {
        return photoRepo.fetchEventThumbnail(eventId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun fishermanThumbnail(fishermanId: String): Flow<ByteArray?> {
        return photoRepo.fetchFishermanThumbnail(fishermanId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun skyConditionThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchSkyConditionThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun speciesSummary(tripId: String, speciesId: String): Flow<SpeciesSummary?> {
        return fishRepo.getSpeciesSummary(tripId = tripId, speciesId = speciesId)
    }

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }

    fun waterClarityThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchWaterClarityThumbnail(id).flowOn(Dispatchers.IO)
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

    fun addFisherman(
        fisherman: Fisherman,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishermanRepo.addFisherman(fisherman)
                onSuccess() // ONLY runs if addFisherman completes without throwing
            } catch (e: SQLiteConstraintException) {
                _toastMessage.emit("Fisherman already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding fisherman.")
            }
        }
    }

    fun addTripFisherman(
        fishermanId: String,
        tackleBoxId: String?
    ) {
        val tripId = _selectedTripId.value ?: return
        viewModelScope.launch {
            tripRepo.addTripFisherman(tripId, fishermanId, tackleBoxId)
        }
    }

    fun updateTripFisherman(
        fishermanId: String,
        tackleBoxId: String?
    ) {
        val tripId = _selectedTripId.value ?: return
        viewModelScope.launch {
            tripRepo.upsertTripFisherman(
                TripFisherman(tripId, fishermanId, tackleBoxId)
            )
        }
    }

    fun removeTripFisherman(fishermanId: String) {
        val tripId = _selectedTripId.value ?: return

        viewModelScope.launch {
            tripRepo.removeFishermanFromTrip(tripId, fishermanId)
        }
    }

    fun addFisherman(
        firstName: String,
        lastName: String,
        nickname: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val fisherman = fishermanRepo.getFishermanByName(firstName, lastName, nickname)
            if (fisherman == null) {
                val fisherman =
                    Fisherman(firstName = firstName, lastName = lastName, nickname = nickname)

                try {
                    fishermanRepo.addFisherman(fisherman)
                    onSuccess() // ONLY runs if addFisherman completes without throwing
                } catch (e: SQLiteConstraintException) {
                    _toastMessage.emit("Fisherman already exists.")
                } catch (e: Exception) {
                    _toastMessage.emit("An error occurred while adding fisherman.")
                }
            } else {
                _toastMessage.emit("Fisherman already exists.")
            }
        }
    }

    fun insertTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
        }
    }


    fun createAndAssignTackleBox(tackleBox: TackleBox) {
        val tripId = _selectedTripId.value ?: return
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
            tripRepo.upsertTripFisherman(
                TripFisherman(
                    tripId,
                    tackleBox.fishermanId,
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

    fun setTripThumbnail(tripId: String, photoId: String) {
        viewModelScope.launch { photoRepo.setTripThumbnail(tripId, photoId) }
    }

    fun deleteTripPhoto(tripId: String, photoId: String) {
        viewModelScope.launch { photoRepo.deleteTripPhoto(tripId, photoId) }
    }

    fun addEventPhoto(eventId: String, uri: Uri, selected: Boolean) {
        viewModelScope.launch {
            photoRepo.addEventPhoto(eventId, uri, selected)
                .onSuccess {  }
                .onFailure {  }
        }
    }

    fun setEventThumbnail(eventId: String, photoId: String) {
        viewModelScope.launch { photoRepo.setEventThumbnail(eventId, photoId) }
    }

    fun deleteEventPhoto(eventId: String, photoId: String) {
        viewModelScope.launch { photoRepo.deleteEventPhoto(eventId, photoId) }
    }

    fun addSpecies(
        item: Species,
        onSuccess: (Species) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishRepo.addSpecies(item)
                onSuccess(item)
            } catch (e: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Species '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding species.")
            }
        }
    }

    fun addTripTargetSpecies(tripId: String, species: Species) {
        addSpecies(species) { addedSpecies ->
            addTripTargetSpecies(tripId, addedSpecies.id)
        }
    }

    fun addTripTargetSpecies(tripId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.insertTripTargetSpecies(
                TripTargetSpecies(tripId = tripId, speciesId = speciesId)
            )
        }
    }

    fun removeTripTargetSpecies(tripId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.deleteTripTargetSpecies(tripId = tripId, speciesId = speciesId)
        }
    }

    // Functions for Body of Water manipulation
    fun addBodyOfWater(
        item: BodyOfWater,
        onSuccess: (BodyOfWater) -> Unit
    ) {
        viewModelScope.launch {
            try {
                envRepo.addBodyOfWater(item)
                onSuccess(item)
            } catch (e: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Body of water '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding body of water.")
            }
        }
    }

    fun addTripBodyOfWater(tripId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.insertTripBodyOfWater(
                TripBodyOfWater(tripId = tripId, bodyOfWaterId = bodyOfWaterId)
            )
        }
    }

    fun addTripBodyOfWater(tripId: String, bodyOfWater: BodyOfWater) {
        addBodyOfWater(bodyOfWater) { addedBodyOfWater ->
            addTripBodyOfWater(tripId, addedBodyOfWater.id)
        }
    }

    fun removeTripBodyOfWater(tripId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.deleteTripBodyOfWater(tripId = tripId, bodyOfWaterId = bodyOfWaterId)
        }
    }

    fun updateBodyOfWaterForTrip(tripId: String, newBodyOfWaterId: String?) {
        viewModelScope.launch {
            fishRepo.updateFishBodyOfWater(
                newBodyOfWaterId = newBodyOfWaterId,
                tripId = tripId
            )
        }
    }

    fun addWaterClarity(
        item: WaterClarity,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                envRepo.addWaterClarity(item)
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _toastMessage.emit("Water Clarity '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding water clarity.")
            }
        }
    }

    fun updateWater(water: Water) {
        viewModelScope.launch {
            envRepo.upsertWater(water)
        }
    }

    fun deleteWater(id: String) {
        viewModelScope.launch {
            envRepo.deleteWater(id = id)
        }
    }

    fun addSkyCondition(
        item: SkyCondition,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                envRepo.addSkyCondition(item)
                onSuccess()
            } catch (e: SQLiteConstraintException) {
                _toastMessage.emit("Sky Condition '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding sky condition.")
            }
        }
    }

    fun deleteWeather(id: String) {
        viewModelScope.launch {
            envRepo.deleteWeather(id = id)
        }
    }

    fun updateWeather(weather: Weather) {
        viewModelScope.launch {
            envRepo.upsertWeather(weather)
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
    private val environmentRepository: EnvironmentRepository,
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
                environmentRepository,
                fishermanRepository,
                fishRepository,
                photoRepository,
                tripRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
