package com.funjim.fishstory.repository

import com.funjim.fishstory.database.dao.BodyOfWaterDao
import com.funjim.fishstory.database.dao.EventDao
import com.funjim.fishstory.database.dao.FishDao
import com.funjim.fishstory.database.dao.FishermanDao
import com.funjim.fishstory.database.dao.LimitDao
import com.funjim.fishstory.database.dao.LureDao
import com.funjim.fishstory.database.dao.NoteDao
import com.funjim.fishstory.database.dao.PhotoDao
import com.funjim.fishstory.database.PhotoFishEntity
import com.funjim.fishstory.database.dao.TripDao
import com.funjim.fishstory.database.toBaitSummaryDomainList
import com.funjim.fishstory.database.toBodyOfWaterDomainList
import com.funjim.fishstory.database.toBodyOfWaterSummaryDomainList
import com.funjim.fishstory.database.toDomain
import com.funjim.fishstory.database.toSpeciesDomainList
import com.funjim.fishstory.database.toEntity
import com.funjim.fishstory.database.toSpeciesEntityList
import com.funjim.fishstory.database.toEventDomainList
import com.funjim.fishstory.database.toFishWithDetailsDomainList
import com.funjim.fishstory.database.toFishermanDomainList
import com.funjim.fishstory.database.toFishermanSummaryDomainList
import com.funjim.fishstory.database.toLimitWithSpeciesDomainList
import com.funjim.fishstory.database.toLureWithColorsDomainList
import com.funjim.fishstory.database.toNoteDomainList
import com.funjim.fishstory.database.toSpeciesSummaryDomainList
import com.funjim.fishstory.database.toTripDomainList
import com.funjim.fishstory.database.toWaterSummaryDomainList
import com.funjim.fishstory.database.toWeatherSummaryDomainList
import com.funjim.fishstory.model.BaitSummary
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.BodyOfWaterSummary
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventWithCounts
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishCounts
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.FishermanWithCounts
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitType
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.LureWithCounts
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.model.SpeciesWithCounts
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripWithCounts
import com.funjim.fishstory.model.WaterSummary
import com.funjim.fishstory.model.WeatherSummary
import com.funjim.fishstory.ui.utils.FishFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FishRepository(
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val limitDao: LimitDao,
    private val lureDao: LureDao,
    private val noteDao: NoteDao,
    private val photoDao: PhotoDao,
    private val eventDao: EventDao,
    private val tripDao: TripDao
) {
    // Basic Data Streams
    val allSpecies: Flow<List<Species>> = fishDao.getAllSpecies()
        .map { list -> list.toSpeciesDomainList() }
    val baitSummaries: Flow<List<BaitSummary>> = fishDao.getBaitSummaries()
        .map { list -> list.toBaitSummaryDomainList() }
    val bodyOfWaterSummaries: Flow<List<BodyOfWaterSummary>> = fishDao.getBodyOfWaterSummaries()
        .map { list -> list.toBodyOfWaterSummaryDomainList() }
    val speciesSummaries: Flow<List<SpeciesSummary>> = fishDao.getSpeciesSummaries()
        .map { list -> list.toSpeciesSummaryDomainList() }

    fun getBodiesOfWater(filter: FishFilter): Flow<List<BodyOfWater>> {
        return bodyOfWaterDao.getBodiesOfWaterWithFish(
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId
        ).map { list -> list.toBodyOfWaterDomainList() }
    }

    fun getTrips(filter: FishFilter): Flow<List<Trip>> {
        return tripDao.getTripsWithFish(
            bodyOfWaterId = filter.bodyOfWaterId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId
        ).map { list -> list.toTripDomainList() }
    }

    fun getEvents(filter: FishFilter): Flow<List<Event>> {
        return eventDao.getEventsWithFish(
            bodyOfWaterId = filter.bodyOfWaterId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId
        ).map { list -> list.toEventDomainList() }
    }

    fun getFishermen(filter: FishFilter): Flow<List<Fisherman>> {
        return fishermanDao.getFishermenWithFish(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId
        ).map { list -> list.toFishermanDomainList() }
    }

    fun getLures(filter: FishFilter): Flow<List<LureWithColors>> {
        return lureDao.getLuresWithFish(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            speciesId = filter.speciesId,
            tripId = filter.tripId
        ).map { list -> list.toLureWithColorsDomainList() }
    }

    fun getSpecies(filter: FishFilter): Flow<List<Species>> {
        return fishDao.getSpeciesWithFish(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            tripId = filter.tripId).map { list -> list.toSpeciesDomainList() }
    }

    fun getBodyOfWater(id: String): Flow<BodyOfWater?> {
        return bodyOfWaterDao.getBodyOfWater(id)
            .map { entity -> entity?.toDomain() }
    }
    fun getEventById(id: String): Flow<Event?> {
        return eventDao.getEventById(id)
            .map { entity -> entity?.toDomain() }
    }
    fun getFisherman(id: String): Flow<Fisherman?> {
        return fishermanDao.getFisherman(id)
            .map { entity -> entity?.toDomain() }
    }
    fun getSpecies(id: String): Flow<Species?> {
        return fishDao.getSpecies(id)
            .map { entity -> entity?.toDomain() }
    }
    fun getTrip(id: String): Flow<Trip?> {
        return tripDao.getTrip(id)
            .map { entity -> entity?.toDomain() }
    }

    fun getEventBodyOfWaterSummaries(
        eventId: String
    ): Flow<List<BodyOfWaterSummary>> {
        return fishDao.getEventBodyOfWaterSummaries(
            eventId = eventId)
            .map { list -> list.toBodyOfWaterSummaryDomainList() }
    }

    fun getTripBodyOfWaterSummaries(
        tripId: String
    ): Flow<List<BodyOfWaterSummary>> {
        return fishDao.getTripBodyOfWaterSummaries(
            tripId = tripId)
            .map { list -> list.toBodyOfWaterSummaryDomainList() }
    }

    fun getEventFishermanSummaries(
        eventId: String
    ): Flow<List<FishermanSummary>> {
        return fishermanDao.getEventFishermanSummaries(eventId = eventId)
            .map { list -> list.toFishermanSummaryDomainList() }
    }

    fun getTripFishermanSummaries(
        tripId: String
    ): Flow<List<FishermanSummary>> {
        return fishermanDao.getTripFishermanSummaries(tripId = tripId)
            .map { list -> list.toFishermanSummaryDomainList() }
    }

    fun getSpeciesSummary(
        tripId: String? = null,
        eventId: String? = null,
        speciesId: String): Flow<SpeciesSummary?> {
        return fishDao.getSpeciesSummary(
            tripId = tripId,
            eventId = eventId,
            speciesId = speciesId)
            .map { entity -> entity?.toDomain() }
    }

    fun getEventTargetSpeciesSummaries(
        eventId: String
    ): Flow<List<SpeciesSummary>> {
        return fishDao.getEventTargetSpeciesSummaries(eventId = eventId)
            .map { list -> list.toSpeciesSummaryDomainList() }
    }

    fun getTripTargetSpeciesSummaries(
        tripId: String
    ): Flow<List<SpeciesSummary>> {
        return fishDao.getTripTargetSpeciesSummaries(tripId = tripId)
            .map { list -> list.toSpeciesSummaryDomainList() }
    }

    fun getWaterSummaries(
        tripId: String? = null,
        eventId: String? = null
    ): Flow<List<WaterSummary>> {
        return fishDao.getWaterSummaries(
            tripId = tripId,
            eventId = eventId)
            .map { list -> list.toWaterSummaryDomainList() }
    }

    fun getWeatherSummaries(
        tripId: String? = null,
        eventId: String? = null
    ): Flow<List<WeatherSummary>> {
        return fishDao.getWeatherSummaries(
            tripId = tripId,
            eventId = eventId)
            .map { list -> list.toWeatherSummaryDomainList() }
    }

    fun getFishCounts(filter: FishFilter): Flow<FishCounts> {
        return fishDao.getFishCounts(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId
        ).map { entity -> entity.toDomain() }
    }

    fun getTopTrip(filter: FishFilter) : Flow<TripWithCounts?> {
        return fishDao.getTopTrip(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId).map { entity -> entity?.toDomain() }
    }

    fun getTopEvent(filter: FishFilter) : Flow<EventWithCounts?> {
        return fishDao.getTopEvent(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId).map { entity -> entity?.toDomain() }
    }

    fun getTopFisherman(filter: FishFilter) : Flow<FishermanWithCounts?> {
        return fishDao.getTopFisherman(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId).map { entity -> entity?.toDomain() }
    }

    fun getTopSpecies(filter: FishFilter) : Flow<SpeciesWithCounts?> {
        return fishDao.getTopSpecies(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId).map { entity -> entity?.toDomain() }
    }

    fun getTopLure(filter: FishFilter) : Flow<LureWithCounts?> {
        return fishDao.getTopLure(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId).map { entity -> entity?.toDomain() }
    }

    suspend fun getFish(id: String) = fishDao.getFish(id)
    fun getFishWithPhotos(id: String) = fishDao.getFishWithPhotos(id)

    // The Core Filtering Logic (Migrated from ViewModel)
    fun getFilteredFish(filter: FishFilter): Flow<List<FishWithDetails>> {
        return fishDao.getFishWithDetails(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId,
            waterId = filter.waterId,
            weatherId = filter.weatherId,
            targetOnly = filter.targetOnly
        ).map { list -> list.toFishWithDetailsDomainList() }
    }

    suspend fun upsertFish(fish: Fish) = fishDao.upsertFish(fish.toEntity())
    suspend fun deleteFish(fish: Fish) = fishDao.deleteFish(fish.toEntity())

    suspend fun addFishPhoto(fishId: String, photo: Photo) {
        val result = photoDao.insertPhoto(photo.toEntity())

        val photoId = if (result != -1L) {
            photo.id
        } else {
            photoDao.getPhotoIdByUri(photo.uri)
        }

        if (photoId != null) {
            photoDao.addFishPhoto(PhotoFishEntity(photoId, fishId))
        }
    }
    suspend fun deleteFishPhoto(fishId: String, photoId: String) =
        photoDao.deleteFishPhoto(PhotoFishEntity(photoId, fishId))

    fun getNotesForFish(id: String): Flow<List<Note>> {
        return noteDao.getNotesForFish(id).map { list -> list.toNoteDomainList() }
    }

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

    suspend fun addSpecies(species: Species) {
        fishDao.insertSpecies(species.toEntity())
    }
    suspend fun updateSpecies(species: Species) {
        fishDao.updateSpecies(species.toEntity())
    }
    suspend fun deleteSpecies(species: Species) {
        fishDao.deleteSpecies(species.toEntity())
    }

    fun getLimitsForEvent(eventId: String): Flow<List<Limit>> {
        return limitDao.getLimitsForEvent(eventId = eventId)
            .map { list -> list.toLimitWithSpeciesDomainList() }
    }

    fun getLimitsForTrip(tripId: String): Flow<List<Limit>> {
        return limitDao.getLimitsForTrip(tripId = tripId)
            .map { list -> list.toLimitWithSpeciesDomainList() }
    }

    suspend fun addLimitToEvent(eventId: String, limit: Limit) {
        limitDao.insertLimitForEvent(
            limit = limit.toEntity(),
            species = limit.species.toSpeciesEntityList(),
            eventId = eventId
        )
    }

    suspend fun addLimitToTrip(tripId: String, limit: Limit) {
        limitDao.insertLimitForTrip(
            limit = limit.toEntity(),
            species = limit.species.toSpeciesEntityList(),
            tripId = tripId
        )
    }

    suspend fun deleteLimit(limit: Limit) {
        limitDao.deleteLimit(limit.toEntity())
    }

    fun getCaughtCountForLimit(eventId: String, limit: Limit): Flow<Int> {
        val speciesIds = limit.species.map { it.id }

        return when (limit.type) {
            LimitType.BAG_LIMIT -> {
                fishDao.getKeptCountForSpecies(eventId, speciesIds)
            }
            LimitType.MIN_SIZE -> {
                fishDao.getKeptCountMinSize(eventId, speciesIds, limit.lowerSize)
            }
            LimitType.MAX_SIZE -> {
                fishDao.getKeptCountMaxSize(eventId, speciesIds, limit.upperSize)
            }
            LimitType.SLOT_LIMIT -> {
                fishDao.getKeptCountSlotLimit(eventId, speciesIds, limit.lowerSize, limit.upperSize)
            }
            LimitType.TROPHY_LIMIT -> {
                fishDao.getKeptCountTrophyLimit(eventId, speciesIds, limit.upperSize)
            }
        }
    }
}