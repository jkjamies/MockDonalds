package com.mockdonalds.app.features.recents.presentation

import com.mockdonalds.app.features.recents.api.domain.RecentItem
import com.slack.circuit.runtime.CircuitUiState

sealed class RecentsUiState : CircuitUiState {
    abstract val eventSink: (RecentsEvent) -> Unit

    data class Loading(
        override val eventSink: (RecentsEvent) -> Unit,
    ) : RecentsUiState()

    data class Success(
        val items: List<RecentItem>,
        override val eventSink: (RecentsEvent) -> Unit,
    ) : RecentsUiState()

    data class Empty(
        override val eventSink: (RecentsEvent) -> Unit,
    ) : RecentsUiState()
}
