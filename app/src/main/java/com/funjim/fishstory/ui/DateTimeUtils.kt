package com.funjim.fishstory.ui

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object DateTimeUtils {
    // Converts Long to LocalDateTime for easy picking
    fun Long.toLocalDateTime(): LocalDateTime {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    // Updates only the Date part of a timestamp
    fun updateDate(currentTimestamp: Long, selectedUtcMidnight: Long): Long {
        val selectedLocalDate = Instant.ofEpochMilli(selectedUtcMidnight)
            .atZone(ZoneOffset.UTC)   // interpret as UTC, not local
            .toLocalDate()

        return Instant.ofEpochMilli(currentTimestamp)
            .atZone(ZoneId.systemDefault())
            .with(selectedLocalDate)  // swap just the date, keep the time
            .toInstant()
            .toEpochMilli()
    }

    // Updates only the Time part of a timestamp
    fun updateTime(currentTimestamp: Long, hour: Int, minute: Int): Long {
        val currentDate = currentTimestamp.toLocalDateTime().toLocalDate()

        return currentDate.atTime(hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}