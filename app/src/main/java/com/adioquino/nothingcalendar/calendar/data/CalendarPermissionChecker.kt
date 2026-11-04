package com.adioquino.nothingcalendar.calendar.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun interface CalendarPermissionChecker {
    fun canReadCalendar(): Boolean
}

class AndroidCalendarPermissionChecker(context: Context) : CalendarPermissionChecker {
    private val appContext = context.applicationContext

    override fun canReadCalendar(): Boolean = ContextCompat.checkSelfPermission(
        appContext,
        Manifest.permission.READ_CALENDAR,
    ) == PackageManager.PERMISSION_GRANTED
}
