package com.adioquino.nothingcalendar.widget.render

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetSizeResolverTest {
    private val defaultSize = WidgetSize(110, 110)

    @Test
    fun `provided launcher sizes are used and deduplicated`() {
        val result = WidgetSizeResolver.resolveBounds(
            minWidthDp = 110,
            minHeightDp = 110,
            maxWidthDp = 400,
            maxHeightDp = 500,
            providedSizes = listOf(
                WidgetSize(250, 400),
                WidgetSize(250, 400),
                WidgetSize(200, 300),
            ),
            defaultSize = defaultSize,
        )

        assertEquals(listOf(WidgetSize(250, 400), WidgetSize(200, 300)), result)
    }

    @Test
    fun `invalid provided sizes are ignored`() {
        val result = WidgetSizeResolver.resolveBounds(
            minWidthDp = 110,
            minHeightDp = 110,
            maxWidthDp = 400,
            maxHeightDp = 500,
            providedSizes = listOf(WidgetSize(0, 400), WidgetSize(250, 0)),
            defaultSize = defaultSize,
        )

        assertEquals(listOf(WidgetSize(110, 110), WidgetSize(400, 500)), result)
    }

    @Test
    fun `missing bounds fall back to the default size`() {
        val result = WidgetSizeResolver.resolveBounds(
            minWidthDp = 0,
            minHeightDp = 0,
            maxWidthDp = 0,
            maxHeightDp = 0,
            providedSizes = emptyList(),
            defaultSize = defaultSize,
        )

        assertEquals(listOf(defaultSize), result)
    }

    @Test
    fun `max bounds smaller than minimum are clamped`() {
        val result = WidgetSizeResolver.resolveBounds(
            minWidthDp = 250,
            minHeightDp = 400,
            maxWidthDp = 100,
            maxHeightDp = 100,
            providedSizes = emptyList(),
            defaultSize = defaultSize,
        )

        assertEquals(listOf(WidgetSize(250, 400)), result)
    }

    @Test
    fun `only one axis provided still resolves a valid size`() {
        val result = WidgetSizeResolver.resolveBounds(
            minWidthDp = 250,
            minHeightDp = 0,
            maxWidthDp = 250,
            maxHeightDp = 400,
            providedSizes = emptyList(),
            defaultSize = defaultSize,
        )

        assertEquals(listOf(WidgetSize(250, 110), WidgetSize(250, 400)), result)
    }
}
