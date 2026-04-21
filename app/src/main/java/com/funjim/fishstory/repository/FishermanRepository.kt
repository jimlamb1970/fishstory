package com.funjim.fishstory.repository

import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FishermanRepository(
    private val fishermanDao: FishermanDao,
    private val lureDao: LureDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao,
) {
    val allFishermen: Flow<List<Fisherman>> = fishermanDao.getAllFishermen()
    fun getFishermenForTrip(tripId: String): Flow<List<Fisherman>> =
        fishermanDao.getFishermenForTrip(tripId)
    fun getFishermenForSegment(segmentId: String): Flow<List<Fisherman>> =
        fishermanDao.getFishermenForSegment(segmentId)

    // TODO - this really should be from Lure Repo
    val allLureColors = lureDao.getAllLureColors()

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
                FishermanSortOrder.MOST_CATCHES -> summaries.sortedByDescending { it.totalCatches }
                else -> summaries.sortedBy { it.fisherman.fullName.lowercase() }
                /* TODO -- add later
                    FishermanSortOrder.MOST_RELEASED -> summaries.sortedByDescending { it.totalReleased }
                    FishermanSortOrder.MOST_TRIPS -> summaries.sortedByDescending { it.totalTrips }
                */
            }
            if (reversed) sorted.reversed() else sorted
        }
    }

    fun getFishermanFullStatistics(id: String): Flow<FishermanFullStatistics?> =
        fishermanDao.getFishermanFullStatistics(id)

    suspend fun addFisherman(fisherman: Fisherman) {
        fishermanDao.insert(fisherman)
        // Automatic Tackle Box creation
        val existing = tackleBoxDao.getExistingTackleBoxForFisherman(fisherman.id)
        if (existing == null) {
            tackleBoxDao.insertTackleBox(
                TackleBox(fishermanId = fisherman.id, name = "${fisherman.firstName}'s Tackle Box")
            )
        }
    }

    suspend fun getFishermanByName(
        firstName: String,
        lastName: String,
        nickname: String): Fisherman? {
        return fishermanDao.getFishermanByName(firstName, lastName, nickname)
    }

    suspend fun deleteFisherman(fisherman: Fisherman) {
        fishermanDao.deleteFisherman(fisherman)
    }

    // TODO - change to upsert
    suspend fun updateFisherman(fisherman: Fisherman) = fishermanDao.update(fisherman)

    fun getTripSummariesForFisherman(id: String): Flow<List<TripSummary>> =
        fishermanDao.getTripSummariesForFisherman(id)

    fun getLureNamesInTackleBox(tackleBoxId: String?): Flow<List<String>> {
        val luresFlow = tackleBoxDao.getLuresInTackleBox(tackleBoxId ?: "")
        val colorsFlow = lureDao.getAllLureColors()

        return combine(luresFlow, colorsFlow) { lures, colors ->
            val colorMap = colors.associate { it.id to it.name }
            lures.map { lure ->
                val primary = colorMap[lure.primaryColorId]
                val secondary = colorMap[lure.secondaryColorId]
                val glow = colorMap[lure.glowColorId]
                lure.getDisplayName(primary, secondary, glow)
            }.sorted()
        }
    }

    // --- Photo Logic ---
    fun getPhotosForFisherman(id: String): Flow<List<Photo>> = photoDao.getPhotosForFisherman(id)

    val lurePhotos: Flow<Map<String, List<Photo>>> = photoDao.getAllLurePhotos()
        .map { photos ->
            photos.filter { it.lureId != null }.groupBy { it.lureId!! }
        }

    suspend fun addPhoto(photo: Photo) = photoDao.insertPhoto(photo)
    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

    // --- Tackle Box Logic ---
    suspend fun createTackleBox(fishermanId: String, name: String) =
        tackleBoxDao.insertTackleBox(TackleBox(fishermanId = fishermanId, name = name))

    suspend fun insertTackleBox(tackleBox: TackleBox) = tackleBoxDao.insertTackleBox(tackleBox)

    fun getTackleBoxesForFisherman(fishermanId: String): Flow<List<TackleBox>> =
        tackleBoxDao.getTackleBoxesForFisherman(fishermanId)

    fun getLuresInTackleBox(tackleBoxId: String): Flow<List<Lure>> =
        tackleBoxDao.getLuresInTackleBox(tackleBoxId)

    suspend fun deleteTackleBox(tackleBox: TackleBox) = tackleBoxDao.deleteTackleBox(tackleBox)
}