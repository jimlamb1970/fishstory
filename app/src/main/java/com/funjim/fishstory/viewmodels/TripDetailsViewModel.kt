package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.funjim.fishstory.database.entity.TackleBoxStatus
import com.funjim.fishstory.database.mapper.toSummary
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.NoteRepository
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

class TripDetailsViewModel(
    private val tripId: String,
    private val locationProvider: LocationProvider,
    private val configRepo: ConfigurationRepository,
    private val envRepo: EnvironmentRepository,
    private val fishermanRepo: FishermanRepository,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val noteRepo: NoteRepository,
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

    var pendingSkyConditionId: String? by mutableStateOf(null)
        private set
    var pendingWaterClarityId: String? by mutableStateOf(null)
        private set

    // --- (UI State) ---
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

    var showSmartTackleBoxPrompt by mutableStateOf(false)
        private set

    init {
        checkAndPromptForSmartTackleBox()
    }

    private fun checkAndPromptForSmartTackleBox() {
        viewModelScope.launch {
            val trip = tripRepo.getTripById(tripId) // Assuming you have a trip fetcher
            val currentTime = System.currentTimeMillis()

            if (trip != null && trip.endDate < currentTime) {
                if (trip.tackleBoxStatus == TackleBoxStatus.PENDING) {
                    val hasPhysicalBoxes = fishermanRepo.hasAssignedTackleBoxes(tripId)
                    if (hasPhysicalBoxes) {
                        showSmartTackleBoxPrompt = true
                    }
                }
            }
        }
    }

    fun onConfirmTackleBoxConversion(includeEvents: Boolean) {
        viewModelScope.launch {
            fishermanRepo.convertTripToSmartTackleBoxes(tripId, includeEvents)
            tripRepo.updateTripTackleBoxStatus(tripId, includeEvents, TackleBoxStatus.SMART)
            showSmartTackleBoxPrompt = false
        }
    }

    fun onDismissSmartTackleBoxPrompt(
        tackleBoxStatus: TackleBoxStatus,
        includeEvents: Boolean
    ) {
        if (tackleBoxStatus == TackleBoxStatus.ASSIGNED) {
            viewModelScope.launch {
                tripRepo.updateTripTackleBoxStatus(tripId, includeEvents, TackleBoxStatus.ASSIGNED)
            }
        }
        showSmartTackleBoxPrompt = false
    }

    fun onPendingSkyConditionId(id: String?) {
        pendingSkyConditionId = id
    }
    fun onPendingWaterClarityId(id: String?) {
        pendingWaterClarityId = id
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

    val allLimits: StateFlow<List<LimitWithSpecies>> = fishRepo.getAllLimits()
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

    // --- Data Streams ---
    val selectedTripDetailedSummary =
        tripRepo.getTripDetailedSummary(tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedTripWithDetails =
        tripRepo.getTripWithDetails(tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val fishermanSummaries: StateFlow<List<FishermanSummary>> =
        fishRepo.getTripFishermanSummaries(tripId = tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bodyOfWaterSummaries: StateFlow<List<BodyOfWaterSummary>> =
        fishRepo.getTripBodyOfWaterSummaries(tripId = tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventSummaries: StateFlow<List<EventSummary>> =
        tripRepo.getEventSummaries(tripId)
            .map { list ->
                list.sortedBy { it.event.startTime }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val limitSummaries: StateFlow<List<LimitSummary>> =
        combine(
            fishRepo.getLimitsForTrip(tripId),
            eventSummaries
        ) { tripLimits, events ->
            tripLimits to events
        }.flatMapLatest { (tripLimits, events) ->
            if (events.isEmpty()) {
                // There are no events in this trip, so just return the trip limits with
                // an empty list of event summaries
                val initialSummaries = tripLimits.map { limit ->
                    LimitSummary(
                        limit = limit,
                        summaryList = emptyList(),
                        scope = LimitScope.TRIP
                    )
                }
                flowOf(initialSummaries)
            } else {
                // First, for each event, we need to determine if there are limits
                // assigned specifically to each event
                val eventLimitsFlows: List<Flow<Pair<EventSummary, List<LimitWithSpecies>>>> = events.map { eventSummary ->
                    fishRepo.getLimitsForEvent(eventSummary.event.id).map { eventLimits ->
                        eventSummary to eventLimits
                    }
                }

                combine(eventLimitsFlows) { eventLimitsPairs ->
                    // This is list of all the limits at the trip level
                    val tripScoped = tripLimits.map { it to LimitScope.TRIP }

                    // For each event, there will be a list of limits at the event level
                    val eventScoped = eventLimitsPairs.flatMap { (eventSummary, limits) ->
                        limits.map { limit -> (limit to LimitScope.EVENT) to eventSummary }
                    }

                    // A list of unique limits will be all the event specific limits plus
                    // the trip specific limits
                    val uniqueLimits = (eventScoped.map { it.first } + tripScoped)
                        .distinctBy { (limit, _) -> limit.id }

                    // Build a map that will allow us to get the event summary for each limit
                    val eventLimitOwnerMap = eventScoped.associate { (limitScoped, eventSummary) ->
                        limitScoped.first.id to eventSummary
                    }

                    Triple(uniqueLimits, events, eventLimitOwnerMap)
                }.flatMapLatest { (uniqueLimits, allEvents, eventLimitOwnerMap) ->
                    if (uniqueLimits.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        // We now have to process each limit in turn
                        val parentLimitFlows: List<Flow<LimitSummary>> = uniqueLimits.map { (limit, scope) ->
                            // If it is a trip limit, we need to query every event in the
                            // trip to get the caught count for each event
                            if (scope == LimitScope.TRIP) {
                                val childFlows = allEvents.map { eventSummary ->
                                    fishRepo.getCaughtCountForLimit(eventSummary.event.id, limit).map { caught ->
                                        LimitEventSummary(
                                            limit = limit,
                                            event = eventSummary.event,
                                            fishermanCount = eventSummary.fishermanCount,
                                            caughtCount = caught
                                        )
                                    }
                                }

                                combine(childFlows) { summariesArray ->
                                    LimitSummary(
                                        limit = limit,
                                        summaryList = summariesArray.toList(),
                                        scope = LimitScope.TRIP
                                    )
                                }
                            } else {
                                // Otherwise, it is an event limit.  So, we just need to
                                // query the single event to get the caught count
                                val ownerEventSummary = eventLimitOwnerMap[limit.id]

                                // This should never happen -- but better safe than sorry
                                if (ownerEventSummary == null) {
                                    flowOf(
                                        LimitSummary(
                                            limit = limit,
                                            summaryList = emptyList(),
                                            scope = LimitScope.EVENT
                                        )
                                    )
                                } else {
                                    fishRepo.getCaughtCountForLimit(ownerEventSummary.event.id, limit).map { caught ->
                                        LimitSummary(
                                            limit = limit,
                                            summaryList = listOf(
                                                LimitEventSummary(
                                                    limit = limit,
                                                    event = ownerEventSummary.event,
                                                    fishermanCount = ownerEventSummary.fishermanCount,
                                                    caughtCount = caught
                                                )
                                            ),
                                            scope = LimitScope.EVENT
                                        )
                                    }
                                }
                            }
                        }

                        // Combine all the limit summaries into a single list
                        combine(parentLimitFlows) { summariesArray ->
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

    val waterSummaries: StateFlow<List<WaterSummary>> =
        fishRepo.getWaterSummaries(tripId = tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val targetSpeciesSummaries: StateFlow<List<SpeciesSummary>> =
        fishRepo.getTripTargetSpeciesSummaries(tripId = tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weatherSummaries: StateFlow<List<WeatherSummary>> =
        fishRepo.getWeatherSummaries(tripId = tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return fishermanRepo.getTackleBoxesForFisherman(fishermanId)
    }

    val tripTackleBoxMap: StateFlow<Map<String, String?>> =
        getTackleBoxMapForTrip(tripId = tripId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun getTackleBoxMapForTrip(tripId: String): Flow<Map<String, String?>> {
        return tripRepo.getTackleBoxMapForTrip(tripId)
    }

    // Body of Water Thumbnails and Photos
    fun deleteBodyOfWaterThumbnail(id: String) {
        viewModelScope.launch {
            photoRepo.deleteBodyOfWaterThumbnail(id)
        }
    }
    fun getBodyOfWaterThumbnail(bodyOfWaterId: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(bodyOfWaterId)
            .flowOn(Dispatchers.IO)
    }
    fun updateBodyOfWaterThumbnail(id: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateBodyOfWaterThumbnail(id, uri)
        }
    }
    // Event Thumbnails and Photos
    fun addEventPhoto(eventId: String, uri: Uri) {
        viewModelScope.launch {
            photoRepo.addEventPhoto(eventId, uri) {}
                .onSuccess {  }
                .onFailure {  }
        }
    }
    fun deleteEventPhoto(photo: EventPhoto) {
        viewModelScope.launch { photoRepo.deleteEventPhoto(photo) }
    }
    fun getEventPhotos(eventId: String): Flow<List<EventPhoto>> {
        return photoRepo.getPhotosForEvent(eventId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }
    fun getEventThumbnail(eventId: String): Flow<ByteArray?> {
        return photoRepo.fetchEventThumbnail(eventId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }
    fun setEventThumbnail(photo: EventPhoto) {
        viewModelScope.launch { photoRepo.setEventThumbnail(photo) }
    }
    // Fisherman Thumbnails and Photos
    fun addFishermanPhoto(fishermanId: String, uri: Uri) {
        viewModelScope.launch {
            photoRepo.addFishermanPhoto(fishermanId, uri) {}
                .onSuccess {  }
                .onFailure {  }
        }
    }
    fun deleteFishermanPhoto(photo: FishermanPhoto) {
        viewModelScope.launch { photoRepo.deleteFishermanPhoto(photo) }
    }
    fun getFishermanPhotos(fishermanId: String): Flow<List<FishermanPhoto>> {
        return photoRepo.getPhotosForFisherman(fishermanId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }
    fun getFishermanThumbnail(fishermanId: String): Flow<ByteArray?> {
        return photoRepo.fetchFishermanThumbnail(fishermanId)
            .flowOn(Dispatchers.IO) // Ensures DB work stays off main thread
    }
    fun setFishermanThumbnail(photo: FishermanPhoto) {
        viewModelScope.launch { photoRepo.setFishermanThumbnail(photo) }
    }

    // Sky Condition Thumbnails and Photos
    fun getSkyConditionThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchSkyConditionThumbnail(id).flowOn(Dispatchers.IO)
    }
    // Species Thumbnails
    fun deleteSpeciesThumbnail(speciesId: String) {
        viewModelScope.launch {
            photoRepo.deleteSpeciesThumbnail(speciesId)
        }
    }
    fun getSpeciesThumbnail(speciesId: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(speciesId)
            .flowOn(Dispatchers.IO)
    }
    fun updateSpeciesThumbnail(speciesId: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            photoRepo.updateSpeciesThumbnail(speciesId, uri)
        }
    }
    // Trip Thumbnails and Photos
    fun addTripPhoto(tripId: String, uri: Uri) {
        viewModelScope.launch {
            photoRepo.addTripPhoto(tripId, uri) {}
                .onSuccess {  }
                .onFailure {  }
        }
    }
    fun deleteTripPhoto(photo: TripPhoto) {
        viewModelScope.launch { photoRepo.deleteTripPhoto(photo) }
    }
    fun getTripThumbnail(): Flow<ByteArray?> {
        return photoRepo.fetchTripThumbnail(tripId)
            .flowOn(Dispatchers.IO)
    }
    fun setTripThumbnail(photo: TripPhoto) {
        viewModelScope.launch { photoRepo.setTripThumbnail(photo) }
    }

    // Water Thumbnails
    fun getWaterClarityThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchWaterClarityThumbnail(id).flowOn(Dispatchers.IO)
    }

    fun getTackleBoxSummary(
        fishermanId: String
    ): Flow<TackleBoxSummary> {
        return tackleBoxRepo.getTripTackleBoxSummary(
            fishermanId = fishermanId,
            tripId = tripId)
    }

    fun getLuresInTackleBox(
        fishermanId: String?,
        tackleBoxId: String?
    ): Flow<List<LureWithColorsSummary>> {
        return if (tackleBoxId == null) {
            lureRepo.getLureSummariesWithColors(
                tripId = tripId,
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

    fun addLimit(
        limit: LimitWithSpecies,
        onSuccess: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (fishRepo.limitExists(limit)) {
                _toastMessage.emit("Limit '${limit.name}' already exists.")
                onSuccess(false)
            } else {
                fishRepo.addLimitToTrip(
                    tripId = tripId,
                    limit = limit
                )
                onSuccess(true)
            }
        }
    }

    fun addLimitToTrip(limit: LimitWithSpecies) {
        viewModelScope.launch {
            fishRepo.addLimitToTrip(
                limitId = limit.id,
                tripId = tripId
            )
        }
    }
    fun removeLimitFromTrip(limit: LimitWithSpecies) {
        viewModelScope.launch {
            fishRepo.removeLimitFromTrip(limitId = limit.id, tripId = tripId)
        }
    }

    fun addNote(noteId: String?, content: String) {
        viewModelScope.launch {
            if (noteId == null) {
                noteRepo.addNoteToTrip(tripId, content)
            } else {
                noteRepo.updateTripNote(noteId, content)
            }
        }
    }
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepo.deleteTripNote(noteId)
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

    fun addTripFisherman(
        fishermanId: String,
        tackleBoxId: String?
    ) {
        viewModelScope.launch {
            tripRepo.addTripFisherman(tripId, fishermanId, tackleBoxId)
        }
    }

    fun updateTripFisherman(
        fishermanId: String,
        tackleBoxId: String?
    ) {
        viewModelScope.launch {
            tripRepo.upsertTripFisherman(
                TripFisherman(tripId, fishermanId, tackleBoxId)
            )
        }
    }

    fun removeTripFisherman(fishermanId: String) {
        viewModelScope.launch {
            fishermanRepo.removeFishermanFromTrip(
                fishermanId = fishermanId,
                tripId = tripId)
        }
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

    fun createAndAssignTackleBox(
        tackleBox: TackleBox,
        onSuccess: (TackleBox) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishermanRepo.insertTackleBox(tackleBox)
                tripRepo.upsertTripFisherman(
                    TripFisherman(
                        tripId,
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
    fun addTripTargetSpecies(
        tripId: String,
        item: Species,
        onSuccess: (Species) -> Unit
    ) {
        addSpecies(item) { added ->
            addTripTargetSpecies(tripId, added.id)
            onSuccess(added)
        }
    }

    fun addTripTargetSpecies(tripId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.addTargetSpeciesToTrip(
                TripTargetSpecies(tripId = tripId, speciesId = speciesId)
            )
        }
    }

    fun removeTripTargetSpecies(tripId: String, speciesId: String) {
        viewModelScope.launch {
            tripRepo.deleteTargetSpeciesFromTrip(tripId = tripId, speciesId = speciesId)
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
            } catch (_: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Body of water '${item.name}' already exists.")
            } catch (_: Exception) {
                _toastMessage.emit("An error occurred while adding body of water.")
            }
        }
    }

    fun addTripBodyOfWater(tripId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.insertBodyOfWaterForTrip(
                TripBodyOfWater(tripId = tripId, bodyOfWaterId = bodyOfWaterId)
            )
        }
    }

    fun addTripBodyOfWater(
        tripId: String,
        item: BodyOfWater,
        onSuccess: (BodyOfWater) -> Unit
    ) {
        addBodyOfWater(item) { added ->
            addTripBodyOfWater(tripId, added.id)
            onSuccess(added)
        }
    }

    fun removeTripBodyOfWater(tripId: String, bodyOfWaterId: String) {
        viewModelScope.launch {
            envRepo.deleteBodyOfWaterForTrip(tripId = tripId, bodyOfWaterId = bodyOfWaterId)
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

    fun deleteTrip() {
        viewModelScope.launch {
            tripRepo.deleteTripById(tripId)
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
    fun toggleShowAllCategories() {
        viewModelScope.launch {
            configRepo.updateShowAllCategories(appSettings.value.display.showAllCategories.not())
        }
    }
}

sealed interface TripDetailsUiState {
    object Loading : TripDetailsUiState

    data class Success(
        val details: TripWithDetails,
        val summary: TripDetailedSummary,
        val eventSummaries: List<EventSummary> = emptyList()
    ) : TripDetailsUiState
}

class TripDetailsViewModelFactory(
    private val tripId: String,
    private val locationProvider: LocationProvider,
    private val configRepository: ConfigurationRepository,
    private val environmentRepository: EnvironmentRepository,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val lureRepository: LureRepository,
    private val noteRepository: NoteRepository,
    private val photoRepository: PhotoRepository,
    private val tackleBoxRepository: TackleBoxRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(TripDetailsViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return TripDetailsViewModel(
                tripId,
                locationProvider,
                configRepository,
                environmentRepository,
                fishermanRepository,
                fishRepository,
                lureRepository,
                noteRepository,
                photoRepository,
                tackleBoxRepository,
                tripRepository,
                savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
