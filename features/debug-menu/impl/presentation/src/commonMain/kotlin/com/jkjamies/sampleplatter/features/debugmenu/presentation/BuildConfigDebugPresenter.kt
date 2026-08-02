package com.jkjamies.sampleplatter.features.debugmenu.presentation

import androidx.compose.runtime.Composable
import com.jkjamies.sampleplatter.core.buildconfig.AppBuildConfig
import com.jkjamies.sampleplatter.core.buildconfig.BuildConfigField
import com.jkjamies.sampleplatter.core.buildconfig.asFields
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(BuildConfigDebugScreen::class, AppScope::class)
@Inject
@Composable
fun BuildConfigDebugPresenter(
    navigator: Navigator,
    dispatchers: StrataDispatchers,
    buildConfig: AppBuildConfig,
): BuildConfigDebugUiState {
    rememberStrata(dispatchers)

    return BuildConfigDebugUiState(
        fields = buildConfig.asFields().filter { it.group != BuildConfigField.Group.Secrets },
        eventSink = { event ->
            when (event) {
                is BuildConfigDebugEvent.BackClicked -> navigator.pop()
            }
        },
    )
}
