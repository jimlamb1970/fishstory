package com.funjim.fishstory.repository

import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.SegmentDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Segment
import com.funjim.fishstory.model.SegmentFishermanCrossRef
import com.funjim.fishstory.model.SegmentSummary
import com.funjim.fishstory.model.SegmentWithDetails
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripFishermanCrossRef
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.model.TripWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepository(
    private val tripDao: TripDao,
    private val segmentDao: SegmentDao,
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

    /**
     * Upcoming trips are in the future (start date > now).
     */
    fun getUpcomingTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.startDate > now }.sortedBy { it.startDate }
    }

    /**
     * Previous trips are in the past (end date < now).
     */
    fun getPreviousTrips(): Flow<List<Trip>> = tripDao.getAllTrips().map { trips ->
        val now = System.currentTimeMillis()
        trips.filter { it.endDate < now }.sortedByDescending { it.endDate }
    }

    /**
     * Get the fisherman IDs for a given trip.
     */
    fun getFishermanIdsForTrip(tripId: String): Flow<List<String>> =
        tripDao.getFishermanIdsForTrip(tripId)

    // Segment Streams
    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>> =
        segmentDao.getSegmentsForTrip(tripId)
    fun getSegmentSummaries(tripId: String): Flow<List<SegmentSummary>> =
        segmentDao.getSegmentSummaries(tripId)

    fun getSegmentWithDetails(segmentId: String): Flow<SegmentWithDetails?> =
        segmentDao.getSegmentWithDetails(segmentId)

    // --- Photo Logic ---
    fun getPhotosForTrip(id: String): Flow<List<Photo>> = photoDao.getPhotosForTrip(id)
    fun getPhotosForSegment(id: String): Flow<List<Photo>> = photoDao.getPhotosForSegment(id)
    suspend fun addPhoto(photo: Photo) = photoDao.insertPhoto(photo)
    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

    // Trip Operations
    suspend fun upsertTrip(trip: Trip) = tripDao.upsertTrip(trip)
    suspend fun deleteTrip(tripId: String) = tripDao.deleteTripById(tripId)

    // Segment Operations
    suspend fun upsertSegment(segment: Segment) = segmentDao.upsertSegment(segment)
    suspend fun deleteSegment(segment: Segment) = segmentDao.deleteSegment(segment)

    // Fishermen and TackleBox Operations
    suspend fun upsertTripFishermanCrossRef(crossRef: TripFishermanCrossRef) =
        tripDao.upsertTripFishermanCrossRef(crossRef)
    suspend fun upsertSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef) =
        segmentDao.upsertSegmentFishermanCrossRef(crossRef)
    fun getTripFishermanTackleBoxId(tripId: String, fishermanId: String): Flow<String?> =
        tripDao.getTripFishermanTackleBoxId(tripId, fishermanId)
    fun getSegmentFishermanTackleBoxId(segmentId: String, fishermanId: String): Flow<String?> =
        segmentDao.getSegmentFishermanTackleBoxId(segmentId, fishermanId)

    fun getTripFishermenTackleBoxIds(tripId: String): Flow<Map<String, String?>> =
        tripDao.getTripFishermenTackleBoxIds(tripId)
    fun getSegmentFishermenTackleBoxIds(segmentId: String): Flow<Map<String, String?>> =
        segmentDao.getSegmentFishermenTackleBoxIds(segmentId)

    suspend fun deleteTripFishermanCrossRef(crossRef: TripFishermanCrossRef) =
        tripDao.deleteTripFishermanCrossRef(crossRef)
    suspend fun deleteSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef) =
        segmentDao.deleteSegmentFishermanCrossRef(crossRef)
}