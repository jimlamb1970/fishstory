package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventDetailedSummary
import com.funjim.fishstory.model.EventFishermanCrossRef
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.EventWithDetails
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.EventTargetSpecies
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripDetailedSummary
import com.funjim.fishstory.model.TripFishermanCrossRef
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.model.TripTargetSpecies
import com.funjim.fishstory.model.TripWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepository(
    private val database: FishstoryDatabase,
    private val eventDao: EventDao,
    private val tripDao: TripDao
) {
    // Trip Streams
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    val allTripSummaries: Flow<List<TripSummary>> = tripDao.getTripSummaries()
    
    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?> =
        tripDao.getTripWithDetails(tripId)

    /**
     * An active trip is one where the current time is between start and end.
     * We'll take the most recent one if multiple overlap.
     */
    fun getActiveTrip(): Flow<Trip?> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.firstOrNull { now in it.startDate..it.endDate }
    }

    fun getActiveTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { now in it.startDate..it.endDate }
    }

    fun getActiveTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { now in it.trip.startDate..it.trip.endDate }.sortedByDescending { it.trip.startDate }
    }

    /**
     * Upcoming trips are in the future (start date > now).
     */
    fun getUpcomingTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.startDate > now }.sortedBy { it.startDate }
    }

    fun getUpcomingTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.trip.startDate > now }.sortedBy { it.trip.startDate }
    }


    /**
     * Previous trips are in the past (end date < now).
     */
    fun getPreviousTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.endDate < now }.sortedByDescending { it.endDate }
    }

    fun getPreviousTripSummaries(): Flow<List<TripSummary>> = tripDao.getTripSummaries().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.trip.endDate < now}.sortedByDescending { it.trip.endDate }
    }

    fun getEventsForActiveTrips(currentTime: Long): Flow<List<EventSummary>> =
        eventDao.getEventsForActiveTrips(currentTime)
    fun getEventSummaries(tripId: String): Flow<List<EventSummary>> =
        eventDao.getTripEventSummaries(tripId)

    fun getTripSummary(tripId: String): Flow<TripSummary> =
        tripDao.getTripSummary(tripId)
    fun getTripDetailedSummary(tripId: String): Flow<TripDetailedSummary> =
        tripDao.getTripDetailedSummary(tripId)

    fun getEventSummary(eventId: String): Flow<EventSummary> =
        eventDao.getEventSummary(eventId)
    fun getEventDetailedSummary(eventId: String): Flow<EventDetailedSummary> =
        eventDao.getEventDetailedSummary(eventId)

    fun getEventWithDetails(eventId: String): Flow<EventWithDetails?> =
        eventDao.getEventWithDetails(eventId)


    // Trip Operations
    suspend fun upsertTrip(trip: Trip) = tripDao.upsertTrip(trip)
    suspend fun deleteTripById(tripId: String) = tripDao.deleteTripById(tripId)

    // Segment Operations
    suspend fun upsertEvent(event: Event) = eventDao.upsertEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    suspend fun deleteEventById(segmentId: String) = eventDao.deleteEventById(segmentId)

    // Fishermen and TackleBox Operations
    suspend fun upsertTripFishermanCrossRef(crossRef: TripFishermanCrossRef) =
        tripDao.upsertTripFishermanCrossRef(crossRef)
    suspend fun upsertSegmentFishermanCrossRef(crossRef: EventFishermanCrossRef) =
        eventDao.upsertEventFishermanCrossRef(crossRef)
    fun getTripFishermanTackleBoxId(tripId: String, fishermanId: String): Flow<String?> =
        tripDao.getTripFishermanTackleBoxId(tripId, fishermanId)
    fun getSegmentFishermanTackleBoxId(segmentId: String, fishermanId: String): Flow<String?> =
        eventDao.getTackleBoxIdForFisherman(segmentId, fishermanId)

    fun getTackleBoxMapForTrip(tripId: String): Flow<Map<String, String?>> =
        tripDao.getTripFishermenTackleBoxIds(tripId)
    fun getTackleBoxMapForEvent(eventId: String): Flow<Map<String, String?>> =
        eventDao.getFishermanTackleBoxMapping(eventId)

    fun getTripFishermen(tripId: String): Flow<List<Fisherman>> =
        tripDao.getFishermenForTrip(tripId)
    fun getEventFishermen(eventId: String): Flow<List<Fisherman>> =
        eventDao.getFishermenForEvent(eventId)

    suspend fun deleteSegmentFishermanCrossRef(crossRef: EventFishermanCrossRef) =
        eventDao.deleteEventFishermanCrossRef(crossRef)

    suspend fun removeFishermanFromTripAndAllEvents(tripId: String, fishermanId: String) =
        tripDao.removeFishermanCrossRefFromTripAndAllEvents(tripId, fishermanId)
    suspend fun removeFishermenNotInSet(segmentId: String, newSet: Set<String>) =
        eventDao.removeFishermenNotInSet(segmentId, newSet)

    // Target Species
    fun getEventTargetSpecies(eventId: String): Flow<List<Species>> =
        eventDao.getEventTargetSpecies(eventId)
    suspend fun insertEventTargetSpecies(crossRef: EventTargetSpecies) {
        database.withTransaction {
            eventDao.insertEventTargetSpecies(crossRef)

            val tripId = eventDao.getTripIdForEvent(crossRef.eventId)

            tripDao.insertTripTargetSpecies(
                TripTargetSpecies(
                    tripId = tripId,
                    speciesId = crossRef.speciesId
                )
            )
        }
    }
    suspend fun deleteEventTargetSpecies(eventId: String, speciesId: String) =
        eventDao.deleteEventTargetSpecies(eventId, speciesId)

    fun getTripTargetSpecies(tripId: String): Flow<List<Species>> =
        tripDao.getTripTargetSpecies(tripId)

    suspend fun insertTripTargetSpecies(crossRef: TripTargetSpecies) {
        database.withTransaction {
            tripDao.insertTripTargetSpecies(crossRef)

            val eventIds = eventDao.getEventIdsForTrip(crossRef.tripId)

            val eventTargets = eventIds.map { eventId ->
                EventTargetSpecies(eventId = eventId, speciesId = crossRef.speciesId)
            }
            eventDao.insertTargetSpeciesForEvents(eventTargets)
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
