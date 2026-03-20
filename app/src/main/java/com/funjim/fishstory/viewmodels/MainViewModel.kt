package com.funjim.fishstory.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.funjim.fishstory.database.*
import com.funjim.fishstory.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

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

    // Draft state for new trip
    private val _draftSegments = MutableStateFlow<List<Segment>>(emptyList())
    val draftSegments = _draftSegments.asStateFlow()

    private val _draftFishermanIds = MutableStateFlow<Set<Int>>(emptySet())
    val draftFishermanIds = _draftFishermanIds.asStateFlow()

    fun addDraftSegment(name: String, startTime: Long) {
        // Use a temporary negative ID for draft segments to ensure they have unique keys in LazyColumn
        val tempId = (_draftSegments.value.minOfOrNull { it.id } ?: 0) - 1
        val newSegment = Segment(id = tempId, tripId = 0, name = name, startTime = startTime)
        _draftSegments.value = _draftSegments.value + newSegment
    }

    fun removeDraftSegment(segment: Segment) {
        _draftSegments.value = _draftSegments.value - segment
    }

    fun addDraftFisherman(fishermanId: Int) {
        _draftFishermanIds.value = _draftFishermanIds.value + fishermanId
    }

    fun removeDraftFisherman(fishermanId: Int) {
        _draftFishermanIds.value = _draftFishermanIds.value - fishermanId
    }

    fun toggleDraftFisherman(fishermanId: Int) {
        val current = _draftFishermanIds.value
        if (current.contains(fishermanId)) {
            removeDraftFisherman(fishermanId)
        } else {
            addDraftFisherman(fishermanId)
        }
    }

    fun clearDrafts() {
        _draftSegments.value = emptyList()
        _draftFishermanIds.value = emptySet()
    }

    fun getTripWithFishermen(tripId: Int): Flow<TripWithFishermen> {
        return tripDao.getTripWithFishermen(tripId)
    }

    fun getTripWithDetails(tripId: Int): Flow<TripWithDetails> {
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

    fun getFishermanWithTrips(fishermanId: Int): Flow<FishermanWithTrips> {
        return fishermanDao.getFishermanWithTrips(fishermanId)
    }

    fun getFishermanWithDetails(fishermanId: Int): Flow<FishermanWithDetails> {
        return fishermanDao.getFishermanWithDetails(fishermanId)
    }

    suspend fun addSegment(segment: Segment) {
        segmentDao.insertSegment(segment)
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

    fun getSegmentWithFishermen(segmentId: Int): Flow<SegmentWithFishermen> {
        return segmentDao.getSegmentWithFishermen(segmentId)
    }

    fun getSegmentWithDetails(segmentId: Int): Flow<SegmentWithDetails> {
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
