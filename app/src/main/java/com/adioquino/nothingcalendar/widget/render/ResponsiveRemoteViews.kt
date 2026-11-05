package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.os.Build
import android.util.SizeF
import android.util.TypedValue
import android.view.View
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
        val horizontalPaddingPx = (spec.horizontalPaddingDp * density).toInt()
        val verticalPaddingPx = (spec.verticalPaddingDp * density).toInt()
        views.setViewPadding(
            com.adioquino.nothingcalendar.R.id.widget_root,
            horizontalPaddingPx,
            verticalPaddingPx,
            horizontalPaddingPx,
            verticalPaddingPx,
        )
        if (kind == WidgetKind.MONTH || kind == WidgetKind.COMBINED) {
            views.setViewVisibility(
                com.adioquino.nothingcalendar.R.id.month_weekdays,
                if (spec.showWeekdayHeader) View.VISIBLE else View.GONE,
            )
        }
        if (kind != WidgetKind.MONTH) {
            views.setViewVisibility(
                com.adioquino.nothingcalendar.R.id.agenda_header,
                if (spec.showAgendaHeader) View.VISIBLE else View.GONE,
            )
            views.setTextViewTextSize(
                com.adioquino.nothingcalendar.R.id.agenda_header,
                TypedValue.COMPLEX_UNIT_SP,
                spec.sectionHeadingTextSizeSp,
            )
        }
        if (kind == WidgetKind.COMBINED) {
            val horizontalInsetPx = (spec.agendaPanelHorizontalInsetDp * density).toInt()
            val verticalInsetPx = (spec.agendaPanelVerticalInsetDp * density).toInt()
            views.setViewPadding(
                com.adioquino.nothingcalendar.R.id.agenda_panel,
                horizontalInsetPx,
                verticalInsetPx,
                horizontalInsetPx,
                verticalInsetPx,
            )
        }
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
