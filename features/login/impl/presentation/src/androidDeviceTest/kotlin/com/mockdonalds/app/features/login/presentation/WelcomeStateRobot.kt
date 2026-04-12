package com.mockdonalds.app.features.login.presentation

import com.mockdonalds.app.core.test.StateRobot

class WelcomeStateRobot : StateRobot<WelcomeUiState, WelcomeEvent>() {

    override fun defaultState() = WelcomeUiState(
        eventSink = createEventSink(),
    )
}
