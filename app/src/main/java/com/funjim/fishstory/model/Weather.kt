package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Weather(
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val eventId: String,
    val temperature: Long? = null,
    val skyConditionId: String? = null,
    val windDirection: WindDirection? = null,
    val windSpeed: Long? = null,
    val atmosphericPressure: Long? = null,
    val airVisibility: Long? = null,
    val airHumidity: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Weather) return false
        return tripId == other.tripId &&
                eventId == other.eventId &&
                temperature == other.temperature &&
                skyConditionId == other.skyConditionId &&
                windDirection == other.windDirection &&
                windSpeed == other.windSpeed &&
                atmosphericPressure == other.atmosphericPressure &&
                airVisibility == other.airVisibility &&
                airHumidity == other.airHumidity &&
                timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = tripId.hashCode()
        result = 31 * result + eventId.hashCode()
        result = 31 * result + (temperature?.hashCode() ?: 0)
        result = 31 * result + (skyConditionId?.hashCode() ?: 0)
        result = 31 * result + (windDirection?.hashCode() ?: 0)
        result = 31 * result + (windSpeed?.hashCode() ?: 0)
        result = 31 * result + (atmosphericPressure?.hashCode() ?: 0)
        result = 31 * result + (airVisibility?.hashCode() ?: 0)
        result = 31 * result + (airHumidity?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

data class WeatherWithDetails(
    val weather: Weather,
    val skyCondition: SkyCondition? = null
)

data class WeatherSummary(
    val weather: Weather,
    val skyCondition: SkyCondition? = null,
    val fishCaught: Int = 0,
    val fishKept: Int = 0,
    val targetFishCaught: Int = 0,
    val targetFishKept: Int = 0,
    val largestFish: Long,
    val smallestFish: Long
)
