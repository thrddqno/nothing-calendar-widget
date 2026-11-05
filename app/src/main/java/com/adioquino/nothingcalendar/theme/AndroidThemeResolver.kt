package com.adioquino.nothingcalendar.theme

import android.content.Context
import com.adioquino.nothingcalendar.R

class AndroidThemeResolver(context: Context) : ThemeResolver {
    private val appContext = context.applicationContext

    override fun resolve(): WidgetPalette = WidgetPalette(
        background = appContext.getColor(R.color.widget_surface_base),
        panelBackground = appContext.getColor(R.color.widget_surface_panel),
        primaryText = appContext.getColor(R.color.widget_text_primary),
        secondaryText = appContext.getColor(R.color.widget_text_secondary),
        accent = appContext.getColor(R.color.widget_accent_nothing),
        todayForeground = appContext.getColor(R.color.widget_today_foreground),
        divider = appContext.getColor(R.color.widget_divider),
    )
}
