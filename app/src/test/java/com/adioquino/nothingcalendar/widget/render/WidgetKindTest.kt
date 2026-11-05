package com.adioquino.nothingcalendar.widget.render

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetKindTest {
    @Test
    fun `defines four independent widget kinds`() {
        assertEquals(4, WidgetKind.entries.size)
        assertEquals(4, WidgetKind.entries.map(WidgetKind::layoutRes).toSet().size)
    }
}
