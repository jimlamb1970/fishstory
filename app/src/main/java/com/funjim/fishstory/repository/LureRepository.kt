package com.funjim.fishstory.repository

import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLureCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LureRepository(
    private val lureDao: LureDao,
    private val tackleBoxDao: TackleBoxDao,
    private val photoDao: PhotoDao,
    private val fishermanDao: FishermanDao
) {
    // Data Streams
    val allLures: Flow<List<Lure>> = lureDao.getAllLures()
    val allLureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()

    val lurePhotos: Flow<Map<String, List<Photo>>> = photoDao.getAllLurePhotos()
        .map { photos ->
            photos.filter { it.lureId != null }.groupBy { it.lureId!! }
        }

    suspend fun getLureById(id: String): Lure? = lureDao.getLureById(id)

    // Tackle Box Logic
    fun getLuresInTackleBox(tackleBoxId: String): Flow<List<Lure>> {
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
    suspend fun insertLure(lure: Lure) = lureDao.insertLure(lure)
    suspend fun deleteLure(lure: Lure) = lureDao.deleteLure(lure)

    suspend fun insertLureColor(lureColor: LureColor) = lureDao.insertLureColor(lureColor)
    suspend fun upsertLureColor(lureColor: LureColor) = lureDao.upsertLureColor(lureColor)
    suspend fun deleteLureColor(lureColor: LureColor) = lureDao.deleteLureColor(lureColor)

    suspend fun getFishermanById(id: String) = fishermanDao.getFishermanById(id)
    fun getTackleBoxById(id: String) = tackleBoxDao.getTackleBoxById(id)

    suspend fun updateTackleBox(tackleBox: TackleBox) = tackleBoxDao.updateTackleBox(tackleBox)
}