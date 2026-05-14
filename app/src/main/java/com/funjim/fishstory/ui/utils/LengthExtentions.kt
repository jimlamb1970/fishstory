package com.funjim.fishstory.ui.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

// Centralized constants so there's only one source of truth for the math
private const val IMPERIAL_MULTIPLIER = 254000.0
private const val METRIC_MULTIPLIER = 10000.0

// ==========================================
// 1. INPUT LAYER: Double -> Database Long
// ==========================================

/**
 * Converts a raw UI Double value (Inches) to the universal database storage unit.
 */
fun Double.inchesToStorage(): Long {
    return (this * IMPERIAL_MULTIPLIER).roundToLong()
}

/**
 * Converts a raw UI Double value (Millimeters) to the universal database storage unit.
 */
fun Double.mmToStorage(): Long {
    return (this * METRIC_MULTIPLIER).roundToLong()
}

// ==========================================
// 2. DISPLAY LAYER: Database Long -> Double
// ==========================================

/**
 * Converts the universal database storage unit back to an Imperial Double for UI display.
 */
fun Long.toInches(): Double {
    return this / IMPERIAL_MULTIPLIER
}

/**
 * Converts the universal database storage unit back to a Metric Double for UI display.
 */
fun Long.toMm(): Double {
    return this / METRIC_MULTIPLIER
}

/**
 * Unified entry point for the UI. Returns the correct formatted string based on settings.
 * @param useMetric Current system setting toggle.
 * @param useFractions If true and system is Imperial, displays "2 1/16\"". If false, displays "2.0625\"".
 */
fun Long.toDisplayString(useMetric: Boolean, useFractions: Boolean = false): String {
    return if (useMetric) {
        this.toMmDisplayString()
    } else {
        if (useFractions) this.toFractionalInchesDisplayString() else this.toDecimalInchesDisplayString()
    }
}

// Reusable formatter that drops trailing zeros (e.g., 2.5000 becomes 2.5)
private val uiDecimalFormatter = DecimalFormat("#.####", DecimalFormatSymbols(Locale.US)).apply {
    roundingMode = RoundingMode.HALF_UP
}

/**
 * Converts the stored database Long to a clean millimeter text string (e.g., "52.38 mm").
 */
fun Long.toMmDisplayString(): String {
    val mmValue = this.toMm() // Uses your existing division logic
    return "${uiDecimalFormatter.format(mmValue)} mm"
}

/**
 * Converts the stored database Long to a clean decimal inch text string (e.g., "2.0625\"").
 */
fun Long.toDecimalInchesDisplayString(): String {
    val inchValue = this.toInches() // Uses your existing division logic
    return "${uiDecimalFormatter.format(inchValue)}\""
}

/**
 * Converts the stored database Long into a clean fractional inch string rounded to the nearest 1/16th.
 * Example: 523875L -> "2 1/16\""
 */
fun Long.toFractionalInchesDisplayString(): String {
    val totalInches = this.toInches()
    val wholeInches = totalInches.toInt()

    // Extract the decimal fraction portion and calculate total sixteenths
    val fractionPart = totalInches - wholeInches
    val sixteenths = (fractionPart * 16).roundToInt()

    return when {
        // Case 1: The fraction rounds up to a full inch
        sixteenths == 16 -> "${wholeInches + 1}\""

        // Case 2: No fraction, just whole inches
        sixteenths == 0 -> if (wholeInches == 0) "0\"" else "$wholeInches\""

        // Case 3: It's a pure fraction less than an inch (e.g., "1/16\"")
        wholeInches == 0 -> "${reduceFraction(sixteenths, 16)}\""

        // Case 4: Standard mixed fraction (e.g., "2 1/16\"")
        else -> "$wholeInches ${reduceFraction(sixteenths, 16)}\""
    }
}

/**
 * Simplifies sixteenths fractions down to their lowest common denominator (e.g., 4/16 -> 1/4).
 */
private fun reduceFraction(numerator: Int, denominator: Int): String {
    var num = numerator
    var den = denominator

    // Euclidean algorithm to find greatest common divisor
    var a = num
    var b = den
    while (b != 0) {
        val t = b
        b = a % b
        a = t
    }
    val gcd = a

    num /= gcd
    den /= gcd

    return "$num/$den"
}