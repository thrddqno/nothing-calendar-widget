package com.adioquino.nothingcalendar.widget.render

import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import java.util.concurrent.ConcurrentHashMap

internal class WidgetStateCache {
    private val states = ConcurrentHashMap<Int, WidgetLoadState>()

    fun get(appWidgetId: Int): WidgetLoadState? = states[appWidgetId]

    fun put(appWidgetId: Int, state: WidgetLoadState) {
        if (state is WidgetLoadState.Ready || state is WidgetLoadState.NoUpcomingEvents) {
            states[appWidgetId] = state
        }
    }

    fun invalidate(appWidgetIds: IntArray) {
        appWidgetIds.forEach(states::remove)
    }
}
