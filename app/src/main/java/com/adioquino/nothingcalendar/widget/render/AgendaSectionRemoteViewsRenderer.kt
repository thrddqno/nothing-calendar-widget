package com.adioquino.nothingcalendar.widget.render

import android.content.Context
import android.view.View
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.widget.agenda.AgendaIndicatorColorResolver
import com.adioquino.nothingcalendar.widget.agenda.AgendaRenderModel

class AgendaSectionRemoteViewsRenderer(
    private val context: Context,
) {
    fun renderMessage(views: RemoteViews, message: String, textColor: Int) {
        hideRows(views)
        views.setViewVisibility(R.id.agenda_overflow, View.GONE)
        views.setViewVisibility(R.id.agenda_empty_state, View.VISIBLE)
        views.setTextViewText(R.id.agenda_empty_state, message)
        views.setTextColor(R.id.agenda_empty_state, textColor)
    }

    fun renderEvents(
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
