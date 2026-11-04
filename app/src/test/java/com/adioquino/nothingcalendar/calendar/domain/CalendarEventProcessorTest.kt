package com.adioquino.nothingcalendar.calendar.domain

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarEventProcessorTest {
    @Test
    fun `sorts chronologically and removes exact duplicate instances`() {
        val later = event(2, "2026-05-03T10:00:00Z", "2026-05-03T11:00:00Z")
        val earlier = event(1, "2026-05-03T08:00:00Z", "2026-05-03T09:00:00Z")

        val result = CalendarEventProcessor.chronological(listOf(later, earlier, earlier))

        assertEquals(listOf(1L, 2L), result.map(CalendarEvent::eventId))
    }

    @Test
    fun `upcoming keeps ongoing occurrence and excludes completed occurrence`() {
        val now = Instant.parse("2026-05-03T10:00:00Z")
        val completed = event(1, "2026-05-03T08:00:00Z", "2026-05-03T10:00:00Z")
        val ongoing = event(2, "2026-05-03T09:00:00Z", "2026-05-03T11:00:00Z")

        val result = CalendarEventProcessor.upcoming(
            events = listOf(completed, ongoing),
            nowEpochMillis = now.toEpochMilli(),
            rangeEndEpochMillis = now.plusSeconds(86_400).toEpochMilli(),
            limit = 5,
        )

        assertEquals(listOf(ongoing), result)
    }

    @Test
    fun `preserves separate recurring occurrences and original event ID`() {
        val first = event(
            id = 20,
            start = "2026-05-04T09:00:00Z",
            end = "2026-05-04T10:00:00Z",
            originalEventId = 10,
        )
        val second = first.copy(
            startsAt = Instant.parse("2026-05-11T09:00:00Z"),
            endsAt = Instant.parse("2026-05-11T10:00:00Z"),
        )

        val result = CalendarEventProcessor.chronological(listOf(second, first))

        assertEquals(2, result.size)
        assertTrue(result.all { it.originalEventId == 10L })
        assertFalse(result[0].instanceId == result[1].instanceId)
    }

    @Test
    fun `all-day dates use UTC calendar dates instead of device offset`() {
        val allDay = event(
            id = 1,
            start = "2026-06-01T00:00:00Z",
            end = "2026-06-02T00:00:00Z",
            allDay = true,
        )

        val dates = with(CalendarEventProcessor) {
            allDay.touchedDates(ZoneId.of("America/Los_Angeles"))
        }

        assertEquals(listOf(LocalDate.of(2026, 6, 1)), dates)
    }

    @Test
    fun `timed multi-day event marks every touched local date`() {
        val overnight = event(
            id = 1,
            start = "2026-05-01T22:00:00Z",
            end = "2026-05-03T02:00:00Z",
        )

        val dates = with(CalendarEventProcessor) {
            overnight.touchedDates(ZoneId.of("UTC"))
        }

        assertEquals(
            listOf(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 2),
                LocalDate.of(2026, 5, 3),
            ),
            dates,
        )
    }

    @Test
    fun `event ending at midnight does not mark next date`() {
        val event = event(
            id = 1,
            start = "2026-05-01T20:00:00Z",
            end = "2026-05-02T00:00:00Z",
        )

        val dates = with(CalendarEventProcessor) { event.touchedDates(ZoneId.of("UTC")) }

        assertEquals(listOf(LocalDate.of(2026, 5, 1)), dates)
    }

    @Test
    fun `month indicators clamp cross-month event and retain colors`() {
        val event = event(
            id = 1,
            start = "2026-04-30T20:00:00Z",
            end = "2026-05-02T00:00:00Z",
            color = 0xFF112233.toInt(),
        )

        val result = CalendarEventProcessor.monthIndicators(
            listOf(event),
            YearMonth.of(2026, 5),
            ZoneId.of("UTC"),
        )

        assertEquals(setOf(LocalDate.of(2026, 5, 1)), result.days.keys)
        assertEquals(1, result.days.getValue(LocalDate.of(2026, 5, 1)).count)
        assertEquals(
            setOf(0xFF112233.toInt()),
            result.days.getValue(LocalDate.of(2026, 5, 1)).calendarColors,
        )
    }

    @Test
    fun `month indicators do not depend on weekday of first day`() {
        val sundayFirst = event(
            id = 1,
            start = "2026-11-01T08:00:00Z",
            end = "2026-11-01T09:00:00Z",
        )

        val result = CalendarEventProcessor.monthIndicators(
            listOf(sundayFirst),
            YearMonth.of(2026, 11),
            ZoneId.of("UTC"),
        )

        assertEquals(1, result.days.getValue(LocalDate.of(2026, 11, 1)).count)
    }

    @Test
    fun `time-zone conversion moves timed event to correct local date`() {
        val event = event(
            id = 1,
            start = "2026-05-01T01:00:00Z",
            end = "2026-05-01T02:00:00Z",
        )

        val dates = with(CalendarEventProcessor) {
            event.touchedDates(ZoneId.of("America/Los_Angeles"))
        }

        assertEquals(listOf(LocalDate.of(2026, 4, 30)), dates)
    }

    private fun event(
        id: Long,
        start: String,
        end: String,
        originalEventId: Long? = null,
        allDay: Boolean = false,
        color: Int? = null,
    ) = CalendarEvent(
        eventId = id,
        originalEventId = originalEventId,
        calendarId = 100,
        title = "Private test event",
        startsAt = Instant.parse(start),
        endsAt = Instant.parse(end),
        isAllDay = allDay,
        eventTimeZone = if (allDay) "UTC" else "Europe/London",
        calendarColor = color,
    )
}
