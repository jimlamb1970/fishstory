package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.FishermanWithDetails
import com.funjim.fishstory.model.FishermanWithTrips
import com.funjim.fishstory.model.TripFishermanCrossRef
import com.funjim.fishstory.model.TripSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface FishermanDao {
    @Query("SELECT * FROM fisherman_table")
    fun getAllFishermen(): Flow<List<Fisherman>>

    @Query("SELECT * FROM fisherman_table WHERE id = :id")
    suspend fun getFishermanById(id: String): Fisherman?
    @Query("SELECT * FROM fisherman_table WHERE id = :id")
    fun getFisherman (id: String): Flow<Fisherman?>

    @Query("""SELECT f.* FROM fisherman_table AS f
            JOIN trip_fisherman_cross_ref AS tref ON f.id = tref.fishermanId
            WHERE tref.tripId = :tripId 
            ORDER BY f.firstName, f.nickname, f.lastName ASC
            """)
    fun getFishermenForTrip(tripId: String): Flow<List<Fisherman>>

    @Query("""SELECT f.* FROM fisherman_table AS f
            JOIN event_fisherman_cross_ref AS sref ON f.id = sref.fishermanId
            WHERE sref.eventId = :eventId
            ORDER BY f.firstName, f.nickname, f.lastName ASC
            """)
    fun getFishermenForEvent(eventId: String): Flow<List<Fisherman>>

    @Query("DELETE FROM fisherman_table")
    suspend fun deleteAllFishermen()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fisherman: Fisherman)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFisherman(fisherman: Fisherman)

    @Upsert
    suspend fun upsert(fisherman: Fisherman)

    @Update
    suspend fun update(fisherman: Fisherman)

    @Delete
    suspend fun deleteFisherman(fisherman: Fisherman)

    @Query("SELECT * FROM fisherman_table WHERE firstName = :first AND lastName = :last AND nickname = :nick LIMIT 1")
    suspend fun getFishermanByName(first: String, last: String, nick: String): Fisherman?

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(firstName: String, lastName: String, nickname: String? = null): String {
        val existingFisherman = getFishermanByName(firstName, lastName, nickname ?: "")
        return if (existingFisherman != null) {
            existingFisherman.id
        } else {
            val newFisherman =
                Fisherman(firstName = firstName, lastName = lastName, nickname = nickname ?: "")
            upsert(newFisherman)
            newFisherman.id
        }
    }

    @Transaction
    @Query("SELECT * FROM fisherman_table WHERE id = :fishermanId")
    fun getFishermanWithTrips(fishermanId: String): Flow<FishermanWithTrips?>

    @Transaction
    @Query("SELECT * FROM fisherman_table WHERE id = :fishermanId")
    fun getFishermanWithDetails(fishermanId: String): Flow<FishermanWithDetails?>

    @Transaction
    @Query("""
    SELECT 
        f.*, 
        (SELECT COUNT(*) FROM fish_table WHERE fishermanId = f.id) AS totalCatches,
        (SELECT COUNT(*) FROM fish_table WHERE fishermanId = f.id AND isReleased = 1) AS totalReleased,
        (SELECT COUNT(*) FROM trip_fisherman_cross_ref WHERE fishermanId = f.id) AS totalTrips
    FROM fisherman_table AS f
    GROUP BY f.id
""")
    fun getFishermanSummaries(): Flow<List<FishermanSummary>>

    @Transaction
    @Query(
        """
WITH 
-- 1. Identify the Largest Fish for this fisherman
LargestFish AS (
    SELECT f.length, f.timestamp, sp.name as speciesName
    FROM fish_table f
    JOIN species_table sp ON f.speciesId = sp.id
    WHERE f.fishermanId = :fId
    ORDER BY f.length DESC, f.timestamp DESC LIMIT 1
),
-- 2. Identify the Smallest Fish for this fisherman
SmallestFish AS (
    SELECT f.length, f.timestamp, sp.name as speciesName
    FROM fish_table f
    JOIN species_table sp ON f.speciesId = sp.id
    WHERE f.fishermanId = :fId AND f.length > 0
    ORDER BY f.length ASC, f.timestamp DESC LIMIT 1
),
-- 3. Calculate Trip stats (Only for trips that have already started)
TripStats AS (
    SELECT 
        t.name, t.startDate, 
        COUNT(fish.id) as catchCount,
        ROW_NUMBER() OVER (ORDER BY COUNT(fish.id) DESC, t.id DESC) as best_row,
        ROW_NUMBER() OVER (ORDER BY COUNT(fish.id) ASC, t.id DESC) as worst_row
    FROM trip_fisherman_cross_ref tref
    JOIN trip_table t ON tref.tripId = t.id
    LEFT JOIN fish_table fish ON t.id = fish.tripId AND fish.fishermanId = :fId
    WHERE tref.fishermanId = :fId 
      AND t.startDate <= :currentTime  -- Exclude future trips
    GROUP BY t.id
),
-- 4. Calculate Event stats (Only for events that have already started)
EventStats AS (
    SELECT 
        s.name, s.startTime, t.name as tripName,
        COUNT(fish.id) as catchCount,
        ROW_NUMBER() OVER (ORDER BY COUNT(fish.id) DESC, s.id DESC) as best_row,
        ROW_NUMBER() OVER (ORDER BY COUNT(fish.id) ASC, s.id DESC) as worst_row
    FROM event_fisherman_cross_ref sref
    JOIN event_table s ON sref.eventId = s.id
    JOIN trip_table t ON s.tripId = t.id
    LEFT JOIN fish_table fish ON s.id = fish.eventId AND fish.fishermanId = :fId
    WHERE sref.fishermanId = :fId 
      AND s.startTime <= :currentTime -- Exclude future events
    GROUP BY s.id
)

SELECT 
    f.*,
    -- Largest Fish
    (SELECT length FROM LargestFish) AS largestFishLength,
    (SELECT timestamp FROM LargestFish) AS largestFishTimestamp,
    (SELECT speciesName FROM LargestFish) AS largestFishSpecies,

    -- Smallest Fish
    (SELECT length FROM SmallestFish) AS smallestFishLength,
    (SELECT timestamp FROM SmallestFish) AS smallestFishTimestamp,
    (SELECT speciesName FROM SmallestFish) AS smallestFishSpecies,

    -- Best Trip
    (SELECT catchCount FROM TripStats WHERE best_row = 1) AS mostTripCatches,
    (SELECT name FROM TripStats WHERE best_row = 1) AS bestTripName,
    (SELECT startDate FROM TripStats WHERE best_row = 1) AS bestTripTime,

    -- Worst Trip (Skunk Trip)
    (SELECT catchCount FROM TripStats WHERE worst_row = 1) AS fewestTripCatches,
    (SELECT name FROM TripStats WHERE worst_row = 1) AS worstTripName,
    (SELECT startDate FROM TripStats WHERE worst_row = 1) AS worstTripTime,

    -- Best Event
    (SELECT catchCount FROM EventStats WHERE best_row = 1) AS mostEventCatches,
    (SELECT name FROM EventStats WHERE best_row = 1) AS bestEventName,
    (SELECT tripName FROM EventStats WHERE best_row = 1) AS bestEventTripName,
    (SELECT startTime FROM EventStats WHERE best_row = 1) AS bestEventTime,

    -- Worst Event (Skunk Event)
    (SELECT catchCount FROM EventStats WHERE worst_row = 1) AS fewestEventCatches,
    (SELECT name FROM EventStats WHERE worst_row = 1) AS worstEventName,
    (SELECT tripName FROM EventStats WHERE worst_row = 1) AS worstEventTripName,
    (SELECT startTime FROM EventStats WHERE worst_row = 1) AS worstEventTime

FROM fisherman_table AS f
WHERE f.id = :fId
"""
    )
    fun getFishermanFullStatistics(fId: String, currentTime: Long): Flow<FishermanFullStatistics>

    @Query("""
    SELECT 
        t.*, 
        0 as eventCount,
        COUNT(f.id) AS totalCaught,
        SUM(CASE WHEN f.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept,
        -1 as fishermanCount,
        -1 as tackleBoxCount,
        NULL as bigFishName,
        "" as bigFishSpecies,
        0 as bigFishLength,
        NULL as mostCaughtName,
        0 as mostCaught
    FROM trip_table AS t
    JOIN trip_fisherman_cross_ref AS tref ON t.id = tref.tripId
    LEFT JOIN fish_table AS f ON t.id = f.tripId AND f.fishermanId = :fishermanId
    WHERE tref.fishermanId = :fishermanId
    GROUP BY t.id
    ORDER BY t.startDate DESC
""")
    fun getTripSummariesForFisherman(fishermanId: String): Flow<List<TripSummary>>

    @Query("""
    SELECT 
        t.*, 
        0 as eventCount,
        COUNT(f.id) AS totalCaught,
        SUM(CASE WHEN f.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept,
        -1 as fishermanCount,
        -1 as tackleBoxCount,
        NULL as bigFishName,
        "" as bigFishSpecies,
        0 as bigFishLength,
        NULL as mostCaughtName,
        0 as mostCaught
    FROM trip_table AS t
    JOIN trip_fisherman_cross_ref AS tref ON t.id = tref.tripId
    LEFT JOIN fish_table AS f ON t.id = f.tripId AND f.fishermanId = :fishermanId
    WHERE tref.fishermanId = :fishermanId
      AND (t.startDate > :currentTime)
    GROUP BY t.id
    ORDER BY t.startDate ASC
""")
    fun getUpcomingTripSummariesForFisherman(fishermanId: String, currentTime: Long): Flow<List<TripSummary>>

    @Query("""
    SELECT 
        t.*, 
        0 as eventCount,
        COUNT(f.id) AS totalCaught,
        SUM(CASE WHEN f.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept,
        -1 as fishermanCount,
        -1 as tackleBoxCount,
        NULL as bigFishName,
        "" as bigFishSpecies,
        0 as bigFishLength,
        NULL as mostCaughtName,
        0 as mostCaught
    FROM trip_table AS t
    JOIN trip_fisherman_cross_ref AS tref ON t.id = tref.tripId
    LEFT JOIN fish_table AS f ON t.id = f.tripId AND f.fishermanId = :fishermanId
    WHERE tref.fishermanId = :fishermanId
      AND (t.startDate <= :currentTime) AND (t.endDate >= :currentTime)
    GROUP BY t.id
    ORDER BY t.startDate DESC
""")
    fun getActiveTripSummariesForFisherman(fishermanId: String, currentTime: Long): Flow<List<TripSummary>>

    @Query("""
    SELECT 
        t.*, 
        0 as eventCount,
        COUNT(f.id) AS totalCaught,
        SUM(CASE WHEN f.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept,
        -1 as fishermanCount,
        -1 as tackleBoxCount,
        NULL as bigFishName,
        "" as bigFishSpecies,
        0 as bigFishLength,
        NULL as mostCaughtName,
        0 as mostCaught
    FROM trip_table AS t
    JOIN trip_fisherman_cross_ref AS tref ON t.id = tref.tripId
    LEFT JOIN fish_table AS f ON t.id = f.tripId AND f.fishermanId = :fishermanId
    WHERE tref.fishermanId = :fishermanId
      AND (t.endDate < :currentTime)
    GROUP BY t.id
    ORDER BY t.startDate DESC
""")
    fun getPastTripSummariesForFisherman(fishermanId: String, currentTime: Long): Flow<List<TripSummary>>

    @Insert
    suspend fun insertCrossRef(crossRef: TripFishermanCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: com.funjim.fishstory.model.TripFishermanCrossRef)
}
