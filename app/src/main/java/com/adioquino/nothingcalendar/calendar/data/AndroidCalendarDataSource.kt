package com.adioquino.nothingcalendar.calendar.data

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.provider.CalendarContract
import com.adioquino.nothingcalendar.calendar.domain.CalendarEvent
import com.adioquino.nothingcalendar.calendar.domain.CalendarQueryRange
import com.adioquino.nothingcalendar.calendar.domain.DeviceCalendar
import java.time.Instant

class AndroidCalendarDataSource(
    private val contentResolver: ContentResolver,
    private val permissionChecker: CalendarPermissionChecker,
) : CalendarDataSource {

    override fun queryCalendars(): ProviderQueryResult<List<DeviceCalendar>> = guardedQuery {
        contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            CALENDAR_PROJECTION,
            null,
            null,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " COLLATE NOCASE ASC",
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toDeviceCalendar())
            }
        }.orEmpty()
    }

    override fun queryInstances(
        range: CalendarQueryRange,
        calendarIds: Set<Long>,
    ): ProviderQueryResult<List<CalendarEvent>> {
        if (calendarIds.isEmpty()) return ProviderQueryResult.Success(emptyList())

        return guardedQuery {
            val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also { builder ->
                ContentUris.appendId(builder, range.startsAt.toEpochMilli())
                ContentUris.appendId(builder, range.endsAt.toEpochMilli())
            }.build()
            val placeholders = List(calendarIds.size) { "?" }.joinToString(",")
            val selection = "${CalendarContract.Instances.CALENDAR_ID} IN ($placeholders)"
            val selectionArgs = calendarIds.map(Long::toString).toTypedArray()

            contentResolver.query(
                uri,
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                "${CalendarContract.Instances.BEGIN} ASC, ${CalendarContract.Instances.END} ASC",
            )?.use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add(cursor.toCalendarEvent())
                }
            }.orEmpty()
        }
    }

    private inline fun <T> guardedQuery(query: () -> T): ProviderQueryResult<T> {
        if (!permissionChecker.canReadCalendar()) return ProviderQueryResult.PermissionDenied

        return try {
            ProviderQueryResult.Success(query())
        } catch (_: SecurityException) {
            ProviderQueryResult.PermissionDenied
        } catch (_: RuntimeException) {
            ProviderQueryResult.ProviderUnavailable
        }
    }

    private fun Cursor.toDeviceCalendar(): DeviceCalendar = DeviceCalendar(
        id = requireLong(CalendarContract.Calendars._ID),
        name = optionalString(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME).orEmpty(),
        accountName = optionalString(CalendarContract.Calendars.ACCOUNT_NAME).orEmpty(),
        accountType = optionalString(CalendarContract.Calendars.ACCOUNT_TYPE),
        color = optionalInt(CalendarContract.Calendars.CALENDAR_COLOR),
        isVisible = optionalInt(CalendarContract.Calendars.VISIBLE) == 1,
        syncEvents = optionalInt(CalendarContract.Calendars.SYNC_EVENTS) == 1,
    )

    private fun Cursor.toCalendarEvent(): CalendarEvent = CalendarEvent(
        eventId = requireLong(CalendarContract.Instances.EVENT_ID),
        originalEventId = optionalLong(CalendarContract.Events.ORIGINAL_ID),
        calendarId = requireLong(CalendarContract.Instances.CALENDAR_ID),
        title = optionalString(CalendarContract.Instances.TITLE).orEmpty(),
        startsAt = Instant.ofEpochMilli(requireLong(CalendarContract.Instances.BEGIN)),
        endsAt = Instant.ofEpochMilli(requireLong(CalendarContract.Instances.END)),
        isAllDay = optionalInt(CalendarContract.Instances.ALL_DAY) == 1,
        eventTimeZone = optionalString(CalendarContract.Instances.EVENT_TIMEZONE),
        calendarColor = optionalInt(CalendarContract.Instances.CALENDAR_COLOR),
    )

    private fun Cursor.requireLong(columnName: String): Long = getLong(getColumnIndexOrThrow(columnName))

    private fun Cursor.optionalLong(columnName: String): Long? = getColumnIndex(columnName)
        .takeIf { it >= 0 && !isNull(it) }
        ?.let(::getLong)

    private fun Cursor.optionalInt(columnName: String): Int? = getColumnIndex(columnName)
        .takeIf { it >= 0 && !isNull(it) }
        ?.let(::getInt)

    private fun Cursor.optionalString(columnName: String): String? = getColumnIndex(columnName)
        .takeIf { it >= 0 && !isNull(it) }
        ?.let(::getString)

    private companion object {
        val CALENDAR_PROJECTION = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.SYNC_EVENTS,
        )

        val INSTANCE_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Events.ORIGINAL_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_TIMEZONE,
            CalendarContract.Instances.CALENDAR_COLOR,
        )
    }
}
