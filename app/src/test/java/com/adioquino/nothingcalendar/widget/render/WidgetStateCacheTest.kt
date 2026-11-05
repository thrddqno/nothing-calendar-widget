package com.adioquino.nothingcalendar.widget.render

import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class WidgetStateCacheTest {
    private val snapshot = WidgetSnapshotFactory.empty(
        now = Instant.parse("2026-11-05T20:00:00Z"),
        zoneId = ZoneId.of("UTC"),
        firstDayOfWeek = DayOfWeek.MONDAY,
    )

    @Test
    fun `ready state is reused for a resize`() {
        val cache = WidgetStateCache()
        val state = WidgetLoadState.Ready(snapshot)

        cache.put(7, state)

        assertSame(state, cache.get(7))
    }

    @Test
    fun `no upcoming events state is reused for a resize`() {
        val cache = WidgetStateCache()
        val state = WidgetLoadState.NoUpcomingEvents(snapshot)

        cache.put(7, state)

        assertSame(state, cache.get(7))
    }

    @Test
    fun `error and loading states are not cached`() {
        val cache = WidgetStateCache()

        cache.put(1, WidgetLoadState.Loading)
        cache.put(2, WidgetLoadState.PermissionDenied(snapshot))
        cache.put(3, WidgetLoadState.NoAvailableCalendars(snapshot))
        cache.put(
            4,
            WidgetLoadState.ProviderFailure(CalendarReadFailure.PROVIDER_UNAVAILABLE, snapshot),
        )

        assertNull(cache.get(1))
        assertNull(cache.get(2))
        assertNull(cache.get(3))
        assertNull(cache.get(4))
    }

    @Test
    fun `permission denial evicts previously cached private snapshot`() {
        val cache = WidgetStateCache()
        cache.put(7, WidgetLoadState.Ready(snapshot))

        cache.put(7, WidgetLoadState.PermissionDenied(snapshot))

        assertNull(cache.get(7))
    }

    @Test
    fun `provider failure evicts stale cached snapshot`() {
        val cache = WidgetStateCache()
        cache.put(7, WidgetLoadState.Ready(snapshot))

        cache.put(
            7,
            WidgetLoadState.ProviderFailure(CalendarReadFailure.PROVIDER_UNAVAILABLE, snapshot),
        )

        assertNull(cache.get(7))
    }

    @Test
    fun `deleting one widget keeps other widget snapshots`() {
        val cache = WidgetStateCache()
        cache.put(1, WidgetLoadState.Ready(snapshot))
        cache.put(2, WidgetLoadState.Ready(snapshot))

        cache.invalidate(intArrayOf(1))

        assertNull(cache.get(1))
        assertEquals(WidgetLoadState.Ready(snapshot), cache.get(2))
    }

    @Test
    fun `multiple instances never share cached state`() {
        val cache = WidgetStateCache()
        val first = WidgetLoadState.Ready(snapshot)
        val second = WidgetLoadState.NoUpcomingEvents(snapshot.copy(generatedAt = Instant.EPOCH))

        cache.put(10, first)
        cache.put(11, second)

        assertSame(first, cache.get(10))
        assertSame(second, cache.get(11))
    }

    @Test
    fun `snapshot from a previous month is not reused for resize`() {
        val cache = WidgetStateCache()
        cache.put(7, WidgetLoadState.Ready(snapshot))

        val nextMonth = YearMonth.from(snapshot.generatedAt.atZone(ZoneId.of("UTC")))
            .plusMonths(1)
            .atDay(1)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()

        assertNull(cache.getValid(7, nextMonth, ZoneId.of("UTC")))
    }

    @Test
    fun `current-month snapshot is valid for resize`() {
        val cache = WidgetStateCache()
        val state = WidgetLoadState.Ready(snapshot)
        cache.put(7, state)

        assertSame(
            state,
            cache.getValid(7, snapshot.generatedAt.plusSeconds(60), ZoneId.of("UTC")),
        )
    }
}
