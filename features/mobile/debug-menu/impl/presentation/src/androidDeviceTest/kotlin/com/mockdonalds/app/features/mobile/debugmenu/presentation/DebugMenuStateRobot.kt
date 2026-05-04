package com.mockdonalds.app.features.mobile.debugmenu.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.mobile.debugmenu.api.navigation.BuildConfigDebugScreen
import com.mockdonalds.app.features.mobile.debugmenu.api.navigation.FeatureFlagsDebugScreen

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
