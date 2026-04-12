package com.mockdonalds.app.features.login.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.features.login.api.navigation.WelcomeScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(WelcomeScreen::class, AppScope::class)
@Inject
@Composable
fun WelcomePresenter(
    screen: WelcomeScreen,
    navigator: Navigator,
): WelcomeUiState {
    return WelcomeUiState(
        eventSink = { event ->
            when (event) {
                is WelcomeEvent.ContinueClicked -> {
                    navigator.pop()
                    navigator.pop()
                    screen.returnTo?.let { navigator.goTo(it) }
                }
            }
        },
    )
}
