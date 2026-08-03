package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_table ORDER BY startDate DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("DELETE FROM trip_table")
    suspend fun deleteAllTrips()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrip(trip: TripEntity)

    @Upsert
    suspend fun upsertTrip(trip: TripEntity)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trip_table WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(name: String): String {
        val existingTrip = getTripByName(name)
        return if (existingTrip != null) {
            existingTrip.id
        } else {
            val newTrip = TripEntity(name = name)
            upsertTrip(newTrip)
            newTrip.id
        }
    }

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTrip(tripId: String): Flow<TripEntity?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripById(tripId: String): Flow<TripEntity?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE name = :name")
    fun getTripByName(name: String): TripEntity?

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithFishermen(tripId: String): Flow<TripEntityWithFishermen?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithFishermenAndSpecies(tripId: String): Flow<TripEntityWithFishermenAndSpecies?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithDetails(tripId: String): Flow<TripEntityWithDetails?>

    @Transaction
    @Query("""
SELECT 
    t.*,
    -- Counts
    (SELECT COUNT(*) FROM event_table et WHERE et.tripId = t.id) as eventCount,

    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.tripId = t.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.tripId = t.id) as fishKept,

    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id) as fishermanCount,
    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.tripId = t.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.tripId = t.id) as targetFishKept

FROM trip_table t
ORDER BY t.startDate DESC
""")
    fun getTripSummaries(): Flow<List<TripEntitySummary>>

    @Transaction
    @Query("""
SELECT 
    t.*,
    -- Counts
    (SELECT COUNT(*) FROM event_table et WHERE et.tripId = t.id) as eventCount,

    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.tripId = t.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.tripId = t.id) as fishKept,

    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id) as fishermanCount,
    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.tripId = t.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.tripId = t.id) as targetFishKept

FROM trip_table t
WHERE t.id = :tripId
ORDER BY t.startDate DESC
""")
    fun getTripSummary(tripId: String): Flow<TripEntitySummary?>

    @Query("SELECT * FROM v_trip_detailed_summary WHERE id = :tripId ORDER BY startDate DESC")
    fun getTripDetailedSummary(tripId: String): Flow<TripEntityDetailedSummary?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: TripFishermanEntity)

    @Upsert
    suspend fun upsertTripFisherman(crossRef: TripFishermanEntity)

    // TODO -- replace updateTripFishmanTackleBox with upsertTripFishermanCrossRef
    @Update
    suspend fun updateTripFishermanTackleBox(crossRef: TripFishermanEntity)

    @Query("SELECT * FROM trip_fisherman_cross_ref")
    fun getAllTripFishermanCrossRefs(): Flow<List<TripFishermanEntity>>

    @Query("SELECT * FROM trip_fisherman_cross_ref WHERE tripId = :tripId")
    fun getTripFishermanCrossRefs(tripId: String): Flow<List<TripFishermanEntity>>

    @Query("""
    SELECT DISTINCT f.* FROM fisherman_table AS f
    JOIN trip_fisherman_cross_ref AS xr ON f.id = xr.fishermanId
    WHERE xr.tripId = :tripId
""")
    fun getFishermenForTrip(tripId: String): Flow<List<FishermanEntity>>

    @Query("""
    SELECT fishermanId, tackleBoxId 
    FROM trip_fisherman_cross_ref 
    WHERE tripId = :tripId
""")
    fun getTripFishermenTackleBoxIds(tripId: String): Flow<Map<
            @MapColumn(columnName = "fishermanId") String,
            @MapColumn(columnName = "tackleBoxId") String?
            >>

    @Query("SELECT * FROM trip_fisherman_cross_ref WHERE tripId = :tripId AND fishermanId = :fishermanId LIMIT 1")
    suspend fun getTripFishermanCrossRef(tripId: String, fishermanId: String): TripFishermanEntity?

    @Query("DELETE FROM trip_fisherman_cross_ref")
    suspend fun deleteAllTripFishermanCrossRefs()

    @Transaction
    suspend fun removeFishermanCrossRefFromTripAndAllEvents(tripId: String, fishermanId: String) {
        // 1. Remove from the Trip level
        deleteFishermanFromTrip(tripId, fishermanId)

        // 2. Remove from all events that belong to this specific trip
        // This uses a subquery to find every event tied to that tripId
        deleteFishermanFromEvents(tripId, fishermanId)
    }

    @Query("DELETE FROM trip_fisherman_cross_ref WHERE tripId = :tripId AND fishermanId = :fishermanId")
    suspend fun deleteFishermanFromTrip(tripId: String, fishermanId: String)

    @Query("""
    DELETE FROM event_fisherman_cross_ref 
    WHERE fishermanId = :fishermanId 
    AND eventId IN (SELECT id FROM event_table WHERE tripId = :tripId)
""")
    suspend fun deleteFishermanFromEvents(tripId: String, fishermanId: String)

    @Query("""
        SELECT trip_table.* FROM trip_table 
        INNER JOIN fish_table ON trip_table.id = fish_table.tripId 
        WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
        GROUP BY trip_table.id
    """)
    fun getTripsWithFish(
        bodyOfWaterId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null
    ): Flow<List<TripEntity>>

    @Query("SELECT * FROM trip_target_species")
    fun getAllTripTargetSpecies(): Flow<List<TripTargetSpeciesEntity>>

    @Query("""
        SELECT species_table.* FROM species_table
        INNER JOIN trip_target_species ON species_table.id = trip_target_species.speciesId
        WHERE trip_target_species.tripId = :tripId
        GROUP BY species_table.id
        """)
    fun getTripTargetSpecies(tripId: String): Flow<List<SpeciesEntity>>

    @Upsert
    suspend fun insertTripTargetSpecies(crossRef: TripTargetSpeciesEntity)

    @Query("DELETE FROM trip_target_species WHERE tripId = :tripId AND speciesId = :speciesId")
    suspend fun deleteTripTargetSpecies(tripId: String, speciesId: String)

    @Query("DELETE FROM trip_target_species")
    suspend fun deleteAllTripTargetSpecies()
}
