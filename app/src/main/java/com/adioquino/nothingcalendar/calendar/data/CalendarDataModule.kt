package com.adioquino.nothingcalendar.calendar.data

import android.content.Context
import com.adioquino.nothingcalendar.settings.SettingsRepository
import com.adioquino.nothingcalendar.settings.DataStoreSettingsRepository

object CalendarDataModule {
    fun repository(context: Context): CalendarRepository {
        val appContext = context.applicationContext
        return repository(
            context = appContext,
            settingsRepository = DataStoreSettingsRepository(appContext),
            permissionChecker = AndroidCalendarPermissionChecker(appContext),
        )
    }

    fun repository(
        context: Context,
        settingsRepository: SettingsRepository,
        permissionChecker: CalendarPermissionChecker,
    ): CalendarRepository {
        val appContext = context.applicationContext
        return AndroidCalendarRepository(
            dataSource = AndroidCalendarDataSource(
                contentResolver = appContext.contentResolver,
                permissionChecker = permissionChecker,
            ),
            settingsRepository = settingsRepository,
        )
    }
}
