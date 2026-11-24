package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.EventEntity
import com.funjim.fishstory.database.entity.EventEntityDetailedSummary
import com.funjim.fishstory.database.entity.EventEntitySummary
import com.funjim.fishstory.database.entity.EventEntityWithDetails
import com.funjim.fishstory.database.entity.EventEntityWithInfo
import com.funjim.fishstory.database.entity.TackleBoxStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("DELETE FROM event_table")
    suspend fun deleteAllEvents()
    @Query("SELECT * FROM event_table ORDER BY startTime DESC")
    fun getAllEvents(): Flow<List<EventEntity>>
    @Query("SELECT * FROM event_table ORDER BY startTime DESC")
    suspend fun getAllEventsList(): List<EventEntity>
    @Transaction
    @Query("SELECT * FROM event_table")
    fun getAllEventsWithInfo(): Flow<List<EventEntityWithInfo>>

    @Delete
    suspend fun deleteEvent(event: EventEntity)
    @Query("DELETE FROM event_table WHERE id = :id")
    suspend fun deleteEventById(id: String)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEvent(event: EventEntity)
    @Upsert
    suspend fun upsertEvent(event: EventEntity)

    @Query("UPDATE event_table SET tackleBoxStatus = :status WHERE id = :eventId")
    suspend fun updateEventTackleBoxStatus(eventId: String, status: TackleBoxStatus)
    @Query("UPDATE event_table SET tackleBoxStatus = :status WHERE tripId = :tripId")
    suspend fun updateTripTackleBoxStatus(tripId: String, status: TackleBoxStatus)

    @Transaction
    @Query("SELECT * FROM event_table WHERE id = :id")
    fun getEvent(id: String): Flow<EventEntity?>
    @Query("SELECT * FROM event_table WHERE id = :id")
    suspend fun getEventById(id: String): EventEntity?
    @Query("SELECT * FROM event_table WHERE tripId = :tripId AND name = :name")
    suspend fun getEventByName(tripId: String, name: String): EventEntity?

    @Query("SELECT * FROM event_table WHERE tripId = :tripId")
    fun getEventsForTrip(tripId: String): Flow<List<EventEntity>>

    @Query("SELECT id FROM event_table WHERE tripId = :tripId")
    suspend fun getEventIdsForTrip(tripId: String): List<String>

    @Query(" SELECT tripId FROM event_table WHERE id = :eventId")
    suspend fun getTripIdForEvent(eventId: String): String

    @Transaction
    @Query(
        """
SELECT 
    s.*,
    -- Basic Counts (Overall)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishKept,

    (SELECT COUNT(*) FROM event_fisherman xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    -- Basic Counts (Target Only)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishKept
FROM event_table s
JOIN trip_table AS t ON s.tripId = t.id
WHERE :currentTime BETWEEN t.startTime AND t.endTime
ORDER BY s.startTime ASC
"""
    )
    fun getEventsForActiveTrips(currentTime: Long): Flow<List<EventEntitySummary>>

    @Transaction
    @Query("SELECT * FROM event_table WHERE tripId = :tripId")
    fun getEventsWithDetailsForTrip(tripId: String): Flow<List<EventEntityWithDetails>>

    @Query("""
    SELECT * FROM event_table 
    WHERE startTime <= :currentTime 
    AND (endTime IS NULL OR endTime >= :currentTime) 
    ORDER BY startTime DESC
""")
    fun getActiveEvents(currentTime: Long = System.currentTimeMillis()): Flow<List<EventEntity>>

    @Transaction
    @Query("SELECT * FROM event_table WHERE id = :eventId")
    fun getEventWithDetails(eventId: String): Flow<EventEntityWithDetails?>

    @Transaction
    @Query("SELECT * FROM event_table WHERE id = :eventId")
    fun getEventWithInfo(eventId: String): Flow<EventEntityWithInfo?>

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(tripId: String, name: String): String {
        val foundEvent = getEventByName(tripId, name)
        return if (foundEvent != null) {
            foundEvent.id
        } else {
            val newEvent = EventEntity(tripId = tripId, name = name)
            upsertEvent(newEvent)
            newEvent.id
        }
    }

    @Transaction
    @Query(
        """
SELECT 
    s.*,
    -- Basic Counts (Overall)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishKept,

    (SELECT COUNT(*) FROM event_fisherman xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    -- Basic Counts (Target Only)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishKept
FROM event_table s
WHERE s.tripId = :tripId
ORDER BY s.startTime DESC"""
    )
    fun getTripEventSummaries(tripId: String): Flow<List<EventEntitySummary>>

    @Transaction
    @Query(
        """
SELECT 
    s.*,
    -- Basic Counts (Overall)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishKept,

    (SELECT COUNT(*) FROM event_fisherman xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    -- Basic Counts (Target Only)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishKept
FROM event_table s
WHERE s.id = :eventId
ORDER BY s.startTime DESC"""
    )
    fun getEventSummary(eventId: String): Flow<EventEntitySummary?>

    @Query("SELECT * FROM v_event_detailed_summary WHERE id = :eventId ORDER BY startTime DESC")
    fun getEventDetailedSummary(eventId: String): Flow<EventEntityDetailedSummary?>

    @Query("""
        SELECT event_table.* FROM event_table 
        INNER JOIN fish_table ON event_table.id = fish_table.eventId
        WHERE event_table.tripId = :tripId
          AND (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
        GROUP BY event_table.id
        ORDER BY startTime DESC
    """)
    fun getEventsWithFish(
        bodyOfWaterId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<List<EventEntity>>
}