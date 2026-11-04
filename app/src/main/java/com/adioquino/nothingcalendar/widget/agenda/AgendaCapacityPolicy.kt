package com.adioquino.nothingcalendar.widget.agenda

object AgendaCapacityPolicy {
    const val MAX_EVENT_ROWS = 4
    const val HEADER_AND_PADDING_DP = 30
    const val EVENT_ROW_HEIGHT_DP = 36
    const val OVERFLOW_HEIGHT_DP = 14

    fun calculate(
        availableHeightDp: Int,
        eligibleEventCount: Int,
    ): AgendaCapacity {
        require(availableHeightDp >= 0) { "Available height must not be negative" }
        require(eligibleEventCount >= 0) { "Event count must not be negative" }
        if (eligibleEventCount == 0) return AgendaCapacity(0, false, 0)

        val contentHeight = (availableHeightDp - HEADER_AND_PADDING_DP).coerceAtLeast(0)
        val rowCapacity = (contentHeight / EVENT_ROW_HEIGHT_DP)
            .coerceIn(1, MAX_EVENT_ROWS)
        val visibleRows = minOf(rowCapacity, eligibleEventCount)
        val hiddenCount = (eligibleEventCount - visibleRows).coerceAtLeast(0)
        val remainingHeight = contentHeight - visibleRows * EVENT_ROW_HEIGHT_DP
        val showOverflow = hiddenCount > 0 && remainingHeight >= OVERFLOW_HEIGHT_DP
        return AgendaCapacity(visibleRows, showOverflow, hiddenCount)
    }
}

data class AgendaCapacity(
    val visibleEventRows: Int,
    val showOverflow: Boolean,
    val hiddenEventCount: Int,
)
