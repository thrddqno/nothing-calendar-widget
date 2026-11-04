package com.adioquino.nothingcalendar.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val dataStore = context.applicationContext.settingsDataStore

    override val useDynamicColors: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLORS] ?: true
    }

    override val selectedCalendarIds: Flow<Set<Long>?> = dataStore.data.map { preferences ->
        preferences[SELECTED_CALENDAR_IDS]?.mapNotNull(String::toLongOrNull)?.toSet()
    }

    override suspend fun setUseDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS] = enabled
        }
    }

    override suspend fun setSelectedCalendarIds(calendarIds: Set<Long>) {
        dataStore.edit { preferences ->
            preferences[SELECTED_CALENDAR_IDS] = calendarIds.map(Long::toString).toSet()
        }
    }

    override suspend fun useDefaultCalendarSelection() {
        dataStore.edit { preferences ->
            preferences.remove(SELECTED_CALENDAR_IDS)
        }
    }

    private companion object {
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val SELECTED_CALENDAR_IDS = stringSetPreferencesKey("selected_calendar_ids")
    }
}
