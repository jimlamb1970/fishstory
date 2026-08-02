package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BaitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBait(bait: BaitEntity)

    @Upsert
    suspend fun upsertBait(bait: BaitEntity)

    @Query("SELECT * FROM bait_table ORDER BY name ASC")
    fun getAllBaits(): Flow<List<BaitEntity>>

    @Query("SELECT * FROM bait_table WHERE id = :id")
    fun getBait (id: String): Flow<BaitEntity?>

    @Delete
    suspend fun deleteBait(bait: BaitEntity)

    @Query("DELETE FROM bait_table")
    suspend fun deleteAllBaits()

    @Query("SELECT * FROM trip_bait")
    fun getAllTripBaits(): Flow<List<TripBaitEntity>>

    @Query("""
        SELECT bait_table.* FROM bait_table
        INNER JOIN trip_bait ON bait_table.id = trip_bait.baitId
        WHERE trip_bait.tripId = :tripId
        GROUP BY bait_table.id
        """)
    fun getTripBaits(tripId: String): Flow<List<BaitEntity>>

    @Upsert
    suspend fun insertTripBait(crossRef: TripBaitEntity)

    @Query("DELETE FROM trip_bait WHERE tripId = :tripId AND baitId = :baitId")
    suspend fun deleteTripBait(tripId: String, baitId: String)

    @Query("DELETE FROM trip_bait")
    suspend fun deleteAllTripBaits()

    @Query("SELECT * FROM event_bait")
    fun getAllEventBaits(): Flow<List<EventBaitEntity>>

    @Query("""
        SELECT bait_table.* FROM bait_table
        INNER JOIN event_bait ON bait_table.id = event_bait.baitId
        WHERE event_bait.eventId = :eventId
        GROUP BY bait_table.id
        """)
    fun getEventBaits(eventId: String): Flow<List<BaitEntity>>

    @Upsert
    suspend fun insertEventBait(crossRef: EventBaitEntity)

    @Upsert
    suspend fun insertBaitForEvents(crossRefs: List<EventBaitEntity>)

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
        tripId: String?): Flow<List<BaitEntity>>
}