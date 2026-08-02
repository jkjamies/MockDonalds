package com.jkjamies.sampleplatter.features.debugmenu.presentation

import com.jkjamies.sampleplatter.core.test.StateRobot
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.BuildConfigDebugScreen
import com.jkjamies.sampleplatter.features.debugmenu.api.navigation.FeatureFlagsDebugScreen

class DebugMenuStateRobot : StateRobot<DebugMenuUiState, DebugMenuEvent>() {

    override fun defaultState() = DebugMenuUiState(
        entries = listOf(
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
        ),
        eventSink = createEventSink(),
    )

    fun stateWithNoEntries() = defaultState().copy(
        entries = emptyList(),
        eventSink = createEventSink(),
    )
}
