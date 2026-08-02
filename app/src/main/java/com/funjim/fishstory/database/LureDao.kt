package com.funjim.fishstory.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LureDao {
    // Lure queries
    @Query("SELECT * FROM lure_table")
    fun getAllLures(): Flow<List<LureEntity>>

    @Query("DELETE FROM lure_table")
    suspend fun deleteAllLures()

    @Query("SELECT * FROM lure_table WHERE id = :id")
    suspend fun getLureById(id: String): LureEntity?
    @Query("SELECT * FROM lure_table WHERE id = :id")
    fun getLure(id: String): Flow<LureEntity?>

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithPhotos(lureId: String): LureEntityWithPhotos?

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    fun getLureWithColors(lureId: String): Flow<LureEntityWithColors?>

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithDetails(lureId: String): LureEntityWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLure(lure: LureEntity)

    @Upsert
    suspend fun upsertLure(lure: LureEntity)

    @Upsert
    suspend fun upsertLurePrimaryColor(crossRef: LurePrimaryColorEntity)

    @Upsert
    suspend fun upsertLureSecondaryColor(crossRef: LureSecondaryColorEntity)

    @Upsert
    suspend fun upsertLureGlowColor(crossRef: LureGlowColorEntity)

    @Delete
    suspend fun deleteLure(lure: LureEntity)

    // LureColor queries
    @Query("SELECT * FROM lure_color_table")
    fun getAllLureColors(): Flow<List<LureColorEntity>>
    @Query("DELETE FROM lure_color_table")
    suspend fun deleteAllLureColors()

    @Query("SELECT * FROM lure_primary_color_cross_ref")
    fun getAllLurePrimaryColors(): Flow<List<LurePrimaryColorEntity>>
    @Query("DELETE FROM lure_primary_color_cross_ref")
    suspend fun deleteAllLurePrimaryColorCrossRefs()

    @Query("SELECT * FROM lure_secondary_color_cross_ref")
    fun getAllLureSecondaryColors(): Flow<List<LureSecondaryColorEntity>>
    @Query("DELETE FROM lure_secondary_color_cross_ref")
    suspend fun deleteAllLureSecondaryColorCrossRefs()

    @Query("SELECT * FROM lure_glow_color_cross_ref")
    fun getAllLureGlowColors(): Flow<List<LureGlowColorEntity>>
    @Query("DELETE FROM lure_glow_color_cross_ref")
    suspend fun deleteAllLureGlowColorCrossRefs()

    @Query("SELECT * FROM lure_color_table")
    suspend fun getAllLureColorsList(): List<LureColorEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLureColor(color: LureColorEntity)

    @Upsert
    suspend fun upsertLureColor(color: LureColorEntity)

    @Delete
    suspend fun deleteLureColor(color: LureColorEntity)

    @Transaction
    @Query("SELECT * FROM lure_table")
    fun getLuresWithColors(): Flow<List<LureEntityWithColors>>

    @Transaction
    @Query("""
    SELECT 
        l.*, 
        SUM(f.caughtCount) AS fishCaught,
        SUM(f.keptCount) AS fishKept,

        (
            SELECT COALESCE(SUM(
                CASE 
                    WHEN target.eventId IS NOT NULL THEN ft_sub.caughtCount 
                    ELSE 0 
                END
            ), 0)
            FROM fish_table AS ft_sub
            -- Join inside the subquery to calculate 'isTarget' dynamic flag
            LEFT JOIN event_target_species AS target 
                ON ft_sub.eventId = target.eventId 
                AND ft_sub.speciesId = target.speciesId
            WHERE ft_sub.lureId = l.id
        ) as targetFishCaught,

        (
            SELECT COALESCE(SUM(
                CASE 
                    WHEN target.eventId IS NOT NULL THEN ft_sub.keptCount 
                    ELSE 0 
                END
            ), 0)
            FROM fish_table AS ft_sub
            LEFT JOIN event_target_species AS target 
                ON ft_sub.eventId = target.eventId 
                AND ft_sub.speciesId = target.speciesId
            WHERE ft_sub.lureId = l.id
        ) as targetFishKept,

        MAX(f.length) AS largestFish,
        MIN(f.length) AS smallestFish
    FROM lure_table AS l
    LEFT JOIN fish_table AS f ON l.id = f.lureId
    GROUP BY l.id
""")
    fun getLureSummariesWithColors(): Flow<List<LureEntityWithColorsSummary>>

    @Transaction
    @Query("""
        SELECT DISTINCT lure_table.* FROM lure_table 
        INNER JOIN fish_table ON lure_table.id = fish_table.lureId 
        WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
          AND (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY lure_table.id
    """)
    fun getLuresWithFish(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<List<LureEntityWithColors>>
}
