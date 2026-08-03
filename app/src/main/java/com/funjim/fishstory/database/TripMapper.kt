package com.funjim.fishstory.database

import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripDetailedSummary
import com.funjim.fishstory.model.TripSummary
import com.funjim.fishstory.model.TripWithDetails
import com.funjim.fishstory.model.TripWithFishermen
import com.funjim.fishstory.model.TripWithFishermenAndSpecies

// TripEntity <-> Trip
fun TripEntity.toDomain(): Trip {
    return Trip(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

fun List<TripEntity>.toTripDomainList(): List<Trip> {
    return map { it.toDomain() }
}

fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

// TripEntityWithFishermen <-> TripWithFishermen
fun TripEntityWithFishermen.toDomain(): TripWithFishermen {
    return TripWithFishermen(
        trip = trip.toDomain(),
        fishermen = fishermen.toFishermanDomainList()
    )
}

fun List<TripEntityWithFishermen>.toTripWithFishermenDomainList(): List<TripWithFishermen> {
    return map { it.toDomain() }
}

// TripEntityWithFishermenAndSpecies <-> TripWithFishermenAndSpecies
fun TripEntityWithFishermenAndSpecies.toDomain(): TripWithFishermenAndSpecies {
    return TripWithFishermenAndSpecies(
        trip = trip.toDomain(),
        fishermen = fishermen.toFishermanDomainList(),
        targetSpecies = targetSpecies.toSpeciesDomainList()
    )
}

fun List<TripEntityWithFishermenAndSpecies>.toTripWithFishermenAndSpeciesDomainList(): List<TripWithFishermenAndSpecies> {
    return map { it.toDomain() }
}

// TripEntityWithDetails <-> TripWithDetails
fun TripEntityWithDetails.toDomain(): TripWithDetails {
    return TripWithDetails(
        trip = trip.toDomain(),
        events = events.toEventWithInfoDomainList(),
        fishermen = fishermen.toFishermanDomainList(),
        photos = photos.toPhotoDomainList(),
        targetSpecies = targetSpecies.toSpeciesDomainList(),
        bodiesOfWater = bodiesOfWater.toBodyOfWaterDomainList()
    )
}

fun List<TripEntityWithDetails>.toTripWithDetailsDomainList(): List<TripWithDetails> {
    return map { it.toDomain() }
}

// TripEntityDetailedSummary <-> TripDetailedSummary
fun TripEntityDetailedSummary.toDomain(): TripDetailedSummary {
    return TripDetailedSummary(
        id = id,
        name = name,
        startDate = startDate,
        endDate = endDate,
        eventCount = eventCount,
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

fun List<TripEntityDetailedSummary>.toTripDetailedSummaryDomainList(): List<TripDetailedSummary> {
    return map { it.toDomain() }
}

// TripEntitySummary <-> TripSummary
fun TripEntitySummary.toDomain(): TripSummary {
    return TripSummary(
        trip = trip.toDomain(),
        eventCount = eventCount,
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        fishermanCount = fishermanCount,
        tackleBoxCount = tackleBoxCount
    )
}

fun List<TripEntitySummary>.toTripSummaryDomainList(): List<TripSummary> {
    return map { it.toDomain() }
}
