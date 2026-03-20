package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SegmentDao {
    @Query("SELECT * FROM segment_table WHERE tripId = :tripId")
    fun getSegmentsForTrip(tripId: Int): Flow<List<Segment>>

    @Query("SELECT * FROM segment_table WHERE endTime IS NULL ORDER BY startTime DESC")
    fun getActiveSegments(): Flow<List<Segment>>

    @Transaction
    @Query("SELECT * FROM segment_table WHERE id = :segmentId")
    fun getSegmentWithFishermen(segmentId: Int): Flow<SegmentWithFishermen>

    @Transaction
    @Query("SELECT * FROM segment_table WHERE id = :segmentId")
    fun getSegmentWithDetails(segmentId: Int): Flow<SegmentWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: Segment): Long

    @Delete
    suspend fun deleteSegment(segment: Segment)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef)

    @Delete
    suspend fun deleteSegmentFishermanCrossRef(crossRef: SegmentFishermanCrossRef)
}
