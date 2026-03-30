package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLureCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface TackleBoxDao {
    @Query("SELECT * FROM tackle_box_table WHERE fishermanId = :fishermanId")
    fun getTackleBoxForFisherman(fishermanId: String): Flow<TackleBox?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTackleBox(tackleBox: TackleBox)

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
