package com.adioquino.nothingcalendar.calendar.data

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarEventProcessor
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange
import com.adioquino.nothingcalendar.calendar.domain.CalendarSelectionResolver
import com.adioquino.nothingcalendar.calendar.domain.DeviceCalendar
import com.adioquino.nothingcalendar.calendar.domain.MonthIndicators
import com.adioquino.nothingcalendar.settings.SettingsRepository
import java.time.Clock
import java.time.Duration
import java.time.YearMonth
import java.time.ZoneId
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AndroidCalendarRepository(
    private val dataSource: CalendarDataSource,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CalendarRepository {

    override suspend fun calendars(): CalendarReadResult<List<DeviceCalendar>> = withContext(ioDispatcher) {
        dataSource.queryCalendars().toReadResult()
    }

    override suspend fun events(
        range: CalendarQueryRange,
    ): CalendarReadResult<List<CalendarEvent>> = withContext(ioDispatcher) {
        querySelectedEvents(range).mapSuccess(CalendarEventProcessor::chronological)
    }

    override suspend fun events(
        range: CalendarQueryRange,
        calendarIds: Set<Long>,
    ): CalendarReadResult<List<CalendarEvent>> = withContext(ioDispatcher) {
        dataSource.queryInstances(range, calendarIds)
            .toReadResult()
            .mapSuccess(CalendarEventProcessor::chronological)
    }

    override suspend fun upcomingEvents(
        limit: Int,
        lookahead: Duration,
    ): CalendarReadResult<List<CalendarEvent>> = withContext(ioDispatcher) {
        if (limit < 0 || lookahead.isZero || lookahead.isNegative || lookahead > MAX_LOOKAHEAD) {
            return@withContext CalendarReadResult.Failure(CalendarReadFailure.INVALID_QUERY)
        }
        if (limit == 0) return@withContext CalendarReadResult.Success(emptyList())

        val now = clock.instant()
        val rangeEnd = now.plus(lookahead)
        val range = CalendarQueryRange(now, rangeEnd)
        querySelectedEvents(range).mapSuccess { events ->
            CalendarEventProcessor.upcoming(
                events = events,
                nowEpochMillis = now.toEpochMilli(),
                rangeEndEpochMillis = rangeEnd.toEpochMilli(),
                limit = limit,
            )
        }
    }

    override suspend fun monthIndicators(
        month: YearMonth,
        zoneId: ZoneId,
    ): CalendarReadResult<MonthIndicators> = withContext(ioDispatcher) {
        val start = month.atDay(1).atStartOfDay(zoneId).toInstant()
        val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant()
        querySelectedEvents(CalendarQueryRange(start, end)).mapSuccess { events ->
            CalendarEventProcessor.monthIndicators(events, month, zoneId)
        }
    }

    private suspend fun querySelectedEvents(
        range: CalendarQueryRange,
    ): CalendarReadResult<List<CalendarEvent>> {
        val calendars = when (val result = dataSource.queryCalendars()) {
            is ProviderQueryResult.Success -> result.value
            ProviderQueryResult.PermissionDenied -> return CalendarReadResult.PermissionDenied
            ProviderQueryResult.ProviderUnavailable -> {
                return CalendarReadResult.Failure(CalendarReadFailure.PROVIDER_UNAVAILABLE)
            }
        }
        val persistedSelection = try {
            settingsRepository.selectedCalendarIds.first()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            return CalendarReadResult.Failure(CalendarReadFailure.PROVIDER_UNAVAILABLE)
        }
        val selectedIds = CalendarSelectionResolver.resolve(calendars, persistedSelection)
        return dataSource.queryInstances(range, selectedIds).toReadResult()
    }

    private fun <T, R> CalendarReadResult<T>.mapSuccess(
        transform: (T) -> R,
    ): CalendarReadResult<R> = when (this) {
        is CalendarReadResult.Success -> CalendarReadResult.Success(transform(value))
        is CalendarReadResult.Failure -> this
        CalendarReadResult.PermissionDenied -> CalendarReadResult.PermissionDenied
    }

    private fun <T> ProviderQueryResult<T>.toReadResult(): CalendarReadResult<T> = when (this) {
        is ProviderQueryResult.Success -> CalendarReadResult.Success(value)
        ProviderQueryResult.PermissionDenied -> CalendarReadResult.PermissionDenied
        ProviderQueryResult.ProviderUnavailable -> {
            CalendarReadResult.Failure(CalendarReadFailure.PROVIDER_UNAVAILABLE)
        }
    }

    private companion object {
        val MAX_LOOKAHEAD: Duration = Duration.ofDays(366)
    }
}
