package com.adioquino.nothingcalendar.widget.combined

import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import com.adioquino.nothingcalendar.widget.render.WidgetKind
import com.adioquino.nothingcalendar.widget.render.WidgetLayoutSpec
import com.adioquino.nothingcalendar.widget.render.WidgetResponsiveSizingPolicy
import com.adioquino.nothingcalendar.widget.render.WidgetSizingInput

object CombinedLayoutPolicy {
    fun calculate(
        availableHeightDp: Int,
        monthWeekCount: Int,
        availableWidthDp: Int = 250,
        fontScale: Float = 1f,
        eligibleEventCount: Int = 0,
    ): CombinedLayoutCapacity {
        require(availableHeightDp >= 0) { "Available height must not be negative" }
        require(monthWeekCount in WidgetSnapshotFactory.MIN_WEEK_COUNT..WidgetSnapshotFactory.MAX_WEEK_COUNT) {
            "Month must contain four to six weeks"
        }
        val spec = WidgetResponsiveSizingPolicy.calculate(
            WidgetSizingInput(
                kind = WidgetKind.COMBINED,
                availableWidthDp = availableWidthDp,
                availableHeightDp = availableHeightDp,
                fontScale = fontScale,
                monthRowCount = monthWeekCount,
                availableAgendaEventCount = eligibleEventCount,
            ),
        )
        return CombinedLayoutCapacity(
            monthSectionHeightDp = spec.monthSectionHeightDp,
            agendaContentHeightDp = spec.agendaContentHeightDp,
            calendarRowHeightDp = spec.calendarRowHeightDp,
            agendaRowHeightDp = spec.agendaRowHeightDp,
            agendaOverflowHeightDp = spec.overflowHeightDp,
            maxAgendaRows = spec.maxAgendaRows,
            spec = spec,
        )
    }
}

data class CombinedLayoutCapacity(
    val monthSectionHeightDp: Int,
    val agendaContentHeightDp: Int,
    val calendarRowHeightDp: Int,
    val agendaRowHeightDp: Int,
    val agendaOverflowHeightDp: Int,
    val maxAgendaRows: Int,
    val spec: WidgetLayoutSpec,
)
