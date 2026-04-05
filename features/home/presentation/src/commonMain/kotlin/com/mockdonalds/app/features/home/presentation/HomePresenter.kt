package com.mockdonalds.app.features.home.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.features.home.api.navigation.HomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope

@CircuitInject(HomeScreen::class, AppScope::class)
@Composable
fun HomePresenter(
    navigator: Navigator,
): HomeUiState {
    return HomeUiState(
        eventSink = {},
    )
}
