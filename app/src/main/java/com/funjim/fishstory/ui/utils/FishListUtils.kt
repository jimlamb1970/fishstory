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

@Serializable
data class FishDetailsRoute(
    val fishId: String,
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
    val fishId: String? = null,
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

        fun fromDetailsRoute(route: FishDetailsRoute) = FishFilter(
            fishId = route.fishId,
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
            targetOnly = filter.targetOnly
        )

        fun toDetailsRoute(filter: FishFilter, fishId: String) = FishDetailsRoute(
            fishId = fishId,
            bodyOfWaterId = filter.bodyOfWaterId,
            eventId = filter.eventId,
            fishermanId = filter.fishermanId,
            lureId = filter.lureId,
            speciesId = filter.speciesId,
            tripId = filter.tripId,
            waterId = filter.waterId,
            weatherId = filter.weatherId,
            targetOnly = filter.targetOnly
        )
    }
}

// Extension helper for seamless transition from list route to details route
fun FishListRoute.toDetailsRoute(fishId: String): FishDetailsRoute = FishDetailsRoute(
    fishId = fishId,
    bodyOfWaterId = this.bodyOfWaterId,
    eventId = this.eventId,
    fishermanId = this.fishermanId,
    lureId = this.lureId,
    speciesId = this.speciesId,
    tripId = this.tripId,
    waterId = this.waterId,
    weatherId = this.weatherId,
    targetOnly = this.targetOnly
)