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
    suspend fun insertPhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("SELECT * FROM photo_table")
    fun getAllPhotos(): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE tripId = :tripId")
    fun getPhotosForTrip(tripId: String): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE segmentId = :segmentId")
    fun getPhotosForSegment(segmentId: String): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE lureId = :lureId")
    fun getPhotosForLure(lureId: String): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE fishermanId = :fishermanId")
    fun getPhotosForFisherman(fishermanId: String): Flow<List<Photo>>

    @Query("SELECT * FROM photo_table WHERE fishId = :fishId")
    fun getPhotosForFish(fishId: String): Flow<List<Photo>>
}
