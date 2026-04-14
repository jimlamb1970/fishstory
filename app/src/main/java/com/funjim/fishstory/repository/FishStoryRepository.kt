package com.funjim.fishstory.repository

import android.widget.GridLayout
import com.funjim.fishstory.database.FishDao
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.database.SegmentDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.viewmodels.FishermanSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FishStoryRepository(
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val lureDao: LureDao,
    private val photoDao: PhotoDao,
    private val segmentDao: SegmentDao,
    private val tackleBoxDao: TackleBoxDao,
    private val tripDao: TripDao
) {
    // Basic Data Streams
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()
    val allSpecies: Flow<List<Species>> = fishDao.getAllSpecies()
    val allLureColors: Flow<List<LureColor>> = lureDao.getAllLureColors()

    fun getTrip(id: String) = tripDao.getTrip(id)
    fun getSegmentsForTrip(tripId: String) = segmentDao.getSegmentsForTrip(tripId)
    fun getSegment(id: String) = segmentDao.getSegment(id)
    fun getFisherman(id: String) = fishermanDao.getFisherman(id)
    suspend fun getFish(id: String) = fishDao.getFish(id)

    // Detail Lookups
    fun getTripWithDetails(id: String) = tripDao.getTripWithDetails(id)
    fun getSegmentWithDetails(id: String) = segmentDao.getSegmentWithDetails(id)
    fun getFishermanWithDetails(id: String) = fishermanDao.getFishermanWithDetails(id)

    // The Core Filtering Logic (Migrated from ViewModel)
    fun getFilteredFish(
        tripId: String?,
        segmentId: String?,
        fishermanId: String?
    ): Flow<List<FishWithDetails>> {
        return when {
            !segmentId.isNullOrBlank() -> fishDao.getFishForSegment(segmentId)
            !tripId.isNullOrBlank() -> fishDao.getFishForTrip(tripId)
            !fishermanId.isNullOrBlank() -> fishDao.getFishForFisherman(fishermanId)
            else -> fishDao.getAllFishWithDetails()
        }
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

    suspend fun deleteFisherman(fisherman: Fisherman) {
        fishermanDao.deleteFisherman(fisherman)
    }

    suspend fun updateFisherman(fisherman: Fisherman) = fishermanDao.update(fisherman)

    fun getTripSummariesForFisherman(id: String): Flow<List<TripSummary>> =
        fishermanDao.getTripSummariesForFisherman(id)

    // --- Lure & Color Logic ---
    fun getLureColors(): Flow<List<LureColor>> = lureDao.getAllLureColors()

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

    fun getPhotosForLure(id: String): Flow<List<Photo>> = photoDao.getPhotosForLure(id)

    val fishPhotos: Flow<Map<String, List<Photo>>> = photoDao.getAllFishPhotos()
        .map { photos ->
            photos.filter { it.fishId != null }.groupBy { it.fishId!! }
        }
    val lurePhotos: Flow<Map<String, List<Photo>>> = photoDao.getAllLurePhotos()
        .map { photos ->
            photos.filter { it.lureId != null }.groupBy { it.lureId!! }
        }

    suspend fun upsertFish(fish: Fish) = fishDao.upsertFish(fish)
    suspend fun deleteFish(fish: Fish) = fishDao.deleteFish(fish)

    suspend fun addPhoto(photo: Photo) = photoDao.insertPhoto(photo)
    suspend fun deletePhoto(photo: Photo) = photoDao.deletePhoto(photo)

    suspend fun addSpecies(species: Species) = fishDao.insertSpecies(species)
    suspend fun deleteSpecies(species: Species) = fishDao.deleteSpecies(species)

    // --- Tackle Box Logic ---
    suspend fun createTackleBox(fishermanId: String, name: String) =
        tackleBoxDao.insertTackleBox(TackleBox(fishermanId = fishermanId, name = name))

    suspend fun deleteTackleBox(tackleBox: TackleBox) = tackleBoxDao.deleteTackleBox(tackleBox)
}