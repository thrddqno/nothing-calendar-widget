package com.adioquino.nothingcalendar

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestrictedRemoteViewsProbeTest {
    @Test
    fun inflateEveryWidgetLayoutWithRestrictedHostContext() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val target = instrumentation.targetContext
        val host = instrumentation.context
        val doto = target.resources.getFont(R.font.doto_variable)

        listOf(
            R.layout.widget_month,
            R.layout.widget_agenda,
            R.layout.widget_today_agenda,
            R.layout.widget_combined,
        ).forEach { layout ->
            val restrictedRoot = RemoteViews(target.packageName, layout).apply(host, null)
            restrictedRoot.findViewById<TextView>(R.id.today_day)?.let { display ->
                val dotoPaint = Paint().apply {
                    typeface = doto
                    textSize = display.textSize
                    fontVariationSettings = display.fontVariationSettings
                }
                val sansPaint = Paint().apply {
                    typeface = Typeface.SANS_SERIF
                    textSize = display.textSize
                }
                Log.i(
                    "RestrictedProbe",
                    "restricted layout=$layout actual=${display.paint.measureText("05")} " +
                        "doto=${dotoPaint.measureText("05")} " +
                        "sans=${sansPaint.measureText("05")} " +
                        "variation=${display.fontVariationSettings}",
                )
            }
        }
    }
}
