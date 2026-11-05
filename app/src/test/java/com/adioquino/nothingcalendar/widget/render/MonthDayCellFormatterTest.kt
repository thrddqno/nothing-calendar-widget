package com.adioquino.nothingcalendar.widget.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MonthDayCellFormatterTest {
    @Test
    fun `date without events renders only the number`() {
        assertEquals("1", MonthDayCellFormatter.format(1, hasIndicator = false))
        assertEquals("31", MonthDayCellFormatter.format(31, hasIndicator = false))
    }

    @Test
    fun `event indicator is placed on a second centered line`() {
        assertEquals("1\n•", MonthDayCellFormatter.format(1, hasIndicator = true))
        assertEquals("30\n•", MonthDayCellFormatter.format(30, hasIndicator = true))
    }

    @Test
    fun `invalid day values are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            MonthDayCellFormatter.format(0, hasIndicator = true)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MonthDayCellFormatter.format(32, hasIndicator = true)
        }
    }
}
