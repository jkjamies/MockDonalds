package com.mockdonalds.app.features.rewards.presentation

import com.slack.circuit.runtime.CircuitUiState

data class RewardsUiState(
    val eventSink: (RewardsEvent) -> Unit,
) : CircuitUiState

sealed interface RewardsEvent
