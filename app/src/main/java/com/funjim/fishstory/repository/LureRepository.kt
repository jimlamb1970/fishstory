package com.funjim.fishstory.repository

import com.funjim.fishstory.database.BaitDao
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.database.TackleBoxLureEntity
import com.funjim.fishstory.database.toBaitDomainList
import com.funjim.fishstory.database.toDomain
import com.funjim.fishstory.database.toEntity
import com.funjim.fishstory.database.toLureColorDomainList
import com.funjim.fishstory.database.toLureWithColorsDomainList
import com.funjim.fishstory.database.toLureWithColorsSummaryDomainList
import com.funjim.fishstory.model.Bait
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureGlowColor
import com.funjim.fishstory.model.LurePrimaryColor
import com.funjim.fishstory.model.LureSecondaryColor
import com.funjim.fishstory.model.LureWithColorsSummary
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.LureWithDetails
import com.funjim.fishstory.model.TackleBox
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LureRepository(
    private val baitDao: BaitDao,
    private val fishermanDao: FishermanDao,
    private val lureDao: LureDao,
    private val tackleBoxDao: TackleBoxDao
) {
    // Data Streams
    val allLureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()
        .map { list -> list.toLureColorDomainList() }
    val allBaits: Flow<List<Bait>> = baitDao.getAllBaits()
        .map { list -> list.toBaitDomainList() }

    fun getAllLures(): Flow<List<LureWithColors>> {
        return lureDao.getLuresWithColors()
            .map { list -> list.toLureWithColorsDomainList() }
    }

    fun getLureSummariesWithColors(): Flow<List<LureWithColorsSummary>> {
        return lureDao.getLureSummariesWithColors()
            .map { list -> list.toLureWithColorsSummaryDomainList() }
    }

    fun getLureWithColors(id: String): Flow<LureWithColors?> {
        return lureDao.getLureWithColors(id)
            .map { entity -> entity?.toDomain() }
    }

    suspend fun getLureWithDetails(id: String): LureWithDetails? {
        return lureDao.getLureWithDetails(id)?.toDomain()
    }

    suspend fun upsertLurePrimaryColor(crossRef: LurePrimaryColor) {
        lureDao.upsertLurePrimaryColor(crossRef.toEntity())
    }
    suspend fun upsertLureSecondaryColor(crossRef: LureSecondaryColor) {
        lureDao.upsertLureSecondaryColor(crossRef.toEntity())
    }
    suspend fun upsertLureGlowColor(crossRef: LureGlowColor) {
        lureDao.upsertLureGlowColor(crossRef.toEntity())
    }

    // Tackle Box Logic
    fun getLuresInTackleBox(tackleBoxId: String): Flow<List<LureWithColors>> {
        return tackleBoxDao.getLuresInTackleBox(tackleBoxId)
            .map { list -> list.toLureWithColorsDomainList()
        }
    }

    suspend fun addLureToTackleBox(tackleBoxId: String, lureId: String) {
        tackleBoxDao.insertLureToTackleBox(TackleBoxLureEntity(tackleBoxId, lureId))
    }

    suspend fun removeLureFromTackleBox(tackleBoxId: String, lureId: String) {
        tackleBoxDao.removeLureFromTackleBox(TackleBoxLureEntity(tackleBoxId, lureId))
    }

    // Lure Operations
    suspend fun upsertLure(lure: Lure) {
        lureDao.upsertLure(lure.toEntity())
    }
    suspend fun deleteLure(lure: Lure) {
        lureDao.deleteLure(lure.toEntity())
    }
    suspend fun insertLureColor(lureColor: LureColor) {
        lureDao.insertLureColor(lureColor.toEntity())
    }
    suspend fun upsertLureColor(lureColor: LureColor) {
        lureDao.upsertLureColor(lureColor.toEntity())
    }
    suspend fun deleteLureColor(lureColor: LureColor) {
        lureDao.deleteLureColor(lureColor.toEntity())
    }

    suspend fun addBait(bait: Bait) {
        baitDao.insertBait(bait.toEntity())
    }
    suspend fun upsertBait(bait: Bait) {
        baitDao.upsertBait(bait.toEntity())
    }
    suspend fun deleteBait(bait: Bait) {
        baitDao.deleteBait(bait.toEntity())
    }

    suspend fun getFishermanById(id: String) = fishermanDao.getFishermanById(id)
    fun getTackleBoxById(id: String): Flow<TackleBox?> {
        return tackleBoxDao.getTackleBoxById(id).map { entity -> entity?.toDomain() }
    }
    suspend fun updateTackleBox(tackleBox: TackleBox) {
        tackleBoxDao.updateTackleBox(tackleBox.toEntity())
    }
}