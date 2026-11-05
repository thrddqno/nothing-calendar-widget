package com.adioquino.nothingcalendar.widget.render

object MonthDayCellFormatter {
    fun format(dayOfMonth: Int, hasIndicator: Boolean): MonthDayCellContent {
        require(dayOfMonth in 1..31) { "Day must be between 1 and 31" }
        return MonthDayCellContent(
            dateText = dayOfMonth.toString(),
            indicatorVisible = hasIndicator,
        )
    }
}

data class MonthDayCellContent(
    val dateText: String,
    val indicatorVisible: Boolean,
)
