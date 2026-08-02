package com.funjim.fishstory.database

import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterWithDetails

// Extension: Database Entity -> Domain Model
fun WaterEntity.toDomain(): Water {
    return Water(
        id = id,
        eventId = eventId,
        depth = depth,
        temperature = temperature,
        clarityId = clarityId,
        timestamp = timestamp
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<WaterEntity>.toWaterDomainList(): List<Water> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Water.toEntity(): WaterEntity {
    return WaterEntity(
        id = id,
        eventId = eventId,
        depth = depth,
        temperature = temperature,
        clarityId = clarityId,
        timestamp = timestamp
    )
}

fun WaterEntityWithDetails.toDomain(): WaterWithDetails {
    return WaterWithDetails(
        water = water.toDomain(),
        clarity = clarity?.toDomain()
    )
}

fun List<WaterEntityWithDetails>.toWaterWithDetailsDomainList(): List<WaterWithDetails> {
    return map { it.toDomain() }
}