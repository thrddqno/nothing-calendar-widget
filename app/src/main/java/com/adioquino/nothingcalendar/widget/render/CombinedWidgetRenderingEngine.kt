package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver
import com.adioquino.nothingcalendar.widget.combined.CombinedAgendaStatus
import com.adioquino.nothingcalendar.widget.combined.CombinedWidgetPresenter
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

class CombinedWidgetRenderingEngine(
    private val context: Context,
    private val themeResolver: ThemeResolver = AndroidThemeResolver(context),
) : WidgetRenderingEngine {
    override fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        state: WidgetLoadState,
    ) {
        require(kind == WidgetKind.COMBINED) { "Combined renderer only supports combined widgets" }
        val palette = themeResolver.resolve()
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
        val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 400)
        val now = Instant.now()
        val loadingSnapshot = WidgetSnapshotFactory.empty(
            now = now,
            zoneId = ZoneId.systemDefault(),
            firstDayOfWeek = localeFirstDayOfWeek(),
        )
        val model = CombinedWidgetPresenter(createAgendaWidgetPresenter(context)).present(
            state = state,
            loadingSnapshot = loadingSnapshot,
            now = now,
            availableHeightDp = minHeightDp,
        )
        val views = RemoteViews(context.packageName, R.layout.widget_combined).apply {
            setInt(R.id.widget_root, "setBackgroundColor", palette.background)
            setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
            setOnClickPendingIntent(R.id.month_title, AppNavigation.calendarPendingIntent(context, now))
            setTextColor(R.id.agenda_header, palette.primaryText)
            setInt(R.id.combined_divider, "setBackgroundColor", palette.secondaryText)
        }
        MonthSectionRemoteViewsRenderer().render(
            views = views,
            month = model.month,
            primaryTextColor = palette.primaryText,
            secondaryTextColor = palette.secondaryText,
            availableWidthDp = minWidthDp,
            dateTextSizeSp = COMBINED_DATE_TEXT_SIZE_SP,
        )
        val agendaRenderer = AgendaSectionRemoteViewsRenderer(context)
        if (model.agendaStatus == CombinedAgendaStatus.EVENTS) {
            agendaRenderer.renderEvents(
                views = views,
                model = model.agenda,
                backgroundColor = palette.background,
                primaryTextColor = palette.primaryText,
                secondaryTextColor = palette.secondaryText,
            )
        } else {
            agendaRenderer.renderMessage(
                views = views,
                message = model.agendaStatus.message(),
                textColor = palette.secondaryText,
            )
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun CombinedAgendaStatus.message(): String = when (this) {
        CombinedAgendaStatus.EMPTY -> context.getString(R.string.widget_nothing_upcoming)
        CombinedAgendaStatus.LOADING -> context.getString(R.string.widget_loading_compact)
        CombinedAgendaStatus.PERMISSION_DENIED -> {
            context.getString(R.string.widget_calendar_access_required)
        }
        CombinedAgendaStatus.NO_CALENDARS -> context.getString(R.string.widget_no_calendars_available)
        CombinedAgendaStatus.PROVIDER_FAILURE -> context.getString(R.string.widget_provider_failure)
        CombinedAgendaStatus.EVENTS -> error("Event status does not have a message")
    }

    private fun localeFirstDayOfWeek(): DayOfWeek = when (
        WeekFields.of(Locale.getDefault()).firstDayOfWeek
    ) {
        DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }

    private companion object {
        const val COMBINED_DATE_TEXT_SIZE_SP = 10f
    }
}
