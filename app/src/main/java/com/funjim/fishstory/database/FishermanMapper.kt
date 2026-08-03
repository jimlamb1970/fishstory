package com.funjim.fishstory.database

import com.funjim.fishstory.model.EventFisherman
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanFullStatistics
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.model.TripFisherman

// FishermanEntity <-> Fisherman
fun FishermanEntity.toDomain(): Fisherman {
    return Fisherman(
        id = id,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

fun List<FishermanEntity>.toFishermanDomainList(): List<Fisherman> {
    return map { it.toDomain() }
}

fun Fisherman.toEntity(): FishermanEntity {
    return FishermanEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        nickname = nickname,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

// FishermanEntitySummary <-> FishermanSummary
fun FishermanEntitySummary.toDomain(): FishermanSummary {
    return FishermanSummary(
        fisherman = fisherman.toDomain(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        totalTrips = totalTrips,
        totalTackleBoxes = totalTackleBoxes
    )
}

fun List<FishermanEntitySummary>.toFishermanSummaryDomainList(): List<FishermanSummary> {
    return map { it.toDomain() }
}

// FishermanEntityFullStatistics <-> FishermanFullStatistics
fun FishermanEntityFullStatistics.toDomain(): FishermanFullStatistics {
    return FishermanFullStatistics(
        fisherman = fisherman.toDomain(),
        tackleBoxesWithLures = tackleBoxesWithLures.map { it?.toDomain() },
        largestFishLength = largestFishLength,
        largestFishTimestamp = largestFishTimestamp,
        largestFishSpecies = largestFishSpecies,
        smallestFishLength = smallestFishLength,
        smallestFishTimestamp = smallestFishTimestamp,
        smallestFishSpecies = smallestFishSpecies,
        mostTripCatches = mostTripCatches,
        bestTripName = bestTripName,
        bestTripTime = bestTripTime,
        mostEventCatches = mostEventCatches,
        bestEventName = bestEventName,
        bestEventTripName = bestEventTripName,
        bestEventTime = bestEventTime,
        fewestTripCatches = fewestTripCatches,
        worstTripName = worstTripName,
        worstTripTime = worstTripTime,
        fewestEventCatches = fewestEventCatches,
        worstEventName = worstEventName,
        worstEventTripName = worstEventTripName,
        worstEventTime = worstEventTime
    )
}

// EventFishermanEntity <-> EventFisherman
fun EventFisherman.toEntity(): EventFishermanEntity {
    return EventFishermanEntity(
        eventId = eventId,
        fishermanId = fishermanId,
        tackleBoxId = tackleBoxId
    )
}

// TripFishermanEntity <-> TripFisherman
fun TripFisherman.toEntity(): TripFishermanEntity {
    return TripFishermanEntity(
        tripId = tripId,
        fishermanId = fishermanId,
        tackleBoxId = tackleBoxId
    )
}
