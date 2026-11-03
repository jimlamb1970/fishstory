package com.funjim.fishstory.repository

import com.funjim.fishstory.database.dao.FishermanDao
import com.funjim.fishstory.database.dao.TackleBoxDao
import com.funjim.fishstory.database.entity.TackleBoxEntity
import com.funjim.fishstory.database.mapper.toDomain
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toEventFishermanDomainList
import com.funjim.fishstory.database.mapper.toFishermanDomainList
import com.funjim.fishstory.database.mapper.toLureWithColorsDomainList
import com.funjim.fishstory.database.mapper.toTackleBoxDomainList
import com.funjim.fishstory.database.mapper.toTackleBoxLureDomainList
import com.funjim.fishstory.database.mapper.toTripSummaryDomainList
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.EventFisherman
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.InsertResult
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLure
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class FishermanRepository(
    private val fishermanDao: FishermanDao,
    private val tackleBoxDao: TackleBoxDao
) {
    val allFishermen: Flow<List<Fisherman>> = fishermanDao.getAllFishermen()
        .map { list -> list.toFishermanDomainList() }
    fun getFishermenForTrip(tripId: String): Flow<List<Fisherman>> {
        return fishermanDao.getFishermenForTrip(tripId)
            .map { list -> list.toFishermanDomainList() }
    }

    // The following flows are used for export
    fun getAllFishermenForEvent(eventId: String): Flow<List<Fisherman>> {
        return fishermanDao.getAllFishermenForEvent(eventId)
            .map { list -> list.toFishermanDomainList() }
    }
    fun getAllTackleBoxesForEvent(eventId: String): Flow<List<TackleBox>> {
        return tackleBoxDao.getAllTackleBoxesForEvent(eventId)
            .map { list -> list.toTackleBoxDomainList() }
    }
    fun getEventFishermenForEvent(eventId: String): Flow<List<EventFisherman>> {
        return fishermanDao.getEventFishermenForEvent(eventId)
            .map { list -> list.toEventFishermanDomainList() }
    }
    fun getTackleBoxLuresForEvent(eventId: String): Flow<List<TackleBoxLure>> {
        return tackleBoxDao.getTackleBoxLuresForEvent(eventId)
            .map { list -> list.toTackleBoxLureDomainList() }
    }
    suspend fun findOrCreateFisherman(item: Fisherman): InsertResult<Fisherman> {
        // Check for existing ID and equality
        val existingId = fishermanDao.getFishermanById(item.id)
        return if ((existingId != null) && (existingId.equals(item))) {
            InsertResult(existingId.toDomain(), false)
        } else {
            // Check for existing name and equality
            val existingData = fishermanDao.getFishermanByName(
                first = item.firstName,
                last = item.lastName,
                nickname = item.nickname)
            if ((existingData != null) && (existingData.equals(item))) {
                InsertResult(existingData.toDomain(), false)
            } else {
                // Determine if existing ID be used or if a new one needed
                if (existingId != null) {
                    val newObject = item.copy(id = UUID.randomUUID().toString())
                    addFisherman(newObject)
                    InsertResult(newObject, true)
                } else {
                    addFisherman(item)
                    InsertResult(item, true)
                }
            }
        }
    }

    suspend fun findOrCreateTackleBox(tackleBox: TackleBox): InsertResult<TackleBox> {
        val existing = tackleBoxDao.getTackleBoxById(tackleBox.id)
        return if (existing != null) {
            InsertResult(existing.toDomain(), false)
        } else {
            val existing = tackleBoxDao.getTackleBoxByName(
                name = tackleBox.name,
                fishermanId = tackleBox.fishermanId)
            if (existing != null) {
                InsertResult(existing.toDomain(), false)
            } else {
                tackleBoxDao.insertTackleBox(tackleBox.toEntity())
                InsertResult(tackleBox, true)
            }
        }
    }

    fun getFishermenForEvent(eventId: String): Flow<List<Fisherman>> {
        return fishermanDao.getFishermenForEvent(eventId)
            .map { list -> list.toFishermanDomainList() }
    }

    /**
     * Provides a sorted list of fishermen summaries.
     * Moving this here allows the Dashboard to easily grab the "Top 3" fishermen
     * by simply using .map { it.take(3) } on this flow.
     */
    fun getSortedFishermanSummaries(
        order: FishermanSortOrder,
        reversed: Boolean
    ): Flow<List<FishermanSummary>> {
        return fishermanDao.getFishermanSummaries().map { summaries ->
            val sorted = when (order) {
                FishermanSortOrder.NAME_AZ -> summaries.sortedBy { it.fisherman.fullName.lowercase() }
                FishermanSortOrder.MOST_CATCHES -> summaries.sortedByDescending { it.fishCaught }
                FishermanSortOrder.MOST_KEPT -> summaries.sortedByDescending { it.fishKept }
                FishermanSortOrder.MOST_TRIPS -> summaries.sortedByDescending { it.totalTrips }
            }
            val finalList = if (reversed) sorted.reversed() else sorted

            finalList.map { it.toDomain() }
        }
    }

    fun getFishermanFullStatistics(id: String): Flow<FishermanFullStatistics> =
        fishermanDao.getFishermanFullStatistics(id, System.currentTimeMillis())
            .filterNotNull()
            .map { stats ->
                // Create a copy of the object with the list sorted by name
                stats.copy(
                    tackleBoxesWithLures = stats.tackleBoxesWithLures.sortedBy {
                        it?.tackleBox?.name?.lowercase()
                    }
                ).toDomain()
            }

    suspend fun addFisherman(fisherman: Fisherman) {
        fishermanDao.insertFisherman(fisherman.toEntity())
        // Automatic Tackle Box creation
        val existing = tackleBoxDao.getExistingTackleBoxForFisherman(fisherman.id)
        if (existing == null) {
            tackleBoxDao.insertTackleBox(
                TackleBoxEntity(
                    fishermanId = fisherman.id,
                    name = "${fisherman.firstName}'s Tackle Box"
                )
            )
        }
    }

    suspend fun getFishermanByName(
        firstName: String,
        lastName: String,
        nickname: String): Fisherman? {
        return fishermanDao.getFishermanByName(firstName, lastName, nickname)?.toDomain()
    }
    suspend fun deleteFisherman(fisherman: Fisherman) {
        fishermanDao.deleteFisherman(fisherman.toEntity())
    }
    suspend fun deleteFishermanById(id: String) {
        fishermanDao.deleteFishermanById(id)
    }

    suspend fun updateFisherman(fisherman: Fisherman) {
        fishermanDao.upsertFisherman(fisherman.toEntity())
    }

    fun getTripSummariesForFisherman(id: String): Flow<List<TripSummary>> {
        return fishermanDao.getTripSummariesForFisherman(id)
            .map { list -> list.toTripSummaryDomainList() }
    }

    fun getUpcomingTripSummariesForFisherman(id: String): Flow<List<TripSummary>> {
        return fishermanDao.getUpcomingTripSummariesForFisherman(id, System.currentTimeMillis())
            .map { list -> list.toTripSummaryDomainList() }
    }
    fun getActiveTripSummariesForFisherman(id: String): Flow<List<TripSummary>> {
        return fishermanDao.getActiveTripSummariesForFisherman(id, System.currentTimeMillis())
            .map { list -> list.toTripSummaryDomainList() }
    }
    fun getPastTripSummariesForFisherman(id: String): Flow<List<TripSummary>> {
        return fishermanDao.getPastTripSummariesForFisherman(id, System.currentTimeMillis())
            .map { list -> list.toTripSummaryDomainList() }
    }

    suspend fun removeFishermanFromTrip(tripId: String, fishermanId: String) =
        fishermanDao.removeFishermanFromTrip(
            fishermanId = fishermanId,
            tripId = tripId
        )

    // --- Tackle Box Logic ---
    suspend fun createTackleBox(tackleBox: TackleBox) {
        tackleBoxDao.insertTackleBox(tackleBox.toEntity())
    }
    suspend fun insertTackleBox(tackleBox: TackleBox) {
        tackleBoxDao.insertTackleBox(tackleBox.toEntity())
    }
    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> {
        return tackleBoxDao.getTackleBoxesForFisherman(fishermanId).map { list ->
            list.toTackleBoxDomainList()
        }
    }
    fun getLuresInTackleBox(tackleBoxId: String): Flow<List<LureWithColors>> {
        return tackleBoxDao.getLuresInTackleBox(tackleBoxId)
            .map { list -> list.toLureWithColorsDomainList() }
    }
    suspend fun deleteTackleBox(tackleBox: TackleBox) {
        tackleBoxDao.deleteTackleBox(tackleBox.toEntity())
    }
}
