package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.database.LureEntity
import com.funjim.fishstory.database.LureEntityWithColors
import com.funjim.fishstory.database.TackleBoxEntity
import com.funjim.fishstory.database.TackleBoxLureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TackleBoxDao {
    @Query("SELECT * FROM tackle_box_table")
    fun getAllTackleBoxes(): Flow<List<TackleBoxEntity>>

    @Query("DELETE FROM tackle_box_table")
    suspend fun deleteAllTackleBoxes()

    @Query("SELECT * FROM tackle_box_lure_cross_ref")
    fun getAllTackleBoxLureCrossRefs(): Flow<List<TackleBoxLureEntity>>

    @Query("DELETE FROM tackle_box_lure_cross_ref")
    suspend fun deleteAllTackleBoxLureCrossRefs()

    // TODO -- need to rework this logic
    @Query("SELECT * FROM tackle_box_table WHERE fishermanId = :fishermanId LIMIT 1")
    suspend fun getExistingTackleBoxForFisherman(fishermanId: String): TackleBoxEntity?

    @Query("SELECT * FROM tackle_box_table WHERE name = :name AND fishermanId = :fishermanId LIMIT 1")
    suspend fun getExistingTackleBoxForFishermanByName(name: String, fishermanId: String): TackleBoxEntity?

    @Query("SELECT * FROM tackle_box_table WHERE id = :id")
    fun getTackleBoxById(id: String): Flow<TackleBoxEntity?>

    @Query("SELECT * FROM tackle_box_table WHERE fishermanId = :fishermanId")
    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBoxEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTackleBox(tackleBox: TackleBoxEntity)

    // TODO - rename these getOrCreate functions
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

    @Delete
    suspend fun deleteTackleBox(tackleBox: TackleBoxEntity)

    @Upsert
    suspend fun upsertTackleBox(tackleBox: TackleBoxEntity)

    @Update
    suspend fun updateTackleBox(tackleBox: TackleBoxEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertLureToTackleBox(crossRef: TackleBoxLureEntity)

    @Delete
    suspend fun removeLureFromTackleBox(crossRef: TackleBoxLureEntity)

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