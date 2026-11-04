package com.adioquino.nothingcalendar.calendar.domain

object CalendarSelectionResolver {
    fun resolve(
        calendars: List<DeviceCalendar>,
        persistedSelection: Set<Long>?,
    ): Set<Long> {
        val availableIds = calendars.mapTo(mutableSetOf(), DeviceCalendar::id)
        return persistedSelection
            ?.intersect(availableIds)
            ?: calendars
                .asSequence()
                .filter { it.isVisible && it.syncEvents }
                .map(DeviceCalendar::id)
                .toSet()
    }
}
