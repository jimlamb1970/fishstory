package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Fish(
    val id: String = UUID.randomUUID().toString(),
    val speciesId: String,
    val fishermanId: String,
    val tripId: String,
    val eventId: String,
    val caughtCount: Int = 0,
    val keptCount: Int = 0,
    val lureId: String? = null,
    val baitId: String? = null,
    val length: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bodyOfWaterId: String? = null,
    val waterId: String? = null,
    val holeNumber: Int? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class FishWithPhotos(
    val fish: Fish,
    val photos: List<Photo>
)

data class FishWithDetails(
    val fish: Fish,
    val species: Species,
    val fisherman: Fisherman,
    val trip: Trip,
    val event: Event,
    val lure: LureWithColors?,
    val bait: Bait?,
    val bodyOfWater: BodyOfWater?,
    val photoCount: Int = 0
) {
    val fullLureName: String
        get() {
            if (lure == null) return "No Lure"

            return lure.lure.name
        }
}

data class FishCounts(
    val totalCaught: Int = 0,
    val totalKept: Int = 0,
    val totalTargetCaught: Int = 0,
    val totalTargetKept: Int = 0,
    val bodyOfWaterCount: Int = 0,
    val eventCount: Int = 0,
    val fishermanCount: Int = 0,
    val lureCount: Int = 0,
    val tripCount: Int = 0,
)

data class TripWithCounts(
    val trip: Trip,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class EventWithCounts(
    val event: Event,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class FishermanWithCounts(
    val fisherman: Fisherman,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class SpeciesWithCounts(
    val species: Species,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class LureWithCounts(
    val lure: LureWithColors,
    val totalCaught: Int = 0,
    val totalKept: Int = 0
)

data class FishSummary(
    val counts: FishCounts = FishCounts(),
    val topTrip: TripWithCounts? = null,
    val topEvent: EventWithCounts? = null,
    val topFisherman: FishermanWithCounts? = null,
    val topSpecies: SpeciesWithCounts? = null,
    val topLure: LureWithCounts? = null
)
