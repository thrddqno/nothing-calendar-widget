package com.adioquino.nothingcalendar.widget.presentation

import com.adioquino.nothingcalendar.calendar.data.CalendarPermissionChecker
import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import com.adioquino.nothingcalendar.calendar.data.CalendarReadResult
import com.adioquino.nothingcalendar.calendar.data.CalendarRepository
import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange
import com.adioquino.nothingcalendar.calendar.domain.DeviceCalendar
import com.adioquino.nothingcalendar.calendar.domain.MonthIndicators
import com.adioquino.nothingcalendar.settings.SettingsRepository
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetDataLoaderTest {
    private val now = Instant.parse("2026-05-10T12:00:00Z")
    private val calendar = DeviceCalendar(
        id = 1,
        name = "Calendar",
        accountName = "account@example.test",
        accountType = "test",
        color = 100,
        isVisible = true,
        syncEvents = true,
    )

    @Test
    fun `permission denial avoids repository queries`() = runBlocking {
        val repository = FakeCalendarRepository()
        val loader = loader(repository, permissionGranted = false)

        assertEquals(WidgetLoadState.PermissionDenied, loader.load())
        assertEquals(0, repository.calendarCalls)
    }

    @Test
    fun `empty calendars produce no available calendars`() = runBlocking {
        val repository = FakeCalendarRepository(
            calendarsResult = CalendarReadResult.Success(emptyList()),
        )

        assertEquals(WidgetLoadState.NoAvailableCalendars, loader(repository).load())
        assertEquals(0, repository.eventCalls)
    }

    @Test
    fun `removed selected calendar produces no available calendars`() = runBlocking {
        val repository = FakeCalendarRepository(
            calendarsResult = CalendarReadResult.Success(listOf(calendar)),
        )
        val settings = FakeSettingsRepository(selectedIds = setOf(99))

        assertEquals(
            WidgetLoadState.NoAvailableCalendars,
            loader(repository, settingsRepository = settings).load(),
        )
    }

    @Test
    fun `provider failure remains distinguishable from empty data`() = runBlocking {
        val repository = FakeCalendarRepository(
            calendarsResult = CalendarReadResult.Failure(CalendarReadFailure.PROVIDER_UNAVAILABLE),
        )

        assertEquals(
            WidgetLoadState.ProviderFailure(CalendarReadFailure.PROVIDER_UNAVAILABLE),
            loader(repository).load(),
        )
    }

    @Test
    fun `empty agenda returns snapshot with no upcoming state`() = runBlocking {
        val repository = FakeCalendarRepository(
            calendarsResult = CalendarReadResult.Success(listOf(calendar)),
        )

        val result = loader(repository).load() as WidgetLoadState.NoUpcomingEvents

        assertEquals(YearMonth.of(2026, 5), result.snapshot.month.displayedMonth)
        assertEquals(0, result.snapshot.agenda.totalEligibleEvents)
        assertEquals(42, result.snapshot.month.cells.size)
    }

    @Test
    fun `ready snapshot carries real occurrence and month indicator`() = runBlocking {
        val event = event("2026-05-10T11:00:00Z", "2026-05-10T13:00:00Z")
        val repository = FakeCalendarRepository(
            calendarsResult = CalendarReadResult.Success(listOf(calendar)),
            eventsResult = CalendarReadResult.Success(listOf(event)),
            upcomingResult = CalendarReadResult.Success(listOf(event)),
        )

        val result = loader(repository).load() as WidgetLoadState.Ready

        assertEquals(event.instanceId, result.snapshot.agenda.occurrences.single().occurrenceId)
        assertTrue(result.snapshot.agenda.occurrences.single().isOngoing)
        assertEquals(
            1,
            result.snapshot.month.cells.single { it.date.toString() == "2026-05-10" }.indicators.count,
        )
        assertEquals(listOf(setOf(1L), setOf(1L)), repository.queriedCalendarIds)
    }

    @Test
    fun `device zone controls today month and visible query boundaries`() = runBlocking {
        val instant = Instant.parse("2026-05-01T01:00:00Z")
        val zone = ZoneId.of("America/Los_Angeles")
        val repository = FakeCalendarRepository(
            calendarsResult = CalendarReadResult.Success(listOf(calendar)),
        )
        val loader = loader(
            repository = repository,
            clock = Clock.fixed(instant, ZoneOffset.UTC),
            zoneId = zone,
        )

        val result = loader.load() as WidgetLoadState.NoUpcomingEvents

        assertEquals(YearMonth.of(2026, 4), result.snapshot.month.displayedMonth)
        assertEquals(
            WidgetSnapshotFactory.visibleDateRange(YearMonth.of(2026, 4), DayOfWeek.MONDAY)
                .start.atStartOfDay(zone).toInstant(),
            repository.eventRanges.first().startsAt,
        )
    }

    @Test
    fun `provider calls run on injected IO dispatcher`() {
        val executor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "widget-test-io")
        }
        executor.asCoroutineDispatcher().use { dispatcher ->
            val repository = FakeCalendarRepository(
                calendarsResult = CalendarReadResult.Success(listOf(calendar)),
            )

            runBlocking { loader(repository, ioDispatcher = dispatcher).load() }

            assertTrue(repository.queryThreads.isNotEmpty())
            assertTrue(repository.queryThreads.all { it.startsWith("widget-test-io") })
            assertFalse(repository.queryThreads.any { it == Thread.currentThread().name })
        }
    }

    @Test
    fun `concurrent loads complete without shared mutable failures`() = runBlocking {
        val repository = FakeCalendarRepository(
            calendarsResult = CalendarReadResult.Success(listOf(calendar)),
        )
        val loader = loader(repository, ioDispatcher = Dispatchers.Default)

        val states = coroutineScope {
            List(20) { async { loader.load() } }.awaitAll()
        }

        assertEquals(20, states.size)
        assertTrue(states.all { it is WidgetLoadState.NoUpcomingEvents })
    }

    private fun loader(
        repository: FakeCalendarRepository,
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
        permissionGranted: Boolean = true,
        clock: Clock = Clock.fixed(now, ZoneOffset.UTC),
        zoneId: ZoneId = ZoneId.of("UTC"),
        ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.Unconfined,
    ) = WidgetDataLoader(
        calendarRepository = repository,
        settingsRepository = settingsRepository,
        permissionChecker = CalendarPermissionChecker { permissionGranted },
        clock = clock,
        zoneId = zoneId,
        ioDispatcher = ioDispatcher,
    )

    private fun event(start: String, end: String) = CalendarEvent(
        eventId = 10,
        originalEventId = 5,
        calendarId = 1,
        title = "Real test occurrence",
        startsAt = Instant.parse(start),
        endsAt = Instant.parse(end),
        isAllDay = false,
        eventTimeZone = "UTC",
        calendarColor = 100,
    )

    private class FakeCalendarRepository(
        private val calendarsResult: CalendarReadResult<List<DeviceCalendar>> =
            CalendarReadResult.Success(emptyList()),
        private val eventsResult: CalendarReadResult<List<CalendarEvent>> =
            CalendarReadResult.Success(emptyList()),
        private val upcomingResult: CalendarReadResult<List<CalendarEvent>> =
            CalendarReadResult.Success(emptyList()),
    ) : CalendarRepository {
        @Volatile var calendarCalls = 0
        @Volatile var eventCalls = 0
        val eventRanges = java.util.Collections.synchronizedList(mutableListOf<CalendarQueryRange>())
        val queriedCalendarIds = java.util.Collections.synchronizedList(mutableListOf<Set<Long>>())
        val queryThreads = java.util.Collections.synchronizedList(mutableListOf<String>())

        override suspend fun calendars(): CalendarReadResult<List<DeviceCalendar>> {
            calendarCalls += 1
            queryThreads += Thread.currentThread().name
            return calendarsResult
        }

        override suspend fun events(
            range: CalendarQueryRange,
        ): CalendarReadResult<List<CalendarEvent>> {
            eventCalls += 1
            eventRanges += range
            queryThreads += Thread.currentThread().name
            return eventsResult
        }

        override suspend fun events(
            range: CalendarQueryRange,
            calendarIds: Set<Long>,
        ): CalendarReadResult<List<CalendarEvent>> {
            eventCalls += 1
            eventRanges += range
            queriedCalendarIds += calendarIds
            queryThreads += Thread.currentThread().name
            return if (eventCalls == 1) eventsResult else upcomingResult
        }

        override suspend fun upcomingEvents(
            limit: Int,
            lookahead: Duration,
        ): CalendarReadResult<List<CalendarEvent>> {
            queryThreads += Thread.currentThread().name
            return upcomingResult
        }

        override suspend fun monthIndicators(
            month: YearMonth,
            zoneId: ZoneId,
        ): CalendarReadResult<MonthIndicators> = error("Not used by widget loader")
    }

    private class FakeSettingsRepository(
        selectedIds: Set<Long>? = null,
        firstDay: DayOfWeek = DayOfWeek.MONDAY,
    ) : SettingsRepository {
        private val dynamicColors = MutableStateFlow(true)
        private val selection = MutableStateFlow(selectedIds)
        private val weekStart = MutableStateFlow(firstDay)

        override val useDynamicColors: Flow<Boolean> = dynamicColors
        override val selectedCalendarIds: Flow<Set<Long>?> = selection
        override val firstDayOfWeek: Flow<DayOfWeek> = weekStart

        override suspend fun setUseDynamicColors(enabled: Boolean) {
            dynamicColors.value = enabled
        }

        override suspend fun setSelectedCalendarIds(calendarIds: Set<Long>) {
            selection.value = calendarIds
        }

        override suspend fun useDefaultCalendarSelection() {
            selection.value = null
        }

        override suspend fun setFirstDayOfWeek(firstDayOfWeek: DayOfWeek) {
            weekStart.value = firstDayOfWeek
        }
    }
}
