package com.mockdonalds.app.features.more.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope

@CircuitInject(MoreScreen::class, AppScope::class)
@Composable
fun MorePresenter(
    navigator: Navigator,
): MoreUiState {
    return MoreUiState(
        eventSink = {},
    )
}
