package com.adioquino.nothingcalendar.calendar.data

sealed interface CalendarReadResult<out T> {
    data class Success<T>(val value: T) : CalendarReadResult<T>

    data object PermissionDenied : CalendarReadResult<Nothing>

    data class Failure(val reason: CalendarReadFailure) : CalendarReadResult<Nothing>
}

enum class CalendarReadFailure {
    PROVIDER_UNAVAILABLE,
    INVALID_QUERY,
}
