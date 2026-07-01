package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.EventBodyOfWater
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripBodyOfWater
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyOfWaterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyOfWater(bodyOfWater: BodyOfWater)

    @Query("SELECT * FROM body_of_water_table")
    fun getAllBodiesOfWater(): Flow<List<BodyOfWater>>

    @Query("SELECT * FROM body_of_water_table WHERE id = :id")
    fun getBodyOfWater (id: String): Flow<BodyOfWater?>

    @Delete
    suspend fun deleteBodyOfWater(bodyOfWater: BodyOfWater)

    @Query("DELETE FROM body_of_water_table")
    suspend fun deleteAllBodiesOfWater()

    @Query("SELECT * FROM trip_body_of_water")
    fun getAllTripBodiesOfWater(): Flow<List<TripBodyOfWater>>

    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table
        INNER JOIN trip_body_of_water ON body_of_water_table.id = trip_body_of_water.bodyOfWaterId
        WHERE trip_body_of_water.tripId = :tripId
        GROUP BY body_of_water_table.id
        """)
    fun getTripBodiesOfWater(tripId: String): Flow<List<BodyOfWater>>

    @Upsert
    suspend fun insertTripBodyOfWater(crossRef: TripBodyOfWater)

    @Query("DELETE FROM trip_body_of_water WHERE tripId = :tripId AND bodyOfWaterId = :bodyOfWaterId")
    suspend fun deleteTripBodyOfWater(tripId: String, bodyOfWaterId: String)

    @Query("DELETE FROM trip_body_of_water")
    suspend fun deleteAllTripBodiesOfWater()

    @Query("SELECT * FROM event_body_of_water")
    fun getAllEventBodiesOfWater(): Flow<List<EventBodyOfWater>>

    @Query("""
        SELECT body_of_water_table.* FROM body_of_water_table
        INNER JOIN event_body_of_water ON body_of_water_table.id = event_body_of_water.bodyOfWaterId
        WHERE event_body_of_water.eventId = :eventId
        GROUP BY body_of_water_table.id
        """)
    fun getEventBodiesOfWater(eventId: String): Flow<List<BodyOfWater>>

    @Upsert
    suspend fun insertEventBodyOfWater(crossRef: EventBodyOfWater)

    @Upsert
    suspend fun insertBodyOfWaterForEvents(crossRefs: List<EventBodyOfWater>)

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
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY body_of_water_table.id
    """)
    fun getBodiesOfWaterWithFish(
        eventId: String?,
        fishermanId: String?,
        lureId: String?,
        tripId: String?): Flow<List<BodyOfWater>>

}