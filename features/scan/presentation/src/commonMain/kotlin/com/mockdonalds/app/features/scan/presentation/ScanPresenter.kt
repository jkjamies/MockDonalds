package com.mockdonalds.app.features.scan.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.features.scan.api.navigation.ScanScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope

@CircuitInject(ScanScreen::class, AppScope::class)
@Composable
fun ScanPresenter(
    navigator: Navigator,
): ScanUiState {
    return ScanUiState(
        eventSink = {},
    )
}
