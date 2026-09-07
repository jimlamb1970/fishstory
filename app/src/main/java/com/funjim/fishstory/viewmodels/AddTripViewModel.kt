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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.forEach
import kotlin.collections.map

class AddTripViewModel(
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

    private val _tripBodiesOfWater = MutableStateFlow<List<BodyOfWater>>(emptyList())
    val tripBodiesOfWater = _tripBodiesOfWater.asStateFlow()
    fun updateTripBodiesOfWater(bodyOfWater: BodyOfWater) {
        addBodyOfWater(bodyOfWater) { addedBodyOfWater ->
            _tripBodiesOfWater.update { it -> it + addedBodyOfWater }
        }
    }
    fun updateTripBodiesOfWater(ids: List<BodyOfWater>) {
        _tripBodiesOfWater.value = ids
    }

    private val _tripFishermen = MutableStateFlow<List<Fisherman>>(emptyList())
    val tripFishermen = _tripFishermen.asStateFlow()
    fun updateTripFishermen(item: Fisherman) {
        addFisherman(item) { addedItem ->
            _tripFishermen.update { it -> it + addedItem }
        }
    }
    fun updateTripFishermen(ids: List<Fisherman>) {
        _tripFishermen.value = ids
    }

    private val _tripTackleBoxMap = MutableStateFlow<Map<String, String?>>(emptyMap())
    val tripTackleBoxMap = _tripTackleBoxMap.asStateFlow()
    fun updateTripTackleBoxMap(map: Map<String, String?>) {
        _tripTackleBoxMap.value = map
    }

    private val _tripTargetSpecies = MutableStateFlow<List<Species>>(emptyList())
    val tripTargetSpecies = _tripTargetSpecies.asStateFlow()
    fun updateTripTargetSpecies(species: Species) {
        addSpecies(species) { addedSpecies ->
            _tripTargetSpecies.update { it -> it + addedSpecies }
        }
    }
    fun updateTripTargetSpecies(ids: List<Species>) {
        _tripTargetSpecies.value = ids
    }

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return fishermanRepo.getTackleBoxesForFisherman(fishermanId)
    }

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

    // --- Actions ---
    fun saveTrip() {
        val name = tripDraft.value.name.trim()
        val updatedDraft = tripDraft.value.copy(name = name)
        updateTripDraft { updatedDraft }

        viewModelScope.launch {
            try {
                tripRepo.upsertTrip(updatedDraft)

                persistBodiesOfWater()
                persistFishermen()
                persistTargetSpecies()
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while saving trip.")
            }
        }
    }

    fun saveTripWithEvent() {
        val name = tripDraft.value.name.trim()
        val updatedDraft = tripDraft.value.copy(name = name)
        updateTripDraft { updatedDraft }

        val event = Event(
            name = updatedDraft.name,
            tripId = updatedDraft.id,
            startTime = updatedDraft.startDate,
            endTime = updatedDraft.endDate,
            latitude = updatedDraft.latitude,
            longitude = updatedDraft.longitude,
            isLocked = true,
            isFavorite = false
        )

        viewModelScope.launch {
            try {
                tripRepo.upsertTrip(updatedDraft)
                tripRepo.upsertEvent(event)

                persistBodiesOfWater()
                persistFishermen()
                persistTargetSpecies()
            } catch (e: Exception) {
                _toastMessage.emit("An error occurred while saving trip.")
            }
        }
    }

    fun addFishermanToTrip(
        fishermanId: String,
        tackleBoxId: String? = null
    ) {
        viewModelScope.launch {
            tripRepo.addTripFisherman(
                tripId = tripDraft.value.id,
                fishermanId = fishermanId,
                tackleBoxId = tackleBoxId
            )
        }
    }

    fun persistBodiesOfWater() {
        _tripBodiesOfWater.value.forEach { item ->
            viewModelScope.launch {
                envRepo.insertTripBodyOfWater(
                    TripBodyOfWater(
                        tripId = tripDraft.value.id,
                        bodyOfWaterId = item.id)
                )
            }
        }
    }
    fun persistFishermen() {
        _tripFishermen.value.forEach { item ->
            addFishermanToTrip(item.id, _tripTackleBoxMap.value[item.id])
        }
    }
    fun persistTargetSpecies() {
        _tripTargetSpecies.value.forEach { species ->
            viewModelScope.launch {
                tripRepo.insertTripTargetSpecies(
                    TripTargetSpecies(
                        tripId = tripDraft.value.id,
                        speciesId = species.id
                    )
                )
            }
        }
    }

    // --- Trip Draft State ---
    private val _tripDraft = MutableStateFlow(Trip(id = UUID.randomUUID().toString(), name = ""))
    val tripDraft = _tripDraft.asStateFlow()

    fun updateTripDraft(update: (Trip) -> Trip) {
        _tripDraft.update(update)
    }
}

class AddTripViewModelFactory(
    private val locationProvider: LocationProvider,
    private val environmentRepository: EnvironmentRepository,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val photoRepository: PhotoRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTripViewModel(
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
