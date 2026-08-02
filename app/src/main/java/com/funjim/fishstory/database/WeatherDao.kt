package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkyCondition(skyConditionEntity: SkyConditionEntity)

    @Upsert
    suspend fun upsertSkyCondition(skyConditionEntity: SkyConditionEntity)

    @Query("SELECT * FROM sky_condition_table ORDER BY name ASC")
    fun getAllSkyConditions(): Flow<List<SkyConditionEntity>>

    @Query("SELECT * FROM sky_condition_table WHERE id = :id")
    fun getSkyCondition(id: String): Flow<SkyConditionEntity?>

    @Query("DELETE FROM sky_condition_table")
    suspend fun deleteAllSkyConditions()

    @Delete
    suspend fun deleteSkyCondition(skyConditionEntity: SkyConditionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)

    @Upsert
    suspend fun upsertWeather(weatherEntity: WeatherEntity)

    @Query("SELECT * FROM weather_table ORDER BY timestamp DESC")
    fun getAllWeather(): Flow<List<WeatherEntity>>

    @Query("SELECT * FROM weather_table WHERE id = :id")
    fun getWeather(id: String): Flow<WeatherEntity?>

    @Query("DELETE FROM weather_table")
    suspend fun deleteAllWeather()

    @Delete
    suspend fun deleteWeather(weatherEntity: WeatherEntity)

    @Query("DELETE FROM weather_table WHERE id = :id")
    suspend fun deleteWeather(id: String)
}