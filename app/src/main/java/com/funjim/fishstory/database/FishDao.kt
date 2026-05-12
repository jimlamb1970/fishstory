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
            pc.name AS lurePrimaryColorName, 
            sc.name AS lureSecondaryColorName,
            gc.name AS lureGlowColorName
        FROM fish_table AS f
        LEFT JOIN lure_table AS l ON f.lureId = l.id
        LEFT JOIN lure_color_table AS pc ON l.primaryColorId = pc.id
        LEFT JOIN lure_color_table AS sc ON l.secondaryColorId = sc.id
        LEFT JOIN lure_color_table AS gc ON l.glowColorId = gc.id
        WHERE (:tripId IS NULL OR f.tripId = :tripId)
          AND (:eventId IS NULL OR f.eventId = :eventId)
          AND (:fishermanId IS NULL OR f.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR f.lureId = :lureId)
        ORDER BY f.timestamp DESC
    """
    )
    fun getFishWithDetails(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<List<FishWithDetails>>

    @Transaction
    @Query(
        """
        SELECT 
            f.*, 
            pc.name AS lurePrimaryColorName, 
            sc.name AS lureSecondaryColorName,
            gc.name AS lureGlowColorName
        FROM fish_table AS f
        LEFT JOIN lure_table AS l ON f.lureId = l.id
        LEFT JOIN lure_color_table AS pc ON l.primaryColorId = pc.id
        LEFT JOIN lure_color_table AS sc ON l.secondaryColorId = sc.id
        LEFT JOIN lure_color_table AS gc ON l.glowColorId = gc.id
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
            pc.name AS lurePrimaryColorName, 
            sc.name AS lureSecondaryColorName,
            gc.name AS lureGlowColorName
        FROM fish_table AS f
        LEFT JOIN lure_table AS l ON f.lureId = l.id
        LEFT JOIN lure_color_table AS pc ON l.primaryColorId = pc.id
        LEFT JOIN lure_color_table AS sc ON l.secondaryColorId = sc.id
        LEFT JOIN lure_color_table AS gc ON l.glowColorId = gc.id
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
            pc.name AS lurePrimaryColorName, 
            sc.name AS lureSecondaryColorName,
            gc.name AS lureGlowColorName
        FROM fish_table AS f
        LEFT JOIN lure_table AS l ON f.lureId = l.id
        LEFT JOIN lure_color_table AS pc ON l.primaryColorId = pc.id
        LEFT JOIN lure_color_table AS sc ON l.secondaryColorId = sc.id
        LEFT JOIN lure_color_table AS gc ON l.glowColorId = gc.id
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
    suspend fun getFishWithPhotos(id: String): FishWithPhotos?

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
        s.*, 
        COUNT(f.id) AS caughtCount,
        SUM(CASE WHEN f.isReleased = 0 THEN 1 ELSE 0 END) AS keptCount,
        MAX(f.length) AS largestFish,
        MIN(f.length) AS smallest
    FROM species_table AS s
    LEFT JOIN fish_table AS f ON s.id = f.speciesId
    GROUP BY s.id
""")
    fun getSpeciesSummaries(): Flow<List<SpeciesSummary>>

    @Query("""
    SELECT 
        COUNT(fish_table.id) AS totalCaught,
        SUM(CASE WHEN fish_table.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept,
        COUNT(DISTINCT fish_table.tripId) AS tripCount,
        COUNT(DISTINCT fish_table.eventId) AS eventCount,
        COUNT(DISTINCT fish_table.fishermanId) AS fishermanCount,
        COUNT(DISTINCT fish_table.lureId) AS lureCount
    FROM fish_table
    WHERE (:tripId IS NULL OR fish_table.tripId = :tripId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
""")
    fun getFishCounts(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<FishCounts>

    @Query("""
    SELECT trip_table.*, 
           COUNT(fish_table.id) AS totalCaught,
           SUM(CASE WHEN fish_table.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept
    FROM trip_table
    JOIN fish_table ON trip_table.id = fish_table.lureId
    WHERE (:tripId IS NULL OR fish_table.tripId = :tripId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
    GROUP BY trip_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopTrip(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<TripWithCounts?>

    @Query("""
    SELECT event_table.*, 
           COUNT(fish_table.id) AS totalCaught,
           SUM(CASE WHEN fish_table.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept
    FROM event_table
    JOIN fish_table ON event_table.id = fish_table.lureId
    WHERE (:tripId IS NULL OR fish_table.tripId = :tripId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
    GROUP BY event_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopEvent(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<EventWithCounts?>

    @Query("""
    SELECT fisherman_table.*, 
           COUNT(fish_table.id) AS totalCaught,
           SUM(CASE WHEN fish_table.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept
    FROM fisherman_table
    JOIN fish_table ON fisherman_table.id = fish_table.lureId
    WHERE (:tripId IS NULL OR fish_table.tripId = :tripId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
    GROUP BY fisherman_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopFisherman(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<FishermanWithCounts?>

    @Query("""
    SELECT species_table.*, 
           COUNT(fish_table.id) AS totalCaught,
           SUM(CASE WHEN fish_table.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept
    FROM species_table
    JOIN fish_table ON species_table.id = fish_table.lureId
    WHERE (:tripId IS NULL OR fish_table.tripId = :tripId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
    GROUP BY species_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopSpecies(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<SpeciesWithCounts?>

    @Query("""
    SELECT lure_table.*, 
           p.name AS primaryName, 
           s.name AS secondaryName, 
           g.name AS glowName,
           COUNT(fish_table.id) AS totalCaught,
           SUM(CASE WHEN fish_table.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept
    FROM lure_table
    LEFT JOIN lure_color_table p ON primaryColorId = p.id
    LEFT JOIN lure_color_table s ON secondaryColorId = s.id
    LEFT JOIN lure_color_table g ON glowColorId = g.id
    JOIN fish_table ON lure_table.id = fish_table.lureId
    WHERE (:tripId IS NULL OR fish_table.tripId = :tripId)
      AND (:eventId IS NULL OR fish_table.eventId = :eventId)
      AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
      AND (:lureId IS NULL OR fish_table.lureId = :lureId)
    GROUP BY lure_table.id
    ORDER BY totalCaught DESC
    LIMIT 1
""")
    fun getTopLure(
        tripId: String?,
        eventId: String?,
        fishermanId: String?,
        lureId: String?
    ): Flow<LureWithCounts?>
}
