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
    val waterId: String? = null,
    val weatherId: String? = null,
    val targetOnly: Boolean = false
)

data class FishFilter(
    val bodyOfWaterId: String? = null,
    val eventId: String? = null,
    val fishermanId: String? = null,
    val lureId: String? = null,
    val speciesId: String? = null,
    val tripId: String? = null,
    val waterId: String? = null,
    val weatherId: String? = null,
    val targetOnly: Boolean = false
) {
    // Convenient extension to map route to domain filter
    companion object {
        fun fromRoute(route: FishListRoute) = FishFilter(
            bodyOfWaterId = route.bodyOfWaterId,
            eventId = route.eventId,
            fishermanId = route.fishermanId,
            lureId = route.lureId,
            speciesId = route.speciesId,
            tripId = route.tripId,
            waterId = route.waterId,
            weatherId = route.weatherId,
            targetOnly = route.targetOnly
        )

        fun toRoute(filter: FishFilter) = FishListRoute(
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId,
            waterId = filter.waterId,
            weatherId = filter.weatherId,
            targetOnly = filter.targetOnly)
    }
}