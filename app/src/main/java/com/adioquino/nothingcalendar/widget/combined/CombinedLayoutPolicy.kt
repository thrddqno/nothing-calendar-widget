package com.adioquino.nothingcalendar.widget.combined

import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory

object CombinedLayoutPolicy {
    const val ROOT_VERTICAL_PADDING_DP = 12
    const val MONTH_HEADER_HEIGHT_DP = 24
    const val WEEKDAY_HEADER_HEIGHT_DP = 16
    const val MONTH_WEEK_HEIGHT_DP = 24
    const val DIVIDER_SECTION_HEIGHT_DP = 13
    const val AGENDA_HEADER_HEIGHT_DP = 20
    const val AGENDA_ROW_HEIGHT_DP = 34
    const val AGENDA_OVERFLOW_HEIGHT_DP = 14
    const val MAX_AGENDA_ROWS = 4

    fun calculate(
        availableHeightDp: Int,
        monthWeekCount: Int,
    ): CombinedLayoutCapacity {
        require(availableHeightDp >= 0) { "Available height must not be negative" }
        require(monthWeekCount in WidgetSnapshotFactory.MIN_WEEK_COUNT..WidgetSnapshotFactory.MAX_WEEK_COUNT) {
            "Month must contain four to six weeks"
        }
        val monthSectionHeight = MONTH_HEADER_HEIGHT_DP +
            WEEKDAY_HEADER_HEIGHT_DP +
            monthWeekCount * MONTH_WEEK_HEIGHT_DP
        val fixedHeight = ROOT_VERTICAL_PADDING_DP +
            monthSectionHeight +
            DIVIDER_SECTION_HEIGHT_DP +
            AGENDA_HEADER_HEIGHT_DP
        return CombinedLayoutCapacity(
            monthSectionHeightDp = monthSectionHeight,
            agendaContentHeightDp = (availableHeightDp - fixedHeight).coerceAtLeast(0),
        )
    }
}

data class CombinedLayoutCapacity(
    val monthSectionHeightDp: Int,
    val agendaContentHeightDp: Int,
)
