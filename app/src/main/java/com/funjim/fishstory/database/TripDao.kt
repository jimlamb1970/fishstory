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

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTrip(tripId: String): Flow<Trip?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?>

    @Query("""
    SELECT 
        t.*,
        (SELECT COUNT(*) FROM fish_table f WHERE f.tripId = t.id) as totalCaught,
        (SELECT COUNT(*) FROM fish_table f WHERE f.tripId = t.id AND f.isReleased = 0) as totalKept,
        (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id) as fishermanCount,
        (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
        
        -- BIG FISH LOGIC (Added f.id tie-breaker)
        (
            SELECT 
                CASE 
                    WHEN fm.nickname IS NOT NULL AND fm.nickname != '' 
                    THEN fm.firstName || ' "' || fm.nickname || '" ' || fm.lastName 
                    ELSE fm.firstName || ' ' || fm.lastName 
                END
            FROM fish_table f 
            JOIN fisherman_table fm ON f.fishermanId = fm.id 
            WHERE f.tripId = t.id 
            ORDER BY f.length DESC, f.id ASC LIMIT 1
        ) as bigFishName,
        (
            SELECT sp.name
            FROM fish_table f
            JOIN species_table sp ON f.speciesId = sp.id
            WHERE f.tripId = t.id
            ORDER BY f.length DESC, f.id ASC LIMIT 1
        ) as bigFishSpecies,
        (
            SELECT f.length
            FROM fish_table f
            WHERE f.tripId = t.id
            ORDER BY f.length DESC, f.id ASC LIMIT 1
        ) as bigFishLength,
        
        -- MOST CAUGHT LOGIC (Added fm.id tie-breaker)
        (
            SELECT 
                CASE 
                    WHEN fm.nickname IS NOT NULL AND fm.nickname != '' 
                    THEN fm.firstName || ' "' || fm.nickname || '" ' || fm.lastName 
                    ELSE fm.firstName || ' ' || fm.lastName 
                END
            FROM fish_table f 
            JOIN fisherman_table fm ON f.fishermanId = fm.id 
            WHERE f.tripId = t.id 
            GROUP BY f.fishermanId 
            ORDER BY COUNT(f.id) DESC, fm.id ASC LIMIT 1
        ) as mostCaughtName,
        (
            SELECT COUNT(f.id)
            FROM fish_table f 
            WHERE f.tripId = t.id 
            GROUP BY f.fishermanId 
            ORDER BY COUNT(f.id) DESC, f.fishermanId ASC LIMIT 1
        ) as mostCaught
    FROM trip_table t
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
    suspend fun removeFishermanCrossRefFromTripAndAllSegments(tripId: String, fishermanId: String) {
        // 1. Remove from the Trip level
        deleteFishermanFromTrip(tripId, fishermanId)

        // 2. Remove from all segments that belong to this specific trip
        // This uses a subquery to find every segment tied to that tripId
        deleteFishermanFromSegmentsForTrip(tripId, fishermanId)
    }

    @Query("DELETE FROM trip_fisherman_cross_ref WHERE tripId = :tripId AND fishermanId = :fishermanId")
    suspend fun deleteFishermanFromTrip(tripId: String, fishermanId: String)

    @Query("""
    DELETE FROM segment_fisherman_cross_ref 
    WHERE fishermanId = :fishermanId 
    AND segmentId IN (SELECT id FROM segment_table WHERE tripId = :tripId)
""")
    suspend fun deleteFishermanFromSegmentsForTrip(tripId: String, fishermanId: String)
}
