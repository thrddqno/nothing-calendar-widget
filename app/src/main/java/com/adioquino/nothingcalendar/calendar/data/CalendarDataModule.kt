package com.adioquino.nothingcalendar.calendar.data

import android.content.Context
import com.adioquino.nothingcalendar.settings.DataStoreSettingsRepository

object CalendarDataModule {
    fun repository(context: Context): CalendarRepository {
        val appContext = context.applicationContext
        return AndroidCalendarRepository(
            dataSource = AndroidCalendarDataSource(
                contentResolver = appContext.contentResolver,
                permissionChecker = AndroidCalendarPermissionChecker(appContext),
            ),
            settingsRepository = DataStoreSettingsRepository(appContext),
        )
    }
}
