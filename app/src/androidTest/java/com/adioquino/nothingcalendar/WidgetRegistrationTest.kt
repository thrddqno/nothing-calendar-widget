package com.adioquino.nothingcalendar

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adioquino.nothingcalendar.widget.agenda.AgendaWidgetProvider
import com.adioquino.nothingcalendar.widget.combined.CombinedWidgetProvider
import com.adioquino.nothingcalendar.widget.month.MonthWidgetProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetRegistrationTest {
    @Test
    fun allWidgetProvidersAreExportedWithMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = context.packageManager
        val providerClasses = listOf(
            MonthWidgetProvider::class.java,
            AgendaWidgetProvider::class.java,
            CombinedWidgetProvider::class.java,
        )

        providerClasses.forEach { providerClass ->
            val info = packageManager.getReceiverInfo(
                ComponentName(context, providerClass),
                PackageManager.GET_META_DATA,
            )
            assertTrue(info.exported)
            assertNotNull(info.metaData)
            assertTrue(info.metaData.getInt(AppWidgetManager.META_DATA_APPWIDGET_PROVIDER) != 0)
        }
    }

    @Test
    fun launcherDiscoversThreeWidgetProviders() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val providers = AppWidgetManager.getInstance(context).installedProviders
            .filter { it.provider.packageName == context.packageName }

        assertEquals(3, providers.size)
    }
}
