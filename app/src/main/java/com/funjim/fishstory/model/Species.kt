package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Species(
    val id: String = UUID.randomUUID().toString(),
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Species) return false

        return this.name == other.name
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

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
    val largestFish: Long = 0,
    val smallestFish: Long = 0
)
