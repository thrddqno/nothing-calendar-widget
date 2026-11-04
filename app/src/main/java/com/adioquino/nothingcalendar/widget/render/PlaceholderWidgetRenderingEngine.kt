package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver

class PlaceholderWidgetRenderingEngine(
    private val context: Context,
    private val themeResolver: ThemeResolver = AndroidThemeResolver(context),
) : WidgetRenderingEngine {

    override fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
    ) {
        val palette = themeResolver.resolve()
        val views = RemoteViews(context.packageName, kind.layoutRes).apply {
            setTextViewText(R.id.placeholder_title, context.getString(kind.titleRes))
            setTextViewText(R.id.placeholder_message, context.getString(kind.messageRes))
            setTextColor(R.id.placeholder_title, palette.primaryText)
            setTextColor(R.id.placeholder_message, palette.secondaryText)
            setInt(R.id.widget_root, "setBackgroundColor", palette.background)
            setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
