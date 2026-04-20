package com.mockdonalds.app.features.debugmenu.presentation

import com.slack.circuit.runtime.CircuitUiState

data class FeatureFlagsDebugUiState(
    val eventSink: (FeatureFlagsDebugEvent) -> Unit,
) : CircuitUiState

sealed class FeatureFlagsDebugEvent {
    data object BackClicked : FeatureFlagsDebugEvent()
}
