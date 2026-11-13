package com.funjim.fishstory.viewmodels

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.funjim.fishstory.database.mapper.toSummary
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TackleBoxRepository
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class EventViewModel(
    private val locationProvider: LocationProvider,
    private val configRepo: ConfigurationRepository,
    private val envRepo: EnvironmentRepository,
    private val fishermanRepo: FishermanRepository,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tackleBoxRepo: TackleBoxRepository,
    private val tripRepo: TripRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(), LocationProvider by locationProvider {
    val appSettings: StateFlow<AppSettings> = configRepo.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val inputCategory: String? = savedStateHandle["category"]
    private val initialCategory: CategoryType = inputCategory?.let {
        try {
            CategoryType.valueOf(it)
        } catch (_: Exception) {
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

    val fishermen: Flow<List<Fisherman>> = fishermanRepo.allFishermen

    // --- Data Streams ---
    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return fishermanRepo.getTackleBoxesForFisherman(fishermanId)
    }

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
    val selectedEventDetailedSummary = _selectedEventId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                tripRepo.getEventDetailedSummary(id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val limitSummaries: StateFlow<List<LimitSummary>> = combine(
        _selectedEventId,
        _selectedTripId
    ) { eventId, tripId ->
        eventId to tripId
    }.flatMapLatest { (eventId, tripId) ->
        if (eventId.isNullOrBlank()) {
            flowOf(emptyList())
        } else {
            val eventLimitsFlow = fishRepo.getLimitsForEvent(eventId)
            val tripLimitsFlow = if (tripId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                fishRepo.getLimitsForTrip(tripId)
            }

            combine(
                eventLimitsFlow,
                tripLimitsFlow,
                fishermanSummaries
            ) { eventLimits, tripLimits, fishermen ->
                // Map limits to Scoped items
                val scopedEventLimits = eventLimits.map { it to LimitScope.EVENT }
                val scopedTripLimits = tripLimits.map { it to LimitScope.TRIP }

                val combinedScopedLimits = (scopedEventLimits + scopedTripLimits)
                    .distinctBy { (limit, _) -> limit.id }

                val numFishermen = fishermen.size.coerceAtLeast(1)

                combinedScopedLimits to numFishermen
            }.flatMapLatest { (scopedLimits, numFishermen) ->
                if (scopedLimits.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val countFlows = scopedLimits.map { (limit, scope) ->
                        fishRepo.getCaughtCountForLimit(eventId, limit).map { caught ->
                            val singleEventSummary = LimitEventSummary(
                                limit = limit,
                                event = null, // Or pass active event if available in scope
                                fishermanCount = numFishermen,
                                caughtCount = caught
                            )

                            LimitSummary(
                                limit = limit,
                                summaryList = listOf(singleEventSummary),
                                scope = scope
                            )
                        }
                    }
                    combine(countFlows) { summariesArray ->
                        summariesArray.toList()
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun getTackleBoxSummary(
        fishermanId: String
    ): Flow<TackleBoxSummary> {
        val eventId = _selectedEventId.value ?: return flowOf(
            TackleBoxSummary(
                tackleBox = null,
                lures = emptyList()
            )
        )

        return tackleBoxRepo.getEventTackleBoxSummary(
            fishermanId = fishermanId,
            eventId = eventId)
    }

    fun getLuresInTackleBox(
        fishermanId: String?,
        tackleBoxId: String?
    ): Flow<List<LureWithColorsSummary>> {
        val tripId = _selectedTripId.value ?: return flowOf(emptyList())
        val eventId = _selectedEventId.value ?: return flowOf(emptyList())

        return if (tackleBoxId == null) {
            lureRepo.getLureSummariesWithColors(
                tripId = tripId,
                eventId = eventId,
                fishermanId = fishermanId
            )
        } else {
            fishermanRepo.getLuresInTackleBox(tackleBoxId).map { list ->
                list.map { lureWithColors ->
                    lureWithColors.toSummary()
                }
            }
        }
    }

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLimits: StateFlow<List<LimitWithSpecies>> = fishRepo.getAllLimits()
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
            } catch (_: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Body of water '${item.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding body of water.")
            }
        }
    }
    fun addEventBodyOfWater(eventId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.insertBodyOfWaterForEvent(
                EventBodyOfWater(eventId = eventId, bodyOfWaterId = bodyOfWaterId)
            )
        }
    }
    fun addEventBodyOfWater(
        eventId: String,
        item: BodyOfWater,
        onSuccess: (BodyOfWater) -> Unit
    ) {
        addBodyOfWater(item) { added ->
            addEventBodyOfWater(eventId, added.id)
            onSuccess(added)
        }
    }
    fun addSpecies(
        item: Species,
        onSuccess: (Species) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishRepo.addSpecies(item)
                onSuccess(item)
            } catch (_: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Species '${item.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding species.")
            }
        }
    }
    fun addTargetSpeciesToEvent(eventId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.addTargetSpeciesToEvent(
                EventTargetSpecies(eventId = eventId, speciesId = speciesId)
            )
        }
    }
    fun addTargetSpeciesToEvent(
        eventId: String,
        item: Species,
        onSuccess: (Species) -> Unit
    ) {
        addSpecies(item) { added ->
            addTargetSpeciesToEvent(eventId, added.id)
            onSuccess(added)
        }
    }

    fun removeEventBodyOfWater(eventId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.deleteBodyOfWaterForEvent(eventId = eventId, bodyOfWaterId = bodyOfWaterId)
        }
    }

    fun removeEventTargetSpecies(eventId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.deleteEventTargetSpecies(eventId = eventId, speciesId = speciesId)
        }
    }

    // Thumbnail functions
    fun bodyOfWaterThumbnail(bodyOfWaterId: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(bodyOfWaterId)
            .flowOn(Dispatchers.IO)
    }
    fun eventThumbnail(): Flow<ByteArray?> {
        val eventId = _selectedEventId.value ?: return(flowOf(null))

        return photoRepo.fetchEventThumbnail(eventId)
            .flowOn(Dispatchers.IO)
    }
    fun fishermanThumbnail(fishermanId: String): Flow<ByteArray?> {
        return photoRepo.fetchFishermanThumbnail(fishermanId)
            .flowOn(Dispatchers.IO)
    }
    fun skyConditionThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchSkyConditionThumbnail(id)
            .flowOn(Dispatchers.IO)
    }
    fun getSpeciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }
    fun waterClarityThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchWaterClarityThumbnail(id)
            .flowOn(Dispatchers.IO)
    }

    fun deleteBodyOfWaterThumbnail(id: String) {
        viewModelScope.launch {
            photoRepo.deleteBodyOfWaterThumbnail(id)
        }
    }
    fun deleteSpeciesThumbnail(speciesId: String) {
        viewModelScope.launch {
            photoRepo.deleteSpeciesThumbnail(speciesId)
        }
    }
    fun setFishermanThumbnail(fishermanId: String, photoId: String) {
        viewModelScope.launch {
            photoRepo.setFishermanThumbnail(fishermanId, photoId)
        }
    }
    fun updateBodyOfWaterThumbnail(id: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateBodyOfWaterThumbnail(id, uri)
        }
    }
    fun updateSpeciesThumbnail(speciesId: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateSpeciesThumbnail(speciesId, uri)
        }
    }

    // Photo functions
    fun fishermanPhotos(fishermanId: String): Flow<List<Photo>> {
        return photoRepo.getPhotosForFisherman(fishermanId)
            .flowOn(Dispatchers.IO)
    }
    fun addFishermanPhoto(fishermanId: String, uri: Uri, selected: Boolean) {
        viewModelScope.launch {
            photoRepo.addFishermanPhoto(fishermanId, uri, selected)
                .onSuccess {  }
                .onFailure {  }
        }
    }
    fun deleteFishermanPhoto(fishermanId: String, photoId: String) {
        viewModelScope.launch {
            photoRepo.deleteFishermanPhoto(fishermanId, photoId)
        }
    }

    // --- Actions ---
    // Event functions
    fun deleteEvent() {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            tripRepo.deleteEventById(eventId)
        }
    }
    fun upsertEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.upsertEvent(event)
        }
    }

    // Limit functions
    fun addLimit(
        limit: LimitWithSpecies,
        onSuccess: (Boolean) -> Unit
    ) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            if (fishRepo.limitExists(limit)) {
                _toastMessage.emit("Limit '${limit.name}' already exists.")
                onSuccess(false)
            } else {
                fishRepo.addLimitToEvent(
                    eventId = eventId,
                    limit = limit
                )
                onSuccess(true)
            }
        }
    }

    fun addLimitToEvent(limit: LimitWithSpecies) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            fishRepo.addLimitToEvent(
                limitId = limit.id,
                eventId = eventId
            )
        }
    }

    fun removeLimitFromEvent(limit: LimitWithSpecies) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            fishRepo.removeLimitFromEvent(
                limitId = limit.id,
                eventId = eventId
            )
        }
    }

    // Note functions
    fun addNote(noteId: String?, content: String) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            if (noteId == null) {
                tripRepo.addNoteToEvent(eventId, content)
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

    // Water functions
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
            } catch (_: SQLiteConstraintException) {
                _toastMessage.emit("Water Clarity '${item.name}' already exists.")
            } catch (_: Exception) {
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

    // Weather functions
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
            } catch (_: SQLiteConstraintException) {
                _toastMessage.emit("Sky Condition '${item.name}' already exists.")
            } catch (_: Exception) {
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
            } catch (_: SQLiteConstraintException) {
                _toastMessage.emit("Fisherman '${fisherman.fullName}' already exists.")
            } catch (_: Exception) {
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

    fun createAndAssignTackleBox(
        tackleBox: TackleBox,
        onSuccess: (TackleBox) -> Unit
    ) {
        val eventId = _selectedEventId.value ?: return
        viewModelScope.launch {
            try {
                fishermanRepo.insertTackleBox(tackleBox)
                tripRepo.updateEventFisherman(
                    EventFisherman(
                        eventId,
                        tackleBox.fishermanId,
                        tackleBox.id
                    )
                )
                onSuccess(tackleBox)
            } catch (_: SQLiteConstraintException) {
                _toastMessage.emit("Tackle box '${tackleBox.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding tackle box.")
            }
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

    fun selectTrip(id: String) {
        _selectedTripId.value = id
    }

    fun selectEvent(id: String) {
        _selectedEventId.value = id
    }

    fun insertTackleBox(
        tackleBox: TackleBox,
        onSuccess: (TackleBox) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishermanRepo.insertTackleBox(tackleBox)
                onSuccess(tackleBox)
            } catch (_: SQLiteConstraintException) {
                _toastMessage.emit("Tackle box '${tackleBox.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding tackle box.")
            }
        }
    }

    fun toggleHelp() {
        viewModelScope.launch {
            configRepo.updateShowHelp(appSettings.value.display.showHelp.not())
        }
    }
    fun toggleImperialUnits() {
        viewModelScope.launch {
            configRepo.updateUseImperialUnits(appSettings.value.display.useImperialUnits.not())
        }
    }

    private val json = Json { prettyPrint = true }

    suspend fun exportDatabaseAsJson(context: Context, uri: Uri): Boolean {
        val eventId = _selectedEventId.value ?: return false
        val tripId = _selectedTripId.value ?: return false

        return withContext(Dispatchers.IO) {
            try {
                val trip = tripRepo.getTripById(tripId) ?: return@withContext false
                val event = tripRepo.getEventById(eventId) ?: return@withContext false

                val eventData = FishStoryEventData(
                    trip = trip,
                    event = event,
                    baits = lureRepo.getAllBaitsForEvent(eventId),
                    bodiesOfWater = envRepo.getAllBodiesOfWaterForEvent(eventId),
                    fishermen = fishermanRepo.getAllFishermenForEvent(eventId),
                    limits = fishRepo.getAllLimitsForEvent(eventId),
                    lures = lureRepo.getAllLuresForEvent(eventId),
                    lureColors = lureRepo.getAllLureColorsForEvent(eventId),
                    notes = tripRepo.getAllNotesForEvent(eventId),
                    species = fishRepo.getAllSpeciesForEvent(eventId),
                    skyConditions = envRepo.getAllSkyConditionsForEvent(eventId),
                    tackleBoxes = fishermanRepo.getAllTackleBoxesForEvent(eventId),
                    waterClarities = envRepo.getAllWaterClarityForEvent(eventId),
                    waterConditions = envRepo.getAllWaterConditionsForEvent(eventId),
                    weatherConditions = envRepo.getAllWeatherConditionsForEvent(eventId),
                    fish = fishRepo.getAllFishForEvent(eventId),
                    eventBaits = lureRepo.getEventBaitsForEvent(eventId),
                    eventBodiesOfWater = envRepo.getEventBodiesOfWaterForEvent(eventId),
                    eventFishermen = fishermanRepo.getEventFishermenForEvent(eventId),
                    eventLimits = fishRepo.getEventLimitsForEvent(eventId),
                    eventTargetSpecies = fishRepo.getEventTargetSpeciesForEvent(eventId),
                    limitSpecies = fishRepo.getLimitSpeciesForEvent(eventId),
                    lurePrimaryColors = lureRepo.getLurePrimaryColorsForEvent(eventId),
                    lureSecondaryColors = lureRepo.getLureSecondaryColorsForEvent(eventId),
                    lureGlowColors = lureRepo.getLureGlowColorsForEvent(eventId),
                    notesEvent = tripRepo.getEventNotesForEvent(eventId),
                    notesFish = tripRepo.getFishNotesForEvent(eventId),
                    tackleBoxLures = fishermanRepo.getTackleBoxLuresForEvent(eventId)
                )

                val jsonString = json.encodeToString(eventData)
                context.contentResolver.openOutputStream(uri)?.use { it.write(jsonString.toByteArray()) }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
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
    private val configurationRepository: ConfigurationRepository,
    private val environmentRepository: EnvironmentRepository,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val lureRepository: LureRepository,
    private val photoRepository: PhotoRepository,
    private val tacklerBoxRepository: TackleBoxRepository,
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
                configurationRepository,
                environmentRepository,
                fishermanRepository,
                fishRepository,
                lureRepository,
                photoRepository,
                tacklerBoxRepository,
                tripRepository,
                savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
