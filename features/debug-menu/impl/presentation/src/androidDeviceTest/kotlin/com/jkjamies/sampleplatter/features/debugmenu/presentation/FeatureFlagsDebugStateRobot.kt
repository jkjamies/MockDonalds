package com.jkjamies.sampleplatter.features.debugmenu.presentation

import com.jkjamies.sampleplatter.core.test.StateRobot

class FeatureFlagsDebugStateRobot : StateRobot<FeatureFlagsDebugUiState, FeatureFlagsDebugEvent>() {

    override fun defaultState() = FeatureFlagsDebugUiState(
        rows = emptyList(),
        eventSink = createEventSink(),
    )
}
