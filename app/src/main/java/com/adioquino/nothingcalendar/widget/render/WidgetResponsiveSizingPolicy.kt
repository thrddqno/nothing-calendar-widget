package com.adioquino.nothingcalendar.widget.render

import kotlin.math.ceil
import kotlin.math.floor

object WidgetResponsiveSizingPolicy {
    fun calculate(input: WidgetSizingInput): WidgetLayoutSpec {
        require(input.availableWidthDp >= 0) { "Available width must not be negative" }
        require(input.availableHeightDp >= 0) { "Available height must not be negative" }
        require(input.fontScale > 0f) { "Font scale must be positive" }
        require(input.monthRowCount in 0..6) { "Month row count must be between zero and six" }
        require(input.availableAgendaEventCount >= 0) { "Event count must not be negative" }

        val width = (input.availableWidthDp - input.insets.horizontalDp).coerceAtLeast(0)
        val height = (input.availableHeightDp - input.insets.verticalDp).coerceAtLeast(0)
        val compactWidth = if (input.kind == WidgetKind.TODAY_AGENDA) 240 else 180
        val mode = when {
            width < compactWidth ||
                (height < 120 && input.kind != WidgetKind.TODAY_AGENDA) ||
                input.fontScale >= 1.3f -> WidgetLayoutMode.COMPACT
            width >= 300 && height >= 300 && input.fontScale <= 1.15f -> WidgetLayoutMode.EXPANDED
            else -> WidgetLayoutMode.NORMAL
        }
        val horizontalPadding = if (mode == WidgetLayoutMode.COMPACT) 4 else 6
        val verticalPadding = if (mode == WidgetLayoutMode.COMPACT) 4 else 6
        val metrics = metrics(input.kind, mode, input.fontScale)
        val monthRows = input.monthRowCount.coerceAtLeast(1)
        val monthRowHeight = when (input.kind) {
            WidgetKind.MONTH -> floor(
                (height - verticalPadding * 2 - metrics.headerHeightDp - metrics.weekdayHeaderHeightDp)
                    .coerceAtLeast(0) / monthRows.toDouble(),
            ).toInt().coerceAtLeast(1)
            WidgetKind.COMBINED -> {
                val fixedHeight = verticalPadding * 2 +
                    metrics.headerHeightDp + metrics.weekdayHeaderHeightDp +
                    metrics.sectionSpacingDp + metrics.agendaPanelVerticalInsetDp * 2 +
                    metrics.agendaHeaderHeightDp
                val minimumRowHeight = minOf(
                    metrics.minimumReadableCalendarRowHeightDp,
                    metrics.calendarRowHeightDp,
                )
                (metrics.calendarRowHeightDp downTo minimumRowHeight).maxWith(
                    compareBy<Int> { rowHeight ->
                        calculateAgendaCapacity(
                            contentHeightDp = (
                                height - fixedHeight - rowHeight * monthRows
                            ).coerceAtLeast(0),
                            eventCount = input.availableAgendaEventCount,
                            rowHeightDp = metrics.agendaRowHeightDp,
                            overflowHeightDp = metrics.overflowHeightDp,
                            maxRows = metrics.maxAgendaRows,
                        ).visibleEventRows
                    }.thenBy { it },
                )
            }
            else -> 0
        }
        val monthSectionHeight = if (input.monthRowCount == 0) {
            0
        } else {
            metrics.headerHeightDp + metrics.weekdayHeaderHeightDp + monthRowHeight * input.monthRowCount
        }
        val agendaContentHeight = when (input.kind) {
            WidgetKind.MONTH -> 0
            WidgetKind.AGENDA, WidgetKind.TODAY_AGENDA -> {
                height - verticalPadding * 2 - metrics.headerHeightDp
            }
            WidgetKind.COMBINED -> {
                height - verticalPadding * 2 - monthSectionHeight - metrics.sectionSpacingDp -
                    metrics.agendaPanelVerticalInsetDp * 2 - metrics.agendaHeaderHeightDp
            }
        }.coerceAtLeast(0)
        val agendaCapacity = calculateAgendaCapacity(
            contentHeightDp = agendaContentHeight,
            eventCount = input.availableAgendaEventCount,
            rowHeightDp = metrics.agendaRowHeightDp,
            overflowHeightDp = metrics.overflowHeightDp,
            maxRows = metrics.maxAgendaRows,
        )
        return WidgetLayoutSpec(
            mode = mode,
            horizontalPaddingDp = horizontalPadding,
            verticalPaddingDp = verticalPadding,
            headerHeightDp = metrics.headerHeightDp,
            weekdayHeaderHeightDp = metrics.weekdayHeaderHeightDp,
            monthHeadingTextSizeSp = metrics.monthHeadingTextSizeSp,
            sectionHeadingTextSizeSp = metrics.sectionHeadingTextSizeSp,
            displayTextSizeSp = metrics.displayTextSizeSp,
            weekdayTextSizeSp = metrics.weekdayTextSizeSp,
            dateTextSizeSp = metrics.dateTextSizeSp,
            calendarRowHeightDp = monthRowHeight,
            monthSectionHeightDp = monthSectionHeight,
            sectionSpacingDp = metrics.sectionSpacingDp,
            agendaPanelHorizontalInsetDp = metrics.agendaPanelHorizontalInsetDp,
            agendaPanelVerticalInsetDp = metrics.agendaPanelVerticalInsetDp,
            agendaHeaderHeightDp = metrics.agendaHeaderHeightDp,
            agendaContentHeightDp = agendaContentHeight,
            agendaRowHeightDp = metrics.agendaRowHeightDp,
            eventTitleTextSizeSp = metrics.eventTitleTextSizeSp,
            eventMetaTextSizeSp = metrics.eventMetaTextSizeSp,
            overflowHeightDp = metrics.overflowHeightDp,
            agendaCapacity = agendaCapacity,
            maxAgendaRows = metrics.maxAgendaRows,
            showWeekdayHeader = input.monthRowCount > 0,
            showAgendaHeader = input.kind != WidgetKind.MONTH && metrics.agendaHeaderHeightDp > 0,
        )
    }

