package com.funjim.fishstory.repository

import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureGlowColorCrossRef
import com.funjim.fishstory.model.LurePrimaryColorCrossRef
import com.funjim.fishstory.model.LureSecondaryColorCrossRef
import com.funjim.fishstory.model.LureSummaryWithColors
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.LureWithDetails
import com.funjim.fishstory.model.LureWithPhotos
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLureCrossRef
import kotlinx.coroutines.flow.Flow

class LureRepository(
    private val lureDao: LureDao,
    private val tackleBoxDao: TackleBoxDao,
    private val photoDao: PhotoDao,
    private val fishermanDao: FishermanDao
) {
    // Data Streams
    val allLureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()

    fun getAllLures(): Flow<List<LureWithColors>> {
        return lureDao.getLuresWithColors()
    }

    fun getLureSummariesWithColors(): Flow<List<LureSummaryWithColors>> {
        return lureDao.getLureSummariesWithColors()
    }

    suspend fun getLureWithPhotos(id: String): LureWithPhotos? = lureDao.getLureWithPhotos(id)

    fun getLureWithColors(id: String): Flow<LureWithColors?> = lureDao.getLureWithColors(id)

    suspend fun getLureWithDetails(id: String): LureWithDetails? = lureDao.getLureWithDetails(id)

    suspend fun upsertLurePrimaryColorCrossRef(crossRef: LurePrimaryColorCrossRef) {
        lureDao.upsertLurePrimaryColorCrossRef(crossRef)
    }
    suspend fun upsertLureSecondaryColorCrossRef(crossRef: LureSecondaryColorCrossRef) {
        lureDao.upsertLureSecondaryColorCrossRef(crossRef)
    }
    suspend fun upsertLureGlowColorCrossRef(crossRef: LureGlowColorCrossRef) {
        lureDao.upsertLureGlowColorCrossRef(crossRef)
    }

    // Tackle Box Logic
    fun getLuresInTackleBox(tackleBoxId: String): Flow<List<LureWithColors>> {
        return tackleBoxDao.getLuresInTackleBox(tackleBoxId)
    }

    suspend fun getOrCreateTackleBox(fishermanId: String): TackleBox {
        return tackleBoxDao.getExistingTackleBoxForFisherman(fishermanId)
            ?: TackleBox(fishermanId = fishermanId).also {
                tackleBoxDao.insertTackleBox(it)
            }
    }

    suspend fun addLureToTackleBox(tackleBoxId: String, lureId: String) {
        tackleBoxDao.insertLureToTackleBox(TackleBoxLureCrossRef(tackleBoxId, lureId))
    }

    suspend fun removeLureFromTackleBox(tackleBoxId: String, lureId: String) {
        tackleBoxDao.removeLureFromTackleBox(TackleBoxLureCrossRef(tackleBoxId, lureId))
    }

    // Lure Operations
    suspend fun upsertLure(lure: Lure) = lureDao.upsertLure(lure)
    suspend fun deleteLure(lure: Lure) = lureDao.deleteLure(lure)

    suspend fun insertLureColor(lureColor: LureColor) = lureDao.insertLureColor(lureColor)
    suspend fun upsertLureColor(lureColor: LureColor) = lureDao.upsertLureColor(lureColor)
    suspend fun deleteLureColor(lureColor: LureColor) = lureDao.deleteLureColor(lureColor)

    suspend fun getFishermanById(id: String) = fishermanDao.getFishermanById(id)
    fun getTackleBoxById(id: String) = tackleBoxDao.getTackleBoxById(id)

    suspend fun updateTackleBox(tackleBox: TackleBox) = tackleBoxDao.updateTackleBox(tackleBox)
}