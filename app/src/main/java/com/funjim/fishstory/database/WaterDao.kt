package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterClarity(waterClarity: WaterClarity)

    @Upsert
    suspend fun upsertWaterClarity(waterClarity: WaterClarity)

    @Query("SELECT * FROM water_clarity_table ORDER BY name ASC")
    fun getAllWaterClarity(): Flow<List<WaterClarity>>

    @Query("SELECT * FROM water_clarity_table WHERE id = :id")
    fun getWaterClarity(id: String): Flow<WaterClarity?>

    @Query("DELETE FROM water_clarity_table")
    suspend fun deleteAllWaterClarity()

    @Delete
    suspend fun deleteWaterClarity(waterClarity: WaterClarity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(water: Water)

    @Upsert
    suspend fun upsertWater(water: Water)

    @Query("SELECT * FROM water_table ORDER BY timestamp DESC")
    fun getAllWater(): Flow<List<Water>>

    @Query("SELECT * FROM water_table WHERE id = :id")
    fun getWater(id: String): Flow<Water?>

    @Query("DELETE FROM water_table")
    suspend fun deleteAllWater()

    @Delete
    suspend fun deleteWater(water: Water)

    @Query("DELETE FROM water_table WHERE id = :waterId")
    suspend fun deleteWater(waterId: String)

}