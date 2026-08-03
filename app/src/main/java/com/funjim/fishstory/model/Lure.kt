package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LureColor(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hexCode: String? = null
)

@Serializable
data class Lure(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hookCount: Int = 1,
    val glows: Boolean = false,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
) {
    fun getDisplayName(): String {
        val sb = StringBuilder(name)

        sb.append(" : Need to fix for colors")

        return sb.toString()
    }
}

@Serializable
data class LurePrimaryColor(
    val lureId: String,  // Maps to lure
    val colorId: String  // Maps to color
)

@Serializable
data class LureSecondaryColor(
    val lureId: String,  // Maps to lure
    val colorId: String  // Maps to color
)

@Serializable
data class LureGlowColor(
    val lureId: String,  // Maps to lure
    val colorId: String  // Maps to color
)

data class LureWithPhotos(
    val lure: Lure,
    val photos: List<Photo>
)

data class LureWithColors(
    val lure: Lure,
    val primaryColors: List<LureColor>,
    val secondaryColors: List<LureColor>,
    val glowColors: List<LureColor>
)

data class LureWithDetails(
    val lure: Lure,
    val primaryColors: List<LureColor>,
    val secondaryColors: List<LureColor>,
    val glowColors: List<LureColor>,
    val photos: List<Photo>
)

data class LureWithColorsSummary(
    val lure: Lure,
    val primaryColors: List<LureColor>,
    val secondaryColors: List<LureColor>,
    val glowColors: List<LureColor>,

    val fishCaught: Int,
    val fishKept: Int,
    val targetFishCaught: Int,
    val targetFishKept: Int,
    val largestFish: Double,
    val smallestFish: Double
)
