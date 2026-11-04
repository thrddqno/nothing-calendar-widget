package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import com.adioquino.nothingcalendar.widget.presentation.WidgetDataModule
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object WidgetUpdateCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sequence = AtomicLong()
    private val generations = ConcurrentHashMap<Int, Long>()

    fun update(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        kind: WidgetKind,
        pendingResult: BroadcastReceiver.PendingResult,
    ) {
        val appContext = context.applicationContext
        val renderer = PlaceholderWidgetRenderingEngine(appContext)
        if (appWidgetIds.isEmpty()) {
            pendingResult.finish()
            return
        }
        val generation = sequence.incrementAndGet()
        appWidgetIds.forEach { appWidgetId ->
            generations[appWidgetId] = generation
            runCatching {
                renderer.render(appWidgetManager, appWidgetId, kind, WidgetLoadState.Loading)
            }
        }

        scope.launch {
            try {
                val state = try {
                    WidgetDataModule.loader(appContext).load()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    WidgetLoadState.ProviderFailure(CalendarReadFailure.PROVIDER_UNAVAILABLE)
                }
                appWidgetIds.forEach { appWidgetId ->
                    runCatching {
                        if (
                            generations[appWidgetId] == generation &&
                            appWidgetManager.getAppWidgetInfo(appWidgetId) != null
                        ) {
                            renderer.render(appWidgetManager, appWidgetId, kind, state)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    fun invalidate(appWidgetIds: IntArray) {
        appWidgetIds.forEach(generations::remove)
    }
}
