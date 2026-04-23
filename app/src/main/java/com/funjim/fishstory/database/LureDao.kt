package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface LureDao {
    // Lure queries
    @Query("SELECT * FROM lure_table")
    fun getAllLures(): Flow<List<Lure>>

    @Query("DELETE FROM lure_table")
    suspend fun deleteAllLures()

    @Query("SELECT * FROM lure_table WHERE id = :id")
    suspend fun getLureById(id: String): Lure?

    @Query("""
        SELECT * FROM lure_table 
        WHERE name = :name 
        AND primaryColorId IS :primaryColorId 
        AND secondaryColorId IS :secondaryColorId 
        AND glows = :glows
        AND glowColorId IS :glowColorId 
    """)
    suspend fun getLureByExactMatch(name: String, primaryColorId: String?, secondaryColorId: String?, glows: Boolean, glowColorId: String?): Lure?

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(lure: Lure): String {
        val existingLure = getLureByExactMatch(
            name = lure.name,
            primaryColorId = lure.primaryColorId,
            secondaryColorId = lure.secondaryColorId,
            glows = lure.glows,
            glowColorId = lure.glowColorId
        )
        return if (existingLure != null) {
            existingLure.id
        } else {
            upsertLure(lure)
            lure.id
        }
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLure(lure: Lure)

    @Upsert
    suspend fun upsertLure(lure: Lure)

    @Delete
    suspend fun deleteLure(lure: Lure)

    // LureColor queries
    @Query("SELECT * FROM lure_color_table")
    fun getAllLureColors(): Flow<List<LureColor>>

    @Query("SELECT * FROM lure_color_table")
    suspend fun getAllLureColorsList(): List<LureColor>

    @Query("DELETE FROM lure_color_table")
    suspend fun deleteAllLureColors()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLureColor(color: LureColor)

    @Upsert
    suspend fun upsertLureColor(color: LureColor)

    @Delete
    suspend fun deleteLureColor(color: LureColor)
}
