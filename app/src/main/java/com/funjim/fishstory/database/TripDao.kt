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
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripFishermanCrossRef
import com.funjim.fishstory.model.TripWithDetails
import com.funjim.fishstory.model.TripWithFishermen
import com.funjim.fishstory.model.TripSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_table ORDER BY startDate DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("DELETE FROM trip_table")
    suspend fun deleteAllTrips()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrip(trip: Trip)

    @Upsert
    suspend fun upsertTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("DELETE FROM trip_table WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(name: String): String {
        val existingTrip = getTripByName(name)
        return if (existingTrip != null) {
            existingTrip.id
        } else {
            val newTrip = Trip(name = name)
            upsertTrip(newTrip)
            newTrip.id
        }
    }

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTrip(tripId: String): Flow<Trip?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE name = :name")
    fun getTripByName(name: String): Trip?

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?>

    @Query("""
WITH 
-- 1. Identify the single biggest fish for every trip
BigFishPerTrip AS (
    SELECT 
        f.tripId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.tripId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
),
-- 2. Identify the fisherman with the most catches per trip
MostCaughtPerTrip AS (
    SELECT 
        f.tripId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.tripId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    GROUP BY f.tripId, f.fishermanId
)

SELECT 
    t.*,
    -- Counts
    (SELECT COUNT(*) FROM event_table et WHERE et.tripId = t.id) as eventCount,
    (SELECT COUNT(*) FROM fish_table f WHERE f.tripId = t.id) as totalCaught,
    (SELECT COUNT(*) FROM fish_table f WHERE f.tripId = t.id AND f.isReleased = 0) as totalKept,
    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id) as fishermanCount,
    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
    
    -- Big Fish Data (joined via CTE)
    CASE 
        WHEN bfm.nickname IS NOT NULL AND bfm.nickname != '' 
        THEN bfm.firstName || ' "' || bfm.nickname || '" ' || bfm.lastName 
        ELSE bfm.firstName || ' ' || bfm.lastName 
    END as bigFishName,
    bsp.name as bigFishSpecies,
    bf.length as bigFishLength,
    
    -- Most Caught Data (joined via CTE)
    CASE 
        WHEN mcm.nickname IS NOT NULL AND mcm.nickname != '' 
        THEN mcm.firstName || ' "' || mcm.nickname || '" ' || mcm.lastName 
        ELSE mcm.firstName || ' ' || mcm.lastName 
    END as mostCaughtName,
    mc.catchCount as mostCaught

FROM trip_table t
-- Join Big Fish
LEFT JOIN BigFishPerTrip bf ON t.id = bf.tripId AND bf.row_num = 1
LEFT JOIN fisherman_table bfm ON bf.fishermanId = bfm.id
LEFT JOIN species_table bsp ON bf.speciesId = bsp.id

-- Join Most Caught
LEFT JOIN MostCaughtPerTrip mc ON t.id = mc.tripId AND mc.row_num = 1
LEFT JOIN fisherman_table mcm ON mc.fishermanId = mcm.id

ORDER BY t.startDate DESC
""")
    fun getTripSummaries(): Flow<List<TripSummary>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: TripFishermanCrossRef)

    @Upsert
    suspend fun upsertTripFishermanCrossRef(crossRef: TripFishermanCrossRef)

    // TODO -- replace updateTripFishmanTackleBox with upsertTripFishermanCrossRef
    @Update
    suspend fun updateTripFishermanTackleBox(crossRef: TripFishermanCrossRef)

    @Query("SELECT * FROM trip_fisherman_cross_ref")
    fun getAllTripFishermanCrossRefs(): Flow<List<TripFishermanCrossRef>>

    @Query("SELECT * FROM trip_fisherman_cross_ref WHERE tripId = :tripId")
    fun getTripFishermanCrossRefs(tripId: String): Flow<List<TripFishermanCrossRef>>

    @Query("SELECT fishermanId FROM trip_fisherman_cross_ref WHERE tripId = :tripId")
    fun getFishermanIdsForTrip(tripId: String): Flow<List<String>>

    @Query("SELECT tackleBoxId FROM trip_fisherman_cross_ref WHERE tripId = :tripId AND fishermanId = :fishermanId")
    fun getTripFishermanTackleBoxId(tripId: String, fishermanId: String): Flow<String?>

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
    suspend fun getTripFishermanCrossRef(tripId: String, fishermanId: String): TripFishermanCrossRef?

    @Query("DELETE FROM trip_fisherman_cross_ref WHERE tripId = :tripId AND fishermanId NOT IN (:fishermenIds)")
    suspend fun removeFishermenNotInSet(tripId: String, fishermenIds: Set<String>)

    // TODO -- replace deleteCrossRef with deleteTripFishermanCrossRef
    @Delete
    suspend fun deleteCrossRef(crossRef: TripFishermanCrossRef)

    @Delete
    suspend fun deleteTripFishermanCrossRef(crossRef: TripFishermanCrossRef)

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
}
