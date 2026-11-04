package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshot
import java.time.format.DateTimeFormatter
import java.util.Locale

class PlaceholderWidgetRenderingEngine(
    private val context: Context,
    private val themeResolver: ThemeResolver = AndroidThemeResolver(context),
) : WidgetRenderingEngine {

    override fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        state: WidgetLoadState,
    ) {
        val palette = themeResolver.resolve()
        val (title, message) = diagnosticText(kind, state)
        val views = RemoteViews(context.packageName, kind.layoutRes).apply {
            setTextViewText(R.id.placeholder_title, title)
            setTextViewText(R.id.placeholder_message, message)
            setTextColor(R.id.placeholder_title, palette.primaryText)
            setTextColor(R.id.placeholder_message, palette.secondaryText)
            setInt(R.id.widget_root, "setBackgroundColor", palette.background)
            setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun diagnosticText(kind: WidgetKind, state: WidgetLoadState): Pair<String, String> = when (state) {
        WidgetLoadState.Loading -> context.getString(kind.titleRes) to context.getString(R.string.widget_loading)
        is WidgetLoadState.PermissionDenied -> {
            context.getString(kind.titleRes) to context.getString(R.string.widget_permission_required)
        }
        is WidgetLoadState.NoAvailableCalendars -> {
            context.getString(kind.titleRes) to context.getString(R.string.widget_no_calendars)
        }
        is WidgetLoadState.ProviderFailure -> {
            context.getString(kind.titleRes) to context.getString(R.string.widget_provider_failure)
        }
        is WidgetLoadState.Ready -> loadedText(kind, state.snapshot)
        is WidgetLoadState.NoUpcomingEvents -> loadedText(kind, state.snapshot)
    }

    private fun loadedText(kind: WidgetKind, snapshot: WidgetSnapshot): Pair<String, String> {
        val monthTitle = snapshot.month.displayedMonth.format(
            DateTimeFormatter.ofPattern("MMMM uuuu", Locale.getDefault()),
        )
        val eventCount = snapshot.agenda.totalEligibleEvents
        val eventMessage = context.resources.getQuantityString(
            R.plurals.widget_upcoming_event_count,
            eventCount,
            eventCount,
        )
        return when (kind) {
            WidgetKind.MONTH -> monthTitle to context.getString(R.string.widget_calendar_loaded)
            WidgetKind.AGENDA -> context.getString(kind.titleRes) to eventMessage
            WidgetKind.COMBINED -> monthTitle to eventMessage
        }
    }
}
