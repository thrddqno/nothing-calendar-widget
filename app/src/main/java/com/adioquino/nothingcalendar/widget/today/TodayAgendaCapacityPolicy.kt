package com.adioquino.nothingcalendar.widget.today

import com.adioquino.nothingcalendar.widget.agenda.AgendaCapacity
import com.adioquino.nothingcalendar.widget.render.WidgetKind
import com.adioquino.nothingcalendar.widget.render.WidgetLayoutMode
import com.adioquino.nothingcalendar.widget.render.WidgetResponsiveSizingPolicy
import com.adioquino.nothingcalendar.widget.render.WidgetSizingInput

object TodayAgendaCapacityPolicy {
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

        val spec = WidgetResponsiveSizingPolicy.calculate(
            WidgetSizingInput(
                kind = WidgetKind.TODAY_AGENDA,
                availableWidthDp = availableWidthDp,
                availableHeightDp = availableHeightDp,
                fontScale = fontScale,
                availableAgendaEventCount = eligibleEventCount,
            ),
        )
        val capacity = spec.agendaCapacity
        return TodayAgendaCapacity(
            visibleEventRows = capacity.visibleEventRows,
            eventRowHeightDp = spec.agendaRowHeightDp,
            showOverflow = capacity.showOverflow,
            hiddenEventCount = capacity.hiddenEventCount,
            compact = spec.mode == WidgetLayoutMode.COMPACT,
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
