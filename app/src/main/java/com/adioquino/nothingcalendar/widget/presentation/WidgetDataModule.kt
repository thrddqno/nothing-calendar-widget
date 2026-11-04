package com.adioquino.nothingcalendar.widget.presentation

import android.content.Context
import com.adioquino.nothingcalendar.calendar.data.AndroidCalendarPermissionChecker
import com.adioquino.nothingcalendar.calendar.data.CalendarDataModule
import com.adioquino.nothingcalendar.settings.DataStoreSettingsRepository

object WidgetDataModule {
    fun loader(context: Context): WidgetDataLoader {
        val appContext = context.applicationContext
        val settingsRepository = DataStoreSettingsRepository(appContext)
        val permissionChecker = AndroidCalendarPermissionChecker(appContext)
        return WidgetDataLoader(
            calendarRepository = CalendarDataModule.repository(
                context = appContext,
                settingsRepository = settingsRepository,
                permissionChecker = permissionChecker,
            ),
            settingsRepository = settingsRepository,
            permissionChecker = permissionChecker,
        )
    }
}
