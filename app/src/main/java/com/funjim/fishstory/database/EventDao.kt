package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event_table ORDER BY startTime DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("DELETE FROM event_table")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM event_table WHERE tripId = :tripId")
    fun getEventsForTrip(tripId: String): Flow<List<Event>>

    @Query("""
    SELECT s.* FROM event_table AS s
    JOIN trip_table AS t ON s.tripId = t.id
    WHERE strftime('%s', 'now') BETWEEN t.startDate AND t.endDate
    ORDER BY s.startTime ASC
""")
    fun getEventsForActiveTrips(): Flow<List<Event>>

    @Query(
        """
WITH 
-- 1. Find the biggest fish for every event
BigFishPerEvent AS (
    SELECT 
        f.eventId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
),
-- 2. Find the fisherman with the most catches per event
MostCaughtPerEvent AS (
    SELECT 
        f.eventId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    GROUP BY f.eventId, f.fishermanId
)

SELECT 
    s.*,
    -- Basic Counts
    (SELECT COUNT(*) FROM fish_table f WHERE f.eventId = s.id) as fishCaught,
    (SELECT COUNT(*) FROM fish_table f WHERE f.eventId = s.id AND f.isReleased = 0) as fishKept,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
    
    -- Big Fish Data (joined from CTE)
    CASE 
        WHEN bfm.nickname IS NOT NULL AND bfm.nickname != '' 
        THEN bfm.firstName || ' "' || bfm.nickname || '" ' || bfm.lastName 
        ELSE bfm.firstName || ' ' || bfm.lastName 
    END as bigFishName,
    bsp.name as bigFishSpecies,
    bf.length as bigFishLength,
    
    -- Most Caught Data (joined from CTE)
    CASE 
        WHEN mcm.nickname IS NOT NULL AND mcm.nickname != '' 
        THEN mcm.firstName || ' "' || mcm.nickname || '" ' || mcm.lastName 
        ELSE mcm.firstName || ' ' || mcm.lastName 
    END as mostCaughtName,
    mc.catchCount as mostCaught

FROM event_table s
JOIN trip_table AS t ON s.tripId = t.id

-- Join Big Fish logic
LEFT JOIN BigFishPerEvent bf ON s.id = bf.eventId AND bf.row_num = 1
LEFT JOIN fisherman_table bfm ON bf.fishermanId = bfm.id
LEFT JOIN species_table bsp ON bf.speciesId = bsp.id

-- Join Most Caught logic
LEFT JOIN MostCaughtPerEvent mc ON s.id = mc.eventId AND mc.row_num = 1
LEFT JOIN fisherman_table mcm ON mc.fishermanId = mcm.id

WHERE :currentTime BETWEEN t.startDate AND t.endDate
ORDER BY s.startTime ASC
"""
    )
    fun getEventsForActiveTrips(currentTime: Long): Flow<List<EventSummary>>

    @Query("SELECT * FROM event_table WHERE id = :id")
    fun getEventById(id: String): Flow<Event?>

    @Transaction
    @Query("SELECT * FROM event_table WHERE tripId = :tripId")
    fun getEventsWithDetailsForTrip(tripId: String): Flow<List<EventWithDetails>>

    @Query("""
    SELECT * FROM event_table 
    WHERE startTime <= :currentTime 
    AND (endTime IS NULL OR endTime >= :currentTime) 
    ORDER BY startTime DESC
""")
    fun getActiveEvents(currentTime: Long = System.currentTimeMillis()): Flow<List<Event>>

    @Transaction
    @Query("SELECT * FROM event_table WHERE id = :eventId")
    fun getEventWithFishermen(eventId: String): Flow<EventWithFishermen?>

    @Query("""
    SELECT DISTINCT f.* FROM fisherman_table AS f
    JOIN event_fisherman_cross_ref AS xr ON f.id = xr.fishermanId
    WHERE xr.eventId = :eventId
""")
    fun getFishermenForEvent(eventId: String): Flow<List<Fisherman>>

    @Transaction
    @Query("SELECT * FROM event_table WHERE id = :eventId")
    fun getEventWithDetails(eventId: String): Flow<EventWithDetails?>

    // TODO - convert these upsert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Upsert
    suspend fun upsertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM event_table WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("SELECT * FROM event_table WHERE tripId = :tripId AND name = :name")
    suspend fun findEventByName(tripId: String, name: String): Event?

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(tripId: String, name: String): String {
        val foundEvent = findEventByName(tripId, name)
        return if (foundEvent != null) {
            foundEvent.id
        } else {
            val newEvent = Event(tripId = tripId, name = name)
            upsertEvent(newEvent)
            newEvent.id
        }
    }

    @Query(
        """
WITH 
-- 1. Get the biggest fish per event for this specific trip
BigFishPerEvent AS (
    SELECT 
        f.eventId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
    WHERE f.tripId = :tripId
),
-- 2. Get the fisherman with the most catches per event for this specific trip
MostCaughtPerEvent AS (
    SELECT 
        f.eventId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    WHERE f.tripId = :tripId
    GROUP BY f.eventId, f.fishermanId
)

SELECT 
    s.*,
    -- Basic Counts
    (SELECT COUNT(*) FROM fish_table f WHERE f.eventId = s.id) as fishCaught,
    (SELECT COUNT(*) FROM fish_table f WHERE f.eventId = s.id AND f.isReleased = 0) as fishKept,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
    
    -- Big Fish Data (joined from CTE)
    CASE 
        WHEN bfm.nickname IS NOT NULL AND bfm.nickname != '' 
        THEN bfm.firstName || ' "' || bfm.nickname || '" ' || bfm.lastName 
        ELSE bfm.firstName || ' ' || bfm.lastName 
    END as bigFishName,
    bsp.name as bigFishSpecies,
    bf.length as bigFishLength,
    
    -- Most Caught Data (joined from CTE)
    CASE 
        WHEN mcm.nickname IS NOT NULL AND mcm.nickname != '' 
        THEN mcm.firstName || ' "' || mcm.nickname || '" ' || mcm.lastName 
        ELSE mcm.firstName || ' ' || mcm.lastName 
    END as mostCaughtName,
    mc.catchCount as mostCaught

FROM event_table s

-- Left join ensures we don't hide events with no fish yet
LEFT JOIN BigFishPerEvent bf ON s.id = bf.eventId AND bf.row_num = 1
LEFT JOIN fisherman_table bfm ON bf.fishermanId = bfm.id
LEFT JOIN species_table bsp ON bf.speciesId = bsp.id

LEFT JOIN MostCaughtPerEvent mc ON s.id = mc.eventId AND mc.row_num = 1
LEFT JOIN fisherman_table mcm ON mc.fishermanId = mcm.id

WHERE s.tripId = :tripId
ORDER BY s.startTime DESC"""
    )
    fun getTripEventSummaries(tripId: String): Flow<List<EventSummary>>


    // TODO -- convert these to upsert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventFishermanCrossRef(crossRef: EventFishermanCrossRef)

    @Upsert
    suspend fun upsertEventFishermanCrossRef(crossRef: EventFishermanCrossRef)

    @Update
    suspend fun updateEventFishermanCrossRef(crossRef: EventFishermanCrossRef)

    @Query("SELECT * FROM event_fisherman_cross_ref")
    fun getAllEventFishermanCrossRefs(): Flow<List<EventFishermanCrossRef>>

    @Query("SELECT tackleBoxId FROM event_fisherman_cross_ref WHERE eventId = :eventId AND fishermanId = :fishermanId")
    fun getTackleBoxIdForFisherman(eventId: String, fishermanId: String): Flow<String?>

    @Query(
        """
    SELECT fishermanId, tackleBoxId 
    FROM event_fisherman_cross_ref 
    WHERE eventId = :eventId
"""
    )
    fun getFishermanTackleBoxMapping(eventId: String): Flow<Map<
            @MapColumn(columnName = "fishermanId") String,
            @MapColumn(columnName = "tackleBoxId") String?
            >>

    @Query("DELETE FROM event_fisherman_cross_ref WHERE eventId = :eventId AND fishermanId NOT IN (:fishermenIds)")
    suspend fun removeFishermenNotInSet(eventId: String, fishermenIds: Set<String>)

    @Delete
    suspend fun deleteEventFishermanCrossRef(crossRef: EventFishermanCrossRef)

    @Query("DELETE FROM event_fisherman_cross_ref")
    suspend fun deleteAllEventFishermanCrossRefs()
}
