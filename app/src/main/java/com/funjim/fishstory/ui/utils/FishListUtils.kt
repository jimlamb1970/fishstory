package com.funjim.fishstory.ui.utils

import kotlinx.serialization.Serializable

@Serializable
data class FishListRoute(
    val bodyOfWaterId: String? = null,
    val eventId: String? = null,
    val fishermanId: String? = null,
    val lureId: String? = null,
    val speciesId: String? = null,
    val tripId: String? = null,
    val targetOnly: Boolean = false
)

data class FishListFilter(
    val bodyOfWaterId: String? = null,
    val eventId: String? = null,
    val fishermanId: String? = null,
    val lureId: String? = null,
    val speciesId: String? = null,
    val tripId: String? = null,
    val targetOnly: Boolean = false
) {
    // Convenient extension to map route to domain filter
    companion object {
        fun fromRoute(route: FishListRoute) = FishListFilter(
            bodyOfWaterId = route.bodyOfWaterId,
            eventId = route.eventId,
            fishermanId = route.fishermanId,
            lureId = route.lureId,
            speciesId = route.speciesId,
            tripId = route.tripId,
            targetOnly = route.targetOnly
        )
    }
}