package com.adioquino.nothingcalendar.widget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import com.adioquino.nothingcalendar.R
import com.adioquino.nothingcalendar.navigation.AppNavigation
import com.adioquino.nothingcalendar.theme.AndroidThemeResolver
import com.adioquino.nothingcalendar.theme.ThemeResolver
import com.adioquino.nothingcalendar.widget.agenda.AgendaCapacity
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import java.time.Instant

class AgendaWidgetRenderingEngine(
    private val context: Context,
    private val themeResolver: ThemeResolver = AndroidThemeResolver(context),
) : WidgetRenderingEngine {
    override fun render(
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        kind: WidgetKind,
        state: WidgetLoadState,
    ) {
        require(kind == WidgetKind.AGENDA) { "Agenda renderer only supports agenda widgets" }
        val palette = themeResolver.resolve()
        val snapshot = when (state) {
            is WidgetLoadState.Ready -> state.snapshot
            is WidgetLoadState.NoUpcomingEvents -> state.snapshot
            else -> null
        }
        val sizes = WidgetSizeResolver.resolve(
            appWidgetManager.getAppWidgetOptions(appWidgetId),
            WidgetSize(DEFAULT_SIZE_DP, DEFAULT_SIZE_DP),
        )
        val now = Instant.now()
        ResponsiveRemoteViews.update(appWidgetManager, appWidgetId, sizes) { size ->
            var spec = WidgetResponsiveSizingPolicy.calculate(
                WidgetSizingInput(
                    kind = WidgetKind.AGENDA,
                    availableWidthDp = size.widthDp,
                    availableHeightDp = size.heightDp,
                    fontScale = context.resources.configuration.fontScale,
                ),
            )
            val views = RemoteViews(context.packageName, R.layout.widget_agenda)
            views.setOnClickPendingIntent(R.id.widget_root, AppNavigation.settingsPendingIntent(context))
            views.setTextColor(R.id.agenda_header, palette.primaryText)
            val sectionRenderer = AgendaSectionRemoteViewsRenderer(context)
            if (snapshot == null) {
                sectionRenderer.renderMessage(views, state.message(), palette.secondaryText)
            } else {
                val model = createAgendaWidgetPresenter(context).presentWithCapacity(
                    occurrences = snapshot.agenda.occurrences,
                    now = now,
                ) { eligibleCount ->
                    spec = WidgetResponsiveSizingPolicy.calculate(
                        WidgetSizingInput(
                            kind = WidgetKind.AGENDA,
                            availableWidthDp = size.widthDp,
                            availableHeightDp = size.heightDp,
                            fontScale = context.resources.configuration.fontScale,
                            availableAgendaEventCount = eligibleCount,
                        ),
                    )
                    spec.agendaCapacity.toAgendaCapacity()
                }
                if (model.rows.isEmpty()) {
                    sectionRenderer.renderMessage(
                        views,
                        context.getString(R.string.widget_nothing_in_next_12_hours),
                        palette.secondaryText,
                    )
                } else {
                    sectionRenderer.renderEvents(
                        views,
                        model,
                        palette.background,
                        palette.primaryText,
                        palette.secondaryText,
                        eventTitleTextSizeSp = spec.eventTitleTextSizeSp,
                        eventMetaTextSizeSp = spec.eventMetaTextSizeSp,
                    )
                }
            }
            ResponsiveRemoteViews.applyLayout(
                views = views,
                kind = WidgetKind.AGENDA,
                spec = spec,
                density = context.resources.displayMetrics.density,
                agendaRowIds = AgendaSectionRemoteViewsRenderer.ROW_IDS.take(4).toIntArray(),
            )
            views
        }
    }

    private fun WidgetLoadState.message(): String = when (this) {
        WidgetLoadState.Loading -> context.getString(R.string.widget_loading)
        is WidgetLoadState.PermissionDenied -> context.getString(R.string.widget_calendar_access_required)
        is WidgetLoadState.NoAvailableCalendars -> context.getString(R.string.widget_no_calendars)
        is WidgetLoadState.ProviderFailure -> context.getString(R.string.widget_provider_failure)
        is WidgetLoadState.Ready, is WidgetLoadState.NoUpcomingEvents -> {
            context.getString(R.string.widget_nothing_in_next_12_hours)
        }
    }

    private fun ResponsiveAgendaCapacity.toAgendaCapacity() =
        AgendaCapacity(
            visibleEventRows = visibleEventRows,
            showOverflow = showOverflow,
            hiddenEventCount = hiddenEventCount,
        )

    private companion object {
        const val DEFAULT_SIZE_DP = 110
    }
}
