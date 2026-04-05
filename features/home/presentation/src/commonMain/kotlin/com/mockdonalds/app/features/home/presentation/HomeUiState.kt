package com.mockdonalds.app.features.home.presentation

import com.slack.circuit.runtime.CircuitUiState

data class HomeUiState(
    val eventSink: (HomeEvent) -> Unit,
) : CircuitUiState

sealed interface HomeEvent
