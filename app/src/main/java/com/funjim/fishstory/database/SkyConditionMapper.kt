package com.funjim.fishstory.database

import com.funjim.fishstory.model.SkyCondition

// Extension: Database Entity -> Domain Model
fun SkyConditionEntity.toDomain(): SkyCondition {
    return SkyCondition(
        id = id,
        name = name
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<SkyConditionEntity>.toDomainList(): List<SkyCondition> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun SkyCondition.toEntity(): SkyConditionEntity {
    return SkyConditionEntity(
        id = id,
        name = name
    )
}