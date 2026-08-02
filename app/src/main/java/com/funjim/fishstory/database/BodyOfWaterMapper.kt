package com.funjim.fishstory.database

import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.BodyOfWaterSummary
import com.funjim.fishstory.model.EventBodyOfWater
import com.funjim.fishstory.model.TripBodyOfWater

// Extension: Database Entity -> Domain Model
fun BodyOfWaterEntity.toDomain(): BodyOfWater {
    return BodyOfWater(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<BodyOfWaterEntity>.toBodyOfWaterDomainList(): List<BodyOfWater> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun BodyOfWater.toEntity(): BodyOfWaterEntity {
    return BodyOfWaterEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude
    )
}

// Extension: Database Entity -> Domain Model
fun BodyOfWaterSummaryEntity.toDomain(): BodyOfWaterSummary {
    return BodyOfWaterSummary(
        bodyOfWater = bodyOfWater.toDomain(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<BodyOfWaterSummaryEntity>.toBodyOfWaterSummaryDomainList(): List<BodyOfWaterSummary> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun BodyOfWaterSummary.toEntity(): BodyOfWaterSummaryEntity {
    return BodyOfWaterSummaryEntity(
        bodyOfWater = bodyOfWater.toEntity(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}

fun List<EventBodyOfWater>.toEventBodyOfWaterEntityList(): List<EventBodyOfWaterEntity> {
    return map { it.toEntity() }
}

fun EventBodyOfWater.toEntity(): EventBodyOfWaterEntity {
    return EventBodyOfWaterEntity(
        eventId = eventId,
        bodyOfWaterId = bodyOfWaterId
    )
}

fun List<EventBodyOfWaterEntity>.toEventBodyOfWaterDomainList(): List<EventBodyOfWater> {
    return map { it.toDomain() }
}

fun EventBodyOfWaterEntity.toDomain(): EventBodyOfWater {
    return EventBodyOfWater(
        eventId = eventId,
        bodyOfWaterId = bodyOfWaterId
    )
}

fun TripBodyOfWater.toEntity(): TripBodyOfWaterEntity {
    return TripBodyOfWaterEntity(
        tripId = tripId,
        bodyOfWaterId = bodyOfWaterId
    )
}

fun List<TripBodyOfWaterEntity>.toTripBodyOfWaterDomainList(): List<TripBodyOfWater> {
    return map { it.toDomain() }
}

fun TripBodyOfWaterEntity.toDomain(): TripBodyOfWater {
    return TripBodyOfWater(
        tripId = tripId,
        bodyOfWaterId = bodyOfWaterId
    )
}

