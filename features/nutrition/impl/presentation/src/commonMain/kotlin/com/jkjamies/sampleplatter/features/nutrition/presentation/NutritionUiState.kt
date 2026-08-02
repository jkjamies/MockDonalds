package com.jkjamies.sampleplatter.features.nutrition.presentation

import com.slack.circuit.runtime.CircuitUiState

data class NutritionUiState(
    val url: String?,
    val eventSink: (NutritionEvent) -> Unit,
) : CircuitUiState
