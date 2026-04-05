package com.mockdonalds.app.features.order.presentation

import com.slack.circuit.runtime.CircuitUiState

data class OrderUiState(
    val eventSink: (OrderEvent) -> Unit,
) : CircuitUiState

sealed interface OrderEvent
