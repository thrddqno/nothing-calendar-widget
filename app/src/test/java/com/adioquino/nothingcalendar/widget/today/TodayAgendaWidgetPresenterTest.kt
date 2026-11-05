package com.adioquino.nothingcalendar.widget.today

import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayAgendaWidgetPresenterTest {
    private val now = Instant.parse("2026-11-05T12:00:00Z")
    private val presenter = TodayAgendaWidgetPresenter(
        datePresenter = TodayDatePresenter(Locale.US, ZoneId.of("UTC")),
        agendaPresenter = AgendaWidgetPresenter(
            AgendaEventFormatter(
                locale = Locale.US,
                zoneId = ZoneId.of("UTC"),
                use24HourTime = true,
                strings = AgendaFormatStrings("ALL DAY", "NOW", "Untitled event"),
                includeEndTime = true,
            ),
        ),
    )

    @Test
    fun `ready agenda orders ongoing and future events and excludes completed`() {
        val completed = event(1, -2, 0)
        val ongoing = event(2, -1, 1)
        val future = event(3, 2, 3)
        val snapshot = snapshot(listOf(future, completed, ongoing))

        val result = present(WidgetLoadState.Ready(snapshot), 250)

        assertEquals(listOf(2L, 3L), result.agenda.rows.map { it.eventId })
        assertEquals("NOW", result.agenda.rows.first().temporalLabel)
        assertEquals(now.plusSeconds(7_200), result.agenda.rows.last().startsAt)
    }

    @Test
    fun `timed events include compact start and end time`() {
        val snapshot = snapshot(listOf(event(1, 1, 2)))

        val result = present(WidgetLoadState.Ready(snapshot), 250)

        assertEquals("13:00–14:00", result.agenda.rows.single().temporalLabel)
    }

    @Test
    fun `all-day multi-day event retains all-day semantics`() {
        val allDay = CalendarEvent(
            eventId = 8,
            originalEventId = null,
            calendarId = 1,
            title = "Conference",
            startsAt = Instant.parse("2026-11-06T00:00:00Z"),
            endsAt = Instant.parse("2026-11-08T00:00:00Z"),
            isAllDay = true,
            eventTimeZone = "UTC",
            calendarColor = null,
        )

        val result = present(WidgetLoadState.Ready(snapshot(listOf(allDay))), 250)

        assertEquals("FRI ALL DAY", result.agenda.rows.single().temporalLabel)
    }

    @Test
    fun `empty permission no-calendar and provider states preserve current date`() {
        val empty = snapshot(emptyList())
        val states = listOf(
            WidgetLoadState.NoUpcomingEvents(empty) to TodayAgendaStatus.EMPTY,
            WidgetLoadState.PermissionDenied(empty) to TodayAgendaStatus.PERMISSION_DENIED,
            WidgetLoadState.NoAvailableCalendars(empty) to TodayAgendaStatus.NO_CALENDARS,
            WidgetLoadState.ProviderFailure(CalendarReadFailure.PROVIDER_UNAVAILABLE, empty) to
                TodayAgendaStatus.PROVIDER_FAILURE,
        )

        states.forEach { (state, expectedStatus) ->
            val result = present(state, 250)
            assertEquals(TodayDateModel("NOV", "05", "THU"), result.date)
            assertEquals(expectedStatus, result.agendaStatus)
            assertTrue(result.agenda.rows.isEmpty())
        }
    }

    @Test
    fun `multiple widget widths do not leak capacity state`() {
        val snapshot = snapshot((1L..4L).map { event(it, it, it + 1) })

        val normal = present(WidgetLoadState.Ready(snapshot), 250)
        val compact = present(WidgetLoadState.Ready(snapshot), 220)

        assertNotEquals(normal.agenda.rows.size, compact.agenda.rows.size)
        assertEquals(3, normal.agenda.rows.size)
        assertEquals(2, compact.agenda.rows.size)
    }

    private fun present(state: WidgetLoadState, widthDp: Int) = presenter.present(
        state = state,
        loadingSnapshot = snapshot(emptyList()),
        now = now,
        availableWidthDp = widthDp,
        availableHeightDp = 110,
        fontScale = 1f,
    )

    private fun snapshot(events: List<CalendarEvent>): WidgetSnapshot = WidgetSnapshotFactory.create(
        month = YearMonth.of(2026, 11),
        today = LocalDate.of(2026, 11, 5),
        firstDayOfWeek = DayOfWeek.MONDAY,
        indicators = emptyMap(),
        agendaEvents = events,
        now = now,
    )

    private fun event(id: Long, startOffsetHours: Long, endOffsetHours: Long) = CalendarEvent(
        eventId = id,
        originalEventId = null,
        calendarId = 1,
        title = "Event $id",
        startsAt = now.plusSeconds(startOffsetHours * 3_600),
        endsAt = now.plusSeconds(endOffsetHours * 3_600),
        isAllDay = false,
        eventTimeZone = "UTC",
        calendarColor = null,
    )
}
