package com.mockdonalds.app.features.login.presentation

import com.slack.circuit.runtime.CircuitUiState

data class LoginUiState(
    val logoUrl: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val eventSink: (LoginEvent) -> Unit,
) : CircuitUiState

sealed class LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent()
    data object SignInClicked : LoginEvent()
    data object AppleSignInClicked : LoginEvent()
    data object GoogleSignInClicked : LoginEvent()
}
