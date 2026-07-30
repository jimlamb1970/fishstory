package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.BodyOfWaterDao
import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.WaterDao
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.EventBodyOfWater
import com.funjim.fishstory.model.TripBodyOfWater
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import kotlinx.coroutines.flow.Flow
import kotlin.collections.map

class EnvironmentRepository(
    private val database: FishstoryDatabase,
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val eventDao: EventDao,
    private val waterDao: WaterDao
) {
    // Basic Data Streams
    val allBodiesOfWater: Flow<List<BodyOfWater>> = bodyOfWaterDao.getAllBodiesOfWater()
    val allWaterClarity: Flow<List<WaterClarity>> = waterDao.getAllWaterClarity()

    suspend fun addBodyOfWater(bodyOfWater: BodyOfWater) = bodyOfWaterDao.insertBodyOfWater(bodyOfWater)
    suspend fun upsertBodyOfWater(bodyOfWater: BodyOfWater) = bodyOfWaterDao.upsertBodyOfWater(bodyOfWater)
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

            bodyOfWaterDao.deleteBodyOfWaterForEvents(eventIds, bodyOfWaterId)
        }
    }

    suspend fun insertEventBodyOfWater(
        crossRef: EventBodyOfWater,
        cascade: Boolean = true) {
        database.withTransaction {
            bodyOfWaterDao.insertEventBodyOfWater(crossRef)

            if (cascade) {
                val tripId = eventDao.getTripIdForEvent(crossRef.eventId)
                bodyOfWaterDao.insertTripBodyOfWater(
                    TripBodyOfWater(
                        tripId = tripId,
                        bodyOfWaterId = crossRef.bodyOfWaterId
                    )
                )
            }
        }
    }

    suspend fun deleteEventBodyOfWater(eventId: String, bodyOfWaterId: String) =
        bodyOfWaterDao.deleteEventBodyOfWater(eventId, bodyOfWaterId)

    suspend fun addWater(water: Water) = waterDao.insertWater(water)
    suspend fun upsertWater(water: Water) = waterDao.upsertWater(water)
    suspend fun deleteWater(water: Water) = waterDao.deleteWater(water)
    suspend fun deleteWater(waterId: String) = waterDao.deleteWater(waterId)

    suspend fun addWaterClarity(waterClarity: WaterClarity) = waterDao.insertWaterClarity(waterClarity)
    suspend fun upsertWaterClarity(waterClarity: WaterClarity) = waterDao.upsertWaterClarity(waterClarity)
    suspend fun deleteWaterClarity(waterClarity: WaterClarity) = waterDao.deleteWaterClarity(waterClarity)
}