package com.adioquino.nothingcalendar.widget.agenda

import com.adioquino.nothingcalendar.widget.presentation.WidgetAgendaOccurrence
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaWindowPolicyTest {
    private val now = Instant.parse("2026-11-05T20:00:00Z")
    private val policy = AgendaWindowPolicy()

    @Test
    fun `event starting now is eligible`() {
        assertEligible(occurrence(1, now, now.plusSeconds(3_600)), 1)
    }

    @Test
    fun `already ongoing event is eligible`() {
        assertEligible(occurrence(1, now.minusSeconds(3_600), now.plusSeconds(3_600)), 1)
    }

    @Test
    fun `completed event is excluded`() {
        assertEligible(occurrence(1, now.minusSeconds(7_200), now.minusSeconds(1)), 0)
    }

    @Test
    fun `event starting within twelve hours is eligible`() {
        assertEligible(occurrence(1, now.plusSeconds(43_199), now.plusSeconds(46_000)), 1)
    }

    @Test
    fun `event starting exactly at window end is excluded`() {
        assertEligible(occurrence(1, now.plusSeconds(43_200), now.plusSeconds(46_800)), 0)
    }

    @Test
    fun `event starting beyond window is excluded`() {
        assertEligible(occurrence(1, now.plusSeconds(43_201), now.plusSeconds(46_800)), 0)
    }

    @Test
    fun `event ending exactly at window start is excluded`() {
        assertEligible(occurrence(1, now.minusSeconds(3_600), now), 0)
    }

    @Test
    fun `event spanning entire window is eligible`() {
        assertEligible(occurrence(1, now.minusSeconds(86_400), now.plusSeconds(86_400)), 1)
    }

    @Test
    fun `overnight event overlapping window is eligible`() {
        val event = occurrence(
            1,
            Instant.parse("2026-11-05T23:00:00Z"),
            Instant.parse("2026-11-06T02:00:00Z"),
        )

        assertEligible(event, 1)
    }

    @Test
    fun `tomorrow event inside window is eligible`() {
        val event = occurrence(
            1,
            Instant.parse("2026-11-06T06:00:00Z"),
            Instant.parse("2026-11-06T07:00:00Z"),
        )

        assertEligible(event, 1)
    }

    @Test
    fun `tomorrow event outside window is excluded`() {
        val event = occurrence(
            1,
            Instant.parse("2026-11-06T09:00:00Z"),
            Instant.parse("2026-11-06T10:00:00Z"),
        )

        assertEligible(event, 0)
    }

    @Test
    fun `all-day and multi-day overlaps use interval semantics`() {
        val allDay = occurrence(
            1,
            Instant.parse("2026-11-05T00:00:00Z"),
            Instant.parse("2026-11-06T00:00:00Z"),
            allDay = true,
        )
        val multiDay = occurrence(
            2,
            Instant.parse("2026-11-04T12:00:00Z"),
            Instant.parse("2026-11-07T12:00:00Z"),
        )

        assertEquals(listOf(2L, 1L), policy.eligibleOccurrences(listOf(allDay, multiDay), now).map { it.eventId })
    }

    @Test
    fun `recurring instances remain distinct and chronological`() {
        val later = occurrence(10, now.plusSeconds(7_200), now.plusSeconds(10_800), id = "series:2")
        val earlier = occurrence(10, now.plusSeconds(3_600), now.plusSeconds(5_400), id = "series:1")

        assertEquals(
            listOf("series:1", "series:2"),
            policy.eligibleOccurrences(listOf(later, earlier), now).map { it.occurrenceId },
        )
    }

    @Test
    fun `duplicate occurrence IDs are removed`() {
        val original = occurrence(1, now.plusSeconds(3_600), now.plusSeconds(7_200), id = "duplicate")

        assertEquals(1, policy.eligibleOccurrences(listOf(original, original.copy()), now).size)
    }

    @Test
    fun `zero overlapping events returns empty list`() {
        val result = policy.eligibleOccurrences(
            listOf(occurrence(1, now.plusSeconds(86_400), now.plusSeconds(90_000))),
            now,
        )

        assertTrue(result.isEmpty())
    }

    private fun assertEligible(occurrence: WidgetAgendaOccurrence, expectedCount: Int) {
        assertEquals(expectedCount, policy.eligibleOccurrences(listOf(occurrence), now).size)
    }

    private fun occurrence(
        eventId: Long,
        start: Instant,
        end: Instant,
        id: String = "$eventId:$start",
        allDay: Boolean = false,
    ) = WidgetAgendaOccurrence(
        eventId = eventId,
        occurrenceId = id,
        title = "Event $eventId",
        startsAt = start,
        endsAt = end,
        isAllDay = allDay,
        calendarColor = null,
        isOngoing = start <= now && end > now,
    )
}
