package com.funjim.fishstory.repository

import com.funjim.fishstory.model.DisplaySettings
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.funjim.fishstory.model.PreferencesKeys
import com.funjim.fishstory.model.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

interface ConfigurationRepository {
    val displaySettings: Flow<DisplaySettings>

    suspend fun updateUseVerboseCards(useVerbose: Boolean)
    suspend fun updateCautionBlinkInterval(intervalMs: Long)
}


class ConfigurationRepositoryImpl(
    private val context: Context
) : ConfigurationRepository {

    override val displaySettings: Flow<DisplaySettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            DisplaySettings(
                verboseCards = preferences[PreferencesKeys.VERBOSE_CARDS] ?: true,
                cautionBlinkInterval = preferences[PreferencesKeys.CAUTION_BLINK_INTERVAL] ?: 1000L,
            )
        }

    override suspend fun updateUseVerboseCards(useVerbose: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VERBOSE_CARDS] = useVerbose
        }
    }

    override suspend fun updateCautionBlinkInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAUTION_BLINK_INTERVAL] = intervalMs
        }
    }
}