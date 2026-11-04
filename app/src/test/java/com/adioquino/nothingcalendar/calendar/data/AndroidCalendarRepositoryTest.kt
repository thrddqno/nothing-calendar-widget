package com.adioquino.nothingcalendar.calendar.data

import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange
import com.adioquino.nothingcalendar.calendar.domain.DeviceCalendar
import com.adioquino.nothingcalendar.settings.SettingsRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidCalendarRepositoryTest {
    private val now = Instant.parse("2026-05-10T12:00:00Z")
    private val visibleCalendar = calendar(1, visible = true, synced = true)

    @Test
    fun `returns permission denied without querying instances`() = runBlocking {
        val dataSource = FakeCalendarDataSource(
            calendarsResult = ProviderQueryResult.PermissionDenied,
        )
        val repository = repository(dataSource)

        val result = repository.upcomingEvents(limit = 5)

        assertEquals(CalendarReadResult.PermissionDenied, result)
        assertTrue(dataSource.queriedCalendarIds.isEmpty())
    }

    @Test
    fun `returns empty events when no calendars exist`() = runBlocking {
        val dataSource = FakeCalendarDataSource(
            calendarsResult = ProviderQueryResult.Success(emptyList()),
        )
        val repository = repository(dataSource)

        val result = repository.upcomingEvents(limit = 5)

        assertEquals(CalendarReadResult.Success(emptyList<CalendarEvent>()), result)
        assertEquals(listOf(emptySet<Long>()), dataSource.queriedCalendarIds)
    }

    @Test
    fun `includes ongoing events and excludes completed occurrences`() = runBlocking {
        val completed = event(1, "2026-05-10T10:00:00Z", "2026-05-10T11:00:00Z")
        val ongoing = event(2, "2026-05-10T11:00:00Z", "2026-05-10T13:00:00Z")
        val future = event(3, "2026-05-10T14:00:00Z", "2026-05-10T15:00:00Z")
        val dataSource = FakeCalendarDataSource(
            calendarsResult = ProviderQueryResult.Success(listOf(visibleCalendar)),
            instances = listOf(future, completed, ongoing),
        )

        val result = repository(dataSource).upcomingEvents(limit = 5)

        assertEquals(
            listOf(ongoing, future),
            (result as CalendarReadResult.Success).value,
        )
    }

    @Test
    fun `uses same persisted filter for agenda and month`() = runBlocking {
        val secondCalendar = calendar(2, visible = true, synced = true)
        val dataSource = FakeCalendarDataSource(
            calendarsResult = ProviderQueryResult.Success(listOf(visibleCalendar, secondCalendar)),
        )
        val settings = FakeSettingsRepository(selectedIds = setOf(2))
        val repository = repository(dataSource, settings)

        repository.upcomingEvents(limit = 5)
        repository.monthIndicators(
            month = java.time.YearMonth.of(2026, 5),
            zoneId = ZoneId.of("UTC"),
        )

        assertEquals(listOf(setOf(2L), setOf(2L)), dataSource.queriedCalendarIds)
    }

    @Test
    fun `rejects unbounded agenda lookahead`() = runBlocking {
        val repository = repository(
            FakeCalendarDataSource(ProviderQueryResult.Success(listOf(visibleCalendar))),
        )

        val result = repository.upcomingEvents(limit = 5, lookahead = Duration.ofDays(367))

        assertEquals(
            CalendarReadResult.Failure(CalendarReadFailure.INVALID_QUERY),
            result,
        )
    }

    private fun repository(
        dataSource: FakeCalendarDataSource,
        settingsRepository: SettingsRepository = FakeSettingsRepository(),
    ) = AndroidCalendarRepository(
        dataSource = dataSource,
        settingsRepository = settingsRepository,
        clock = Clock.fixed(now, ZoneOffset.UTC),
        ioDispatcher = Dispatchers.Unconfined,
    )

    private fun event(id: Long, start: String, end: String) = CalendarEvent(
        eventId = id,
        originalEventId = null,
        calendarId = 1,
        title = "Test",
        startsAt = Instant.parse(start),
        endsAt = Instant.parse(end),
        isAllDay = false,
        eventTimeZone = "UTC",
        calendarColor = null,
    )

    private fun calendar(id: Long, visible: Boolean, synced: Boolean) = DeviceCalendar(
        id = id,
        name = "Calendar $id",
        accountName = "account$id@example.test",
        accountType = "test",
        color = null,
        isVisible = visible,
        syncEvents = synced,
    )

    private class FakeCalendarDataSource(
        private val calendarsResult: ProviderQueryResult<List<DeviceCalendar>>,
        private val instances: List<CalendarEvent> = emptyList(),
    ) : CalendarDataSource {
        val queriedCalendarIds = mutableListOf<Set<Long>>()

        override fun queryCalendars(): ProviderQueryResult<List<DeviceCalendar>> = calendarsResult

        override fun queryInstances(
            range: CalendarQueryRange,
            calendarIds: Set<Long>,
        ): ProviderQueryResult<List<CalendarEvent>> {
            queriedCalendarIds += calendarIds
            return ProviderQueryResult.Success(
                instances.filter { event ->
                    event.calendarId in calendarIds &&
                        event.endsAt.isAfter(range.startsAt) &&
                        event.startsAt.isBefore(range.endsAt)
                },
            )
        }
    }

    private class FakeSettingsRepository(
        selectedIds: Set<Long>? = null,
    ) : SettingsRepository {
        private val dynamicColors = MutableStateFlow(true)
        private val selection = MutableStateFlow(selectedIds)

        override val useDynamicColors: Flow<Boolean> = dynamicColors
        override val selectedCalendarIds: Flow<Set<Long>?> = selection

        override suspend fun setUseDynamicColors(enabled: Boolean) {
            dynamicColors.value = enabled
        }

        override suspend fun setSelectedCalendarIds(calendarIds: Set<Long>) {
            selection.value = calendarIds
        }

        override suspend fun useDefaultCalendarSelection() {
            selection.value = null
        }
    }
}
