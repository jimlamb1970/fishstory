package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
import kotlinx.coroutines.flow.Flow

@Dao
interface LureDao {
    @Delete
    suspend fun deleteLure(lure: LureEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLure(lure: LureEntity)
    @Upsert
    suspend fun upsertLure(lure: LureEntity)
    @Query("DELETE FROM lure_table")
    suspend fun deleteAllLures()

    @Delete
    suspend fun deleteLureColor(color: LureColorEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertLureColor(color: LureColorEntity)
    @Upsert
    suspend fun upsertLureColor(color: LureColorEntity)
    @Query("DELETE FROM lure_color_table")
    suspend fun deleteAllLureColors()

    @Query("DELETE FROM lure_primary_color WHERE lureId = :lureId")
    suspend fun deleteLurePrimaryColors(lureId: String)
    @Upsert
    suspend fun upsertLurePrimaryColor(crossRef: LurePrimaryColorEntity)
    @Query("DELETE FROM lure_glow_color")
    suspend fun deleteAllLureGlowColors()

    @Query("DELETE FROM lure_secondary_color WHERE lureId = :lureId")
    suspend fun deleteLureSecondaryColors(lureId: String)
    @Upsert
    suspend fun upsertLureSecondaryColor(crossRef: LureSecondaryColorEntity)
    @Query("DELETE FROM lure_secondary_color")
    suspend fun deleteAllLureSecondaryColors()

    @Query("DELETE FROM lure_glow_color WHERE lureId = :lureId")
    suspend fun deleteLureGlowColors(lureId: String)
    @Upsert
    suspend fun upsertLureGlowColor(crossRef: LureGlowColorEntity)
    @Query("DELETE FROM lure_primary_color")
    suspend fun deleteAllLurePrimaryColors()

    @Query("SELECT * FROM lure_table WHERE id = :id")
    fun getLure(id: String): Flow<LureEntity?>
    @Query("SELECT * FROM lure_table WHERE id = :id")
    suspend fun getLureById(id: String): LureEntity?
    @Query("""
    SELECT * FROM lure_table AS l
    WHERE l.name = :name 
      AND l.hookCount = :hookCount 
      AND l.glows = :glows
      AND (
          SELECT COALESCE(GROUP_CONCAT(colorId, ','), '') 
          FROM (SELECT colorId FROM lure_primary_color WHERE lureId = l.id ORDER BY colorId)
      ) = :sortedPrimaryColors
      AND (
          SELECT COALESCE(GROUP_CONCAT(colorId, ','), '') 
          FROM (SELECT colorId FROM lure_secondary_color WHERE lureId = l.id ORDER BY colorId)
      ) = :sortedSecondaryColors
      AND (
          SELECT COALESCE(GROUP_CONCAT(colorId, ','), '') 
          FROM (SELECT colorId FROM lure_glow_color WHERE lureId = l.id ORDER BY colorId)
      ) = :sortedGlowColors
    LIMIT 1
""")
    suspend fun getLureByData(
        name: String,
        hookCount: Int,
        glows: Boolean,
        sortedPrimaryColors: String,
        sortedSecondaryColors: String,
        sortedGlowColors: String
    ): LureEntity?

    @Query("SELECT * FROM lure_color_table WHERE id = :id")
    suspend fun getLureColorById(id: String): LureColorEntity?
    @Query("""
        SELECT * FROM lure_color_table AS l
        WHERE l.name = :name
          AND l.hexCode IS :hexCode
        LIMIT 1
    """)
    suspend fun getLureColorByData(
        name: String,
        hexCode: String?
    ): LureColorEntity?

    @Query("SELECT * FROM lure_table")
    fun getAllLures(): Flow<List<LureEntity>>
    @Query("SELECT * FROM lure_table")
    suspend fun getAllLuresList(): List<LureEntity>
    @Query("""
        SELECT lure_table.* FROM lure_table
        INNER JOIN tackle_box_lure ON lure_table.id = tackle_box_lure.lureId
        INNER JOIN event_fisherman ON tackle_box_lure.tackleBoxId = event_fisherman.tackleBoxId
        WHERE event_fisherman.eventId = :eventId
        UNION
        SELECT lure_table.* FROM lure_table 
        INNER JOIN fish_table ON lure_table.id = fish_table.lureId  
        WHERE fish_table.eventId = :eventId
    """)
    suspend fun getAllLuresForEvent(eventId: String): List<LureEntity>

    @Query("SELECT * FROM lure_color_table")
    fun getAllLureColors(): Flow<List<LureColorEntity>>
    @Query("SELECT * FROM lure_color_table")
    suspend fun getAllLureColorsList(): List<LureColorEntity>
    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure.lureId FROM tackle_box_lure
            INNER JOIN event_fisherman ON tackle_box_lure.tackleBoxId = event_fisherman.tackleBoxId
            WHERE event_fisherman.eventId = :eventId
            
            UNION
            
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        ),
        LureColors AS (
            SELECT colorId FROM lure_primary_color WHERE lureId IN (SELECT lureId FROM EventLures)
            UNION
            SELECT colorId FROM lure_secondary_color WHERE lureId IN (SELECT lureId FROM EventLures)
            UNION
            SELECT colorId FROM lure_glow_color WHERE lureId IN (SELECT lureId FROM EventLures)
        )
        SELECT lure_color_table.* FROM lure_color_table
        INNER JOIN LureColors ON lure_color_table.id = LureColors.colorId
    """
    )
    suspend fun getAllLureColorsForEvent(eventId: String): List<LureColorEntity>

    @Query("SELECT * FROM lure_primary_color")
    fun getAllLurePrimaryColors(): Flow<List<LurePrimaryColorEntity>>
    @Query("SELECT * FROM lure_primary_color")
    suspend fun getAllLurePrimaryColorsList(): List<LurePrimaryColorEntity>
    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure.lureId FROM tackle_box_lure
            INNER JOIN event_fisherman ON tackle_box_lure.tackleBoxId = event_fisherman.tackleBoxId
            WHERE event_fisherman.eventId = :eventId
            UNION
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        )
        SELECT * FROM lure_primary_color WHERE lureId IN (SELECT lureId FROM EventLures)
    """
    )
    suspend fun getLurePrimaryColorsForEvent(eventId: String): List<LurePrimaryColorEntity>

