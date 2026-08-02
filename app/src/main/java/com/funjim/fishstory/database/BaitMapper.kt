package com.funjim.fishstory.database

import com.funjim.fishstory.model.Bait
import com.funjim.fishstory.model.BaitSummary

// Extension: Database Entity -> Domain Model
fun BaitEntity.toDomain(): Bait {
    return Bait(
        id = id,
        name = name
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<BaitEntity>.toBaitDomainList(): List<Bait> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Bait.toEntity(): BaitEntity {
    return BaitEntity(
        id = id,
        name = name
    )
}
// Extension: Database Entity -> Domain Model
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

// Extension: List of Entities -> List of Domain Models
fun List<BaitSummaryEntity>.toBaitSummaryDomainList(): List<BaitSummary> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
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