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
        return calculateCapacity(
            availableContentHeightDp = contentHeight,
            eligibleEventCount = eligibleEventCount,
            rowCapacity = rowCapacity,
            rowHeightDp = EVENT_ROW_HEIGHT_DP,
            overflowHeightDp = OVERFLOW_HEIGHT_DP,
        )
    }

    fun calculateWithinHeight(
        availableContentHeightDp: Int,
        eligibleEventCount: Int,
        rowHeightDp: Int = EVENT_ROW_HEIGHT_DP,
        overflowHeightDp: Int = OVERFLOW_HEIGHT_DP,
        maxEventRows: Int = MAX_EVENT_ROWS,
    ): AgendaCapacity {
        require(availableContentHeightDp >= 0) { "Available content height must not be negative" }
        require(eligibleEventCount >= 0) { "Event count must not be negative" }
        require(rowHeightDp > 0) { "Row height must be positive" }
        require(overflowHeightDp > 0) { "Overflow height must be positive" }
        require(maxEventRows > 0) { "Maximum event rows must be positive" }
        if (eligibleEventCount == 0) return AgendaCapacity(0, false, 0)

        val rowCapacity = (availableContentHeightDp / rowHeightDp)
            .coerceIn(0, maxEventRows)
        return calculateCapacity(
            availableContentHeightDp = availableContentHeightDp,
            eligibleEventCount = eligibleEventCount,
            rowCapacity = rowCapacity,
            rowHeightDp = rowHeightDp,
            overflowHeightDp = overflowHeightDp,
        )
    }

    private fun calculateCapacity(
        availableContentHeightDp: Int,
        eligibleEventCount: Int,
        rowCapacity: Int,
        rowHeightDp: Int,
        overflowHeightDp: Int,
    ): AgendaCapacity {
        val visibleRows = minOf(rowCapacity, eligibleEventCount)
        val hiddenCount = (eligibleEventCount - visibleRows).coerceAtLeast(0)
        val remainingHeight = availableContentHeightDp - visibleRows * rowHeightDp
        val showOverflow = hiddenCount > 0 && remainingHeight >= overflowHeightDp
        return AgendaCapacity(visibleRows, showOverflow, hiddenCount)
    }
}

data class AgendaCapacity(
    val visibleEventRows: Int,
    val showOverflow: Boolean,
    val hiddenEventCount: Int,
)
