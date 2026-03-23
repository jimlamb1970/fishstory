package com.funjim.fishstory.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.funjim.fishstory.database.*
import com.funjim.fishstory.model.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class MainViewModel(
    private val tripDao: TripDao,
    private val fishermanDao: FishermanDao,
    private val segmentDao: SegmentDao,
    private val lureDao: LureDao,
    private val fishDao: FishDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao
) : ViewModel() {

    val trips: Flow<List<Trip>> = tripDao.getAllTrips()
    val fishermen: Flow<List<Fisherman>> = fishermanDao.getAllFishermen()
    val lures: Flow<List<Lure>> = lureDao.getAllLures()
    val lureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()
    val species: Flow<List<Species>> = fishDao.getAllSpecies()
    val activeSegments: Flow<List<Segment>> = segmentDao.getActiveSegments()
    val allFish: Flow<List<FishWithDetails>> = fishDao.getAllFishWithDetails()

    // Draft state for new trip
    private val _draftSegments = MutableStateFlow<List<Segment>>(emptyList())
    val draftSegments = _draftSegments.asStateFlow()

    private val _draftFishermanIds = MutableStateFlow<Set<Int>>(emptySet())
    val draftFishermanIds = _draftFishermanIds.asStateFlow()

    // Maps draft segment tempId -> set of fisherman IDs
    private val _draftSegmentFishermanIds = MutableStateFlow<Map<Int, Set<Int>>>(emptyMap())
    val draftSegmentFishermanIds = _draftSegmentFishermanIds.asStateFlow()

    private val _draftTripName = MutableStateFlow("")
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
        _draftSegmentFishermanIds.update { it - (-1) }
    }

    fun addDraftSegment(
        name: String,
        startTime: Long,
        endTime: Long? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        // 1. Calculate the new temp ID based on the current list
        val currentSegments = _draftSegments.value
        val tempId = (currentSegments.minOfOrNull { it.id } ?: 0) - 1

        val newSegment = Segment(
            id = tempId,
            tripId = 0,
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
        val segmentFishermanIds = _draftSegmentFishermanIds.value[-1] ?: _draftFishermanIds.value.toSet()

        _draftSegmentFishermanIds.update { currentMap ->
            (currentMap - (-1)) + (tempId to segmentFishermanIds)
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

    fun addDraftFisherman(fishermanId: Int) {
        _draftFishermanIds.update { it + fishermanId }
    }

    fun removeDraftFisherman(fishermanId: Int) {
        _draftFishermanIds.update { it - fishermanId }
    }

    fun addDraftSegmentFisherman(segmentId: Int, fishermanId: Int) {
        _draftSegmentFishermanIds.update { currentMap ->
            val current = currentMap[segmentId] ?: emptySet()
            currentMap + (segmentId to current + fishermanId)
        }
    }

    fun removeDraftSegmentFisherman(segmentId: Int, fishermanId: Int) {
        _draftSegmentFishermanIds.update { currentMap ->
            val current = currentMap[segmentId] ?: emptySet()
            currentMap + (segmentId to current - fishermanId)
        }
    }

    fun toggleDraftFisherman(fishermanId: Int) {
        _draftFishermanIds.update { current ->
            if (current.contains(fishermanId)) current - fishermanId else current + fishermanId
        }
    }

    fun clearDrafts() {
        _draftSegments.value = emptyList()
        _draftSegmentFishermanIds.value = emptyMap()
        _draftFishermanIds.value = emptySet()
        _draftTripName.value = ""
        val now = System.currentTimeMillis()
        _draftTripStartDate.value = now
        _draftTripEndDate.value = now
        _draftLatitude.value = null
        _draftLongitude.value = null
    }

    fun getTripWithFishermen(tripId: Int): Flow<TripWithFishermen?> {
        return tripDao.getTripWithFishermen(tripId)
    }

    fun getTripWithDetails(tripId: Int): Flow<TripWithDetails?> {
        return tripDao.getTripWithDetails(tripId)
    }

    suspend fun addTrip(trip: Trip): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    suspend fun addFisherman(fisherman: Fisherman): Long {
        val fishermanId = fishermanDao.insert(fisherman)
        // Automatically create a tackle box for the new fisherman
        tackleBoxDao.insertTackleBox(TackleBox(fishermanId = fishermanId.toInt()))
        return fishermanId
    }

    suspend fun updateFisherman(fisherman: Fisherman) {
        fishermanDao.update(fisherman)
    }

    suspend fun deleteFisherman(fisherman: Fisherman) {
        fishermanDao.deleteFisherman(fisherman)
    }

    suspend fun addFishermanToTrip(tripId: Int, fishermanId: Int) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.insertCrossRef(crossRef)
    }

    suspend fun deleteFishermanFromTrip(tripId: Int, fishermanId: Int) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.deleteCrossRef(crossRef)
    }

    suspend fun deleteTripFromFisherman(tripId: Int, fishermanId: Int) {
        val crossRef = TripFishermanCrossRef(tripId, fishermanId)
        tripDao.deleteCrossRef(crossRef)
    }

    fun getFishermanWithTrips(fishermanId: Int): Flow<FishermanWithTrips?> {
        return fishermanDao.getFishermanWithTrips(fishermanId)
    }

    fun getFishermanWithDetails(fishermanId: Int): Flow<FishermanWithDetails?> {
        return fishermanDao.getFishermanWithDetails(fishermanId)
    }

    suspend fun addSegment(segment: Segment): Long {
        return segmentDao.insertSegment(segment)
    }

    suspend fun addSegmentWithFishermen(segment: Segment, fishermanIds: Collection<Int>) {
        val segmentId = segmentDao.insertSegment(segment).toInt()
        fishermanIds.forEach { fid ->
            segmentDao.insertSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fid))
        }
    }

    suspend fun updateSegment(segment: Segment) {
        segmentDao.updateSegment(segment)
    }

    suspend fun deleteSegment(segment: Segment) {
        segmentDao.deleteSegment(segment)
    }

    fun getSegmentsForTrip(tripId: Int): Flow<List<Segment>> {
        return segmentDao.getSegmentsForTrip(tripId)
    }

    fun getSegmentsWithDetailsForTrip(tripId: Int): Flow<List<SegmentWithDetails>> {
        return segmentDao.getSegmentsWithDetailsForTrip(tripId)
    }

    fun getSegmentWithFishermen(segmentId: Int): Flow<SegmentWithFishermen?> {
        return segmentDao.getSegmentWithFishermen(segmentId)
    }

    fun getSegmentWithDetails(segmentId: Int): Flow<SegmentWithDetails?> {
        return segmentDao.getSegmentWithDetails(segmentId)
    }

    suspend fun addFishermanToSegment(segmentId: Int, fishermanId: Int) {
        segmentDao.insertSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fishermanId))
    }

    suspend fun deleteFishermanFromSegment(segmentId: Int, fishermanId: Int) {
        segmentDao.deleteSegmentFishermanCrossRef(SegmentFishermanCrossRef(segmentId, fishermanId))
    }

    suspend fun addLure(lure: Lure): Long {
        return lureDao.insertLure(lure)
    }

    suspend fun deleteLure(lure: Lure) {
        lureDao.deleteLure(lure)
    }

    fun getLuresForFisherman(fishermanId: Int): Flow<List<Lure>> {
        return tackleBoxDao.getLuresForFisherman(fishermanId)
    }

    suspend fun addLureToFishermanTackleBox(fishermanId: Int, lureId: Int) {
        var tackleBox = tackleBoxDao.getTackleBoxForFisherman(fishermanId).firstOrNull()
        if (tackleBox == null) {
            val id = tackleBoxDao.insertTackleBox(TackleBox(fishermanId = fishermanId))
            tackleBox = TackleBox(id = id.toInt(), fishermanId = fishermanId)
        }
        tackleBoxDao.insertLureToTackleBox(TackleBoxLureCrossRef(tackleBox.id, lureId))
    }

    suspend fun removeLureFromFishermanTackleBox(fishermanId: Int, lureId: Int) {
        val tackleBox = tackleBoxDao.getTackleBoxForFisherman(fishermanId).firstOrNull()
        if (tackleBox != null) {
            tackleBoxDao.removeLureFromTackleBox(TackleBoxLureCrossRef(tackleBox.id, lureId))
        }
    }

    suspend fun addLureColor(color: LureColor): Long {
        return lureDao.insertLureColor(color)
    }

    suspend fun deleteLureColor(color: LureColor) {
        lureDao.deleteLureColor(color)
    }

    fun getFishForTrip(tripId: Int): Flow<List<FishWithDetails>> {
        return fishDao.getFishForTrip(tripId)
    }

    fun getFishForSegment(segmentId: Int): Flow<List<FishWithDetails>> {
        return fishDao.getFishForSegment(segmentId)
    }

    suspend fun getFishById(id: Int): Fish? {
        return fishDao.getFishById(id)
    }

    suspend fun addFish(fish: Fish): Long {
        return fishDao.insertFish(fish)
    }

    suspend fun updateFish(fish: Fish) {
        fishDao.updateFish(fish)
    }

    suspend fun deleteFishObject(fish: Fish) {
        fishDao.deleteFish(fish)
    }

    suspend fun addSpecies(name: String): Long {
        return fishDao.insertSpecies(Species(name = name))
    }

    suspend fun deleteSpecies(species: Species) {
        fishDao.deleteSpecies(species)
    }

    // Photo operations
    suspend fun addPhoto(photo: Photo): Long {
        return photoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: Photo) {
        photoDao.deletePhoto(photo)
    }

    fun getPhotosForTrip(tripId: Int): Flow<List<Photo>> = photoDao.getPhotosForTrip(tripId)
    fun getPhotosForSegment(segmentId: Int): Flow<List<Photo>> = photoDao.getPhotosForSegment(segmentId)
    fun getPhotosForLure(lureId: Int): Flow<List<Photo>> = photoDao.getPhotosForLure(lureId)
    fun getPhotosForFisherman(fishermanId: Int): Flow<List<Photo>> = photoDao.getPhotosForFisherman(fishermanId)
    fun getPhotosForFish(fishId: Int): Flow<List<Photo>> = photoDao.getPhotosForFish(fishId)

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
