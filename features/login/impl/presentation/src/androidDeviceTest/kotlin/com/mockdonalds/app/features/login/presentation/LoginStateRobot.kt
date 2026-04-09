package com.mockdonalds.app.features.login.presentation

import com.mockdonalds.app.core.test.StateRobot

class LoginStateRobot : StateRobot<LoginUiState, LoginEvent>() {

    override fun defaultState() = LoginUiState(
        logoUrl = "",
        email = "",
        isLoading = false,
        errorMessage = null,
        eventSink = createEventSink(),
    )

    fun stateWithEmail(email: String) = defaultState().copy(
        email = email,
        eventSink = createEventSink(),
    )
}
