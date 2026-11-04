package com.adioquino.nothingcalendar.navigation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.adioquino.nothingcalendar.settings.SettingsActivity
import java.time.Instant

object AppNavigation {
    fun settingsPendingIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        SETTINGS_REQUEST_CODE,
        Intent(context, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun calendarPendingIntent(context: Context, instant: Instant): PendingIntent {
        val calendarUri = CalendarContract.CONTENT_URI.buildUpon()
            .appendPath("time")
            .appendPath(instant.toEpochMilli().toString())
            .build()
        return PendingIntent.getActivity(
            context,
            CALENDAR_REQUEST_CODE,
            Intent(Intent.ACTION_VIEW, calendarUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private const val SETTINGS_REQUEST_CODE = 100
    private const val CALENDAR_REQUEST_CODE = 101
}
