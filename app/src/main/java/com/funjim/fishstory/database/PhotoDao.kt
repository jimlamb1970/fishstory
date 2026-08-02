package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo_table")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photo_bait_cross_ref")
    fun getAllPhotosForBait(): Flow<List<PhotoBaitEntity>>
    @Query("SELECT * FROM photo_body_of_water_cross_ref")
    fun getAllPhotosForBodiesOfWater(): Flow<List<PhotoBodyOfWaterEntity>>
    @Query("SELECT * FROM photo_event_cross_ref")
    fun getAllPhotosForEvents(): Flow<List<PhotoEventEntity>>
    @Query("SELECT * FROM photo_fish_cross_ref")
    fun getAllPhotosForFish(): Flow<List<PhotoFishEntity>>
    @Query("SELECT * FROM photo_fisherman_cross_ref")
    fun getAllPhotosForFishermen(): Flow<List<PhotoFishermanEntity>>
    @Query("SELECT * FROM photo_lure_cross_ref")
    fun getAllPhotosForLures(): Flow<List<PhotoLureEntity>>
    @Query("SELECT * FROM photo_species_cross_ref")
    fun getAllPhotosForSpecies(): Flow<List<PhotoSpeciesEntity>>
    @Query("SELECT * FROM photo_trip_cross_ref")
    fun getAllPhotosForTrips(): Flow<List<PhotoTripEntity>>

    @Query("DELETE FROM photo_table")
    suspend fun deleteAllPhotos()

    @Query("DELETE FROM photo_bait_cross_ref")
    suspend fun deleteAllPhotosForBait()
    @Query("DELETE FROM photo_body_of_water_cross_ref")
    suspend fun deleteAllPhotosForBodiesOfWater()
    @Query("DELETE FROM photo_event_cross_ref")
    suspend fun deleteAllPhotosForEvents()
    @Query("DELETE FROM photo_fish_cross_ref")
    suspend fun deleteAllPhotosForFish()
    @Query("DELETE FROM photo_fisherman_cross_ref")
    suspend fun deleteAllPhotosForFishermen()
    @Query("DELETE FROM photo_lure_cross_ref")
    suspend fun deleteAllPhotosForLures()
    @Query("DELETE FROM photo_species_cross_ref")
    suspend fun deleteAllPhotosForSpecies()
    @Query("DELETE FROM photo_trip_cross_ref")
    suspend fun deleteAllPhotosForTrips()


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Query("SELECT id FROM photo_table WHERE uri = :uri LIMIT 1")
    suspend fun getPhotoIdByUri(uri: String): String?

    @Query("SELECT id FROM photo_table WHERE hashcode = :hashcode LIMIT 1")
    suspend fun getPhotoIdByHash(hashcode: String): String?

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Upsert
    suspend fun addBaitPhoto(crossRef: PhotoBaitEntity)
    @Delete
    suspend fun deleteBaitPhoto(crossRef: PhotoBaitEntity)
    @Upsert
    suspend fun addBodyOfWaterPhoto(crossRef: PhotoBodyOfWaterEntity)
    @Delete
    suspend fun deleteBodyOfWaterPhoto(crossRef: PhotoBodyOfWaterEntity)
    @Upsert
    suspend fun addTripPhoto(crossRef: PhotoTripEntity)
    @Delete
    suspend fun deleteTripPhoto(crossRef: PhotoTripEntity)
    @Upsert
    suspend fun addEventPhoto(crossRef: PhotoEventEntity)
    @Delete
    suspend fun deleteEventPhoto(crossRef: PhotoEventEntity)
    @Upsert
    suspend fun addFishermanPhoto(crossRef: PhotoFishermanEntity)
    @Delete
    suspend fun deleteFishermanPhoto(crossRef: PhotoFishermanEntity)
    @Upsert
    suspend fun addLurePhoto(crossRef: PhotoLureEntity)
    @Delete
    suspend fun deleteLurePhoto(crossRef: PhotoLureEntity)
    @Upsert
    suspend fun addFishPhoto(crossRef: PhotoFishEntity)
    @Delete
    suspend fun deleteFishPhoto(crossRef: PhotoFishEntity)
    @Upsert
    suspend fun addSpeciesPhoto(crossRef: PhotoSpeciesEntity)
    @Delete
    suspend fun deleteSpeciesPhoto(crossRef: PhotoSpeciesEntity)


    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_trip_cross_ref ON photo_table.id = photo_trip_cross_ref.photoId
    WHERE photo_trip_cross_ref.tripId = :tripId
""")
    fun getPhotosForTrip(tripId: String): Flow<List<PhotoEntity>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_event_cross_ref ON photo_table.id = photo_event_cross_ref.photoId
    WHERE photo_event_cross_ref.eventId = :eventId
""")
    fun getPhotosForEvent(eventId: String): Flow<List<PhotoEntity>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_lure_cross_ref ON photo_table.id = photo_lure_cross_ref.photoId
    WHERE photo_lure_cross_ref.lureId = :lureId
