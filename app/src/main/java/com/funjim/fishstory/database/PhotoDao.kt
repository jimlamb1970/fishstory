package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.funjim.fishstory.model.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo): Long

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("SELECT * FROM photo_table WHERE tripId = :tripId")
    fun getPhotosForTrip(tripId: Int): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE segmentId = :segmentId")
    fun getPhotosForSegment(segmentId: Int): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE lureId = :lureId")
    fun getPhotosForLure(lureId: Int): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE fishermanId = :fishermanId")
    fun getPhotosForFisherman(fishermanId: Int): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE fishId = :fishId")
    fun getPhotosForFish(fishId: Int): Flow<List<Photo>>
}
