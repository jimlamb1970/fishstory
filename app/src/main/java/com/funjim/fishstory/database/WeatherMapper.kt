package com.funjim.fishstory.database

import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.model.WeatherWithDetails

// Extension: Database Entity -> Domain Model
fun WeatherEntity.toDomain(): Weather {
    return Weather(
        id = id,
        eventId = eventId,
        temperature = temperature,
        skyConditionId = skyConditionId,
        windDirection = windDirection,
        windSpeed = windSpeed,
        atmosphericPressure = atmosphericPressure,
        airVisibility = airVisibility,
        airHumidity = airHumidity,
        timestamp = timestamp
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<WeatherEntity>.toWeatherDomainList(): List<Weather> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Weather.toEntity(): WeatherEntity {
    return WeatherEntity(
        id = id,
        eventId = eventId,
        temperature = temperature,
        skyConditionId = skyConditionId,
        windDirection = windDirection,
        windSpeed = windSpeed,
        atmosphericPressure = atmosphericPressure,
        airVisibility = airVisibility,
        airHumidity = airHumidity,
        timestamp = timestamp
    )
}

fun WeatherEntityWithDetails.toDomain(): WeatherWithDetails {
    return WeatherWithDetails(
        weather = weather.toDomain(),           // Uses WeatherEntity.toDomain()
        skyCondition = skyCondition?.toDomain() // Uses SkyConditionEntity.toDomain() (safe-call for nullable)
    )
}

fun List<WeatherEntityWithDetails>.toWeatherWithDetailsDomainList(): List<WeatherWithDetails> {
    return map { it.toDomain() }
}