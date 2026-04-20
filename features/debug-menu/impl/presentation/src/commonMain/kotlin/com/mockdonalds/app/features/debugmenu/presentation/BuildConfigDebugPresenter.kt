package com.mockdonalds.app.features.debugmenu.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.rememberCenterPost
import com.mockdonalds.app.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(BuildConfigDebugScreen::class, AppScope::class)
@Inject
@Composable
fun BuildConfigDebugPresenter(
    navigator: Navigator,
    dispatchers: CenterPostDispatchers,
): BuildConfigDebugUiState {
    rememberCenterPost(dispatchers)

    return BuildConfigDebugUiState(
        eventSink = { event ->
            when (event) {
                is BuildConfigDebugEvent.BackClicked -> navigator.pop()
            }
        },
    )
}
