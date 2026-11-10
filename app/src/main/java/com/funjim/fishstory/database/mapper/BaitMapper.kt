package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.BaitEntity
import com.funjim.fishstory.database.entity.BaitSummaryEntity
import com.funjim.fishstory.database.entity.EventBaitEntity
import com.funjim.fishstory.database.entity.TripBaitEntity
import com.funjim.fishstory.model.Bait
import com.funjim.fishstory.model.BaitSummary
import com.funjim.fishstory.model.EventBait
import com.funjim.fishstory.model.TripBait

fun BaitEntity.toDomain(): Bait {
    return Bait(
        id = id,
        name = name
    )
}
fun List<BaitEntity>.toBaitDomainList(): List<Bait> {
    return map { it.toDomain() }
}
fun Bait.toEntity(): BaitEntity {
    return BaitEntity(
        id = id,
        name = name
    )
}

fun EventBaitEntity.toDomain(): EventBait {
    return EventBait(
        eventId = eventId,
        baitId = baitId
    )
}
fun List<EventBaitEntity>.toEventBaitDomainList(): List<EventBait> {
    return map { it.toDomain() }
}
fun EventBait.toEntity(): EventBaitEntity {
    return EventBaitEntity(
        eventId = eventId,
        baitId = baitId
    )
}

fun TripBaitEntity.toDomain(): TripBait {
    return TripBait(
        tripId = tripId,
        baitId = baitId
    )
}
fun List<TripBaitEntity>.toTripBaitDomainList(): List<TripBait> {
    return map { it.toDomain() }
}
fun TripBait.toEntity(): TripBaitEntity {
    return TripBaitEntity(
        tripId = tripId,
        baitId = baitId
    )
}

fun BaitSummaryEntity.toDomain(): BaitSummary {
    return BaitSummary(
        bait = bait.toDomain(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}
fun List<BaitSummaryEntity>.toBaitSummaryDomainList(): List<BaitSummary> {
    return map { it.toDomain() }
}
fun BaitSummary.toEntity(): BaitSummaryEntity {
    return BaitSummaryEntity(
        bait = bait.toEntity(),
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}