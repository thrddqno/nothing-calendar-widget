package com.adioquino.nothingcalendar.calendar.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarSelectionResolverTest {
    private val calendars = listOf(
        calendar(id = 1, visible = true, synced = true),
        calendar(id = 2, visible = false, synced = true),
        calendar(id = 3, visible = true, synced = false),
    )

    @Test
    fun `default includes only visible synchronized calendars`() {
        assertEquals(setOf(1L), CalendarSelectionResolver.resolve(calendars, null))
    }

    @Test
    fun `explicit selection can include hidden or unsynchronized calendars`() {
        assertEquals(setOf(2L, 3L), CalendarSelectionResolver.resolve(calendars, setOf(2, 3)))
    }

    @Test
    fun `removed calendars are discarded and new calendars are not added to explicit selection`() {
        assertEquals(setOf(1L), CalendarSelectionResolver.resolve(calendars, setOf(1, 99)))
    }

    @Test
    fun `empty explicit selection stays empty`() {
        assertEquals(emptySet<Long>(), CalendarSelectionResolver.resolve(calendars, emptySet()))
    }

    private fun calendar(id: Long, visible: Boolean, synced: Boolean) = DeviceCalendar(
        id = id,
        name = "Calendar $id",
        accountName = "account$id@example.test",
        accountType = "test",
        color = null,
        isVisible = visible,
        syncEvents = synced,
    )
}
