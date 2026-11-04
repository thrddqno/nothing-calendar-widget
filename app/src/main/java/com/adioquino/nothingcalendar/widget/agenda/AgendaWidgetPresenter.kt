package com.adioquino.nothingcalendar.widget.agenda

import com.adioquino.nothingcalendar.widget.presentation.WidgetAgendaOccurrence
import java.time.Instant

class AgendaWidgetPresenter(
    private val formatter: AgendaEventFormatter,
) {
    fun present(
        occurrences: List<WidgetAgendaOccurrence>,
        now: Instant,
        availableHeightDp: Int,
    ): AgendaRenderModel = present(
        occurrences = occurrences,
        now = now,
        capacityResolver = { eligibleCount ->
            AgendaCapacityPolicy.calculate(availableHeightDp, eligibleCount)
        },
    )

    fun presentWithinHeight(
        occurrences: List<WidgetAgendaOccurrence>,
        now: Instant,
        availableContentHeightDp: Int,
        rowHeightDp: Int = AgendaCapacityPolicy.EVENT_ROW_HEIGHT_DP,
        overflowHeightDp: Int = AgendaCapacityPolicy.OVERFLOW_HEIGHT_DP,
        maxEventRows: Int = AgendaCapacityPolicy.MAX_EVENT_ROWS,
    ): AgendaRenderModel = present(
        occurrences = occurrences,
        now = now,
        capacityResolver = { eligibleCount ->
            AgendaCapacityPolicy.calculateWithinHeight(
                availableContentHeightDp = availableContentHeightDp,
                eligibleEventCount = eligibleCount,
                rowHeightDp = rowHeightDp,
                overflowHeightDp = overflowHeightDp,
                maxEventRows = maxEventRows,
            )
        },
    )

    private fun present(
        occurrences: List<WidgetAgendaOccurrence>,
        now: Instant,
        capacityResolver: (Int) -> AgendaCapacity,
    ): AgendaRenderModel {
        val eligible = occurrences
            .asSequence()
            .distinctBy(WidgetAgendaOccurrence::occurrenceId)
            .filter { it.endsAt.isAfter(now) }
            .sortedWith(
                compareBy<WidgetAgendaOccurrence>(WidgetAgendaOccurrence::startsAt)
                    .thenBy(WidgetAgendaOccurrence::endsAt)
                    .thenBy(WidgetAgendaOccurrence::eventId),
            )
            .toList()
        val capacity = capacityResolver(eligible.size)
        val rows = eligible.take(capacity.visibleEventRows).map { occurrence ->
            val formatted = formatter.format(occurrence, now)
            AgendaRowModel(
                eventId = occurrence.eventId,
                occurrenceId = occurrence.occurrenceId,
                title = formatted.title,
                temporalLabel = formatted.temporalLabel,
                calendarColor = occurrence.calendarColor,
            )
        }
        return AgendaRenderModel(
            rows = rows,
            totalEligibleEvents = eligible.size,
            hiddenEventCount = capacity.hiddenEventCount,
            showOverflow = capacity.showOverflow,
        )
    }
}

data class AgendaRenderModel(
    val rows: List<AgendaRowModel>,
    val totalEligibleEvents: Int,
    val hiddenEventCount: Int,
    val showOverflow: Boolean,
)

data class AgendaRowModel(
    val eventId: Long,
    val occurrenceId: String,
    val title: String,
    val temporalLabel: String,
    val calendarColor: Int?,
)
