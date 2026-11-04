package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetMonthState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class MonthWidgetRenderingEngine(
    private val context: Context,
    private val themeResolver: ThemeResolver = AndroidThemeResolver(context),
) : WidgetRenderingEngine {
    override fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        state: WidgetLoadState,
    ) {
        require(kind == WidgetKind.MONTH) { "Month renderer only supports month widgets" }
        val palette = themeResolver.resolve()
        val month = state.monthState()
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110)
        val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
        val views = RemoteViews(context.packageName, R.layout.widget_month).apply {
            setTextViewText(R.id.month_title, formatMonthTitle(month, minWidthDp))
            setTextColor(R.id.month_title, palette.primaryText)
            setInt(R.id.widget_root, "setBackgroundColor", palette.background)
            setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
            renderStatus(this, state, palette.secondaryText)
            renderWeekdays(this, month, palette.secondaryText)
            renderDays(this, month, palette.primaryText, minHeightDp)
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun WidgetLoadState.monthState(): WidgetMonthState = when (this) {
        is WidgetLoadState.Ready -> snapshot.month
        is WidgetLoadState.NoUpcomingEvents -> snapshot.month
        is WidgetLoadState.PermissionDenied -> snapshot.month
        is WidgetLoadState.NoAvailableCalendars -> snapshot.month
        is WidgetLoadState.ProviderFailure -> snapshot.month
        WidgetLoadState.Loading -> WidgetSnapshotFactory.empty(
            now = Instant.now(),
            zoneId = ZoneId.systemDefault(),
            firstDayOfWeek = localeFirstDayOfWeek(),
        ).month
    }

    private fun renderStatus(views: RemoteViews, state: WidgetLoadState, textColor: Int) {
        val statusText = when (state) {
            WidgetLoadState.Loading -> context.getString(R.string.widget_loading_compact)
            is WidgetLoadState.PermissionDenied -> context.getString(R.string.widget_permission_required)
            is WidgetLoadState.NoAvailableCalendars -> context.getString(R.string.widget_no_calendars)
            is WidgetLoadState.ProviderFailure -> context.getString(R.string.widget_events_unavailable)
            is WidgetLoadState.Ready, is WidgetLoadState.NoUpcomingEvents -> null
        }
        views.setViewVisibility(R.id.month_status, if (statusText == null) View.GONE else View.VISIBLE)
        if (statusText != null) {
            views.setTextViewText(R.id.month_status, statusText)
            views.setTextColor(R.id.month_status, textColor)
        }
    }

    private fun renderWeekdays(views: RemoteViews, month: WidgetMonthState, textColor: Int) {
        WEEKDAY_VIEW_IDS.forEachIndexed { index, viewId ->
            val day = month.firstDayOfWeek.plus(index.toLong())
            views.setTextViewText(viewId, day.getDisplayName(TextStyle.NARROW, Locale.getDefault()))
            views.setTextColor(viewId, textColor)
        }
    }

    private fun renderDays(
        views: RemoteViews,
        month: WidgetMonthState,
        textColor: Int,
        minHeightDp: Int,
    ) {
        WEEK_ROW_IDS.forEachIndexed { index, rowId ->
            views.setViewVisibility(rowId, if (index < month.weekCount) View.VISIBLE else View.GONE)
        }
        val dateTextSize = if (minHeightDp < COMPACT_HEIGHT_DP) 8f else 10f
        DAY_VIEW_IDS.forEachIndexed { index, viewId ->
            val cell = month.cells.getOrNull(index)
            val visibleCell = cell?.takeIf { it.belongsToDisplayedMonth }
            val text = when {
                visibleCell == null -> ""
                visibleCell.indicators.count > 0 -> "${visibleCell.date.dayOfMonth}•"
                else -> visibleCell.date.dayOfMonth.toString()
            }
            views.setTextViewText(viewId, text)
            views.setTextColor(viewId, textColor)
            views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, dateTextSize)
            views.setInt(
                viewId,
                "setBackgroundResource",
                if (visibleCell?.isToday == true) R.drawable.widget_month_today else 0,
            )
        }
    }

    private fun formatMonthTitle(month: WidgetMonthState, minWidthDp: Int): String {
        val pattern = when {
            minWidthDp >= WIDE_WIDTH_DP -> "MMMM uuuu"
            minWidthDp >= MEDIUM_WIDTH_DP -> "MMM uuuu"
            else -> "MMM"
        }
        return month.displayedMonth.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    private fun localeFirstDayOfWeek(): DayOfWeek = when (
        WeekFields.of(Locale.getDefault()).firstDayOfWeek
    ) {
        DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }

    private companion object {
        const val MEDIUM_WIDTH_DP = 140
        const val WIDE_WIDTH_DP = 180
        const val COMPACT_HEIGHT_DP = 125

        val WEEKDAY_VIEW_IDS = intArrayOf(
            R.id.month_weekday_0,
            R.id.month_weekday_1,
            R.id.month_weekday_2,
            R.id.month_weekday_3,
            R.id.month_weekday_4,
            R.id.month_weekday_5,
            R.id.month_weekday_6,
        )

        val WEEK_ROW_IDS = intArrayOf(
            R.id.month_week_0,
            R.id.month_week_1,
            R.id.month_week_2,
            R.id.month_week_3,
            R.id.month_week_4,
            R.id.month_week_5,
        )

        val DAY_VIEW_IDS = intArrayOf(
            R.id.month_day_0, R.id.month_day_1, R.id.month_day_2, R.id.month_day_3,
            R.id.month_day_4, R.id.month_day_5, R.id.month_day_6, R.id.month_day_7,
            R.id.month_day_8, R.id.month_day_9, R.id.month_day_10, R.id.month_day_11,
            R.id.month_day_12, R.id.month_day_13, R.id.month_day_14, R.id.month_day_15,
            R.id.month_day_16, R.id.month_day_17, R.id.month_day_18, R.id.month_day_19,
            R.id.month_day_20, R.id.month_day_21, R.id.month_day_22, R.id.month_day_23,
            R.id.month_day_24, R.id.month_day_25, R.id.month_day_26, R.id.month_day_27,
            R.id.month_day_28, R.id.month_day_29, R.id.month_day_30, R.id.month_day_31,
            R.id.month_day_32, R.id.month_day_33, R.id.month_day_34, R.id.month_day_35,
            R.id.month_day_36, R.id.month_day_37, R.id.month_day_38, R.id.month_day_39,
            R.id.month_day_40, R.id.month_day_41,
        )
    }
}
