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
    fun `all three agenda widgets use identical ordering filtering and deduplication`() {
        val ongoing = event(1, now.minusSeconds(3_600), now.plusSeconds(1_800))
        val future = event(2, now.plusSeconds(1_800), now.plusSeconds(3_600))
        val allDay = event(
            3,
            now.plusSeconds(3_600),
            now.plusSeconds(90_000),
            isAllDay = true,
        )
        val overnight = event(4, now.plusSeconds(7_200), now.plusSeconds(50_000))
        val recurringFirst = event(5, now.plusSeconds(10_800), now.plusSeconds(12_600), originalId = 50)
        val recurringSecond = event(5, now.plusSeconds(14_400), now.plusSeconds(16_200), originalId = 50)
        val completed = event(6, now.minusSeconds(7_200), now.minusSeconds(1))
        val boundary = event(7, now.plusSeconds(43_200), now.plusSeconds(46_800))
        val snapshot = WidgetSnapshotFactory.create(
            month = YearMonth.of(2026, 11),
            today = LocalDate.of(2026, 11, 5),
            firstDayOfWeek = DayOfWeek.MONDAY,
            indicators = emptyMap(),
            agendaEvents = listOf(
                boundary,
                recurringSecond,
                completed,
                overnight,
                recurringFirst,
                allDay,
                future,
                ongoing,
                recurringFirst,
            ),
            now = now,
        )
        val state = WidgetLoadState.Ready(snapshot)

        val canonical = agendaPresenter.presentWithCapacity(snapshot.agenda.occurrences, now) { count ->
            AgendaCapacity(count, showOverflow = false, hiddenEventCount = 0)
        }
        val agenda = agendaPresenter.present(snapshot.agenda.occurrences, now, 300)
        val combined = CombinedWidgetPresenter(agendaPresenter)
            .present(state, snapshot, now, 400).agenda
        val today = TodayAgendaWidgetPresenter(
            TodayDatePresenter(Locale.US, ZoneId.of("UTC")),
            agendaPresenter,
        ).present(state, snapshot, now, 250, 110, 1f).agenda

        assertEquals(6, canonical.totalEligibleEvents)
        assertPrefixOf(canonical, agenda)
        assertPrefixOf(canonical, combined)
        assertPrefixOf(canonical, today)
        assertEquals(canonical.occurrenceIds(), combined.occurrenceIds())
    }

    private fun assertPrefixOf(expected: AgendaRenderModel, actual: AgendaRenderModel) {
        assertEquals(expected.totalEligibleEvents, actual.totalEligibleEvents)
        assertEquals(expected.occurrenceIds().take(actual.rows.size), actual.occurrenceIds())
    }

    private fun AgendaRenderModel.occurrenceIds(): List<String> =
        rows.map(AgendaRowModel::occurrenceId)

    private fun event(
        id: Long,
        start: Instant,
        end: Instant,
        isAllDay: Boolean = false,
        originalId: Long? = null,
    ) = CalendarEvent(
        eventId = id,
        originalEventId = originalId,
        calendarId = 1,
        title = "Event $id",
        startsAt = start,
        endsAt = end,
        isAllDay = isAllDay,
        eventTimeZone = "UTC",
        calendarColor = null,
    )
}
