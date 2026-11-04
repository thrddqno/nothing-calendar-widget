package com.adioquino.nothingcalendar.widget.agenda

import com.adioquino.nothingcalendar.widget.presentation.WidgetAgendaOccurrence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class AgendaEventFormatter(
    private val locale: Locale,
    private val zoneId: ZoneId,
    private val use24HourTime: Boolean,
    private val strings: AgendaFormatStrings,
) {
    fun format(
        occurrence: WidgetAgendaOccurrence,
        now: Instant,
    ): FormattedAgendaEvent {
        val today = now.atZone(zoneId).toLocalDate()
        val eventDate = occurrence.localCalendarDate(zoneId)
        val ongoing = !occurrence.startsAt.isAfter(now) && occurrence.endsAt.isAfter(now)
        return FormattedAgendaEvent(
            title = occurrence.title.trim().ifEmpty { strings.untitledEvent },
            temporalLabel = when {
                occurrence.isAllDay -> formatAllDayDate(eventDate, today)
                ongoing -> strings.now
                else -> formatTimedDate(occurrence.startsAt, eventDate, today)
            },
        )
    }

    private fun formatAllDayDate(eventDate: LocalDate, today: LocalDate): String = when (eventDate) {
        today -> strings.allDay
        today.plusDays(1) -> "${eventDate.weekdayLabel()} ${strings.allDay}"
        else -> "${eventDate.laterDateLabel()} · ${strings.allDay}"
    }

    private fun formatTimedDate(
        startsAt: Instant,
        eventDate: LocalDate,
        today: LocalDate,
    ): String {
        val time = startsAt.atZone(zoneId).format(timeFormatter())
        return when (eventDate) {
            today -> time
            today.plusDays(1) -> "${eventDate.weekdayLabel()} $time"
            else -> "${eventDate.laterDateLabel()} · $time"
        }
    }

    private fun timeFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern(
        if (use24HourTime) "HH:mm" else "h:mm a",
        locale,
    )

    private fun LocalDate.weekdayLabel(): String = format(
        DateTimeFormatter.ofPattern("EEE", locale),
    ).uppercase(locale)

    private fun LocalDate.laterDateLabel(): String = format(
        DateTimeFormatter.ofPattern("EEE dd MMM", locale),
    ).uppercase(locale)

    private fun WidgetAgendaOccurrence.localCalendarDate(deviceZoneId: ZoneId): LocalDate =
        startsAt.atZone(if (isAllDay) ZoneOffset.UTC else deviceZoneId).toLocalDate()
}

data class AgendaFormatStrings(
    val allDay: String,
    val now: String,
    val untitledEvent: String,
)

data class FormattedAgendaEvent(
    val title: String,
    val temporalLabel: String,
)
