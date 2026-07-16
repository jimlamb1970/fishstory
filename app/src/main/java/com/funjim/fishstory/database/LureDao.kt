package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureGlowColorCrossRef
import com.funjim.fishstory.model.LurePrimaryColorCrossRef
import com.funjim.fishstory.model.LureSecondaryColorCrossRef
import com.funjim.fishstory.model.LureSummary
import com.funjim.fishstory.model.LureSummaryWithColors
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.LureWithDetails
import com.funjim.fishstory.model.LureWithPhotos
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
    @Query("SELECT * FROM lure_table WHERE id = :id")
    fun getLure(id: String): Flow<Lure?>

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithPhotos(lureId: String): LureWithPhotos?

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    fun getLureWithColors(lureId: String): Flow<LureWithColors?>

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithDetails(lureId: String): LureWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLure(lure: Lure)

    @Upsert
    suspend fun upsertLure(lure: Lure)

    @Upsert
    suspend fun upsertLurePrimaryColorCrossRef(crossRef: LurePrimaryColorCrossRef)

    @Upsert
    suspend fun upsertLureSecondaryColorCrossRef(crossRef: LureSecondaryColorCrossRef)

    @Upsert
    suspend fun upsertLureGlowColorCrossRef(crossRef: LureGlowColorCrossRef)

    @Delete
    suspend fun deleteLure(lure: Lure)

    // LureColor queries
    @Query("SELECT * FROM lure_color_table")
    fun getAllLureColors(): Flow<List<LureColor>>
    @Query("DELETE FROM lure_color_table")
    suspend fun deleteAllLureColors()

    @Query("SELECT * FROM lure_primary_color_cross_ref")
    fun getAllLurePrimaryColorCrossRefs(): Flow<List<LurePrimaryColorCrossRef>>
    @Query("DELETE FROM lure_primary_color_cross_ref")
    suspend fun deleteAllLurePrimaryColorCrossRefs()

    @Query("SELECT * FROM lure_secondary_color_cross_ref")
    fun getAllLureSecondaryColorCrossRefs(): Flow<List<LureSecondaryColorCrossRef>>
    @Query("DELETE FROM lure_secondary_color_cross_ref")
    suspend fun deleteAllLureSecondaryColorCrossRefs()

    @Query("SELECT * FROM lure_glow_color_cross_ref")
    fun getAllLureGlowColorCrossRefs(): Flow<List<LureGlowColorCrossRef>>
    @Query("DELETE FROM lure_glow_color_cross_ref")
    suspend fun deleteAllLureGlowColorCrossRefs()

    @Query("SELECT * FROM lure_color_table")
    suspend fun getAllLureColorsList(): List<LureColor>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLureColor(color: LureColor)

    @Upsert
    suspend fun upsertLureColor(color: LureColor)

    @Delete
    suspend fun deleteLureColor(color: LureColor)

    /*
    @Query("""
    SELECT *, 
           p.name AS primaryName, 
           s.name AS secondaryName, 
           g.name AS glowName
    FROM lure_table
    LEFT JOIN lure_color_table p ON primaryColorId = p.id
    LEFT JOIN lure_color_table s ON secondaryColorId = s.id
    LEFT JOIN lure_color_table g ON glowColorId = g.id
""")
    fun getLuresWithNames(): Flow<List<LureWithNamesTuple>>
*/

    @Transaction
    @Query("""
    SELECT 
        l.*, 
        SUM(f.caughtCount) AS caughtCount,
        SUM(f.keptCount) AS keptCount,
        MAX(f.length) AS largestFish,
        MIN(f.length) AS smallestFish
    FROM lure_table AS l
    LEFT JOIN fish_table AS f ON l.id = f.lureId
    GROUP BY l.id
""")
    fun getLureSummaries(): Flow<List<LureSummary>>

    @Transaction
    @Query("SELECT * FROM lure_table")
    fun getLuresWithColors(): Flow<List<LureWithColors>>

    @Transaction
    @Query("""
    SELECT 
        l.*, 
        SUM(f.caughtCount) AS caughtCount,
        SUM(f.keptCount) AS keptCount,
        MAX(f.length) AS largestFish,
        MIN(f.length) AS smallestFish
    FROM lure_table AS l
    LEFT JOIN fish_table AS f ON l.id = f.lureId
    GROUP BY l.id
""")
    fun getLureSummariesWithColors(): Flow<List<LureSummaryWithColors>>

    @Transaction
    @Query("""
        SELECT DISTINCT lure_table.* FROM lure_table 
        INNER JOIN fish_table ON lure_table.id = fish_table.lureId 
        WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
          AND (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY lure_table.id
    """)
    fun getLuresWithFish(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        tripId: String?): Flow<List<LureWithColors>>
}