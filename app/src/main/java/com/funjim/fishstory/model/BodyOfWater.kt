package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BodyOfWater(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is BodyOfWater) return false

        return this.name == other.name &&
                this.latitude == other.latitude &&
                this.longitude == other.longitude
    }

    override fun hashCode(): Int {
        var result = latitude?.hashCode() ?: 0
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

@Serializable
data class EventBodyOfWater(
    val eventId: String,
    val bodyOfWaterId: String
)

@Serializable
data class TripBodyOfWater(
    val tripId: String,
    val bodyOfWaterId: String
)

data class BodyOfWaterSummary(
    val bodyOfWater: BodyOfWater,
    val fishCaught: Int = 0,
    val fishKept: Int = 0,
    val targetFishCaught: Int = 0,
    val targetFishKept: Int = 0,
    val largestFish: Long = 0,
    val smallestFish: Long = 0
)
