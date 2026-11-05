package com.adioquino.nothingcalendar

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
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

    @Test
    fun bundledDotoFontLoadsAndMapsToDisplayRoles() {
        assertNotNull(context.resources.getFont(R.font.doto_variable))

        val month = inflater.inflate(R.layout.widget_month, null, false)
        val today = inflater.inflate(R.layout.widget_today_agenda, null, false)
        val heading = month.findViewById<TextView>(R.id.month_title)
        val display = today.findViewById<TextView>(R.id.today_day)

        assertTrue(heading.fontVariationSettings.orEmpty().contains("wght"))
        assertTrue(heading.fontVariationSettings.orEmpty().contains("700"))
        assertTrue(display.fontVariationSettings.orEmpty().contains("wght"))
        assertTrue(display.fontVariationSettings.orEmpty().contains("800"))
        assertTrue(heading.paint.hasGlyph("日"))
    }

    @Test
    fun readableContentUsesSystemSansRoles() {
        val combined = inflater.inflate(R.layout.widget_combined, null, false)
        val weekday = combined.findViewById<TextView>(R.id.month_weekday_0)
        val date = combined.findViewById<TextView>(R.id.month_day_0)
        val title = combined.findViewById<TextView>(R.id.agenda_title_0)
        val metadata = combined.findViewById<TextView>(R.id.agenda_meta_0)

        assertEquals(400, weekday.typeface.weight)
        assertEquals(500, date.typeface.weight)
        assertEquals(500, title.typeface.weight)
        assertEquals(400, metadata.typeface.weight)
        assertTrue(weekday.fontVariationSettings.isNullOrEmpty())
        assertTrue(title.fontVariationSettings.isNullOrEmpty())
    }

    @Test
    fun semanticTokensMatchAcrossWidgetLayouts() {
        val density = context.resources.displayMetrics.density
        assertEquals(0xFF242725.toInt(), context.getColor(R.color.widget_surface_base))
        assertEquals(0xFFE54848.toInt(), context.getColor(R.color.widget_accent_nothing))
        assertEquals(
            3f * density,
            context.resources.getDimension(R.dimen.widget_event_accent_width),
            0.01f,
        )

        listOf(
            R.layout.widget_month,
            R.layout.widget_agenda,
            R.layout.widget_today_agenda,
            R.layout.widget_combined,
        ).forEach { layout ->
            val root = inflater.inflate(layout, null, false)
            assertNotNull(root.background)
        }
    }

    @Test
    fun remoteViewsInflationRetainsBundledDisplayFont() {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_today_agenda)
        val root = remoteViews.apply(context, null)
        val display = root.findViewById<TextView>(R.id.today_day)

        assertNotNull(display)
        assertTrue(display.fontVariationSettings.orEmpty().contains("800"))
        assertEquals(800, display.typeface.weight)
    }
}
