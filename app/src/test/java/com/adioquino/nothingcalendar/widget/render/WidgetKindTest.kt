package com.adioquino.nothingcalendar.widget.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WidgetKindTest {
    @Test
    fun `defines three independent widget kinds`() {
        assertEquals(3, WidgetKind.entries.size)
        assertNotEquals(WidgetKind.MONTH.layoutRes, WidgetKind.AGENDA.layoutRes)
        assertNotEquals(WidgetKind.AGENDA.layoutRes, WidgetKind.COMBINED.layoutRes)
    }
}
