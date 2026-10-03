package com.funjim.fishstory.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "app_display_settings")

object PreferencesKeys {
    val VERBOSE_CARDS = booleanPreferencesKey("verbose_cards")
    val SHOW_LOCATION_MAP = booleanPreferencesKey("show_location_map")
    val CAUTION_BLINK_INTERVAL = longPreferencesKey("caution_blink_interval")
    val USE_IMPERIAL_UNITS = booleanPreferencesKey("use_imperial_units")
}
data class DisplaySettings(
    val cautionBlinkInterval: Long = 1000L,
    val useImperialUnits: Boolean = true,
    val verboseCards: Boolean = true
)
