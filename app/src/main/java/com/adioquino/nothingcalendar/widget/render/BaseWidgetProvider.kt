package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

abstract class BaseWidgetProvider(
    private val kind: WidgetKind,
) : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        WidgetUpdateCoordinator.update(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            kind = kind,
            pendingResult = goAsync(),
        )
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        WidgetUpdateCoordinator.invalidate(appWidgetIds)
        super.onDeleted(context, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        WidgetUpdateCoordinator.update(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = intArrayOf(appWidgetId),
            kind = kind,
            pendingResult = goAsync(),
        )
    }
}
