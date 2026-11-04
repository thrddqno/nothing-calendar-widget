package com.adioquino.nothingcalendar.calendar.domain

import java.time.Instant

data class CalendarEvent(
    val id: Long,
    val calendarId: Long,
    val title: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val isAllDay: Boolean,
) {
    init {
        require(!endsAt.isBefore(startsAt)) { "Event end must not precede start" }
    }
}
