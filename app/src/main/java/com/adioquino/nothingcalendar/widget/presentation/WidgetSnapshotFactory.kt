package com.adioquino.nothingcalendar.widget.presentation

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarEventProcessor
import com.adioquino.nothingcalendar.calendar.domain.DayIndicators
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

object WidgetSnapshotFactory {
    const val MONTH_CELL_COUNT = 42

    fun visibleDateRange(
        month: YearMonth,
        firstDayOfWeek: DayOfWeek,
    ): ClosedOpenDateRange {
        requireSupportedFirstDay(firstDayOfWeek)
        val firstOfMonth = month.atDay(1)
        val leadingDays = Math.floorMod(
            firstOfMonth.dayOfWeek.value - firstDayOfWeek.value,
            DayOfWeek.entries.size,
        )
        val firstVisibleDate = firstOfMonth.minusDays(leadingDays.toLong())
        return ClosedOpenDateRange(
            start = firstVisibleDate,
            endExclusive = firstVisibleDate.plusDays(MONTH_CELL_COUNT.toLong()),
        )
    }

    fun create(
        month: YearMonth,
        today: LocalDate,
        firstDayOfWeek: DayOfWeek,
        indicators: Map<LocalDate, DayIndicators>,
        agendaEvents: List<CalendarEvent>,
        now: Instant,
    ): WidgetSnapshot {
        val range = visibleDateRange(month, firstDayOfWeek)
        val cells = List(MONTH_CELL_COUNT) { index ->
            val date = range.start.plusDays(index.toLong())
            val dayIndicators = indicators[date]
            WidgetMonthCell(
                date = date,
                belongsToDisplayedMonth = YearMonth.from(date) == month,
                isToday = date == today,
                indicators = WidgetEventIndicators(
                    count = dayIndicators?.count ?: 0,
                    calendarColors = dayIndicators?.calendarColors?.toSet().orEmpty(),
                ),
            )
        }
        val agenda = CalendarEventProcessor.chronological(agendaEvents).map { event ->
            WidgetAgendaOccurrence(
                eventId = event.eventId,
                occurrenceId = event.instanceId,
                title = event.title,
                startsAt = event.startsAt,
                endsAt = event.endsAt,
                isAllDay = event.isAllDay,
                calendarColor = event.calendarColor,
                isOngoing = !event.startsAt.isAfter(now) && event.endsAt.isAfter(now),
            )
        }.toList()

        return WidgetSnapshot(
            month = WidgetMonthState(
                displayedMonth = month,
                today = today,
                firstDayOfWeek = firstDayOfWeek,
                cells = cells,
            ),
            agenda = WidgetAgendaState(
                occurrences = agenda,
                totalEligibleEvents = agenda.size,
            ),
            generatedAt = now,
        )
    }

    private fun requireSupportedFirstDay(firstDayOfWeek: DayOfWeek) {
        require(firstDayOfWeek == DayOfWeek.MONDAY || firstDayOfWeek == DayOfWeek.SUNDAY) {
            "First day of week must be Monday or Sunday"
        }
    }
}

data class ClosedOpenDateRange(
    val start: LocalDate,
    val endExclusive: LocalDate,
)
