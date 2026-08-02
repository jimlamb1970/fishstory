package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BodyOfWater(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

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
    val fishCaught: Int,
    val fishKept: Int,
    val targetFishCaught: Int,
    val targetFishKept: Int,
    val largestFish: Double,
    val smallestFish: Double
)
