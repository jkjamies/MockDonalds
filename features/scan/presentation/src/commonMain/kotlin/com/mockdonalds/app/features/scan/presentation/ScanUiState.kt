package com.mockdonalds.app.features.scan.presentation

import com.slack.circuit.runtime.CircuitUiState

data class ScanUiState(
    val eventSink: (ScanEvent) -> Unit,
) : CircuitUiState

sealed interface ScanEvent
