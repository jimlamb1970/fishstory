package com.funjim.fishstory.database

import com.funjim.fishstory.model.WaterClarity

// Extension: Database Entity -> Domain Model
fun WaterClarityEntity.toDomain(): WaterClarity {
    return WaterClarity(
        id = id,
        name = name
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<WaterClarityEntity>.toDomainList(): List<WaterClarity> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun WaterClarity.toEntity(): WaterClarityEntity {
    return WaterClarityEntity(
        id = id,
        name = name
    )
}