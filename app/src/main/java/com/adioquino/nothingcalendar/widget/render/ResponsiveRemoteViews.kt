package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.os.Build
import android.util.SizeF
import android.util.TypedValue
import android.widget.RemoteViews

object ResponsiveRemoteViews {
    fun update(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        sizes: List<WidgetSize>,
        createViews: (WidgetSize) -> RemoteViews,
    ) {
        val distinctSizes = sizes.filter(WidgetSize::isValid).distinct()
        if (distinctSizes.isEmpty()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val variants = distinctSizes.associate { size ->
                SizeF(size.widthDp.toFloat(), size.heightDp.toFloat()) to createViews(size)
            }
            appWidgetManager.updateAppWidget(appWidgetId, RemoteViews(variants))
        } else {
            appWidgetManager.updateAppWidget(appWidgetId, createViews(distinctSizes.first()))
        }
    }

    fun applyLayout(
        views: RemoteViews,
        kind: WidgetKind,
        spec: WidgetLayoutSpec,
        density: Float,
        monthWeekRowIds: IntArray = intArrayOf(),
        agendaRowIds: IntArray = intArrayOf(),
    ) {
        val paddingPx = (spec.outerPaddingDp * density).toInt()
        views.setViewPadding(com.adioquino.nothingcalendar.R.id.widget_root, paddingPx, paddingPx, paddingPx, paddingPx)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

        if (kind == WidgetKind.COMBINED) {
            setHeight(views, com.adioquino.nothingcalendar.R.id.month_title, spec.headerHeightDp)
            setHeight(views, com.adioquino.nothingcalendar.R.id.month_weekdays, spec.weekdayHeaderHeightDp)
            monthWeekRowIds.forEach { setHeight(views, it, spec.calendarRowHeightDp) }
        }
        if (kind != WidgetKind.MONTH) {
            setHeight(views, com.adioquino.nothingcalendar.R.id.agenda_header, spec.agendaHeaderHeightDp)
            agendaRowIds.forEach { setHeight(views, it, spec.agendaRowHeightDp) }
            setHeight(views, com.adioquino.nothingcalendar.R.id.agenda_overflow, spec.overflowHeightDp)
        }
    }

    private fun setHeight(views: RemoteViews, viewId: Int, heightDp: Int) {
        if (heightDp > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setViewLayoutHeight(viewId, heightDp.toFloat(), TypedValue.COMPLEX_UNIT_DIP)
        }
    }
}
