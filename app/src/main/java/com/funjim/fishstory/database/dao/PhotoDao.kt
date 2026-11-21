package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.BaitPhotoEntity
import com.funjim.fishstory.database.entity.BodyOfWaterPhotoEntity
import com.funjim.fishstory.database.entity.EventPhotoEntity
import com.funjim.fishstory.database.entity.FishPhotoEntity
import com.funjim.fishstory.database.entity.FishermanPhotoEntity
import com.funjim.fishstory.database.entity.LurePhotoEntity
import com.funjim.fishstory.database.entity.SkyConditionPhotoEntity
import com.funjim.fishstory.database.entity.SpeciesPhotoEntity
import com.funjim.fishstory.database.entity.TripPhotoEntity
import com.funjim.fishstory.database.entity.WaterClarityPhotoEntity
import com.funjim.fishstory.model.FishermanPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM bait_photo_table")
    fun getAllBaitPhotos(): Flow<List<BaitPhotoEntity>>
    @Query("SELECT * FROM bait_photo_table")
    suspend fun getAllBaitPhotosList(): List<BaitPhotoEntity>

    @Query("SELECT * FROM body_of_water_photo_table")
    fun getAllBodyOfWaterPhotos(): Flow<List<BodyOfWaterPhotoEntity>>
    @Query("SELECT * FROM body_of_water_photo_table")
    suspend fun getAllBodyOfWaterPhotosList(): List<BodyOfWaterPhotoEntity>

    @Query("SELECT * FROM event_photo_table")
    fun getAllEventPhotos(): Flow<List<EventPhotoEntity>>
    @Query("SELECT * FROM event_photo_table")
    suspend fun getAllEventPhotosList(): List<EventPhotoEntity>

    @Query("SELECT * FROM fish_photo_table")
    fun getAllFishPhotos(): Flow<List<FishPhotoEntity>>
    @Query("SELECT * FROM fish_photo_table")
    suspend fun getAllFishPhotosList(): List<FishPhotoEntity>

    @Query("SELECT * FROM fisherman_photo_table")
    fun getAllFishermanPhotos(): Flow<List<FishermanPhotoEntity>>
    @Query("SELECT * FROM fisherman_photo_table")
    suspend fun getAllFishermanPhotosList(): List<FishermanPhotoEntity>

    @Query("SELECT * FROM lure_photo_table")
    fun getAllLurePhotos(): Flow<List<LurePhotoEntity>>
    @Query("SELECT * FROM lure_photo_table")
    suspend fun getAllLurePhotosList(): List<LurePhotoEntity>

    @Query("SELECT * FROM sky_condition_photo_table")
    suspend fun getAllSkyConditionPhotosList(): List<SkyConditionPhotoEntity>

    @Query("SELECT * FROM species_photo_table")
    fun getAllSpeciesPhotos(): Flow<List<SpeciesPhotoEntity>>
    @Query("SELECT * FROM species_photo_table")
    suspend fun getAllSpeciesPhotosList(): List<SpeciesPhotoEntity>

    @Query("SELECT * FROM trip_photo_table")
    fun getAllTripPhotos(): Flow<List<TripPhotoEntity>>
    @Query("SELECT * FROM trip_photo_table")
    suspend fun getAllTripPhotosList(): List<TripPhotoEntity>

    @Query("SELECT * FROM water_clarity_photo_table")
    suspend fun getAllWaterClarityPhotosList(): List<WaterClarityPhotoEntity>

    @Query("DELETE FROM bait_photo_table")
    suspend fun deleteAllBaitPhotos()
    @Query("DELETE FROM body_of_water_photo_table")
    suspend fun deleteAllBodyOfWaterPhotos()
    @Query("DELETE FROM event_photo_table")
    suspend fun deleteAllEventPhotos()
    @Query("DELETE FROM fish_photo_table")
    suspend fun deleteAllFishPhotos()
    @Query("DELETE FROM fisherman_photo_table")
    suspend fun deleteAllFishermanPhotos()
    @Query("DELETE FROM lure_photo_table")
    suspend fun deleteAllLurePhotos()
    @Query("DELETE FROM sky_condition_photo_table")
    suspend fun deleteAllSkyConditionPhotos()
    @Query("DELETE FROM species_photo_table")
    suspend fun deleteAllSpeciesPhotos()
    @Query("DELETE FROM trip_photo_table")
    suspend fun deleteAllTripPhotos()
    @Query("DELETE FROM water_clarity_photo_table")
    suspend fun deleteAllWaterClarityPhotos()

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertBaitPhoto(photo: BaitPhotoEntity): Long
    @Upsert
    suspend fun upsertBaitPhoto(photo: BaitPhotoEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertBodyOfWaterPhoto(photo: BodyOfWaterPhotoEntity): Long
    @Upsert
    suspend fun upsertBodyOfWaterPhoto(photo: BodyOfWaterPhotoEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEventPhoto(photo: EventPhotoEntity): Long
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertFishPhoto(photo: FishPhotoEntity): Long
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertFishermanPhoto(photo: FishermanPhotoEntity): Long
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertLurePhoto(photo: LurePhotoEntity): Long
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertSkyConditionPhoto(photo: SkyConditionPhotoEntity): Long
    @Upsert
    suspend fun upsertSkyConditionPhoto(photo: SkyConditionPhotoEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertSpeciesPhoto(photo: SpeciesPhotoEntity): Long
    @Upsert
    suspend fun upsertSpeciesPhoto(photo: SpeciesPhotoEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertTripPhoto(photo: TripPhotoEntity): Long
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertWaterClarityPhoto(photo: WaterClarityPhotoEntity): Long
    @Upsert
    suspend fun upsertWaterClarityPhoto(photo: WaterClarityPhotoEntity)


    @Query("SELECT id FROM bait_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getBaitPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM body_of_water_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getBodyOfWaterPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM event_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getEventPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM fish_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getFishPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM fisherman_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getFishermanPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM lure_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getLurePhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM sky_condition_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getSkyConditionPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM species_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getSpeciesPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM trip_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getTripPhotoIdByHash(hashcode: String, parentId: String): String?
    @Query("SELECT id FROM water_clarity_photo_table WHERE hashcode = :hashcode AND parentId = :parentId LIMIT 1")
    suspend fun getWaterClarityPhotoIdByHash(hashcode: String, parentId: String): String?

    @Delete
    suspend fun deleteBaitPhoto(photo: BaitPhotoEntity)
    @Query("DELETE FROM bait_photo_table WHERE id = :id")
    suspend fun deleteBaitPhotoById(id: String)
    @Delete
    suspend fun deleteBodyOfWaterPhoto(photo: BodyOfWaterPhotoEntity)
    @Query("DELETE FROM body_of_water_photo_table WHERE id = :id")
    suspend fun deleteBodyOfWaterPhotoById(id: String)
    @Delete
    suspend fun deleteEventPhoto(photo: EventPhotoEntity)
    @Query("DELETE FROM event_photo_table WHERE id = :id")
    suspend fun deleteEventPhotoById(id: String)
    @Delete
    suspend fun deleteFishPhoto(photo: FishPhotoEntity)
    @Query("DELETE FROM fish_photo_table WHERE id = :id")
    suspend fun deleteFishPhotoById(id: String)
    @Delete
    suspend fun deleteFishermanPhoto(photo: FishermanPhotoEntity)
    @Query("DELETE FROM fisherman_photo_table WHERE id = :id")
    suspend fun deleteFishermanPhotoById(id: String)
    @Delete
    suspend fun deleteLurePhoto(photo: LurePhotoEntity)
    @Query("DELETE FROM lure_photo_table WHERE id = :id")
    suspend fun deleteLurePhotoById(id: String)
    @Delete
    suspend fun deleteSkyConditionPhoto(photo: SkyConditionPhotoEntity)
    @Query("DELETE FROM sky_condition_photo_table WHERE id = :id")
    suspend fun deleteSkyConditionPhotoById(id: String)
    @Delete
    suspend fun deleteSpeciesPhoto(photo: SpeciesPhotoEntity)
    @Query("DELETE FROM species_photo_table WHERE id = :id")
    suspend fun deleteSpeciesPhotoById(id: String)
    @Delete
    suspend fun deleteTripPhoto(photo: TripPhotoEntity)
    @Query("DELETE FROM trip_photo_table WHERE id = :id")
    suspend fun deleteTripPhotoById(id: String)
    @Delete
    suspend fun deleteWaterClarityPhoto(photo: WaterClarityPhotoEntity)
    @Query("DELETE FROM water_clarity_photo_table WHERE id = :id")
    suspend fun deleteWaterClarityPhotoById(id: String)

    @Query("""
        SELECT bait_photo_table.* FROM bait_photo_table
        WHERE parentId = :id
        LIMIT 1
    """)
    suspend fun getBaitPhoto(id: String): BaitPhotoEntity?
    @Query(
        """
        SELECT body_of_water_photo_table.* FROM body_of_water_photo_table
        WHERE parentId = :id
        LIMIT 1
    """)
    suspend fun getBodyOfWaterPhoto(id: String): BodyOfWaterPhotoEntity?
    @Query("""
        SELECT sky_condition_photo_table.* FROM sky_condition_photo_table
        WHERE parentId = :id
        LIMIT 1
    """)
    suspend fun getSkyConditionPhoto(id: String): SkyConditionPhotoEntity?
    @Query("""
        SELECT species_photo_table.* FROM species_photo_table
        WHERE parentId = :id
        LIMIT 1
    """)
    suspend fun getSpeciesPhoto(id: String): SpeciesPhotoEntity?
    @Query("""
        SELECT water_clarity_photo_table.* FROM water_clarity_photo_table
        WHERE parentId = :id
        LIMIT 1
    """)
    suspend fun getWaterClarityPhoto(id: String): WaterClarityPhotoEntity?

    @Query("""
        SELECT event_photo_table.* FROM event_photo_table
        WHERE parentId = :eventId
    """)
    fun getEventPhotos(eventId: String): Flow<List<EventPhotoEntity>>
    @Query("""
        SELECT fish_photo_table.* FROM fish_photo_table
        WHERE parentId = :fishId
    """)
    fun getFishPhotos(fishId: String): Flow<List<FishPhotoEntity>>
    @Query("""
        SELECT fisherman_photo_table.* FROM fisherman_photo_table
        WHERE parentId = :fishermanId
    """)
    fun getFishermanPhotos(fishermanId: String): Flow<List<FishermanPhotoEntity>>
    @Query("""
        SELECT lure_photo_table.*  FROM lure_photo_table
        WHERE parentId = :lureId
    """)
    fun getLurePhotos(lureId: String): Flow<List<LurePhotoEntity>>
    @Query("""
        SELECT trip_photo_table.* FROM trip_photo_table
        WHERE parentId = :tripId
    """)
    fun getTripPhotos(tripId: String): Flow<List<TripPhotoEntity>>

    @Query("""
        SELECT thumbnail FROM bait_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getBaitThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM body_of_water_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getBodyOfWaterThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM event_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getEventThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM fish_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getFishThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM fisherman_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getFishermanThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM lure_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getLureThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM sky_condition_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getSkyConditionThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM species_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getSpeciesThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM trip_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getTripThumbnail(id: String): Flow<ByteArray?>
    @Query("""
        SELECT thumbnail FROM water_clarity_photo_table 
        WHERE parentId = :id 
        ORDER BY isPrimary DESC, timestamp ASC 
        LIMIT 1
    """)
    fun getWaterClarityThumbnail(id: String): Flow<ByteArray?>


    @Transaction
    suspend fun setPrimaryEventPhoto(photo: EventPhotoEntity) {
        clearPrimaryEventPhoto(photo.parentId)
        updatePrimaryEventPhoto(photo.parentId, photo.id)
    }
    @Transaction
    suspend fun setPrimaryFishPhoto(photo: FishPhotoEntity) {
        clearPrimaryFishPhoto(photo.parentId)
        updatePrimaryFishPhoto(photo.parentId, photo.id)
    }
    @Transaction
    suspend fun setPrimaryFishermanPhoto(photo: FishermanPhotoEntity) {
        clearPrimaryFishermanPhoto(photo.parentId)
        updatePrimaryFishermanPhoto(photo.parentId, photo.id)
    }
    @Transaction
    suspend fun setPrimaryLurePhoto(photo: LurePhotoEntity) {
        clearPrimaryLurePhoto(photo.parentId)
        updatePrimaryLurePhoto(photo.parentId, photo.id)
    }
    @Transaction
    suspend fun setPrimaryTripPhoto(photo: TripPhotoEntity) {
        clearPrimaryTripPhoto(photo.parentId)
        updatePrimaryTripPhoto(photo.parentId, photo.id)
    }

    @Query("UPDATE event_photo_table SET isPrimary = 0 WHERE parentId = :eventId")
    suspend fun clearPrimaryEventPhoto(eventId: String)
    @Query("UPDATE fish_photo_table SET isPrimary = 0 WHERE parentId = :fishId")
    suspend fun clearPrimaryFishPhoto(fishId: String)
    @Query("UPDATE fisherman_photo_table SET isPrimary = 0 WHERE parentId = :fishermanId")
    suspend fun clearPrimaryFishermanPhoto(fishermanId: String)
    @Query("UPDATE lure_photo_table SET isPrimary = 0 WHERE parentId = :lureId")
    suspend fun clearPrimaryLurePhoto(lureId: String)
    @Query("UPDATE trip_photo_table SET isPrimary = 0 WHERE parentId = :tripId")
    suspend fun clearPrimaryTripPhoto(tripId: String)

    @Query("UPDATE event_photo_table SET isPrimary = 1 WHERE parentId = :eventId AND id = :photoId")
    suspend fun updatePrimaryEventPhoto(eventId: String, photoId: String)
    @Query("UPDATE fish_photo_table SET isPrimary = 1 WHERE parentId = :fishId AND id = :photoId")
    suspend fun updatePrimaryFishPhoto(fishId: String, photoId: String)
    @Query("UPDATE fisherman_photo_table SET isPrimary = 1 WHERE parentId = :fishermanId AND id = :photoId")
    suspend fun updatePrimaryFishermanPhoto(fishermanId: String, photoId: String)
    @Query("UPDATE lure_photo_table SET isPrimary = 1 WHERE parentId = :lureId AND id = :photoId")
    suspend fun updatePrimaryLurePhoto(lureId: String, photoId: String)
    @Query("UPDATE trip_photo_table SET isPrimary = 1 WHERE parentId = :tripId AND id = :photoId")
    suspend fun updatePrimaryTripPhoto(tripId: String, photoId: String)

    @Query("""
        SELECT SUM(cnt) FROM (
            SELECT COUNT(*) as cnt FROM bait_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM body_of_water_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM event_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM fish_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM fisherman_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM lure_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM sky_condition_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM species_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM trip_photo_table WHERE uri = :uri
            UNION ALL
            SELECT COUNT(*) FROM water_clarity_photo_table WHERE uri = :uri
        )
    """)
    suspend fun countPhotosByUri(uri: String): Int
}