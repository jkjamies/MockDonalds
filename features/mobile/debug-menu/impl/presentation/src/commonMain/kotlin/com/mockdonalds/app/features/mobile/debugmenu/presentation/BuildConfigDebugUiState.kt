package com.mockdonalds.app.features.mobile.debugmenu.presentation

import com.mockdonalds.app.core.buildconfig.BuildConfigField
import com.slack.circuit.runtime.CircuitUiState

data class BuildConfigDebugUiState(
    val fields: List<BuildConfigField>,
    val eventSink: (BuildConfigDebugEvent) -> Unit,
) : CircuitUiState

sealed class BuildConfigDebugEvent {
    data object BackClicked : BuildConfigDebugEvent()
}
