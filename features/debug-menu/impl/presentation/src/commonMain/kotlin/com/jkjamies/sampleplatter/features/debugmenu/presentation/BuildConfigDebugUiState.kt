package com.jkjamies.sampleplatter.features.debugmenu.presentation

import com.jkjamies.sampleplatter.core.buildconfig.BuildConfigField
import com.slack.circuit.runtime.CircuitUiState

data class BuildConfigDebugUiState(
    val fields: List<BuildConfigField>,
    val eventSink: (BuildConfigDebugEvent) -> Unit,
) : CircuitUiState

sealed class BuildConfigDebugEvent {
    data object BackClicked : BuildConfigDebugEvent()
}
