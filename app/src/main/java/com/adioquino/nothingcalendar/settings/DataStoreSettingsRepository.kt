package com.adioquino.nothingcalendar.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val dataStore = context.applicationContext.settingsDataStore

    override val useDynamicColors: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLORS] ?: true
    }

    override suspend fun setUseDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS] = enabled
        }
    }

    private companion object {
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
    }
}
