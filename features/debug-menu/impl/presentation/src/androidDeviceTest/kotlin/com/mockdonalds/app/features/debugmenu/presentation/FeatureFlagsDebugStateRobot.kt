package com.mockdonalds.app.features.debugmenu.presentation

import com.mockdonalds.app.core.test.StateRobot

class FeatureFlagsDebugStateRobot : StateRobot<FeatureFlagsDebugUiState, FeatureFlagsDebugEvent>() {

    override fun defaultState() = FeatureFlagsDebugUiState(
        rows = emptyList(),
        eventSink = createEventSink(),
    )
}
