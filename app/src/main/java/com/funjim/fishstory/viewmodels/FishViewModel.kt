package com.funjim.fishstory.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.FishDao
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.SegmentDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FishViewModel(
    private val fishDao: FishDao,
    private val tripDao: TripDao,
    private val segmentDao: SegmentDao,
    private val fishermanDao: FishermanDao,
    private val photoDao: PhotoDao
) : ViewModel() {

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()
    val species: Flow<List<Species>> = fishDao.getAllSpecies()

    private val _selectedTripIdForFilter = MutableStateFlow<String?>(null)
    val selectedTripIdForFilter = _selectedTripIdForFilter.asStateFlow()

    private val _selectedSegmentIdForFilter = MutableStateFlow<String?>(null)
    val selectedSegmentIdForFilter = _selectedSegmentIdForFilter.asStateFlow()

    private val _fishermanIdForFilter = MutableStateFlow<String?>(null)
    val fishermanIdForFilter = _fishermanIdForFilter.asStateFlow()

    // Observe the Trip Details reactively
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentTrip = _selectedTripIdForFilter
        .filterNotNull()
        .flatMapLatest { id -> tripDao.getTripWithDetails(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe the Segment Details reactively
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSegment = _selectedSegmentIdForFilter
        .filterNotNull()
        .flatMapLatest { id -> segmentDao.getSegmentWithDetails(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentFisherman = _fishermanIdForFilter
        .filterNotNull()
        .flatMapLatest { id -> fishermanDao.getFishermanWithDetails(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateSelectedTripIdForFilter(id: String?) {
        _selectedTripIdForFilter.value = id
        _selectedSegmentIdForFilter.value = null
    }

    fun updateSelectedSegmentIdForFilter(id: String?) {
        _selectedSegmentIdForFilter.value = id
    }

    fun updateFishermanIdForFilter(id: String?) {
        _fishermanIdForFilter.value = id
    }

    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>> {
        return segmentDao.getSegmentsForTrip(tripId)
    }

    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForSegment(segmentId)
    }

    fun getFishForFisherman(fishermanId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForFisherman(fishermanId)
    }

    suspend fun getFishById(id: String): Fish? {
        return fishDao.getFishById(id)
    }

    private val _sortOrder = MutableStateFlow(FishSortOrder.TIMESTAMP_NEWEST_FIRST)
    val sortOrder = _sortOrder.asStateFlow()

    private val _isReversed = MutableStateFlow(false)
    val isReversed = _isReversed.asStateFlow()

    private data class FishFilterParams(
        val tripId: String?,
        val segmentId: String?,
        val fishermanId: String?,
        val sortOrder: FishSortOrder,
        val isReversed: Boolean
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    val fishForScope: StateFlow<List<FishWithDetails>> = combine(
        selectedTripIdForFilter,
        selectedSegmentIdForFilter,
        fishermanIdForFilter,
        _sortOrder,
        _isReversed
    ) { tripId, segId, fishermanId, sortOrder, isReversed ->
        FishFilterParams(tripId, segId, fishermanId, sortOrder, isReversed)
    }.flatMapLatest { params ->
        val baseFlow = when {
            !params.segmentId.isNullOrBlank() -> getFishForSegment(params.segmentId)
            !params.tripId.isNullOrBlank() -> getFishForTrip(params.tripId)
            !params.fishermanId.isNullOrBlank() -> getFishForFisherman(params.fishermanId)
            else -> flowOf(emptyList())
        }
        baseFlow.map { list ->
            val sortedList = when (params.sortOrder) {
                FishSortOrder.TIMESTAMP_NEWEST_FIRST -> list.sortedByDescending { it.timestamp }
                FishSortOrder.TRIP_AZ -> list.sortedBy { it.tripName }
                FishSortOrder.SEGMENT_AZ -> list.sortedBy { it.segmentName }
                FishSortOrder.FISHERMAN_AZ -> list.sortedBy { it.fishermanName }
                FishSortOrder.SPECIES_AZ -> list.sortedBy { it.speciesName }
                FishSortOrder.LENGTH_LONGEST_FIRST -> list.sortedByDescending { it.length }
                FishSortOrder.HOLE_NUMBER_ASC -> list.sortedBy { it.holeNumber }
                FishSortOrder.RELEASED -> list.sortedBy { it.isReleased }
                FishSortOrder.LURE -> list.sortedBy { it.getFullLureName() }
            }

            if (params.isReversed) sortedList.reversed() else sortedList
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleReverse() {
        _isReversed.value = !_isReversed.value
    }

    fun updateSortOrder(newOrder: FishSortOrder) {
        _sortOrder.value = newOrder
    }

    fun upsertFish(fish: Fish) {
        viewModelScope.launch {
            fishDao.upsertFish(fish)
        }
    }

    fun deleteFishObject(fish: Fish) {
        viewModelScope.launch {
            fishDao.deleteFish(fish)
        }
    }

    fun addSpecies(species: Species) {
        viewModelScope.launch {
            fishDao.insertSpecies(species)
        }
    }

    fun deleteSpecies(species: Species) {
        viewModelScope.launch {
            fishDao.deleteSpecies(species)
        }
    }

    fun addPhoto(photo: Photo) {
        viewModelScope.launch {
            photoDao.insertPhoto(photo)
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            photoDao.deletePhoto(photo)
        }
    }
    val fishPhotos: StateFlow<Map<String, List<Photo>>> = photoDao.getAllFishPhotos()
        .map { photos ->
            photos.filter { it.fishId != null }
                .groupBy { it.fishId!! } // The !! is safe here because of the filter
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

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
    private val fishDao: FishDao,
    private val tripDao: TripDao,
    private val segmentDao: SegmentDao,
    private val fishermanDao: FishermanDao,
    private val photoDao: PhotoDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishViewModel(fishDao, tripDao, segmentDao, fishermanDao, photoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
