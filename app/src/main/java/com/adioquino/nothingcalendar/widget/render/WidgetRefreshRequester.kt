package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.adioquino.nothingcalendar.widget.agenda.AgendaWidgetProvider
import com.adioquino.nothingcalendar.widget.combined.CombinedWidgetProvider
import com.adioquino.nothingcalendar.widget.month.MonthWidgetProvider
import com.adioquino.nothingcalendar.widget.today.TodayAgendaWidgetProvider

object WidgetRefreshRequester {
    fun refreshAll(context: Context) {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        PROVIDERS.forEach { providerClass ->
            val component = ComponentName(appContext, providerClass)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            if (appWidgetIds.isNotEmpty()) {
                appContext.sendBroadcast(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                        this.component = component
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                    },
                )
            }
        }
    }

    private val PROVIDERS = listOf(
        MonthWidgetProvider::class.java,
        AgendaWidgetProvider::class.java,
        CombinedWidgetProvider::class.java,
        TodayAgendaWidgetProvider::class.java,
    )
}
