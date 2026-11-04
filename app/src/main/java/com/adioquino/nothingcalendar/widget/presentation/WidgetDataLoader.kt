package com.adioquino.nothingcalendar.widget.presentation

import com.adioquino.nothingcalendar.calendar.data.CalendarPermissionChecker
import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import com.adioquino.nothingcalendar.calendar.data.CalendarReadResult
import com.adioquino.nothingcalendar.calendar.data.CalendarRepository
import com.adioquino.nothingcalendar.calendar.domain.CalendarEventProcessor
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange
import com.adioquino.nothingcalendar.calendar.domain.CalendarSelectionResolver
import com.adioquino.nothingcalendar.settings.SettingsRepository
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class WidgetDataLoader(
    private val calendarRepository: CalendarRepository,
    private val settingsRepository: SettingsRepository,
    private val permissionChecker: CalendarPermissionChecker,
    private val clock: Clock = Clock.systemUTC(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val agendaLookahead: Duration = Duration.ofDays(90),
) {
    init {
        require(!agendaLookahead.isZero && !agendaLookahead.isNegative) {
            "Agenda lookahead must be positive"
        }
        require(agendaLookahead <= Duration.ofDays(366)) {
            "Agenda lookahead must not exceed 366 days"
        }
    }

    suspend fun load(): WidgetLoadState = withContext(ioDispatcher) {
        if (!permissionChecker.canReadCalendar()) return@withContext WidgetLoadState.PermissionDenied

        try {
            loadSnapshot()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            WidgetLoadState.ProviderFailure(CalendarReadFailure.PROVIDER_UNAVAILABLE)
        }
    }

    private suspend fun loadSnapshot(): WidgetLoadState {
        val calendars = when (val result = calendarRepository.calendars()) {
            is CalendarReadResult.Success -> result.value
            CalendarReadResult.PermissionDenied -> return WidgetLoadState.PermissionDenied
            is CalendarReadResult.Failure -> return WidgetLoadState.ProviderFailure(result.reason)
        }
        val (persistedSelection, firstDayOfWeek) = combine(
            settingsRepository.selectedCalendarIds,
            settingsRepository.firstDayOfWeek,
        ) { selectedIds, firstDay -> selectedIds to firstDay }.first()
        val selectedIds = CalendarSelectionResolver.resolve(calendars, persistedSelection)
        if (selectedIds.isEmpty()) return WidgetLoadState.NoAvailableCalendars

        val now = clock.instant()
        val today = now.atZone(zoneId).toLocalDate()
        val month = YearMonth.from(today)
        val visibleRange = WidgetSnapshotFactory.visibleDateRange(month, firstDayOfWeek)
        val eventRange = CalendarQueryRange(
            visibleRange.start.atStartOfDay(zoneId).toInstant(),
            visibleRange.endExclusive.atStartOfDay(zoneId).toInstant(),
        )
        val monthEvents = when (val result = calendarRepository.events(eventRange, selectedIds)) {
            is CalendarReadResult.Success -> result.value
            CalendarReadResult.PermissionDenied -> return WidgetLoadState.PermissionDenied
            is CalendarReadResult.Failure -> return WidgetLoadState.ProviderFailure(result.reason)
        }
        val agendaEnd = now.plus(agendaLookahead)
        val agendaRange = CalendarQueryRange(now, agendaEnd)
        val agendaEvents = when (val result = calendarRepository.events(agendaRange, selectedIds)) {
            is CalendarReadResult.Success -> CalendarEventProcessor.upcoming(
                events = result.value,
                nowEpochMillis = now.toEpochMilli(),
                rangeEndEpochMillis = agendaEnd.toEpochMilli(),
                limit = Int.MAX_VALUE,
            )
            CalendarReadResult.PermissionDenied -> return WidgetLoadState.PermissionDenied
            is CalendarReadResult.Failure -> return WidgetLoadState.ProviderFailure(result.reason)
        }
        val indicators = CalendarEventProcessor.dateIndicators(
            events = monthEvents,
            firstDate = visibleRange.start,
            endDateExclusive = visibleRange.endExclusive,
            zoneId = zoneId,
        )
        val snapshot = WidgetSnapshotFactory.create(
            month = month,
            today = today,
            firstDayOfWeek = firstDayOfWeek,
            indicators = indicators,
            agendaEvents = agendaEvents,
            now = now,
        )
        return if (agendaEvents.isEmpty()) {
            WidgetLoadState.NoUpcomingEvents(snapshot)
        } else {
            WidgetLoadState.Ready(snapshot)
        }
    }
}
