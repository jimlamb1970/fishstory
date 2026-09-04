package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishermanRepo: FishermanRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), LocationProvider by locationProvider {
    private val inputCategory: String? = savedStateHandle["category"]
    private val initialCategory: CategoryType = inputCategory?.let {
        try {
            CategoryType.valueOf(it)
        } catch (e: Exception) {
            null
        }
    } ?: CategoryType.TARGET_SPECIES

    private val _selectedCategory = MutableStateFlow(initialCategory)
    val selectedCategory: StateFlow<CategoryType> = _selectedCategory.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    fun onCategorySelected(category: CategoryType) {
        _selectedCategory.value = category
    }

    private val _showPhotos = MutableStateFlow(false)
    val showPhotos: StateFlow<Boolean> = _showPhotos.asStateFlow()
    fun toggleShowPhotos() {
        _showPhotos.value = !_showPhotos.value
    }

    // --- (UI State) ---
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)
    private val _eventCrewOverride = MutableStateFlow(false)

    val fishermen: Flow<List<Fisherman>> = fishermanRepo.allFishermen

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

    @OptIn(ExperimentalCoroutinesApi::class)
    val bodyOfWaterSummaries: StateFlow<List<BodyOfWaterSummary>> = _selectedEventId
        .flatMapLatest { eventId ->
            if (eventId == null) {
                flowOf(emptyList()) // This clears the map when you set id to null
            } else {
                fishRepo.getEventBodyOfWaterSummaries(eventId = eventId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishermanSummaries: StateFlow<List<FishermanSummary>> = _selectedEventId
        .flatMapLatest { eventId ->
            if (eventId == null) {
                flowOf(emptyList()) // Clear if either ID is missing
            } else {
                fishRepo.getEventFishermanSummaries(eventId = eventId)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val targetSpeciesSummaries: StateFlow<List<SpeciesSummary>> = _selectedEventId
        .flatMapLatest { eventId ->
            if (eventId == null) {
                flowOf(emptyList()) // Clear if either ID is missing
            } else {
                fishRepo.getEventTargetSpeciesSummaries(eventId = eventId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val waterSummaries: StateFlow<List<WaterSummary>> = _selectedEventId
        .flatMapLatest { eventId ->
            fishRepo.getWaterSummaries(eventId = eventId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val weatherSummaries: StateFlow<List<WeatherSummary>> = _selectedEventId
        .flatMapLatest { eventId ->
            fishRepo.getWeatherSummaries(eventId = eventId)
        }
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

    fun addEventBodyOfWater(eventId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.insertEventBodyOfWater(
                EventBodyOfWater(eventId = eventId, bodyOfWaterId = bodyOfWaterId)
            )
        }
    }

    fun addEventBodyOfWater(eventId: String, bodyOfWater: BodyOfWater) {
        addBodyOfWater(bodyOfWater) { addedBodyOfWater ->
            addEventBodyOfWater(eventId, addedBodyOfWater.id)
        }
    }

    fun addEventTargetSpecies(eventId: String, species: Species) {
        addSpecies(species) { addedSpecies ->
            addEventTargetSpecies(eventId, addedSpecies.id)
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

    fun fishermanThumbnail(fishermanId: String): Flow<ByteArray?> {
        return photoRepo.fetchFishermanThumbnail(fishermanId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }

    fun skyConditionThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchSkyConditionThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun speciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }

    fun waterClarityThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchWaterClarityThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun speciesSummary(eventId: String, speciesId: String): Flow<SpeciesSummary?> {
        return fishRepo.getSpeciesSummary(eventId = eventId, speciesId = speciesId)
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

    // --- Actions ---
    fun upsertEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.upsertEvent(event)
        }
    }

    fun addWater(water: Water) {
        viewModelScope.launch {
            envRepo.addWater(water)
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

    fun addWeather(weather: Weather) {
        viewModelScope.launch {
            envRepo.addWeather(weather)
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

    fun addFishermanToEvent(
        fishermanId: String,
        tackleBoxId: String? = null
    ) {
        val eventId = _selectedEventId.value ?: return
        val tripId = _selectedTripId.value ?: return

        viewModelScope.launch {
            val result = tripRepo.addFishermanToEventAndTrip(
                tripId = tripId,
                eventId = eventId,
                fishermanId = fishermanId,
                tackleBoxId = tackleBoxId
            )

            if (result.addedToTrip) {
                _toastMessage.emit("Fisherman was also added to the trip.")
            }
        }
    }

    fun createAndAssignTackleBox(tackleBox: TackleBox) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
            tripRepo.updateEventFisherman(
                EventFisherman(
                    eventId,
                    tackleBox.fishermanId,
                    tackleBox.id
                )
            )
        }
    }

    fun updateEventFisherman(
        fishermanId: String,
        tackleBoxId: String? = null
    ) {
        val eventId = _selectedEventId.value ?: return

        viewModelScope.launch {
            tripRepo.updateEventFisherman(
                EventFisherman(eventId, fishermanId, tackleBoxId)
            )
        }
    }

    fun deleteFishermanFromEvent(
        fishermanId: String
    ) {
        val eventId = _selectedEventId.value ?: return

        viewModelScope.launch {
            tripRepo.deleteEventFisherman(EventFisherman(eventId, fishermanId))
        }
    }

    fun deleteBodyOfWaterThumbnail(id: String) {
        viewModelScope.launch {
            photoRepo.deleteBodyOfWaterThumbnail(id)
        }
    }

    fun updateBodyOfWaterThumbnail(id: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateBodyOfWaterThumbnail(id, uri)
        }
    }

    fun eventThumbnail(): Flow<ByteArray?> {
        val eventId = _selectedEventId.value ?: return(flowOf(null))

        return photoRepo.fetchEventThumbnail(eventId)
            .flowOn(Dispatchers.IO)
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
        val summary: EventDetailedSummary
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
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(
                locationProvider,
                environmentRepository,
                fishermanRepository,
                fishRepository,
                photoRepository,
                tripRepository,
                savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
