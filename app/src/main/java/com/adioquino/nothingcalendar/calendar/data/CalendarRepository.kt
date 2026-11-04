package com.adioquino.nothingcalendar.calendar.data

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange
import com.adioquino.nothingcalendar.calendar.domain.DeviceCalendar
import com.adioquino.nothingcalendar.calendar.domain.MonthIndicators
import java.time.Duration
import java.time.YearMonth
import java.time.ZoneId

interface CalendarRepository {
    suspend fun calendars(): CalendarReadResult<List<DeviceCalendar>>

    suspend fun events(range: CalendarQueryRange): CalendarReadResult<List<CalendarEvent>>

    suspend fun upcomingEvents(
        limit: Int,
        lookahead: Duration = Duration.ofDays(90),
    ): CalendarReadResult<List<CalendarEvent>>

    suspend fun monthIndicators(
        month: YearMonth,
        zoneId: ZoneId,
    ): CalendarReadResult<MonthIndicators>
}
