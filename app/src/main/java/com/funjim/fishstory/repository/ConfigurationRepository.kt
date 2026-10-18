package com.funjim.fishstory.repository

import com.funjim.fishstory.model.DisplaySettings
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.funjim.fishstory.model.AppSettings
import com.funjim.fishstory.model.FishSettings
import com.funjim.fishstory.model.PreferencesKeys
import com.funjim.fishstory.model.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

interface ConfigurationRepository {
    val appSettings: Flow<AppSettings>

    suspend fun updateCautionBlinkInterval(intervalMs: Long)
    suspend fun updateColorTheme(colorTheme: String)
    suspend fun updateScrollBarOnLeft(onLeft: Boolean)
    suspend fun updateUseImperialUnits(useImperial: Boolean)
    suspend fun updateUseVerboseCards(useVerbose: Boolean)
}


class ConfigurationRepositoryImpl(
    private val context: Context
) : ConfigurationRepository {

    override val appSettings: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            AppSettings(
                display = DisplaySettings(
                    cautionBlinkInterval = preferences[PreferencesKeys.CAUTION_BLINK_INTERVAL] ?: 1000L,
                    colorTheme = preferences[PreferencesKeys.COLOR_THEME] ?: "App Default",
                    useImperialUnits = preferences[PreferencesKeys.USE_IMPERIAL_UNITS] ?: true,
                    verboseCards = preferences[PreferencesKeys.VERBOSE_CARDS] ?: true,
                    scrollbarOnLeft = preferences[PreferencesKeys.SCROLLBAR_ON_LEFT] ?: false
                ),
                fish = FishSettings(
                    showBait = preferences[PreferencesKeys.SHOW_FISH_BAIT] ?: true,
                    showHoleNumber = preferences[PreferencesKeys.SHOW_FISH_HOLE_NUMBER] ?: true,
                    showKept = preferences[PreferencesKeys.SHOW_FISH_KEPT] ?: true,
                    showLength = preferences[PreferencesKeys.SHOW_FISH_LENGTH] ?: true,
                    showWeight = preferences[PreferencesKeys.SHOW_FISH_WEIGHT] ?: true
                )
            )
        }

    override suspend fun updateCautionBlinkInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAUTION_BLINK_INTERVAL] = intervalMs
        }
    }

    override suspend fun updateColorTheme(colorTheme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_THEME] = colorTheme
        }
    }

    override suspend fun updateScrollBarOnLeft(onLeft: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCROLLBAR_ON_LEFT] = onLeft
        }
    }

    override suspend fun updateUseImperialUnits(useImperial: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_IMPERIAL_UNITS] = useImperial
        }
    }

    override suspend fun updateUseVerboseCards(useVerbose: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VERBOSE_CARDS] = useVerbose
        }
    }
}