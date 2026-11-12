package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.PhotoBaitEntity
import com.funjim.fishstory.database.entity.PhotoBodyOfWaterEntity
import com.funjim.fishstory.database.entity.PhotoEntity
import com.funjim.fishstory.database.entity.PhotoEventEntity
import com.funjim.fishstory.database.entity.PhotoFishEntity
import com.funjim.fishstory.database.entity.PhotoFishermanEntity
import com.funjim.fishstory.database.entity.PhotoLureEntity
import com.funjim.fishstory.database.entity.PhotoSkyConditionEntity
import com.funjim.fishstory.database.entity.PhotoSpeciesEntity
import com.funjim.fishstory.database.entity.PhotoTripEntity
import com.funjim.fishstory.database.entity.PhotoWaterClarityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo_table")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photo_table")
    suspend fun getAllPhotosList(): List<PhotoEntity>

    @Query("SELECT * FROM photo_bait")
    fun getAllPhotosForBait(): Flow<List<PhotoBaitEntity>>

    @Query("SELECT * FROM photo_bait")
    suspend fun getAllPhotosForBaitList(): List<PhotoBaitEntity>

    @Query("SELECT * FROM photo_body_of_water")
    fun getAllPhotosForBodiesOfWater(): Flow<List<PhotoBodyOfWaterEntity>>

    @Query("SELECT * FROM photo_body_of_water")
    suspend fun getAllPhotosForBodiesOfWaterList(): List<PhotoBodyOfWaterEntity>

    @Query("SELECT * FROM photo_event")
    fun getAllPhotosForEvents(): Flow<List<PhotoEventEntity>>

    @Query("SELECT * FROM photo_event")
    suspend fun getAllPhotosForEventsList(): List<PhotoEventEntity>

    @Query("SELECT * FROM photo_fish")
    fun getAllPhotosForFish(): Flow<List<PhotoFishEntity>>

    @Query("SELECT * FROM photo_fish")
    suspend fun getAllPhotosForFishList(): List<PhotoFishEntity>

    @Query("SELECT * FROM photo_fisherman")
    fun getAllPhotosForFishermen(): Flow<List<PhotoFishermanEntity>>

    @Query("SELECT * FROM photo_fisherman")
    suspend fun getAllPhotosForFishermenList(): List<PhotoFishermanEntity>

    @Query("SELECT * FROM photo_lure")
    fun getAllPhotosForLures(): Flow<List<PhotoLureEntity>>

    @Query("SELECT * FROM photo_lure")
    suspend fun getAllPhotosForLuresList(): List<PhotoLureEntity>

    @Query("SELECT * FROM photo_sky_condition")
    suspend fun getAllPhotosForSkyConditionsList(): List<PhotoSkyConditionEntity>

    @Query("SELECT * FROM photo_species")
    fun getAllPhotosForSpecies(): Flow<List<PhotoSpeciesEntity>>

    @Query("SELECT * FROM photo_species")
    suspend fun getAllPhotosForSpeciesList(): List<PhotoSpeciesEntity>

    @Query("SELECT * FROM photo_trip")
    fun getAllPhotosForTrips(): Flow<List<PhotoTripEntity>>

    @Query("SELECT * FROM photo_trip")
    suspend fun getAllPhotosForTripsList(): List<PhotoTripEntity>

    @Query("SELECT * FROM photo_water_clarity")
    suspend fun getAllPhotosForWaterClarityList(): List<PhotoWaterClarityEntity>

    @Query("DELETE FROM photo_table")
    suspend fun deleteAllPhotos()

    @Query("DELETE FROM photo_bait")
    suspend fun deleteAllPhotosForBait()

    @Query("DELETE FROM photo_body_of_water")
    suspend fun deleteAllPhotosForBodiesOfWater()

    @Query("DELETE FROM photo_event")
    suspend fun deleteAllPhotosForEvents()

    @Query("DELETE FROM photo_fish")
    suspend fun deleteAllPhotosForFish()

    @Query("DELETE FROM photo_fisherman")
    suspend fun deleteAllPhotosForFishermen()

    @Query("DELETE FROM photo_lure")
    suspend fun deleteAllPhotosForLures()

    @Query("DELETE FROM photo_species")
    suspend fun deleteAllPhotosForSpecies()

    @Query("DELETE FROM photo_trip")
    suspend fun deleteAllPhotosForTrips()


    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
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
    suspend fun addSkyConditionPhoto(crossRef: PhotoSkyConditionEntity)
    @Delete
    suspend fun deleteSkyConditionPhoto(crossRef: PhotoSkyConditionEntity)

    @Upsert
    suspend fun addSpeciesPhoto(crossRef: PhotoSpeciesEntity)
    @Delete
    suspend fun deleteSpeciesPhoto(crossRef: PhotoSpeciesEntity)

    @Upsert
    suspend fun addWaterClarityPhoto(crossRef: PhotoWaterClarityEntity)
    @Delete
    suspend fun deleteWaterClarityPhoto(crossRef: PhotoWaterClarityEntity)

    @Query(
        """
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_trip ON photo_table.id = photo_trip.photoId
    WHERE photo_trip.tripId = :tripId
"""
    )
    fun getPhotosForTrip(tripId: String): Flow<List<PhotoEntity>>

    @Query(
        """
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_event ON photo_table.id = photo_event.photoId
    WHERE photo_event.eventId = :eventId
"""
    )
    fun getPhotosForEvent(eventId: String): Flow<List<PhotoEntity>>

    @Query(
        """
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_lure ON photo_table.id = photo_lure.photoId
    WHERE photo_lure.lureId = :lureId
"""
    )
    fun getPhotosForLure(lureId: String): Flow<List<PhotoEntity>>

    @Query(
        """
    SELECT photo_table.* FROM photo_table 
    INNER JOIN photo_lure ON photo_table.id = photo_lure.photoId
"""
    )
    fun getAllLurePhotos(): Flow<List<PhotoEntity>>

    @Query(
        """
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fish ON photo_table.id = photo_fish.photoId
    WHERE photo_fish.fishId = :fishId
"""
    )
    fun getPhotosForFish(fishId: String): Flow<List<PhotoEntity>>

    @Query(
        """
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fish ON photo_table.id = photo_fish.photoId
"""
    )
    fun getAllFishPhotos(): Flow<List<PhotoEntity>>

    @Query(
        """
    SELECT photo_table.* FROM photo_table
    INNER JOIN photo_fisherman ON photo_table.id = photo_fisherman.photoId
    WHERE photo_fisherman.fishermanId = :fishermanId
"""
    )
    fun getPhotosForFisherman(fishermanId: String): Flow<List<PhotoEntity>>

    @Query(
        """
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_bait ON photo_table.id = photo_bait.photoId
        WHERE photo_bait.baitId = :id
        LIMIT 1
    """
    )
    suspend fun getPhotoForBait(id: String): PhotoEntity?

    @Query(
        """
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_body_of_water ON photo_table.id = photo_body_of_water.photoId
        WHERE photo_body_of_water.bodyOfWaterId = :id
        LIMIT 1
    """
    )
    suspend fun getPhotoForBodyOfWater(id: String): PhotoEntity?

    @Query(
        """
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_sky_condition ON photo_table.id = photo_sky_condition.photoId
        WHERE photo_sky_condition.skyConditionId = :id
        LIMIT 1
    """
    )
    suspend fun getPhotoForSkyCondition(id: String): PhotoEntity?

    @Query(
        """
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_species ON photo_table.id = photo_species.photoId
        WHERE photo_species.speciesId = :speciesId
        LIMIT 1
    """
    )
    suspend fun getPhotoForSpecies(speciesId: String): PhotoEntity?

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_trip ON photo_table.id = photo_trip.photoId
    WHERE tripId = :tripId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForTrip(tripId: String): Flow<ByteArray?>

    @Query(
        """
        SELECT photo_table.* FROM photo_table
        INNER JOIN photo_water_clarity ON photo_table.id = photo_water_clarity.photoId
        WHERE photo_water_clarity.waterClarityId = :id
        LIMIT 1
    """
    )
    suspend fun getPhotoForWaterClarity(id: String): PhotoEntity?


    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_bait ON photo_table.id = photo_bait.photoId
    WHERE baitId = :id 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForBait(id: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_body_of_water ON photo_table.id = photo_body_of_water.photoId
    WHERE bodyOfWaterId = :bodyOfWaterId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForBodyOfWater(bodyOfWaterId: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_event ON photo_table.id = photo_event.photoId
    WHERE eventId = :eventId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForEvent(eventId: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_fish ON photo_table.id = photo_fish.photoId
    WHERE fishId = :fishId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForFish(fishId: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_fisherman ON photo_table.id = photo_fisherman.photoId
    WHERE fishermanId = :fishermanId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForFisherman(fishermanId: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_lure ON photo_table.id = photo_lure.photoId
    WHERE lureId = :lureId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForLure(lureId: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_sky_condition ON photo_table.id = photo_sky_condition.photoId
    WHERE skyConditionId = :id 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForSkyCondition(id: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_species ON photo_table.id = photo_species.photoId
    WHERE speciesId = :speciesId 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForSpecies(speciesId: String): Flow<ByteArray?>

    @Query(
        """
    SELECT thumbnail FROM photo_table 
    INNER JOIN photo_water_clarity ON photo_table.id = photo_water_clarity.photoId
    WHERE waterClarityId = :id 
    ORDER BY isPrimary DESC, timestamp ASC 
    LIMIT 1
"""
    )
    fun getThumbnailForWaterClarity(id: String): Flow<ByteArray?>


    @Transaction
    suspend fun setPrimaryPhotoForEvent(eventId: String, photoId: String) {
        clearPrimaryPhotoForEvent(eventId)
        setPhotoAsPrimaryForEvent(eventId, photoId)
    }

    @Query("UPDATE photo_event SET isPrimary = 0 WHERE eventId = :eventId")
    suspend fun clearPrimaryPhotoForEvent(eventId: String)

    @Query("UPDATE photo_event SET isPrimary = 1 WHERE eventId = :eventId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForEvent(eventId: String, photoId: String)

    @Transaction
    suspend fun setPrimaryPhotoForFish(fishId: String, photoId: String) {
        clearPrimaryPhotoForFish(fishId)
        setPhotoAsPrimaryForFish(fishId, photoId)
    }

    @Query("UPDATE photo_fish SET isPrimary = 0 WHERE fishId = :fishId")
    suspend fun clearPrimaryPhotoForFish(fishId: String)

    @Query("UPDATE photo_fish SET isPrimary = 1 WHERE fishId = :fishId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForFish(fishId: String, photoId: String)

    @Transaction
    suspend fun setPrimaryPhotoForFisherman(fishermanId: String, photoId: String) {
        clearPrimaryPhotoForFisherman(fishermanId)
        setPhotoAsPrimaryForFisherman(fishermanId, photoId)
    }

    @Query("UPDATE photo_fisherman SET isPrimary = 0 WHERE fishermanId = :fishermanId")
    suspend fun clearPrimaryPhotoForFisherman(fishermanId: String)

    @Query("UPDATE photo_fisherman SET isPrimary = 1 WHERE fishermanId = :fishermanId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForFisherman(fishermanId: String, photoId: String)

    @Transaction
    suspend fun setPrimaryPhotoForLure(lureId: String, photoId: String) {
        clearPrimaryPhotoForLure(lureId)
        setPhotoAsPrimaryForLure(lureId, photoId)
    }

    @Query("UPDATE photo_lure SET isPrimary = 0 WHERE lureId = :lureId")
    suspend fun clearPrimaryPhotoForLure(lureId: String)

    @Query("UPDATE photo_lure SET isPrimary = 1 WHERE lureId = :lureId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForLure(lureId: String, photoId: String)

    @Transaction
    suspend fun setPrimaryPhotoForTrip(tripId: String, photoId: String) {
        clearPrimaryPhotoForTrip(tripId)
        setPhotoAsPrimaryForTrip(tripId, photoId)
    }

    @Query("UPDATE photo_trip SET isPrimary = 0 WHERE tripId = :tripId")
    suspend fun clearPrimaryPhotoForTrip(tripId: String)

    @Query("UPDATE photo_trip SET isPrimary = 1 WHERE tripId = :tripId AND photoId = :photoId")
    suspend fun setPhotoAsPrimaryForTrip(tripId: String, photoId: String)

    @Query(
        """
        SELECT * FROM photo_table 
        WHERE NOT EXISTS (SELECT 1 FROM photo_bait WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_body_of_water WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_event WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_fish WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_fisherman WHERE photoId = photo_table.id)
          And NOT EXISTS (SELECT 1 FROM photo_lure WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_sky_condition WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_species WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_trip WHERE photoId = photo_table.id)
          AND NOT EXISTS (SELECT 1 FROM photo_water_clarity WHERE photoId = photo_table.id)
    """
    )
    suspend fun getOrphanedPhotos(): List<PhotoEntity>

    @Query(
        """
    DELETE FROM photo_table 
    WHERE NOT EXISTS (SELECT 1 FROM photo_bait WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_body_of_water WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_event WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_fish WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_fisherman WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_lure WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_sky_condition WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_species WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_trip WHERE photoId = photo_table.id)
      AND NOT EXISTS (SELECT 1 FROM photo_water_clarity WHERE photoId = photo_table.id)
"""
    )
    suspend fun deleteOrphanedPhotos(): Int
}