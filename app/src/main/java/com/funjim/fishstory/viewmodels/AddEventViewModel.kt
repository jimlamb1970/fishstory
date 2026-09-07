package com.funjim.fishstory.viewmodels

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.map

class AddEventViewModel(
    private val locationProvider: LocationProvider,
    private val envRepo: EnvironmentRepository,
    private val fishermanRepo: FishermanRepository,
    private val fishRepo: FishRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository
) : ViewModel(), LocationProvider by locationProvider {
    private val _hasLocationPermission = MutableStateFlow(locationProvider.hasLocationPermission())
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // --- (UI State) ---
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)

    // --- Event Draft State ---
    private val _eventDraft = MutableStateFlow(Event(id = UUID.randomUUID().toString(), name = "", tripId = ""))
    val eventDraft = _eventDraft.asStateFlow()
    fun clearEventDraft() {
        _eventDraft.value = Event(id = UUID.randomUUID().toString(), name = "", tripId = "")
    }
    fun updateEventDraft(update: (Event) -> Event) {
        _eventDraft.update(update)
        _selectedEventId.value = _eventDraft.value.id
    }

    private val _eventBodiesOfWater = MutableStateFlow<List<BodyOfWater>>(emptyList())
    val eventBodiesOfWater = _eventBodiesOfWater.asStateFlow()
    fun updateEventBodiesOfWater(bodyOfWater: BodyOfWater) {
        addBodyOfWater(bodyOfWater) { addedBodyOfWater ->
            _eventBodiesOfWater.update { it -> it + addedBodyOfWater }
        }
    }
    fun updateEventBodiesOfWater(ids: List<BodyOfWater>) {
        _eventBodiesOfWater.value = ids
    }

    private val _eventFishermen = MutableStateFlow<List<Fisherman>>(emptyList())
    val eventFishermen = _eventFishermen.asStateFlow()
    fun updateEventFishermen(item: Fisherman) {
        addFisherman(item) { addedItem ->
            _eventFishermen.update { it -> it + addedItem }
        }
    }
    fun updateEventFishermen(ids: List<Fisherman>) {
        _eventFishermen.value = ids
    }

    private val _eventTackleBoxMap = MutableStateFlow<Map<String, String?>>(emptyMap())
    val eventTackleBoxMap = _eventTackleBoxMap.asStateFlow()
    fun updateEventTackleBoxMap(map: Map<String, String?>) {
        _eventTackleBoxMap.value = map
    }

    private val _eventTargetSpecies = MutableStateFlow<List<Species>>(emptyList())
    val eventTargetSpecies = _eventTargetSpecies.asStateFlow()
    fun updateEventTargetSpecies(species: Species) {
        addSpecies(species) { addedSpecies ->
            _eventTargetSpecies.update { it -> it + addedSpecies }
        }
    }
    fun updateEventTargetSpecies(ids: List<Species>) {
        _eventTargetSpecies.value = ids
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

    fun persistBodiesOfWater() {
        _eventBodiesOfWater.value.forEach { item ->
            viewModelScope.launch {
                envRepo.insertEventBodyOfWater(
                    EventBodyOfWater(
                        eventId = eventDraft.value.id,
                        bodyOfWaterId = item.id)
                )
            }
        }
    }
    fun persistFishermen() {
        _eventFishermen.value.forEach { item ->
            addFishermanToEvent(item.id, _eventTackleBoxMap.value[item.id])
        }
    }
    fun persistTargetSpecies() {
        _eventTargetSpecies.value.forEach { species ->
            viewModelScope.launch {
                tripRepo.insertEventTargetSpecies(
                    EventTargetSpecies(
                        eventId = eventDraft.value.id,
                        speciesId = species.id
                    )
                )
            }
        }
    }

    // --- Data Streams ---
    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return fishermanRepo.getTackleBoxesForFisherman(fishermanId)
    }

    fun getLureCountForTackleBox(tackleBoxId: String?): Flow<Int> {
        return fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "").map { it.size }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripTackleBoxMap: StateFlow<Map<String, String?>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyMap()) // This clears the map when you set id to null
            } else {
                tripRepo.getTackleBoxMapForTrip(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

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

    fun getLuresInTackleBox(tackleBoxId: String?): Flow<List<LureWithColors>> {
        return fishermanRepo.getLuresInTackleBox(tackleBoxId ?: "")
    }

    val allBodiesOfWater: StateFlow<List<BodyOfWater>> = envRepo.allBodiesOfWater
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val allFishermen: StateFlow<List<Fisherman>> = fishermanRepo.allFishermen
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

    val uiAddEventState: StateFlow<AddEventUiState> = combine(
        selectedTrip,
        tripTackleBoxMap,
        _eventDraft
    ) { trip, tackleBoxMap, event ->
        if (trip != null) {
            val ids = trip.fishermen.map { it.id }.toSet()
            _tripFishermanIds.value = ids

            AddEventUiState.Success(
                trip = trip,
                tripTackleBoxMap = tackleBoxMap,
                event = event
            )
        } else {
            AddEventUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddEventUiState.Loading
    )

    fun bodyOfWaterThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchBodyOfWaterThumbnail(id)
            .flowOn(Dispatchers.IO)
    }
    fun fishermanThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchFishermanThumbnail(id)
            .flowOn(Dispatchers.IO)
    }
    fun speciesThumbnail(id: String): Flow<ByteArray?> {
        return photoRepo.fetchSpeciesThumbnail(id)
            .flowOn(Dispatchers.IO)
    }

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
                _toastMessage.emit("Body of Water '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding body of water.")
            }
        }
    }
    fun addFisherman(
        item: Fisherman,
        onSuccess: (Fisherman) -> Unit
    ) {
        viewModelScope.launch {
            try {
                fishermanRepo.addFisherman(item)
                onSuccess(item)
            } catch (e: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Fisherman '${item.fullName}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding fisherman.")
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
            } catch (e: SQLiteConstraintException) {
                // Catches duplicate UNIQUE constraint failures
                _toastMessage.emit("Species '${item.name}' already exists.")
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while adding species.")
            }
        }
    }

    // --- Actions ---
    fun saveEvent(
    ) {
        val name = eventDraft.value.name.trim()
        updateEventDraft { eventDraft.value.copy(name = name) }

        persistEvent(eventDraft.value)
        persistBodiesOfWater()
        persistFishermen()
        persistTargetSpecies()
    }

    fun persistEvent(event: Event) {
        viewModelScope.launch {
            tripRepo.upsertEvent(event)
        }
    }

    fun createAndAssignEventTackleBox(fishermanId: String, eventId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            fishermanRepo.insertTackleBox(tackleBox)
            tripRepo.updateEventFisherman(
                EventFisherman(
                    eventId,
                    fishermanId,
                    tackleBox.id
                )
            )
        }
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

    // --- Crew Draft State ---
    private val _tripFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val tripFishermenIds = _tripFishermanIds.asStateFlow()

    private val _eventFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val eventFishermenIds = _eventFishermanIds.asStateFlow()

    fun toggleEventFisherman(id: String) {
        _eventFishermanIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun updateEventFishermanIds(ids: Set<String>) {
        _eventFishermanIds.value = ids
    }

    fun insertTackleBox(tackleBox: TackleBox) {
        viewModelScope.launch {
            fishermanRepo.insertTackleBox(tackleBox)
        }
    }
}

sealed interface AddEventUiState {
    object Loading : AddEventUiState

    data class Success(
        val trip: TripWithInfo,
        val tripTackleBoxMap: Map<String, String?>,
        val event: Event
    ) : AddEventUiState
}

class AddEventViewModelFactory(
    private val locationProvider: LocationProvider,
    private val environmentRepository: EnvironmentRepository,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val photoRepository: PhotoRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEventViewModel(
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
