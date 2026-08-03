package com.funjim.fishstory.database

import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventDetailedSummary
import com.funjim.fishstory.model.EventSummary
import com.funjim.fishstory.model.EventWithDetails
import com.funjim.fishstory.model.EventWithInfo

// EventEntity <-> Event
fun EventEntity.toDomain(): Event {
    return Event(
        id = id,
        tripId = tripId,
        name = name,
        startTime = startTime,
        endTime = endTime,
        latitude = latitude,
        longitude = longitude,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

fun List<EventEntity>.toEventDomainList(): List<Event> {
    return map { it.toDomain() }
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        tripId = tripId,
        name = name,
        startTime = startTime,
        endTime = endTime,
        latitude = latitude,
        longitude = longitude,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

// EventEntityWithDetails <-> EventWithDetails
fun EventEntityWithDetails.toDomain(): EventWithDetails {
    return EventWithDetails(
        event = event.toDomain(),
        trip = trip.toDomain(),
        fishermen = fishermen.toFishermanDomainList(),
        photos = photos.toPhotoDomainList(),
        targetSpecies = targetSpecies.toSpeciesDomainList(),
        baits = baits.toBaitDomainList(),
        bodiesOfWater = bodiesOfWater.toBodyOfWaterDomainList(),
        waterList = waterList.toWaterWithDetailsDomainList(),
        weatherList = weatherList.toWeatherWithDetailsDomainList()
    )
}

fun List<EventEntityWithDetails>.toEventWithDetailsDomainList(): List<EventWithDetails> {
    return map { it.toDomain() }
}

// EventEntityWithInfo <-> EventWithInfo
fun EventEntityWithInfo.toDomain(): EventWithInfo {
    return EventWithInfo(
        event = event.toDomain(),
        targetSpecies = targetSpecies.toSpeciesDomainList(),
        baits = baits.toBaitDomainList(),
        bodiesOfWater = bodiesOfWater.toBodyOfWaterDomainList(),
    )
}

fun List<EventEntityWithInfo>.toEventWithInfoDomainList(): List<EventWithInfo> {
    return map { it.toDomain() }
}

// EventEntityDetailedSummary <-> EventDetailedSummary
fun EventEntityDetailedSummary.toDomain(): EventDetailedSummary {
    return EventDetailedSummary(
        id = id,
        name = name,
        tripId = tripId,
        startTime = startTime,
        endTime = endTime,
        tripName = tripName,
        tripStartTime = tripStartTime,
        tripEndTime = tripEndTime,
        fishCaught = fishCaught,
        fishKept = fishKept,
        bigFishFisherman = bigFishFisherman,
        bigFishSpecies = bigFishSpecies,
        bigFishLength = bigFishLength,
        mostCaughtFisherman = mostCaughtFisherman,
        mostCaught = mostCaught,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        targetBigFishFisherman = targetBigFishFisherman,
        targetBigFishSpecies = targetBigFishSpecies,
        targetBigFishLength = targetBigFishLength,
        targetMostCaughtFisherman = targetMostCaughtFisherman,
        targetMostCaught = targetMostCaught,
        fishermanCount = fishermanCount,
        tackleBoxCount = tackleBoxCount
    )
}

fun List<EventEntityDetailedSummary>.toEventDetailedSummaryDomainList(): List<EventDetailedSummary> {
    return map { it.toDomain() }
}

// EventEntitySummary <-> EventSummary
fun EventEntitySummary.toDomain(): EventSummary {
    return EventSummary(
        event = event.toDomain(),
        trip = trip.toDomain(),
        waterList = waterList.toWaterWithDetailsDomainList(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        fishermanCount = fishermanCount,
        tackleBoxCount = tackleBoxCount
    )
}

fun List<EventEntitySummary>.toEventSummaryDomainList(): List<EventSummary> {
    return map { it.toDomain() }
}

