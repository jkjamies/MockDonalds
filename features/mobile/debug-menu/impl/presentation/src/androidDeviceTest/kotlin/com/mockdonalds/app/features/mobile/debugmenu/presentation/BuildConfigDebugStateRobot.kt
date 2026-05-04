package com.mockdonalds.app.features.mobile.debugmenu.presentation

import com.mockdonalds.app.core.buildconfig.asFields
import com.mockdonalds.app.core.buildconfig.test.FakeAppBuildConfig
import com.mockdonalds.app.core.test.StateRobot

class BuildConfigDebugStateRobot : StateRobot<BuildConfigDebugUiState, BuildConfigDebugEvent>() {

    override fun defaultState() = BuildConfigDebugUiState(
        fields = FakeAppBuildConfig().asFields(),
        eventSink = createEventSink(),
    )
}
