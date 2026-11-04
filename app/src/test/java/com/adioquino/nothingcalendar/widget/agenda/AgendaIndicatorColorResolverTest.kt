package com.adioquino.nothingcalendar.widget.agenda

import org.junit.Assert.assertEquals
import org.junit.Test

class AgendaIndicatorColorResolverTest {
    @Test
    fun `missing calendar color uses opaque neutral fallback`() {
        val result = AgendaIndicatorColorResolver.resolve(
            calendarColor = null,
            backgroundColor = 0xFFFFFFFF.toInt(),
            fallbackColor = 0xFF666666.toInt(),
        )

        assertEquals(0xFF666666.toInt(), result)
    }

    @Test
    fun `low contrast calendar color uses fallback`() {
        val result = AgendaIndicatorColorResolver.resolve(
            calendarColor = 0xFFF8F8F8.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            fallbackColor = 0xFF444444.toInt(),
        )

        assertEquals(0xFF444444.toInt(), result)
    }
}
