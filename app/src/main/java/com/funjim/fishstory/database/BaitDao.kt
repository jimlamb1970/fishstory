package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.funjim.fishstory.model.Bait
import com.funjim.fishstory.model.EventBait
import com.funjim.fishstory.model.TripBait
import kotlinx.coroutines.flow.Flow

@Dao
interface BaitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBait(bait: Bait)

    @Upsert
    suspend fun upsertBait(bait: Bait)

    @Query("SELECT * FROM bait_table ORDER BY name ASC")
    fun getAllBaits(): Flow<List<Bait>>

    @Query("SELECT * FROM bait_table WHERE id = :id")
    fun getBait (id: String): Flow<Bait?>

    @Delete
    suspend fun deleteBait(bait: Bait)

    @Query("DELETE FROM bait_table")
    suspend fun deleteAllBaits()

    @Query("SELECT * FROM trip_bait")
    fun getAllTripBaits(): Flow<List<TripBait>>

    @Query("""
        SELECT bait_table.* FROM bait_table
        INNER JOIN trip_bait ON bait_table.id = trip_bait.baitId
        WHERE trip_bait.tripId = :tripId
        GROUP BY bait_table.id
        """)
    fun getTripBaits(tripId: String): Flow<List<Bait>>

    @Upsert
    suspend fun insertTripBait(crossRef: TripBait)

    @Query("DELETE FROM trip_bait WHERE tripId = :tripId AND baitId = :baitId")
    suspend fun deleteTripBait(tripId: String, baitId: String)

    @Query("DELETE FROM trip_bait")
    suspend fun deleteAllTripBaits()

    @Query("SELECT * FROM event_bait")
    fun getAllEventBaits(): Flow<List<EventBait>>

    @Query("""
        SELECT bait_table.* FROM bait_table
        INNER JOIN event_bait ON bait_table.id = event_bait.baitId
        WHERE event_bait.eventId = :eventId
        GROUP BY bait_table.id
        """)
    fun getEventBaits(eventId: String): Flow<List<Bait>>

    @Upsert
    suspend fun insertEventBait(crossRef: EventBait)

    @Upsert
    suspend fun insertBaitForEvents(crossRefs: List<EventBait>)

    @Query("DELETE FROM event_bait WHERE eventId = :eventId AND baitId = :baitId")
    suspend fun deleteEventBait(eventId: String, baitId: String)

    @Query("DELETE FROM event_bait WHERE eventId IN (:eventIds) AND baitId = :baitId")
    suspend fun deleteBaitForEvents(eventIds: List<String>, baitId: String)

    @Query("DELETE FROM event_bait")
    suspend fun deleteAllEventBaits()

    @Query("""
        SELECT bait_table.* FROM bait_table 
        INNER JOIN fish_table ON bait_table.id = fish_table.baitId  
        WHERE (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY bait_table.id
    """)
    fun getBaitsWithFish(
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?): Flow<List<Bait>>

}