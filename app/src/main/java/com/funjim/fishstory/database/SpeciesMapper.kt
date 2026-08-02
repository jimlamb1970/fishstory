package com.funjim.fishstory.database

import com.funjim.fishstory.model.EventTargetSpecies
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.SpeciesSummary
import com.funjim.fishstory.model.TripTargetSpecies

// Extension: Database Entity -> Domain Model
fun SpeciesEntity.toDomain(): Species {
    return Species(
        id = id,
        name = name
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<SpeciesEntity>.toSpeciesDomainList(): List<Species> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Species.toEntity(): SpeciesEntity {
    return SpeciesEntity(
        id = id,
        name = name
    )
}

// Extension: Database Entity -> Domain Model
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

// Extension: List of Entities -> List of Domain Models
fun List<SpeciesSummaryEntity>.toSpeciesSummaryDomainList(): List<SpeciesSummary> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
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

// Extension: Database Entity -> Domain Model
fun EventTargetSpeciesEntity.toDomain(): EventTargetSpecies {
    return EventTargetSpecies(
        eventId = eventId,
        speciesId = speciesId
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<EventTargetSpeciesEntity>.toEventTargetSpeciesDomainList(): List<EventTargetSpecies> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun EventTargetSpecies.toEntity(): EventTargetSpeciesEntity {
    return EventTargetSpeciesEntity(
        eventId = eventId,
        speciesId = speciesId
    )
}

// Extension: Database Entity -> Domain Model
fun TripTargetSpeciesEntity.toDomain(): TripTargetSpecies {
    return TripTargetSpecies(
        tripId = tripId,
        speciesId = speciesId
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<TripTargetSpeciesEntity>.toTripTargetSpeciesDomainList(): List<TripTargetSpecies> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun TripTargetSpecies.toEntity(): TripTargetSpeciesEntity {
    return TripTargetSpeciesEntity(
        tripId = tripId,
        speciesId = speciesId
    )
}
