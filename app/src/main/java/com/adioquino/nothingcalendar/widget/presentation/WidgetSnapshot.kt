package com.adioquino.nothingcalendar.widget.presentation

import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

data class WidgetSnapshot(
    val month: WidgetMonthState,
    val agenda: WidgetAgendaState,
    val generatedAt: Instant,
)

data class WidgetMonthState(
    val displayedMonth: YearMonth,
    val today: LocalDate,
    val firstDayOfWeek: DayOfWeek,
    val weekCount: Int,
    val cells: List<WidgetMonthCell>,
)

data class WidgetMonthCell(
    val date: LocalDate,
    val belongsToDisplayedMonth: Boolean,
    val isToday: Boolean,
    val indicators: WidgetEventIndicators,
)

data class WidgetEventIndicators(
    val count: Int,
    val calendarColors: Set<Int>,
)

data class WidgetAgendaState(
    val occurrences: List<WidgetAgendaOccurrence>,
    val totalEligibleEvents: Int,
)

data class WidgetAgendaOccurrence(
    val eventId: Long,
    val occurrenceId: String,
    val title: String,
    val startsAt: Instant,
    val endsAt: Instant,
    val isAllDay: Boolean,
    val calendarColor: Int?,
    val isOngoing: Boolean,
)

sealed interface WidgetLoadState {
    data object Loading : WidgetLoadState

    data class Ready(val snapshot: WidgetSnapshot) : WidgetLoadState

    data class NoUpcomingEvents(val snapshot: WidgetSnapshot) : WidgetLoadState

    data class PermissionDenied(val snapshot: WidgetSnapshot) : WidgetLoadState

    data class NoAvailableCalendars(val snapshot: WidgetSnapshot) : WidgetLoadState

    data class ProviderFailure(
        val reason: CalendarReadFailure,
        val snapshot: WidgetSnapshot,
    ) : WidgetLoadState
}
