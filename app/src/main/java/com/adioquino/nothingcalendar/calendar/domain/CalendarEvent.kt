package com.adioquino.nothingcalendar.calendar.domain

import java.time.Instant

data class CalendarEvent(
    val eventId: Long,
    val originalEventId: Long?,
    val calendarId: Long,
    val title: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val isAllDay: Boolean,
    val eventTimeZone: String?,
    val calendarColor: Int?,
) {
    init {
        require(!endsAt.isBefore(startsAt)) { "Event end must not precede start" }
    }

    val instanceId: String
        get() = "$calendarId:$eventId:${startsAt.toEpochMilli()}:${endsAt.toEpochMilli()}"
}
