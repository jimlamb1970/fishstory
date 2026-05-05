package com.funjim.fishstory.repository

import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.FishDao
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.EventWithFishermen
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FishRepository(
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val lureDao: LureDao,
    private val photoDao: PhotoDao,
    private val eventDao: EventDao,
    private val tripDao: TripDao
) {
    // Basic Data Streams
    val allSpecies: Flow<List<Species>> = fishDao.getAllSpecies()
    val speciesSummaries: Flow<List<SpeciesSummary>> = fishDao.getSpeciesSummaries()

    fun getSegmentWithFishermen(segmentId: String): Flow<EventWithFishermen?> {
        return eventDao.getEventWithFishermen(segmentId)
    }

    fun getTrips(fishermanId: String?, lureId: String?): Flow<List<Trip>> =
        tripDao.getTripsWithFish(fishermanId, lureId)
    fun getEvents(tripId: String?, fishermanId: String?, lureId: String?) =
        eventDao.getEventsWithFish(tripId, fishermanId, lureId)
    fun getFishermen(tripId: String?, eventId: String?, lureId: String?) =
        fishermanDao.getFishermenWithFish(tripId, eventId, lureId)

    fun getTrip(id: String) = tripDao.getTrip(id)
    fun getEventById(id: String) = eventDao.getEventById(id)
    fun getFisherman(id: String) = fishermanDao.getFisherman(id)
    fun getLure(id: String) = lureDao.getLure(id)

    fun getFishCounts(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ) = fishDao.getFishCounts(tripId, eventId, fishermanId, lureId)
    fun getTopTrip(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ) = fishDao.getTopTrip(tripId, eventId, fishermanId, lureId)
    fun getTopEvent(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ) = fishDao.getTopEvent(tripId, eventId, fishermanId, lureId)
    fun getTopFisherman(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ) = fishDao.getTopFisherman(tripId, eventId, fishermanId, lureId)
    fun getTopSpecies(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ) = fishDao.getTopSpecies(tripId, eventId, fishermanId, lureId)
    fun getTopLure(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ) = fishDao.getTopLure(tripId, eventId, fishermanId, lureId)


    suspend fun getFish(id: String) = fishDao.getFish(id)

    // The Core Filtering Logic (Migrated from ViewModel)
    fun getFilteredFish(
        tripId: String?,
        eventId: String?,
        fishermanId: String?
    ): Flow<List<FishWithDetails>> = fishDao.getFishWithDetails(tripId, eventId, fishermanId)

    val fishPhotos: Flow<Map<String, List<Photo>>> = photoDao.getAllFishPhotos()
        .map { photos ->
            photos.filter { it.fishId != null }.groupBy { it.fishId!! }
        }

    suspend fun upsertFish(fish: Fish) = fishDao.upsertFish(fish)
    suspend fun deleteFish(fish: Fish) = fishDao.deleteFish(fish)

    suspend fun addPhoto(photo: Photo) = photoDao.insertPhoto(photo)
    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

    suspend fun addSpecies(species: Species) = fishDao.insertSpecies(species)
    suspend fun upsertSpecies(species: Species) = fishDao.upsertSpecies(species)
    suspend fun deleteSpecies(species: Species) = fishDao.deleteSpecies(species)
}