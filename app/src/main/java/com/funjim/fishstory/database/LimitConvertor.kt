package com.funjim.fishstory.database

import androidx.room.TypeConverter
import com.funjim.fishstory.model.LimitType

class LimitTypeConverter {
    @TypeConverter
    fun fromLimitType(type: LimitType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toLimitType(value: String?): LimitType? {
        return value?.let {
            try {
                enumValueOf<LimitType>(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}