""")
    fun getPhotosForLure(lureId: String): Flow<List<PhotoEntity>>

    @Query("""
    SELECT photo_table.* FROM photo_table 
    INNER JOIN photo_lure_cross_ref ON photo_table.id = photo_lure_cross_ref.photoId
""")
    fun getAllLurePhotos(): Flow<List<PhotoEntity>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fish_cross_ref ON photo_table.id = photo_fish_cross_ref.photoId
    WHERE photo_fish_cross_ref.fishId = :fishId
""")
    fun getPhotosForFish(fishId: String): Flow<List<PhotoEntity>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fish_cross_ref ON photo_table.id = photo_fish_cross_ref.photoId
""")
    fun getAllFishPhotos(): Flow<List<PhotoEntity>>

    @Query("""
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fisherman_cross_ref ON photo_table.id = photo_fisherman_cross_ref.photoId
    WHERE photo_fisherman_cross_ref.fishermanId = :fishermanId
""")
    fun getPhotosForFisherman(fishermanId: String): Flow<List<PhotoEntity>>

    @Query("""
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_bait_cross_ref ON photo_table.id = photo_bait_cross_ref.photoId
        WHERE photo_bait_cross_ref.baitId = :id
        LIMIT 1
    """)
    suspend fun getPhotoForBait(id: String): PhotoEntity?

    @Query("""
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_body_of_water_cross_ref ON photo_table.id = photo_body_of_water_cross_ref.photoId
        WHERE photo_body_of_water_cross_ref.bodyOfWaterId = :id
        LIMIT 1
    """)
    suspend fun getPhotoForBodyOfWater(id: String): PhotoEntity?

    @Query("""
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_species_cross_ref ON photo_table.id = photo_species_cross_ref.photoId
        WHERE photo_species_cross_ref.speciesId = :speciesId
        LIMIT 1
    """)
    suspend fun getPhotoForSpecies(speciesId: String): PhotoEntity?

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
    INNER JOIN photo_bait_cross_ref ON photo_table.id = photo_bait_cross_ref.photoId
    WHERE baitId = :id 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForBait(id: String): Flow<ByteArray?>

    @Query("""
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_body_of_water_cross_ref ON photo_table.id = photo_body_of_water_cross_ref.photoId
    WHERE bodyOfWaterId = :bodyOfWaterId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForBodyOfWater(bodyOfWaterId: String): Flow<ByteArray?>

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

    @Query("""
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_species_cross_ref ON photo_table.id = photo_species_cross_ref.photoId
    WHERE speciesId = :speciesId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
""")
    fun getThumbnailForSpecies(speciesId: String): Flow<ByteArray?>

    @Transaction
    suspend fun setPrimaryPhotoForEvent(eventId: String, photoId: String) {
        clearPrimaryPhotoForEvent(eventId)
        setPhotoAsPrimaryForEvent(eventId, photoId)
    }

    @Query("UPDATE photo_event_cross_ref SET isPrimary = 0 WHERE eventId = :eventId")
    suspend fun clearPrimaryPhotoForEvent(eventId: String)

    @Query("UPDATE photo_event_cross_ref SET isPrimary = 1 WHERE eventId = :eventId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForEvent(eventId: String, photoId: String)

    @Transaction
    suspend fun setPrimaryPhotoForFisherman(fishermanId: String, photoId: String) {
        clearPrimaryPhotoForFisherman(fishermanId)
        setPhotoAsPrimaryForFisherman(fishermanId, photoId)
    }

    @Query("UPDATE photo_fisherman_cross_ref SET isPrimary = 0 WHERE fishermanId = :fishermanId")
    suspend fun clearPrimaryPhotoForFisherman(fishermanId: String)

    @Query("UPDATE photo_fisherman_cross_ref SET isPrimary = 1 WHERE fishermanId = :fishermanId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForFisherman(fishermanId: String, photoId: String)

    @Transaction
    suspend fun setPrimaryPhotoForLure(lureId: String, photoId: String) {
        clearPrimaryPhotoForLure(lureId)
        setPhotoAsPrimaryForLure(lureId, photoId)
    }

    @Query("UPDATE photo_lure_cross_ref SET isPrimary = 0 WHERE lureId = :lureId")
    suspend fun clearPrimaryPhotoForLure(lureId: String)

    @Query("UPDATE photo_lure_cross_ref SET isPrimary = 1 WHERE lureId = :lureId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForLure(lureId: String, photoId: String)

    @Transaction
    suspend fun setPrimaryPhotoForTrip(tripId: String, photoId: String) {
        clearPrimaryPhotoForTrip(tripId)
        setPhotoAsPrimaryForTrip(tripId, photoId)
    }

    @Query("UPDATE photo_trip_cross_ref SET isPrimary = 0 WHERE tripId = :tripId")
    suspend fun clearPrimaryPhotoForTrip(tripId: String)

    @Query("UPDATE photo_trip_cross_ref SET isPrimary = 1 WHERE tripId = :tripId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForTrip(tripId: String, photoId: String)
}
