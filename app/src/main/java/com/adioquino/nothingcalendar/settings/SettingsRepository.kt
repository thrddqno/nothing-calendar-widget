package com.adioquino.nothingcalendar.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val useDynamicColors: Flow<Boolean>
    val selectedCalendarIds: Flow<Set<Long>?>

    suspend fun setUseDynamicColors(enabled: Boolean)

    suspend fun setSelectedCalendarIds(calendarIds: Set<Long>)

    suspend fun useDefaultCalendarSelection()
}
