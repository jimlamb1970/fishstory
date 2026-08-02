package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.EventWithCounts
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishCounts
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.FishWithPhotos
import com.funjim.fishstory.model.FishermanWithCounts
import com.funjim.fishstory.model.LureWithCounts
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.model.SpeciesWithCounts
import com.funjim.fishstory.model.TripWithCounts
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {
    @Transaction
    @Query(
        """
        SELECT 
            f.*, 
            (SELECT COUNT(*) FROM photo_fish_cross_ref AS pf WHERE pf.fishId = f.id) AS photoCount
        FROM fish_table AS f
        LEFT JOIN lure_table AS l ON f.lureId = l.id
        WHERE (:bodyOfWaterId IS NULL OR f.bodyOfWaterId = :bodyOfWaterId)
          AND (:eventId IS NULL OR f.eventId = :eventId)
          AND (:fishermanId IS NULL OR f.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR f.lureId = :lureId)
          AND (:speciesId IS NULL OR f.speciesId = :speciesId)
          AND (:tripId IS NULL OR f.tripId = :tripId)
          AND (:targetOnly IS NULL 
            OR :targetOnly = 0 
            OR EXISTS (SELECT 1 FROM event_target_species AS ets 
                WHERE ets.eventId = f.eventId 
                  AND ets.speciesId = f.speciesId)
          )
        ORDER BY f.timestamp DESC
    """
    )
    fun getFishWithDetails(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null,
        targetOnly: Boolean = false
    ): Flow<List<FishWithDetails>>

    @Transaction
    @Query(
        """
        SELECT 
            f.*, 
            (SELECT COUNT(*) FROM photo_fish_cross_ref AS pf WHERE pf.fishId = f.id) AS photoCount
        FROM fish_table AS f
        WHERE f.tripId = :tripId
        ORDER BY f.timestamp DESC
    """
    )
    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>>

    @Transaction
    @Query(
        """
        SELECT 
            f.*, 
            (SELECT COUNT(*) FROM photo_fish_cross_ref AS pf WHERE pf.fishId = f.id) AS photoCount
        FROM fish_table AS f
        WHERE f.fishermanId = :fishermanId
        ORDER BY f.timestamp DESC
    """
    )
    fun getFishForFisherman(fishermanId: String): Flow<List<FishWithDetails>>

    @Transaction
    @Query(
        """
        SELECT 
            f.*, 
            (SELECT COUNT(*) FROM photo_fish_cross_ref AS pf WHERE pf.fishId = f.id) AS photoCount
        FROM fish_table AS f
        WHERE f.eventId = :eventId
        ORDER BY f.timestamp DESC
    """
    )
    fun getFishForEvent(eventId: String): Flow<List<FishWithDetails>>

    @Query("SELECT * FROM fish_table ORDER BY timestamp DESC")
    fun getAllFish(): Flow<List<Fish>>

    @Query("DELETE FROM fish_table")
    suspend fun deleteAllFish()

    @Query("SELECT * FROM fish_table WHERE id = :id")
    suspend fun getFishById(id: String): Fish?
    @Query("SELECT * FROM fish_table WHERE id = :id")
    suspend fun getFish(id: String): Fish?

    @Transaction
    @Query("SELECT * FROM fish_table WHERE id = :id")
    fun getFishWithPhotos(id: String): Flow<FishWithPhotos>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: Fish)

    @Upsert
    suspend fun upsertFish(fish: Fish)

    @Update
    suspend fun updateFish(fish: Fish)

    @Delete
    suspend fun deleteFish(fish: Fish)

    @Transaction
    @Query("SELECT * FROM species_table WHERE id = :speciesId")
    fun getSpecies(speciesId: String): Flow<Species?>

    @Query("SELECT * FROM species_table ORDER BY name ASC")
    fun getAllSpecies(): Flow<List<Species>>

    @Query("SELECT * FROM species_table ORDER BY name ASC")
    suspend fun getAllSpeciesList(): List<Species>

    @Query("DELETE FROM species_table")
    suspend fun deleteAllSpecies()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpecies(species: Species)

    @Upsert
    suspend fun upsertSpecies(species: Species)

    @Delete
    suspend fun deleteSpecies(species: Species)

    @Query("""
    SELECT 
        bait.*, 
        SUM(f.caughtCount) AS fishCaught,
        SUM(f.keptCount) AS fishKept,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish,
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
            WHERE ft_sub.baitId = bait.id
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
            WHERE ft_sub.baitId = bait.id
        ) as targetFishKept
    FROM bait_table AS bait
    LEFT JOIN fish_table AS f ON bait.id = f.baitId
    GROUP BY bait.id
""")
    fun getBaitSummaries(): Flow<List<BaitSummaryEntity>>

    @Query("""
    SELECT 
        bow.*, 
        SUM(f.caughtCount) AS fishCaught,
        SUM(f.keptCount) AS fishKept,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish,
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
            WHERE ft_sub.bodyOfWaterId = bow.id
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
            WHERE ft_sub.bodyOfWaterId = bow.id
        ) as targetFishKept
    FROM body_of_water_table AS bow
    LEFT JOIN fish_table AS f ON bow.id = f.bodyOfWaterId
    GROUP BY bow.id
""")
    fun getBodyOfWaterSummaries(): Flow<List<BodyOfWaterSummaryEntity>>

    @Query("""
    SELECT 
        s.*, 
        SUM(f.caughtCount) AS fishCaught,
        SUM(f.keptCount) AS fishKept,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish,
        SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.caughtCount 
                ELSE 0 
            END
        ) AS targetFishCaught,
        SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.keptCount 
                ELSE 0 
            END
        ) AS targetFishKept
    FROM species_table AS s
    LEFT JOIN fish_table AS f ON s.id = f.speciesId
    LEFT JOIN event_target_species AS target 
        ON f.eventId = target.eventId 
        AND s.id = target.speciesId
    GROUP BY s.id
""")
    fun getSpeciesSummaries(): Flow<List<SpeciesSummary>>

    @Query("""
    SELECT 
        s.*, 
        -- Total Caught
        (
            SELECT COALESCE(SUM(ft.caughtCount), 0) 
            FROM fish_table ft 
            WHERE ft.speciesId = s.id
        ) AS fishCaught,

        -- Total Kept
        (
            SELECT COALESCE(SUM(ft.keptCount), 0) 
            FROM fish_table ft 
            WHERE ft.speciesId = s.id
        ) AS fishKept,

        -- Largest & Smallest
        (
            SELECT COALESCE(MAX(ft.length), 0.0) 
            FROM fish_table ft 
            WHERE ft.speciesId = s.id
        ) AS largestFish,

        (
            SELECT COALESCE(MIN(CASE WHEN ft.length > 0 THEN ft.length END), 0.0) 
            FROM fish_table ft 
            WHERE ft.speciesId = s.id
        ) AS smallestFish,

        -- Target Fish Caught (Only sums fish entries matching event_target_species)
        (
            SELECT COALESCE(SUM(ft.caughtCount), 0)
            FROM fish_table ft
            INNER JOIN event_target_species target 
                ON ft.eventId = target.eventId 
               AND ft.speciesId = target.speciesId
            WHERE ft.speciesId = s.id
        ) AS targetFishCaught,

        -- Target Fish Kept
        (
            SELECT COALESCE(SUM(ft.keptCount), 0)
            FROM fish_table ft
            INNER JOIN event_target_species target 
                ON ft.eventId = target.eventId 
               AND ft.speciesId = target.speciesId
            WHERE ft.speciesId = s.id
        ) AS targetFishKept

    FROM species_table AS s
    GROUP BY s.id
""")
    fun getSpeciesSummaries2(): Flow<List<SpeciesSummary>>

    @Query("""
    SELECT 
        SUM(fish_table.caughtCount) AS totalCaught,
        SUM(fish_table.keptCount) AS totalKept,

        SUM(
            CASE 
                -- Check if a corresponding entry exists in the target table
                WHEN target.eventId IS NOT NULL THEN fish_table.caughtCount 
                ELSE 0 
            END
        ) AS totalTargetCaught,
        SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN fish_table.keptCount 
                ELSE 0 
            END
        ) AS totalTargetKept,

        COUNT(DISTINCT fish_table.bodyOfWaterId) AS bodyOfWaterCount,
        COUNT(DISTINCT fish_table.eventId) AS eventCount,
        COUNT(DISTINCT fish_table.fishermanId) AS fishermanCount,
        COUNT(DISTINCT fish_table.lureId) AS lureCount,
        COUNT(DISTINCT fish_table.tripId) AS tripCount
    FROM fish_table
    LEFT JOIN event_target_species AS target 
        ON fish_table.eventId = target.eventId 
        AND fish_table.speciesId = target.speciesId
    WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
      AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
""")
    fun getFishCounts(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<FishCounts>

    @Query("""
    SELECT trip_table.*, 
           SUM(fish_table.caughtCount) AS totalCaught,
           SUM(fish_table.keptCount) AS totalKept
    FROM trip_table
    JOIN fish_table ON trip_table.id = fish_table.lureId
    WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
      AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY trip_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopTrip(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<TripWithCounts?>

    @Query("""
    SELECT event_table.*, 
           SUM(fish_table.caughtCount) AS totalCaught,
           SUM(fish_table.keptCount) AS totalKept
    FROM event_table
    JOIN fish_table ON event_table.id = fish_table.lureId
    WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
      AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY event_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopEvent(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<EventWithCounts?>

    @Query("""
    SELECT fisherman_table.*, 
           SUM(fish_table.caughtCount) AS totalCaught,
           SUM(fish_table.keptCount) AS totalKept
    FROM fisherman_table
    JOIN fish_table ON fisherman_table.id = fish_table.lureId
    WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
      AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY fisherman_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopFisherman(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<FishermanWithCounts?>

    @Query("""
    SELECT species_table.*, 
           SUM(fish_table.caughtCount) AS totalCaught,
           SUM(fish_table.keptCount) AS totalKept
    FROM species_table
    JOIN fish_table ON species_table.id = fish_table.lureId
    WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
      AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY species_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopSpecies(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<SpeciesWithCounts?>

    @Transaction
    @Query("""
    SELECT lure_table.*, 
           SUM(fish_table.caughtCount) AS totalCaught,
           SUM(fish_table.keptCount) AS totalKept
    FROM lure_table
    JOIN fish_table ON lure_table.id = fish_table.lureId
    WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
      AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY lure_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopLure(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<LureWithCounts?>

    @Query("""
        UPDATE fish_table 
        SET bodyOfWaterId = :newBodyOfWaterId 
        WHERE (:tripId IS NOT NULL AND tripId = :tripId)
           OR (:eventId IS NOT NULL AND eventId = :eventId)
    """)
    suspend fun updateBodyOfWaterForTripOrEvent(
        newBodyOfWaterId: String?,
        tripId: String?,
        eventId: String?
    )

    @Transaction
    @Query("""
        SELECT DISTINCT species_table.* FROM species_table 
        INNER JOIN fish_table ON species_table.id = fish_table.speciesId 
        WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
          AND (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY species_table.id
    """)
    fun getSpeciesWithFish(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        tripId: String? = null
    ): Flow<List<Species>>
}
