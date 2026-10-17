package com.funjim.fishstory.ui.utils

import com.funjim.fishstory.model.Weather
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/** 1 km = 1,000,000 mm (EXACT) */
fun Long.kmToDbValue(): Long = this * 1_000_000L

/** 1 mile = 1,609,344 mm (EXACT) */
fun Long.milesToDbValue(): Long = this * 1_609_344L

/** Converts DB Millimeters to Kilometers */
fun Long.toKm(): Long = this / 1_000_000L

/** Converts DB Millimeters to Miles */
fun Long.toMiles(): Long = this / 1_609_344L

private val MM_PER_MILE = BigDecimal("1609344")
private val MM_PER_KM = BigDecimal("1000000")

private val BASE_PER_MPH = BigDecimal("16093440")
private val BASE_PER_KMH = BigDecimal("10000000")

/**
 * Converts DB airVisibility (stored in Millimeters as Long) to UI string.
 * @param useImperial If false, converts to Kilometers (KM). If true, converts to Miles (mi).
 */
fun Long?.airVisibilityDisplayString(useImperial: Boolean): String {
    if (this == null) return ""
    val divisor = if (!useImperial) MM_PER_KM else MM_PER_MILE

    return BigDecimal(this)
        .divide(divisor, 1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * Converts UI input string to DB Millimeters (Long).
 * @param useImperial If false, treats input as KM. If true, treats input as Miles.
 */
fun String.visibilityInputToDbValueOrNull(useImperial: Boolean): Long? {
    val decimalInput = this.toBigDecimalOrNull() ?: return null
    val multiplier = if (!useImperial) MM_PER_KM else MM_PER_MILE

    return decimalInput.multiply(multiplier)
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

/**
 * Converts DB windSpeed (stored in base Long unit) to UI string.
 * @param useImperial If false, converts to KM/H. If true, converts to MPH.
 */
fun Long?.windSpeedDisplayString(useImperial: Boolean): String {
    if (this == null) return ""
    val divisor = if (!useImperial) BASE_PER_KMH else BASE_PER_MPH

    return BigDecimal(this)
        .divide(divisor, 1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

/**
 * Converts UI input string to DB Wind Speed (Long).
 * @param useImperial If false, treats input as KM/H. If true, treats input as MPH.
 */
fun String.windSpeedInputToDbValueOrNull(useImperial: Boolean): Long? {
    val decimalInput = this.toBigDecimalOrNull() ?: return null
    val multiplier = if (!useImperial) BASE_PER_KMH else BASE_PER_MPH

    return decimalInput.multiply(multiplier)
        .setScale(0, RoundingMode.HALF_UP)
        .toLong()
}

fun Weather.airHumidityDisplayString(): String? {
    val airHumidityDb = airHumidity ?: return null
    return String.format(Locale.getDefault(), "${airHumidityDb}%%")
}
fun Weather.airVisibilityDisplayString(useImperial: Boolean): String? {
    val visibilityDb = airVisibility ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%s km", visibilityDb.airVisibilityDisplayString(false))
    } else {
        String.format(Locale.getDefault(), "%s m", visibilityDb.airVisibilityDisplayString(true))
    }
}
fun Weather.tempDisplayString(useImperial: Boolean): String? {
    val tempDb = temperature ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%.1f°C", tempDb.toCelsiusDouble())
    } else {
        String.format(Locale.getDefault(), "%.1f°F", tempDb.toFahrenheitDouble())
    }
}
fun Weather.windSpeedDisplayString(useImperial: Boolean): String? {
    val speedDb = windSpeed ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%s km/h", speedDb.windSpeedDisplayString(false))
    } else {
        String.format(Locale.getDefault(), "%s mph", speedDb.windSpeedDisplayString(true))
    }
}
