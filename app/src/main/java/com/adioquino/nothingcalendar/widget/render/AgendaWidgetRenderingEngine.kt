package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import java.time.Instant

class AgendaWidgetRenderingEngine(
    private val context: Context,
    private val themeResolver: ThemeResolver = AndroidThemeResolver(context),
) : WidgetRenderingEngine {
    override fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        state: WidgetLoadState,
    ) {
        require(kind == WidgetKind.AGENDA) { "Agenda renderer only supports agenda widgets" }
        val palette = themeResolver.resolve()
        val views = RemoteViews(context.packageName, R.layout.widget_agenda)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        views.setInt(R.id.widget_root, "setBackgroundColor", palette.background)
        views.setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
        views.setTextColor(R.id.agenda_header, palette.primaryText)
        val sectionRenderer = AgendaSectionRemoteViewsRenderer(context)

        val snapshot = when (state) {
            is WidgetLoadState.Ready -> state.snapshot
            is WidgetLoadState.NoUpcomingEvents -> state.snapshot
            else -> null
        }
        if (snapshot == null) {
            sectionRenderer.renderMessage(views, state.message(), palette.secondaryText)
        } else {
            val now = Instant.now()
            val model = createAgendaWidgetPresenter(context).present(
                occurrences = snapshot.agenda.occurrences,
                now = now,
                availableHeightDp = minHeightDp,
            )
            if (model.rows.isEmpty()) {
                sectionRenderer.renderMessage(
                    views,
                    context.getString(R.string.widget_nothing_in_next_12_hours),
                    palette.secondaryText,
                )
            } else {
                sectionRenderer.renderEvents(
                    views,
                    model,
                    palette.background,
                    palette.primaryText,
                    palette.secondaryText,
                )
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun WidgetLoadState.message(): String = when (this) {
        WidgetLoadState.Loading -> context.getString(R.string.widget_loading)
        is WidgetLoadState.PermissionDenied -> context.getString(R.string.widget_calendar_access_required)
        is WidgetLoadState.NoAvailableCalendars -> context.getString(R.string.widget_no_calendars)
        is WidgetLoadState.ProviderFailure -> context.getString(R.string.widget_provider_failure)
        is WidgetLoadState.Ready, is WidgetLoadState.NoUpcomingEvents -> {
            context.getString(R.string.widget_nothing_in_next_12_hours)
        }
    }
}
