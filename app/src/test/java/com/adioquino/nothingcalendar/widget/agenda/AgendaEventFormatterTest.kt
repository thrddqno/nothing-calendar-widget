package com.adioquino.nothingcalendar.widget.agenda

import com.adioquino.nothingcalendar.widget.presentation.WidgetAgendaOccurrence
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaEventFormatterTest {
    private val now = Instant.parse("2026-11-05T12:00:00Z")

    @Test
    fun `today event uses 24-hour time only`() {
        val result = formatter(use24HourTime = true).format(
            occurrence("2026-11-05T18:05:00Z", "2026-11-05T19:00:00Z"),
            now,
        )

        assertEquals("18:05", result.temporalLabel)
    }

    @Test
    fun `today event uses locale 12-hour time`() {
        val result = formatter(use24HourTime = false).format(
            occurrence("2026-11-05T18:05:00Z", "2026-11-05T19:00:00Z"),
            now,
        )

        assertEquals("6:05 PM", result.temporalLabel)
    }

    @Test
    fun `optional end time formats same-day range`() {
        val result = formatter(includeEndTime = true).format(
            occurrence("2026-11-05T18:05:00Z", "2026-11-05T19:15:00Z"),
            now,
        )

        assertEquals("18:05–19:15", result.temporalLabel)
    }

    @Test
    fun `widgets format times with AM and PM`() {
        val morning = formatter(use24HourTime = false).format(
            occurrence("2026-11-05T08:00:00Z", "2026-11-05T09:00:00Z"),
            Instant.parse("2026-11-05T07:00:00Z"),
        )
        val afternoon = formatter(use24HourTime = false).format(
            occurrence("2026-11-05T13:00:00Z", "2026-11-05T14:00:00Z"),
            now,
        )

        assertEquals("8:00 AM", morning.temporalLabel)
        assertEquals("1:00 PM", afternoon.temporalLabel)
    }

    @Test
    fun `noon and midnight use correct meridiem`() {
        val noon = formatter(use24HourTime = false).format(
            occurrence("2026-11-05T12:00:00Z", "2026-11-05T12:30:00Z"),
            Instant.parse("2026-11-05T11:00:00Z"),
        )
        val midnight = formatter(use24HourTime = false).format(
            occurrence("2026-11-06T00:00:00Z", "2026-11-06T00:30:00Z"),
            now,
        )

        assertEquals("12:00 PM", noon.temporalLabel)
        assertEquals("FRI 12:00 AM", midnight.temporalLabel)
    }

    @Test
    fun `same-meridiem range omits repeated marker without ambiguity`() {
        val result = formatter(use24HourTime = false, includeEndTime = true).format(
            occurrence("2026-11-05T12:00:00Z", "2026-11-05T12:30:00Z"),
            Instant.parse("2026-11-05T11:00:00Z"),
        )

        assertEquals("12:00–12:30 PM", result.temporalLabel)
    }

    @Test
    fun `range crossing meridiem retains both markers`() {
        val result = formatter(use24HourTime = false, includeEndTime = true).format(
            occurrence("2026-11-05T11:30:00Z", "2026-11-05T13:00:00Z"),
            Instant.parse("2026-11-05T10:00:00Z"),
        )

        assertEquals("11:30 AM–1:00 PM", result.temporalLabel)
    }

    @Test
    fun `tomorrow event includes abbreviated weekday`() {
        val result = formatter().format(
            occurrence("2026-11-06T09:00:00Z", "2026-11-06T10:00:00Z"),
            now,
        )

        assertEquals("FRI 09:00", result.temporalLabel)
    }

    @Test
    fun `later event includes weekday date month and time`() {
        val result = formatter().format(
            occurrence("2026-11-09T09:00:00Z", "2026-11-09T10:00:00Z"),
            now,
        )

        assertEquals("MON 09 NOV · 09:00", result.temporalLabel)
    }

    @Test
    fun `all-day event uses UTC calendar date without local time conversion`() {
        val losAngelesNow = Instant.parse("2026-06-01T19:00:00Z")
        val result = formatter(zoneId = ZoneId.of("America/Los_Angeles")).format(
            occurrence(
                start = "2026-06-02T00:00:00Z",
                end = "2026-06-03T00:00:00Z",
                allDay = true,
            ),
            losAngelesNow,
        )

        assertEquals("TUE ALL DAY", result.temporalLabel)
    }

    @Test
    fun `ongoing multi-day timed event displays now`() {
        val result = formatter().format(
            occurrence("2026-11-04T09:00:00Z", "2026-11-06T09:00:00Z"),
            now,
        )

        assertEquals("NOW", result.temporalLabel)
    }

    @Test
    fun `blank title uses neutral fallback`() {
        val result = formatter().format(
            occurrence("2026-11-05T18:00:00Z", "2026-11-05T19:00:00Z", title = "  "),
            now,
        )

        assertEquals("Untitled event", result.title)
    }

    @Test
    fun `long Unicode title remains intact for view ellipsis`() {
        val title = "設計レビュー — Product planning with a very long title 🚀"
        val result = formatter().format(
            occurrence("2026-11-05T18:00:00Z", "2026-11-05T19:00:00Z", title = title),
            now,
        )

        assertEquals(title, result.title)
        assertTrue(result.title.contains("🚀"))
    }

    @Test
    fun `timed date follows device zone across UTC boundary`() {
        val zone = ZoneId.of("America/Los_Angeles")
        val localNow = Instant.parse("2026-11-05T06:00:00Z")
        val result = formatter(zoneId = zone, use24HourTime = false).format(
            occurrence("2026-11-05T07:30:00Z", "2026-11-05T08:30:00Z"),
            localNow,
        )

        assertEquals("11:30 PM", result.temporalLabel)
    }

    private fun formatter(
        zoneId: ZoneId = ZoneId.of("UTC"),
        use24HourTime: Boolean = true,
        includeEndTime: Boolean = false,
    ) = AgendaEventFormatter(
        locale = Locale.US,
        zoneId = zoneId,
        use24HourTime = use24HourTime,
        strings = AgendaFormatStrings("ALL DAY", "NOW", "Untitled event"),
        includeEndTime = includeEndTime,
    )

    private fun occurrence(
        start: String,
        end: String,
        allDay: Boolean = false,
        title: String = "Event",
    ) = WidgetAgendaOccurrence(
        eventId = 1,
        occurrenceId = "1:$start",
        title = title,
        startsAt = Instant.parse(start),
        endsAt = Instant.parse(end),
        isAllDay = allDay,
        calendarColor = null,
        isOngoing = false,
    )
}
