package com.adioquino.nothingcalendar

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.adioquino.nothingcalendar.navigation.AppNavigation
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun eventOccurrencesUseDistinctPendingIntentIdentities() {
        val start = Instant.parse("2026-11-05T20:00:00Z")
        val first = AppNavigation.eventPendingIntent(
            context,
            eventId = 7,
            startsAt = start,
            endsAt = start.plusSeconds(3_600),
        )
        val recurring = AppNavigation.eventPendingIntent(
            context,
            eventId = 7,
            startsAt = start.plusSeconds(86_400),
            endsAt = start.plusSeconds(90_000),
        )

        assertNotEquals(first, recurring)
        assertEquals(context.packageName, first.creatorPackage)
    }

    @Test
    fun calendarTimesUseDistinctPendingIntentIdentities() {
        val first = AppNavigation.calendarPendingIntent(
            context,
            Instant.parse("2026-11-05T20:00:00Z"),
        )
        val second = AppNavigation.calendarPendingIntent(
            context,
            Instant.parse("2026-11-06T20:00:00Z"),
        )

        assertNotEquals(first, second)
    }

    @Test
    @SdkSuppress(minSdkVersion = 31)
    fun navigationPendingIntentsAreImmutable() {
        val start = Instant.parse("2026-11-05T20:00:00Z")

        assertTrue(AppNavigation.settingsPendingIntent(context).isImmutable)
        assertTrue(AppNavigation.calendarPendingIntent(context, start).isImmutable)
        assertTrue(
            AppNavigation.eventPendingIntent(
                context,
                eventId = 7,
                startsAt = start,
                endsAt = start.plusSeconds(3_600),
            ).isImmutable,
        )
    }
}
