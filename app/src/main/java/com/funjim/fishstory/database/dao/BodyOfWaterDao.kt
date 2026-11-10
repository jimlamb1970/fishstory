package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.BodyOfWaterEntity
import com.funjim.fishstory.database.entity.EventBodyOfWaterEntity
import com.funjim.fishstory.database.entity.TripBodyOfWaterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyOfWaterDao {
    // CRUD operations
    @Delete
    suspend fun deleteBodyOfWater(bodyOfWater: BodyOfWaterEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertBodyOfWater(bodyOfWater: BodyOfWaterEntity)
    @Update
    suspend fun updateBodyOfWater(bodyOfWater: BodyOfWaterEntity)
    @Query("DELETE FROM body_of_water_table")
    suspend fun deleteAllBodiesOfWater()

    // CRUD operations for Trip Cross-References
    @Query("DELETE FROM trip_body_of_water WHERE tripId = :tripId AND bodyOfWaterId = :bodyOfWaterId")
    suspend fun deleteBodyOfWaterForTrip(tripId: String, bodyOfWaterId: String)
    @Upsert
    suspend fun upsertBodyOfWaterForTrip(crossRef: TripBodyOfWaterEntity)
    @Query("DELETE FROM trip_body_of_water")
    suspend fun deleteAllTripBodiesOfWater()

    // CRUD operations for Event Cross-References
    @Query("DELETE FROM event_body_of_water WHERE eventId = :eventId AND bodyOfWaterId = :bodyOfWaterId")
    suspend fun deleteBodyOfWaterForEvent(eventId: String, bodyOfWaterId: String)
    @Query("DELETE FROM event_body_of_water WHERE eventId IN (:eventIds) AND bodyOfWaterId = :bodyOfWaterId")
    suspend fun deleteBodyOfWaterForEvents(eventIds: List<String>, bodyOfWaterId: String)
    @Upsert
    suspend fun upsertBodyOfWaterForEvent(crossRef: EventBodyOfWaterEntity)
    @Upsert
    suspend fun upsertBodyOfWaterForEvents(crossRefs: List<EventBodyOfWaterEntity>)
    @Query("DELETE FROM event_body_of_water")
    suspend fun deleteAllEventBodiesOfWater()

    // Single object queries
    @Query("SELECT * FROM body_of_water_table WHERE id = :id")
    fun getBodyOfWater (id: String): Flow<BodyOfWaterEntity?>
    @Query("SELECT * FROM body_of_water_table WHERE id = :id LIMIT 1")
    suspend fun getBodyOfWaterById(id: String): BodyOfWaterEntity?
    @Query("SELECT * FROM body_of_water_table WHERE name = :name LIMIT 1")
    suspend fun getBodyOfWaterByName(name: String): BodyOfWaterEntity?

    // Multiple object queries
    @Query("SELECT * FROM body_of_water_table ORDER BY name ASC")
    fun getAllBodiesOfWater(): Flow<List<BodyOfWaterEntity>>
    @Query("SELECT * FROM body_of_water_table ORDER BY name ASC")
    suspend fun getAllBodiesOfWaterList(): List<BodyOfWaterEntity>
    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table
        INNER JOIN trip_body_of_water ON body_of_water_table.id = trip_body_of_water.bodyOfWaterId
        WHERE trip_body_of_water.tripId = :tripId
        GROUP BY body_of_water_table.id
        ORDER BY name ASC
        """)
    fun getBodiesOfWaterForTrip(tripId: String): Flow<List<BodyOfWaterEntity>>
    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table
        INNER JOIN event_body_of_water ON body_of_water_table.id = event_body_of_water.bodyOfWaterId
        WHERE event_body_of_water.eventId = :eventId
        UNION
        SELECT body_of_water_table.* FROM body_of_water_table 
        INNER JOIN fish_table ON body_of_water_table.id = fish_table.bodyOfWaterId  
        WHERE fish_table.eventId = :eventId
        ORDER BY name ASC
    """)
    suspend fun getAllBodiesOfWaterForEvent(eventId: String): List<BodyOfWaterEntity>
    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table
        INNER JOIN event_body_of_water ON body_of_water_table.id = event_body_of_water.bodyOfWaterId
        WHERE event_body_of_water.eventId = :eventId
        ORDER BY name ASC
        """)
    fun getBodiesOfWaterForEvent(eventId: String): Flow<List<BodyOfWaterEntity>>

    // Multiple object Trip Cross-References queries
    @Query("SELECT * FROM trip_body_of_water")
    fun getAllTripBodiesOfWater(): Flow<List<TripBodyOfWaterEntity>>
    @Query("SELECT * FROM trip_body_of_water")
    suspend fun getAllTripBodiesOfWaterList(): List<TripBodyOfWaterEntity>

    // Multiple object Event Cross-References queries
    @Query("SELECT * FROM event_body_of_water")
    fun getAllEventBodiesOfWater(): Flow<List<EventBodyOfWaterEntity>>
    @Query("SELECT * FROM event_body_of_water")
    suspend fun getAllEventBodiesOfWaterList(): List<EventBodyOfWaterEntity>
    @Query("""
        SELECT event_body_of_water.* FROM event_body_of_water WHERE eventId = :eventId
    """)
    suspend fun getEventBodiesOfWaterForEvent(eventId: String): List<EventBodyOfWaterEntity>

    // Specialized queries
    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table 
        INNER JOIN fish_table ON body_of_water_table.id = fish_table.bodyOfWaterId  
        WHERE (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY body_of_water_table.id
        ORDER BY name ASC
    """)
    fun getBodiesOfWaterWithFish(
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<List<BodyOfWaterEntity>>
}