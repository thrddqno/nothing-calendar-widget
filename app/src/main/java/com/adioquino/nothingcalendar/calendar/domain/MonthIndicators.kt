package com.adioquino.nothingcalendar.calendar.domain

import java.time.LocalDate
import java.time.YearMonth

data class MonthIndicators(
    val month: YearMonth,
    val days: Map<LocalDate, DayIndicators>,
)

data class DayIndicators(
    val count: Int,
    val calendarColors: Set<Int>,
)
