package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.BodyOfWaterDao
import com.funjim.fishstory.database.EventDao
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.TripBodyOfWaterEntity
import com.funjim.fishstory.database.WaterDao
import com.funjim.fishstory.database.WeatherDao
import com.funjim.fishstory.database.toBodyOfWaterDomainList
import com.funjim.fishstory.database.toDomainList
import com.funjim.fishstory.database.toEntity
import com.funjim.fishstory.database.toEventBodyOfWaterEntityList
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.EventBodyOfWater
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.TripBodyOfWater
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.Weather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class EnvironmentRepository(
    private val database: FishstoryDatabase,
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val eventDao: EventDao,
    private val waterDao: WaterDao,
    private val weatherDao: WeatherDao
) {
    // Basic Data Streams
    val allBodiesOfWater: Flow<List<BodyOfWater>> = bodyOfWaterDao.getAllBodiesOfWater()
        .map { list -> list.toBodyOfWaterDomainList() }
    val allSkyConditions: Flow<List<SkyCondition>> = weatherDao.getAllSkyConditions()
        .map { list -> list.toDomainList() }
    val allWaterClarity: Flow<List<WaterClarity>> = waterDao.getAllWaterClarity()
        .map { list -> list.toDomainList() }

    suspend fun addBodyOfWater(bodyOfWater: BodyOfWater) {
        bodyOfWaterDao.insertBodyOfWater(bodyOfWater.toEntity())
    }
    suspend fun upsertBodyOfWater(bodyOfWater: BodyOfWater) {
        bodyOfWaterDao.upsertBodyOfWater(bodyOfWater.toEntity())
    }
    suspend fun deleteBodyOfWater(bodyOfWater: BodyOfWater) {
        bodyOfWaterDao.deleteBodyOfWater(bodyOfWater.toEntity())
    }

    suspend fun insertTripBodyOfWater(
        crossRef: TripBodyOfWater,
        cascade: Boolean = true) {
        database.withTransaction {
            bodyOfWaterDao.insertTripBodyOfWater(crossRef.toEntity())

            if (cascade) {
                val eventIds = eventDao.getEventIdsForTrip(crossRef.tripId)

                if (eventIds.isNotEmpty()) {
                    val crossRefs = eventIds.map { eventId ->
                        EventBodyOfWater(eventId = eventId, bodyOfWaterId = crossRef.bodyOfWaterId)
                    }
                    bodyOfWaterDao.insertBodyOfWaterForEvents(crossRefs.toEventBodyOfWaterEntityList())
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
            bodyOfWaterDao.insertEventBodyOfWater(crossRef.toEntity())

            if (cascade) {
                val tripId = eventDao.getTripIdForEvent(crossRef.eventId)
                bodyOfWaterDao.insertTripBodyOfWater(
                    TripBodyOfWaterEntity(
                        tripId = tripId,
                        bodyOfWaterId = crossRef.bodyOfWaterId
                    )
                )
            }
        }
    }

    suspend fun deleteEventBodyOfWater(eventId: String, bodyOfWaterId: String) =
        bodyOfWaterDao.deleteEventBodyOfWater(eventId, bodyOfWaterId)

    suspend fun addWater(water: Water) {
        waterDao.insertWater(water.toEntity())
    }
    suspend fun upsertWater(water: Water) {
        waterDao.upsertWater(water.toEntity())
    }
    suspend fun deleteWater(water: Water) {
        waterDao.deleteWater(water.toEntity())
    }
    suspend fun deleteWater(id: String) {
        waterDao.deleteWater(id)
    }

    suspend fun addWaterClarity(waterClarity: WaterClarity) {
        waterDao.insertWaterClarity(waterClarity.toEntity())
    }
    suspend fun upsertWaterClarity(waterClarity: WaterClarity) {
        waterDao.upsertWaterClarity(waterClarity.toEntity())
    }
    suspend fun deleteWaterClarity(waterClarity: WaterClarity) {
        waterDao.deleteWaterClarity(waterClarity.toEntity())
    }

    suspend fun addWeather(weather: Weather) {
        weatherDao.insertWeather(weather.toEntity())
    }
    suspend fun upsertWeather(weather: Weather) {
        weatherDao.upsertWeather(weather.toEntity())
    }
    suspend fun deleteWeather(weather: Weather) {
        weatherDao.deleteWeather(weather.toEntity())
    }
    suspend fun deleteWeather(id: String) {
        weatherDao.deleteWeather(id)
    }

    suspend fun addSkyCondition(skyCondition: SkyCondition) {
        weatherDao.insertSkyCondition(skyCondition.toEntity())
    }
    suspend fun upsertSkyCondition(skyCondition: SkyCondition) {
        weatherDao.upsertSkyCondition(skyCondition.toEntity())
    }
    suspend fun deleteSkyCondition(skyCondition: SkyCondition) {
        weatherDao.deleteSkyCondition(skyCondition.toEntity())
    }
}