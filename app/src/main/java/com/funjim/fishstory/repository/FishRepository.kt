package com.funjim.fishstory.repository

import com.funjim.fishstory.database.FishDao
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.SegmentDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FishRepository(
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val photoDao: PhotoDao,
    private val segmentDao: SegmentDao,
    private val tripDao: TripDao
) {
    // Basic Data Streams
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    val allSpecies: Flow<List<Species>> = fishDao.getAllSpecies()

    fun getTrip(id: String) = tripDao.getTrip(id)
    fun getSegmentsForTrip(tripId: String) = segmentDao.getSegmentsForTrip(tripId)
    fun getSegment(id: String) = segmentDao.getSegment(id)
    fun getFisherman(id: String) = fishermanDao.getFisherman(id)
    suspend fun getFish(id: String) = fishDao.getFish(id)

    // The Core Filtering Logic (Migrated from ViewModel)
    fun getFilteredFish(
        tripId: String?,
        segmentId: String?,
        fishermanId: String?
    ): Flow<List<FishWithDetails>> {
        return when {
            !segmentId.isNullOrBlank() -> fishDao.getFishForSegment(segmentId)
            !tripId.isNullOrBlank() -> fishDao.getFishForTrip(tripId)
            !fishermanId.isNullOrBlank() -> fishDao.getFishForFisherman(fishermanId)
            else -> fishDao.getAllFishWithDetails()
        }
    }

    val fishPhotos: Flow<Map<String, List<Photo>>> = photoDao.getAllFishPhotos()
        .map { photos ->
            photos.filter { it.fishId != null }.groupBy { it.fishId!! }
        }

    suspend fun upsertFish(fish: Fish) = fishDao.upsertFish(fish)
    suspend fun deleteFish(fish: Fish) = fishDao.deleteFish(fish)

    suspend fun addPhoto(photo: Photo) = photoDao.insertPhoto(photo)
    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

    suspend fun addSpecies(species: Species) = fishDao.insertSpecies(species)
    suspend fun deleteSpecies(species: Species) = fishDao.deleteSpecies(species)
}