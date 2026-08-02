package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Weather(
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val temperature: Long? = null,
    val skyConditionId: String? = null,
    val windDirection: WindDirection? = null,
    val windSpeed: Long? = null,
    val atmosphericPressure: Long? = null,
    val airVisibility: Long? = null,
    val airHumidity: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
    )

data class WeatherWithDetails(
    val weather: Weather,
    val skyCondition: SkyCondition? = null
)
