package com.funjim.fishstory.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event_table ORDER BY startTime DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Transaction
    @Query("SELECT * FROM event_table")
    fun getAllEventsWithInfo(): Flow<List<EventEntityWithInfo>>

    @Query("DELETE FROM event_table")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM event_table WHERE tripId = :tripId")
    fun getEventsForTrip(tripId: String): Flow<List<EventEntity>>

    @Query("SELECT id FROM event_table WHERE tripId = :tripId")
    suspend fun getEventIdsForTrip(tripId: String): List<String>

    @Query(" SELECT tripId FROM event_table WHERE id = :eventId")
    suspend fun getTripIdForEvent(eventId: String): String

    @Query("""
    SELECT s.* FROM event_table AS s
    JOIN trip_table AS t ON s.tripId = t.id
    WHERE strftime('%s', 'now') BETWEEN t.startDate AND t.endDate
    ORDER BY s.startTime ASC
""")
    fun getEventsForActiveTrips(): Flow<List<EventEntity>>

    @Transaction
    @Query(
        """
SELECT 
    s.*,
    -- Basic Counts (Overall)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishKept,

    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    -- Basic Counts (Target Only)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishKept
FROM event_table s
JOIN trip_table AS t ON s.tripId = t.id
WHERE :currentTime BETWEEN t.startDate AND t.endDate
ORDER BY s.startTime ASC
"""
    )
    fun getEventsForActiveTrips(currentTime: Long): Flow<List<EventEntitySummary>>

    @Query("SELECT * FROM event_table WHERE id = :id")
    fun getEventById(id: String): Flow<EventEntity?>

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

    @Query("""
    SELECT DISTINCT f.* FROM fisherman_table AS f
    JOIN event_fisherman_cross_ref AS xr ON f.id = xr.fishermanId
    WHERE xr.eventId = :eventId
""")
    fun getFishermenForEvent(eventId: String): Flow<List<FishermanEntity>>

    @Transaction
    @Query("SELECT * FROM event_table WHERE id = :eventId")
    fun getEventWithDetails(eventId: String): Flow<EventEntityWithDetails?>

    @Transaction
    @Query("SELECT * FROM event_table WHERE id = :eventId")
    fun getEventWithInfo(eventId: String): Flow<EventEntityWithInfo?>

    // TODO - convert these upsert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Upsert
    suspend fun upsertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM event_table WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("SELECT * FROM event_table WHERE tripId = :tripId AND name = :name")
    suspend fun findEventByName(tripId: String, name: String): EventEntity?

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(tripId: String, name: String): String {
        val foundEvent = findEventByName(tripId, name)
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

    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

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

    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

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

    // TODO -- convert these to upsert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEventFishermanCrossRef(crossRef: EventFishermanEntity)

    @Upsert
    suspend fun upsertEventFisherman(crossRef: EventFishermanEntity)

    @Update
    suspend fun updateEventFishermanCrossRef(crossRef: EventFishermanEntity)

    @Query("SELECT * FROM event_fisherman_cross_ref")
    fun getAllEventFishermanCrossRefs(): Flow<List<EventFishermanEntity>>

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

    @Delete
    suspend fun deleteEventFishermanCrossRef(crossRef: EventFishermanEntity)

    @Query("DELETE FROM event_fisherman_cross_ref")
    suspend fun deleteAllEventFishermanCrossRefs()

    @Query("""
        SELECT event_table.* FROM event_table 
        INNER JOIN fish_table ON event_table.id = fish_table.eventId
        WHERE event_table.tripId = :tripId
          AND (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
        GROUP BY event_table.id
    """)
    fun getEventsWithFish(
        bodyOfWaterId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<List<EventEntity>>

    @Query("SELECT * FROM event_target_species")
    fun getAllEventTargetSpecies(): Flow<List<EventTargetSpeciesEntity>>

    @Query("""
        SELECT species_table.* FROM species_table
        INNER JOIN event_target_species ON species_table.id = event_target_species.speciesId
        WHERE event_target_species.eventId = :eventId
        GROUP BY species_table.id
        """)
    fun getEventTargetSpecies(eventId: String): Flow<List<SpeciesEntity>>

    @Upsert
    suspend fun insertEventTargetSpecies(crossRef: EventTargetSpeciesEntity)

    @Upsert
    suspend fun insertTargetSpeciesForEvents(targets: List<EventTargetSpeciesEntity>)

    @Query("DELETE FROM event_target_species WHERE eventId = :eventId AND speciesId = :speciesId")
    suspend fun deleteEventTargetSpecies(eventId: String, speciesId: String)

    @Query("DELETE FROM event_target_species WHERE eventId IN (:eventIds) AND speciesId = :speciesId")
    suspend fun deleteTargetSpeciesForEvents(eventIds: List<String>, speciesId: String)

    @Query("DELETE FROM event_target_species")
    suspend fun deleteAllEventTargetSpecies()
}
