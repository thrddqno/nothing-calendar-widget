package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager

interface WidgetRenderingEngine {
    fun render(appWidgetManager: AppWidgetManager, appWidgetId: Int, kind: WidgetKind)
}
