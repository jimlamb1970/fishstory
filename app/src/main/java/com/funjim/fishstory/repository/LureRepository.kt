package com.funjim.fishstory.repository

import androidx.room.Transaction
import com.funjim.fishstory.database.dao.BaitDao
import com.funjim.fishstory.database.dao.FishermanDao
import com.funjim.fishstory.database.dao.LureDao
import com.funjim.fishstory.database.dao.TackleBoxDao
import com.funjim.fishstory.database.entity.EventBaitEntity
import com.funjim.fishstory.database.entity.TackleBoxLureEntity
import com.funjim.fishstory.database.mapper.toBaitDomainList
import com.funjim.fishstory.database.mapper.toDomain
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toEventBaitDomainList
import com.funjim.fishstory.database.mapper.toLureColorDomainList
import com.funjim.fishstory.database.mapper.toLureDomainList
import com.funjim.fishstory.database.mapper.toLureGlowColorDomainList
import com.funjim.fishstory.database.mapper.toLurePrimaryColorDomainList
import com.funjim.fishstory.database.mapper.toLureSecondaryColorDomainList
import com.funjim.fishstory.database.mapper.toLureWithColorsDomainList
import com.funjim.fishstory.database.mapper.toLureWithColorsSummaryDomainList
import com.funjim.fishstory.model.Bait
import com.funjim.fishstory.model.EventBait
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.InsertResult
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
import java.util.UUID

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
        return lureDao.getAllLuresWithColors()
            .map { list -> list.toLureWithColorsDomainList() }
    }
    fun getAllLuresForEvent(eventId: String): Flow<List<Lure>> {
        return lureDao.getAllLuresForEvent(eventId)
            .map { list -> list.toLureDomainList() }
    }
    fun getAllLureColorsForEvent(eventId: String): Flow<List<LureColor>> {
        return lureDao.getAllLureColorsForEvent(eventId)
            .map { list -> list.toLureColorDomainList() }
    }
    fun getLurePrimaryColorsForEvent(eventId: String): Flow<List<LurePrimaryColor>> {
        return lureDao.getLurePrimaryColorsForEvent(eventId)
            .map { list -> list.toLurePrimaryColorDomainList() }
    }
    fun getLureSecondaryColorsForEvent(eventId: String): Flow<List<LureSecondaryColor>> {
        return lureDao.getLureSecondaryColorsForEvent(eventId)
            .map { list -> list.toLureSecondaryColorDomainList() }
    }
    fun getLureGlowColorsForEvent(eventId: String): Flow<List<LureGlowColor>> {
        return lureDao.getLureGlowColorsForEvent(eventId)
            .map { list -> list.toLureGlowColorDomainList() }
    }
    suspend fun findOrCreateLureColor(item: LureColor): InsertResult<LureColor> {
        // Check for existing ID and equality
        val existingId = lureDao.getLureColorById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = lureDao.getLureColorByData(
                name = item.name,
                hexCode = item.hexCode
            )
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newItem = item.copy(id = UUID.randomUUID().toString())
                    lureDao.upsertLureColor(newItem.toEntity())
                    InsertResult(newItem, true)
                } else {
                    lureDao.upsertLureColor(item.toEntity())
                    InsertResult(item, true)
                }
            }
        }
    }
    suspend fun findOrCreateLure(
        item: Lure,
        primaryColors: List<String>,
        secondaryColors: List<String>,
        glowColors: List<String>): InsertResult<Lure> {
        val sortedPrimaryColors = primaryColors.sorted().joinToString(",")
        val sortedSecondaryColors = secondaryColors.sorted().joinToString(",")
        val sortedGlowColors = glowColors.sorted().joinToString(",")

        // Check for existing ID and equality
        val existingId = lureDao.getLureWithColorsById(item.id)
        return if ((existingId != null) &&
            (existingId.toDomain().equals(
                item,
                sortedPrimaryColors,
                sortedSecondaryColors,
                sortedGlowColors))
        ) {
            InsertResult(item, false)
        } else {
            // Check for existing data and equality
            val existingData = lureDao.getLureByData(
                item.name,
                item.hookCount,
                item.glows,
                sortedPrimaryColors,
                sortedSecondaryColors,
                sortedGlowColors
            )
            // Unlike other 'findOrCreate' functions, this one is not verifying that the items
            // are equal.  In this case, relying entirely on the DAO to check for equality
            if (existingData != null) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newItem = item.copy(id = UUID.randomUUID().toString())
                    lureDao.upsertLure(newItem.toEntity())
                    primaryColors.forEach { primaryColor ->
                        upsertLurePrimaryColor(
                            LurePrimaryColor(newItem.id, primaryColor)
                        )
                    }
                    secondaryColors.forEach { secondaryColor ->
                        upsertLureSecondaryColor(
                            LureSecondaryColor(newItem.id, secondaryColor)
                        )
                    }
                    glowColors.forEach { glowColor ->
                        upsertLureGlowColor(
                            LureGlowColor(newItem.id, glowColor)
                        )
                    }
                    InsertResult(newItem, true)
                } else {
                    lureDao.upsertLure(item.toEntity())
                    primaryColors.forEach { primaryColor ->
                        upsertLurePrimaryColor(
                            LurePrimaryColor(item.id, primaryColor)
                        )
                    }
                    secondaryColors.forEach { secondaryColor ->
                        upsertLureSecondaryColor(
                            LureSecondaryColor(item.id, secondaryColor)
                        )
                    }
                    glowColors.forEach { glowColor ->
                        upsertLureGlowColor(
                            LureGlowColor(item.id, glowColor)
                        )
                    }
                    InsertResult(item, true)
                }
            }
        }
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

    @Transaction
    suspend fun deleteLureColors(lureId: String) {
        lureDao.deleteLurePrimaryColors(lureId)
        lureDao.deleteLureSecondaryColors(lureId)
        lureDao.deleteLureGlowColors(lureId)
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
        tackleBoxDao.upsertTackleBoxLure(
            TackleBoxLureEntity(
                tackleBoxId = tackleBoxId,
                lureId =lureId)
        )
    }
    suspend fun removeLureFromTackleBox(tackleBoxId: String, lureId: String) {
        tackleBoxDao.deleteTackleBoxLure(
            TackleBoxLureEntity(
                tackleBoxId = tackleBoxId,
                lureId =lureId)
        )
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
    suspend fun updateLureColor(lureColor: LureColor) {
        lureDao.upsertLureColor(lureColor.toEntity())
    }
    suspend fun deleteLureColor(lureColor: LureColor) {
        lureDao.deleteLureColor(lureColor.toEntity())
    }

    // Bait Operations
    suspend fun addBait(bait: Bait) {
        baitDao.insertBait(bait.toEntity())
    }
    suspend fun deleteBait(bait: Bait) {
        baitDao.deleteBait(bait.toEntity())
    }
    suspend fun updateBait(bait: Bait) {
        baitDao.upsertBait(bait.toEntity())
    }

    suspend fun addBaitToEvent(baitId: String, eventId: String) {
        baitDao.upsertBaitForEvent(EventBaitEntity(eventId, baitId))
    }
    suspend fun removeBaitFromEvent(baitId: String, eventId: String) {
        baitDao.deleteBaitForEvent(eventId, baitId)
    }

    fun getAllBaitsForEvent(eventId: String): Flow<List<Bait>> {
        return baitDao.getAllBaitsForEvent(eventId).map { list -> list.toBaitDomainList() }
    }
    fun getEventBaitsForEvent(eventId: String): Flow<List<EventBait>> {
        return baitDao.getEventsBaitsForEvent(eventId).map { list -> list.toEventBaitDomainList() }
    }

    suspend fun findOrCreateBait(item: Bait): InsertResult<Bait> {
        // Check for existing ID and equality
        val existingId = baitDao.getBaitById(item.id)
        return if ((existingId != null) && (existingId.toDomain() == item)) {
            InsertResult(item, false)
        } else {
            // Check for existing name and equality
            val existingData = baitDao.getBaitByName(item.name)
            if ((existingData != null) && (existingData.toDomain() == item)) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    addBait(newObject)
                    InsertResult(newObject, true)
                } else {
                    addBait(item)
                    InsertResult(item, true)
                }
            }
        }
    }

    // TODO move to fisherman repo
    suspend fun getFishermanById(id: String): Fisherman? {
        return fishermanDao.getFishermanById(id)?.toDomain()
    }
    fun getTackleBox(id: String): Flow<TackleBox?> {
        return tackleBoxDao.getTackleBox(id).map { entity -> entity?.toDomain() }
    }
    suspend fun updateTackleBox(tackleBox: TackleBox) {
        tackleBoxDao.updateTackleBox(tackleBox.toEntity())
    }
}