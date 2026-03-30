package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripFishermanCrossRef
import com.funjim.fishstory.model.TripWithDetails
import com.funjim.fishstory.model.TripWithFishermen
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trip_table ORDER BY startDate DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithFishermen(tripId: String): Flow<TripWithFishermen?>

    @Transaction
    @Query("SELECT * FROM trip_table WHERE id = :tripId")
    fun getTripWithDetails(tripId: String): Flow<TripWithDetails?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: TripFishermanCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: TripFishermanCrossRef)
}
