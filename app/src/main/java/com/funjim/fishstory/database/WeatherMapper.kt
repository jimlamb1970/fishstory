package com.funjim.fishstory.database

import com.funjim.fishstory.model.Weather

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
fun List<WeatherEntity>.toDomainList(): List<Weather> {
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