    private fun calculateAgendaCapacity(
        contentHeightDp: Int,
        eventCount: Int,
        rowHeightDp: Int,
        overflowHeightDp: Int,
        maxRows: Int,
    ): ResponsiveAgendaCapacity {
        if (eventCount == 0 || rowHeightDp == 0 || maxRows == 0) {
            return ResponsiveAgendaCapacity(0, false, 0)
        }
        val fittingRows = (contentHeightDp / rowHeightDp).coerceIn(0, maxRows)
        var visibleRows = minOf(fittingRows, eventCount)
        var hiddenCount = (eventCount - visibleRows).coerceAtLeast(0)
        if (hiddenCount > 0 && contentHeightDp - visibleRows * rowHeightDp < overflowHeightDp) {
            visibleRows = (visibleRows - 1).coerceAtLeast(0)
            hiddenCount = eventCount - visibleRows
        }
        return ResponsiveAgendaCapacity(
            visibleEventRows = visibleRows,
            showOverflow = hiddenCount > 0 &&
                contentHeightDp - visibleRows * rowHeightDp >= overflowHeightDp,
            hiddenEventCount = hiddenCount,
        )
    }

    private fun metrics(kind: WidgetKind, mode: WidgetLayoutMode, fontScale: Float): Metrics {
        val largeFont = fontScale >= 1.15f
        fun scaled(base: Int): Int = ceil(base * fontScale.coerceAtLeast(1f)).toInt()
        val agendaRowHeight = when {
            fontScale >= 1.5f -> 42
            fontScale >= 1.3f -> 38
            largeFont -> 36
            kind == WidgetKind.TODAY_AGENDA -> 24
            kind == WidgetKind.COMBINED -> 32
            else -> 34
        }
        return when (kind) {
            WidgetKind.MONTH -> Metrics(
                headerHeightDp = scaled(if (mode == WidgetLayoutMode.COMPACT) 14 else 18),
                weekdayHeaderHeightDp = scaled(if (mode == WidgetLayoutMode.COMPACT) 10 else 12),
                monthHeadingTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 14f else 16f,
                weekdayTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 7f else 8f,
                dateTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 8f else 10f,
            )
            WidgetKind.AGENDA -> Metrics(
                headerHeightDp = scaled(18),
                agendaHeaderHeightDp = scaled(18),
                sectionHeadingTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 11f else 12f,
                agendaRowHeightDp = agendaRowHeight,
                overflowHeightDp = scaled(14),
                maxAgendaRows = 4,
                eventTitleTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 10f else 11f,
            )
            WidgetKind.TODAY_AGENDA -> Metrics(
                headerHeightDp = scaled(14),
                agendaHeaderHeightDp = scaled(14),
                monthHeadingTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 10f else 11f,
                sectionHeadingTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 9f else 10f,
                displayTextSizeSp = when {
                    fontScale >= 1.5f -> 30f
                    fontScale >= 1.3f -> 34f
                    fontScale >= 1.15f -> 40f
                    mode == WidgetLayoutMode.COMPACT -> 40f
                    else -> 48f
                },
                weekdayTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 10f else 11f,
                agendaRowHeightDp = agendaRowHeight,
                overflowHeightDp = scaled(12),
                maxAgendaRows = if (mode == WidgetLayoutMode.COMPACT || largeFont) 2 else 3,
                eventTitleTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 10f else 11f,
            )
            WidgetKind.COMBINED -> Metrics(
                headerHeightDp = scaled(20),
                weekdayHeaderHeightDp = scaled(12),
                monthHeadingTextSizeSp = when {
                    fontScale >= 1.5f -> 13f
                    fontScale >= 1.3f -> 14f
                    mode == WidgetLayoutMode.COMPACT -> 16f
                    else -> 18f
                },
                sectionHeadingTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 11f else 12f,
                weekdayTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 7f else 8f,
                dateTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 8f else 10f,
                calendarRowHeightDp = when {
                    fontScale >= 1.5f -> 34
                    fontScale >= 1.3f -> 30
                    largeFont -> 26
                    else -> 22
                },
                minimumReadableCalendarRowHeightDp = when {
                    fontScale >= 1.5f -> 16
                    fontScale >= 1.15f -> 15
                    else -> 14
                },
                sectionSpacingDp = 6,
                agendaPanelHorizontalInsetDp = 8,
                agendaPanelVerticalInsetDp = 2,
                agendaHeaderHeightDp = scaled(16),
                agendaRowHeightDp = agendaRowHeight,
                overflowHeightDp = scaled(14),
                maxAgendaRows = 6,
                eventTitleTextSizeSp = if (mode == WidgetLayoutMode.COMPACT) 10f else 11f,
            )
        }
    }

