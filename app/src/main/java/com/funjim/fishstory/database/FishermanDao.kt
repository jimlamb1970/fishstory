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
    @Query("""
    SELECT 
        f.*,

        -- FISH EXTREMES
        (SELECT MAX(length) FROM fish_table WHERE fishermanId = :fId) AS largestFishLength,
        (SELECT timestamp FROM fish_table WHERE fishermanId = :fId 
         ORDER BY length DESC, timestamp DESC LIMIT 1) AS largestFishTimestamp,

        (SELECT MIN(length) FROM fish_table WHERE fishermanId = :fId AND length > 0) AS smallestFishLength,
        (SELECT timestamp FROM fish_table WHERE fishermanId = :fId AND length > 0 
         ORDER BY length ASC, timestamp DESC LIMIT 1) AS smallestFishTimestamp,

        -- BEST TRIP
        (SELECT COUNT(f.id) as c 
         FROM trip_fisherman_cross_ref tref
         LEFT JOIN fish_table f ON tref.tripId = f.tripId AND f.fishermanId = :fId
         WHERE tref.fishermanId = :fId
         GROUP BY tref.tripId ORDER BY c DESC LIMIT 1) AS mostTripCatches,
        (SELECT t.name FROM trip_fisherman_cross_ref tref
         JOIN trip_table t ON tref.tripId = t.id
         LEFT JOIN fish_table fish ON t.id = fish.tripId AND fish.fishermanId = :fId
         WHERE tref.fishermanId = :fId
         GROUP BY t.id ORDER BY COUNT(fish.id) DESC, t.id DESC LIMIT 1) AS bestTripName,
        (SELECT t.startDate FROM trip_fisherman_cross_ref tref
         JOIN trip_table t ON tref.tripId = t.id
         LEFT JOIN fish_table fish ON t.id = fish.tripId AND fish.fishermanId = :fId
         WHERE tref.fishermanId = :fId
         GROUP BY t.id ORDER BY COUNT(fish.id) DESC, t.id DESC LIMIT 1) AS bestTripTime,

        -- BEST SEGMENT & ITS PARENT TRIP
        (SELECT COUNT(f.id) as c 
         FROM segment_fisherman_cross_ref sref
         LEFT JOIN fish_table f ON sref.segmentId = f.segmentId AND f.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY sref.segmentId ORDER BY c DESC LIMIT 1) AS mostSegmentCatches,
        (SELECT s.name FROM segment_fisherman_cross_ref sref
         JOIN segment_table s ON sref.segmentId = s.id
         LEFT JOIN fish_table fish ON s.id = fish.segmentId AND fish.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY s.id ORDER BY COUNT(fish.id) DESC, s.id DESC LIMIT 1) AS bestSegmentName,
        (SELECT t.name FROM segment_fisherman_cross_ref sref
         JOIN segment_table s ON sref.segmentId = s.id
         JOIN trip_table t ON s.tripId = t.id
         LEFT JOIN fish_table fish ON s.id = fish.segmentId AND fish.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY s.id ORDER BY COUNT(fish.id) DESC, s.id DESC LIMIT 1) AS bestSegmentTripName,
        (SELECT s.startTime FROM segment_fisherman_cross_ref sref
         JOIN segment_table s ON sref.segmentId = s.id
         LEFT JOIN fish_table fish ON s.id = fish.segmentId AND fish.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY s.id ORDER BY COUNT(fish.id) DESC, s.id DESC LIMIT 1) AS bestSegmentTime,

        -- WORST TRIP (The "Skunk" Trip)
        (SELECT COUNT(f.id) as c 
         FROM trip_fisherman_cross_ref tref
         LEFT JOIN fish_table f ON tref.tripId = f.tripId AND f.fishermanId = :fId
         WHERE tref.fishermanId = :fId
         GROUP BY tref.tripId ORDER BY c ASC LIMIT 1) AS fewestTripCatches,
        (SELECT t.name FROM trip_fisherman_cross_ref tref
         JOIN trip_table t ON tref.tripId = t.id
         LEFT JOIN fish_table fish ON t.id = fish.tripId AND fish.fishermanId = :fId
         WHERE tref.fishermanId = :fId
         GROUP BY t.id ORDER BY COUNT(fish.id) ASC, t.id DESC LIMIT 1) AS worstTripName, 
        (SELECT t.startDate FROM trip_fisherman_cross_ref tref
         JOIN trip_table t ON tref.tripId = t.id
         LEFT JOIN fish_table fish ON t.id = fish.tripId AND fish.fishermanId = :fId
         WHERE tref.fishermanId = :fId
         GROUP BY t.id ORDER BY COUNT(fish.id) ASC, t.id DESC LIMIT 1) AS worstTripTime,

        -- WORST SEGMENT (The "Skunk" Segment)
        (SELECT COUNT(f.id) as c 
         FROM segment_fisherman_cross_ref sref
         LEFT JOIN fish_table f ON sref.segmentId = f.segmentId AND f.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY sref.segmentId ORDER BY c ASC LIMIT 1) AS fewestSegmentCatches,
        (SELECT s.name FROM segment_fisherman_cross_ref sref
         JOIN segment_table s ON sref.segmentId = s.id
         LEFT JOIN fish_table fish ON s.id = fish.segmentId AND fish.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY s.id ORDER BY COUNT(fish.id) ASC, s.id DESC LIMIT 1) AS worstSegmentName,
        (SELECT t.name FROM segment_fisherman_cross_ref sref
         JOIN segment_table s ON sref.segmentId = s.id
         JOIN trip_table t ON s.tripId = t.id
         LEFT JOIN fish_table fish ON s.id = fish.segmentId AND fish.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY s.id ORDER BY COUNT(fish.id) ASC, s.id DESC LIMIT 1) AS worstSegmentTripName,
        (SELECT s.startTime FROM segment_fisherman_cross_ref sref
         JOIN segment_table s ON sref.segmentId = s.id
         LEFT JOIN fish_table fish ON s.id = fish.segmentId AND fish.fishermanId = :fId
         WHERE sref.fishermanId = :fId
         GROUP BY s.id ORDER BY COUNT(fish.id) ASC, s.id DESC LIMIT 1) AS worstSegmentTime

    FROM fisherman_table AS f
    WHERE f.id = :fId
""")
    fun getFishermanFullStatistics(fId: String): Flow<FishermanFullStatistics?>

    @Query("""
    SELECT 
        t.*, 
        COUNT(f.id) AS totalCaught,
        SUM(CASE WHEN f.isReleased = 0 THEN 1 ELSE 0 END) AS totalKept,
        0 as totalKept,
        -1 as fishermanCount,
        -1 as tackleBoxCount,
        NULL as bigFishWinner,
        NULL as topRodName
    FROM trip_table AS t
    JOIN trip_fisherman_cross_ref AS tref ON t.id = tref.tripId
    LEFT JOIN fish_table AS f ON t.id = f.tripId AND f.fishermanId = :fishermanId
    WHERE tref.fishermanId = :fishermanId
    GROUP BY t.id
    ORDER BY t.startDate DESC
""")
    fun getTripSummariesForFisherman(fishermanId: String): Flow<List<TripSummary>>

    @Insert
    suspend fun insertCrossRef(crossRef: TripFishermanCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: com.funjim.fishstory.model.TripFishermanCrossRef)
}
