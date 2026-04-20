package com.mockdonalds.app.features.debugmenu.presentation

import com.slack.circuit.runtime.CircuitUiState

data class BuildConfigDebugUiState(
    val eventSink: (BuildConfigDebugEvent) -> Unit,
) : CircuitUiState

sealed class BuildConfigDebugEvent {
    data object BackClicked : BuildConfigDebugEvent()
}
