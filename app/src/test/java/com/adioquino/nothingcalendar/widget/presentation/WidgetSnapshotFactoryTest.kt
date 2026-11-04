package com.adioquino.nothingcalendar.widget.presentation

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.DayIndicators
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetSnapshotFactoryTest {
    @Test
    fun `Monday grid includes leading and trailing cells`() {
        val range = WidgetSnapshotFactory.visibleDateRange(
            YearMonth.of(2026, 5),
            DayOfWeek.MONDAY,
        )

        assertEquals(LocalDate.of(2026, 4, 27), range.start)
        assertEquals(LocalDate.of(2026, 6, 8), range.endExclusive)
    }

    @Test
    fun `Sunday grid uses Sunday preference`() {
        val range = WidgetSnapshotFactory.visibleDateRange(
            YearMonth.of(2026, 5),
            DayOfWeek.SUNDAY,
        )

        assertEquals(LocalDate.of(2026, 4, 26), range.start)
        assertEquals(LocalDate.of(2026, 6, 7), range.endExclusive)
    }

    @Test
    fun `snapshot marks today month membership and indicators`() {
        val today = LocalDate.of(2026, 5, 10)
        val snapshot = WidgetSnapshotFactory.create(
            month = YearMonth.of(2026, 5),
            today = today,
            firstDayOfWeek = DayOfWeek.MONDAY,
            indicators = mapOf(today to DayIndicators(2, setOf(10, 20))),
            agendaEvents = emptyList(),
            now = Instant.parse("2026-05-10T12:00:00Z"),
        )

        assertEquals(42, snapshot.month.cells.size)
        val todayCell = snapshot.month.cells.single(WidgetMonthCell::isToday)
        assertEquals(today, todayCell.date)
        assertTrue(todayCell.belongsToDisplayedMonth)
        assertEquals(2, todayCell.indicators.count)
        assertFalse(snapshot.month.cells.first().belongsToDisplayedMonth)
    }

    @Test
    fun `ongoing status uses exclusive event end`() {
        val now = Instant.parse("2026-05-10T12:00:00Z")
        val ongoing = event(1, "2026-05-10T11:00:00Z", "2026-05-10T13:00:00Z")
        val ended = event(2, "2026-05-10T11:00:00Z", "2026-05-10T12:00:00Z")

        val snapshot = WidgetSnapshotFactory.create(
            month = YearMonth.of(2026, 5),
            today = LocalDate.of(2026, 5, 10),
            firstDayOfWeek = DayOfWeek.MONDAY,
            indicators = emptyMap(),
            agendaEvents = listOf(ongoing, ended),
            now = now,
        )

        assertTrue(snapshot.agenda.occurrences.single { it.eventId == 1L }.isOngoing)
        assertFalse(snapshot.agenda.occurrences.single { it.eventId == 2L }.isOngoing)
        assertEquals(2, snapshot.agenda.totalEligibleEvents)
    }

    private fun event(id: Long, start: String, end: String) = CalendarEvent(
        eventId = id,
        originalEventId = null,
        calendarId = 1,
        title = "Test event",
        startsAt = Instant.parse(start),
        endsAt = Instant.parse(end),
        isAllDay = false,
        eventTimeZone = "UTC",
        calendarColor = null,
    )
}
