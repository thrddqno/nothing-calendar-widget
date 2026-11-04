package com.adioquino.nothingcalendar.settings

import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val useDynamicColors: Flow<Boolean>
    val selectedCalendarIds: Flow<Set<Long>?>
    val firstDayOfWeek: Flow<DayOfWeek>

    suspend fun setUseDynamicColors(enabled: Boolean)

    suspend fun setSelectedCalendarIds(calendarIds: Set<Long>)

    suspend fun useDefaultCalendarSelection()

    suspend fun setFirstDayOfWeek(firstDayOfWeek: DayOfWeek)
}
