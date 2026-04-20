package com.mockdonalds.app.features.debugmenu.presentation

import com.mockdonalds.app.core.test.StateRobot

class BuildConfigDebugStateRobot : StateRobot<BuildConfigDebugUiState, BuildConfigDebugEvent>() {

    override fun defaultState() = BuildConfigDebugUiState(
        eventSink = createEventSink(),
    )
}
