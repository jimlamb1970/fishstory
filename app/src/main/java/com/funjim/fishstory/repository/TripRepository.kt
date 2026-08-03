package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.EventTargetSpeciesEntity
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.database.toDomain
import com.funjim.fishstory.database.toEntity
import com.funjim.fishstory.database.toEventSummaryDomainList
import com.funjim.fishstory.database.toFishermanDomainList
import com.funjim.fishstory.database.toSpeciesDomainList
import com.funjim.fishstory.database.toTripDomainList
import com.funjim.fishstory.database.toTripSummaryDomainList
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventDetailedSummary
import com.funjim.fishstory.model.EventFisherman
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.EventWithDetails
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.EventTargetSpecies
import com.funjim.fishstory.model.EventWithInfo
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripDetailedSummary
import com.funjim.fishstory.model.TripFisherman
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.model.TripTargetSpecies
import com.funjim.fishstory.model.TripWithDetails
import com.funjim.fishstory.model.TripWithFishermen
import com.funjim.fishstory.model.TripWithFishermenAndSpecies
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepository(
    private val database: FishstoryDatabase,
    private val eventDao: EventDao,
    private val tripDao: TripDao
) {
    // Trip Streams
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
        .map { list -> list.toTripDomainList() }
    val allTripSummaries: Flow<List<TripSummary>> = tripDao.getTripSummaries()
        .map { list -> list.toTripSummaryDomainList() }
    
    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?> {
        return tripDao.getTripWithDetails(tripId)
            .map { it?.toDomain() }
    }

    fun getTripWithFishermenAndSpecies(tripId: String): Flow<TripWithFishermenAndSpecies?> {
        return tripDao.getTripWithFishermenAndSpecies(tripId)
            .map { it?.toDomain() }
    }

    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?> {
        return tripDao.getTripWithFishermen(tripId)
            .map { it?.toDomain() }
    }

    /**
     * An active trip is one where the current time is between start and end.
     * We'll take the most recent one if multiple overlap.
     */
    fun getActiveTrip(): Flow<Trip?> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.firstOrNull { now in it.startDate..it.endDate }?.toDomain()
    }

    fun getActiveTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { now in it.startDate..it.endDate }
            .toTripDomainList()
    }

    fun getActiveTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { now in it.trip.startDate..it.trip.endDate }
            .sortedByDescending { it.trip.startDate }
            .toTripSummaryDomainList()
    }

    /**
     * Upcoming trips are in the future (start date > now).
     */
    fun getUpcomingTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.startDate > now }
            .sortedBy { it.startDate }
            .toTripDomainList()
    }

    fun getUpcomingTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.trip.startDate > now }
            .sortedBy { it.trip.startDate }
            .toTripSummaryDomainList()
    }


    /**
     * Previous trips are in the past (end date < now).
     */
    fun getPreviousTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.endDate < now }
            .sortedByDescending { it.endDate }
            .toTripDomainList()
    }

    fun getPreviousTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.trip.endDate < now}
            .sortedByDescending { it.trip.endDate }
            .toTripSummaryDomainList()
    }

    fun getEventsForActiveTrips(currentTime: Long): Flow<List<EventSummary>> {
        return eventDao.getEventsForActiveTrips(currentTime)
            .map { list -> list.toEventSummaryDomainList() }
    }

    fun getEventSummaries(tripId: String): Flow<List<EventSummary>> {
        return eventDao.getTripEventSummaries(tripId)
            .map { list -> list.toEventSummaryDomainList() }
    }

    fun getTripSummary(tripId: String): Flow<TripSummary?> {
        return tripDao.getTripSummary(tripId)
            .map { it?.toDomain() }
    }

    fun getTripDetailedSummary(tripId: String): Flow<TripDetailedSummary?> {
        return tripDao.getTripDetailedSummary(tripId)
            .map { it?.toDomain() }
    }

    fun getEventSummary(eventId: String): Flow<EventSummary?> {
        return eventDao.getEventSummary(eventId)
            .map { it?.toDomain() }
    }

    fun getEventDetailedSummary(eventId: String): Flow<EventDetailedSummary?> {
        return eventDao.getEventDetailedSummary(eventId)
            .map { it?.toDomain() }
    }

    fun getEventWithDetails(eventId: String): Flow<EventWithDetails?> {
        return eventDao.getEventWithDetails(eventId)
            .map { it?.toDomain() }
    }

    fun getEventWithInfo(eventId: String): Flow<EventWithInfo?> {
        return eventDao.getEventWithInfo(eventId)
            .map { it?.toDomain() }
    }

    // Trip Operations
    suspend fun upsertTrip(trip: Trip) {
        tripDao.upsertTrip(trip.toEntity())
    }
    suspend fun deleteTripById(id: String) {
        tripDao.deleteTripById(id)
    }

    // Segment Operations
    suspend fun upsertEvent(event: Event) {
        eventDao.upsertEvent(event.toEntity())
    }
    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event.toEntity())
    }
    suspend fun deleteEventById(id: String) {
        eventDao.deleteEventById(id)
    }

    // Fishermen and TackleBox Operations
    suspend fun upsertTripFisherman(crossRef: TripFisherman) {
        tripDao.upsertTripFisherman(crossRef.toEntity())
    }
    suspend fun upsertEventFisherman(crossRef: EventFisherman) {
        eventDao.upsertEventFisherman(crossRef.toEntity())
    }

    fun getTackleBoxMapForTrip(tripId: String): Flow<Map<String, String?>> =
        tripDao.getTripFishermenTackleBoxIds(tripId)
    fun getTackleBoxMapForEvent(eventId: String): Flow<Map<String, String?>> =
        eventDao.getFishermanTackleBoxMapping(eventId)

    fun getTripFishermen(tripId: String): Flow<List<Fisherman>> {
        return tripDao.getFishermenForTrip(tripId)
            .map { list -> list.toFishermanDomainList() }
    }
    fun getEventFishermen(eventId: String): Flow<List<Fisherman>> {
        return eventDao.getFishermenForEvent(eventId)
            .map { list -> list.toFishermanDomainList() }
    }

    suspend fun deleteEventFishermanCrossRef(crossRef: EventFisherman) {
        eventDao.deleteEventFishermanCrossRef(crossRef.toEntity())
    }

    suspend fun removeFishermanFromTripAndAllEvents(tripId: String, fishermanId: String) =
        tripDao.removeFishermanCrossRefFromTripAndAllEvents(tripId, fishermanId)

    // Target Species
    fun getEventTargetSpecies(eventId: String): Flow<List<Species>> {
        return eventDao.getEventTargetSpecies(eventId).map { speciesList ->
            speciesList.toSpeciesDomainList()
        }
    }
    suspend fun insertEventTargetSpecies(
        crossRef: EventTargetSpecies,
        cascade: Boolean = true) {
        database.withTransaction {
            eventDao.insertEventTargetSpecies(crossRef.toEntity())

            if (cascade) {
                val tripId = eventDao.getTripIdForEvent(crossRef.eventId)
                tripDao.insertTripTargetSpecies(
                    TripTargetSpecies(
                        tripId = tripId,
                        speciesId = crossRef.speciesId
                    ).toEntity()
                )
            }
        }
    }
    suspend fun deleteEventTargetSpecies(eventId: String, speciesId: String) =
        eventDao.deleteEventTargetSpecies(eventId, speciesId)

    fun getTripTargetSpecies(tripId: String): Flow<List<Species>> {
        return tripDao.getTripTargetSpecies(tripId).map { speciesList ->
            speciesList.toSpeciesDomainList()
        }
    }

    suspend fun insertTripTargetSpecies(
        crossRef: TripTargetSpecies,
        cascade: Boolean = true) {
        database.withTransaction {
            tripDao.insertTripTargetSpecies(crossRef.toEntity())
            if (cascade) {
                val eventIds = eventDao.getEventIdsForTrip(crossRef.tripId)
                if (eventIds.isNotEmpty()) {
                    val eventTargets = eventIds.map { eventId ->
                        EventTargetSpeciesEntity(eventId = eventId, speciesId = crossRef.speciesId)
                    }
                    eventDao.insertTargetSpeciesForEvents(eventTargets)
                }
            }
        }
    }
    suspend fun deleteTripTargetSpecies(tripId: String, speciesId: String) {
        database.withTransaction {
            tripDao.deleteTripTargetSpecies(tripId, speciesId)
            val eventIds = eventDao.getEventIdsForTrip(tripId)
            eventDao.deleteTargetSpeciesForEvents(eventIds, speciesId)
        }
    }
}
