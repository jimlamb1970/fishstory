package com.funjim.fishstory.ui.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
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
private val INCH_FRACTIONS = listOf(
    "0" to 0.0,
    "⅛" to 0.125,
    "¼" to 0.25,
    "⅜" to 0.375,
    "½" to 0.5,
    "⅝" to 0.625,
    "¾" to 0.75,
    "⅞" to 0.875
)
fun Long.toFractionalInchesDisplayString(): String {
    val totalInches = this.toInches()
    val wholeInches = totalInches.toInt()
    val fractionPart = totalInches - wholeInches

    // Find the nearest matching fraction symbol
    val fractionLabel = INCH_FRACTIONS
        .minByOrNull { abs(it.second - fractionPart) }
        ?.first ?: "0"

    return when {
        // Case 1: Fraction is close to 1 whole inch (e.g., 0.99)
        abs(fractionPart - 1.0) < 0.001 -> "${wholeInches + 1}″"

        // Case 2: Pure whole number with no fractional remainder
        fractionLabel == "0" -> if (wholeInches == 0) "0″" else "$wholeInches″"

        // Case 3: Pure fraction less than an inch (e.g., "½″")
        wholeInches == 0 -> "$fractionLabel″"

        // Case 4: Standard mixed number (e.g., "12½″")
        else -> "$wholeInches$fractionLabel″"
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