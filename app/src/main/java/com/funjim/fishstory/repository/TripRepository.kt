package com.funjim.fishstory.repository

import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventFishermanCrossRef
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.EventWithDetails
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripFishermanCrossRef
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.model.TripWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepository(
    private val tripDao: TripDao,
    private val eventDao: EventDao,
    private val photoDao: PhotoDao,
    private val fishermanDao: FishermanDao
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

    /**
     * Get the fisherman IDs for a given trip.
     */
    fun getFishermanIdsForTrip(tripId: String): Flow<List<String>> =
        tripDao.getFishermanIdsForTrip(tripId)

    // Segment Streams
    fun getSegmentsForTrip(tripId: String): Flow<List<Event>> =
        eventDao.getEventsForTrip(tripId)
    fun getSegmentsForActiveTrips(currentTime: Long): Flow<List<EventSummary>> =
        eventDao.getEventsForActiveTrips(currentTime)
    fun getSegmentSummaries(tripId: String): Flow<List<EventSummary>> =
        eventDao.getTripEventSummaries(tripId)

    fun getSegmentWithDetails(segmentId: String): Flow<EventWithDetails?> =
        eventDao.getEventWithDetails(segmentId)

    fun getActiveSegments(): Flow<List<Event>> =
        eventDao.getAllEvents().map { segments ->
            val now = System.currentTimeMillis()
            segments.filter { now in it.startTime..it.endTime }
        }

    fun getActiveSegmentsForTrip(tripId: String): Flow<List<Event>> =
        eventDao.getEventsForTrip(tripId).map { segments ->
        val now = System.currentTimeMillis()
        segments.filter { now in it.startTime..it.endTime }
    }

    fun getUpcomingSegments(): Flow<List<Event>> = eventDao.getAllEvents().map { segments ->
        val now = System.currentTimeMillis()
        segments.filter { it.startTime > now }.sortedBy { it.startTime }
    }

    // --- Photo Logic ---
    fun getPhotosForTrip(id: String): Flow<List<Photo>> = photoDao.getPhotosForTrip(id)
    fun getPhotosForSegment(id: String): Flow<List<Photo>> = photoDao.getPhotosForEvent(id)
    suspend fun addPhoto(photo: Photo) = photoDao.insertPhoto(photo)
    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

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

    fun getTripFishermenTackleBoxIds(tripId: String): Flow<Map<String, String?>> =
        tripDao.getTripFishermenTackleBoxIds(tripId)
    fun getFishermanTackleBoxMapping(eventId: String): Flow<Map<String, String?>> =
        eventDao.getFishermanTackleBoxMapping(eventId)

    fun getEventFishermen(eventId: String): Flow<List<Fisherman>> =
        eventDao.getFishermenForEvent(eventId)

    suspend fun deleteTripFishermanCrossRef(crossRef: TripFishermanCrossRef) =
        tripDao.deleteTripFishermanCrossRef(crossRef)
    suspend fun deleteSegmentFishermanCrossRef(crossRef: EventFishermanCrossRef) =
        eventDao.deleteEventFishermanCrossRef(crossRef)

    suspend fun removeFishermanCrossRefFromTripAndAllSegments(tripId: String, fishermanId: String) =
        tripDao.removeFishermanCrossRefFromTripAndAllEvents(tripId, fishermanId)
    suspend fun removeFishermenNotInSet(segmentId: String, newSet: Set<String>) =
        eventDao.removeFishermenNotInSet(segmentId, newSet)
}
