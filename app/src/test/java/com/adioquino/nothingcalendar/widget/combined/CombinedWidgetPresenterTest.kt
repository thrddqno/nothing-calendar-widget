package com.adioquino.nothingcalendar.widget.combined

import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.DayIndicators
import com.adioquino.nothingcalendar.widget.agenda.AgendaEventFormatter
import com.adioquino.nothingcalendar.widget.agenda.AgendaFormatStrings
import com.adioquino.nothingcalendar.widget.agenda.AgendaWidgetPresenter
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshot
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CombinedWidgetPresenterTest {
    private val now = Instant.parse("2026-08-10T12:00:00Z")
    private val presenter = CombinedWidgetPresenter(
        AgendaWidgetPresenter(
            AgendaEventFormatter(
                locale = Locale.US,
                zoneId = ZoneId.of("UTC"),
                use24HourTime = true,
                strings = AgendaFormatStrings("ALL DAY", "NOW", "Untitled event"),
            ),
        ),
    )

    @Test
    fun `ready model uses month and agenda from same immutable snapshot`() {
        val snapshot = snapshot(events = listOf(event(1, 13, 14)))

        val result = presenter.present(WidgetLoadState.Ready(snapshot), snapshot(), now, 400)

        assertSame(snapshot.month, result.month)
        assertEquals(snapshot.agenda.occurrences.single().occurrenceId, result.agenda.rows.single().occurrenceId)
    }

    @Test
    fun `six-row month fits complete month and as many agenda rows as space allows`() {
        val snapshot = snapshot(events = (1L..6L).map { event(it, 12 + it, 13 + it) })

        val result = presenter.present(WidgetLoadState.Ready(snapshot), snapshot(), now, 400)

        assertEquals(6, result.month.weekCount)
        assertEquals(6, result.agenda.rows.size)
        assertEquals(0, result.agenda.hiddenEventCount)
        assertFalse(result.agenda.showOverflow)
    }

    @Test
    fun `default 4x4 widget keeps complete six-row month and one complete agenda row`() {
        val snapshot = snapshot(events = listOf(event(1, 13, 14)))

        val result = presenter.present(WidgetLoadState.Ready(snapshot), snapshot(), now, 250)

        assertEquals(6, result.month.weekCount)
        assertEquals(1, result.agenda.rows.size)
        assertEquals(0, result.agenda.hiddenEventCount)
    }

    @Test
    fun `default 4x4 six-row month shows two events with accurate overflow`() {
        val snapshot = snapshot(events = (1L..7L).map { event(it, 12 + it, 13 + it) })

        val result = presenter.present(WidgetLoadState.Ready(snapshot), snapshot(), now, 250)

        assertEquals(6, result.month.weekCount)
        assertEquals(2, result.agenda.rows.size)
        assertEquals(5, result.agenda.hiddenEventCount)
        assertTrue(result.agenda.showOverflow)
    }

    @Test
    fun `combined agenda preserves chronological ordering and ongoing state`() {
        val ongoing = event(1, 11, 13)
        val later = event(2, 16, 17)
        val snapshot = snapshot(events = listOf(later, ongoing))

        val result = presenter.present(WidgetLoadState.Ready(snapshot), snapshot(), now, 400)

        assertEquals(listOf(1L, 2L), result.agenda.rows.map { it.eventId })
        assertEquals("NOW", result.agenda.rows.first().temporalLabel)
    }

    @Test
    fun `all-day multi-day and long titles retain agenda formatting`() {
        val title = "Conference planning with a very long Unicode title 東京"
        val event = CalendarEvent(
            eventId = 9,
            originalEventId = null,
            calendarId = 1,
            title = title,
            startsAt = Instant.parse("2026-08-10T00:00:00Z"),
            endsAt = Instant.parse("2026-08-12T00:00:00Z"),
            isAllDay = true,
            eventTimeZone = "UTC",
            calendarColor = 0xFF336699.toInt(),
        )
        val snapshot = snapshot(events = listOf(event))

        val result = presenter.present(WidgetLoadState.Ready(snapshot), snapshot(), now, 400)

        assertEquals(title, result.agenda.rows.single().title)
        assertEquals("ALL DAY", result.agenda.rows.single().temporalLabel)
    }

    @Test
    fun `empty and failure states retain month with correct agenda status`() {
        val empty = snapshot(events = emptyList())
        val states = listOf(
            WidgetLoadState.NoUpcomingEvents(empty) to CombinedAgendaStatus.EMPTY,
            WidgetLoadState.PermissionDenied(empty) to CombinedAgendaStatus.PERMISSION_DENIED,
            WidgetLoadState.NoAvailableCalendars(empty) to CombinedAgendaStatus.NO_CALENDARS,
            WidgetLoadState.ProviderFailure(CalendarReadFailure.PROVIDER_UNAVAILABLE, empty) to
                CombinedAgendaStatus.PROVIDER_FAILURE,
        )

        states.forEach { (state, expectedStatus) ->
            val result = presenter.present(state, snapshot(), now, 400)
            assertSame(empty.month, result.month)
            assertEquals(expectedStatus, result.agendaStatus)
            assertTrue(result.agenda.rows.isEmpty())
        }
    }

    @Test
    fun `permission snapshot exposes today without event indicators`() {
        val empty = WidgetSnapshotFactory.empty(now, ZoneId.of("UTC"), DayOfWeek.MONDAY)

        val result = presenter.present(WidgetLoadState.PermissionDenied(empty), snapshot(), now, 400)

        assertTrue(result.month.cells.any { it.isToday })
        assertTrue(result.month.cells.all { it.indicators.count == 0 })
    }

    @Test
    fun `multiple widget presentations do not leak capacity state`() {
        val manyEvents = snapshot(events = (1L..6L).map { event(it, 12 + it, 13 + it) })
        val empty = snapshot(events = emptyList())

        val first = presenter.present(WidgetLoadState.Ready(manyEvents), snapshot(), now, 400)
        val second = presenter.present(WidgetLoadState.NoUpcomingEvents(empty), snapshot(), now, 250)

        assertNotEquals(first.agenda.rows.size, second.agenda.rows.size)
        assertEquals(CombinedAgendaStatus.EMPTY, second.agendaStatus)
        assertEquals(0, second.agenda.hiddenEventCount)
    }

    private fun snapshot(
        events: List<CalendarEvent> = emptyList(),
    ): WidgetSnapshot = WidgetSnapshotFactory.create(
        month = YearMonth.of(2026, 8),
        today = LocalDate.of(2026, 8, 10),
        firstDayOfWeek = DayOfWeek.MONDAY,
        indicators = mapOf(LocalDate.of(2026, 8, 10) to DayIndicators(1, setOf(100))),
        agendaEvents = events,
        now = now,
    )

    private fun event(id: Long, startHour: Long, endHour: Long) = CalendarEvent(
        eventId = id,
        originalEventId = null,
        calendarId = 1,
        title = "Event $id",
        startsAt = now.plusSeconds((startHour - 12) * 3_600),
        endsAt = now.plusSeconds((endHour - 12) * 3_600),
        isAllDay = false,
        eventTimeZone = "UTC",
        calendarColor = null,
    )
}
