package com.adioquino.nothingcalendar.widget.combined

import org.junit.Assert.assertEquals
import org.junit.Test

class CombinedLayoutPolicyTest {
    @Test
    fun `four-row month reserves only four complete week rows`() {
        val result = CombinedLayoutPolicy.calculate(400, 4)

        assertEquals(120, result.monthSectionHeightDp)
        assertEquals(242, result.agendaContentHeightDp)
    }

    @Test
    fun `five-row month reserves five complete week rows`() {
        val result = CombinedLayoutPolicy.calculate(400, 5)

        assertEquals(142, result.monthSectionHeightDp)
        assertEquals(220, result.agendaContentHeightDp)
    }

    @Test
    fun `six-row month reserves six complete week rows`() {
        val result = CombinedLayoutPolicy.calculate(400, 6)

        assertEquals(164, result.monthSectionHeightDp)
        assertEquals(198, result.agendaContentHeightDp)
    }

    @Test
    fun `small dimensions preserve month before agenda space`() {
        val result = CombinedLayoutPolicy.calculate(250, 6)

        assertEquals(164, result.monthSectionHeightDp)
        assertEquals(48, result.agendaContentHeightDp)
    }
}
