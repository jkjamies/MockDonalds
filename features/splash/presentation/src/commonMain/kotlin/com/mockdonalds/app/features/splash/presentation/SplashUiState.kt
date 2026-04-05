package com.mockdonalds.app.features.splash.presentation

import com.slack.circuit.runtime.CircuitUiState

data class SplashUiState(
    val eventSink: (SplashEvent) -> Unit,
) : CircuitUiState

sealed interface SplashEvent {
    data object AnimationComplete : SplashEvent
}
