package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.compareTo

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
    val upperInclusive: Boolean = true,
    val species: List<Species> = emptyList()
)

enum class LimitScope {
    EVENT,
    TRIP
}

@Serializable
data class LimitEventSummary(
    val limit: Limit,
    val event: Event? = null,
    val fishermanCount: Int = 0,
    val caughtCount: Int = 0
) {
    val totalLimitCount: Int
        get() = limit.count * fishermanCount

    val isLimitReached: Boolean
        get() = caughtCount == totalLimitCount

    val isLimitExceeded: Boolean
        get() = caughtCount > totalLimitCount

}

data class LimitSummary(
    val limit: Limit,
    val summaryList: List<LimitEventSummary>,
    val scope: LimitScope = LimitScope.EVENT
) {
    val isLimitExceeded: Boolean
        get() = summaryList.any { it.isLimitExceeded }

    val isLimitReached: Boolean
        get() = limit.count > 0 && summaryList.isNotEmpty() && summaryList.any { it.isLimitReached }

    val isTripLimit: Boolean
        get() = scope == LimitScope.TRIP
}
