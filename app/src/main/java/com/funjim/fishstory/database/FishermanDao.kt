package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanWithDetails
import com.funjim.fishstory.model.FishermanWithTrips
import com.funjim.fishstory.model.TripFishermanCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface FishermanDao {
    @Insert
    suspend fun insert(fisherman: Fisherman): Long // Return the inserted fisherman's ID

    @Update
    suspend fun update(fisherman: Fisherman)

    @Query("SELECT * FROM fisherman_table")
    fun getAllFishermen(): Flow<List<Fisherman>>

    @Delete
    suspend fun deleteFisherman(fisherman: Fisherman)

    @Transaction
    @Query("SELECT * FROM fisherman_table WHERE id = :fishermanId")
    fun getFishermanWithTrips(fishermanId: Int): Flow<FishermanWithTrips>

    @Transaction
    @Query("SELECT * FROM fisherman_table WHERE id = :fishermanId")
    fun getFishermanWithDetails(fishermanId: Int): Flow<FishermanWithDetails>

    @Insert
    suspend fun insertCrossRef(crossRef: TripFishermanCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: com.funjim.fishstory.model.TripFishermanCrossRef)
}
