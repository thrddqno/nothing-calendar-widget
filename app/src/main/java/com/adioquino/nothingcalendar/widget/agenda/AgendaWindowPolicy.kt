package com.adioquino.nothingcalendar.widget.agenda

import com.adioquino.nothingcalendar.widget.presentation.WidgetAgendaOccurrence
import java.time.Duration
import java.time.Instant

class AgendaWindowPolicy(
    private val windowDuration: Duration = WINDOW_DURATION,
) {
    init {
        require(!windowDuration.isZero && !windowDuration.isNegative) {
            "Agenda window duration must be positive"
        }
    }

    fun eligibleOccurrences(
        occurrences: List<WidgetAgendaOccurrence>,
        windowStart: Instant,
    ): List<WidgetAgendaOccurrence> {
        val windowEnd = windowStart.plus(windowDuration)
        return occurrences
            .asSequence()
            .sortedWith(
                compareBy<WidgetAgendaOccurrence>(WidgetAgendaOccurrence::startsAt)
                    .thenBy(WidgetAgendaOccurrence::endsAt)
                    .thenBy(WidgetAgendaOccurrence::eventId),
            )
            .distinctBy(WidgetAgendaOccurrence::occurrenceId)
            .filter { occurrence ->
                occurrence.startsAt.isBefore(windowEnd) && occurrence.endsAt.isAfter(windowStart)
            }
            .toList()
    }

    companion object {
        val WINDOW_DURATION: Duration = Duration.ofHours(12)
    }
}
