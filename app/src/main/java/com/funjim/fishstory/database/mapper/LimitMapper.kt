package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.LimitEntity
import com.funjim.fishstory.database.entity.LimitWithSpeciesEntity
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitWithSpecies

/**
 * Maps DB Entity -> Domain Model
 */
fun LimitEntity.toDomain(): Limit {
    return Limit(
        id = id,
        name = name,
        type = type,
        count = count,
        lowerSize = lowerSize,
        lowerInclusive = lowerInclusive,
        upperSize = upperSize,
        upperInclusive = upperInclusive
    )
}
fun LimitWithSpeciesEntity.toDomain(): LimitWithSpecies {
    return LimitWithSpecies(
        id = limit.id,
        name = limit.name,
        type = limit.type,
        count = limit.count,
        lowerSize = limit.lowerSize,
        lowerInclusive = limit.lowerInclusive,
        upperSize = limit.upperSize,
        upperInclusive = limit.upperInclusive,
        species = species.toSpeciesDomainList()
    )
}

/**
 * Maps Domain Model -> DB Entity
 */
fun LimitWithSpecies.toEntity(): LimitEntity {
    return LimitEntity(
        id = id,
        name = name,
        type = type,
        count = count,
        lowerSize = lowerSize,
        lowerInclusive = lowerInclusive,
        upperSize = upperSize,
        upperInclusive = upperInclusive
    )
}

/**
 * List mapping convenience helpers
 */
fun List<LimitEntity>.toLimitDomainList(): List<Limit> = map { it.toDomain() }
fun List<LimitWithSpeciesEntity>.toLimitWithSpeciesDomainList(): List<LimitWithSpecies> = map { it.toDomain() }

fun List<LimitWithSpecies>.toLimitEntityList(): List<LimitEntity> = map { it.toEntity() }