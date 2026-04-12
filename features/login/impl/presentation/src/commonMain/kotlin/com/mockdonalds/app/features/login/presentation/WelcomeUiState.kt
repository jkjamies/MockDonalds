package com.mockdonalds.app.features.login.presentation

import com.slack.circuit.runtime.CircuitUiState

data class WelcomeUiState(
    val eventSink: (WelcomeEvent) -> Unit,
) : CircuitUiState

sealed class WelcomeEvent {
    data object ContinueClicked : WelcomeEvent()
}
