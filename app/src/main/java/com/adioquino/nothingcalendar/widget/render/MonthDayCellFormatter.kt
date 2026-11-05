package com.adioquino.nothingcalendar.widget.render

object MonthDayCellFormatter {
    fun format(dayOfMonth: Int, hasIndicator: Boolean): String {
        require(dayOfMonth in 1..31) { "Day must be between 1 and 31" }
        return if (hasIndicator) "$dayOfMonth\n•" else dayOfMonth.toString()
    }
}
