package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.PhotoEventCrossRef
import com.funjim.fishstory.model.PhotoFishCrossRef
import com.funjim.fishstory.model.PhotoFishermanCrossRef
import com.funjim.fishstory.model.PhotoLureCrossRef
import com.funjim.fishstory.model.PhotoTripCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo_table")
    fun getAllPhotos(): Flow<List<Photo>>

    @Query("DELETE FROM photo_table")
    suspend fun deleteAllPhotos()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPhoto(photo: Photo): Long

    @Query("SELECT id FROM photo_table WHERE uri = :uri LIMIT 1")
    suspend fun getPhotoIdByUri(uri: String): String?

    @Query("SELECT id FROM photo_table WHERE hashcode = :hashcode LIMIT 1")
    suspend fun getPhotoIdByHash(hashcode: String): String?

    @Upsert
    suspend fun upsertPhotoEventCrossRef(crossRef: PhotoEventCrossRef)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Upsert
    suspend fun addTripPhoto(crossRef: PhotoTripCrossRef)
    @Delete
    suspend fun deleteTripPhoto(crossRef: PhotoTripCrossRef)
    @Upsert
    suspend fun addEventPhoto(crossRef: PhotoEventCrossRef)
    @Delete
    suspend fun deleteEventPhoto(crossRef: PhotoEventCrossRef)
    @Upsert
    suspend fun addFishermanPhoto(crossRef: PhotoFishermanCrossRef)
    @Delete
    suspend fun deleteFishermanPhoto(crossRef: PhotoFishermanCrossRef)
    @Upsert
    suspend fun addLurePhoto(crossRef: PhotoLureCrossRef)
    @Delete
    suspend fun deleteLurePhoto(crossRef: PhotoLureCrossRef)
    @Upsert
    suspend fun addFishPhoto(crossRef: PhotoFishCrossRef)
    @Delete
    suspend fun deleteFishPhoto(crossRef: PhotoFishCrossRef)


    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_trip_cross_ref ON photo_table.id = photo_trip_cross_ref.photoId
    WHERE photo_trip_cross_ref.tripId = :tripId
""")
    fun getPhotosForTrip(tripId: String): Flow<List<Photo>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_event_cross_ref ON photo_table.id = photo_event_cross_ref.photoId
    WHERE photo_event_cross_ref.eventId = :eventId
""")
    fun getPhotosForEvent(eventId: String): Flow<List<Photo>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_lure_cross_ref ON photo_table.id = photo_lure_cross_ref.photoId
    WHERE photo_lure_cross_ref.lureId = :lureId
""")
    fun getPhotosForLure(lureId: String): Flow<List<Photo>>

    @Query("""
    SELECT photo_table.* FROM photo_table 
    INNER JOIN photo_lure_cross_ref ON photo_table.id = photo_lure_cross_ref.photoId
""")
    fun getAllLurePhotos(): Flow<List<Photo>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fish_cross_ref ON photo_table.id = photo_fish_cross_ref.photoId
    WHERE photo_fish_cross_ref.fishId = :fishId
""")
    fun getPhotosForFish(fishId: String): Flow<List<Photo>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fish_cross_ref ON photo_table.id = photo_fish_cross_ref.photoId
""")
    fun getAllFishPhotos(): Flow<List<Photo>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fisherman_cross_ref ON photo_table.id = photo_fisherman_cross_ref.photoId
    WHERE photo_fisherman_cross_ref.fishermanId = :fishermanId
""")
    fun getPhotosForFisherman(fishermanId: String): Flow<List<Photo>>

    @Query("""
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_trip_cross_ref ON photo_table.id = photo_trip_cross_ref.photoId
    WHERE tripId = :tripId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForTrip(tripId: String): Flow<ByteArray?>

    @Query("""
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_event_cross_ref ON photo_table.id = photo_event_cross_ref.photoId
    WHERE eventId = :eventId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForEvent(eventId: String): Flow<ByteArray?>

    @Query("""
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_fish_cross_ref ON photo_table.id = photo_fish_cross_ref.photoId
    WHERE fishId = :fishId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForFish(fishId: String): Flow<ByteArray?>

    @Query("""
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_fisherman_cross_ref ON photo_table.id = photo_fisherman_cross_ref.photoId
    WHERE fishermanId = :fishermanId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForFisherman(fishermanId: String): Flow<ByteArray?>

    @Query("""
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_lure_cross_ref ON photo_table.id = photo_lure_cross_ref.photoId
    WHERE lureId = :lureId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForLure(lureId: String): Flow<ByteArray?>
}
