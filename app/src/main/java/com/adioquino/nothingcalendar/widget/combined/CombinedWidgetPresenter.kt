package com.adioquino.nothingcalendar.widget.combined

import com.adioquino.nothingcalendar.widget.agenda.AgendaRenderModel
import com.adioquino.nothingcalendar.widget.agenda.AgendaWidgetPresenter
import com.adioquino.nothingcalendar.widget.presentation.WidgetLoadState
import com.adioquino.nothingcalendar.widget.presentation.WidgetMonthState
import com.adioquino.nothingcalendar.widget.presentation.WidgetSnapshot
import java.time.Instant

class CombinedWidgetPresenter(
    private val agendaPresenter: AgendaWidgetPresenter,
) {
    fun present(
        state: WidgetLoadState,
        loadingSnapshot: WidgetSnapshot,
        now: Instant,
        availableHeightDp: Int,
    ): CombinedRenderModel {
        val snapshot = state.snapshotOr(loadingSnapshot)
        val layout = CombinedLayoutPolicy.calculate(
            availableHeightDp = availableHeightDp,
            monthWeekCount = snapshot.month.weekCount,
        )
        val status = state.agendaStatus()
        val agenda = if (status == CombinedAgendaStatus.EVENTS) {
            agendaPresenter.presentWithinHeight(
                occurrences = snapshot.agenda.occurrences,
                now = now,
                availableContentHeightDp = layout.agendaContentHeightDp,
                rowHeightDp = CombinedLayoutPolicy.AGENDA_ROW_HEIGHT_DP,
                overflowHeightDp = CombinedLayoutPolicy.AGENDA_OVERFLOW_HEIGHT_DP,
                maxEventRows = CombinedLayoutPolicy.MAX_AGENDA_ROWS,
            )
        } else {
            AgendaRenderModel(
                rows = emptyList(),
                totalEligibleEvents = 0,
                hiddenEventCount = 0,
                showOverflow = false,
            )
        }
        return CombinedRenderModel(
            month = snapshot.month,
            agenda = agenda,
            agendaStatus = if (status == CombinedAgendaStatus.EVENTS && agenda.totalEligibleEvents == 0) {
                CombinedAgendaStatus.EMPTY
            } else {
                status
            },
            layout = layout,
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

    private fun WidgetLoadState.agendaStatus(): CombinedAgendaStatus = when (this) {
        WidgetLoadState.Loading -> CombinedAgendaStatus.LOADING
        is WidgetLoadState.Ready -> CombinedAgendaStatus.EVENTS
        is WidgetLoadState.NoUpcomingEvents -> CombinedAgendaStatus.EMPTY
        is WidgetLoadState.PermissionDenied -> CombinedAgendaStatus.PERMISSION_DENIED
        is WidgetLoadState.NoAvailableCalendars -> CombinedAgendaStatus.NO_CALENDARS
        is WidgetLoadState.ProviderFailure -> CombinedAgendaStatus.PROVIDER_FAILURE
    }
}

data class CombinedRenderModel(
    val month: WidgetMonthState,
    val agenda: AgendaRenderModel,
    val agendaStatus: CombinedAgendaStatus,
    val layout: CombinedLayoutCapacity,
)

enum class CombinedAgendaStatus {
    EVENTS,
    EMPTY,
    LOADING,
    PERMISSION_DENIED,
    NO_CALENDARS,
    PROVIDER_FAILURE,
}
