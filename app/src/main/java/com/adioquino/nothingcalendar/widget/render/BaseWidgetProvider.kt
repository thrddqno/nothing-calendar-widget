package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

abstract class BaseWidgetProvider(
    private val kind: WidgetKind,
) : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val renderer = PlaceholderWidgetRenderingEngine(context)
        appWidgetIds.forEach { appWidgetId ->
            renderer.render(appWidgetManager, appWidgetId, kind)
        }
    }
}
