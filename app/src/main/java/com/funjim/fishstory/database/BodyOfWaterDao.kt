package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyOfWaterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyOfWater(bodyOfWater: BodyOfWaterEntity)

    @Upsert
    suspend fun upsertBodyOfWater(bodyOfWater: BodyOfWaterEntity)

    @Query("SELECT * FROM body_of_water_table ORDER BY name ASC")
    fun getAllBodiesOfWater(): Flow<List<BodyOfWaterEntity>>

    @Query("SELECT * FROM body_of_water_table WHERE id = :id")
    fun getBodyOfWater (id: String): Flow<BodyOfWaterEntity?>

    @Delete
    suspend fun deleteBodyOfWater(bodyOfWater: BodyOfWaterEntity)

    @Query("DELETE FROM body_of_water_table")
    suspend fun deleteAllBodiesOfWater()

    @Query("SELECT * FROM trip_body_of_water")
    fun getAllTripBodiesOfWater(): Flow<List<TripBodyOfWaterEntity>>

    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table
        INNER JOIN trip_body_of_water ON body_of_water_table.id = trip_body_of_water.bodyOfWaterId
        WHERE trip_body_of_water.tripId = :tripId
        GROUP BY body_of_water_table.id
        """)
    fun getTripBodiesOfWater(tripId: String): Flow<List<BodyOfWaterEntity>>

    @Upsert
    suspend fun insertTripBodyOfWater(crossRef: TripBodyOfWaterEntity)

    @Query("DELETE FROM trip_body_of_water WHERE tripId = :tripId AND bodyOfWaterId = :bodyOfWaterId")
    suspend fun deleteTripBodyOfWater(tripId: String, bodyOfWaterId: String)

    @Query("DELETE FROM trip_body_of_water")
    suspend fun deleteAllTripBodiesOfWater()

    @Query("SELECT * FROM event_body_of_water")
    fun getAllEventBodiesOfWater(): Flow<List<EventBodyOfWaterEntity>>

    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table
        INNER JOIN event_body_of_water ON body_of_water_table.id = event_body_of_water.bodyOfWaterId
        WHERE event_body_of_water.eventId = :eventId
        GROUP BY body_of_water_table.id
        """)
    fun getEventBodiesOfWater(eventId: String): Flow<List<BodyOfWaterEntity>>

    @Upsert
    suspend fun insertEventBodyOfWater(crossRef: EventBodyOfWaterEntity)

    @Upsert
    suspend fun insertBodyOfWaterForEvents(crossRefs: List<EventBodyOfWaterEntity>)

    @Query("DELETE FROM event_body_of_water WHERE eventId = :eventId AND bodyOfWaterId = :bodyOfWaterId")
    suspend fun deleteEventBodyOfWater(eventId: String, bodyOfWaterId: String)

    @Query("DELETE FROM event_body_of_water WHERE eventId IN (:eventIds) AND bodyOfWaterId = :bodyOfWaterId")
    suspend fun deleteBodyOfWaterForEvents(eventIds: List<String>, bodyOfWaterId: String)

    @Query("DELETE FROM event_body_of_water")
    suspend fun deleteAllEventBodiesOfWater()

    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table 
        INNER JOIN fish_table ON body_of_water_table.id = fish_table.bodyOfWaterId  
        WHERE (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:speciesId IS NULL OR fish_table.speciesId = :speciesId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY body_of_water_table.id
    """)
    fun getBodiesOfWaterWithFish(
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        speciesId: String? = null,
        tripId: String? = null
    ): Flow<List<BodyOfWaterEntity>>
}