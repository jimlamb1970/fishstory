package com.funjim.fishstory.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Species(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

@Serializable
data class EventTargetSpecies(
    val eventId: String,
    val speciesId: String
)

@Serializable
data class TripTargetSpecies(
    val tripId: String,
    val speciesId: String
)

data class SpeciesSummary(
    val species: Species,
    val fishCaught: Int = 0,
    val fishKept: Int = 0,
    val targetFishCaught: Int = 0,
    val targetFishKept: Int = 0,
    val largestFish: Double,
    val smallestFish: Double
)
