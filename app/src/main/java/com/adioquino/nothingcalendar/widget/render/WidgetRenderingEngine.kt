package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState

interface WidgetRenderingEngine {
    fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        state: WidgetLoadState,
    )
}
