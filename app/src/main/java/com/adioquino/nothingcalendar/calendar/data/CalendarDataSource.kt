package com.adioquino.nothingcalendar.calendar.data

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange
import com.adioquino.nothingcalendar.calendar.domain.DeviceCalendar

interface CalendarDataSource {
    fun queryCalendars(): ProviderQueryResult<List<DeviceCalendar>>

    fun queryInstances(
        range: CalendarQueryRange,
        calendarIds: Set<Long>,
    ): ProviderQueryResult<List<CalendarEvent>>
}

sealed interface ProviderQueryResult<out T> {
    data class Success<T>(val value: T) : ProviderQueryResult<T>

    data object PermissionDenied : ProviderQueryResult<Nothing>

    data object ProviderUnavailable : ProviderQueryResult<Nothing>
}
