package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.BaitEntity
import com.funjim.fishstory.database.entity.EventBaitEntity
import com.funjim.fishstory.database.entity.TripBaitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BaitDao {
    // CRUD operations
    @Delete
    suspend fun deleteBait(bait: BaitEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertBait(bait: BaitEntity)
    @Upsert
    suspend fun upsertBait(bait: BaitEntity)
    @Query("DELETE FROM bait_table")
    suspend fun deleteAllBaits()

    // CRUD operations for Trip Cross-References
    @Query("DELETE FROM trip_bait WHERE tripId = :tripId AND baitId = :baitId")
    suspend fun deleteBaitForTrip(tripId: String, baitId: String)
    @Upsert
    suspend fun upsertBaitForTrip(crossRef: TripBaitEntity)
    @Query("DELETE FROM trip_bait")
    suspend fun deleteAllTripBaits()

    // CRUD operations for Event Cross-References
    @Query("DELETE FROM event_bait WHERE eventId = :eventId AND baitId = :baitId")
    suspend fun deleteBaitForEvent(eventId: String, baitId: String)
    @Upsert
    suspend fun upsertBaitForEvent(crossRef: EventBaitEntity)
    @Query("DELETE FROM event_bait WHERE eventId IN (:eventIds) AND baitId = :baitId")
    suspend fun deleteBaitForEvents(eventIds: List<String>, baitId: String)
    @Upsert
    suspend fun upsertBaitForEvents(crossRefs: List<EventBaitEntity>)
    @Query("DELETE FROM event_bait")
    suspend fun deleteAllEventBaits()

    // Single object queries
    @Query("SELECT * FROM bait_table WHERE id = :id")
    fun getBait (id: String): Flow<BaitEntity?>
    @Query("SELECT * FROM bait_table WHERE id = :id LIMIT 1")
    suspend fun getBaitById(id: String): BaitEntity?
    @Query("SELECT * FROM bait_table WHERE name = :name LIMIT 1")
    suspend fun getBaitByName(name: String): BaitEntity?

    // Multiple object queries
    @Query("SELECT * FROM bait_table ORDER BY name ASC")
    fun getAllBaits(): Flow<List<BaitEntity>>
    @Query("""
        SELECT bait_table.* FROM bait_table
        INNER JOIN trip_bait ON bait_table.id = trip_bait.baitId
        WHERE trip_bait.tripId = :tripId
        GROUP BY bait_table.id
        ORDER BY name ASC
        """)
    fun getBaitsForTrip(tripId: String): Flow<List<BaitEntity>>
    @Query("""
        SELECT bait_table.* FROM bait_table
        INNER JOIN event_bait ON bait_table.id = event_bait.baitId
        WHERE event_bait.eventId = :eventId
        UNION
        SELECT bait_table.* FROM bait_table 
        INNER JOIN fish_table ON bait_table.id = fish_table.baitId  
        WHERE fish_table.eventId = :eventId
        ORDER BY name ASC
    """)
    suspend fun getAllBaitsForEvent(eventId: String): List<BaitEntity>
    @Query("""
        SELECT bait_table.* FROM bait_table
        INNER JOIN event_bait ON bait_table.id = event_bait.baitId
        WHERE event_bait.eventId = :eventId
        ORDER BY name ASC
        """)
    fun getBaitsForEvent(eventId: String): Flow<List<BaitEntity>>

    // Multiple object Trip Cross-References queries
    @Query("SELECT * FROM trip_bait")
    fun getAllTripBaits(): Flow<List<TripBaitEntity>>

    // Multiple object Event Cross-References queries
    @Query("SELECT * FROM event_bait")
    fun getAllEventBaits(): Flow<List<EventBaitEntity>>
    @Query("""
        SELECT event_bait.* FROM event_bait WHERE eventId = :eventId
    """)
    suspend fun getEventsBaitsForEvent(eventId: String): List<EventBaitEntity>

    // Specialized queries
    @Query("""
        SELECT bait_table.* FROM bait_table 
        INNER JOIN fish_table ON bait_table.id = fish_table.baitId  
        WHERE (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY bait_table.id
        ORDER BY name ASC
    """)
    fun getBaitsWithFish(
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        speciesId: String? = null,
        tripId: String?): Flow<List<BaitEntity>>
}