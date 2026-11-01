package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.LureColorEntity
import com.funjim.fishstory.database.entity.LureEntity
import com.funjim.fishstory.database.entity.LureEntityWithColors
import com.funjim.fishstory.database.entity.LureEntityWithColorsSummary
import com.funjim.fishstory.database.entity.LureEntityWithDetails
import com.funjim.fishstory.database.entity.LureEntityWithPhotos
import com.funjim.fishstory.database.entity.LureGlowColorEntity
import com.funjim.fishstory.database.entity.LurePrimaryColorEntity
import com.funjim.fishstory.database.entity.LureSecondaryColorEntity
import com.funjim.fishstory.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LureDao {
    // Lure queries
    @Query("SELECT * FROM lure_table")
    fun getAllLures(): Flow<List<LureEntity>>
    @Query("DELETE FROM lure_table")
    suspend fun deleteAllLures()

    @Delete
    suspend fun deleteLure(lure: LureEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLure(lure: LureEntity)
    @Upsert
    suspend fun upsertLure(lure: LureEntity)

    @Query("SELECT * FROM lure_table WHERE id = :id")
    fun getLure(id: String): Flow<LureEntity?>
    @Query("SELECT * FROM lure_table WHERE id = :id")
    suspend fun getLureById(id: String): LureEntity?

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithPhotos(lureId: String): LureEntityWithPhotos?

    @Transaction
    @Query("SELECT * FROM lure_table")
    fun getAllLuresWithColors(): Flow<List<LureEntityWithColors>>
    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    fun getLureWithColors(lureId: String): Flow<LureEntityWithColors?>

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithDetails(lureId: String): LureEntityWithDetails?

    @Query("""
        SELECT lure_table.* FROM lure_table
        INNER JOIN tackle_box_lure_cross_ref ON lure_table.id = tackle_box_lure_cross_ref.lureId
        INNER JOIN event_fisherman_cross_ref ON tackle_box_lure_cross_ref.tackleBoxId = event_fisherman_cross_ref.tackleBoxId
        WHERE event_fisherman_cross_ref.eventId = :eventId
        
        UNION
        
        SELECT lure_table.* FROM lure_table 
        INNER JOIN fish_table ON lure_table.id = fish_table.lureId  
        WHERE fish_table.eventId = :eventId
    """)
    fun getAllLuresForEvent(eventId: String): Flow<List<LureEntity>>

    @Query("DELETE FROM lure_primary_color_cross_ref WHERE lureId = :lureId")
    suspend fun deletePrimaryColorsForLure(lureId: String)

    @Query("DELETE FROM lure_secondary_color_cross_ref WHERE lureId = :lureId")
    suspend fun deleteSecondaryColorsForLure(lureId: String)

    @Query("DELETE FROM lure_glow_color_cross_ref WHERE lureId = :lureId")
    suspend fun deleteGlowColorsForLure(lureId: String)

    @Upsert
    suspend fun upsertLurePrimaryColor(crossRef: LurePrimaryColorEntity)
    @Upsert
    suspend fun upsertLureSecondaryColor(crossRef: LureSecondaryColorEntity)
    @Upsert
    suspend fun upsertLureGlowColor(crossRef: LureGlowColorEntity)


    // LureColor queries
    @Query("SELECT * FROM lure_color_table")
    fun getAllLureColors(): Flow<List<LureColorEntity>>
    @Query("SELECT * FROM lure_color_table")
    suspend fun getAllLureColorsList(): List<LureColorEntity>
    @Query("DELETE FROM lure_color_table")
    suspend fun deleteAllLureColors()

    @Delete
    suspend fun deleteLureColor(color: LureColorEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertLureColor(color: LureColorEntity)
    @Update
    suspend fun updateLureColor(color: LureColorEntity)

    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure_cross_ref.lureId FROM tackle_box_lure_cross_ref
            INNER JOIN event_fisherman_cross_ref ON tackle_box_lure_cross_ref.tackleBoxId = event_fisherman_cross_ref.tackleBoxId
            WHERE event_fisherman_cross_ref.eventId = :eventId
            
            UNION
            
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        ),
        LureColors AS (
            SELECT colorId FROM lure_primary_color_cross_ref WHERE lureId IN (SELECT lureId FROM EventLures)
            UNION
            SELECT colorId FROM lure_secondary_color_cross_ref WHERE lureId IN (SELECT lureId FROM EventLures)
            UNION
            SELECT colorId FROM lure_glow_color_cross_ref WHERE lureId IN (SELECT lureId FROM EventLures)
        )
        SELECT lure_color_table.* FROM lure_color_table
        INNER JOIN LureColors ON lure_color_table.id = LureColors.colorId
    """
    )
    fun getAllLureColorsForEvent(eventId: String): Flow<List<LureColorEntity>>
    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure_cross_ref.lureId FROM tackle_box_lure_cross_ref
            INNER JOIN event_fisherman_cross_ref ON tackle_box_lure_cross_ref.tackleBoxId = event_fisherman_cross_ref.tackleBoxId
            WHERE event_fisherman_cross_ref.eventId = :eventId
            
            UNION
            
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        )
        SELECT * FROM lure_primary_color_cross_ref WHERE lureId IN (SELECT lureId FROM EventLures)
    """
    )
    fun getLurePrimaryColorsForEvent(eventId: String): Flow<List<LurePrimaryColorEntity>>
    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure_cross_ref.lureId FROM tackle_box_lure_cross_ref
            INNER JOIN event_fisherman_cross_ref ON tackle_box_lure_cross_ref.tackleBoxId = event_fisherman_cross_ref.tackleBoxId
            WHERE event_fisherman_cross_ref.eventId = :eventId
            
            UNION
            
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        )
        SELECT * FROM lure_secondary_color_cross_ref WHERE lureId IN (SELECT lureId FROM EventLures)
    """
    )
    fun getLureSecondaryColorsForEvent(eventId: String): Flow<List<LureSecondaryColorEntity>>
    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure_cross_ref.lureId FROM tackle_box_lure_cross_ref
            INNER JOIN event_fisherman_cross_ref ON tackle_box_lure_cross_ref.tackleBoxId = event_fisherman_cross_ref.tackleBoxId
            WHERE event_fisherman_cross_ref.eventId = :eventId
            
            UNION
            
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        )
        SELECT * FROM lure_glow_color_cross_ref WHERE lureId IN (SELECT lureId FROM EventLures)
    """
    )
    fun getLureGlowColorsForEvent(eventId: String): Flow<List<LureGlowColorEntity>>

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