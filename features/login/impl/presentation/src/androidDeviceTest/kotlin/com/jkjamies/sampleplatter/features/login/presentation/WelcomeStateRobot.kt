package com.jkjamies.sampleplatter.features.login.presentation

import com.jkjamies.sampleplatter.core.test.StateRobot

class WelcomeStateRobot : StateRobot<WelcomeUiState, WelcomeEvent>() {

    override fun defaultState() = WelcomeUiState(
        eventSink = createEventSink(),
    )
}
