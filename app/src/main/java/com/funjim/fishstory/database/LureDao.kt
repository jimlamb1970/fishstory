package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import kotlinx.coroutines.flow.Flow

@Dao
interface LureDao {
    // Lure queries
    @Query("SELECT * FROM lure_table")
    fun getAllLures(): Flow<List<Lure>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLure(lure: Lure): Long

    @Delete
    suspend fun deleteLure(lure: Lure)

    // LureColor queries
    @Query("SELECT * FROM lure_color_table")
    fun getAllLureColors(): Flow<List<LureColor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLureColor(color: LureColor): Long

    @Delete
    suspend fun deleteLureColor(color: LureColor)
}
