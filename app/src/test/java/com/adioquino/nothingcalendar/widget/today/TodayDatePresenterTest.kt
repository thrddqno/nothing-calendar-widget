package com.adioquino.nothingcalendar.widget.today

import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TodayDatePresenterTest {
    @Test
    fun `formats current date with month day and weekday`() {
        val result = presenter(Locale.US, "UTC").present(Instant.parse("2026-11-05T12:00:00Z"))

        assertEquals(TodayDateModel("NOV", "05", "THU"), result)
    }

    @Test
    fun `single-digit day retains leading zero`() {
        val result = presenter(Locale.US, "UTC").present(Instant.parse("2026-01-03T12:00:00Z"))

        assertEquals("03", result.day)
    }

    @Test
    fun `month rollover follows current instant`() {
        val presenter = presenter(Locale.US, "UTC")

        assertEquals("JAN", presenter.present(Instant.parse("2026-01-31T23:59:59Z")).month)
        assertEquals("FEB", presenter.present(Instant.parse("2026-02-01T00:00:00Z")).month)
    }

    @Test
    fun `year rollover updates all date fields`() {
        val result = presenter(Locale.US, "UTC").present(Instant.parse("2027-01-01T00:00:00Z"))

        assertEquals(TodayDateModel("JAN", "01", "FRI"), result)
    }

    @Test
    fun `leap day formats correctly`() {
        val result = presenter(Locale.US, "UTC").present(Instant.parse("2024-02-29T12:00:00Z"))

        assertEquals(TodayDateModel("FEB", "29", "THU"), result)
    }

    @Test
    fun `formatting follows locale`() {
        val instant = Instant.parse("2026-11-05T12:00:00Z")
        val english = presenter(Locale.US, "UTC").present(instant)
        val french = presenter(Locale.FRENCH, "UTC").present(instant)

        assertNotEquals(english.weekday, french.weekday)
        assertEquals("JEU.", french.weekday)
    }

    @Test
    fun `device zone controls date across UTC boundary`() {
        val instant = Instant.parse("2027-01-01T00:30:00Z")

        assertEquals("01", presenter(Locale.US, "UTC").present(instant).day)
        assertEquals("31", presenter(Locale.US, "America/Los_Angeles").present(instant).day)
    }

    private fun presenter(locale: Locale, zoneId: String) = TodayDatePresenter(
        locale = locale,
        zoneId = ZoneId.of(zoneId),
    )
}
