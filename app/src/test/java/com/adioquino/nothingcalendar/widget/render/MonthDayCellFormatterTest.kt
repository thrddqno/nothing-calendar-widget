package com.adioquino.nothingcalendar.widget.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MonthDayCellFormatterTest {
    @Test
    fun `date without events renders only the number`() {
        assertEquals(MonthDayCellContent("1", false), MonthDayCellFormatter.format(1, false))
        assertEquals(MonthDayCellContent("31", false), MonthDayCellFormatter.format(31, false))
    }

    @Test
    fun `indicator visibility is independent from date text`() {
        assertEquals(MonthDayCellContent("1", true), MonthDayCellFormatter.format(1, true))
        assertEquals(MonthDayCellContent("30", true), MonthDayCellFormatter.format(30, true))
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
