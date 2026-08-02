package com.funjim.fishstory.model

import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Bait(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

@Serializable
data class EventBait(
    val eventId: String,
    val baitId: String
)

@Serializable
data class TripBait(
    val tripId: String,
    val baitId: String
)

data class BaitSummary(
    val bait: Bait,
    val fishCaught: Int,
    val fishKept: Int,
    val targetFishCaught: Int,
    val targetFishKept: Int,
    val largestFish: Double,
    val smallestFish: Double
)
