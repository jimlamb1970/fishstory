package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.BodyOfWaterDao
import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.EventBodyOfWater
import com.funjim.fishstory.model.TripBodyOfWater
import kotlinx.coroutines.flow.Flow
import kotlin.collections.map

class EnvironmentRepository(
    private val database: FishstoryDatabase,
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val eventDao: EventDao
) {
    // Basic Data Streams
    val allBodiesOfWater: Flow<List<BodyOfWater>> = bodyOfWaterDao.getAllBodiesOfWater()

    suspend fun addBodyOfWater(bodyOfWater: BodyOfWater) = bodyOfWaterDao.insertBodyOfWater(bodyOfWater)
    suspend fun deleteBodyOfWater(bodyOfWater: BodyOfWater) = bodyOfWaterDao.deleteBodyOfWater(bodyOfWater)

    suspend fun insertTripBodyOfWater(
        crossRef: TripBodyOfWater,
        cascade: Boolean = true) {
        database.withTransaction {
            bodyOfWaterDao.insertTripBodyOfWater(crossRef)

            if (cascade) {
                val eventIds = eventDao.getEventIdsForTrip(crossRef.tripId)

                if (eventIds.isNotEmpty()) {
                    val crossRefs = eventIds.map { eventId ->
                        EventBodyOfWater(eventId = eventId, bodyOfWaterId = crossRef.bodyOfWaterId)
                    }
                    bodyOfWaterDao.insertBodyOfWaterForEvents(crossRefs)
                }
            }
        }
    }

    suspend fun deleteTripBodyOfWater(tripId: String, bodyOfWaterId: String) {
        database.withTransaction {
            bodyOfWaterDao.deleteTripBodyOfWater(tripId, bodyOfWaterId)

            val eventIds = eventDao.getEventIdsForTrip(tripId)

            eventDao.deleteBodyOfWaterForEvents(eventIds, bodyOfWaterId)
        }
    }
}