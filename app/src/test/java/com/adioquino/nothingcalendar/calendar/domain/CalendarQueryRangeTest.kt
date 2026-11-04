package com.adioquino.nothingcalendar.calendar.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarQueryRangeTest {
    @Test
    fun `accepts range whose end follows start`() {
        val start = Instant.parse("2026-01-01T00:00:00Z")
        val end = Instant.parse("2026-01-02T00:00:00Z")

        val range = CalendarQueryRange(start, end)

        assertEquals(start, range.startsAt)
        assertEquals(end, range.endsAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects empty range`() {
        val instant = Instant.parse("2026-01-01T00:00:00Z")

        CalendarQueryRange(instant, instant)
    }
}
