package com.adioquino.nothingcalendar.calendar.domain

import java.time.Instant

data class CalendarQueryRange(
    val startsAt: Instant,
    val endsAt: Instant,
) {
    init {
        require(endsAt.isAfter(startsAt)) { "Query range end must follow start" }
    }
}
