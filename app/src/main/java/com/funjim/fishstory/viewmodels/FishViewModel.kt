package com.funjim.fishstory.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.TripRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FishViewModel(
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val tripRepo: TripRepository
) : ViewModel() {
    // UI State flows
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedEventId = MutableStateFlow<String?>(null)
    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    private val _selectedTackleBoxId = MutableStateFlow<String?>(null)

    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    private val _isReversed = MutableStateFlow(false)

    // Exposed State for the UI
    val trips = fishRepo.allTrips
    val species = fishRepo.allSpecies
    val speciesSummaries = fishRepo.speciesSummaries

    private val _lureColors = lureRepo.allLureColors
    val lureColors = _lureColors

    val fishPhotos = fishRepo.fishPhotos.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val selectedTripId = _selectedTripId.asStateFlow()
    val selectedEventId = _selectedEventId.asStateFlow()
    val selectedFishermanId = _selectedFishermanId.asStateFlow()
    val selectedTackleBoxId = _selectedFishermanId.asStateFlow()

    val sortOrder = _sortOrder.asStateFlow()
    val isReversed = _isReversed.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTrip = _selectedTripId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tripEvents: StateFlow<List<Event>> = _selectedTripId
        .flatMapLatest { tripId ->
            if (tripId == null) {
                flowOf(emptyList())
            } else {
                fishRepo.getEventsForTrip(tripId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedEvent = _selectedEventId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getEventById(id) }
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
    val selectedFisherman = _selectedFishermanId
        .filterNotNull()
        .flatMapLatest { id -> fishRepo.getFisherman(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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


    @OptIn(ExperimentalCoroutinesApi::class)
    val fishForScope: StateFlow<List<FishWithDetails>> = combine(
        _selectedTripId,
        _selectedEventId,
        _selectedFishermanId,
        _sortOrder,
        _isReversed
    ) { trip, seg, fish, sort, rev ->
        // Helper to pass params
        FilterParams(trip, seg, fish, sort, rev)
    }.flatMapLatest { params ->
        fishRepo.getFilteredFish(params.tripId, params.eventId, params.fishermanId)
            .map { list -> applySorting(list, params.sortOrder, params.isReversed) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun applySorting(list: List<FishWithDetails>, order: FishSortOrder, reversed: Boolean): List<FishWithDetails> {
        val sorted = when (order) {
            FishSortOrder.TIMESTAMP_NEWEST_FIRST -> list.sortedByDescending { it.timestamp }
            FishSortOrder.LENGTH_LONGEST_FIRST -> list.sortedByDescending { it.length }
            FishSortOrder.LURE -> list.sortedBy { it.fullLureName }
            FishSortOrder.SPECIES_AZ -> list.sortedBy { it.speciesName }
            FishSortOrder.FISHERMAN_AZ -> list.sortedBy { it.fishermanName }
            FishSortOrder.HOLE_NUMBER_ASC -> list.sortedBy { it.holeNumber ?: 999 }
            else -> list
        }
        return if (reversed) sorted.reversed() else sorted
    }

    // UI Events
    fun updateSelectedTrip(id: String?) {
        _selectedTripId.value = id
        _selectedEventId.value = null // Reset event if trip changes
    }
    fun updateSelectedEvent(id: String?) {
        _selectedEventId.value = id
    }

    fun updateSelectedFisherman(id: String?) {
        _selectedFishermanId.value = id
    }

    fun updateSelectedTackleBox(id: String?) {
        _selectedTackleBoxId.value = id
    }

    fun toggleReverse() { _isReversed.value = !_isReversed.value }
    fun updateSortOrder(order: FishSortOrder) { _sortOrder.value = order }

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

    private val _deviceLocation = MutableStateFlow<android.location.Location?>(null)
    val deviceLocation = _deviceLocation.asStateFlow()

    @androidx.annotation.RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    suspend fun getFishCurrentLocation(context: Context): android.location.Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    fun fetchDeviceLocationOnce(context: Context) {
        if (_deviceLocation.value != null) return

        // 1. Explicitly check if permissions are granted
        val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        // 2. Only launch the coroutine if at least one is granted
        if (hasFineLocation || hasCoarseLocation) {
            viewModelScope.launch {
                try {
                    _deviceLocation.value = getFishCurrentLocation(context)
                } catch (e: SecurityException) {
                    // Handle the case where permission was revoked mid-flight
                    _deviceLocation.value = null
                }
            }
        } else {
            // 3. Optional: Trigger a UI event to ask the user for permission
            println("Location permission not granted")
        }
    }

    // In your FishViewModel
    private val _draftFish = MutableStateFlow<Fish?>(null)
    val draftFish = _draftFish.asStateFlow()

    fun clearDraftFish() {
        _draftFish.value = null
    }

    fun initDraftFish(fish: Fish?, tripId: String, eventId: String) {
        // If fish is null, create a default "new" fish
        _draftFish.value = fish ?: Fish(
            id = UUID.randomUUID().toString(),
            speciesId = null,
            fishermanId = null,
            tripId = tripId,
            eventId = eventId,
            length = 10.0,
            timestamp = System.currentTimeMillis(),
            holeNumber = 1
        )
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
    fun updateLength(length: Double) {
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

    fun updateReleased(released: Boolean) {
        _draftFish.update { current ->
            current?.copy(isReleased = released)
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

    fun updateDraftFish(transform: (Fish) -> Fish) {
        _draftFish.value?.let { current ->
            _draftFish.value = transform(current)
        }
    }
}

class FishViewModelFactory(
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val tripRepo: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishViewModel(fishRepo, lureRepo, tripRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
