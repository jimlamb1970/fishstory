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
    )

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
