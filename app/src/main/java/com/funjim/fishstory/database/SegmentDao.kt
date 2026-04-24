package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SegmentDao {
    @Query("SELECT * FROM segment_table ORDER BY startTime DESC")
    fun getAllSegments(): Flow<List<Segment>>

    @Query("DELETE FROM segment_table")
    suspend fun deleteAllSegments()

    @Query("SELECT * FROM segment_table WHERE tripId = :tripId")
    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>>

    @Query("""
    SELECT s.* FROM segment_table AS s
    JOIN trip_table AS t ON s.tripId = t.id
    WHERE strftime('%s', 'now') BETWEEN t.startDate AND t.endDate
    ORDER BY s.startTime ASC
""")
    fun getSegmentsForActiveTrips(): Flow<List<Segment>>

    @Query("""
WITH 
-- 1. Find the biggest fish for every segment
BigFishPerSegment AS (
    SELECT 
        f.segmentId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.segmentId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
),
-- 2. Find the fisherman with the most catches per segment
MostCaughtPerSegment AS (
    SELECT 
        f.segmentId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.segmentId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    GROUP BY f.segmentId, f.fishermanId
)

SELECT 
    s.*,
    -- Basic Counts
    (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id) as fishCaught,
    (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id AND f.isReleased = 0) as fishKept,
    (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
    
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

FROM segment_table s
JOIN trip_table AS t ON s.tripId = t.id

-- Join Big Fish logic
LEFT JOIN BigFishPerSegment bf ON s.id = bf.segmentId AND bf.row_num = 1
LEFT JOIN fisherman_table bfm ON bf.fishermanId = bfm.id
LEFT JOIN species_table bsp ON bf.speciesId = bsp.id

-- Join Most Caught logic
LEFT JOIN MostCaughtPerSegment mc ON s.id = mc.segmentId AND mc.row_num = 1
LEFT JOIN fisherman_table mcm ON mc.fishermanId = mcm.id

WHERE :currentTime BETWEEN t.startDate AND t.endDate
ORDER BY s.startTime ASC
""")
    fun getSegmentsForActiveTrips(currentTime: Long): Flow<List<SegmentSummary>>

    @Query("SELECT * FROM segment_table WHERE id = :id")
    fun getSegment(id: String): Flow<Segment?>

    @Transaction
    @Query("SELECT * FROM segment_table WHERE tripId = :tripId")
    fun getSegmentsWithDetailsForTrip(tripId: String): Flow<List<SegmentWithDetails>>

    @Query("""
    SELECT * FROM segment_table 
    WHERE startTime <= :currentTime 
    AND (endTime IS NULL OR endTime >= :currentTime) 
    ORDER BY startTime DESC
""")
    fun getCurrentActiveSegments(currentTime: Long = System.currentTimeMillis()): Flow<List<Segment>>

    @Transaction
    @Query("SELECT * FROM segment_table WHERE id = :segmentId")
    fun getSegmentWithFishermen(segmentId: String): Flow<SegmentWithFishermen?>

    @Transaction
    @Query("SELECT * FROM segment_table WHERE id = :segmentId")
    fun getSegmentWithDetails(segmentId: String): Flow<SegmentWithDetails?>

    // TODO - convert these upsert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: Segment)

    @Upsert
    suspend fun upsertSegment(segment: Segment)

    @Update
    suspend fun updateSegment(segment: Segment)

    @Delete
    suspend fun deleteSegment(segment: Segment)

    @Query("DELETE FROM segment_table WHERE id = :id")
    suspend fun deleteSegmentById(id: String)

    @Query("SELECT * FROM segment_table WHERE tripId = :tripId AND name = :name")
    suspend fun getSegmentByName(tripId: String, name: String): Segment?

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(tripId: String, name: String): String {
        val existingSegment = getSegmentByName(tripId, name)
        return if (existingSegment != null) {
            existingSegment.id
        } else {
            val newSegment = Segment(tripId = tripId, name = name)
            upsertSegment(newSegment)
            newSegment.id
        }
    }

    @Query("""
WITH 
-- 1. Get the biggest fish per segment for this specific trip
BigFishPerSegment AS (
    SELECT 
        f.segmentId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.segmentId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
    WHERE f.tripId = :tripId
),
-- 2. Get the fisherman with the most catches per segment for this specific trip
MostCaughtPerSegment AS (
    SELECT 
        f.segmentId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.segmentId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    WHERE f.tripId = :tripId
    GROUP BY f.segmentId, f.fishermanId
)

SELECT 
    s.*,
    -- Basic Counts
    (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id) as fishCaught,
    (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id AND f.isReleased = 0) as fishKept,
    (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
    
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

FROM segment_table s

-- Left join ensures we don't hide segments with no fish yet
LEFT JOIN BigFishPerSegment bf ON s.id = bf.segmentId AND bf.row_num = 1
LEFT JOIN fisherman_table bfm ON bf.fishermanId = bfm.id
LEFT JOIN species_table bsp ON bf.speciesId = bsp.id

LEFT JOIN MostCaughtPerSegment mc ON s.id = mc.segmentId AND mc.row_num = 1
LEFT JOIN fisherman_table mcm ON mc.fishermanId = mcm.id

WHERE s.tripId = :tripId
ORDER BY s.startTime DESC""")
    fun getSegmentSummaries(tripId: String): Flow<List<SegmentSummary>>


    // TODO -- convert these to upsert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef)

    @Upsert
    suspend fun upsertSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef)

    @Update
    suspend fun updateSegmentFishermanTackleBox(crossRef: SegmentFishermanCrossRef)

    @Query("SELECT * FROM segment_fisherman_cross_ref")
    fun getAllSegmentFishermanCrossRefs(): Flow<List<SegmentFishermanCrossRef>>

    @Query("SELECT tackleBoxId FROM segment_fisherman_cross_ref WHERE segmentId = :segmentId AND fishermanId = :fishermanId")
    fun getSegmentFishermanTackleBoxId(segmentId: String, fishermanId: String): Flow<String?>

    @Query("""
    SELECT fishermanId, tackleBoxId 
    FROM segment_fisherman_cross_ref 
    WHERE segmentId = :segmentId
""")
    fun getSegmentFishermenTackleBoxIds(segmentId: String): Flow<Map<
            @MapColumn(columnName = "fishermanId") String,
            @MapColumn(columnName = "tackleBoxId") String?
            >>

    @Query("DELETE FROM segment_fisherman_cross_ref WHERE segmentId = :segmentId AND fishermanId NOT IN (:fishermenIds)")
    suspend fun removeFishermenNotInSet(segmentId: String, fishermenIds: Set<String>)

    @Delete
    suspend fun deleteSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef)

    @Query("DELETE FROM segment_fisherman_cross_ref")
    suspend fun deleteAllSegmentFishermanCrossRefs()
}
