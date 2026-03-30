package com.funjim.fishstory.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanWithDetails
import com.funjim.fishstory.model.FishermanWithTrips
import com.funjim.fishstory.model.TripFishermanCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface FishermanDao {
    @Query("SELECT * FROM fisherman_table")
    fun getAllFishermen(): Flow<List<Fisherman>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fisherman: Fisherman)

    @Upsert
    suspend fun upsert(fisherman: Fisherman)

    @Update
    suspend fun update(fisherman: Fisherman)

    @Delete
    suspend fun deleteFisherman(fisherman: Fisherman)

    @Query("SELECT * FROM fisherman_table WHERE firstName = :first AND lastName = :last AND nickname = :nick LIMIT 1")
    suspend fun getFishermanByName(first: String, last: String, nick: String): Fisherman?

    @Transaction
    @Query("SELECT * FROM fisherman_table WHERE id = :fishermanId")
    fun getFishermanWithTrips(fishermanId: String): Flow<FishermanWithTrips?>

    @Transaction
    @Query("SELECT * FROM fisherman_table WHERE id = :fishermanId")
    fun getFishermanWithDetails(fishermanId: String): Flow<FishermanWithDetails?>

    @Insert
    suspend fun insertCrossRef(crossRef: TripFishermanCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: com.funjim.fishstory.model.TripFishermanCrossRef)
}
