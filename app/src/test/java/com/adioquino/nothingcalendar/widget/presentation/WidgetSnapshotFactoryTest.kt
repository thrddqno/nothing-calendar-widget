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
        assertEquals(LocalDate.of(2026, 6, 1), range.endExclusive)
        assertEquals(5, range.weekCount)
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
    fun `all seven possible month starting weekdays align to correct column`() {
        DayOfWeek.entries.forEach { startingWeekday ->
            val month = (2020..2030)
                .asSequence()
                .flatMap { year -> (1..12).asSequence().map { YearMonth.of(year, it) } }
                .first { it.atDay(1).dayOfWeek == startingWeekday }

            listOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY).forEach { firstDayOfWeek ->
                val snapshot = WidgetSnapshotFactory.create(
                    month = month,
                    today = month.atDay(1),
                    firstDayOfWeek = firstDayOfWeek,
                    indicators = emptyMap(),
                    agendaEvents = emptyList(),
                    now = Instant.parse("2026-01-01T00:00:00Z"),
                )
                val expectedColumn = Math.floorMod(
                    startingWeekday.value - firstDayOfWeek.value,
                    7,
                )

                assertEquals(month.atDay(1), snapshot.month.cells[expectedColumn].date)
                assertTrue(snapshot.month.cells[expectedColumn].belongsToDisplayedMonth)
            }
        }
    }

    @Test
    fun `leap and non-leap February include every date exactly once`() {
        val leap = snapshotFor(YearMonth.of(2024, 2), DayOfWeek.MONDAY)
        val common = snapshotFor(YearMonth.of(2025, 2), DayOfWeek.MONDAY)

        assertEquals(29, leap.month.cells.count(WidgetMonthCell::belongsToDisplayedMonth))
        assertEquals(28, common.month.cells.count(WidgetMonthCell::belongsToDisplayedMonth))
        assertEquals(29, leap.month.cells.last { it.belongsToDisplayedMonth }.date.dayOfMonth)
        assertEquals(28, common.month.cells.last { it.belongsToDisplayedMonth }.date.dayOfMonth)
    }

    @Test
    fun `grid uses four five or six rows only when required`() {
        val fourRows = snapshotFor(YearMonth.of(2021, 2), DayOfWeek.MONDAY)
        val fiveRows = snapshotFor(YearMonth.of(2026, 5), DayOfWeek.MONDAY)
        val sixRows = snapshotFor(YearMonth.of(2026, 8), DayOfWeek.MONDAY)

        assertEquals(4, fourRows.month.weekCount)
        assertEquals(5, fiveRows.month.weekCount)
        assertEquals(6, sixRows.month.weekCount)
        listOf(fourRows, fiveRows, sixRows).forEach { snapshot ->
            assertEquals(snapshot.month.weekCount * 7, snapshot.month.cells.size)
            assertTrue(snapshot.month.weekCount in 4..6)
        }
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

        assertEquals(35, snapshot.month.cells.size)
        assertEquals(5, snapshot.month.weekCount)
        val todayCell = snapshot.month.cells.single(WidgetMonthCell::isToday)
        assertEquals(today, todayCell.date)
        assertTrue(todayCell.belongsToDisplayedMonth)
        assertEquals(2, todayCell.indicators.count)
        assertFalse(snapshot.month.cells.first().belongsToDisplayedMonth)
    }

    @Test
    fun `multi-day event indicators appear on every touched grid date`() {
        val indicators = (10..12).associate { day ->
            LocalDate.of(2026, 5, day) to DayIndicators(1, emptySet())
        }
        val snapshot = WidgetSnapshotFactory.create(
            month = YearMonth.of(2026, 5),
            today = LocalDate.of(2026, 5, 1),
            firstDayOfWeek = DayOfWeek.MONDAY,
            indicators = indicators,
            agendaEvents = emptyList(),
            now = Instant.parse("2026-05-01T00:00:00Z"),
        )

        assertEquals(
            setOf(10, 11, 12),
            snapshot.month.cells
                .filter { it.indicators.count > 0 }
                .map { it.date.dayOfMonth }
                .toSet(),
        )
    }

    @Test
    fun `compact grid preserves all dates and seven-column alignment`() {
        val snapshot = snapshotFor(YearMonth.of(2026, 8), DayOfWeek.SUNDAY)
        val inMonth = snapshot.month.cells.filter(WidgetMonthCell::belongsToDisplayedMonth)

        assertEquals((1..31).toList(), inMonth.map { it.date.dayOfMonth })
        assertEquals(6, snapshot.month.cells.indexOfFirst { it.belongsToDisplayedMonth })
        assertEquals(0, snapshot.month.cells.size % 7)
        assertTrue(snapshot.month.cells.size <= 42)
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

    private fun snapshotFor(month: YearMonth, firstDayOfWeek: DayOfWeek) =
        WidgetSnapshotFactory.create(
            month = month,
            today = month.atDay(1),
            firstDayOfWeek = firstDayOfWeek,
            indicators = emptyMap(),
            agendaEvents = emptyList(),
            now = Instant.parse("2026-01-01T00:00:00Z"),
        )
}
