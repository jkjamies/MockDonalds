package com.mockdonalds.app.features.mobile.debugmenu.presentation

import androidx.compose.runtime.Composable
import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.core.buildconfig.asFields
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.presentation.centerpost.rememberCenterPost
import com.mockdonalds.app.features.mobile.debugmenu.api.navigation.BuildConfigDebugScreen
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
    buildConfig: AppBuildConfig,
): BuildConfigDebugUiState {
    rememberCenterPost(dispatchers)

    return BuildConfigDebugUiState(
        fields = buildConfig.asFields(),
        eventSink = { event ->
            when (event) {
                is BuildConfigDebugEvent.BackClicked -> navigator.pop()
            }
        },
    )
}
