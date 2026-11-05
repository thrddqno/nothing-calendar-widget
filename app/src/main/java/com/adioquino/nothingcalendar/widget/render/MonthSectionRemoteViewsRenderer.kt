package com.adioquino.nothingcalendar.widget.render

import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.widget.presentation.WidgetMonthState
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MonthSectionRemoteViewsRenderer(
    private val locale: Locale = Locale.getDefault(),
) {
    fun render(
        views: RemoteViews,
        month: WidgetMonthState,
        primaryTextColor: Int,
        secondaryTextColor: Int,
        availableWidthDp: Int,
        dateTextSizeSp: Float,
    ) {
        views.setTextViewText(R.id.month_title, formatMonthTitle(month, availableWidthDp))
        views.setTextColor(R.id.month_title, primaryTextColor)
        renderWeekdays(views, month, secondaryTextColor)
        renderDays(views, month, primaryTextColor, dateTextSizeSp)
    }

    private fun renderWeekdays(views: RemoteViews, month: WidgetMonthState, textColor: Int) {
        WEEKDAY_VIEW_IDS.forEachIndexed { index, viewId ->
            val day = month.firstDayOfWeek.plus(index.toLong())
            views.setTextViewText(viewId, day.getDisplayName(TextStyle.NARROW, locale))
            views.setTextColor(viewId, textColor)
        }
    }

    private fun renderDays(
        views: RemoteViews,
        month: WidgetMonthState,
        textColor: Int,
        dateTextSizeSp: Float,
    ) {
        WEEK_ROW_IDS.forEachIndexed { index, rowId ->
            views.setViewVisibility(rowId, if (index < month.weekCount) View.VISIBLE else View.GONE)
        }
        DAY_VIEW_IDS.forEachIndexed { index, viewId ->
            val visibleCell = month.cells.getOrNull(index)?.takeIf { it.belongsToDisplayedMonth }
            val text = visibleCell?.let { cell ->
                MonthDayCellFormatter.format(cell.date.dayOfMonth, cell.indicators.count > 0)
            }.orEmpty()
            val styledText = SpannableString(text).apply {
                if (text.contains('\n')) {
                    setSpan(
                        RelativeSizeSpan(INDICATOR_TEXT_SCALE),
                        text.lastIndex,
                        text.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                    )
                }
            }
            views.setTextViewText(viewId, styledText)
            views.setTextColor(viewId, textColor)
            views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, dateTextSizeSp)
            views.setInt(
                viewId,
                "setBackgroundResource",
                if (visibleCell?.isToday == true) R.drawable.widget_month_today else 0,
            )
        }
    }

    private fun formatMonthTitle(month: WidgetMonthState, availableWidthDp: Int): String {
        val pattern = when {
            availableWidthDp >= WIDE_WIDTH_DP -> "MMMM uuuu"
            availableWidthDp >= MEDIUM_WIDTH_DP -> "MMM uuuu"
            else -> "MMM"
        }
        return month.displayedMonth.format(DateTimeFormatter.ofPattern(pattern, locale))
    }

    companion object {
        const val MEDIUM_WIDTH_DP = 140
        const val WIDE_WIDTH_DP = 180
        const val INDICATOR_TEXT_SCALE = 0.55f

        val WEEKDAY_VIEW_IDS = intArrayOf(
            R.id.month_weekday_0,
            R.id.month_weekday_1,
            R.id.month_weekday_2,
            R.id.month_weekday_3,
            R.id.month_weekday_4,
            R.id.month_weekday_5,
            R.id.month_weekday_6,
        )

        val WEEK_ROW_IDS = intArrayOf(
            R.id.month_week_0,
            R.id.month_week_1,
            R.id.month_week_2,
            R.id.month_week_3,
            R.id.month_week_4,
            R.id.month_week_5,
        )

        val DAY_VIEW_IDS = intArrayOf(
            R.id.month_day_0, R.id.month_day_1, R.id.month_day_2, R.id.month_day_3,
            R.id.month_day_4, R.id.month_day_5, R.id.month_day_6, R.id.month_day_7,
            R.id.month_day_8, R.id.month_day_9, R.id.month_day_10, R.id.month_day_11,
            R.id.month_day_12, R.id.month_day_13, R.id.month_day_14, R.id.month_day_15,
            R.id.month_day_16, R.id.month_day_17, R.id.month_day_18, R.id.month_day_19,
            R.id.month_day_20, R.id.month_day_21, R.id.month_day_22, R.id.month_day_23,
            R.id.month_day_24, R.id.month_day_25, R.id.month_day_26, R.id.month_day_27,
            R.id.month_day_28, R.id.month_day_29, R.id.month_day_30, R.id.month_day_31,
            R.id.month_day_32, R.id.month_day_33, R.id.month_day_34, R.id.month_day_35,
            R.id.month_day_36, R.id.month_day_37, R.id.month_day_38, R.id.month_day_39,
            R.id.month_day_40, R.id.month_day_41,
        )
    }
}
