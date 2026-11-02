package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.SkyConditionEntity
import com.funjim.fishstory.database.entity.WaterClarityEntity
import com.funjim.fishstory.database.entity.WeatherEntity
import com.funjim.fishstory.model.SkyCondition
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Delete
    suspend fun deleteSkyCondition(skyConditionEntity: SkyConditionEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertSkyCondition(skyConditionEntity: SkyConditionEntity)
    @Update
    suspend fun updateSkyCondition(skyConditionEntity: SkyConditionEntity)

    @Query("DELETE FROM sky_condition_table")
    suspend fun deleteAllSkyConditions()
    @Query("SELECT * FROM sky_condition_table ORDER BY name ASC")
    fun getAllSkyConditions(): Flow<List<SkyConditionEntity>>
    @Query("""
        SELECT sky_condition_table.* FROM sky_condition_table 
        INNER JOIN weather_table ON sky_condition_table.id = weather_table.skyConditionId  
        WHERE weather_table.eventId = :eventId
    """)
    fun getAllSkyConditionsForEvent(eventId: String): Flow<List<SkyConditionEntity>>

    @Query("SELECT * FROM sky_condition_table WHERE id = :id")
    fun getSkyCondition(id: String): Flow<SkyConditionEntity?>
    @Query("SELECT * FROM sky_condition_table WHERE id = :id")
    suspend fun getSkyConditionById(id: String): SkyConditionEntity?
    @Query("SELECT * FROM sky_condition_table WHERE name = :name")
    suspend fun getSkyConditionByName(name: String): SkyConditionEntity?

    @Query("DELETE FROM weather_table WHERE id = :id")
    suspend fun deleteWeather(id: String)
    @Delete
    suspend fun deleteWeather(weatherEntity: WeatherEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)
    @Upsert
    suspend fun upsertWeather(weatherEntity: WeatherEntity)

    @Query("DELETE FROM weather_table")
    suspend fun deleteAllWeather()
    @Query("SELECT * FROM weather_table ORDER BY timestamp DESC")
    fun getAllWeather(): Flow<List<WeatherEntity>>

    @Query("""
        SELECT weather_table.* FROM weather_table 
        WHERE weather_table.eventId = :eventId
    """)
    fun getAllWeatherConditionsForEvent(eventId: String): Flow<List<WeatherEntity>>

    @Query("SELECT * FROM weather_table WHERE id = :id")
    fun getWeather(id: String): Flow<WeatherEntity?>
    @Query("SELECT * FROM weather_table WHERE id = :id")
    suspend fun getWeatherById(id: String): WeatherEntity?
}