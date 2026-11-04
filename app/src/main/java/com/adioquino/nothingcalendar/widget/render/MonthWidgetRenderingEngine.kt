package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
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
            setInt(R.id.widget_root, "setBackgroundColor", palette.background)
            setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
            renderStatus(this, state, palette.secondaryText)
            MonthSectionRemoteViewsRenderer().render(
                views = this,
                month = month,
                primaryTextColor = palette.primaryText,
                secondaryTextColor = palette.secondaryText,
                availableWidthDp = minWidthDp,
                dateTextSizeSp = if (minHeightDp < COMPACT_HEIGHT_DP) 8f else 10f,
            )
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

    private fun localeFirstDayOfWeek(): DayOfWeek = when (
        WeekFields.of(Locale.getDefault()).firstDayOfWeek
    ) {
        DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }

    private companion object {
        const val COMPACT_HEIGHT_DP = 125
    }
}
