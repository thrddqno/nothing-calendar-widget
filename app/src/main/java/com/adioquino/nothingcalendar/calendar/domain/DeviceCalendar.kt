package com.adioquino.nothingcalendar.calendar.domain

data class DeviceCalendar(
    val id: Long,
    val name: String,
    val accountName: String,
    val accountType: String?,
    val color: Int?,
    val isVisible: Boolean,
    val syncEvents: Boolean,
) {
    /** CalendarContract row IDs remain stable for the lifetime of a provider row. */
    val stableId: String
        get() = id.toString()
}
