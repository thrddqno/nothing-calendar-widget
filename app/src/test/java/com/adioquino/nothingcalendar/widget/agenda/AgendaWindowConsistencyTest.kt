package com.adioquino.nothingcalendar.widget.agenda

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.widget.combined.CombinedWidgetPresenter
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import com.adioquino.nothingcalendar.widget.today.TodayAgendaWidgetPresenter
import com.adioquino.nothingcalendar.widget.today.TodayDatePresenter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AgendaWindowConsistencyTest {
    private val now = Instant.parse("2026-11-05T20:00:00Z")
    private val agendaPresenter = AgendaWidgetPresenter(
        AgendaEventFormatter(
            locale = Locale.US,
            zoneId = ZoneId.of("UTC"),
            use24HourTime = false,
            strings = AgendaFormatStrings("ALL DAY", "NOW", "Untitled event"),
        ),
    )

    @Test
    fun `all three agenda widgets use identical rolling-window eligibility`() {
        val inside = event(1, now.plusSeconds(3_600), now.plusSeconds(7_200))
        val boundary = event(2, now.plusSeconds(43_200), now.plusSeconds(46_800))
        val snapshot = WidgetSnapshotFactory.create(
            month = YearMonth.of(2026, 11),
            today = LocalDate.of(2026, 11, 5),
            firstDayOfWeek = DayOfWeek.MONDAY,
            indicators = emptyMap(),
            agendaEvents = listOf(boundary, inside),
            now = now,
        )
        val state = WidgetLoadState.Ready(snapshot)

        val agenda = agendaPresenter.present(snapshot.agenda.occurrences, now, 150).agendaIds()
        val combined = CombinedWidgetPresenter(agendaPresenter)
            .present(state, snapshot, now, 400).agenda.agendaIds()
        val today = TodayAgendaWidgetPresenter(
            TodayDatePresenter(Locale.US, ZoneId.of("UTC")),
            agendaPresenter,
        ).present(state, snapshot, now, 250, 110, 1f).agenda.agendaIds()

        assertEquals(listOf(1L), agenda)
        assertEquals(agenda, combined)
        assertEquals(agenda, today)
    }

    private fun AgendaRenderModel.agendaIds(): List<Long> {
        assertEquals(1, totalEligibleEvents)
        assertEquals(0, hiddenEventCount)
        return rows.map(AgendaRowModel::eventId)
    }

    private fun event(id: Long, start: Instant, end: Instant) = CalendarEvent(
        eventId = id,
        originalEventId = null,
        calendarId = 1,
        title = "Event $id",
        startsAt = start,
        endsAt = end,
        isAllDay = false,
        eventTimeZone = "UTC",
        calendarColor = null,
    )
}
