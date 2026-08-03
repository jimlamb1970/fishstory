package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Event(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val name: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class EventWithDetails(
    val event: Event,
    val trip: Trip,
    val fishermen: List<Fisherman>,
    val photos: List<Photo>,
    val targetSpecies: List<Species>,
    val baits: List<Bait>,
    val bodiesOfWater: List<BodyOfWater>,
    val waterList: List<WaterWithDetails>,
    val weatherList: List<WeatherWithDetails>,
)

data class EventWithInfo(
    val event: Event,
    val targetSpecies: List<Species>,
    val baits: List<Bait>,
    val bodiesOfWater: List<BodyOfWater>
)

data class EventDetailedSummary(
    val id: String,
    val name: String,
    val tripId: String,
    val startTime: Long,
    val endTime: Long,

    val tripName: String,
    val tripStartTime: Long,
    val tripEndTime: Long,

    // Overall Fish Stats
    val fishCaught: Int,
    val fishKept: Int,
    val bigFishFisherman: String? = null,
    val bigFishSpecies: String? = null,
    val bigFishLength: Long? = null,
    val mostCaughtFisherman: String? = null,
    val mostCaught: Int? = null,

    // Target Fish Stats
    val targetFishCaught: Int,
    val targetFishKept: Int,
    val targetBigFishFisherman: String? = null,
    val targetBigFishSpecies: String? = null,
    val targetBigFishLength: Long? = null,
    val targetMostCaughtFisherman: String? = null,
    val targetMostCaught: Int? = null,

    // Crew Information
    val fishermanCount: Int,
    val tackleBoxCount: Int
)

data class EventSummary(
    val event: Event,
    val trip: Trip,
    val waterList: List<WaterWithDetails>,

    val fishCaught: Int,
    val fishKept: Int,

    val targetFishCaught: Int,
    val targetFishKept: Int,

    val fishermanCount: Int,
    val tackleBoxCount: Int
)