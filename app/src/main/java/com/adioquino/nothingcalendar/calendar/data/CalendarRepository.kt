package com.adioquino.nothingcalendar.calendar.data

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange

/** Future CalendarContract-backed data boundary. */
interface CalendarRepository {
    suspend fun events(range: CalendarQueryRange): List<CalendarEvent>
}
