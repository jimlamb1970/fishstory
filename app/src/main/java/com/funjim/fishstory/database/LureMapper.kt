package com.funjim.fishstory.database

import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureGlowColor
import com.funjim.fishstory.model.LurePrimaryColor
import com.funjim.fishstory.model.LureSecondaryColor
import com.funjim.fishstory.model.LureWithColors
import com.funjim.fishstory.model.LureWithColorsSummary
import com.funjim.fishstory.model.LureWithDetails

// Extension: Database Entity -> Domain Model
fun LureColorEntity.toDomain(): LureColor {
    return LureColor(
        id = id,
        name = name,
        hexCode = hexCode
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<LureColorEntity>.toLureColorDomainList(): List<LureColor> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun LureColor.toEntity(): LureColorEntity {
    return LureColorEntity(
        id = id,
        name = name,
        hexCode = hexCode
    )
}

// Extension: Database Entity -> Domain Model
fun LurePrimaryColorEntity.toDomain(): LurePrimaryColor {
    return LurePrimaryColor(
        lureId = lureId,
        colorId = colorId
    )
}
// Extension: Domain Model -> Database Entity (for Inserts / Updates)
// Extension: List of Entities -> List of Domain Models

fun List<LurePrimaryColorEntity>.toLurePrimaryColorDomainList(): List<LurePrimaryColor> {
    return map { it.toDomain() }
}

// Extension: Database Entity -> Domain Model
fun LurePrimaryColor.toEntity(): LurePrimaryColorEntity {
    return LurePrimaryColorEntity(
        lureId = lureId,
        colorId = colorId
    )
}

// Extension: Database Entity -> Domain Model
fun LureSecondaryColorEntity.toDomain(): LureSecondaryColor {
    return LureSecondaryColor(
        lureId = lureId,
        colorId = colorId
    )
}
// Extension: List of Entities -> List of Domain Models
fun List<LureSecondaryColorEntity>.toLureSecondaryColorDomainList(): List<LureSecondaryColor> {
    return map { it.toDomain() }
}

fun LureSecondaryColor.toEntity(): LureSecondaryColorEntity {
    return LureSecondaryColorEntity(
        lureId = lureId,
        colorId = colorId
    )
}

// Extension: Database Entity -> Domain Model
fun LureGlowColorEntity.toDomain(): LureGlowColor {
    return LureGlowColor(
        lureId = lureId,
        colorId = colorId
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<LureGlowColorEntity>.toLureGowColorDomainList(): List<LureGlowColor> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun LureGlowColor.toEntity(): LureGlowColorEntity {
    return LureGlowColorEntity(
        lureId = lureId,
        colorId = colorId
    )
}

// Extension: Database Entity -> Domain Model
fun LureEntity.toDomain(): Lure {
    return Lure(
        id = id,
        name = name,
        hookCount = hookCount,
        glows = glows,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<LureEntity>.toLureDomainList(): List<Lure> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Lure.toEntity(): LureEntity {
    return LureEntity(
        id = id,
        name = name,
        hookCount = hookCount,
        glows = glows,
        isLocked = isLocked,
        isFavorite = isFavorite
    )
}

// Extension: Database Entity -> Domain Model
fun LureEntityWithColors.toDomain(): LureWithColors {
    return LureWithColors(
        lure = lure.toDomain(),
        primaryColors = primaryColors.map { it.toDomain() },
        secondaryColors = secondaryColors.map { it.toDomain() },
        glowColors = glowColors.map { it.toDomain() }
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<LureEntityWithColors>.toLureWithColorsDomainList(): List<LureWithColors> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun LureWithColors.toEntity(): LureEntityWithColors {
    return LureEntityWithColors(
        lure = lure.toEntity(),
        primaryColors = primaryColors.map { it.toEntity() },
        secondaryColors = secondaryColors.map { it.toEntity() },
        glowColors = glowColors.map { it.toEntity() }
    )
}

// Extension: Database Entity -> Domain Model
fun LureEntityWithDetails.toDomain(): LureWithDetails {
    return LureWithDetails(
        lure = lure.toDomain(),
        primaryColors = primaryColors.map { it.toDomain() },
        secondaryColors = secondaryColors.map { it.toDomain() },
        glowColors = glowColors.map { it.toDomain() },
        photos = photos.map { it.toDomain() }
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<LureEntityWithDetails>.toLureWithDetailsDomainList(): List<LureWithDetails> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun LureWithDetails.toEntity(): LureEntityWithDetails {
    return LureEntityWithDetails(
        lure = lure.toEntity(),
        primaryColors = primaryColors.map { it.toEntity() },
        secondaryColors = secondaryColors.map { it.toEntity() },
        glowColors = glowColors.map { it.toEntity() },
        photos = photos.map { it.toEntity() }
    )
}



// Extension: Database Entity -> Domain Model
fun LureEntityWithColorsSummary.toDomain(): LureWithColorsSummary {
    return LureWithColorsSummary(
        lure = lure.toDomain(),
        primaryColors = primaryColors.map { it.toDomain() },
        secondaryColors = secondaryColors.map { it.toDomain() },
        glowColors = glowColors.map { it.toDomain() },
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<LureEntityWithColorsSummary>.toLureWithColorsSummaryDomainList(): List<LureWithColorsSummary> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun LureWithColorsSummary.toEntity(): LureEntityWithColorsSummary {
    return LureEntityWithColorsSummary(
        lure = lure.toEntity(),
        primaryColors = primaryColors.map { it.toEntity() },
        secondaryColors = secondaryColors.map { it.toEntity() },
        glowColors = glowColors.map { it.toEntity() },
        fishCaught = fishCaught,
        fishKept = fishKept,
        targetFishCaught = targetFishCaught,
        targetFishKept = targetFishKept,
        largestFish = largestFish,
        smallestFish = smallestFish
    )
}
