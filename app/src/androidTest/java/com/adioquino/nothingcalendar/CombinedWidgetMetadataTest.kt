package com.adioquino.nothingcalendar

import android.appwidget.AppWidgetManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.adioquino.nothingcalendar.widget.combined.CombinedWidgetProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CombinedWidgetMetadataTest {
    @Test
    @SdkSuppress(minSdkVersion = 31)
    fun combinedWidgetTargetsFourByFourCells() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val provider = AppWidgetManager.getInstance(context).installedProviders
            .single { it.provider.className == CombinedWidgetProvider::class.java.name }

        assertEquals(4, provider.targetCellWidth)
        assertEquals(4, provider.targetCellHeight)
    }
}
