package com.adioquino.nothingcalendar.widget.agenda

import com.adioquino.nothingcalendar.widget.presentation.WidgetAgendaOccurrence
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AgendaWidgetPresenterTest {
    private val now = Instant.parse("2026-11-05T12:00:00Z")
    private val presenter = AgendaWidgetPresenter(
        AgendaEventFormatter(
            locale = Locale.US,
            zoneId = ZoneId.of("UTC"),
            use24HourTime = true,
            strings = AgendaFormatStrings("ALL DAY", "NOW", "Untitled event"),
        ),
    )

    @Test
    fun `orders ongoing and future occurrences and excludes completed`() {
        val completed = occurrence(1, "2026-11-05T10:00:00Z", "2026-11-05T12:00:00Z")
        val ongoing = occurrence(2, "2026-11-05T11:00:00Z", "2026-11-05T13:00:00Z")
        val future = occurrence(3, "2026-11-05T14:00:00Z", "2026-11-05T15:00:00Z")

        val result = presenter.present(listOf(future, completed, ongoing), now, 150)

        assertEquals(listOf(2L, 3L), result.rows.map(AgendaRowModel::eventId))
        assertEquals("NOW", result.rows.first().temporalLabel)
        assertEquals(2, result.totalEligibleEvents)
    }

    @Test
    fun `removes duplicate occurrence IDs but preserves recurring instances`() {
        val first = occurrence(10, "2026-11-05T13:00:00Z", "2026-11-05T14:00:00Z", "series:1")
        val duplicate = first.copy(title = "Duplicate")
        val second = occurrence(10, "2026-11-05T15:00:00Z", "2026-11-05T16:00:00Z", "series:2")

        val result = presenter.present(listOf(second, duplicate, first), now, 150)

        assertEquals(listOf("series:1", "series:2"), result.rows.map(AgendaRowModel::occurrenceId))
        assertEquals(2, result.totalEligibleEvents)
    }

    @Test
    fun `empty occurrences produce empty model`() {
        val result = presenter.present(emptyList(), now, 110)

        assertEquals(emptyList<AgendaRowModel>(), result.rows)
        assertEquals(0, result.totalEligibleEvents)
        assertEquals(0, result.hiddenEventCount)
    }

    @Test
    fun `missing calendar color stays null for renderer fallback`() {
        val result = presenter.present(
            listOf(occurrence(1, "2026-11-05T14:00:00Z", "2026-11-05T15:00:00Z")),
            now,
            110,
        )

        assertNull(result.rows.single().calendarColor)
    }

    @Test
    fun `overflow counts only eligible events inside rolling window`() {
        val occurrences = List(586) { index ->
            val start = if (index < 4) {
                now.plusSeconds(index.toLong() + 1)
            } else {
                now.plusSeconds(46_800 + index.toLong())
            }
            WidgetAgendaOccurrence(
                eventId = index.toLong(),
                occurrenceId = "occurrence:$index",
                title = "Event $index",
                startsAt = start,
                endsAt = start.plusSeconds(1_800),
                isAllDay = false,
                calendarColor = null,
                isOngoing = false,
            )
        }

        val result = presenter.present(occurrences, now, 164)

        assertEquals(4, result.totalEligibleEvents)
        assertEquals(3, result.rows.size)
        assertEquals(1, result.hiddenEventCount)
        assertEquals(true, result.showOverflow)
    }

    private fun occurrence(
        eventId: Long,
        start: String,
        end: String,
        occurrenceId: String = "$eventId:$start",
    ) = WidgetAgendaOccurrence(
        eventId = eventId,
        occurrenceId = occurrenceId,
        title = "Event $eventId",
        startsAt = Instant.parse(start),
        endsAt = Instant.parse(end),
        isAllDay = false,
        calendarColor = null,
        isOngoing = false,
    )
}
