package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import com.adioquino.nothingcalendar.widget.today.TodayAgendaStatus
import com.adioquino.nothingcalendar.widget.today.TodayAgendaWidgetPresenter
import com.adioquino.nothingcalendar.widget.today.TodayDatePresenter
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

class TodayAgendaWidgetRenderingEngine(
    private val context: Context,
    private val themeResolver: ThemeResolver = AndroidThemeResolver(context),
) : WidgetRenderingEngine {
    override fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        state: WidgetLoadState,
    ) {
        require(kind == WidgetKind.TODAY_AGENDA) {
            "Today agenda renderer only supports today agenda widgets"
        }
        val palette = themeResolver.resolve()
        val sizes = WidgetSizeResolver.resolve(
            appWidgetManager.getAppWidgetOptions(appWidgetId),
            WidgetSize(DEFAULT_WIDTH_DP, DEFAULT_HEIGHT_DP),
        )
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val fontScale = context.resources.configuration.fontScale
        val loadingSnapshot = WidgetSnapshotFactory.empty(
            now = now,
            zoneId = zoneId,
            firstDayOfWeek = localeFirstDayOfWeek(),
        )
        val calendarIntent = AppNavigation.calendarPendingIntent(context, now)
        ResponsiveRemoteViews.update(appWidgetManager, appWidgetId, sizes) { size ->
            val model = TodayAgendaWidgetPresenter(
                datePresenter = TodayDatePresenter(Locale.getDefault(), zoneId),
                agendaPresenter = createAgendaWidgetPresenter(context, includeEndTime = true),
            ).present(
                state = state,
                loadingSnapshot = loadingSnapshot,
                now = now,
                availableWidthDp = size.widthDp,
                availableHeightDp = size.heightDp,
                fontScale = fontScale,
            )
            val spec = WidgetResponsiveSizingPolicy.calculate(
                WidgetSizingInput(
                    kind = WidgetKind.TODAY_AGENDA,
                    availableWidthDp = size.widthDp,
                    availableHeightDp = size.heightDp,
                    fontScale = fontScale,
                    availableAgendaEventCount = model.agenda.totalEligibleEvents,
                ),
            )
            val views = RemoteViews(context.packageName, R.layout.widget_today_agenda).apply {
                setInt(R.id.widget_root, "setBackgroundColor", palette.background)
                setInt(R.id.today_divider, "setBackgroundColor", palette.secondaryText)
                setTextViewText(R.id.today_month, model.date.month)
                setTextViewText(R.id.today_day, model.date.day)
                setTextViewText(R.id.today_weekday, model.date.weekday)
                setTextColor(R.id.today_month, palette.secondaryText)
                setTextColor(R.id.today_day, palette.primaryText)
                setTextColor(R.id.today_weekday, palette.secondaryText)
                setTextColor(R.id.agenda_header, palette.primaryText)
                setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
                setOnClickPendingIntent(R.id.today_date_section, calendarIntent)
                setOnClickPendingIntent(R.id.agenda_header, calendarIntent)
            }
            val agendaRenderer = AgendaSectionRemoteViewsRenderer(context)
            if (model.agendaStatus == TodayAgendaStatus.EVENTS) {
                agendaRenderer.renderEvents(
                    views = views,
                    model = model.agenda,
                    backgroundColor = palette.background,
                    primaryTextColor = palette.primaryText,
                    secondaryTextColor = palette.secondaryText,
                    maxRows = EVENT_ROW_IDS.size,
                )
                model.agenda.rows.forEachIndexed { index, row ->
                    views.setOnClickPendingIntent(
                        EVENT_ROW_IDS[index],
                        AppNavigation.eventPendingIntent(
                            context = context,
                            eventId = row.eventId,
                            startsAt = row.startsAt,
                            endsAt = row.endsAt,
                        ),
                    )
                }
                if (model.agenda.showOverflow) {
                    views.setTextViewText(
                        R.id.agenda_overflow,
                        context.getString(
                            R.string.widget_more_events_compact,
                            model.agenda.hiddenEventCount,
                        ),
                    )
                    views.setOnClickPendingIntent(R.id.agenda_overflow, calendarIntent)
                }
            } else {
                agendaRenderer.renderMessage(
                    views = views,
                    message = model.agendaStatus.message(),
                    textColor = palette.secondaryText,
                )
            }
            ResponsiveRemoteViews.applyLayout(
                views = views,
                kind = WidgetKind.TODAY_AGENDA,
                spec = spec,
                density = context.resources.displayMetrics.density,
                agendaRowIds = EVENT_ROW_IDS,
            )
            views
        }
    }

    private fun TodayAgendaStatus.message(): String = when (this) {
        TodayAgendaStatus.EMPTY -> context.getString(R.string.widget_nothing_in_next_12_hours)
        TodayAgendaStatus.LOADING -> context.getString(R.string.widget_loading_compact)
        TodayAgendaStatus.PERMISSION_DENIED -> {
            context.getString(R.string.widget_calendar_access_required)
        }
        TodayAgendaStatus.NO_CALENDARS -> context.getString(R.string.widget_no_calendars_available)
        TodayAgendaStatus.PROVIDER_FAILURE -> context.getString(R.string.widget_provider_failure)
        TodayAgendaStatus.EVENTS -> error("Event status does not have a message")
    }

    private fun localeFirstDayOfWeek(): DayOfWeek = when (
        WeekFields.of(Locale.getDefault()).firstDayOfWeek
    ) {
        DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }

    private companion object {
        const val DEFAULT_WIDTH_DP = 250
        const val DEFAULT_HEIGHT_DP = 110

        val EVENT_ROW_IDS = intArrayOf(
            R.id.agenda_row_0,
            R.id.agenda_row_1,
            R.id.agenda_row_2,
        )
    }
}
