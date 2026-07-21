package com.funjim.fishstory.repository

import com.funjim.fishstory.database.BodyOfWaterDao
import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.FishDao
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.BaitSummary
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.BodyOfWaterSummary
import com.funjim.fishstory.model.EventWithCounts
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishCounts
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.FishermanWithCounts
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.LureWithCounts
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.PhotoFishCrossRef
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.model.SpeciesWithCounts
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripWithCounts
import kotlinx.coroutines.flow.Flow

class FishRepository(
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val lureDao: LureDao,
    private val photoDao: PhotoDao,
    private val eventDao: EventDao,
    private val tripDao: TripDao
) {
    // Basic Data Streams
    val allSpecies: Flow<List<Species>> = fishDao.getAllSpecies()
    val baitSummaries: Flow<List<BaitSummary>> = fishDao.getBaitSummaries()
    val bodyOfWaterSummaries: Flow<List<BodyOfWaterSummary>> = fishDao.getBodyOfWaterSummaries()
    val speciesSummaries: Flow<List<SpeciesSummary>> = fishDao.getSpeciesSummaries()

    fun getBodiesOfWater(
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
    ): Flow<List<BodyOfWater>> {
        return bodyOfWaterDao.getBodiesOfWaterWithFish(
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)
    }

    fun getTrips(
        bodyOfWaterId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<List<Trip>> =
        tripDao.getTripsWithFish(
            bodyOfWaterId = bodyOfWaterId,
            fishermanId = fishermanId,
            lureId = lureId)

    fun getEvents(
        bodyOfWaterId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?) =
        eventDao.getEventsWithFish(
            bodyOfWaterId = bodyOfWaterId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)

    fun getFishermen(
        bodyOfWaterId: String?,
        eventId: String?,
        lureId: String?,
        tripId: String?) =
        fishermanDao.getFishermenWithFish(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            lureId = lureId,
            tripId = tripId)

    fun getLures(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        tripId: String?
    ): Flow<List<LureWithColors>> {
        return lureDao.getLuresWithFish(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            tripId = tripId)
    }

    fun getBodyOfWater(id: String) = bodyOfWaterDao.getBodyOfWater(id)
    fun getTrip(id: String) = tripDao.getTrip(id)
    fun getTripById(id: String) = tripDao.getTripById(id)
    fun getEventById(id: String) = eventDao.getEventById(id)
    fun getFisherman(id: String) = fishermanDao.getFisherman(id)
    fun getLure(id: String) = lureDao.getLure(id)

    fun getFishCounts(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
    ) : Flow<FishCounts> {
        return fishDao.getFishCounts(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)
    }

    fun getTopTrip(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
    ) : Flow<TripWithCounts?> {
        return fishDao.getTopTrip(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)

    }

    fun getTopEvent(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
    ) : Flow<EventWithCounts?> {
        return fishDao.getTopEvent(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)

    }

    fun getTopFisherman(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
    ) : Flow<FishermanWithCounts?> {
        return fishDao.getTopFisherman(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)

    }

    fun getTopSpecies(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
    ) : Flow<SpeciesWithCounts?> {
        return fishDao.getTopSpecies(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)

    }

    fun getTopLure(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
    ) : Flow<LureWithCounts?> {
        return fishDao.getTopLure(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId)

    }

    suspend fun getFish(id: String) = fishDao.getFish(id)
    fun getFishWithPhotos(id: String) = fishDao.getFishWithPhotos(id)

    // The Core Filtering Logic (Migrated from ViewModel)
    fun getFilteredFish(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?,
        targetOnly: Boolean? = false
    ): Flow<List<FishWithDetails>> {
        return fishDao.getFishWithDetails(
            bodyOfWaterId = bodyOfWaterId,
            eventId = eventId,
            fishermanId = fishermanId,
            lureId = lureId,
            tripId = tripId,
            targetOnly = targetOnly)
    }

    suspend fun upsertFish(fish: Fish) = fishDao.upsertFish(fish)
    suspend fun deleteFish(fish: Fish) = fishDao.deleteFish(fish)

    suspend fun addFishPhoto(fishId: String, photo: Photo) {
        val result = photoDao.insertPhoto(photo)

        val photoId = if (result != -1L) {
            photo.id
        } else {
            photoDao.getPhotoIdByUri(photo.uri)
        }

        if (photoId != null) {
            photoDao.addFishPhoto(PhotoFishCrossRef(photoId, fishId))
        }
    }
    suspend fun deleteFishPhoto(fishId: String, photoId: String) =
        photoDao.deleteFishPhoto(PhotoFishCrossRef(photoId, fishId))

    suspend fun updateFishBodyOfWater(
        newBodyOfWaterId: String?,
        tripId: String? = null,
        eventId: String? = null
    ) {
        // Prevent accidental updates of the entire table if both are null
        if (tripId == null && eventId == null) return

        fishDao.updateBodyOfWaterForTripOrEvent(
            newBodyOfWaterId = newBodyOfWaterId,
            tripId = tripId,
            eventId = eventId
        )
    }

    suspend fun addSpecies(species: Species) = fishDao.insertSpecies(species)
    suspend fun upsertSpecies(species: Species) = fishDao.upsertSpecies(species)
    suspend fun deleteSpecies(species: Species) = fishDao.deleteSpecies(species)

}