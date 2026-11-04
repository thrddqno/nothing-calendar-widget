package com.adioquino.nothingcalendar.calendar.domain

import java.time.Instant
import java.time.Duration

data class CalendarQueryRange(
    val startsAt: Instant,
    val endsAt: Instant,
) {
    init {
        require(endsAt.isAfter(startsAt)) { "Query range end must follow start" }
        require(Duration.between(startsAt, endsAt) <= MAX_DURATION) {
            "Query range must not exceed 366 days"
        }
    }

    private companion object {
        val MAX_DURATION: Duration = Duration.ofDays(366)
    }
}
