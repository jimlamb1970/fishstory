package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.SpeciesEntity
import com.funjim.fishstory.database.entity.WaterClarityEntity
import com.funjim.fishstory.database.entity.WaterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Delete
    suspend fun deleteWaterClarity(waterClarity: WaterClarityEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertWaterClarity(waterClarity: WaterClarityEntity)
    @Update
    suspend fun updateWaterClarity(waterClarity: WaterClarityEntity)

    @Query("DELETE FROM water_clarity_table")
    suspend fun deleteAllWaterClarity()
    @Query("SELECT * FROM water_clarity_table ORDER BY name ASC")
    fun getAllWaterClarity(): Flow<List<WaterClarityEntity>>

    @Query("""
        SELECT water_clarity_table.* FROM water_clarity_table 
        INNER JOIN water_table ON water_clarity_table.id = water_table.clarityId  
        WHERE water_table.eventId = :eventId
    """)
    fun getAllWaterClarityForEvent(eventId: String): Flow<List<WaterClarityEntity>>

    @Query("SELECT * FROM water_clarity_table WHERE id = :id")
    fun getWaterClarity(id: String): Flow<WaterClarityEntity?>
    @Query("SELECT * FROM water_clarity_table WHERE id = :id")
    suspend fun getWaterClarityById(id: String): WaterClarityEntity?
    @Query("SELECT * FROM water_clarity_table WHERE name = :name")
    suspend fun getWaterClarityByName(name: String): WaterClarityEntity?

    @Query("DELETE FROM water_table WHERE id = :id")
    suspend fun deleteWater(id: String)
    @Delete
    suspend fun deleteWater(water: WaterEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertWater(water: WaterEntity)
    @Upsert
    suspend fun upsertWater(water: WaterEntity)

    @Query("DELETE FROM water_table")
    suspend fun deleteAllWater()
    @Query("SELECT * FROM water_table ORDER BY timestamp DESC")
    fun getAllWater(): Flow<List<WaterEntity>>

    @Query("""
        SELECT water_table.* FROM water_table 
        WHERE water_table.eventId = :eventId
    """)
    fun getAllWaterConditionsForEvent(eventId: String): Flow<List<WaterEntity>>

    @Query("SELECT * FROM water_table WHERE id = :id")
    fun getWater(id: String): Flow<WaterEntity?>
    @Query("SELECT * FROM water_table WHERE id = :id")
    suspend fun getWaterById(id: String): WaterEntity?
    @Query("""
        SELECT * FROM water_table 
        WHERE tripId = :tripId 
          AND eventId = :eventId 
          AND depth = :depth 
          AND temperature = :temperature 
          AND clarityId = :clarityId 
          AND timestamp = :timestamp
        LIMIT 1
    """)
    suspend fun getWaterByData(
        tripId: String,
        eventId: String,
        depth: Long?,
        temperature: Long?,
        clarityId: String?,
        timestamp: Long
    ): WaterEntity?
}
