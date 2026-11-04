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
        val capacity = AgendaCapacityPolicy.calculate(availableHeightDp, eligible.size)
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
