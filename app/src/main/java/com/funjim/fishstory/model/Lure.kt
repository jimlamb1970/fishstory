package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class LureColor(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hexCode: String
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LureColor) return false
        return name == other.name && hexCode == other.hexCode
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + hexCode.hashCode()
        return result
    }
}

@Serializable
data class Lure(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val hookCount: Int = 1,
    val glows: Boolean = false,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Lure) return false
        return this.name == other.name &&
                this.hookCount == other.hookCount &&
                this.glows == other.glows
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + hookCount
        result = 31 * result + glows.hashCode()
        return result
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

data class LureWithColors(
    val lure: Lure,
    val primaryColors: List<LureColor>,
    val secondaryColors: List<LureColor>,
    val glowColors: List<LureColor>
) {
    fun equals(other: Any?,
               primaryColors: String,
               secondaryColors: String,
               glowColors: String
    ): Boolean {
        if (this === other) return true
        if (other !is LureWithColors) return false

        val sortedPrimaryColors: String = this.primaryColors
            .map { it.id }
            .sorted()
            .joinToString(",")
        val sortedSecondaryColors: String = this.secondaryColors
            .map { it.id }
            .sorted()
            .joinToString(",")
        val sortedGlowColors: String = this.glowColors
            .map { it.id }
            .sorted()
            .joinToString(",")

        return this.lure == other.lure &&
                sortedPrimaryColors == primaryColors &&
                sortedSecondaryColors == secondaryColors &&
                sortedGlowColors == glowColors
    }
}

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
    val largestFish: Long,
    val smallestFish: Long
)
