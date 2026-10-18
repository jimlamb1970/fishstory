package com.funjim.fishstory.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "app_display_settings")

object PreferencesKeys {
    val SHOW_LOCATION_MAP = booleanPreferencesKey("show_location_map")
    val CAUTION_BLINK_INTERVAL = longPreferencesKey("caution_blink_interval")
    val COLOR_THEME = stringPreferencesKey("color_theme")
    val USE_IMPERIAL_UNITS = booleanPreferencesKey("use_imperial_units")
    val VERBOSE_CARDS = booleanPreferencesKey("verbose_cards")
    val SCROLLBAR_ON_LEFT = booleanPreferencesKey("scrollbar_on_left")

    val SHOW_FISH_BAIT = booleanPreferencesKey("show_fish_bait")
    val SHOW_FISH_HOLE_NUMBER = booleanPreferencesKey("show_fish_hole_number")
    val SHOW_FISH_KEPT = booleanPreferencesKey("show_fish_kept")
    val SHOW_FISH_LENGTH = booleanPreferencesKey("show_fish_length")
    val SHOW_FISH_WEIGHT = booleanPreferencesKey("show_fish_weight")
}

data class AppSettings(
    val display: DisplaySettings = DisplaySettings(),
    val fish: FishSettings = FishSettings(),
)

data class DisplaySettings(
    val cautionBlinkInterval: Long = 1000L,
    val colorTheme: String = "App Default",
    val useImperialUnits: Boolean = true,
    val verboseCards: Boolean = true,
    val scrollbarOnLeft: Boolean = false
)

data class FishSettings(
    val showBait: Boolean = true,
    val showHoleNumber: Boolean = true,
    val showKept: Boolean = true,
    val showLength: Boolean = true,
    val showWeight: Boolean = false
)
