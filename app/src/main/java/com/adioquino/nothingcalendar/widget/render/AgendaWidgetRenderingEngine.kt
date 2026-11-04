package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.text.format.DateFormat
import android.view.View
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver
import com.adioquino.nothingcalendar.widget.agenda.AgendaEventFormatter
import com.adioquino.nothingcalendar.widget.agenda.AgendaFormatStrings
import com.adioquino.nothingcalendar.widget.agenda.AgendaIndicatorColorResolver
import com.adioquino.nothingcalendar.widget.agenda.AgendaRenderModel
import com.adioquino.nothingcalendar.widget.agenda.AgendaWidgetPresenter
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

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

        val snapshot = when (state) {
            is WidgetLoadState.Ready -> state.snapshot
            is WidgetLoadState.NoUpcomingEvents -> state.snapshot
            else -> null
        }
        if (snapshot == null) {
            renderState(views, state, palette.secondaryText)
        } else {
            val now = Instant.now()
            val model = presenter().present(
                occurrences = snapshot.agenda.occurrences,
                now = now,
                availableHeightDp = minHeightDp,
            )
            if (model.rows.isEmpty()) {
                renderEmptyState(views, context.getString(R.string.widget_nothing_upcoming), palette.secondaryText)
            } else {
                renderEvents(views, model, palette.background, palette.primaryText, palette.secondaryText)
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun presenter(): AgendaWidgetPresenter = AgendaWidgetPresenter(
        AgendaEventFormatter(
            locale = Locale.getDefault(),
            zoneId = ZoneId.systemDefault(),
            use24HourTime = DateFormat.is24HourFormat(context),
            strings = AgendaFormatStrings(
                allDay = context.getString(R.string.widget_all_day),
                now = context.getString(R.string.widget_now),
                untitledEvent = context.getString(R.string.widget_untitled_event),
            ),
        ),
    )

    private fun renderState(views: RemoteViews, state: WidgetLoadState, textColor: Int) {
        val message = when (state) {
            WidgetLoadState.Loading -> context.getString(R.string.widget_loading)
            is WidgetLoadState.PermissionDenied -> context.getString(R.string.widget_calendar_access_required)
            is WidgetLoadState.NoAvailableCalendars -> context.getString(R.string.widget_no_calendars)
            is WidgetLoadState.ProviderFailure -> context.getString(R.string.widget_provider_failure)
            is WidgetLoadState.Ready, is WidgetLoadState.NoUpcomingEvents -> {
                context.getString(R.string.widget_nothing_upcoming)
            }
        }
        renderEmptyState(views, message, textColor)
    }

    private fun renderEmptyState(views: RemoteViews, message: String, textColor: Int) {
        hideRows(views)
        views.setViewVisibility(R.id.agenda_overflow, View.GONE)
        views.setViewVisibility(R.id.agenda_empty_state, View.VISIBLE)
        views.setTextViewText(R.id.agenda_empty_state, message)
        views.setTextColor(R.id.agenda_empty_state, textColor)
    }

    private fun renderEvents(
        views: RemoteViews,
        model: AgendaRenderModel,
        backgroundColor: Int,
        primaryTextColor: Int,
        secondaryTextColor: Int,
    ) {
        views.setViewVisibility(R.id.agenda_empty_state, View.GONE)
        ROW_IDS.forEachIndexed { index, rowId ->
            val row = model.rows.getOrNull(index)
            views.setViewVisibility(rowId, if (row == null) View.GONE else View.VISIBLE)
            if (row != null) {
                views.setTextViewText(TITLE_IDS[index], row.title)
                views.setTextViewText(META_IDS[index], row.temporalLabel)
                views.setTextColor(TITLE_IDS[index], primaryTextColor)
                views.setTextColor(META_IDS[index], secondaryTextColor)
                views.setInt(
                    ACCENT_IDS[index],
                    "setBackgroundColor",
                    AgendaIndicatorColorResolver.resolve(
                        row.calendarColor,
                        backgroundColor,
                        secondaryTextColor,
                    ),
                )
            }
        }
        views.setViewVisibility(
            R.id.agenda_overflow,
            if (model.showOverflow) View.VISIBLE else View.GONE,
        )
        if (model.showOverflow) {
            views.setTextViewText(
                R.id.agenda_overflow,
                context.getString(R.string.widget_more_events, model.hiddenEventCount),
            )
            views.setTextColor(R.id.agenda_overflow, secondaryTextColor)
        }
    }

    private fun hideRows(views: RemoteViews) {
        ROW_IDS.forEach { rowId -> views.setViewVisibility(rowId, View.GONE) }
    }

    private companion object {
        val ROW_IDS = intArrayOf(
            R.id.agenda_row_0,
            R.id.agenda_row_1,
            R.id.agenda_row_2,
            R.id.agenda_row_3,
        )
        val TITLE_IDS = intArrayOf(
            R.id.agenda_title_0,
            R.id.agenda_title_1,
            R.id.agenda_title_2,
            R.id.agenda_title_3,
        )
        val META_IDS = intArrayOf(
            R.id.agenda_meta_0,
            R.id.agenda_meta_1,
            R.id.agenda_meta_2,
            R.id.agenda_meta_3,
        )
        val ACCENT_IDS = intArrayOf(
            R.id.agenda_accent_0,
            R.id.agenda_accent_1,
            R.id.agenda_accent_2,
            R.id.agenda_accent_3,
        )
    }
}
