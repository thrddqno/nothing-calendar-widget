package com.adioquino.nothingcalendar.widget.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetResponsiveSizingPolicyTest {
    @Test
    fun `month widget four five and six rows stay inside available height`() {
        val four = WidgetResponsiveSizingPolicy.calculate(month(rows = 4, heightDp = 130))
        val five = WidgetResponsiveSizingPolicy.calculate(month(rows = 5, heightDp = 130))
        val six = WidgetResponsiveSizingPolicy.calculate(month(rows = 6, heightDp = 130))

        assertEquals(120, four.monthSectionHeightDp)
        assertEquals(119, five.monthSectionHeightDp)
        assertEquals(120, six.monthSectionHeightDp)
        assertEquals(24, four.calendarRowHeightDp)
        assertEquals(19, five.calendarRowHeightDp)
        assertEquals(16, six.calendarRowHeightDp)
    }

    @Test
    fun `minimum 2x2 month keeps every week row`() {
        val result = WidgetResponsiveSizingPolicy.calculate(month(rows = 6, heightDp = 110, widthDp = 110))

        assertEquals(WidgetLayoutMode.COMPACT, result.mode)
        assertEquals(4, result.horizontalPaddingDp)
        assertEquals(4, result.verticalPaddingDp)
        assertEquals(8f, result.dateTextSizeSp)
        assertEquals(13, result.calendarRowHeightDp)
        assertEquals(102, result.monthSectionHeightDp)
        assertEquals(0, result.agendaContentHeightDp)
    }

    @Test
    fun `expanded combined month uses lower panel space`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 6, widthDp = 250, heightDp = 400, eventCount = 10),
        )

        assertEquals(WidgetLayoutMode.NORMAL, result.mode)
        assertEquals(6, result.horizontalPaddingDp)
        assertEquals(6, result.verticalPaddingDp)
        assertEquals(20, result.headerHeightDp)
        assertEquals(12, result.weekdayHeaderHeightDp)
        assertEquals(20, result.calendarRowHeightDp)
        assertEquals(152, result.monthSectionHeightDp)
        assertEquals(210, result.agendaContentHeightDp)
        assertEquals(6, result.sectionSpacingDp)
        assertEquals(8, result.agendaPanelHorizontalInsetDp)
        assertEquals(2, result.agendaPanelVerticalInsetDp)
        assertEquals(32, result.agendaRowHeightDp)
        assertEquals(6, result.agendaCapacity.visibleEventRows)
        assertEquals(4, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `combined large font uses fifth row when remaining height permits`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(
                rows = 6,
                widthDp = 250,
                heightDp = 400,
                fontScale = 1.15f,
                eventCount = 10,
            ),
        )

        assertEquals(5, result.agendaCapacity.visibleEventRows)
        assertEquals(5, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `combined five-row month leaves more room for complete agenda rows`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 5, widthDp = 250, heightDp = 400, eventCount = 10),
        )

        assertEquals(142, result.monthSectionHeightDp)
        assertEquals(220, result.agendaContentHeightDp)
        assertEquals(6, result.agendaCapacity.visibleEventRows)
        assertEquals(4, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `combined agenda hides overflow when every event fits`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 4, widthDp = 250, heightDp = 400, eventCount = 2),
        )

        assertEquals(2, result.agendaCapacity.visibleEventRows)
        assertEquals(0, result.agendaCapacity.hiddenEventCount)
        assertFalse(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `default 4x4 six-week month fits two rows and accurate overflow`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 6, widthDp = 250, heightDp = 250, eventCount = 7),
        )

        assertEquals(17, result.calendarRowHeightDp)
        assertEquals(2, result.agendaCapacity.visibleEventRows)
        assertEquals(5, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `default 4x4 five-week month fits three complete agenda rows`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 5, widthDp = 250, heightDp = 250, eventCount = 6),
        )

        assertEquals(14, result.calendarRowHeightDp)
        assertEquals(3, result.agendaCapacity.visibleEventRows)
        assertEquals(3, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `narrow combined widget uses compact padding and still fills rows`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 5, widthDp = 170, heightDp = 400, eventCount = 8),
        )

        assertEquals(WidgetLayoutMode.COMPACT, result.mode)
        assertEquals(4, result.horizontalPaddingDp)
        assertEquals(4, result.verticalPaddingDp)
        assertEquals(8f, result.dateTextSizeSp)
        assertEquals(6, result.agendaCapacity.visibleEventRows)
        assertEquals(2, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `combined calendar rows grow with font scale`() {
        val normal = WidgetResponsiveSizingPolicy.calculate(combined(rows = 5, fontScale = 1.15f))
        val large = WidgetResponsiveSizingPolicy.calculate(combined(rows = 5, fontScale = 1.3f))
        val largest = WidgetResponsiveSizingPolicy.calculate(combined(rows = 6, fontScale = 1.5f))

        assertEquals(26, normal.calendarRowHeightDp)
        assertEquals(30, large.calendarRowHeightDp)
        assertEquals(34, largest.calendarRowHeightDp)
        assertEquals(23, normal.headerHeightDp)
        assertEquals(26, large.headerHeightDp)
        assertEquals(30, largest.headerHeightDp)
    }

    @Test
    fun `short combined widget caps large-font rows to preserve complete grid`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 6, heightDp = 250, fontScale = 1.5f, eventCount = 3),
        )

        assertEquals(16, result.calendarRowHeightDp)
        assertEquals(144, result.monthSectionHeightDp)
        assertEquals(64, result.agendaContentHeightDp)
        assertEquals(1, result.agendaCapacity.visibleEventRows)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `large font reduces agenda capacity instead of clipping rows`() {
        val normal = WidgetResponsiveSizingPolicy.calculate(today(fontScale = 1f, eventCount = 4))
        val large = WidgetResponsiveSizingPolicy.calculate(today(fontScale = 1.15f, eventCount = 4))
        val largest = WidgetResponsiveSizingPolicy.calculate(today(fontScale = 1.5f, eventCount = 4))

        assertEquals(3, normal.agendaCapacity.visibleEventRows)
        assertEquals(1, normal.agendaCapacity.hiddenEventCount)
        assertEquals(1, large.agendaCapacity.visibleEventRows)
        assertEquals(3, large.agendaCapacity.hiddenEventCount)
        assertEquals(1, largest.agendaCapacity.visibleEventRows)
        assertTrue(largest.agendaCapacity.showOverflow)
    }

    @Test
    fun `typography roles follow widget hierarchy`() {
        val month = WidgetResponsiveSizingPolicy.calculate(month(rows = 5, widthDp = 250))
        val combined = WidgetResponsiveSizingPolicy.calculate(combined(rows = 5, widthDp = 250))
        val combinedLargest = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 6, widthDp = 250, fontScale = 1.5f),
        )
        val today = WidgetResponsiveSizingPolicy.calculate(today())

        assertEquals(16f, month.monthHeadingTextSizeSp)
        assertEquals(18f, combined.monthHeadingTextSizeSp)
        assertEquals(13f, combinedLargest.monthHeadingTextSizeSp)
        assertEquals(12f, combined.sectionHeadingTextSizeSp)
        assertEquals(48f, today.displayTextSizeSp)
        assertEquals(11f, today.weekdayTextSizeSp)
    }

    @Test
    fun `today display size contracts as system font scale grows`() {
        val normal = WidgetResponsiveSizingPolicy.calculate(today(fontScale = 1f))
        val medium = WidgetResponsiveSizingPolicy.calculate(today(fontScale = 1.15f))
        val large = WidgetResponsiveSizingPolicy.calculate(today(fontScale = 1.3f))
        val largest = WidgetResponsiveSizingPolicy.calculate(today(fontScale = 1.5f))

        assertEquals(48f, normal.displayTextSizeSp)
        assertEquals(40f, medium.displayTextSizeSp)
        assertEquals(34f, large.displayTextSizeSp)
        assertEquals(30f, largest.displayTextSizeSp)
        assertTrue(normal.displayTextSizeSp * 1f <= 50f)
        assertTrue(medium.displayTextSizeSp * 1.15f <= 50f)
        assertTrue(large.displayTextSizeSp * 1.3f <= 50f)
        assertTrue(largest.displayTextSizeSp * 1.5f <= 50f)
    }

    @Test
    fun `today normal width fits three complete rows`() {
        val result = WidgetResponsiveSizingPolicy.calculate(today(eventCount = 5))

        assertEquals(WidgetLayoutMode.NORMAL, result.mode)
        assertEquals(24, result.agendaRowHeightDp)
        assertEquals(3, result.agendaCapacity.visibleEventRows)
        assertEquals(2, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `today narrow width caps at two complete rows`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            today(widthDp = 220, eventCount = 5),
        )

        assertEquals(WidgetLayoutMode.COMPACT, result.mode)
        assertEquals(2, result.agendaCapacity.visibleEventRows)
        assertEquals(3, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `today insets reduce usable height`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            today(
                eventCount = 3,
                insets = WidgetInsets(startDp = 5, endDp = 5, topDp = 4, bottomDp = 4),
            ),
        )

        assertEquals(3, result.agendaCapacity.visibleEventRows)
        assertEquals(76, result.agendaContentHeightDp)
    }

    @Test
    fun `agenda widget caps at four rows and reports overflow`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            WidgetSizingInput(
                kind = WidgetKind.AGENDA,
                availableWidthDp = 250,
                availableHeightDp = 300,
                fontScale = 1f,
                availableAgendaEventCount = 10,
            ),
        )

        assertEquals(4, result.agendaCapacity.visibleEventRows)
        assertEquals(6, result.agendaCapacity.hiddenEventCount)
        assertTrue(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `empty agenda has no rows and no overflow`() {
        val result = WidgetResponsiveSizingPolicy.calculate(today(eventCount = 0))

        assertEquals(0, result.agendaCapacity.visibleEventRows)
        assertEquals(0, result.agendaCapacity.hiddenEventCount)
        assertFalse(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `single event never shows overflow`() {
        val result = WidgetResponsiveSizingPolicy.calculate(
            combined(rows = 4, widthDp = 250, heightDp = 400, eventCount = 1),
        )

        assertEquals(1, result.agendaCapacity.visibleEventRows)
        assertFalse(result.agendaCapacity.showOverflow)
    }

    @Test
    fun `invalid sizing input is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            WidgetResponsiveSizingPolicy.calculate(today(widthDp = -1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            WidgetResponsiveSizingPolicy.calculate(today(heightDp = -1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            WidgetResponsiveSizingPolicy.calculate(today(fontScale = 0f))
        }
        assertThrows(IllegalArgumentException::class.java) {
            WidgetResponsiveSizingPolicy.calculate(month(rows = 7))
        }
        assertThrows(IllegalArgumentException::class.java) {
            WidgetResponsiveSizingPolicy.calculate(today(eventCount = -1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            WidgetInsets(startDp = -1)
        }
    }

    private fun month(
        rows: Int,
        widthDp: Int = 130,
        heightDp: Int = 130,
        fontScale: Float = 1f,
    ) = WidgetSizingInput(
        kind = WidgetKind.MONTH,
        availableWidthDp = widthDp,
        availableHeightDp = heightDp,
        fontScale = fontScale,
        monthRowCount = rows,
    )

    private fun combined(
        rows: Int,
        widthDp: Int = 250,
        heightDp: Int = 400,
        fontScale: Float = 1f,
        eventCount: Int = 0,
    ) = WidgetSizingInput(
        kind = WidgetKind.COMBINED,
        availableWidthDp = widthDp,
        availableHeightDp = heightDp,
        fontScale = fontScale,
        monthRowCount = rows,
        availableAgendaEventCount = eventCount,
    )

    private fun today(
        widthDp: Int = 250,
        heightDp: Int = 110,
        fontScale: Float = 1f,
        eventCount: Int = 0,
        insets: WidgetInsets = WidgetInsets(),
    ) = WidgetSizingInput(
        kind = WidgetKind.TODAY_AGENDA,
        availableWidthDp = widthDp,
        availableHeightDp = heightDp,
        fontScale = fontScale,
        availableAgendaEventCount = eventCount,
        insets = insets,
    )
}
