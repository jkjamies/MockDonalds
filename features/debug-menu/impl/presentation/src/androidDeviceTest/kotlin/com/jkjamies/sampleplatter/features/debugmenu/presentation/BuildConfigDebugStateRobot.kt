package com.jkjamies.sampleplatter.features.debugmenu.presentation

import com.jkjamies.sampleplatter.core.buildconfig.asFields
import com.jkjamies.sampleplatter.core.buildconfig.test.FakeAppBuildConfig
import com.jkjamies.sampleplatter.core.test.StateRobot

class BuildConfigDebugStateRobot : StateRobot<BuildConfigDebugUiState, BuildConfigDebugEvent>() {

    override fun defaultState() = BuildConfigDebugUiState(
        fields = FakeAppBuildConfig().asFields(),
        eventSink = createEventSink(),
    )
}
