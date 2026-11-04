package com.adioquino.nothingcalendar.calendar.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset

object CalendarEventProcessor {
    fun chronological(events: List<CalendarEvent>): List<CalendarEvent> = events
        .distinctBy(CalendarEvent::instanceId)
        .sortedWith(
            compareBy<CalendarEvent>(CalendarEvent::startsAt)
                .thenBy(CalendarEvent::endsAt)
                .thenBy(CalendarEvent::calendarId)
                .thenBy(CalendarEvent::eventId),
        )

    fun upcoming(
        events: List<CalendarEvent>,
        nowEpochMillis: Long,
        rangeEndEpochMillis: Long,
        limit: Int,
    ): List<CalendarEvent> = chronological(events)
        .asSequence()
        .filter { event ->
            event.endsAt.toEpochMilli() > nowEpochMillis &&
                event.startsAt.toEpochMilli() < rangeEndEpochMillis
        }
        .take(limit)
        .toList()

    fun monthIndicators(
        events: List<CalendarEvent>,
        month: YearMonth,
        zoneId: ZoneId,
    ): MonthIndicators {
        val mutableDays = mutableMapOf<LocalDate, MutableDayIndicators>()
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()
        chronological(events).forEach { event ->
            val (eventStart, eventEnd) = event.touchedDateBounds(zoneId)
            val firstDate = maxOf(eventStart, monthStart)
            val lastDate = minOf(eventEnd, monthEnd)
            if (firstDate.isAfter(lastDate)) return@forEach

            generateSequence(firstDate) { date ->
                date.plusDays(1).takeIf { !it.isAfter(lastDate) }
            }.forEach { date ->
                val indicators = mutableDays.getOrPut(date, ::MutableDayIndicators)
                indicators.count += 1
                event.calendarColor?.let(indicators.calendarColors::add)
            }
        }

        return MonthIndicators(
            month = month,
            days = mutableDays
                .toSortedMap()
                .mapValues { (_, value) ->
                    DayIndicators(value.count, value.calendarColors.toSet())
                },
        )
    }

    fun CalendarEvent.touchedDates(zoneId: ZoneId): List<LocalDate> {
        val (startDate, endDateInclusive) = touchedDateBounds(zoneId)
        return generateSequence(startDate) { date ->
            date.plusDays(1).takeIf { !it.isAfter(endDateInclusive) }
        }.toList()
    }

    private fun CalendarEvent.touchedDateBounds(zoneId: ZoneId): Pair<LocalDate, LocalDate> {
        val effectiveZone = if (isAllDay) ZoneOffset.UTC else zoneId
        val startDate = startsAt.atZone(effectiveZone).toLocalDate()
        val endDateInclusive = when {
            !endsAt.isAfter(startsAt) -> startDate
            else -> endsAt.minusNanos(1).atZone(effectiveZone).toLocalDate()
        }
        return startDate to endDateInclusive
    }

    private data class MutableDayIndicators(
        var count: Int = 0,
        val calendarColors: MutableSet<Int> = mutableSetOf(),
    )
}