    private data class Metrics(
        val headerHeightDp: Int,
        val weekdayHeaderHeightDp: Int = 0,
        val monthHeadingTextSizeSp: Float = 0f,
        val sectionHeadingTextSizeSp: Float = 0f,
        val displayTextSizeSp: Float = 0f,
        val weekdayTextSizeSp: Float = 0f,
        val dateTextSizeSp: Float = 10f,
        val calendarRowHeightDp: Int = 0,
        val minimumReadableCalendarRowHeightDp: Int = 1,
        val sectionSpacingDp: Int = 0,
        val agendaPanelHorizontalInsetDp: Int = 0,
        val agendaPanelVerticalInsetDp: Int = 0,
        val agendaHeaderHeightDp: Int = 0,
        val agendaRowHeightDp: Int = 0,
        val overflowHeightDp: Int = 0,
        val maxAgendaRows: Int = 0,
        val eventTitleTextSizeSp: Float = 11f,
        val eventMetaTextSizeSp: Float = 8f,
    )
}

data class WidgetSizingInput(
    val kind: WidgetKind,
    val availableWidthDp: Int,
    val availableHeightDp: Int,
    val fontScale: Float,
    val monthRowCount: Int = 0,
    val availableAgendaEventCount: Int = 0,
    val insets: WidgetInsets = WidgetInsets(),
)

data class WidgetInsets(
    val startDp: Int = 0,
    val topDp: Int = 0,
    val endDp: Int = 0,
    val bottomDp: Int = 0,
) {
    init {
        require(startDp >= 0 && topDp >= 0 && endDp >= 0 && bottomDp >= 0) {
            "Widget insets must not be negative"
        }
    }

    val horizontalDp: Int get() = startDp + endDp
    val verticalDp: Int get() = topDp + bottomDp
}

enum class WidgetLayoutMode { COMPACT, NORMAL, EXPANDED }

data class ResponsiveAgendaCapacity(
    val visibleEventRows: Int,
    val showOverflow: Boolean,
    val hiddenEventCount: Int,
)

data class WidgetLayoutSpec(
    val mode: WidgetLayoutMode,
    val horizontalPaddingDp: Int,
    val verticalPaddingDp: Int,
    val headerHeightDp: Int,
    val weekdayHeaderHeightDp: Int,
    val monthHeadingTextSizeSp: Float,
    val sectionHeadingTextSizeSp: Float,
    val displayTextSizeSp: Float,
    val weekdayTextSizeSp: Float,
    val dateTextSizeSp: Float,
    val calendarRowHeightDp: Int,
    val monthSectionHeightDp: Int,
    val sectionSpacingDp: Int,
    val agendaPanelHorizontalInsetDp: Int,
    val agendaPanelVerticalInsetDp: Int,
    val agendaHeaderHeightDp: Int,
    val agendaContentHeightDp: Int,
    val agendaRowHeightDp: Int,
    val eventTitleTextSizeSp: Float,
    val eventMetaTextSizeSp: Float,
    val overflowHeightDp: Int,
    val agendaCapacity: ResponsiveAgendaCapacity,
    val maxAgendaRows: Int,
    val showWeekdayHeader: Boolean,
    val showAgendaHeader: Boolean,
)
