package com.funjim.fishstory.model

import androidx.room.Ignore
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Fisherman(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
) {
    @get:Ignore
    val fullName: String
        get() = if (nickname.isNotBlank()) {
            "$firstName \"$nickname\" $lastName".trim()
        } else {
            "$firstName $lastName".trim()
        }
}

data class FishermanWithTrips(
    val fisherman: Fisherman,
    val trips: List<Trip>
)

data class FishermanSummary(
    val fisherman: Fisherman,
    val fishCaught: Int,
    val fishKept: Int,

    val targetFishCaught: Int,
    val targetFishKept: Int,

    val totalTrips: Int,
    val totalTackleBoxes: Int
)

data class FishermanFullStatistics(
    val fisherman: Fisherman,
    val tackleBoxesWithLures: List<TackleBoxWithLures?>,

    // Fish Extremes
    val largestFishLength: Long?,
    val largestFishTimestamp: Long?,
    val largestFishSpecies: String?,
    val smallestFishLength: Long?,
    val smallestFishTimestamp: Long?,
    val smallestFishSpecies: String?,

    // Best Trip
    val mostTripCatches: Int,
    val bestTripName: String?,
    val bestTripTime: Long?,

    // Best Event & Its Parent Trip
    val mostEventCatches: Int,
    val bestEventName: String?,
    val bestEventTripName: String?,
    val bestEventTime: Long?,

    // Worst Trip
    val fewestTripCatches: Int,
    val worstTripName: String?,
    val worstTripTime: Long?,

    // Worst Event & Its Parent Trip
    val fewestEventCatches: Int,
    val worstEventName: String?,
    val worstEventTripName: String?,
    val worstEventTime: Long?
)

@Serializable
data class EventFisherman(
    val eventId: String,
    val fishermanId: String,
    val tackleBoxId: String? = null
)

@Serializable
data class TripFisherman(
    val tripId: String,
    val fishermanId: String,
    val tackleBoxId: String? = null
)
