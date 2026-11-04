package com.adioquino.nothingcalendar.navigation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.adioquino.nothingcalendar.settings.SettingsActivity

object AppNavigation {
    fun settingsPendingIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        SETTINGS_REQUEST_CODE,
        Intent(context, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private const val SETTINGS_REQUEST_CODE = 100
}
