package com.funjim.fishstory.database

import com.funjim.fishstory.model.EventWithCounts
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.FishWithPhotos
import com.funjim.fishstory.model.FishermanWithCounts
import com.funjim.fishstory.model.LureWithCounts
import com.funjim.fishstory.model.SpeciesWithCounts
import com.funjim.fishstory.model.TripWithCounts

// Extension: Database Entity -> Domain Model
fun FishEntity.toDomain(): Fish {
    return Fish(
        id = id,
        speciesId = speciesId,
        fishermanId = fishermanId,
        tripId = tripId,
        eventId = eventId,
        caughtCount = caughtCount,
        keptCount = keptCount,
        lureId = lureId,
        baitId = baitId,
        length = length,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        bodyOfWaterId = bodyOfWaterId,
        waterId = waterId,
        holeNumber = holeNumber,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<FishEntity>.toFishDomainList(): List<Fish> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Fish.toEntity(): FishEntity {
    return FishEntity(
        id = id,
        speciesId = speciesId,
        fishermanId = fishermanId,
        tripId = tripId,
        eventId = eventId,
        caughtCount = caughtCount,
        keptCount = keptCount,
        lureId = lureId,
        baitId = baitId,
        length = length,
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        bodyOfWaterId = bodyOfWaterId,
        waterId = waterId,
        holeNumber = holeNumber,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

fun TripEntityWithCounts.toDomain(): TripWithCounts {
    return TripWithCounts(
        // FIX LATER
        trip = trip,
        totalCaught = totalCaught,
        totalKept = totalKept
    )
}

fun EventEntityWithCounts.toDomain(): EventWithCounts {
    return EventWithCounts(
        // FIX LATER
        event = event,
        totalCaught = totalCaught,
        totalKept = totalKept
    )
}

fun FishermanEntityWithCounts.toDomain(): FishermanWithCounts {
    return FishermanWithCounts(
        // FIX LATER
        fisherman = fisherman,
        totalCaught = totalCaught,
        totalKept = totalKept
    )
}

fun SpeciesEntityWithCounts.toDomain(): SpeciesWithCounts {
    return SpeciesWithCounts(
        species = species.toDomain(),
        totalCaught = totalCaught,
        totalKept = totalKept
    )
}

fun LureEntityWithCounts.toDomain(): LureWithCounts {
    return LureWithCounts(
        // FIX LATER
        lure = lure.toDomain(),
        totalCaught = totalCaught,
        totalKept = totalKept
    )
}

fun FishEntityWithDetails.toDomain(): FishWithDetails {
    return FishWithDetails(
        // FIX LATER
        fish = fish.toDomain(),
        species = species.toDomain(),
        fisherman = fisherman,
        trip = trip,
        event = event,
        lure = lure?.toDomain(),
        bait = bait?.toDomain(),
        bodyOfWater = bodyOfWater?.toDomain(),
        photoCount = photoCount
    )
}

fun List<FishEntityWithDetails>.toFishWithDetailsDomainList(): List<FishWithDetails> {
    return map { it.toDomain() }
}

fun FishEntityWithPhotos.toDomain(): FishWithPhotos {
    return FishWithPhotos(
        fish = fish.toDomain(),
        photos = photos.toPhotoDomainList()
    )
}

