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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MainViewModel(
    private val tripDao: TripDao,
    private val fishermanDao: FishermanDao,
    private val segmentDao: SegmentDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModel() {
    // A simple flow to broadcast volume key directions: 1 for Up, -1 for Down
    private val _volumeKeyEvent = MutableStateFlow(0)
    val volumeKeyEvent = _volumeKeyEvent.asStateFlow()


    fun onVolumeKeyPressed(direction: Int) {
        viewModelScope.launch {
            // We update the value to trigger the Collector in the UI.
            // Even if the direction is the same, we want a "new" event.
            _volumeKeyEvent.value = direction
            kotlinx.coroutines.delay(50)
            // Reset it immediately so the next press (even if same direction) is caught
            _volumeKeyEvent.value = 0
        }
    }

    private val _selectEvent = MutableStateFlow(0)
    val selectEvent = _selectEvent.asStateFlow()

    fun triggerSelect() {
        viewModelScope.launch {
            _selectEvent.value += 1
        }
    }

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()
    val fishermen: Flow<List<Fisherman>> = fishermanDao.getAllFishermen()
    val lures: Flow<List<Lure>> = lureDao.getAllLures()
    val lureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()
    val species: Flow<List<Species>> = fishDao.getAllSpecies()
    val activeSegments: Flow<List<Segment>> = segmentDao.getCurrentActiveSegments()
    val allFish: Flow<List<FishWithDetails>> = fishDao.getAllFishWithDetails()

    // Draft state for new trip
    private val _draftSegments = MutableStateFlow<List<Segment>>(emptyList())
    val draftSegments = _draftSegments.asStateFlow()

    private val _draftFishermanIds = MutableStateFlow<Set<String>>(emptySet())
    val draftFishermanIds = _draftFishermanIds.asStateFlow()

    // Maps draft segment tempId -> set of fisherman IDs
    private val _draftSegmentId = UUID.randomUUID().toString()
    private val _draftSegmentFishermanIds = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val draftSegmentId = _draftSegmentId
    val draftSegmentFishermanIds = _draftSegmentFishermanIds.asStateFlow()

    private val _draftTripId = MutableStateFlow("")
    private val _draftTripName = MutableStateFlow("")
    val draftTripId = _draftTripId.asStateFlow()
    val draftTripName = _draftTripName.asStateFlow()

    private val _draftTripStartDate = MutableStateFlow(System.currentTimeMillis())
    val draftTripStartDate = _draftTripStartDate.asStateFlow()

    private val _draftTripEndDate = MutableStateFlow(System.currentTimeMillis())
    val draftTripEndDate = _draftTripEndDate.asStateFlow()

    private val _draftLatitude = MutableStateFlow<Double?>(null)
    val draftLatitude = _draftLatitude.asStateFlow()

    private val _draftLongitude = MutableStateFlow<Double?>(null)
    val draftLongitude = _draftLongitude.asStateFlow()

    // Draft state for segment currently being added
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

    fun updateDraftTripId(id: String) {
        _draftTripId.value = id
    }
    fun updateDraftTripName(name: String) {
        _draftTripName.value = name
    }

    fun updateDraftTripStartDate(dateMillis: Long) {
        _draftTripStartDate.value = dateMillis
    }

    fun updateDraftTripEndDate(dateMillis: Long) {
        _draftTripEndDate.value = dateMillis
    }

    fun updateDraftLocation(lat: Double?, lon: Double?) {
        _draftLatitude.value = lat
        _draftLongitude.value = lon
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

    fun clearDraftSegment() {
        _draftSegmentName.value = ""
        val now = System.currentTimeMillis()
        _draftSegmentStartDate.value = now
        _draftSegmentEndDate.value = now
        _draftSegmentLatitude.value = null
        _draftSegmentLongitude.value = null
        // Clear the specific draft boat load for the "new segment" screen (id = -1)
        _draftSegmentFishermanIds.update { it - draftSegmentId }
    }

    fun addDraftSegment(
        name: String,
        startTime: Long,
        endTime: Long,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        // 1. Calculate the new temp ID based on the current list
        // If there are no segments, the first segment needs to start at -2
        // This is because -1 is reserved for set of fishermen to add to the segment
        val currentSegments = _draftSegments.value
        val tempId = UUID.randomUUID().toString()

        val newSegment = Segment(
            id = tempId,
            tripId = _draftTripId.value,
            name = name,
            startTime = startTime,
            endTime = endTime,
            latitude = latitude,
            longitude = longitude
        )

        // 2. Use .update for thread-safe state changes
        _draftSegments.update { it + newSegment }

        // 3. Sync the fisherman IDs to the new segment ID
        // The fisherman IDs for this specific "new segment" were being tracked in -1
        val segmentFishermanIds = _draftSegmentFishermanIds.value[draftSegmentId] ?: _draftFishermanIds.value.toSet()

        _draftSegmentFishermanIds.update { currentMap ->
            (currentMap - (draftSegmentId)) + (tempId to segmentFishermanIds)
        }
    }

    fun updateDraftSegment(updatedSegment: Segment) {
        _draftSegments.update { currentList ->
            currentList.map { segment ->
                if (segment.id == updatedSegment.id) updatedSegment else segment
            }
        }
    }

    fun removeDraftSegment(segment: Segment) {
        _draftSegments.update { it - segment }
        _draftSegmentFishermanIds.update { it - segment.id }
    }

    fun addDraftFisherman(fishermanId: String) {
        _draftFishermanIds.update { it + fishermanId }
    }

    fun removeDraftFisherman(fishermanId: String) {
        _draftFishermanIds.update { it - fishermanId }
    }

    fun addDraftSegmentFisherman(segmentId: String, fishermanId: String) {
        _draftSegmentFishermanIds.update { currentMap ->
            val current = currentMap[segmentId] ?: emptySet()
            currentMap + (segmentId to current + fishermanId)
        }
    }

    fun removeDraftSegmentFisherman(segmentId: String, fishermanId: String) {
        _draftSegmentFishermanIds.update { currentMap ->
            val current = currentMap[segmentId] ?: emptySet()
            currentMap + (segmentId to current - fishermanId)
        }
    }

    fun toggleDraftFisherman(fishermanId: String) {
        _draftFishermanIds.update { current ->
            if (current.contains(fishermanId)) current - fishermanId else current + fishermanId
        }
    }

    fun clearDrafts() {
        _draftTripId.value = ""
        _draftTripName.value = ""
        val now = System.currentTimeMillis()
        _draftTripStartDate.value = now
        _draftTripEndDate.value = now

        _draftSegments.value = emptyList()
        _draftSegmentFishermanIds.value = emptyMap()

        _draftFishermanIds.value = emptySet()

        _draftLatitude.value = null
        _draftLongitude.value = null
    }

    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?> {
        return tripDao.getTripWithFishermen(tripId)
    }

    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?> {
        return tripDao.getTripWithDetails(tripId)
    }

    suspend fun addTrip(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    suspend fun addFisherman(fisherman: Fisherman) {
        fishermanDao.insert(fisherman)
        // Automatically create a tackle box for the new fisherman
        tackleBoxDao.insertTackleBox(TackleBox(fishermanId = fisherman.id))
    }

    suspend fun updateFisherman(fisherman: Fisherman) {
        fishermanDao.update(fisherman)
    }

    suspend fun deleteFisherman(fisherman: Fisherman) {
        fishermanDao.deleteFisherman(fisherman)
    }

    suspend fun addFishermanToTrip(tripId: String, fishermanId: String) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.insertCrossRef(crossRef)
    }

    suspend fun deleteFishermanFromTrip(tripId: String, fishermanId: String) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.deleteCrossRef(crossRef)
    }

    suspend fun deleteTripFromFisherman(tripId: String, fishermanId: String) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.deleteCrossRef(crossRef)
    }

    fun getFishermanWithTrips(fishermanId: String): Flow<FishermanWithTrips?> {
        return fishermanDao.getFishermanWithTrips(fishermanId)
    }

    fun getFishermanWithDetails(fishermanId: String): Flow<FishermanWithDetails?> {
        return fishermanDao.getFishermanWithDetails(fishermanId)
    }

    suspend fun addSegment(segment: Segment) {
        segmentDao.insertSegment(segment)
    }

    suspend fun addSegmentWithFishermen(segment: Segment, fishermanIds: Collection<String>) {
        segmentDao.insertSegment(segment)
        fishermanIds.forEach { fid ->
            segmentDao.insertSegmentFishermanCrossRef(SegmentFishermanCrossRef(segment.id, fid))
        }
    }

    suspend fun updateSegment(segment: Segment) {
        segmentDao.updateSegment(segment)
    }

    suspend fun deleteSegment(segment: Segment) {
        segmentDao.deleteSegment(segment)
    }

    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>> {
        return segmentDao.getSegmentsForTrip(tripId)
    }

    fun getSegmentsWithDetailsForTrip(tripId: String): Flow<List<SegmentWithDetails>> {
        return segmentDao.getSegmentsWithDetailsForTrip(tripId)
    }

    fun getSegmentWithFishermen(segmentId: String): Flow<SegmentWithFishermen?> {
        return segmentDao.getSegmentWithFishermen(segmentId)
    }

    fun getSegmentWithDetails(segmentId: String): Flow<SegmentWithDetails?> {
        return segmentDao.getSegmentWithDetails(segmentId)
    }

    suspend fun addFishermanToSegment(segmentId: String, fishermanId: String) {
        segmentDao.insertSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fishermanId))
    }

    suspend fun deleteFishermanFromSegment(segmentId: String, fishermanId: String) {
        segmentDao.deleteSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fishermanId))
    }

    suspend fun addLure(lure: Lure) {
        lureDao.insertLure(lure)
    }

    suspend fun deleteLure(lure: Lure) {
        lureDao.deleteLure(lure)
    }

    suspend fun getLureById(id: String): Lure? {
        return lureDao.getLureById(id)
    }

    fun getLuresForFisherman(fishermanId: String): Flow<List<Lure>> {
        return tackleBoxDao.getLuresForFisherman(fishermanId)
    }

    suspend fun addLureToFishermanTackleBox(fishermanId: String, lureId: String) {
        var tackleBox = tackleBoxDao.getTackleBoxForFisherman(fishermanId).firstOrNull()
        if (tackleBox == null) {
            tackleBox = TackleBox(fishermanId = fishermanId)
            tackleBoxDao.insertTackleBox(tackleBox)
        }
        tackleBoxDao.insertLureToTackleBox(TackleBoxLureCrossRef(tackleBox.id, lureId))
    }

    suspend fun removeLureFromFishermanTackleBox(fishermanId: String, lureId: String) {
        val tackleBox = tackleBoxDao.getTackleBoxForFisherman(fishermanId).firstOrNull()
        if (tackleBox != null) {
            tackleBoxDao.removeLureFromTackleBox(TackleBoxLureCrossRef(tackleBox.id, lureId))
        }
    }

    suspend fun addLureColor(color: LureColor) {
        return lureDao.insertLureColor(color)
    }

    suspend fun deleteLureColor(color: LureColor) {
        lureDao.deleteLureColor(color)
    }

    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
    }

    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>> {
        return fishDao.getFishForSegment(segmentId)
    }

    suspend fun getFishById(id: String): Fish? {
        return fishDao.getFishById(id)
    }

    suspend fun addFish(fish: Fish) {
        fishDao.insertFish(fish)
    }

    suspend fun upsertFish(fish: Fish) {
        return fishDao.upsertFish(fish)
    }

    suspend fun updateFish(fish: Fish) {
        fishDao.updateFish(fish)
    }

    suspend fun deleteFishObject(fish: Fish) {
        fishDao.deleteFish(fish)
    }

    suspend fun addSpecies(species: Species) {
        fishDao.insertSpecies(species)
    }

    suspend fun deleteSpecies(species: Species) {
        fishDao.deleteSpecies(species)
    }

    // Photo operations
    suspend fun addPhoto(photo: Photo) {
        photoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: Photo) {
        photoDao.deletePhoto(photo)
    }

    fun getPhotosForTrip(tripId: String): Flow<List<Photo>> = photoDao.getPhotosForTrip(tripId)
    fun getPhotosForSegment(segmentId: String): Flow<List<Photo>> = photoDao.getPhotosForSegment(segmentId)
    fun getPhotosForLure(lureId: String): Flow<List<Photo>> = photoDao.getPhotosForLure(lureId)
    fun getPhotosForFisherman(fishermanId: String): Flow<List<Photo>> = photoDao.getPhotosForFisherman(fishermanId)
    fun getPhotosForFish(fishId: String): Flow<List<Photo>> = photoDao.getPhotosForFish(fishId)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): android.location.Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }
}

class MainViewModelFactory(
    private val tripDao: TripDao,
    private val fishermanDao: FishermanDao,
    private val segmentDao: SegmentDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(tripDao, fishermanDao, segmentDao, lureDao, fishDao, photoDao, tackleBoxDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
