package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterClarity(waterClarity: WaterClarityEntity)

    @Upsert
    suspend fun upsertWaterClarity(waterClarity: WaterClarityEntity)

    @Query("SELECT * FROM water_clarity_table ORDER BY name ASC")
    fun getAllWaterClarity(): Flow<List<WaterClarityEntity>>

    @Query("SELECT * FROM water_clarity_table WHERE id = :id")
    fun getWaterClarity(id: String): Flow<WaterClarityEntity?>

    @Query("DELETE FROM water_clarity_table")
    suspend fun deleteAllWaterClarity()

    @Delete
    suspend fun deleteWaterClarity(waterClarity: WaterClarityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(water: WaterEntity)

    @Upsert
    suspend fun upsertWater(water: WaterEntity)

    @Query("SELECT * FROM water_table ORDER BY timestamp DESC")
    fun getAllWater(): Flow<List<WaterEntity>>

    @Query("SELECT * FROM water_table WHERE id = :id")
    fun getWater(id: String): Flow<WaterEntity?>

    @Query("DELETE FROM water_table")
    suspend fun deleteAllWater()

    @Delete
    suspend fun deleteWater(water: WaterEntity)

    @Query("DELETE FROM water_table WHERE id = :id")
    suspend fun deleteWater(id: String)
}