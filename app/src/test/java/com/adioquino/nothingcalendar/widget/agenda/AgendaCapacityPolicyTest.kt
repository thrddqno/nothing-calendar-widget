package com.adioquino.nothingcalendar.widget.agenda

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaCapacityPolicyTest {
    @Test
    fun `small height fits one complete event`() {
        assertEquals(1, AgendaCapacityPolicy.calculate(70, 5).visibleEventRows)
    }

    @Test
    fun `nominal height fits two complete events`() {
        assertEquals(2, AgendaCapacityPolicy.calculate(110, 5).visibleEventRows)
    }

    @Test
    fun `larger height fits three complete events`() {
        assertEquals(3, AgendaCapacityPolicy.calculate(150, 5).visibleEventRows)
    }

    @Test
    fun `overflow count equals undisplayed eligible occurrences`() {
        val capacity = AgendaCapacityPolicy.calculate(124, 7)

        assertEquals(2, capacity.visibleEventRows)
        assertEquals(5, capacity.hiddenEventCount)
        assertTrue(capacity.showOverflow)
    }

    @Test
    fun `overflow hides when remaining height is insufficient`() {
        val capacity = AgendaCapacityPolicy.calculate(110, 7)

        assertEquals(5, capacity.hiddenEventCount)
        assertFalse(capacity.showOverflow)
    }
}
