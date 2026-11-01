package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.EventTargetSpeciesEntity
import com.funjim.fishstory.database.entity.SpeciesEntity
import com.funjim.fishstory.database.entity.SpeciesSummaryEntity
import com.funjim.fishstory.database.entity.TripTargetSpeciesEntity
import com.funjim.fishstory.model.EventTargetSpecies
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.model.TripTargetSpecies

fun SpeciesEntity.toDomain(): Species {
    return Species(
        id = id,
        name = name
    )
}
fun List<SpeciesEntity>.toSpeciesDomainList(): List<Species> {
    return map { it.toDomain() }
}
fun Species.toEntity(): SpeciesEntity {
    return SpeciesEntity(
        id = id,
        name = name
    )
}
fun List<Species>.toSpeciesEntityList(): List<SpeciesEntity> {
    return map { it.toEntity() }
}

fun SpeciesSummaryEntity.toDomain(): SpeciesSummary {
    return SpeciesSummary(
        species = species.toDomain(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}
fun List<SpeciesSummaryEntity>.toSpeciesSummaryDomainList(): List<SpeciesSummary> {
    return map { it.toDomain() }
}
fun SpeciesSummary.toEntity(): SpeciesSummaryEntity {
    return SpeciesSummaryEntity(
        species = species.toEntity(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}

fun EventTargetSpeciesEntity.toDomain(): EventTargetSpecies {
    return EventTargetSpecies(
        eventId = eventId,
        speciesId = speciesId
    )
}
fun List<EventTargetSpeciesEntity>.toEventTargetSpeciesDomainList(): List<EventTargetSpecies> {
    return map { it.toDomain() }
}
fun EventTargetSpecies.toEntity(): EventTargetSpeciesEntity {
    return EventTargetSpeciesEntity(
        eventId = eventId,
        speciesId = speciesId
    )
}

fun TripTargetSpeciesEntity.toDomain(): TripTargetSpecies {
    return TripTargetSpecies(
        tripId = tripId,
        speciesId = speciesId
    )
}
fun List<TripTargetSpeciesEntity>.toTripTargetSpeciesDomainList(): List<TripTargetSpecies> {
    return map { it.toDomain() }
}
fun TripTargetSpecies.toEntity(): TripTargetSpeciesEntity {
    return TripTargetSpeciesEntity(
        tripId = tripId,
        speciesId = speciesId
    )
}
