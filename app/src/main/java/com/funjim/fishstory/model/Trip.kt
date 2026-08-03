package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class TripWithFishermen(
    val trip: Trip,
    val fishermen: List<Fisherman>
)

data class TripWithFishermenAndSpecies(
    val trip: Trip,
    val fishermen: List<Fisherman>,
    val targetSpecies: List<Species>
)

data class TripWithDetails(
    val trip: Trip,
    val events: List<EventWithInfo>,
    val fishermen: List<Fisherman>,
    val photos: List<Photo>,
    val targetSpecies: List<Species>,
    val bodiesOfWater: List<BodyOfWater>
)

data class TripDetailedSummary(
    val id: String,
    val name: String,
    val startDate: Long,
    val endDate: Long,

    val eventCount: Int = 0,

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

data class TripSummary(
    val trip: Trip,

    val eventCount: Int = 0,

    val fishCaught: Int,
    val fishKept: Int,

    val targetFishCaught: Int,
    val targetFishKept: Int,

    val fishermanCount: Int,
    val tackleBoxCount: Int
)