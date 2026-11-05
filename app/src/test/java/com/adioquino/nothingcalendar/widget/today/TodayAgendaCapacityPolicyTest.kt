package com.adioquino.nothingcalendar.widget.today

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayAgendaCapacityPolicyTest {
    @Test
    fun `one event uses one complete row`() {
        val result = TodayAgendaCapacityPolicy.calculate(250, 110, 1f, 1)

        assertEquals(1, result.visibleEventRows)
        assertEquals(26, result.eventRowHeightDp)
        assertFalse(result.showOverflow)
    }

    @Test
    fun `constrained width prefers two events`() {
        val result = TodayAgendaCapacityPolicy.calculate(220, 110, 1f, 3)

        assertTrue(result.compact)
        assertEquals(2, result.visibleEventRows)
    }

    @Test
    fun `normal dimensions display three events`() {
        val result = TodayAgendaCapacityPolicy.calculate(250, 110, 1f, 3)

        assertFalse(result.compact)
        assertEquals(3, result.visibleEventRows)
    }

    @Test
    fun `overflow equals undisplayed eligible events`() {
        val result = TodayAgendaCapacityPolicy.calculate(220, 110, 1f, 5)

        assertEquals(2, result.visibleEventRows)
        assertEquals(3, result.hiddenEventCount)
        assertTrue(result.showOverflow)
    }

    @Test
    fun `large font scale selects compact capacity`() {
        val result = TodayAgendaCapacityPolicy.calculate(250, 110, 1.3f, 4)

        assertTrue(result.compact)
        assertEquals(2, result.visibleEventRows)
        assertTrue(result.showOverflow)
    }
}
