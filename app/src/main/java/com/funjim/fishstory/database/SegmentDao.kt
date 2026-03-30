package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SegmentDao {
    @Query("SELECT * FROM segment_table WHERE tripId = :tripId")
    fun getSegmentsForTrip(tripId: String): Flow<List<Segment>>

    @Transaction
    @Query("SELECT * FROM segment_table WHERE tripId = :tripId")
    fun getSegmentsWithDetailsForTrip(tripId: String): Flow<List<SegmentWithDetails>>

    @Query("SELECT * FROM segment_table WHERE endTime IS NULL ORDER BY startTime DESC")
    fun getActiveSegments(): Flow<List<Segment>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: Segment)

    @Update
    suspend fun updateSegment(segment: Segment)

    @Delete
    suspend fun deleteSegment(segment: Segment)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef)

    @Delete
    suspend fun deleteSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef)
}
