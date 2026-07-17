package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.BaitSummary
import com.funjim.fishstory.model.BodyOfWaterSummary
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
          AND (:tripId IS NULL OR f.tripId = :tripId)
        ORDER BY f.timestamp DESC
    """
    )
    fun getFishWithDetails(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
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
        SUM(f.caughtCount) AS caughtCount,
        SUM(f.keptCount) AS keptCount,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish
    FROM bait_table AS bait
    LEFT JOIN fish_table AS f ON bait.id = f.baitId
    GROUP BY bait.id
""")
    fun getBaitSummaries(): Flow<List<BaitSummary>>

    @Query("""
    SELECT 
        bow.*, 
        SUM(f.caughtCount) AS caughtCount,
        SUM(f.keptCount) AS keptCount,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish
    FROM body_of_water_table AS bow
    LEFT JOIN fish_table AS f ON bow.id = f.bodyOfWaterId
    GROUP BY bow.id
""")
    fun getBodyOfWaterSummaries(): Flow<List<BodyOfWaterSummary>>

    @Query("""
    SELECT 
        s.*, 
        SUM(f.caughtCount) AS caughtCount,
        SUM(f.keptCount) AS keptCount,
        MAX(f.length) AS largestFish,
        COALESCE(MIN(CASE WHEN f.length > 0 THEN f.length END), 0.0) AS smallestFish
    FROM species_table AS s
    LEFT JOIN fish_table AS f ON s.id = f.speciesId
    GROUP BY s.id
""")
    fun getSpeciesSummaries(): Flow<List<SpeciesSummary>>

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
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
""")
    fun getFishCounts(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
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
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY trip_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopTrip(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
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
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY event_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopEvent(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
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
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY fisherman_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopFisherman(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
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
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY species_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopSpecies(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
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
      AND (:tripId IS NULL OR fish_table.tripId = :tripId)
    GROUP BY lure_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopLure(
        bodyOfWaterId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?
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
}
