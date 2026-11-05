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
        val sizes = WidgetSizeResolver.resolve(
            appWidgetManager.getAppWidgetOptions(appWidgetId),
            WidgetSize(DEFAULT_WIDTH_DP, DEFAULT_HEIGHT_DP),
        )
        val now = Instant.now()
        val loadingSnapshot = WidgetSnapshotFactory.empty(
            now = now,
            zoneId = ZoneId.systemDefault(),
            firstDayOfWeek = localeFirstDayOfWeek(),
        )
        ResponsiveRemoteViews.update(appWidgetManager, appWidgetId, sizes) { size ->
            val fontScale = context.resources.configuration.fontScale
            val model = CombinedWidgetPresenter(createAgendaWidgetPresenter(context)).present(
                state = state,
                loadingSnapshot = loadingSnapshot,
                now = now,
                availableHeightDp = size.heightDp,
                availableWidthDp = size.widthDp,
                fontScale = fontScale,
            )
            val spec = model.layout.spec
            val views = RemoteViews(context.packageName, R.layout.widget_combined).apply {
                setInt(R.id.widget_root, "setBackgroundColor", palette.background)
                setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
                setOnClickPendingIntent(R.id.month_title, AppNavigation.calendarPendingIntent(context, now))
                setTextColor(R.id.agenda_header, palette.primaryText)
            }
            MonthSectionRemoteViewsRenderer().render(
                views = views,
                month = model.month,
                primaryTextColor = palette.primaryText,
                secondaryTextColor = palette.secondaryText,
                availableWidthDp = size.widthDp,
                dateTextSizeSp = spec.dateTextSizeSp,
            )
            val agendaRenderer = AgendaSectionRemoteViewsRenderer(context)
            if (model.agendaStatus == CombinedAgendaStatus.EVENTS) {
                agendaRenderer.renderEvents(
                    views = views,
                    model = model.agenda,
                    backgroundColor = palette.background,
                    primaryTextColor = palette.primaryText,
                    secondaryTextColor = palette.secondaryText,
                    maxRows = model.layout.maxAgendaRows,
                    eventTitleTextSizeSp = spec.eventTitleTextSizeSp,
                    eventMetaTextSizeSp = spec.eventMetaTextSizeSp,
                )
            } else {
                agendaRenderer.renderMessage(
                    views = views,
                    message = model.agendaStatus.message(),
                    textColor = palette.secondaryText,
                    maxRows = model.layout.maxAgendaRows,
                )
            }
            ResponsiveRemoteViews.applyLayout(
                views = views,
                kind = WidgetKind.COMBINED,
                spec = spec,
                density = context.resources.displayMetrics.density,
                monthWeekRowIds = MonthSectionRemoteViewsRenderer.WEEK_ROW_IDS,
                agendaRowIds = AgendaSectionRemoteViewsRenderer.ROW_IDS,
            )
            views
        }
    }

    private fun CombinedAgendaStatus.message(): String = when (this) {
        CombinedAgendaStatus.EMPTY -> context.getString(R.string.widget_nothing_in_next_12_hours)
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
        const val DEFAULT_WIDTH_DP = 250
        const val DEFAULT_HEIGHT_DP = 250
    }
}
