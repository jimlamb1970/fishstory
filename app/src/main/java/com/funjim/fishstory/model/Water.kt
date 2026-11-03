package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Water(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val eventId: String,
    val depth: Long? = null,
    val temperature: Long? = null,
    val clarityId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Water) return false
        return tripId == other.tripId &&
                eventId == other.eventId &&
                depth == other.depth &&
                temperature == other.temperature &&
                clarityId == other.clarityId &&
                timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = tripId.hashCode()
        result = 31 * result + eventId.hashCode()
        result = 31 * result + (depth?.hashCode() ?: 0)
        result = 31 * result + (temperature?.hashCode() ?: 0)
        result = 31 * result + (clarityId?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

data class WaterWithDetails(
    val water: Water,
    val clarity: WaterClarity? = null
)

data class WaterSummary(
    val water: Water,
    val clarity: WaterClarity? = null,
    val fishCaught: Int = 0,
    val fishKept: Int = 0,
    val targetFishCaught: Int = 0,
    val targetFishKept: Int = 0,
    val largestFish: Long,
    val smallestFish: Long
)
