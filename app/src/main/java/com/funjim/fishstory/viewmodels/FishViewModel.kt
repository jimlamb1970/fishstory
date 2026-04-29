package com.funjim.fishstory.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.*
import com.funjim.fishstory.repository.FishRepository
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FishViewModel(
    private val repository: FishRepository
) : ViewModel() {
    // UI State flows
    private val _selectedTripId = MutableStateFlow<String?>(null)
    private val _selectedSegmentId = MutableStateFlow<String?>(null)
    private val _selectedFishermanId = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    private val _isReversed = MutableStateFlow(false)

    // Exposed State for the UI
    val trips = repository.allTrips
    val species = repository.allSpecies
    val fishPhotos = repository.fishPhotos.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val selectedTripId = _selectedTripId.asStateFlow()
    val selectedSegmentId = _selectedSegmentId.asStateFlow()
    val selectedFishermanId = _selectedFishermanId.asStateFlow()
    val sortOrder = _sortOrder.asStateFlow()
    val isReversed = _isReversed.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentsForTrip: StateFlow<List<Event>> = _selectedTripId
        .flatMapLatest { tripId ->
            if (tripId == null) {
                flowOf(emptyList())
            } else {
                repository.getSegmentsForTrip(tripId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTrip = _selectedTripId
        .filterNotNull()
        .flatMapLatest { id -> repository.getTrip(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSegment = _selectedSegmentId
        .filterNotNull()
        .flatMapLatest { id -> repository.getSegment(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedFisherman = _selectedFishermanId
        .filterNotNull()
        .flatMapLatest { id -> repository.getFisherman(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val fishForScope: StateFlow<List<FishWithDetails>> = combine(
        _selectedTripId,
        _selectedSegmentId,
        _selectedFishermanId,
        _sortOrder,
        _isReversed
    ) { trip, seg, fish, sort, rev ->
        // Helper to pass params
        FilterParams(trip, seg, fish, sort, rev)
    }.flatMapLatest { params ->
        repository.getFilteredFish(params.tripId, params.segmentId, params.fishermanId)
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
        _selectedSegmentId.value = null // Reset segment if trip changes
    }
    fun updateSelectedSegment(id: String?) {
        _selectedSegmentId.value = id
    }

    fun updateSelectedFisherman(id: String?) {
        _selectedFishermanId.value = id
    }

    fun toggleReverse() { _isReversed.value = !_isReversed.value }
    fun updateSortOrder(order: FishSortOrder) { _sortOrder.value = order }

    private data class FilterParams(
        val tripId: String?,
        val segmentId: String?,
        val fishermanId: String?,
        val sortOrder: FishSortOrder,
        val isReversed: Boolean
    )

    suspend fun getFish(id: String): Fish? {
        return repository.getFish(id)
    }
    fun upsertFish(fish: Fish) {
        viewModelScope.launch {
            repository.upsertFish(fish)
        }
    }

    fun deleteFish(fish: Fish) {
        viewModelScope.launch {
            repository.deleteFish(fish)
        }
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            repository.addSpecies(species)
        }
    }

    fun upsertSpecies(species: Species) {
        viewModelScope.launch {
            repository.upsertSpecies(species)
        }
    }

    fun deleteSpecies(species: Species) {
        viewModelScope.launch {
            repository.deleteSpecies(species)
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
}

class FishViewModelFactory(
    private val repository: FishRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
