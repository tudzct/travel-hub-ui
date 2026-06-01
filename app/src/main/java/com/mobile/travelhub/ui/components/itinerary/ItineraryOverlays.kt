package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.runtime.Composable
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.viewmodels.ItineraryUiState

@Composable
fun ItinerarySharedOverlays(
    state: ItineraryUiState,
    onDismissDayEditor: () -> Unit,
    onSaveDay: (ItineraryDay) -> Unit,
    onDeleteEditingDay: () -> Unit,
    onDismissEventEditor: () -> Unit,
    onSaveEvent: (ItineraryEvent) -> Unit,
    onDeleteEditingEvent: () -> Unit
) {
    state.editingDay?.let { day ->
        ItineraryDayEditorDialog(
            day = day,
            onDismiss = onDismissDayEditor,
            onSave = onSaveDay,
            onDelete = onDeleteEditingDay
        )
    }

    state.editingEvent?.let { event ->
        ItineraryEventEditorDialog(
            event = event,
            dayCount = state.days.size,
            dayOptions = state.dayOptions,
            isCreating = state.isCreatingEvent,
            onDismiss = onDismissEventEditor,
            onSave = onSaveEvent,
            onDelete = onDeleteEditingEvent
        )
    }
}
