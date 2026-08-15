package com.funjim.fishstory.database

import androidx.room.*
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
          AND (:waterId IS NULL OR f.waterId = :waterId)
          AND (:weatherId IS NULL OR f.weatherId = :weatherId)
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
        waterId: String? = null,
        weatherId: String? = null,
        targetOnly: Boolean = false
    ): Flow<List<FishEntityWithDetails>>

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
    fun getFishForTrip(tripId: String): Flow<List<FishEntityWithDetails>>

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
    fun getFishForFisherman(fishermanId: String): Flow<List<FishEntityWithDetails>>

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
    fun getFishForEvent(eventId: String): Flow<List<FishEntityWithDetails>>

    @Query("SELECT * FROM fish_table ORDER BY timestamp DESC")
    fun getAllFish(): Flow<List<FishEntity>>

    @Query("DELETE FROM fish_table")
    suspend fun deleteAllFish()

    @Query("SELECT * FROM fish_table WHERE id = :id")
    suspend fun getFishById(id: String): FishEntity?
    @Query("SELECT * FROM fish_table WHERE id = :id")
    suspend fun getFish(id: String): FishEntity?

    @Transaction
    @Query("SELECT * FROM fish_table WHERE id = :id")
    fun getFishWithPhotos(id: String): Flow<FishEntityWithPhotos>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: FishEntity)

    @Upsert
    suspend fun upsertFish(fish: FishEntity)

    @Update
    suspend fun updateFish(fish: FishEntity)

    @Delete
    suspend fun deleteFish(fish: FishEntity)

    @Transaction
    @Query("SELECT * FROM species_table WHERE id = :speciesId")
    fun getSpecies(speciesId: String): Flow<SpeciesEntity?>

    @Query("SELECT * FROM species_table ORDER BY name ASC")
    fun getAllSpecies(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species_table ORDER BY name ASC")
    suspend fun getAllSpeciesList(): List<SpeciesEntity>

    @Query("DELETE FROM species_table")
    suspend fun deleteAllSpecies()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpecies(species: SpeciesEntity)

    @Update
    suspend fun updateSpecies(species: SpeciesEntity)

    @Delete
    suspend fun deleteSpecies(species: SpeciesEntity)

    @Query("""
    SELECT 
        bait.*, 
        COALESCE(SUM(f.caughtCount), 0) AS fishCaught,
        COALESCE(SUM(f.keptCount), 0) AS fishKept,
        COALESCE(MAX(f.length), 0.0) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish,
        
        -- Sums target species catches across all events where the fish was logged
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.caughtCount 
                ELSE 0 
            END
        ), 0) AS targetFishCaught,
        
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.keptCount 
                ELSE 0 
            END
        ), 0) AS targetFishKept
    FROM bait_table AS bait
    LEFT JOIN fish_table AS f 
        ON bait.id = f.baitId
    LEFT JOIN event_target_species AS target 
        ON f.eventId = target.eventId 
        AND f.speciesId = target.speciesId
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
        bow.*, 
        COALESCE(SUM(f.caughtCount), 0) AS fishCaught,
        COALESCE(SUM(f.keptCount), 0) AS fishKept,
        COALESCE(MAX(f.length), 0.0) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish, 
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.caughtCount 
                ELSE 0 
            END
        ), 0) AS targetFishCaught,
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.keptCount 
                ELSE 0 
            END
        ), 0) AS targetFishKept
    FROM body_of_water_table AS bow
    INNER JOIN event_body_of_water AS ebw 
        ON bow.id = ebw.bodyOfWaterId AND ebw.eventId = :eventId
    LEFT JOIN fish_table AS f 
        ON bow.id = f.bodyOfWaterId AND f.eventId = :eventId
    LEFT JOIN event_target_species AS target 
        ON f.eventId = target.eventId AND f.speciesId = target.speciesId
    GROUP BY bow.id
""")
    fun getEventBodyOfWaterSummaries(
        eventId: String
    ): Flow<List<BodyOfWaterSummaryEntity>>

    @Query("""
    SELECT 
        bow.*, 
        COALESCE(SUM(f.caughtCount), 0) AS fishCaught,
        COALESCE(SUM(f.keptCount), 0) AS fishKept,
        COALESCE(MAX(f.length), 0.0) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish, 
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.caughtCount 
                ELSE 0 
            END
        ), 0) AS targetFishCaught,
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.keptCount 
                ELSE 0 
            END
        ), 0) AS targetFishKept
    FROM body_of_water_table AS bow
    INNER JOIN trip_body_of_water AS tbw 
        ON bow.id = tbw.bodyOfWaterId AND tbw.tripId = :tripId
    LEFT JOIN fish_table AS f 
        ON bow.id = f.bodyOfWaterId AND f.tripId = :tripId
    LEFT JOIN event_target_species AS target 
        ON f.eventId = target.eventId AND f.speciesId = target.speciesId
    GROUP BY bow.id
""")
    fun getTripBodyOfWaterSummaries(
        tripId: String
    ): Flow<List<BodyOfWaterSummaryEntity>>

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
    fun getSpeciesSummaries(): Flow<List<SpeciesSummaryEntity>>

    @Query("""
    SELECT 
        s.*, 
        COALESCE(SUM(f.caughtCount), 0) AS fishCaught,
        COALESCE(SUM(f.keptCount), 0) AS fishKept,
        COALESCE(MAX(f.length), 0.0) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish,
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.caughtCount 
                ELSE 0 
            END
        ), 0) AS targetFishCaught,
        COALESCE(SUM(
            CASE 
                WHEN target.eventId IS NOT NULL THEN f.keptCount 
                ELSE 0 
            END
        ), 0) AS targetFishKept
    FROM species_table AS s
    LEFT JOIN fish_table AS f 
        ON s.id = f.speciesId 
        AND (:eventId IS NULL OR f.eventId = :eventId)
        AND (:tripId IS NULL OR f.tripId = :tripId)
    LEFT JOIN event_target_species AS target 
        ON f.eventId = target.eventId 
        AND s.id = target.speciesId
    WHERE s.id = :speciesId
""")
    fun getSpeciesSummary(
        tripId: String? = null,
        eventId: String? = null,
        speciesId: String): Flow<SpeciesSummaryEntity?>

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
    fun getSpeciesSummariesEx(): Flow<List<SpeciesSummaryEntity>>

    @Query("""
    SELECT 
        water.*, 
        COALESCE(SUM(f.caughtCount), 0) AS fishCaught,
        COALESCE(SUM(f.keptCount), 0) AS fishKept,
        COALESCE(MAX(f.length), 0.0) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish,
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
            WHERE ft_sub.waterId = water.id
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
            WHERE ft_sub.waterId = water.id
                AND (:eventId IS NULL OR ft_sub.eventId = :eventId)
                AND (:tripId IS NULL OR ft_sub.tripId = :tripId)
        ) as targetFishKept
    FROM water_table AS water
    LEFT JOIN fish_table AS f ON water.id = f.waterId
        AND (:eventId IS NULL OR f.eventId = :eventId)
        AND (:tripId IS NULL OR f.tripId = :tripId)
    WHERE (:eventId IS NULL OR water.eventId = :eventId)
      AND (:tripId IS NULL OR water.tripId = :tripId)
    GROUP BY water.id
""")
    fun getWaterSummaries(
        tripId: String? = null,
        eventId: String? = null
    ): Flow<List<WaterSummaryEntity>>

    @Query("""
    SELECT 
        weather.*, 
        COALESCE(SUM(f.caughtCount), 0) AS fishCaught,
        COALESCE(SUM(f.keptCount), 0) AS fishKept,
        COALESCE(MAX(f.length), 0.0) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish,
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
            WHERE ft_sub.weatherId = weather.id
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
            WHERE ft_sub.weatherId = weather.id
                AND (:eventId IS NULL OR ft_sub.eventId = :eventId)
                AND (:tripId IS NULL OR ft_sub.tripId = :tripId)
        ) as targetFishKept
    FROM weather_table AS weather
    LEFT JOIN fish_table AS f ON weather.id = f.weatherId
    WHERE (:eventId IS NULL OR weather.eventId = :eventId)
      AND (:tripId IS NULL OR weather.tripId = :tripId)
    GROUP BY weather.id
""")
    fun getWeatherSummaries(
        tripId: String? = null,
        eventId: String? = null
    ): Flow<List<WeatherSummaryEntity>>

    @Query("""
    SELECT 
        water.*, 
        SUM(f.caughtCount) AS fishCaught,
        SUM(f.keptCount) AS fishKept,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0) AS smallestFish,
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
            WHERE ft_sub.waterId = water.id
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
            WHERE ft_sub.waterId = water.id
        ) as targetFishKept
    FROM water_table AS water
    LEFT JOIN fish_table AS f ON water.id = f.waterId
    WHERE water.id = :waterId
""")
    fun getWaterSummary(waterId: String): Flow<WaterSummaryEntity?>

    @Query("""
    SELECT 
        weather.*, 
        SUM(f.caughtCount) AS fishCaught,
        SUM(f.keptCount) AS fishKept,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0) AS smallestFish,
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
            WHERE ft_sub.weatherId = weather.id
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
            WHERE ft_sub.weatherId = weather.id
        ) as targetFishKept
    FROM weather_table AS weather
    LEFT JOIN fish_table AS f ON weather.id = f.weatherId
    GROUP BY weather.id
""")
    fun getWeatherSummaries(): Flow<List<WeatherSummaryEntity>>

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
    ): Flow<FishCountsEntity>

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
    ): Flow<TripEntityWithCounts?>

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
    ): Flow<EventEntityWithCounts?>

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
    ): Flow<FishermanEntityWithCounts?>

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
    ): Flow<SpeciesEntityWithCounts?>

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
    ): Flow<LureEntityWithCounts?>

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
    ): Flow<List<SpeciesEntity>>
}
