package com.jkjamies.sampleplatter.features.debugmenu.presentation

import androidx.compose.runtime.Composable
import com.jkjamies.sampleplatter.core.centerpost.CenterPostDispatchers
import com.jkjamies.sampleplatter.core.presentation.centerpost.rememberCenterPost
import com.jkjamies.sampleplatter.core.presentation.remoteconfig.rememberFlag
import com.jkjamies.sampleplatter.core.remoteconfig.FeatureFlagDefinition
import com.jkjamies.sampleplatter.core.remoteconfig.RemoteConfigProvider
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.FeatureFlagsDebugScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(FeatureFlagsDebugScreen::class, AppScope::class)
@Inject
@Composable
fun FeatureFlagsDebugPresenter(
    navigator: Navigator,
    dispatchers: CenterPostDispatchers,
    definitions: Set<FeatureFlagDefinition>,
    remoteConfig: RemoteConfigProvider,
): FeatureFlagsDebugUiState {
    rememberCenterPost(dispatchers)

    val rows = definitions
        .sortedWith(compareBy({ it.owner }, { it.flag.key }))
        .map { def ->
            FeatureFlagRow(
                key = def.flag.key,
                description = def.description,
                owner = def.owner,
                lifecycle = def.lifecycle,
                enabled = remoteConfig.rememberFlag(def.flag).value,
            )
        }

    return FeatureFlagsDebugUiState(
        rows = rows,
        eventSink = { event ->
            when (event) {
                is FeatureFlagsDebugEvent.BackClicked -> navigator.pop()
            }
        },
    )
}
