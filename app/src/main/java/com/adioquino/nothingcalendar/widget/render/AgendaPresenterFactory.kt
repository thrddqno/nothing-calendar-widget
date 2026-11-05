package com.adioquino.nothingcalendar.widget.render

import android.content.Context
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.widget.agenda.AgendaEventFormatter
import com.adioquino.nothingcalendar.widget.agenda.AgendaFormatStrings
import com.adioquino.nothingcalendar.widget.agenda.AgendaWidgetPresenter
import java.time.ZoneId
import java.util.Locale

fun createAgendaWidgetPresenter(
    context: Context,
    includeEndTime: Boolean = false,
): AgendaWidgetPresenter = AgendaWidgetPresenter(
    AgendaEventFormatter(
        locale = Locale.getDefault(),
        zoneId = ZoneId.systemDefault(),
        use24HourTime = false,
        strings = AgendaFormatStrings(
            allDay = context.getString(R.string.widget_all_day),
            now = context.getString(R.string.widget_now),
            untitledEvent = context.getString(R.string.widget_untitled_event),
        ),
        includeEndTime = includeEndTime,
    ),
)
