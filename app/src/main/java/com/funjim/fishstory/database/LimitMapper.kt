package com.funjim.fishstory.database

import com.funjim.fishstory.model.Limit

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
fun LimitWithSpeciesEntity.toDomain(): Limit {
    return Limit(
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
fun Limit.toEntity(): LimitEntity {
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
fun List<LimitWithSpeciesEntity>.toLimitWithSpeciesDomainList(): List<Limit> = map { it.toDomain() }

fun List<Limit>.toLimitEntityList(): List<LimitEntity> = map { it.toEntity() }