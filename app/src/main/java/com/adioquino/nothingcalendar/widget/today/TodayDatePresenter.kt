package com.adioquino.nothingcalendar.widget.today

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TodayDatePresenter(
    private val locale: Locale,
    private val zoneId: ZoneId,
) {
    fun present(instant: Instant): TodayDateModel {
        val date = instant.atZone(zoneId).toLocalDate()
        return TodayDateModel(
            month = date.format(DateTimeFormatter.ofPattern("MMM", locale)).uppercase(locale),
            day = date.format(DateTimeFormatter.ofPattern("dd", locale)),
            weekday = date.format(DateTimeFormatter.ofPattern("EEE", locale)).uppercase(locale),
        )
    }
}

data class TodayDateModel(
    val month: String,
    val day: String,
    val weekday: String,
)
