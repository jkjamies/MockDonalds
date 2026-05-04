package com.mockdonalds.app.features.kiosk.attract.presentation

import com.mockdonalds.app.features.kiosk.attract.api.domain.Ad
import com.slack.circuit.runtime.CircuitUiState

data class AttractUiState(
    val ads: List<Ad>,
    val currentIndex: Int,
    val rotationSeconds: Int,
    val eventSink: (AttractEvent) -> Unit,
) : CircuitUiState
