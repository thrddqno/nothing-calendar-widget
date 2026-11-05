package com.adioquino.nothingcalendar.widget.today

import com.adioquino.nothingcalendar.widget.agenda.AgendaCapacity

object TodayAgendaCapacityPolicy {
    const val ROOT_VERTICAL_PADDING_DP = 12
    const val AGENDA_HEADER_HEIGHT_DP = 14
    const val EVENT_ROW_HEIGHT_DP = 26
    const val OVERFLOW_HEIGHT_DP = 12
    const val MAX_NORMAL_ROWS = 3
    const val MAX_COMPACT_ROWS = 2
    const val COMPACT_WIDTH_DP = 240
    const val LARGE_FONT_SCALE = 1.15f

    fun calculate(
        availableWidthDp: Int,
        availableHeightDp: Int,
        fontScale: Float,
        eligibleEventCount: Int,
    ): TodayAgendaCapacity {
        require(availableWidthDp >= 0) { "Available width must not be negative" }
        require(availableHeightDp >= 0) { "Available height must not be negative" }
        require(fontScale > 0f) { "Font scale must be positive" }
        require(eligibleEventCount >= 0) { "Event count must not be negative" }

        val compact = availableWidthDp < COMPACT_WIDTH_DP || fontScale > LARGE_FONT_SCALE
        val maxRows = if (compact) MAX_COMPACT_ROWS else MAX_NORMAL_ROWS
        val contentHeight = (
            availableHeightDp - ROOT_VERTICAL_PADDING_DP - AGENDA_HEADER_HEIGHT_DP
        ).coerceAtLeast(0)
        val rowCapacity = (contentHeight / EVENT_ROW_HEIGHT_DP).coerceIn(0, maxRows)
        val visibleRows = minOf(rowCapacity, eligibleEventCount)
        val hiddenCount = (eligibleEventCount - visibleRows).coerceAtLeast(0)
        val remainingHeight = contentHeight - visibleRows * EVENT_ROW_HEIGHT_DP
        return TodayAgendaCapacity(
            visibleEventRows = visibleRows,
            eventRowHeightDp = EVENT_ROW_HEIGHT_DP,
            showOverflow = hiddenCount > 0 && remainingHeight >= OVERFLOW_HEIGHT_DP,
            hiddenEventCount = hiddenCount,
            compact = compact,
        )
    }
}

data class TodayAgendaCapacity(
    val visibleEventRows: Int,
    val eventRowHeightDp: Int,
    val showOverflow: Boolean,
    val hiddenEventCount: Int,
    val compact: Boolean,
) {
    fun asAgendaCapacity(): AgendaCapacity = AgendaCapacity(
        visibleEventRows = visibleEventRows,
        showOverflow = showOverflow,
        hiddenEventCount = hiddenEventCount,
    )
}
