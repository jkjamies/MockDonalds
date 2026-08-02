package com.jkjamies.sampleplatter.features.debugmenu.presentation

import androidx.compose.runtime.Composable
import com.jkjamies.sampleplatter.core.presentation.strata.rememberStrata
import com.jkjamies.sampleplatter.core.strata.StrataDispatchers
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.DebugMenuScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.FeatureFlagsDebugScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(DebugMenuScreen::class, AppScope::class)
@Inject
@Composable
fun DebugMenuPresenter(
    navigator: Navigator,
    dispatchers: StrataDispatchers,
): DebugMenuUiState {
    rememberStrata(dispatchers)

    val entries = listOf(
        DebugMenuEntry(
            id = "feature-flags",
            title = "Feature Flags",
            subtitle = "Inspect and override remote flags",
            target = FeatureFlagsDebugScreen,
        ),
        DebugMenuEntry(
            id = "build-config",
            title = "Build Config",
            subtitle = "Current environment, SDK versions, and build metadata",
            target = BuildConfigDebugScreen,
        ),
    )

    return DebugMenuUiState(
        entries = entries,
        eventSink = { event ->
            when (event) {
                is DebugMenuEvent.BackClicked -> navigator.pop()
                is DebugMenuEvent.EntryClicked -> {
                    entries.firstOrNull { it.id == event.id }?.let { navigator.goTo(it.target) }
                }
            }
        },
    )
}
