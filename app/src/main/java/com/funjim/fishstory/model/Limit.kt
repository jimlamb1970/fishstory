package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class LimitType(
    val label: String,
    val description: String
) {
    BAG_LIMIT(
        label = "Daily Bag Limit",
        description = "Maximum number of fish allowed to keep per day.  All fish caught count towards the limit."
    ),
    MIN_SIZE(
        label = "Minimum Length Limit",
        description = "If the fish size is less than the minimum length and kept, it will count towards the limit."
    ),
    MAX_SIZE(
        label = "Maximum Length Limit",
        description = "If the fish size is greater than the maximum length and kept, it will count towards the limit."
    ),
    SLOT_LIMIT(
        label = "Protected Slot Limit",
        description = "If the fish size falls within the slot and kept, it will count towards the limit."
    ),
    TROPHY_LIMIT(
        label = "Trophy Length Limit",
        description = "If the fish size is greater than the trophy length and kept, it will count towards the limit."
    )
}

@Serializable
data class Limit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: LimitType,
    val count: Int = 0,
    val lowerSize: Long? = null,
    val lowerInclusive: Boolean = true,
    val upperSize: Long? = null,
    val upperInclusive: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Limit) return false

        return this.name == other.name &&
                this.type == other.type &&
                this.count == other.count &&
                this.lowerSize == other.lowerSize &&
                this.lowerInclusive == other.lowerInclusive &&
                this.upperSize == other.upperSize &&
                this.upperInclusive == other.upperInclusive
    }
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + count.hashCode()
        result = 31 * result + (lowerSize?.hashCode() ?: 0)
        result = 31 * result + lowerInclusive.hashCode()
        result = 31 * result + (upperSize?.hashCode() ?: 0)
        result = 31 * result + upperInclusive.hashCode()
        return result
    }

}

@Serializable
data class LimitSpecies(
    val limitId: String,
    val speciesId: String
)

@Serializable
data class EventLimit(
    val eventId: String,
    val limitId: String
)

@Serializable
data class TripLimit(
    val tripId: String,
    val limitId: String
)

data class LimitWithSpecies(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: LimitType,
    val count: Int = 0,
    val lowerSize: Long? = null,
    val lowerInclusive: Boolean = true,
    val upperSize: Long? = null,
    val upperInclusive: Boolean = true,
    val species: List<Species> = emptyList()
) {
    fun equals(other: Any?, species: String): Boolean {
        if (this === other) return true

        if (other !is Limit) return false

        val sortedSpecies: String = this.species
            .map { it.id }
            .sorted()
            .joinToString(",")

        return this.name == other.name &&
                this.type == other.type &&
                this.count == other.count &&
                this.lowerSize == other.lowerSize &&
                this.lowerInclusive == other.lowerInclusive &&
                this.upperSize == other.upperSize &&
                this.upperInclusive == other.upperInclusive &&
                sortedSpecies == species
    }
}

enum class LimitScope {
    EVENT,
    TRIP
}

data class LimitEventSummary(
    val limit: LimitWithSpecies,
    val event: Event? = null,
    val fishermanCount: Int = 0,
    val caughtCount: Int = 0
) {
    val totalLimitCount: Int
        get() = limit.count * fishermanCount

    val isLimitReached: Boolean
        get() = (caughtCount == totalLimitCount) && (caughtCount > 0)

    val isLimitExceeded: Boolean
        get() = caughtCount > totalLimitCount

}

data class LimitSummary(
    val limit: LimitWithSpecies,
    val summaryList: List<LimitEventSummary>,
    val scope: LimitScope = LimitScope.EVENT
) {
    val isLimitExceeded: Boolean
        get() = summaryList.any { it.isLimitExceeded }

    val limitExceededCount: Int
        get() = summaryList.count { it.isLimitExceeded }

    val isLimitReached: Boolean
        get() = limit.count > 0 && summaryList.isNotEmpty() && summaryList.any { it.isLimitReached }

    val limitReachedCount: Int
        get() = summaryList.count { it.isLimitReached && it.caughtCount > 0 }

    val limitCount: Int
        get() = summaryList.size

    val isTripLimit: Boolean
        get() = scope == LimitScope.TRIP
}
