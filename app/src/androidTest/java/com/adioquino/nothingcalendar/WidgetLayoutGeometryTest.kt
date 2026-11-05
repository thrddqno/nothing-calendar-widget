package com.adioquino.nothingcalendar

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetLayoutGeometryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val inflater = LayoutInflater.from(context)

    @Test
    fun monthDateAndIndicatorUseSeparateSiblingViews() {
        val root = inflater.inflate(R.layout.widget_month, null, false)
        val date = root.findViewById<TextView>(R.id.month_day_0)
        val indicator = root.findViewById<ImageView>(R.id.month_indicator_0)

        assertSame(date.parent, indicator.parent)
        assertTrue(date.text.none { it == '•' })
    }

    @Test
    fun combinedAgendaIsNestedRoundedPanel() {
        val root = inflater.inflate(R.layout.widget_combined, null, false) as ViewGroup
        val panel = root.findViewById<ViewGroup>(R.id.agenda_panel)

        assertSame(root, panel.parent)
        assertNotNull(panel.background)
        assertTrue(panel.layoutParams.height == 0)
    }
}
