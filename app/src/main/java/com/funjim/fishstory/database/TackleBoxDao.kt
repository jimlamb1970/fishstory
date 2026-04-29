package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLureCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TackleBoxDao {
    @Query("SELECT * FROM tackle_box_table")
    fun getAllTackleBoxes(): Flow<List<TackleBox>>

    @Query("DELETE FROM tackle_box_table")
    suspend fun deleteAllTackleBoxes()

    @Query("SELECT * FROM tackle_box_lure_cross_ref")
    fun getAllTackleBoxLureCrossRefs(): Flow<List<TackleBoxLureCrossRef>>

    @Query("DELETE FROM tackle_box_lure_cross_ref")
    suspend fun deleteAllTackleBoxLureCrossRefs()

    // TODO -- need to rework this logic
    @Query("SELECT * FROM tackle_box_table WHERE fishermanId = :fishermanId LIMIT 1")
    suspend fun getExistingTackleBoxForFisherman(fishermanId: String): TackleBox?

    @Query("SELECT * FROM tackle_box_table WHERE name = :name AND fishermanId = :fishermanId LIMIT 1")
    suspend fun getExistingTackleBoxForFishermanByName(name: String, fishermanId: String): TackleBox?

    @Query("SELECT * FROM tackle_box_table WHERE id = :id")
    fun getTackleBoxById(id: String): Flow<TackleBox?>

    @Query("SELECT * FROM tackle_box_table WHERE fishermanId = :fishermanId")
    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTackleBox(tackleBox: TackleBox)

    // TODO - rename these getOrCreate functions
    @Transaction
    suspend fun getOrCreate(fishermanId: String, name: String): String {
        val existingTackleBox = getExistingTackleBoxForFishermanByName(name, fishermanId)
        if (existingTackleBox != null) {
            return existingTackleBox.id
        }

        val newTackleBox = TackleBox(fishermanId = fishermanId, name = name)
        upsertTackleBox(newTackleBox)
        return newTackleBox.id
    }

    @Delete
    suspend fun deleteTackleBox(tackleBox: TackleBox)

    @Upsert
    suspend fun upsertTackleBox(tackleBox: TackleBox)

    @Update
    suspend fun updateTackleBox(tackleBox: TackleBox)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLureToTackleBox(crossRef: TackleBoxLureCrossRef)

    @Delete
    suspend fun removeLureFromTackleBox(crossRef: TackleBoxLureCrossRef)

    @Query("""
        SELECT lure_table.* FROM lure_table
        INNER JOIN tackle_box_lure_cross_ref ON lure_table.id = tackle_box_lure_cross_ref.lureId
        WHERE tackle_box_lure_cross_ref.tackleBoxId = :tackleBoxId
    """)
    fun getLuresInTackleBox(tackleBoxId: String): Flow<List<Lure>>
    
    @Query("""
        SELECT lure_table.* FROM lure_table
        INNER JOIN tackle_box_lure_cross_ref ON lure_table.id = tackle_box_lure_cross_ref.lureId
        INNER JOIN tackle_box_table ON tackle_box_lure_cross_ref.tackleBoxId = tackle_box_table.id
        WHERE tackle_box_table.fishermanId = :fishermanId
    """)
    fun getLuresForFisherman(fishermanId: String): Flow<List<Lure>>
}

// TODO - add query to get lures for tacklebox/fishermen with names built out