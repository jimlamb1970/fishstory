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

    fun getAllSkyConditionsForEvent(eventId: String): Flow<List<SkyCondition>> {
        return weatherDao.getAllSkyConditionsForEvent(eventId)
            .map { list -> list.toDomainList() }
    }
    fun getAllWaterClarityForEvent(eventId: String): Flow<List<WaterClarity>> {
        return waterDao.getAllWaterClarityForEvent(eventId)
            .map { list -> list.toDomainList() }
    }

    fun getAllWaterConditionsForEvent(eventId: String): Flow<List<Water>> {
        return waterDao.getAllWaterConditionsForEvent(eventId)
            .map { list -> list.toWaterDomainList() }
    }

    fun getAllWeatherConditionsForEvent(eventId: String): Flow<List<Weather>> {
        return weatherDao.getAllWeatherConditionsForEvent(eventId)
            .map { list -> list.toWeatherDomainList() }
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
    fun getAllBodiesOfWaterForEvent(eventId: String): Flow<List<BodyOfWater>> {
        return bodyOfWaterDao.getAllBodiesOfWaterForEvent(eventId).map { list -> list.toBodyOfWaterDomainList() }
    }
    fun getEventBodiesOfWaterForEvent(eventId: String): Flow<List<EventBodyOfWater>> {
        return bodyOfWaterDao.getEventBodiesOfWaterForEvent(eventId).map { list -> list.toEventBodyOfWaterDomainList() }
    }
    suspend fun findOrCreateBodyOfWater(item: BodyOfWater): InsertResult<BodyOfWater> {
        // Check for existing ID and equality
        val existingId = bodyOfWaterDao.getBodyOfWaterById(item.id)
        return if ((existingId != null) && (existingId.equals(item))) {
            InsertResult(existingId.toDomain(), false)
        } else {
            // Check for existing name and equality
            val existingData = bodyOfWaterDao.getBodyOfWaterByName(item.name)
            if ((existingData != null) && (existingData.equals(item))) {
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
    suspend fun findOrCreateSkyCondition(skyCondition: SkyCondition): InsertResult<SkyCondition> {
        val existing = weatherDao.getSkyConditionById(skyCondition.id)
        return if (existing != null) {
            InsertResult(existing.toDomain(), false)
        } else {
            val existing = weatherDao.getSkyConditionByName(skyCondition.name)
            if (existing != null) {
                InsertResult(existing.toDomain(), false)
            } else {
                weatherDao.insertSkyCondition(skyCondition.toEntity())
                InsertResult(skyCondition, true)
            }
        }
    }
    suspend fun findOrCreateWaterClarity(waterClarity: WaterClarity): InsertResult<WaterClarity> {
        val existing = waterDao.getWaterClarityById(waterClarity.id)
        return if (existing != null) {
            InsertResult(existing.toDomain(), false)
        } else {
            val existing = waterDao.getWaterClarityByName(waterClarity.name)
            if (existing != null) {
                InsertResult(existing.toDomain(), false)
            } else {
                waterDao.insertWaterClarity(waterClarity.toEntity())
                InsertResult(waterClarity, true)
            }
        }
    }
    suspend fun findOrCreateWaterCondition(water: Water): InsertResult<Water> {
        val existing = waterDao.getWaterById(water.id)
        return if (existing != null) {
            InsertResult(existing.toDomain(), false)
        } else {
            waterDao.insertWater(water.toEntity())
            InsertResult(water, true)
        }
    }
    suspend fun findOrCreateWeatherCondition(weather: Weather): InsertResult<Weather> {
        val existing = weatherDao.getWeatherById(weather.id)
        return if (existing != null) {
            InsertResult(existing.toDomain(), false)
        } else {
            weatherDao.insertWeather(weather.toEntity())
            InsertResult(weather, true)
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