    @Query("SELECT * FROM lure_secondary_color")
    fun getAllLureSecondaryColors(): Flow<List<LureSecondaryColorEntity>>
    @Query("SELECT * FROM lure_secondary_color")
    suspend fun getAllLureSecondaryColorsList(): List<LureSecondaryColorEntity>
    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure.lureId FROM tackle_box_lure
            INNER JOIN event_fisherman ON tackle_box_lure.tackleBoxId = event_fisherman.tackleBoxId
            WHERE event_fisherman.eventId = :eventId
            UNION
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        )
        SELECT * FROM lure_secondary_color WHERE lureId IN (SELECT lureId FROM EventLures)
    """
    )
    suspend fun getLureSecondaryColorsForEvent(eventId: String): List<LureSecondaryColorEntity>

    @Query("SELECT * FROM lure_glow_color")
    fun getAllLureGlowColors(): Flow<List<LureGlowColorEntity>>
    @Query("SELECT * FROM lure_glow_color")
    suspend fun getAllLureGlowColorsList(): List<LureGlowColorEntity>
    @Query(
        """
        WITH EventLures AS (
            -- Lures available through participants' tackle boxes for this event
            SELECT tackle_box_lure.lureId FROM tackle_box_lure
            INNER JOIN event_fisherman ON tackle_box_lure.tackleBoxId = event_fisherman.tackleBoxId
            WHERE event_fisherman.eventId = :eventId 
            UNION
            -- Lures used on fish caught during this event
            SELECT fish_table.lureId FROM fish_table
            WHERE fish_table.eventId = :eventId AND fish_table.lureId IS NOT NULL
        )
        SELECT * FROM lure_glow_color WHERE lureId IN (SELECT lureId FROM EventLures)
    """
    )
    suspend fun getLureGlowColorsForEvent(eventId: String): List<LureGlowColorEntity>

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    fun getLureWithColors(lureId: String): Flow<LureEntityWithColors?>
    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithColorsById(lureId: String): LureEntityWithColors?

    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithDetails(lureId: String): LureEntityWithDetails?
    @Transaction
    @Query("SELECT * FROM lure_table WHERE id = :lureId")
    suspend fun getLureWithPhotos(lureId: String): LureEntityWithPhotos?

    @Transaction
    @Query("SELECT * FROM lure_table")
    fun getAllLuresWithColors(): Flow<List<LureEntityWithColors>>

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
            LEFT JOIN event_target_species AS target 
                ON ft_sub.eventId = target.eventId 
                AND ft_sub.speciesId = target.speciesId
            WHERE ft_sub.lureId = l.id
              AND (:fishermanId IS NULL OR ft_sub.fishermanId = :fishermanId)
              AND (:eventId IS NULL OR ft_sub.eventId = :eventId)
              AND (:tripId IS NULL OR ft_sub.tripId = :tripId)
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
              AND (:fishermanId IS NULL OR ft_sub.fishermanId = :fishermanId)
              AND (:eventId IS NULL OR ft_sub.eventId = :eventId)
              AND (:tripId IS NULL OR ft_sub.tripId = :tripId)
        ) as targetFishKept,

        MAX(f.length) AS largestFish,
        MIN(f.length) AS smallestFish
    FROM lure_table AS l
    INNER JOIN (
        SELECT f_sub.* 
        FROM fish_table AS f_sub
        WHERE (:fishermanId IS NULL OR f_sub.fishermanId = :fishermanId)
          AND (:eventId IS NULL OR f_sub.eventId = :eventId)
          AND (:tripId IS NULL OR f_sub.tripId = :tripId)
    ) AS f ON l.id = f.lureId
    GROUP BY l.id
""")
    fun getLureSummariesWithColors(
        tripId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null
    ): Flow<List<LureEntityWithColorsSummary>>

    @Transaction
    @Query("""
    SELECT 
        l.*, 
        COALESCE(SUM(f.caughtCount), 0) AS fishCaught,
        COALESCE(SUM(f.keptCount), 0) AS fishKept,

        COALESCE(
            (
                SELECT SUM(
                    CASE 
                        WHEN target.eventId IS NOT NULL THEN ft_sub.caughtCount 
                        ELSE 0 
                    END
                )
                FROM fish_table AS ft_sub
                LEFT JOIN event_target_species AS target 
                    ON ft_sub.eventId = target.eventId 
                    AND ft_sub.speciesId = target.speciesId
                WHERE ft_sub.lureId = l.id
                  AND ft_sub.tripId = :tripId
                  AND ft_sub.fishermanId = :fishermanId
            ), 0
        ) as targetFishCaught,

        COALESCE(
            (
                SELECT SUM(
                    CASE 
                        WHEN target.eventId IS NOT NULL THEN ft_sub.keptCount 
                        ELSE 0 
                    END
                )
                FROM fish_table AS ft_sub
                LEFT JOIN event_target_species AS target 
                    ON ft_sub.eventId = target.eventId 
                    AND ft_sub.speciesId = target.speciesId
                WHERE ft_sub.lureId = l.id
                  AND ft_sub.tripId = :tripId
                  AND ft_sub.fishermanId = :fishermanId
            ), 0
        ) as targetFishKept,

        MAX(f.length) AS largestFish,
        MIN(f.length) AS smallestFish
    FROM lure_table AS l
    INNER JOIN tackle_box_lure AS tb_lure ON l.id = tb_lure.lureId
    LEFT JOIN fish_table AS f ON l.id = f.lureId 
        AND f.tripId = :tripId 
        AND f.fishermanId = :fishermanId
    WHERE tb_lure.tackleBoxId = :tackleBoxId
    GROUP BY l.id
""")
    fun getLureSummariesForFishermanOnTrip(
        fishermanId: String,
        tackleBoxId: String,
        tripId: String
    ): Flow<List<LureEntityWithColorsSummary>>

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