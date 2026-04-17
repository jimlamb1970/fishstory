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
    SELECT 
        s.*,
        (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id) as fishCaught,
        (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id AND f.isReleased = 0) as fishKept,
        (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id) as fishermanCount,
        (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
        (
            SELECT 
                CASE 
                    WHEN fm.nickname IS NOT NULL AND fm.nickname != '' 
                    THEN fm.firstName || ' "' || fm.nickname || '" ' || fm.lastName 
                    ELSE fm.firstName || ' ' || fm.lastName 
                END
            FROM fish_table f 
            JOIN fisherman_table fm ON f.fishermanId = fm.id 
            WHERE f.segmentId = s.id 
            ORDER BY f.length DESC LIMIT 1
        ) as biggestFish,
        (
            SELECT 
                CASE 
                    WHEN fm.nickname IS NOT NULL AND fm.nickname != '' 
                    THEN fm.firstName || ' "' || fm.nickname || '" ' || fm.lastName 
                    ELSE fm.firstName || ' ' || fm.lastName 
                END
            FROM fish_table f 
            JOIN fisherman_table fm ON f.fishermanId = fm.id 
            WHERE f.segmentId = s.id 
            GROUP BY f.fishermanId 
            ORDER BY COUNT(f.id) DESC LIMIT 1
        ) as mostCaughtName,
        (
            SELECT COUNT(f.id)
            FROM fish_table f 
            WHERE f.segmentId = s.id 
            GROUP BY f.fishermanId 
            ORDER BY COUNT(f.id) DESC LIMIT 1
        ) as mostCaught
    FROM segment_table s
    JOIN trip_table AS t ON s.tripId = t.id
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

    @Query("""
    SELECT 
        s.*,
        (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id) as fishCaught,
        (SELECT COUNT(*) FROM fish_table f WHERE f.segmentId = s.id AND f.isReleased = 0) as fishKept,
        (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id) as fishermanCount,
        (SELECT COUNT(*) FROM segment_fisherman_cross_ref xr WHERE xr.segmentId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,
        (
            SELECT 
                CASE 
                    WHEN fm.nickname IS NOT NULL AND fm.nickname != '' 
                    THEN fm.firstName || ' "' || fm.nickname || '" ' || fm.lastName 
                    ELSE fm.firstName || ' ' || fm.lastName 
                END
            FROM fish_table f 
            JOIN fisherman_table fm ON f.fishermanId = fm.id 
            WHERE f.segmentId = s.id 
            ORDER BY f.length DESC LIMIT 1
        ) as biggestFish,
        (
            SELECT 
                CASE 
                    WHEN fm.nickname IS NOT NULL AND fm.nickname != '' 
                    THEN fm.firstName || ' "' || fm.nickname || '" ' || fm.lastName 
                    ELSE fm.firstName || ' ' || fm.lastName 
                END
            FROM fish_table f 
            JOIN fisherman_table fm ON f.fishermanId = fm.id 
            WHERE f.segmentId = s.id 
            GROUP BY f.fishermanId 
            ORDER BY COUNT(f.id) DESC LIMIT 1
        ) as mostCaughtName,    
        (
            SELECT COUNT(f.id)
            FROM fish_table f 
            WHERE f.segmentId = s.id 
            GROUP BY f.fishermanId 
            ORDER BY COUNT(f.id) DESC LIMIT 1
        ) as mostCaught
    FROM segment_table s
    WHERE s.tripId = :tripId
    ORDER BY s.startTime DESC
""")
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
