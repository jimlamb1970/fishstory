package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.LureEntity
import com.funjim.fishstory.database.entity.LureEntityWithColors
import com.funjim.fishstory.database.entity.TackleBoxEntity
import com.funjim.fishstory.database.entity.TackleBoxLureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TackleBoxDao {
    // CRUD operations
    @Delete
    suspend fun deleteTackleBox(tackleBox: TackleBoxEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTackleBox(tackleBox: TackleBoxEntity)
    @Update
    suspend fun updateTackleBox(tackleBox: TackleBoxEntity)
    @Upsert
    suspend fun upsertTackleBox(tackleBox: TackleBoxEntity)
    @Query("DELETE FROM tackle_box_table")
    suspend fun deleteAllTackleBoxes()

    // CRUD operations for TackleBox-Lure cross-references
    @Delete
    suspend fun deleteTackleBoxLure(crossRef: TackleBoxLureEntity)
    @Upsert
    suspend fun upsertTackleBoxLure(crossRef: TackleBoxLureEntity)
    @Query("DELETE FROM tackle_box_lure_cross_ref")
    suspend fun deleteAllTackleBoxLures()

    // Single object queries
    @Query("SELECT * FROM tackle_box_table WHERE id = :id")
    fun getTackleBox(id: String): Flow<TackleBoxEntity?>
    @Query("SELECT * FROM tackle_box_table WHERE id = :id")
    suspend fun getTackleBoxById(id: String): TackleBoxEntity?
    @Query("SELECT * FROM tackle_box_table WHERE name = :name AND fishermanId = :fishermanId LIMIT 1")
    suspend fun getTackleBoxByName(
        name: String,
        fishermanId: String
    ): TackleBoxEntity?

    // Multiple object queries
    @Query("SELECT * FROM tackle_box_table")
    fun getAllTackleBoxes(): Flow<List<TackleBoxEntity>>
    @Query("""
        SELECT tackle_box_table.* FROM tackle_box_table
        INNER JOIN event_fisherman_cross_ref ON tackle_box_table.id = event_fisherman_cross_ref.tackleBoxId
        WHERE event_fisherman_cross_ref.eventId = :eventId
    """)
    fun getAllTackleBoxesForEvent(eventId: String): Flow<List<TackleBoxEntity>>

    @Query("SELECT * FROM tackle_box_lure_cross_ref")
    fun getAllTackleBoxLures(): Flow<List<TackleBoxLureEntity>>
    @Query("""
        SELECT tackle_box_lure_cross_ref.* FROM tackle_box_lure_cross_ref
        INNER JOIN event_fisherman_cross_ref ON tackle_box_lure_cross_ref.tackleBoxId = event_fisherman_cross_ref.tackleBoxId
        WHERE event_fisherman_cross_ref.eventId = :eventId
    """)
    fun getTackleBoxLuresForEvent(eventId: String): Flow<List<TackleBoxLureEntity>>

    // Special Queries
    @Query("SELECT * FROM tackle_box_table WHERE fishermanId = :fishermanId LIMIT 1")
    suspend fun getExistingTackleBoxForFisherman(fishermanId: String): TackleBoxEntity?

    @Query("SELECT * FROM tackle_box_table WHERE name = :name AND fishermanId = :fishermanId LIMIT 1")
    suspend fun getExistingTackleBoxForFishermanByName(name: String, fishermanId: String): TackleBoxEntity?

    @Query("SELECT * FROM tackle_box_table WHERE fishermanId = :fishermanId")
    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBoxEntity>>

    @Transaction
    suspend fun getOrCreate(fishermanId: String, name: String): String {
        val existingTackleBox = getExistingTackleBoxForFishermanByName(name, fishermanId)
        if (existingTackleBox != null) {
            return existingTackleBox.id
        }

        val newTackleBox = TackleBoxEntity(fishermanId = fishermanId, name = name)
        upsertTackleBox(newTackleBox)
        return newTackleBox.id
    }

    @Transaction
    @Query("""
        SELECT lure_table.* FROM lure_table
        INNER JOIN tackle_box_lure_cross_ref ON lure_table.id = tackle_box_lure_cross_ref.lureId
        WHERE tackle_box_lure_cross_ref.tackleBoxId = :tackleBoxId
    """)
    fun getLuresInTackleBox(tackleBoxId: String): Flow<List<LureEntityWithColors>>

    @Query("""
        SELECT lure_table.* FROM lure_table
        INNER JOIN tackle_box_lure_cross_ref ON lure_table.id = tackle_box_lure_cross_ref.lureId
        INNER JOIN tackle_box_table ON tackle_box_lure_cross_ref.tackleBoxId = tackle_box_table.id
        WHERE tackle_box_table.fishermanId = :fishermanId
    """)
    fun getLuresForFisherman(fishermanId: String): Flow<List<LureEntity>>
}