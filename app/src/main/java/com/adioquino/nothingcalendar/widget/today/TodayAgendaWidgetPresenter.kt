package com.adioquino.nothingcalendar.widget.today

import com.adioquino.nothingcalendar.widget.agenda.AgendaRenderModel
import com.adioquino.nothingcalendar.widget.agenda.AgendaWidgetPresenter
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshot
import java.time.Instant

class TodayAgendaWidgetPresenter(
    private val datePresenter: TodayDatePresenter,
    private val agendaPresenter: AgendaWidgetPresenter,
) {
    fun present(
        state: WidgetLoadState,
        loadingSnapshot: WidgetSnapshot,
        now: Instant,
        availableWidthDp: Int,
        availableHeightDp: Int,
        fontScale: Float,
    ): TodayAgendaRenderModel {
        val snapshot = state.snapshotOr(loadingSnapshot)
        val status = state.agendaStatus()
        var resolvedCapacity = TodayAgendaCapacityPolicy.calculate(
            availableWidthDp = availableWidthDp,
            availableHeightDp = availableHeightDp,
            fontScale = fontScale,
            eligibleEventCount = 0,
        )
        val agenda = if (status == TodayAgendaStatus.EVENTS) {
            agendaPresenter.presentWithCapacity(
                occurrences = snapshot.agenda.occurrences,
                now = now,
            ) { eligibleCount ->
                resolvedCapacity = TodayAgendaCapacityPolicy.calculate(
                    availableWidthDp = availableWidthDp,
                    availableHeightDp = availableHeightDp,
                    fontScale = fontScale,
                    eligibleEventCount = eligibleCount,
                )
                resolvedCapacity.asAgendaCapacity()
            }
        } else {
            emptyAgenda()
        }
        return TodayAgendaRenderModel(
            date = datePresenter.present(now),
            agenda = agenda,
            agendaStatus = if (status == TodayAgendaStatus.EVENTS && agenda.totalEligibleEvents == 0) {
                TodayAgendaStatus.EMPTY
            } else {
                status
            },
            capacity = resolvedCapacity,
        )
    }

    private fun WidgetLoadState.snapshotOr(loadingSnapshot: WidgetSnapshot): WidgetSnapshot = when (this) {
        WidgetLoadState.Loading -> loadingSnapshot
        is WidgetLoadState.Ready -> snapshot
        is WidgetLoadState.NoUpcomingEvents -> snapshot
        is WidgetLoadState.PermissionDenied -> snapshot
        is WidgetLoadState.NoAvailableCalendars -> snapshot
        is WidgetLoadState.ProviderFailure -> snapshot
    }

    private fun WidgetLoadState.agendaStatus(): TodayAgendaStatus = when (this) {
        WidgetLoadState.Loading -> TodayAgendaStatus.LOADING
        is WidgetLoadState.Ready -> TodayAgendaStatus.EVENTS
        is WidgetLoadState.NoUpcomingEvents -> TodayAgendaStatus.EMPTY
        is WidgetLoadState.PermissionDenied -> TodayAgendaStatus.PERMISSION_DENIED
        is WidgetLoadState.NoAvailableCalendars -> TodayAgendaStatus.NO_CALENDARS
        is WidgetLoadState.ProviderFailure -> TodayAgendaStatus.PROVIDER_FAILURE
    }

    private fun emptyAgenda() = AgendaRenderModel(
        rows = emptyList(),
        totalEligibleEvents = 0,
        hiddenEventCount = 0,
        showOverflow = false,
    )
}

data class TodayAgendaRenderModel(
    val date: TodayDateModel,
    val agenda: AgendaRenderModel,
    val agendaStatus: TodayAgendaStatus,
    val capacity: TodayAgendaCapacity,
)

enum class TodayAgendaStatus {
    EVENTS,
    EMPTY,
    LOADING,
    PERMISSION_DENIED,
    NO_CALENDARS,
    PROVIDER_FAILURE,
}
