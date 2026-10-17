package com.funjim.fishstory.ui.utils

import com.funjim.fishstory.model.Water
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Converts Fahrenheit input (Double or String) to DB storage format.
 * Example: 72.5 -> 7250L
 */
fun Double.fahrenheitToDbValue(): Long = (this * 100.0).roundToLong()
fun String.fahrenheitToDbValue(): Long? = this.toDoubleOrNull()?.fahrenheitToDbValue()

/**
 * Converts Celsius input (Double or String) to DB storage format.
 * Example: 22.5 -> (22.5 * 180) + 3200 = 7250L
 */
fun Double.celsiusToDbValue(): Long = (this * 180.0 + 3200.0).roundToLong()
fun String.celsiusToDbValue(): Long? = this.toDoubleOrNull()?.celsiusToDbValue()

/**
 * Converts DB value (7250L) back to Fahrenheit Double (72.5).
 */
fun Long.toFahrenheitDouble(): Double = this / 100.0

/**
 * Converts DB value (7250L) back to Celsius Double (22.5).
 */
fun Long.toCelsiusDouble(): Double = (this - 3200.0) / 180.0

fun Water.tempDisplayString(useImperial: Boolean = true): String? {
    val tempDb = temperature ?: return null
    return if (!useImperial) {
        String.format(Locale.getDefault(), "%.1f°C", tempDb.toCelsiusDouble())
    } else {
        String.format(Locale.getDefault(), "%.1f°F", tempDb.toFahrenheitDouble())
    }
}

fun Water.depthDisplayString(useImperial: Boolean = true): String? {
    return depth?.let { depth ->
        if (useImperial) {
            val feet = depth.toInches().toLong() / 12
            val inches = depth.toInches().toLong() % 12

            when {
                feet > 0 && inches > 0 -> "${feet}' ${inches}\""
                feet > 0 -> "${feet}'"
                else -> "${inches}\""
            }
        } else {
            val totalCm = depth.toCm()
            val meters = totalCm / 100.0

            if (meters >= 1.0) {
                String.format(Locale.US, "%.1fm", meters).let {
                    if (it.endsWith(".0m")) it.replace(".0m", "m") else it
                }
            } else {
                "${totalCm.toLong()}cm"
            }
        }
    }
}
