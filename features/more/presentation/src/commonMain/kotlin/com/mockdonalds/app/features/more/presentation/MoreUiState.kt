package com.mockdonalds.app.features.more.presentation

import com.slack.circuit.runtime.CircuitUiState

data class MoreUiState(
    val eventSink: (MoreEvent) -> Unit,
) : CircuitUiState

sealed interface MoreEvent
