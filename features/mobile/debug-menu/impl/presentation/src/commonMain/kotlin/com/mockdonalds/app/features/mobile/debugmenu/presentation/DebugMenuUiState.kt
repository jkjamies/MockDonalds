package com.mockdonalds.app.features.mobile.debugmenu.presentation

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen

data class DebugMenuEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val target: Screen,
)

data class DebugMenuUiState(
    val entries: List<DebugMenuEntry>,
    val eventSink: (DebugMenuEvent) -> Unit,
) : CircuitUiState

sealed class DebugMenuEvent {
    data object BackClicked : DebugMenuEvent()
    data class EntryClicked(val id: String) : DebugMenuEvent()
}
