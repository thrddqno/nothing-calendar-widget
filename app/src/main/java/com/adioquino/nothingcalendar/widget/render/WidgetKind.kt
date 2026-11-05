package com.adioquino.nothingcalendar.widget.render

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.adioquino.nothingcalendar.R

enum class WidgetKind(
    @param:LayoutRes val layoutRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val messageRes: Int,
) {
    MONTH(
        R.layout.widget_month,
        R.string.widget_month_placeholder_title,
        R.string.widget_month_placeholder_message,
    ),
    AGENDA(
        R.layout.widget_agenda,
        R.string.widget_agenda_placeholder_title,
        R.string.widget_agenda_placeholder_message,
    ),
    COMBINED(
        R.layout.widget_combined,
        R.string.widget_combined_placeholder_title,
        R.string.widget_combined_placeholder_message,
    ),
    TODAY_AGENDA(
        R.layout.widget_today_agenda,
        R.string.widget_today_agenda_name,
        R.string.widget_agenda_placeholder_message,
    ),
}
