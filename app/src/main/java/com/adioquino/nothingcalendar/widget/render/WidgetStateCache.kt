package com.adioquino.nothingcalendar.widget.render

import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshot
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

internal class WidgetStateCache {
    private val states = ConcurrentHashMap<Int, WidgetLoadState>()

    fun get(appWidgetId: Int): WidgetLoadState? = states[appWidgetId]

    fun getValid(appWidgetId: Int, now: Instant, zoneId: ZoneId): WidgetLoadState? {
        val state = states[appWidgetId] ?: return null
        val snapshot = state.snapshot() ?: return null
        val currentMonth = YearMonth.from(now.atZone(zoneId))
        return state.takeIf {
            snapshot.generatedAt <= now && snapshot.month.displayedMonth == currentMonth
        }
    }

    fun put(appWidgetId: Int, state: WidgetLoadState) {
        if (state is WidgetLoadState.Ready || state is WidgetLoadState.NoUpcomingEvents) {
            states[appWidgetId] = state
        } else {
            states.remove(appWidgetId)
        }
    }

    fun invalidate(appWidgetIds: IntArray) {
        appWidgetIds.forEach(states::remove)
    }

    private fun WidgetLoadState.snapshot(): WidgetSnapshot? = when (this) {
        is WidgetLoadState.Ready -> snapshot
        is WidgetLoadState.NoUpcomingEvents -> snapshot
        else -> null
    }
}
