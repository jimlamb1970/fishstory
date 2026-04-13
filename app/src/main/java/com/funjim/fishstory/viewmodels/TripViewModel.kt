package com.funjim.fishstory.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.database.*
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.plus
import kotlin.text.get

class TripViewModel(
    private val tripDao: TripDao,
    private val segmentDao: SegmentDao,
    private val photoDao: PhotoDao,
    private val fishermanDao: FishermanDao,
    private val tackleBoxDao: TackleBoxDao,
    private val fishDao: FishDao
) : ViewModel() {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): android.location.Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }

    private val _rawSummaries = tripDao.getTripSummaries()

    // TODO -- sorting on Trip summaries?
    val tripSummaries: StateFlow<List<TripSummary>> = _rawSummaries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // The "Trigger" - holds the ID of the trip the user clicked
    private val _selectedTripId = MutableStateFlow<String?>(null)

    // The "Reactive Selection" - always stays in sync with the list
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

    // The Function called by your Click Listener
    fun selectTrip(id: String) {
        _selectedTripId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val segmentSummaries: StateFlow<List<SegmentSummary>> = _selectedTripId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                // This query only runs for the currently selected trip
                segmentDao.getSegmentSummaries(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // The "Trigger" - holds the ID of the segment the user clicked
    private val _selectedSegmentId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSegmentSummary: StateFlow<SegmentSummary?> = _selectedSegmentId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                // We watch the master list and filter for the matching ID
                segmentSummaries.map { list ->
                    list.find { it.segment.id == id }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 3. The Function called by your Click Listener
    fun selectSegment(id: String) {
        _selectedSegmentId.value = id
    }

    private val _deviceLocation = MutableStateFlow<android.location.Location?>(null)
    val deviceLocation = _deviceLocation.asStateFlow()

    @androidx.annotation.RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    suspend fun getTripCurrentLocation(context: Context): android.location.Location? {
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
                    _deviceLocation.value = getTripCurrentLocation(context)
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

    // TODO -- really don't want all the fishermen objects
    val fishermen: Flow<List<Fisherman>> = fishermanDao.getAllFishermen()

    fun getFishermanIdsForTrip(tripId: String): Flow<List<String>> {
        return tripDao.getFishermanIdsForTrip(tripId)
    }

    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?> {
        return tripDao.getTripWithFishermen(tripId)
    }

    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?> {
        return tripDao.getTripWithDetails(tripId)
    }

    fun getSegmentWithFishermen(segmentId: String): Flow<SegmentWithFishermen?> {
        return segmentDao.getSegmentWithFishermen(segmentId)
    }

    fun getSegmentsWithDetailsForTrip(tripId: String): Flow<List<SegmentWithDetails>> {
        return segmentDao.getSegmentsWithDetailsForTrip(tripId)
    }

    fun getPhotosForTrip(tripId: String): Flow<List<Photo>> {
        return photoDao.getPhotosForTrip(tripId)
    }

    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>> {
        return segmentDao.getSegmentsForTrip(tripId)
    }

    fun getSegmentWithDetails(segmentId: String): Flow<SegmentWithDetails?> {
        return segmentDao.getSegmentWithDetails(segmentId)
    }

    fun getPhotosForSegment(segmentId: String): Flow<List<Photo>> {
        return photoDao.getPhotosForSegment(segmentId)
    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForSegment(segmentId)
    }

    suspend fun getFishById(id: String): Fish? {
        return fishDao.getFishById(id)
    }

    fun updateFish(fish: Fish) {
        viewModelScope.launch {
            fishDao.updateFish(fish)
        }
    }

    fun deleteFishObject(fish: Fish) {
        viewModelScope.launch {
            fishDao.deleteFish(fish)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripDao.deleteTrip(trip)
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripDao.updateTrip(trip)
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

    fun updateTripFishermanTackleBox(tripId: String, fishermanId: String, tackleBoxId: String) {
        viewModelScope.launch {
            tripDao.updateTripFishermanTackleBox(TripFishermanCrossRef(tripId, fishermanId, tackleBoxId))
        }
    }

    fun updateSegmentFishermanTackleBox(segmentId: String, fishermanId: String, tackleBoxId: String) {
        viewModelScope.launch {
            segmentDao.updateSegmentFishermanTackleBox(SegmentFishermanCrossRef(segmentId, fishermanId, tackleBoxId))
        }
    }

    fun createAndAssignTackleBox(fishermanId: String, tripId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            tackleBoxDao.insertTackleBox(tackleBox)
            tripDao.updateTripFishermanTackleBox(
                TripFishermanCrossRef(
                    tripId,
                    fishermanId,
                    tackleBox.id
                )
            )
        }
    }

    fun createAndAssignSegmentTackleBox(fishermanId: String, segmentId: String, name: String) {
        viewModelScope.launch {
            val tackleBox = TackleBox(fishermanId = fishermanId, name = name)
            tackleBoxDao.insertTackleBox(tackleBox)
            segmentDao.updateSegmentFishermanTackleBox(
                SegmentFishermanCrossRef(
                    segmentId,
                    fishermanId,
                    tackleBox.id
                )
            )
        }
    }

    fun getTripFishermanTackleBoxId(tripId: String, fishermanId: String): Flow<String?> {
        return tripDao.getTripFishermanTackleBoxId(tripId, fishermanId)
    }

    fun getSegmentFishermanTackleBoxId(segmentId: String, fishermanId: String): Flow<String?> {
        return segmentDao.getSegmentFishermanTackleBoxId(segmentId, fishermanId)
    }

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return tackleBoxDao.getTackleBoxesForFisherman(fishermanId)
    }

    fun getLureCountForTackleBox(tackleBoxId: String?): Flow<Int> {
        return tackleBoxDao.getLuresInTackleBox(tackleBoxId ?: "").map { it.size }
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

    fun getPhotosForFish(fishId: String): Flow<List<Photo>> {
        return photoDao.getPhotosForFish(fishId)
    }

    fun updateSegment(segment: Segment) {
        viewModelScope.launch {
            segmentDao.updateSegment(segment)
        }
    }

    fun deleteSegment(segment: Segment) {
        viewModelScope.launch {
            segmentDao.deleteSegment(segment)
        }
    }

    fun deleteFishermanFromSegment(segmentId: String, fishermanId: String) {
        viewModelScope.launch {
            segmentDao.deleteSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fishermanId))
        }
    }

    private val _draftTripStartDate = MutableStateFlow(System.currentTimeMillis())
    val draftTripStartDate = _draftTripStartDate.asStateFlow()

    private val _draftTripEndDate = MutableStateFlow(System.currentTimeMillis())
    val draftTripEndDate = _draftTripEndDate.asStateFlow()

    fun updateDraftTripStartDate(dateMillis: Long) {
        _draftTripStartDate.value = dateMillis
    }

    fun updateDraftTripEndDate(dateMillis: Long) {
        _draftTripEndDate.value = dateMillis
    }

    private val _draftLatitude = MutableStateFlow<Double?>(null)
    val draftLatitude = _draftLatitude.asStateFlow()

    private val _draftLongitude = MutableStateFlow<Double?>(null)
    val draftLongitude = _draftLongitude.asStateFlow()

    fun updateDraftLocation(lat: Double?, lon: Double?) {
        _draftLatitude.value = lat
        _draftLongitude.value = lon
    }

    private val _draftSegmentId = MutableStateFlow("")
    val draftSegmentId = _draftSegmentId.asStateFlow()

    private val _draftSegmentName = MutableStateFlow("")
    val draftSegmentName = _draftSegmentName.asStateFlow()

    private val _draftSegmentStartDate = MutableStateFlow(System.currentTimeMillis())
    val draftSegmentStartDate = _draftSegmentStartDate.asStateFlow()

    private val _draftSegmentEndDate = MutableStateFlow(System.currentTimeMillis())
    val draftSegmentEndDate = _draftSegmentEndDate.asStateFlow()

    private val _draftSegmentLatitude = MutableStateFlow<Double?>(null)
    val draftSegmentLatitude = _draftSegmentLatitude.asStateFlow()

    private val _draftSegmentLongitude = MutableStateFlow<Double?>(null)
    val draftSegmentLongitude = _draftSegmentLongitude.asStateFlow()

    fun clearDraftSegment() {
        _draftSegmentName.value = ""
        val now = System.currentTimeMillis()
        _draftSegmentStartDate.value = now
        _draftSegmentEndDate.value = now
        _draftSegmentLatitude.value = null
        _draftSegmentLongitude.value = null
    }

    fun updateDraftSegmentId(id: String) {
        _draftSegmentId.value = id
    }

    fun updateDraftSegmentName(name: String) {
        _draftSegmentName.value = name
    }

    fun updateDraftSegmentStartDate(dateMillis: Long) {
        _draftSegmentStartDate.value = dateMillis
    }

    fun updateDraftSegmentEndDate(dateMillis: Long) {
        _draftSegmentEndDate.value = dateMillis
    }

    fun updateDraftSegmentLocation(lat: Double?, lon: Double?) {
        _draftSegmentLatitude.value = lat
        _draftSegmentLongitude.value = lon
    }

    private val _draftFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val draftFishermanIds = _draftFishermanIds.asStateFlow()

    fun setDraftFisherman(fishermanIds: Set<String>) {
        _draftFishermanIds.update { fishermanIds }
    }

    private val _draftSegmentFishermanIds = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val draftSegmentFishermanIds = _draftSegmentFishermanIds.asStateFlow()

    fun setDraftSegmentFisherman(fishermanIds: Set<String>) {
        _draftSegmentFishermanIds.update { it + (draftSegmentId.value to fishermanIds) }
    }

    fun addDraftSegmentFisherman(segmentId: String, fishermanId: String) {
        _draftSegmentFishermanIds.update { currentMap ->
            val current = currentMap[segmentId] ?: emptySet()
            currentMap + (segmentId to current + fishermanId)
        }
    }

    // Draft state for new trip
    private val _draftSegments = MutableStateFlow<List<Segment>>(emptyList())
    val draftSegments = _draftSegments.asStateFlow()

    fun upsertDraftSegment(updatedSegment: Segment) {
        _draftSegments.update { currentList ->
            // 1. Check if the segment already exists in the list
            val alreadyExists = currentList.any { it.id == updatedSegment.id }

            if (alreadyExists) {
                // 2. If it exists, map through and replace the old one
                currentList.map { segment ->
                    if (segment.id == updatedSegment.id) updatedSegment else segment
                }
            } else {
                // 3. If it doesn't exist, create a new list with the item added
                currentList + updatedSegment
            }
        }
    }
}

class TripViewModelFactory(
    private val tripDao: TripDao,
    private val segmentDao: SegmentDao,
    private val photoDao: PhotoDao,
    private val fishermanDao: FishermanDao,
    private val tackleBoxDao: TackleBoxDao,
    private val fishDao: FishDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(tripDao, segmentDao, photoDao, fishermanDao, tackleBoxDao, fishDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
