package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import com.adioquino.nothingcalendar.calendar.data.AndroidCalendarPermissionChecker
import com.adioquino.nothingcalendar.calendar.data.CalendarReadFailure
import com.adioquino.nothingcalendar.widget.presentation.WidgetDataModule
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshotFactory
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale
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
    private val stateCache = WidgetStateCache()

    fun update(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        kind: WidgetKind,
        pendingResult: BroadcastReceiver.PendingResult,
    ) = updateInternal(
        context = context,
        appWidgetManager = appWidgetManager,
        appWidgetIds = appWidgetIds,
        kind = kind,
        pendingResult = pendingResult,
        reuseSnapshot = false,
    )

    fun resize(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        pendingResult: BroadcastReceiver.PendingResult,
    ) = updateInternal(
        context = context,
        appWidgetManager = appWidgetManager,
        appWidgetIds = intArrayOf(appWidgetId),
        kind = kind,
        pendingResult = pendingResult,
        reuseSnapshot = true,
    )

    private fun updateInternal(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        kind: WidgetKind,
        pendingResult: BroadcastReceiver.PendingResult,
        reuseSnapshot: Boolean,
    ) {
        val appContext = context.applicationContext
        val renderer: WidgetRenderingEngine = when (kind) {
            WidgetKind.MONTH -> MonthWidgetRenderingEngine(appContext)
            WidgetKind.AGENDA -> AgendaWidgetRenderingEngine(appContext)
            WidgetKind.COMBINED -> CombinedWidgetRenderingEngine(appContext)
            WidgetKind.TODAY_AGENDA -> TodayAgendaWidgetRenderingEngine(appContext)
        }
        if (appWidgetIds.isEmpty()) {
            pendingResult.finish()
            return
        }
        // A cached resize must not cancel an in-flight refresh; that refresh will render with the
        // new launcher options when it completes. Only loads claim a new generation.
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()
        val canReuseSnapshot = reuseSnapshot &&
            AndroidCalendarPermissionChecker(appContext).canReadCalendar()
        if (reuseSnapshot && !canReuseSnapshot) {
            stateCache.invalidate(appWidgetIds)
        }
        val cachedStates = if (canReuseSnapshot) {
            appWidgetIds.map { stateCache.getValid(it, now, zoneId) }
        } else {
            emptyList()
        }
        if (canReuseSnapshot && cachedStates.all { it != null }) {
            appWidgetIds.forEachIndexed { index, appWidgetId ->
                runCatching {
                    renderer.render(appWidgetManager, appWidgetId, kind, cachedStates[index]!!)
                }
            }
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
                    WidgetLoadState.ProviderFailure(
                        reason = CalendarReadFailure.PROVIDER_UNAVAILABLE,
                        snapshot = WidgetSnapshotFactory.empty(
                            now = Instant.now(),
                            zoneId = ZoneId.systemDefault(),
                            firstDayOfWeek = localeFirstDayOfWeek(),
                        ),
                    )
                }
                appWidgetIds.forEach { appWidgetId ->
                    runCatching {
                        if (
                            generations[appWidgetId] == generation &&
                            appWidgetManager.getAppWidgetInfo(appWidgetId) != null
                        ) {
                            stateCache.put(appWidgetId, state)
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
        stateCache.invalidate(appWidgetIds)
    }

    private fun localeFirstDayOfWeek(): DayOfWeek = when (
        WeekFields.of(Locale.getDefault()).firstDayOfWeek
    ) {
        DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }
}
