package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.dao.BodyOfWaterDao
import com.funjim.fishstory.database.dao.EventDao
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.entity.TripBodyOfWaterEntity
import com.funjim.fishstory.database.dao.WaterDao
import com.funjim.fishstory.database.dao.WeatherDao
import com.funjim.fishstory.database.mapper.toBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toDomain
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toDomainList
import com.funjim.fishstory.database.mapper.toEventBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toEventBodyOfWaterEntityList
import com.funjim.fishstory.database.mapper.toWaterDomainList
import com.funjim.fishstory.database.mapper.toWeatherDomainList
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.EventBodyOfWater
import com.funjim.fishstory.model.InsertResult
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.TripBodyOfWater
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.Weather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
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

    suspend fun getAllSkyConditionsForEvent(eventId: String): List<SkyCondition> {
        return weatherDao.getAllSkyConditionsForEvent(eventId).toDomainList()
    }
    suspend fun getAllWaterClarityForEvent(eventId: String): List<WaterClarity> {
        return waterDao.getAllWaterClarityForEvent(eventId).toDomainList()
    }
    suspend fun getAllWaterConditionsForEvent(eventId: String): List<Water> {
        return waterDao.getAllWaterConditionsForEvent(eventId).toWaterDomainList()
    }
    suspend fun getAllWeatherConditionsForEvent(eventId: String): List<Weather> {
        return weatherDao.getAllWeatherConditionsForEvent(eventId).toWeatherDomainList()
    }

    suspend fun addBodyOfWater(bodyOfWater: BodyOfWater) {
        bodyOfWaterDao.insertBodyOfWater(bodyOfWater.toEntity())
    }
    suspend fun updateBodyOfWater(bodyOfWater: BodyOfWater) {
        bodyOfWaterDao.updateBodyOfWater(bodyOfWater.toEntity())
    }
    suspend fun deleteBodyOfWater(bodyOfWater: BodyOfWater) {
        bodyOfWaterDao.deleteBodyOfWater(bodyOfWater.toEntity())
    }
    suspend fun insertBodyOfWaterForTrip(
        crossRef: TripBodyOfWater,
        cascade: Boolean = true) {
        database.withTransaction {
            bodyOfWaterDao.upsertBodyOfWaterForTrip(crossRef.toEntity())
            if (cascade) {
                val eventIds = eventDao.getEventIdsForTrip(crossRef.tripId)
                if (eventIds.isNotEmpty()) {
                    val crossRefs = eventIds.map { eventId ->
                        EventBodyOfWater(eventId = eventId, bodyOfWaterId = crossRef.bodyOfWaterId)
                    }
                    bodyOfWaterDao.upsertBodyOfWaterForEvents(crossRefs.toEventBodyOfWaterEntityList())
                }
            }
        }
    }
    suspend fun deleteBodyOfWaterForTrip(tripId: String, bodyOfWaterId: String) {
        database.withTransaction {
            bodyOfWaterDao.deleteBodyOfWaterForTrip(tripId, bodyOfWaterId)
            val eventIds = eventDao.getEventIdsForTrip(tripId)
            bodyOfWaterDao.deleteBodyOfWaterForEvents(eventIds, bodyOfWaterId)
        }
    }
    suspend fun insertBodyOfWaterForEvent(
        crossRef: EventBodyOfWater,
        cascade: Boolean = true) {
        database.withTransaction {
            bodyOfWaterDao.upsertBodyOfWaterForEvent(crossRef.toEntity())
            if (cascade) {
                val tripId = eventDao.getTripIdForEvent(crossRef.eventId)
                bodyOfWaterDao.upsertBodyOfWaterForTrip(
                    TripBodyOfWaterEntity(
                        tripId = tripId,
                        bodyOfWaterId = crossRef.bodyOfWaterId
                    )
                )
            }
        }
    }
    suspend fun deleteBodyOfWaterForEvent(eventId: String, bodyOfWaterId: String) =
        bodyOfWaterDao.deleteBodyOfWaterForEvent(eventId, bodyOfWaterId)
    suspend fun getAllBodiesOfWaterForEvent(eventId: String): List<BodyOfWater> {
        return bodyOfWaterDao.getAllBodiesOfWaterForEvent(eventId).toBodyOfWaterDomainList()
    }
    suspend fun getEventBodiesOfWaterForEvent(eventId: String): List<EventBodyOfWater> {
        return bodyOfWaterDao.getEventBodiesOfWaterForEvent(eventId).toEventBodyOfWaterDomainList()
    }
    suspend fun findOrCreateBodyOfWater(item: BodyOfWater): InsertResult<BodyOfWater> {
        // Check for existing ID and equality
        val existingId = bodyOfWaterDao.getBodyOfWaterById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing name and equality
            val existingData = bodyOfWaterDao.getBodyOfWaterByName(item.name)
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    addBodyOfWater(newObject)
                    InsertResult(newObject, true)
                } else {
                    addBodyOfWater(item)
                    InsertResult(item, true)
                }
            }
        }
    }
    suspend fun findOrCreateSkyCondition(item: SkyCondition): InsertResult<SkyCondition> {
        // Check for existing ID and equality
        val existingId = weatherDao.getSkyConditionById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = weatherDao.getSkyConditionByName(item.name)
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    weatherDao.insertSkyCondition(newObject.toEntity())
                    InsertResult(newObject, true)
                } else {
                    weatherDao.insertSkyCondition(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }
    suspend fun findOrCreateWaterClarity(item: WaterClarity): InsertResult<WaterClarity> {
        // Check for existing ID and equality
        val existingId = waterDao.getWaterClarityById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = waterDao.getWaterClarityByName(item.name)
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    waterDao.insertWaterClarity(newObject.toEntity())
                    InsertResult(newObject, true)
                } else {
                    waterDao.insertWaterClarity(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }
    suspend fun findOrCreateWaterCondition(item: Water): InsertResult<Water> {
        // Check for existing ID and equality
        val existingId = waterDao.getWaterById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = waterDao.getWaterByData(
                tripId = item.tripId,
                eventId = item.eventId,
                depth = item.depth,
                temperature = item.temperature,
                clarityId = item.clarityId,
                timestamp = item.timestamp
            )
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    waterDao.insertWater(newObject.toEntity())
                    InsertResult(newObject, true)
                } else {
                    waterDao.insertWater(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }
    suspend fun findOrCreateWeatherCondition(item: Weather): InsertResult<Weather> {
        // Check for existing ID and equality
        val existingId = weatherDao.getWeatherById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = weatherDao.getWeatherByData(
                tripId = item.tripId,
                eventId = item.eventId,
                temperature = item.temperature,
                skyConditionId = item.skyConditionId,
                windDirection = item.windDirection,
                windSpeed = item.windSpeed,
                atmosphericPressure = item.atmosphericPressure,
                airVisibility = item.airVisibility,
                airHumidity = item.airHumidity,
                timestamp = item.timestamp,
            )
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    weatherDao.insertWeather(newObject.toEntity())
                    InsertResult(newObject, true)
                } else {
                    weatherDao.insertWeather(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }

    suspend fun addWater(water: Water) {
        waterDao.insertWater(water.toEntity())
    }
    suspend fun upsertWater(water: Water) {
        waterDao.upsertWater(water.toEntity())
    }
    suspend fun deleteWater(id: String) {
        waterDao.deleteWater(id)
    }

    suspend fun addWaterClarity(waterClarity: WaterClarity) {
        waterDao.insertWaterClarity(waterClarity.toEntity())
    }
    suspend fun updateWaterClarity(waterClarity: WaterClarity) {
        waterDao.updateWaterClarity(waterClarity.toEntity())
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
    suspend fun deleteWeather(id: String) {
        weatherDao.deleteWeather(id)
    }

    suspend fun addSkyCondition(skyCondition: SkyCondition) {
        weatherDao.insertSkyCondition(skyCondition.toEntity())
    }
    suspend fun updateSkyCondition(skyCondition: SkyCondition) {
        weatherDao.updateSkyCondition(skyCondition.toEntity())
    }
    suspend fun deleteSkyCondition(skyCondition: SkyCondition) {
        weatherDao.deleteSkyCondition(skyCondition.toEntity())
    }
}