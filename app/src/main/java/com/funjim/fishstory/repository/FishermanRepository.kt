package com.funjim.fishstory.repository

import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.database.TackleBoxEntity
import com.funjim.fishstory.database.toDomain
import com.funjim.fishstory.database.toEntity
import com.funjim.fishstory.database.toFishermanDomainList
import com.funjim.fishstory.database.toLureWithColorsDomainList
import com.funjim.fishstory.database.toTackleBoxDomainList
import com.funjim.fishstory.database.toTripSummaryDomainList
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        fishermanDao.getFishermanFullStatistics(id, System.currentTimeMillis()).map { stats ->
            // Create a copy of the object with the list sorted by name
            stats.copy(
                tackleBoxesWithLures = stats.tackleBoxesWithLures.sortedBy {
                    it?.tackleBox?.name?.lowercase()
                }
            ).toDomain()
        }

    suspend fun addFisherman(fisherman: Fisherman) {
        fishermanDao.insert(fisherman.toEntity())
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

    // TODO - change to upsert
    suspend fun updateFisherman(fisherman: Fisherman) = fishermanDao.update(fisherman.toEntity())

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

    // --- Tackle Box Logic ---
    suspend fun createTackleBox(fishermanId: String, name: String) {
        tackleBoxDao.insertTackleBox(TackleBoxEntity(fishermanId = fishermanId, name = name))
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