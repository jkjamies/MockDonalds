package com.jkjamies.sampleplatter.features.debugmenu.presentation

import com.jkjamies.sampleplatter.core.remoteconfig.FlagLifecycle
import com.slack.circuit.runtime.CircuitUiState

data class FeatureFlagsDebugUiState(
    val rows: List<FeatureFlagRow>,
    val eventSink: (FeatureFlagsDebugEvent) -> Unit,
) : CircuitUiState

data class FeatureFlagRow(
    val key: String,
    val description: String,
    val owner: String,
    val lifecycle: FlagLifecycle,
    val enabled: Boolean,
)

sealed class FeatureFlagsDebugEvent {
    data object BackClicked : FeatureFlagsDebugEvent()
}
