package com.funjim.fishstory.database

import androidx.room.TypeConverter
import com.funjim.fishstory.model.WindDirection

class WeatherConverters {
    @TypeConverter
    fun fromWindDirection(direction: WindDirection?): String? {
        return direction?.name
    }

    @TypeConverter
    fun toWindDirection(value: String?): WindDirection? {
        return value?.let {
            try {
                WindDirection.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null // Safely handle unexpected values
            }
        }
    }
}